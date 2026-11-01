package com.lamontlabs.quantravision.next.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CancellationException;

/** Local, deterministic image geometry. Coordinates are pixels, never market prices. */
public final class ChartEngine {
    public interface Cancellation { void check(); }
    public static final String VERSION = "geometry-1";
    public static final int MIN_CANDLES = 8;
    private static final Cancellation INTERRUPT = () -> {
        if (Thread.currentThread().isInterrupted()) throw new CancellationException();
    };

    public static final class Candle {
        public final int left, right, top, bottom, bodyTop, bodyBottom;
        public final boolean bullish;
        public Candle(int left, int right, int top, int bottom, int bodyTop, int bodyBottom, boolean bullish) {
            if (left < 0 || right <= left || top < 0 || bodyTop < top || bodyBottom <= bodyTop || bottom < bodyBottom)
                throw new IllegalArgumentException("Invalid candle bounds");
            this.left=left; this.right=right; this.top=top; this.bottom=bottom;
            this.bodyTop=bodyTop; this.bodyBottom=bodyBottom; this.bullish=bullish;
        }
        public float x() { return (left+right)/2f; }
        public float middle() { return -(bodyTop+bodyBottom)/2f; }
        public int width() { return right-left; }
        public int body() { return bodyBottom-bodyTop; }
        public int range() { return bottom-top; }
        public boolean hasWick() { return bodyTop-top+bottom-bodyBottom >= 2; }
    }

    public static final class Point {
        public final float x,y;
        public Point(float x, float y) { this.x=x; this.y=y; }
    }

    public static final class Match {
        public final String id, title, description, family;
        public final List<Point> points;
        public final int first, last;
        public final float left, top, right, bottom;
        public Match(String id, String title, String description, String family, int first, int last,
                     List<Point> points, List<Candle> candles) {
            this.id=id+"_"+first+"_"+last; this.title=title; this.description=description; this.family=family;
            this.first=first; this.last=last; this.points=Collections.unmodifiableList(new ArrayList<>(points));
            float t=Float.MAX_VALUE,b=0;
            for(int i=first;i<=last;i++) { t=Math.min(t,candles.get(i).top); b=Math.max(b,candles.get(i).bottom); }
            this.left=candles.get(first).left; this.right=candles.get(last).right; this.top=t; this.bottom=b;
        }
    }

    public static final class Result {
        public final int width, height;
        public final List<Candle> candles;
        public final List<Match> matches;
        public final String problem;
        public final long elapsedMs;
        public Result(int width,int height,List<Candle> candles,List<Match> matches,String problem,long elapsedMs) {
            this.width=width; this.height=height; this.candles=Collections.unmodifiableList(new ArrayList<>(candles));
            this.matches=Collections.unmodifiableList(new ArrayList<>(matches)); this.problem=problem; this.elapsedMs=elapsedMs;
        }
        public boolean readable() { return problem==null; }
    }

    public Result analyze(int width, int height, int[] pixels) { return analyze(width,height,pixels,INTERRUPT); }

    public Result analyze(int width,int height,int[] pixels,Cancellation cancellation) {
        if(width<=0 || height<=0 || (long)width*height != pixels.length || pixels.length>4_000_000)
            throw new IllegalArgumentException("Image must contain 1–4,000,000 pixels");
        long start=System.nanoTime();
        cancellation.check();
        if(width<160 || height<100) return rejected(width,height,start,"This image is too small. Choose a larger chart screenshot.");
        byte[] colors=new byte[pixels.length];
        for(int i=0;i<pixels.length;i++) { if(i%16384==0)cancellation.check(); colors[i]=color(pixels[i]); }
        int[] queue=new int[pixels.length];
        ArrayList<Candle> candidates=new ArrayList<>();
        for(int s=0;s<colors.length;s++) {
            if(s%16384==0)cancellation.check();
            byte c=colors[s]; if(c==0)continue;
            int head=0,tail=1,l=width,r=0,t=height,b=0; queue[0]=s; colors[s]=0;
            while(head<tail) {
                if(head%8192==0)cancellation.check();
                int p=queue[head++],x=p%width,y=p/width;
                l=Math.min(l,x);r=Math.max(r,x);t=Math.min(t,y);b=Math.max(b,y);
                if(x>0 && colors[p-1]==c) { colors[p-1]=0;queue[tail++]=p-1; }
                if(x+1<width && colors[p+1]==c) { colors[p+1]=0;queue[tail++]=p+1; }
                if(y>0 && colors[p-width]==c) { colors[p-width]=0;queue[tail++]=p-width; }
                if(y+1<height && colors[p+width]==c) { colors[p+width]=0;queue[tail++]=p+width; }
            }
            int cw=r-l+1,ch=b-t+1;
            if(cw<2 || cw>Math.max(4,width/25) || ch<3 || ch>height*.65 || tail<6)continue;
            int[] rows=new int[ch];
            for(int i=0;i<tail;i++)rows[queue[i]/width-t]++;
            int bodyTop=-1,bodyBottom=-1,threshold=Math.max(2,(int)Math.ceil(cw*.68));
            for(int y=0;y<ch;y++)if(rows[y]>=threshold) { if(bodyTop<0)bodyTop=t+y;bodyBottom=t+y+1; }
            if(bodyTop<0 || bodyBottom-bodyTop<1)continue;
            boolean solid=true;
            for(int y=bodyTop;y<bodyBottom;y++)if(rows[y-t]<threshold){solid=false;break;}
            if(!solid)continue;
            int reference=pixels[((bodyTop+bodyBottom-1)/2)*width+(l+r)/2],similar=0,total=0;
            for(int y=bodyTop;y<bodyBottom;y++)for(int x=l;x<=r;x++){
                total++;if(distance(pixels[y*width+x],reference)<80)similar++;
            }
            if(similar<total*.72)continue;
            float center=(l+r)/2f;
            boolean straight=true;
            for(int i=0;i<tail;i++) {
                int y=queue[i]/width;
                if((y<bodyTop || y>=bodyBottom) && Math.abs(queue[i]%width-center)>Math.max(1.5,cw*.3)) { straight=false;break; }
            }
            if(!straight)continue;
            int top=Math.min(t,extendWick(pixels,width,height,l,r,bodyTop,-1));
            int bottom=Math.max(b+1,extendWick(pixels,width,height,l,r,bodyBottom-1,1)+1);
            if(bottom-top<Math.max(5,cw*1.25))continue;
            candidates.add(new Candle(l,r+1,top,bottom,bodyTop,bodyBottom,c==1));
        }
        List<Candle> candles=selectSeries(candidates,width,height,cancellation);
        if(candles.size()<MIN_CANDLES) return rejected(width,height,start,
                "No readable candle series found. Use at least 8 red/green or red/teal candles with visible wicks. Crop out menus or try another chart.");
        cancellation.check();
        List<Match> matches=recognize(candles,cancellation);
        cancellation.check();
        return new Result(width,height,candles,matches,null,(System.nanoTime()-start)/1_000_000);
    }

    private Result rejected(int w,int h,long start,String message) {
        return new Result(w,h,Collections.emptyList(),Collections.emptyList(),message,(System.nanoTime()-start)/1_000_000);
    }

    private static byte color(int argb) {
        if((argb>>>24)<160)return 0;
        int r=(argb>>16)&255,g=(argb>>8)&255,b=argb&255;
        if(Math.max(r,Math.max(g,b))<65 || Math.max(r,Math.max(g,b))-Math.min(r,Math.min(g,b))<35)return 0;
        if(g>r*1.18 && g>b*.76)return 1;
        if(r>g*1.18 && r>b*1.08)return 2;
        return 0;
    }

    /** Accept a thin neutral or colored wick, but stop at a full-width grid line. */
    private static int extendWick(int[] px,int w,int h,int l,int r,int from,int direction) {
        int x=(l+r)/2,reach=Math.max(4,(r-l+1)/2+3),last=from,misses=0;
        for(int step=1;step<Math.min(h/3,240);step++) {
            int y=from+step*direction;if(y<0||y>=h)break;
            int bg1=px[y*w+Math.max(0,x-reach)],bg2=px[y*w+Math.min(w-1,x+reach)];
            int center=px[y*w+x];
            boolean contrast=distance(bg1,bg2)<45 && distance(center,bg1)>65 && distance(center,bg2)>65;
            if(contrast) {
                int edge=px[y*w+Math.max(0,l-1)];
                if(distance(edge,center)<35 && (r-l)>3)break;
                last=y;misses=0;
            } else if(++misses>1)break;
        }
        return last;
    }

    private static int distance(int a,int b) {
        return Math.abs(((a>>16)&255)-((b>>16)&255))+Math.abs(((a>>8)&255)-((b>>8)&255))+Math.abs((a&255)-(b&255));
    }

    private static List<Candle> selectSeries(List<Candle> candidates,int width,int height,Cancellation cancel) {
        List<Candle> best=Collections.emptyList();double bestScore=0;
        boolean[] tried=new boolean[Math.max(width+1,2)];
        for(Candle anchor:candidates) {
            cancel.check();int aw=anchor.width();if(tried[aw])continue;tried[aw]=true;
            ArrayList<Candle> same=new ArrayList<>();
            for(Candle c:candidates)if(Math.abs(c.width()-aw)<=Math.max(1,aw*.3))same.add(c);
            same.sort(Comparator.comparingDouble(Candle::x));
            ArrayList<Candle> line=new ArrayList<>();
            for(Candle c:same) {
                if(line.isEmpty() || c.left>=line.get(line.size()-1).right)line.add(c);
                else {
                    Candle prev=line.get(line.size()-1);
                    if((c.hasWick()?1000:0)+c.range()>(prev.hasWick()?1000:0)+prev.range())line.set(line.size()-1,c);
                }
            }
            if(line.size()<MIN_CANDLES || line.get(line.size()-1).right-line.get(0).left<width*.2)continue;
            int wickCount=0;for(Candle c:line)if(c.hasWick())wickCount++;
            if(wickCount<line.size()*.45)continue;
            // Volume bars share a common baseline; candles usually do not.
            int baseline=0;
            for(Candle c:line) { int n=0;for(Candle d:line)if(Math.abs(c.bottom-d.bottom)<=2)n++;baseline=Math.max(baseline,n); }
            if(baseline>=line.size()*.75)continue;
            float[] gaps=new float[line.size()-1];
            for(int i=1;i<line.size();i++)gaps[i-1]=line.get(i).x()-line.get(i-1).x();
            float[] ordered=gaps.clone();Arrays.sort(ordered);float median=ordered[ordered.length/2];
            int consistent=0;for(float gap:gaps)if(gap>=median*.55 && gap<=median*1.8)consistent++;
            if(consistent<gaps.length*.72)continue;
            double score=line.size()+wickCount*.7+(line.get(line.size()-1).right-line.get(0).left)/(double)width*5;
            if(score>bestScore){bestScore=score;best=line;}
        }
        return best.size()>250 ? new ArrayList<>(best.subList(best.size()-250,best.size())):best;
    }

    private static final class Turn {
        final int index;final boolean high;final float value;
        Turn(int index,boolean high,float value){this.index=index;this.high=high;this.value=value;}
    }

    /** Geometric rules. Their fit is not a probability of a future market move. */
    public List<Match> recognize(List<Candle> candles,Cancellation cancellation) {
        ArrayList<Match> out=new ArrayList<>();if(candles.size()<3)return out;
        float max=-Float.MAX_VALUE,min=Float.MAX_VALUE;
        for(Candle c:candles){max=Math.max(max,c.middle());min=Math.min(min,c.middle());}
        float span=max-min;
        List<Turn> turns=new ArrayList<>();
        if(span>=12) {
            int direction=0,extreme=0;
            float threshold=Math.max(4,span*.10f);
            for(int i=1;i<candles.size();i++) {
                cancellation.check();float p=candles.get(i).middle(),ep=candles.get(extreme).middle();
                if(direction==0){if(Math.abs(p-ep)>=threshold)direction=p>ep?1:-1;else continue;}
                if(direction>0) {
                    if(p>=ep)extreme=i;
                    else if(ep-p>=threshold){turns.add(new Turn(extreme,true,ep));direction=-1;extreme=i;}
                } else {
                    if(p<=ep)extreme=i;
                    else if(p-ep>=threshold){turns.add(new Turn(extreme,false,ep));direction=1;extreme=i;}
                }
            }
            for(int i=Math.max(0,turns.size()-12);i+2<turns.size();i++) {
                Turn a=turns.get(i),b=turns.get(i+1),c=turns.get(i+2);
                if(c.index-a.index<5 || Math.abs(a.value-c.value)>span*.13)continue;
                if(a.high && c.high && Math.min(a.value,c.value)-b.value>span*.22)
                    add(out,candles,"double_top","Double top","Two comparable swing highs separated by a clear pullback. This is a visual structure; breakout and market-price confirmation are unavailable.",a.index,c.index,turnPoints(candles,a,b,c));
                if(!a.high && !c.high && b.value-Math.max(a.value,c.value)>span*.22)
                    add(out,candles,"double_bottom","Double bottom","Two comparable swing lows separated by a recovery. This is a visual structure; breakout and market-price confirmation are unavailable.",a.index,c.index,turnPoints(candles,a,b,c));
            }
            for(int i=Math.max(0,turns.size()-10);i+4<turns.size();i++) {
                Turn a=turns.get(i),b=turns.get(i+1),c=turns.get(i+2),d=turns.get(i+3),e=turns.get(i+4);
                if(e.index-a.index<9 || Math.abs(a.value-e.value)>span*.15 || Math.abs(b.value-d.value)>span*.22)continue;
                if(a.high && c.high && e.high && c.value-Math.max(a.value,e.value)>span*.12)
                    add(out,candles,"head_shoulders","Head & shoulders","A higher central peak sits between two comparable shoulders. The outline shows the measured swings, not a trading recommendation.",a.index,e.index,turnPoints(candles,a,b,c,d,e));
                if(!a.high && !c.high && !e.high && Math.min(a.value,e.value)-c.value>span*.12)
                    add(out,candles,"inverse_head_shoulders","Inverse head & shoulders","A lower central trough sits between two comparable shoulders. The outline shows measured image geometry.",a.index,e.index,turnPoints(candles,a,b,c,d,e));
            }
            if(turns.size()>=4) {
                ArrayList<Turn> highs=new ArrayList<>(),lows=new ArrayList<>();
                int start=Math.max(0,turns.size()-6);
                for(int i=start;i<turns.size();i++){Turn t=turns.get(i);(t.high?highs:lows).add(t);}
                if(highs.size()>=2 && lows.size()>=2) {
                    Turn h1=highs.get(0),h2=highs.get(highs.size()-1),l1=lows.get(0),l2=lows.get(lows.size()-1);
                    float dh=h2.value-h1.value,dl=l2.value-l1.value;
                    int first=Math.min(h1.index,l1.index),last=Math.max(h2.index,l2.index);
                    List<Point> points=turnPoints(candles,h1,h2,l2,l1);
                    if(Math.abs(dh)<span*.10 && dl>span*.14 && h2.value>l2.value)
                        add(out,candles,"ascending_triangle","Ascending triangle","Swing highs are approximately level while swing lows rise. Volume and breakout confirmation require market data.",first,last,points);
                    else if(Math.abs(dl)<span*.10 && dh<-span*.14 && h2.value>l2.value)
                        add(out,candles,"descending_triangle","Descending triangle","Swing lows are approximately level while swing highs fall. Volume and breakout confirmation require market data.",first,last,points);
                    else if(dh<-span*.12 && dl>span*.12 && h2.value>l2.value)
                        add(out,candles,"symmetrical_triangle","Symmetrical triangle","Falling swing highs and rising swing lows form a narrowing structure. Direction is not predicted from the screenshot.",first,last,points);
                }
            }
        }
        // Candle formations are limited to the latest five visible candles.
        for(int i=Math.max(1,candles.size()-5);i<candles.size();i++) {
            cancellation.check();Candle c=candles.get(i),p=candles.get(i-1);
            int upper=c.bodyTop-c.top,lower=c.bottom-c.bodyBottom;
            if(c.range()>=12 && c.body()/(float)c.range()<=.15f)
                addCandle(out,candles,"doji","Doji","The visible body is small relative to its full high–low range. The shape alone does not establish a reversal.",i,i);
            else if(lower>=c.body()*2 && upper<=Math.max(2,c.body()*.65))
                addCandle(out,candles,"hammer","Long lower wick","A small body sits near the top of a longer lower wick. Trend context is needed before calling this a hammer reversal.",i,i);
            else if(upper>=c.body()*2 && lower<=Math.max(2,c.body()*.65))
                addCandle(out,candles,"shooting_star","Long upper wick","A small body sits near the bottom of a longer upper wick. Trend context is needed before interpreting the formation.",i,i);
            if(c.bullish!=p.bullish && c.body()>p.body()*1.15 && c.bodyTop<=p.bodyTop && c.bodyBottom>=p.bodyBottom)
                addCandle(out,candles,c.bullish?"bullish_engulfing":"bearish_engulfing",c.bullish?"Bullish engulfing body":"Bearish engulfing body",
                        "The latest body contains the preceding opposite-color body. Gaps, volume and market-price levels are not inferred.",i-1,i);
        }
        if(out.size()>10)return new ArrayList<>(out.subList(0,10));
        return out;
    }

    private static List<Point> turnPoints(List<Candle> candles,Turn... turns) {
        ArrayList<Point> points=new ArrayList<>();
        for(Turn t:turns){Candle c=candles.get(t.index);points.add(new Point(c.x(),t.high?c.top:c.bottom));}
        return points;
    }
    private static void add(List<Match> out,List<Candle> c,String id,String title,String desc,int first,int last,List<Point> points) {
        out.add(new Match(id,title,desc,"Structure",first,last,points,c));
    }
    private static void addCandle(List<Match> out,List<Candle> c,String id,String title,String desc,int first,int last) {
        Candle a=c.get(first),b=c.get(last);
        out.add(new Match(id,title,desc,"Candle",first,last,Arrays.asList(new Point(a.x(),a.top),new Point(b.x(),b.bottom)),c));
    }
}
