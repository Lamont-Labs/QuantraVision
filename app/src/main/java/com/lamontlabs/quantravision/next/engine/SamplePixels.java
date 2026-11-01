package com.lamontlabs.quantravision.next.engine;

import java.util.Arrays;

/** Generated demonstration data, explicitly labeled as a sample in the app. */
public final class SamplePixels {
    public static final int WIDTH=960, HEIGHT=540, COUNT=44;
    public static int[] create(boolean light,boolean neutralWicks) {
        int bg=light?0xfff5f7fb:0xff0d1728,grid=light?0xffdce3ed:0xff1c2b40;
        int green=light?0xff168a70:0xff42ddb2,red=light?0xffd94d63:0xffef7d85;
        int[] p=new int[WIDTH*HEIGHT];Arrays.fill(p,bg);
        for(int y=60;y<500;y+=80)rect(p,0,y,WIDTH,y+1,grid);
        for(int x=54;x<WIDTH;x+=90)rect(p,x,40,x+1,500,grid);
        int[] ix={0,13,23,33,43},yy={385,130,312,136,365};
        int previous=390;
        for(int i=0;i<COUNT;i++) {
            int k=0;while(k+1<ix.length-1 && i>ix[k+1])k++;
            float f=(i-ix[k])/(float)(ix[k+1]-ix[k]);
            int close=Math.round(yy[k]+f*(yy[k+1]-yy[k]))+(i%4==1?4:0);
            int open=previous+(i%3-1)*4;
            if(Math.abs(close-open)<5)open=close+7;
            int left=60+i*19,top=Math.min(open,close),bottom=Math.max(open,close);
            int c=close<open?green:red,wick=neutralWicks?(light?0xff41516a:0xffb3c2d9):c;
            rect(p,left+4,top-10-(i%3)*2,left+6,bottom+11+(i%2)*3,wick);
            rect(p,left,top,left+10,bottom+1,c);previous=close;
        }
        return p;
    }
    public static void rect(int[] p,int l,int t,int r,int b,int color) {
        for(int y=Math.max(0,t);y<Math.min(HEIGHT,b);y++)
            for(int x=Math.max(0,l);x<Math.min(WIDTH,r);x++)p[y*WIDTH+x]=color;
    }
}
