package com.lamontlabs.quantravision.next;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.*;
import android.graphics.drawable.*;
import android.view.*;
import android.widget.*;

final class Ui {
    static final int BG=0xff0b1012,CARD=0xff131b1e,CARD2=0xff1a2427,LINE=0xff2a3436;
    static final int INK=0xffeeeae1,MUTED=0xff8b9b9d,LIME=0xffcdb38b,TEAL=0xff8fc6b2,RED=0xffd7918d;
    static int dp(Context c,float n){return Math.round(n*c.getResources().getDisplayMetrics().density);}
    static LinearLayout column(Context c){LinearLayout v=new LinearLayout(c);v.setOrientation(LinearLayout.VERTICAL);return v;}
    static LinearLayout row(Context c){LinearLayout v=new LinearLayout(c);v.setOrientation(LinearLayout.HORIZONTAL);v.setGravity(Gravity.CENTER_VERTICAL);return v;}
    static TextView text(Context c,String value,float size,int color,boolean bold) {
        TextView v=new TextView(c);v.setText(value);v.setTextSize(size);v.setTextColor(color);v.setIncludeFontPadding(false);
        v.setTypeface(Typeface.create(c.getResources().getFont(R.font.manrope),bold?Typeface.BOLD:Typeface.NORMAL));v.setFontVariationSettings("'wght' "+(bold?650:450));v.setLineSpacing(dp(c,3),1);return v;
    }
    static TextView display(Context c,String value,float size){TextView v=text(c,value,size,INK,false);v.setTypeface(Typeface.createFromAsset(c.getAssets(),"fonts/InstrumentSerif.ttf"));return v;}
    static TextView mono(Context c,String value,float size,int color){TextView v=text(c,value,size,color,false);v.setTypeface(Typeface.createFromAsset(c.getAssets(),"fonts/PlexMono.ttf"));return v;}
    static TextView eyebrow(Context c,String text){TextView v=mono(c,text,10,MUTED);v.setLetterSpacing(.13f);return v;}
    static GradientDrawable shape(int color,int stroke,float radius,Context c) {
        GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(c,radius));if(stroke!=0)d.setStroke(dp(c,1),stroke);return d;
    }
    static LinearLayout card(Context c){LinearLayout v=column(c);v.setPadding(dp(c,18),dp(c,18),dp(c,18),dp(c,18));v.setBackground(shape(CARD,LINE,9,c));return v;}
    static void gap(LinearLayout v,int height){View s=new View(v.getContext());v.addView(s,new LinearLayout.LayoutParams(1,dp(v.getContext(),height)));}
    static void add(LinearLayout parent,View child){parent.addView(child,new LinearLayout.LayoutParams(-1,-2));}
    static TextView pill(Context c,String value,int color) {
        TextView v=text(c,value,11,color,true);v.setPadding(dp(c,10),dp(c,6),dp(c,10),dp(c,6));v.setBackground(shape((color&0x00ffffff)|0x16000000,0,20,c));return v;
    }
    static View button(Context c,String label,String icon,boolean primary,Runnable click) {
        LinearLayout v=row(c);v.setGravity(Gravity.CENTER);v.setMinimumHeight(dp(c,54));v.setPadding(dp(c,16),dp(c,12),dp(c,16),dp(c,12));
        int color=primary?BG:INK;v.setBackground(new RippleDrawable(ColorStateList.valueOf(0x225ee2c5),shape(primary?INK:CARD,primary?0:LINE,7,c),shape(0xffffffff,0,7,c)));
        if(icon!=null){Icon i=new Icon(c,icon,color);LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(dp(c,20),dp(c,20));ip.rightMargin=dp(c,10);v.addView(i,ip);}
        TextView title=text(c,label,15,color,true);v.addView(title);v.setContentDescription("Import screenshot".equals(label)?"Import chart":label);v.setFocusable(true);v.setClickable(true);v.setOnClickListener(view->{if(c.getSharedPreferences("app",0).getBoolean("haptics",true))view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);click.run();});return v;
    }
    static View line(Context c){View v=new View(c);v.setBackgroundColor(LINE);v.setLayoutParams(new LinearLayout.LayoutParams(-1,dp(c,1)));return v;}
    static final class Icon extends View {
        final String name;final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);final int color;
        Icon(Context c,String name,int color){super(c);this.name=name;this.color=color;setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
        @Override protected void onDraw(Canvas canvas){super.onDraw(canvas);canvas.save();canvas.scale(getWidth()/24f,getHeight()/24f);p.setColor(color);p.setStrokeWidth(1.7f);p.setStyle(Paint.Style.STROKE);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);Path path=new Path();
            switch(name){
                case "logo": canvas.drawCircle(10.8f,10.8f,8,p);canvas.drawLine(16.5f,16.5f,22,22,p);canvas.drawLine(7,9,7,14,p);canvas.drawLine(11,6,11,13,p);canvas.drawLine(15,9,15,15,p);break;
                case "overview": canvas.drawRoundRect(3,3,10,11,2,2,p);canvas.drawRoundRect(14,3,21,8,2,2,p);canvas.drawRoundRect(3,15,10,21,2,2,p);canvas.drawRoundRect(14,12,21,21,2,2,p);break;
                case "scan": path.moveTo(3,8);path.lineTo(3,3);path.lineTo(8,3);path.moveTo(16,3);path.lineTo(21,3);path.lineTo(21,8);path.moveTo(21,16);path.lineTo(21,21);path.lineTo(16,21);path.moveTo(8,21);path.lineTo(3,21);path.lineTo(3,16);canvas.drawPath(path,p);canvas.drawLine(7,12,17,12,p);break;
                case "library": canvas.drawRoundRect(3,3,10,10,1,1,p);canvas.drawRoundRect(14,3,21,10,1,1,p);canvas.drawRoundRect(3,14,10,21,1,1,p);canvas.drawRoundRect(14,14,21,21,1,1,p);break;
                case "journal": canvas.drawRoundRect(4,3,20,21,3,3,p);canvas.drawLine(8,8,16,8,p);canvas.drawLine(8,12,16,12,p);canvas.drawLine(8,16,13,16,p);break;
                case "settings": canvas.drawCircle(12,12,4,p);for(int i=0;i<8;i++){double a=i*Math.PI/4;canvas.drawLine(12+(float)Math.cos(a)*7,12+(float)Math.sin(a)*7,12+(float)Math.cos(a)*10,12+(float)Math.sin(a)*10,p);}break;
                case "import": canvas.drawRoundRect(3,9,21,21,3,3,p);canvas.drawLine(12,3,12,15,p);path.moveTo(8,11);path.lineTo(12,15);path.lineTo(16,11);canvas.drawPath(path,p);break;
                case "screen": canvas.drawRoundRect(2,3,22,17,3,3,p);canvas.drawLine(8,22,16,22,p);canvas.drawLine(12,18,12,22,p);break;
                case "arrow": canvas.drawLine(4,12,20,12,p);path.moveTo(14,6);path.lineTo(20,12);path.lineTo(14,18);canvas.drawPath(path,p);break;
                case "crop": path.moveTo(6,2);path.lineTo(6,18);path.lineTo(22,18);path.moveTo(2,6);path.lineTo(18,6);path.lineTo(18,22);canvas.drawPath(path,p);break;
                case "close": canvas.drawLine(6,6,18,18,p);canvas.drawLine(18,6,6,18,p);break;
                case "share": canvas.drawLine(12,16,12,3,p);path.moveTo(7,8);path.lineTo(12,3);path.lineTo(17,8);path.moveTo(4,12);path.lineTo(4,21);path.lineTo(20,21);path.lineTo(20,12);canvas.drawPath(path,p);break;
                case "check": path.moveTo(4,12);path.lineTo(10,18);path.lineTo(21,6);canvas.drawPath(path,p);break;
                case "play": path.moveTo(9,5);path.lineTo(20,12);path.lineTo(9,19);path.close();canvas.drawPath(path,p);break;
                default: canvas.drawLine(6,7,6,18,p);canvas.drawLine(12,3,12,15,p);canvas.drawLine(18,8,18,21,p);canvas.drawRect(4,10,8,15,p);canvas.drawRect(10,6,14,11,p);canvas.drawRect(16,12,20,17,p);
            }canvas.restore();
        }
    }
}
