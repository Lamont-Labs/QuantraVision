package com.lamontlabs.quantravision.next;

import android.content.Context;
import android.graphics.*;
import android.view.View;

/** Original schematics. Never presented as price data or measured charts. */
final class PatternGlyph extends View {
    private final PatternCatalog.Pattern pattern;
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    PatternGlyph(Context c,PatternCatalog.Pattern pattern){super(c);this.pattern=pattern;setContentDescription(pattern.title+" schematic");}
    @Override protected void onDraw(Canvas c){super.onDraw(c);c.save();c.translate(getWidth()*.06f,getHeight()*.12f);c.scale(getWidth()*.88f/100,getHeight()*.76f/70);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(.55f);p.setColor(Ui.LINE);for(int y=10;y<=60;y+=20)c.drawLine(0,y,100,y,p);
        if("Candle".equals(pattern.family)){candle(c,27,18,52,29,40,Ui.MUTED);String id=pattern.id;if(id.contains("engulfing")){candle(c,45,16,58,29,39,id.startsWith("bullish")?Ui.RED:Ui.TEAL);candle(c,68,10,65,21,51,pattern.color);}else if("doji".equals(id))candle(c,64,7,64,34,36,pattern.color);else if("hammer".equals(id))candle(c,64,10,65,14,28,pattern.color);else candle(c,64,6,62,43,57,pattern.color);
        }else{float[] xy;
            switch(pattern.id){
                case "double_top":xy=new float[]{0,60,14,43,28,12,49,48,71,13,88,48,100,59};break;
                case "double_bottom":xy=new float[]{0,9,14,28,28,59,49,22,71,58,88,22,100,11};break;
                case "head_shoulders":xy=new float[]{0,61,15,27,30,49,49,7,68,49,84,28,100,62};break;
                case "inverse_head_shoulders":xy=new float[]{0,8,15,43,30,21,49,63,68,21,84,42,100,8};break;
                case "ascending_triangle":xy=new float[]{0,60,16,12,33,50,50,13,66,37,83,12,100,24};break;
                case "descending_triangle":xy=new float[]{0,10,16,59,33,22,50,58,66,35,83,59,100,47};break;
                default:xy=new float[]{0,62,16,9,33,51,50,21,66,42,83,30,100,35};
            }
            p.setColor(pattern.color);p.setStrokeWidth(1.8f);p.setStrokeJoin(Paint.Join.ROUND);p.setStrokeCap(Paint.Cap.ROUND);Path path=new Path();path.moveTo(xy[0],xy[1]);for(int i=2;i<xy.length;i+=2)path.lineTo(xy[i],xy[i+1]);c.drawPath(path,p);
            p.setStyle(Paint.Style.FILL);for(int i=2;i<xy.length-2;i+=2)c.drawCircle(xy[i],xy[i+1],2.1f,p);
        }c.restore();
    }
    private void candle(Canvas c,float x,float top,float bottom,float bodyTop,float bodyBottom,int color){p.setColor(color);p.setStrokeWidth(1.2f);p.setStyle(Paint.Style.STROKE);c.drawLine(x,top,x,bottom,p);p.setStyle(Paint.Style.FILL);c.drawRoundRect(x-5,bodyTop,x+5,bodyBottom,1,1,p);}
}
