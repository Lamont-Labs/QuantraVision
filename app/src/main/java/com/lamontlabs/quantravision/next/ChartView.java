package com.lamontlabs.quantravision.next;

import android.content.Context;
import android.graphics.*;
import android.view.View;
import com.lamontlabs.quantravision.next.engine.ChartEngine;

final class ChartView extends View {
    private Bitmap image;private ChartEngine.Result result;private int selected=-1;private boolean annotations=true,focus=true;
    ChartView(Context c,Bitmap b,ChartEngine.Result result){super(c);this.image=b;this.result=result;setContentDescription(result==null?"Chart image preview":"Annotated chart with "+result.matches.size()+" visual matches");setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
    void select(int n){selected=n;invalidate();}
    void annotations(boolean value){annotations=value;invalidate();}
    void focus(boolean value){focus=value;invalidate();}
    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);p.setColor(0xff0b1422);
        RectF target=new RectF(0,0,getWidth(),getHeight());c.drawRoundRect(target,Ui.dp(getContext(),14),Ui.dp(getContext(),14),p);
        if(image==null||image.isRecycled())return;
        Path clip=new Path();clip.addRoundRect(target,Ui.dp(getContext(),14),Ui.dp(getContext(),14),Path.Direction.CW);c.save();c.clipPath(clip);
        draw(c,target,image,result,selected,annotations,focus);c.restore();
    }
    private static Rect source(Bitmap b,ChartEngine.Result r,boolean focus) {
        if(r==null||r.candles.isEmpty()||!focus)return new Rect(0,0,b.getWidth(),b.getHeight());
        int l=b.getWidth(),t=b.getHeight(),right=0,bottom=0;
        for(ChartEngine.Candle candle:r.candles){l=Math.min(l,candle.left);t=Math.min(t,candle.top);right=Math.max(right,candle.right);bottom=Math.max(bottom,candle.bottom);}
        int margin=Math.max(16,Math.round((right-l)*.035f));return new Rect(Math.max(0,l-margin),Math.max(0,t-margin),Math.min(b.getWidth(),right+margin),Math.min(b.getHeight(),bottom+margin));
    }
    private static void draw(Canvas c,RectF area,Bitmap b,ChartEngine.Result r,int selected,boolean annotations,boolean focus) {
        Rect src=source(b,r,focus);float scale=Math.min(area.width()/src.width(),area.height()/src.height());
        float x=area.centerX()-src.width()*scale/2,y=area.centerY()-src.height()*scale/2;
        Paint p=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);c.drawBitmap(b,src,new RectF(x,y,x+src.width()*scale,y+src.height()*scale),p);
        if(r==null||!annotations)return;
        for(int i=0;i<r.matches.size();i++) {
            if(selected>=0&&selected!=i)continue;ChartEngine.Match m=r.matches.get(i);
            p.setColor(Ui.LIME);p.setStrokeWidth(Math.max(1.8f,area.width()/220));p.setStrokeJoin(Paint.Join.ROUND);p.setStyle(Paint.Style.STROKE);
            float left=x+(m.left-src.left)*scale,top=y+(m.top-src.top)*scale,right=x+(m.right-src.left)*scale,bottom=y+(m.bottom-src.top)*scale;
            if("Candle".equals(m.family)){p.setColor(0x33b5f57a);p.setStyle(Paint.Style.FILL);c.drawRoundRect(left-3,top-4,right+3,bottom+4,4,4,p);p.setColor(Ui.LIME);p.setStyle(Paint.Style.STROKE);c.drawRoundRect(left-3,top-4,right+3,bottom+4,4,4,p);}
            else {Path path=new Path();for(int j=0;j<m.points.size();j++){ChartEngine.Point point=m.points.get(j);float px=x+(point.x-src.left)*scale,py=y+(point.y-src.top)*scale;if(j==0)path.moveTo(px,py);else path.lineTo(px,py);}c.drawPath(path,p);}
            p.setStyle(Paint.Style.FILL);for(ChartEngine.Point point:m.points)c.drawCircle(x+(point.x-src.left)*scale,y+(point.y-src.top)*scale,Math.max(3,area.width()/150),p);
        }
    }
    static Bitmap export(Bitmap b,ChartEngine.Result r,boolean annotations) {
        Rect src=source(b,r,true);int width=1200,height=Math.max(260,Math.round(width*src.height()/(float)src.width()));
        height=Math.min(2000,height);Bitmap out=Bitmap.createBitmap(width,height+96,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(out);c.drawColor(Ui.BG);
        draw(c,new RectF(24,24,width-24,height-12),b,r,-1,annotations,true);
        Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);p.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));p.setTextSize(26);p.setColor(Ui.LIME);c.drawText("QuantraVision",28,height+30,p);
        p.setTextSize(18);p.setColor(Ui.MUTED);c.drawText("Visual geometry · market prices, volume and times unavailable",28,height+65,p);return out;
    }
}
