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
    public static final String VERSION = "geometry-3";
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
        public final float x,y,top,bottom;
        public Point(float x,float y){this(x,y,y,y);}
        public Point(float x,float y,float top,float bottom){this.x=x;this.y=y;this.top=top;this.bottom=bottom;}
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
        public final String inputType;
        public final List<Point> tracePoints;
        public final String problem;
        public final long elapsedMs;
        public Result(int width,int height,List<Candle> candles,List<Match> matches,String problem,long elapsedMs) {
            this(width,height,candles,matches,Collections.emptyList(),"candles",problem,elapsedMs);
        }
        public Result(int width,int height,List<Candle> candles,List<Match> matches,List<Point> tracePoints,String inputType,String problem,long elapsedMs){
            this.inputType=inputType;this.tracePoints=Collections.unmodifiableList(new ArrayList<>(tracePoints));
            this.width=width; this.height=height; this.candles=Collections.unmodifiableList(new ArrayList<>(candles));
            this.matches=Collections.unmodifiableList(new ArrayList<>(matches)); this.problem=problem; this.elapsedMs=elapsedMs;
        }
        public boolean readable() { return problem==null; }
        public int observationCount(){return "trace".equals(inputType)?tracePoints.size():candles.size();}
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
            if(cw<4 || cw>Math.max(4,width/25) || ch<3 || ch>height*.65 || tail<6)continue;
            int[] rows=new int[ch];
            for(int i=0;i<tail;i++)rows[queue[i]/width-t]++;
            int bodyTop=-1,bodyBottom=-1,threshold=Math.max(2,(int)Math.ceil(cw*.68));
            for(int y=0;y<ch;y++)if(rows[y]>=threshold) { if(bodyTop<0)bodyTop=t+y;bodyBottom=t+y+1; }
            if(bodyTop<0 || bodyBottom-bodyTop<1)continue;
            boolean solid=true;
            for(int y=bodyTop;y<bodyBottom;y++)if(rows[y-t]<threshold){solid=false;break;}
            boolean bordered=true;
            for(int y=bodyTop;y<bodyBottom;y++){boolean leftEdge=false,rightEdge=false;for(int edge=0;edge<2;edge++){leftEdge|=color(pixels[y*width+Math.min(r,l+edge)])==c;rightEdge|=color(pixels[y*width+Math.max(l,r-edge)])==c;}if(!leftEdge||!rightEdge){bordered=false;break;}}
            if(!solid&&!bordered)continue;
            int reference=pixels[((bodyTop+bodyBottom-1)/2)*width+(l+r)/2],similar=0,total=0;
            for(int y=bodyTop;y<bodyBottom;y++)for(int x=l;x<=r;x++){
                total++;if(distance(pixels[y*width+x],reference)<80)similar++;
            }
            // Bilinear scaling can soften two edge columns of a narrow body.
            if(similar<total*.65&&!bordered)continue;
            float center=(l+r)/2f;
            boolean straight=true;
            for(int i=0;i<tail;i++) {
                int y=queue[i]/width;
                // Screen scaling can leave one partly covered body row outside
                // the solid core. It is not an off-center wick.
                if((y==bodyTop-1||y==bodyBottom)&&rows[y-t]>=Math.max(2,cw*.5))continue;
                if((y<bodyTop || y>=bodyBottom) && Math.abs(queue[i]%width-center)>Math.max(1.5,cw*.3)) { straight=false;break; }
            }
            if(!straight)continue;
            int outer=neutralOutline(pixels,width,height,l,r,bodyTop,bodyBottom);
            int top=Math.min(t,extendWick(pixels,width,height,l-outer,r+outer,bodyTop-outer,-1));
            int bottom=Math.max(b+1,extendWick(pixels,width,height,l-outer,r+outer,bodyBottom-1+outer,1)+1);
            if(bottom-top<Math.max(3,cw*.35))continue;
            candidates.add(new Candle(l-outer,r+1+outer,top,bottom,bodyTop,bodyBottom,c==1));
        }
        List<Candle> candles=selectSeries(candidates,width,height,cancellation);
        if(candles.size()<MIN_CANDLES || !paletteCoherent(candles,pixels,width)) return rejected(width,height,start,
                "No readable candle series found. Use at least 8 red/green or red/teal candles with visible wicks. Crop out menus or try another chart.");
        cancellation.check();
        List<Match> matches=recognize(candles,cancellation);
        cancellation.check();
        return new Result(width,height,candles,matches,null,(System.nanoTime()-start)/1_000_000);
    }

    public Result analyzeTrace(int w,int h,int[] px){return analyzeTrace(w,h,px,INTERRUPT);}
    public Result analyzeTrace(int w,int h,int[] px,Cancellation cancel){
        if(w<=0||h<=0||(long)w*h!=px.length||px.length>4_000_000)throw new IllegalArgumentException("Invalid trace image dimensions");long start=System.nanoTime();cancel.check();
        List<Point> points=w>=160&&h>=100?TraceReader.read(w,h,px,cancel):Collections.emptyList();
        if(points.size()<8)return new Result(w,h,Collections.emptyList(),Collections.emptyList(),Collections.emptyList(),"trace","No coherent price trace found. Crop to one clear price plot without indicator overlays, labels or menus.",(System.nanoTime()-start)/1_000_000);
        List<Match> shapes=recognize(traceAnchors(points,w,h),cancel);ArrayList<Match> matches=new ArrayList<>();for(Match m:shapes)if("Structure".equals(m.family))matches.add(m);cancel.check();
        return new Result(w,h,Collections.emptyList(),matches,points,"trace",null,(System.nanoTime()-start)/1_000_000);
    }
    public static List<Candle> traceAnchors(List<Point> points,int w,int h){ArrayList<Candle> anchors=new ArrayList<>();for(Point p:points){int x=Math.max(0,Math.min(w-1,Math.round(p.x))),y=Math.max(0,Math.min(h-2,Math.round(p.y)));anchors.add(new Candle(x,Math.min(w,x+1),Math.min(y,(int)p.top),Math.max(y+1,Math.min(h,(int)p.bottom+1)),y,y+1,true));}return anchors;}
    private static int neutralOutline(int[] px,int w,int h,int l,int r,int top,int bottom){
        for(int pad=1;pad<=3;pad++){
            if(l<pad+2||r+pad+2>=w||top<pad+1||bottom+pad>=h)continue;
            int ink=px[(top-pad)*w+(l+r)/2],bg=px[(top-pad)*w+l-pad-2];if(distance(ink,bg)<100)continue;
            boolean caps=true;for(int x=l-pad;x<=r+pad;x++)if(!darkBorder(px[(top-pad)*w+x],bg)||!darkBorder(px[(bottom+pad-1)*w+x],bg)){caps=false;break;}
            int mid=(top+bottom-1)/2;if(caps&&darkBorder(px[mid*w+l-pad],bg)&&darkBorder(px[mid*w+r+pad],bg)&&distance(px[mid*w+l-pad-2],bg)<60&&distance(px[mid*w+r+pad+2],bg)<60)return pad;
        }return 0;
    }
    private static boolean darkBorder(int pixel,int background){return Math.max((pixel>>16)&255,Math.max((pixel>>8)&255,pixel&255))<140&&distance(pixel,background)>100;}
    private static boolean paletteCoherent(List<Candle> candles,int[] px,int w){
        int coherent=0,total=0;
        for(boolean up:new boolean[]{true,false}){ArrayList<Integer> values=new ArrayList<>();
            for(Candle c:candles)if(c.bullish==up){int best=px[c.bodyTop*w+c.left],saturation=-1;for(int x=c.left;x<c.right;x++){int value=px[c.bodyTop*w+x],r=(value>>16)&255,g=(value>>8)&255,b=value&255,sat=Math.max(r,Math.max(g,b))-Math.min(r,Math.min(g,b));if(sat>saturation){saturation=sat;best=value;}}values.add(best);}
            if(values.isEmpty())continue;int[] rs=new int[values.size()],gs=rs.clone(),bs=rs.clone();for(int i=0;i<values.size();i++){int v=values.get(i);rs[i]=(v>>16)&255;gs[i]=(v>>8)&255;bs[i]=v&255;}Arrays.sort(rs);Arrays.sort(gs);Arrays.sort(bs);int mid=values.size()/2,median=0xff000000|(rs[mid]<<16)|(gs[mid]<<8)|bs[mid];for(int v:values)if(distance(v,median)<95)coherent++;total+=values.size();
        }return total>=MIN_CANDLES&&coherent>=total*.72;
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
        int best=from;
        // Resampling can put the wick one pixel beside the rounded body center.
        for(int offset=-1;offset<=1;offset++) {
            int found=extendColumn(px,w,h,l,r,Math.max(l,Math.min(r,(l+r)/2+offset)),from,direction);
            best=direction<0?Math.min(best,found):Math.max(best,found);
        }
        return best;
    }
    private static int extendColumn(int[] px,int w,int h,int l,int r,int x,int from,int direction) {
        int reach=Math.max(4,(r-l+1)/2+3),last=from,misses=0;
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
            if(line.size()<MIN_CANDLES)continue;
            float[] spacing=new float[line.size()-1];for(int i=1;i<line.size();i++)spacing[i-1]=line.get(i).x()-line.get(i-1).x();Arrays.sort(spacing);float typical=spacing[spacing.length/2];
            ArrayList<Candle> longest=new ArrayList<>(),part=new ArrayList<>();for(Candle candle:line){if(!part.isEmpty()&&candle.x()-part.get(part.size()-1).x()>typical*3.2){if(part.size()>longest.size())longest=part;part=new ArrayList<>();}part.add(candle);}if(part.size()>longest.size())longest=part;line=longest;
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
            int consistent=0;for(float gap:gaps)if(gap>=median*.55 && gap<=median*3.2 && Math.abs(gap/median-Math.round(gap/median))<.24)consistent++;
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
          // A large move elsewhere in the image must not hide a smaller formation.
          // Each pass uses the same geometric rules at a different swing scale.
          for(float fraction:new float[]{.10f,.18f,.26f,.34f}) {
            turns.clear();
            int direction=0,extreme=0,wick=0;
            float threshold=Math.max(4,span*fraction);
            for(int i=1;i<candles.size();i++){
                cancellation.check();float value=candles.get(i).middle(),extremeValue=candles.get(extreme).middle();
                if(direction==0){if(Math.abs(value-extremeValue)<threshold)continue;direction=value>extremeValue?1:-1;wick=0;for(int j=1;j<=i;j++)if(direction>0?candles.get(j).top<candles.get(wick).top:candles.get(j).bottom>candles.get(wick).bottom)wick=j;}
                if(direction>0){if(candles.get(i).top<candles.get(wick).top)wick=i;if(value>=extremeValue)extreme=i;else if(extremeValue-value>=threshold){turns.add(new Turn(wick,true,-candles.get(wick).top));direction=-1;extreme=wick=i;}}
                else{if(candles.get(i).bottom>candles.get(wick).bottom)wick=i;if(value<=extremeValue)extreme=i;else if(value-extremeValue>=threshold){turns.add(new Turn(wick,false,-candles.get(wick).bottom));direction=1;extreme=wick=i;}}
            }
            for(int i=0;i+2<turns.size();i++) {
                Turn a=turns.get(i),b=turns.get(i+1),c=turns.get(i+2);
                float shape=Math.abs(Math.max(a.value,c.value)-b.value);if(!a.high)shape=Math.abs(b.value-Math.min(a.value,c.value));
                if(c.index-a.index<5 || Math.abs(a.value-c.value)>Math.max(3,shape*.22))continue;
                if(a.high && c.high && Math.min(a.value,c.value)-b.value>Math.max(6,shape*.35))
                    add(out,candles,"double_top","Double top","Two comparable swing highs separated by a clear pullback. This is a visual structure; breakout and market-price confirmation are unavailable.",a.index,c.index,turnPoints(candles,a,b,c));
                if(!a.high && !c.high && b.value-Math.max(a.value,c.value)>Math.max(6,shape*.35))
                    add(out,candles,"double_bottom","Double bottom","Two comparable swing lows separated by a recovery. This is a visual structure; breakout and market-price confirmation are unavailable.",a.index,c.index,turnPoints(candles,a,b,c));
            }
            for(int i=0;i+4<turns.size();i++) {
                Turn a=turns.get(i),b=turns.get(i+1),c=turns.get(i+2),d=turns.get(i+3),e=turns.get(i+4);
                float local=Math.max(a.value,Math.max(c.value,e.value))-Math.min(b.value,d.value);if(!a.high)local=Math.max(b.value,d.value)-Math.min(a.value,Math.min(c.value,e.value));
                if(e.index-a.index<9 || local<12 || Math.abs(b.value-d.value)>local*.55)continue;
                // Compare shoulders above/below the sloping neckline.
                float tilt=(d.value-b.value)/(candles.get(d.index).x()-candles.get(b.index).x());
                float ah=a.value-b.value-tilt*(candles.get(a.index).x()-candles.get(b.index).x());
                float ch=c.value-b.value-tilt*(candles.get(c.index).x()-candles.get(b.index).x());
                float eh=e.value-b.value-tilt*(candles.get(e.index).x()-candles.get(b.index).x());
                float sign=a.high?1:-1;ah*=sign;ch*=sign;eh*=sign;
                if(Math.min(ah,eh)<local*.16 || Math.abs(ah-eh)>Math.max(ah,eh)*.40 || ch-Math.max(ah,eh)<local*.12)continue;
                if(a.high && c.high && e.high)
                    add(out,candles,"head_shoulders","Head & shoulders","A higher central peak sits between two comparable shoulders. The outline shows the measured swings, not a trading recommendation.",a.index,e.index,turnPoints(candles,a,b,c,d,e));
                if(!a.high && !c.high && !e.high)
                    add(out,candles,"inverse_head_shoulders","Inverse head & shoulders","A lower central trough sits between two comparable shoulders. The outline shows measured image geometry.",a.index,e.index,turnPoints(candles,a,b,c,d,e));
            }
            for(int begin=0;begin+3<turns.size();begin++)for(int end=begin+3;end<Math.min(turns.size(),begin+6);end++){
                ArrayList<Turn> highs=new ArrayList<>(),lows=new ArrayList<>();for(int i=begin;i<=end;i++){Turn t=turns.get(i);(t.high?highs:lows).add(t);}if(highs.size()<2||lows.size()<2)continue;
                Turn h1=highs.get(0),h2=highs.get(highs.size()-1),l1=lows.get(0),l2=lows.get(lows.size()-1);float local=Math.max(h1.value,h2.value)-Math.min(l1.value,l2.value),dh=h2.value-h1.value,dl=l2.value-l1.value;if(local<10)continue;
                int first=Math.min(h1.index,l1.index),last=Math.max(h2.index,l2.index);
                if(last-first<Math.max(8,candles.size()/10)||!triangleFits(candles,highs,lows,local))continue;
                List<Point> points=turnPoints(candles,h1,h2,l1,l2);
                if(Math.abs(dh)<local*.12&&dl>local*.14&&h2.value>l2.value)add(out,candles,"ascending_triangle","Ascending triangle","Approximately level swing highs and rising swing lows. Breakout and volume confirmation require market data.",first,last,points);
                else if(Math.abs(dl)<local*.12&&dh<-local*.14&&h2.value>l2.value)add(out,candles,"descending_triangle","Descending triangle","Approximately level swing lows and falling swing highs. Breakout and volume confirmation require market data.",first,last,points);
                else if(dh<-local*.12&&dl>local*.12&&h2.value>l2.value)add(out,candles,"symmetrical_triangle","Symmetrical triangle","Falling swing highs and rising swing lows form a narrowing range. Direction is not predicted.",first,last,points);
            }
          }
        }
        // Inspect every measured candle; do not silently ignore older visible formations.
        for(int i=0;i<candles.size();i++) {
            cancellation.check();Candle c=candles.get(i),p=i>0?candles.get(i-1):null;
            int upper=c.bodyTop-c.top,lower=c.bottom-c.bodyBottom;
            if(c.range()>=12 && c.body()/(float)c.range()<=.15f)
                addCandle(out,candles,"doji","Doji","The visible body is small relative to its full high–low range. The shape alone does not establish a reversal.",i,i);
            else if(lower>=c.body()*2 && upper<=Math.max(2,c.body()*.65))
                addCandle(out,candles,"hammer","Long lower wick","A small body sits near the top of a longer lower wick. Trend context is needed before calling this a hammer reversal.",i,i);
            else if(upper>=c.body()*2 && lower<=Math.max(2,c.body()*.65))
                addCandle(out,candles,"shooting_star","Long upper wick","A small body sits near the bottom of a longer upper wick. Trend context is needed before interpreting the formation.",i,i);
            if(p!=null && c.bullish!=p.bullish && c.body()>p.body()*1.15 && c.bodyTop<=p.bodyTop && c.bodyBottom>=p.bodyBottom)
                addCandle(out,candles,c.bullish?"bullish_engulfing":"bearish_engulfing",c.bullish?"Bullish engulfing body":"Bearish engulfing body",
                        "The latest body contains the preceding opposite-color body. Gaps, volume and market-price levels are not inferred.",i-1,i);
        }
        out.sort((a,b)->{int family=Boolean.compare("Candle".equals(a.family),"Candle".equals(b.family));return family!=0?family:Float.compare((b.right-b.left)*(b.bottom-b.top),(a.right-a.left)*(a.bottom-a.top));});
        ArrayList<Match> distinct=new ArrayList<>();
        for(Match candidate:out){
            String kind=candidate.id.substring(0,candidate.id.lastIndexOf('_',candidate.id.lastIndexOf('_')-1));
            boolean duplicate=false;
            for(Match saved:distinct)if(saved.id.startsWith(kind+"_")){
                float overlap=Math.max(0,Math.min(candidate.right,saved.right)-Math.max(candidate.left,saved.left));
                if(overlap>=Math.min(candidate.right-candidate.left,saved.right-saved.left)*.70f){duplicate=true;break;}
            }
            if(!duplicate)distinct.add(candidate);
        }
        return distinct.size()>10?new ArrayList<>(distinct.subList(0,10)):distinct;
    }

    private static boolean triangleFits(List<Candle> candles,List<Turn> highs,List<Turn> lows,float range){
        for(List<Turn> side:Arrays.asList(highs,lows)){
            Turn a=side.get(0),b=side.get(side.size()-1);float width=candles.get(b.index).x()-candles.get(a.index).x();
            if(width<=0)return false;
            for(Turn t:side){float expected=a.value+(b.value-a.value)*(candles.get(t.index).x()-candles.get(a.index).x())/width;if(Math.abs(t.value-expected)>range*.16)return false;}
        }
        return true;
    }

    private static List<Point> turnPoints(List<Candle> candles,Turn... turns) {
        ArrayList<Point> points=new ArrayList<>();
        for(Turn t:turns){Candle c=candles.get(t.index);points.add(new Point(c.x(),t.high?c.top:c.bottom));}
        return points;
    }
    private static void add(List<Match> out,List<Candle> c,String id,String title,String desc,int first,int last,List<Point> points) {
        for(Match m:out)if(m.id.startsWith(id+"_")&&m.first==first&&m.last==last)return;
        out.add(new Match(id,title,desc,"Structure",first,last,points,c));
    }
    private static void addCandle(List<Match> out,List<Candle> c,String id,String title,String desc,int first,int last) {
        Candle a=c.get(first),b=c.get(last);
        out.add(new Match(id,title,desc,"Candle",first,last,Arrays.asList(new Point(a.x(),a.top),new Point(b.x(),b.bottom)),c));
    }
}
