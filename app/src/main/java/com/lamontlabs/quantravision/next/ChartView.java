package com.lamontlabs.quantravision.next;

import android.content.Context;
import android.graphics.*;
import android.view.*;
import com.lamontlabs.quantravision.next.engine.ChartEngine;

final class ChartView extends View {
    private Bitmap image;private ChartEngine.Result result;private int selected=-1;private boolean annotations=true,focus=true,interactive=true;
    private float zoom=1,offsetX,offsetY,lastX,lastY;
    private final ScaleGestureDetector scaler;
    private final GestureDetector gestures;
    ChartView(Context c,Bitmap b,ChartEngine.Result result){super(c);this.image=b;this.result=result;description();setLayerType(View.LAYER_TYPE_SOFTWARE,null);
        scaler=new ScaleGestureDetector(c,new ScaleGestureDetector.SimpleOnScaleGestureListener(){@Override public boolean onScale(ScaleGestureDetector detector){float previous=zoom;zoom=Math.max(1,Math.min(6,zoom*detector.getScaleFactor()));float ratio=zoom/previous;offsetX=(offsetX+getWidth()/2f-detector.getFocusX())*ratio-(getWidth()/2f-detector.getFocusX());offsetY=(offsetY+getHeight()/2f-detector.getFocusY())*ratio-(getHeight()/2f-detector.getFocusY());clamp();description();invalidate();return true;}});
        gestures=new GestureDetector(c,new GestureDetector.SimpleOnGestureListener(){@Override public boolean onDown(MotionEvent e){return true;}@Override public boolean onDoubleTap(MotionEvent e){if(zoom>1.01f)resetTransform();else{zoom=2;offsetX=getWidth()/2f-e.getX();offsetY=getHeight()/2f-e.getY();clamp();description();invalidate();}return true;}@Override public boolean onSingleTapUp(MotionEvent e){performClick();return true;}});
    }
    void setInteractive(boolean value){interactive=value;}
    void resetTransform(){zoom=1;offsetX=offsetY=0;description();invalidate();}
    private void description(){setContentDescription((result==null?"Chart image preview":"Annotated chart with "+result.matches.size()+" visual matches")+String.format(java.util.Locale.ROOT," · zoom %.1f",zoom));}
    private void clamp(){float x=getWidth()*(zoom-1)/2f,y=getHeight()*(zoom-1)/2f;offsetX=Math.max(-x,Math.min(x,offsetX));offsetY=Math.max(-y,Math.min(y,offsetY));}
    @Override public boolean performClick(){super.performClick();return true;}
    @Override public boolean onTouchEvent(MotionEvent e){if(!interactive)return false;scaler.onTouchEvent(e);gestures.onTouchEvent(e);if(e.getActionMasked()==MotionEvent.ACTION_DOWN){lastX=e.getX();lastY=e.getY();}if(e.getActionMasked()==MotionEvent.ACTION_MOVE&&!scaler.isInProgress()&&e.getPointerCount()==1&&zoom>1.01f){offsetX+=e.getX()-lastX;offsetY+=e.getY()-lastY;clamp();invalidate();}lastX=e.getX();lastY=e.getY();if(getParent()!=null)getParent().requestDisallowInterceptTouchEvent(zoom>1.01f||e.getPointerCount()>1);return true;}
    void select(int n){selected=n;invalidate();}
    void annotations(boolean value){annotations=value;invalidate();}
    void focus(boolean value){focus=value;invalidate();}
    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);p.setColor(0xff0a1012);
        RectF target=new RectF(0,0,getWidth(),getHeight());c.drawRoundRect(target,Ui.dp(getContext(),14),Ui.dp(getContext(),14),p);
        if(image==null||image.isRecycled())return;
        Path clip=new Path();clip.addRoundRect(target,Ui.dp(getContext(),14),Ui.dp(getContext(),14),Path.Direction.CW);c.save();c.clipPath(clip);
        c.translate(target.centerX()+offsetX,target.centerY()+offsetY);c.scale(zoom,zoom);c.translate(-target.centerX(),-target.centerY());draw(c,target,image,result,selected,annotations,focus);c.restore();
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
        int brightness=0;for(int px:new int[]{src.left,Math.max(src.left,src.right-1)})for(int py:new int[]{src.top,Math.max(src.top,src.bottom-1)}){int color=b.getPixel(px,py);brightness+=(((color>>16)&255)*3+((color>>8)&255)*6+(color&255))/10;}
        int ink=brightness/4>160?0xff00634f:Ui.LIME;
        for(int i=0;i<r.matches.size();i++) {
            if(selected>=0&&selected!=i)continue;ChartEngine.Match m=r.matches.get(i);
            p.setColor(ink);p.setStrokeWidth(Math.max(1.8f,area.width()/220));p.setStrokeJoin(Paint.Join.ROUND);p.setStyle(Paint.Style.STROKE);
            float left=x+(m.left-src.left)*scale,top=y+(m.top-src.top)*scale,right=x+(m.right-src.left)*scale,bottom=y+(m.bottom-src.top)*scale;
            if("Candle".equals(m.family)){p.setColor((ink&0x00ffffff)|0x33000000);p.setStyle(Paint.Style.FILL);c.drawRoundRect(left-3,top-4,right+3,bottom+4,4,4,p);p.setColor(ink);p.setStyle(Paint.Style.STROKE);c.drawRoundRect(left-3,top-4,right+3,bottom+4,4,4,p);}
            else {Path path=new Path();for(int j=0;j<m.points.size();j++){ChartEngine.Point point=m.points.get(j);float px=x+(point.x-src.left)*scale,py=y+(point.y-src.top)*scale;if(j==0||m.id.contains("_triangle_")&&j==2)path.moveTo(px,py);else path.lineTo(px,py);}c.drawPath(path,p);}
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
