package com.lamontlabs.quantravision.next.engine;

import java.util.*;

/** Experimental single-plot reader. Samples actual foreground pixels, never OHLC or prices. */
final class TraceReader {
    static List<ChartEngine.Point> read(int w,int h,int[] px,ChartEngine.Cancellation cancel){
        int[] histogram=new int[4096];for(int i=0;i<px.length;i+=3){int v=px[i];histogram[((v>>12)&0xf00)|((v>>8)&0xf0)|((v>>4)&15)]++;}int bin=0;for(int i=1;i<histogram.length;i++)if(histogram[i]>histogram[bin])bin=i;int bg=0xff000000|(((bin>>8)*16+8)<<16)|((((bin>>4)&15)*16+8)<<8)|((bin&15)*16+8);
        boolean[] ink=new boolean[px.length];int[] rows=new int[h],cols=new int[w];int pixels=0;
        for(int y=7;y<h;y++){cancel.check();for(int x=0;x<w;x++){int i=y*w+x,v=px[i];if((v>>>24)>=160&&distance(v,bg)>120){ink[i]=true;rows[y]++;cols[x]++;pixels++;}}}
        if(pixels<30||pixels>px.length*.55)return Collections.emptyList();
        for(int y=0;y<h;y++)for(int x=0;x<w;x++)if(rows[y]>w*.45||cols[x]>h*.55)ink[y*w+x]=false;
        for(int y=0;y<h;y++){int start=-1,miss=0;for(int x=0;x<=w;x++){boolean mark=x<w&&ink[y*w+x];if(mark){if(start<0)start=x;miss=0;}else if(start>=0&&(++miss>2||x==w)){int end=x-miss+1;if(end-start>w*.20)for(int k=start;k<end;k++)ink[y*w+k]=false;start=-1;miss=0;}}}
        removeRulings(ink,px,w,h,cancel);
        int stride=Math.max(1,w/320),count=(w+stride-1)/stride;float[] previous=new float[h],next=new float[h],best=new float[h];int[] origin=new int[h];short[][] back=new short[count][h];
        Arrays.fill(previous,0);final float slope=.22f;
        for(int k=0;k<count;k++){cancel.check();int left=k*stride,right=Math.min(w,left+stride);
            for(int y=0;y<h;y++){best[y]=previous[y];origin[y]=y;}
            for(int y=1;y<h;y++)if(best[y-1]-slope>best[y]){best[y]=best[y-1]-slope;origin[y]=origin[y-1];}
            for(int y=h-2;y>=0;y--)if(best[y+1]-slope>best[y]){best[y]=best[y+1]-slope;origin[y]=origin[y+1];}
            for(int y=0;y<h;y++){boolean observed=false;for(int x=left;x<right&&!observed;x++)observed=ink[y*w+x];next[y]=best[y]+(observed?3f:-1.2f);back[k][y]=(short)origin[y];}
            float[] swap=previous;previous=next;next=swap;
        }
        int end=0;for(int y=1;y<h;y++)if(previous[y]>previous[end])end=y;int[] path=new int[count];for(int k=count-1;k>=0;k--){path[k]=end;end=back[k][end];}
        ArrayList<ChartEngine.Point> observed=new ArrayList<>();int seen=0;float min=h,max=0;
        for(int k=0;k<count;k++){cancel.check();int x0=k*stride,x1=Math.min(w,x0+stride),chosenX=-1,chosenY=-1;
            for(int d=0;d<=2&&chosenX<0;d++)for(int sign:new int[]{-1,1}){int y=path[k]+d*sign;if(y<0||y>=h)continue;for(int x=x0;x<x1;x++)if(ink[y*w+x]){chosenX=x;chosenY=y;break;}if(chosenX>=0)break;}
            if(chosenX<0)continue;seen++;int top=chosenY,bottom=chosenY;while(top>7&&ink[(top-1)*w+chosenX])top--;while(bottom+1<h&&ink[(bottom+1)*w+chosenX])bottom++;min=Math.min(min,top);max=Math.max(max,bottom);observed.add(new ChartEngine.Point(chosenX,chosenY,top,bottom));
        }
        if(seen<count*.58||observed.size()<12||max-min<10||observed.get(observed.size()-1).x-observed.get(0).x<w*.55)return Collections.emptyList();
        ArrayList<ChartEngine.Point> sampled=new ArrayList<>();for(int i=0;i<observed.size();i+=Math.max(1,(int)Math.ceil(observed.size()/180f)))sampled.add(observed.get(i));return sampled;
    }
    private static int distance(int a,int b){return Math.abs(((a>>16)&255)-((b>>16)&255))+Math.abs(((a>>8)&255)-((b>>8)&255))+Math.abs((a&255)-(b&255));}
    private static int inkClass(int v){int r=(v>>16)&255,g=(v>>8)&255,b=v&255,max=Math.max(r,Math.max(g,b)),min=Math.min(r,Math.min(g,b));if(max-min<45)return max>55&&max<205?4:0;return max==r?1:max==g?2:3;}
    private static void removeRulings(boolean[] ink,int[] px,int w,int h,ChartEngine.Cancellation cancel){boolean[] seen=new boolean[ink.length];int[] queue=new int[ink.length];for(int start=0;start<ink.length;start++){if(start%16384==0)cancel.check();if(seen[start]||!ink[start]||inkClass(px[start])==0)continue;int head=0,tail=1,left=w,right=0;double sx=0,sy=0,sxx=0,sxy=0,syy=0;queue[0]=start;seen[start]=true;while(head<tail){int i=queue[head++],x=i%w,y=i/w;left=Math.min(left,x);right=Math.max(right,x);sx+=x;sy+=y;sxx+=x*(double)x;sxy+=x*(double)y;syy+=y*(double)y;for(int dy=-1;dy<=1;dy++)for(int dx=-1;dx<=1;dx++){int nx=x+dx,ny=y+dy,j=ny*w+nx;if(nx>=0&&nx<w&&ny>=0&&ny<h&&!seen[j]&&ink[j]&&inkClass(px[j])==inkClass(px[start])){seen[j]=true;queue[tail++]=j;}}}if(right-left>Math.max(40,w*.12)){double denom=sxx-sx*sx/tail,slope=denom>0?(sxy-sx*sy/tail)/denom:0,error=Math.max(0,syy-sy*sy/tail-slope*(sxy-sx*sy/tail))/tail;if(error<5.8)for(int i=0;i<tail;i++)ink[queue[i]]=false;}}}
}
