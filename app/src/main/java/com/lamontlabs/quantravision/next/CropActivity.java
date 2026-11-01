package com.lamontlabs.quantravision.next;

import android.app.Activity;
import android.content.Intent;
import android.graphics.*;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import java.io.File;

public final class CropActivity extends Activity {
    private Bitmap bitmap;private CropCanvas crop;private String input;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);input=getIntent().getStringExtra("image_id");
        try{bitmap=BitmapFactory.decodeFile(((QuantraApplication)getApplication()).session.store.input(input).getPath());if(bitmap==null)throw new IllegalStateException();}
        catch(Exception e){Toast.makeText(this,"The crop image is unavailable.",Toast.LENGTH_SHORT).show();finish();return;}
        LinearLayout root=Ui.column(this);root.setBackgroundColor(Ui.BG);root.setPadding(Ui.dp(this,18),Ui.dp(this,20),Ui.dp(this,18),Ui.dp(this,18));
        if(android.os.Build.VERSION.SDK_INT>=30){getWindow().setDecorFitsSystemWindows(false);root.setOnApplyWindowInsetsListener((v,insets)->{android.graphics.Insets bars=insets.getInsets(WindowInsets.Type.systemBars());v.setPadding(Ui.dp(this,18)+bars.left,Ui.dp(this,20)+bars.top,Ui.dp(this,18)+bars.right,Ui.dp(this,18)+bars.bottom);return insets;});}else root.setFitsSystemWindows(true);
        Ui.add(root,Ui.display(this,"Frame your chart.",36));Ui.gap(root,10);Ui.add(root,Ui.text(this,"Drag a corner to keep the candles and remove surrounding menus.",13,Ui.MUTED,false));Ui.gap(root,16);
        crop=new CropCanvas();root.addView(crop,new LinearLayout.LayoutParams(-1,0,1));Ui.gap(root,18);Ui.add(root,Ui.button(this,"Use crop","check",true,this::apply));Ui.gap(root,10);Ui.add(root,Ui.button(this,"Cancel crop",null,false,this::finish));setContentView(root);
    }
    private void apply() {
        Rect r=crop.imageSelection();
        try {Bitmap selected=Bitmap.createBitmap(bitmap,r.left,r.top,r.width(),r.height());String id=((QuantraApplication)getApplication()).session.store.writeInput(selected);if(selected!=bitmap)selected.recycle();setResult(RESULT_OK,new Intent().putExtra("crop_id",id));finish();}
        catch(Exception e){Toast.makeText(this,"Could not apply this crop. Try a larger region.",Toast.LENGTH_LONG).show();}
    }
    @Override protected void onDestroy(){try{if(isFinishing()&&input!=null)new File(getCacheDir(),"incoming/"+input+".png").delete();}catch(Exception ignored){}super.onDestroy();}
    private final class CropCanvas extends View {
        private final RectF image=new RectF(),selection=new RectF();private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);private int corner=-1;
        CropCanvas(){super(CropActivity.this);setContentDescription("Crop area. Drag its corners to resize.");}
        @Override protected void onSizeChanged(int w,int h,int ow,int oh){float s=Math.min(w/(float)bitmap.getWidth(),h/(float)bitmap.getHeight());float rw=bitmap.getWidth()*s,rh=bitmap.getHeight()*s;image.set((w-rw)/2,(h-rh)/2,(w+rw)/2,(h+rh)/2);selection.set(image);}
        Rect imageSelection(){float sx=bitmap.getWidth()/image.width(),sy=bitmap.getHeight()/image.height();int l=Math.max(0,Math.round((selection.left-image.left)*sx)),t=Math.max(0,Math.round((selection.top-image.top)*sy));int r=Math.min(bitmap.getWidth(),Math.round((selection.right-image.left)*sx)),b=Math.min(bitmap.getHeight(),Math.round((selection.bottom-image.top)*sy));return new Rect(l,t,Math.max(l+1,r),Math.max(t+1,b));}
        @Override protected void onDraw(Canvas canvas){canvas.drawBitmap(bitmap,null,image,p);p.setColor(0xaa080e19);canvas.drawRect(image.left,image.top,image.right,selection.top,p);canvas.drawRect(image.left,selection.bottom,image.right,image.bottom,p);canvas.drawRect(image.left,selection.top,selection.left,selection.bottom,p);canvas.drawRect(selection.right,selection.top,image.right,selection.bottom,p);p.setStyle(Paint.Style.STROKE);p.setColor(Ui.LIME);p.setStrokeWidth(Ui.dp(CropActivity.this,2));canvas.drawRect(selection,p);p.setStyle(Paint.Style.FILL);for(float[] point:corners())canvas.drawCircle(point[0],point[1],Ui.dp(CropActivity.this,7),p);}
        private float[][] corners(){return new float[][]{{selection.left,selection.top},{selection.right,selection.top},{selection.right,selection.bottom},{selection.left,selection.bottom}};}
        @Override public boolean onTouchEvent(MotionEvent e){float x=e.getX(),y=e.getY(),minX=Math.min(Ui.dp(CropActivity.this,48),image.width()*.4f),minY=Math.min(Ui.dp(CropActivity.this,48),image.height()*.4f);
            if(e.getActionMasked()==MotionEvent.ACTION_DOWN){float best=Ui.dp(CropActivity.this,48);float[][] points=corners();for(int i=0;i<4;i++){float distance=(float)Math.hypot(x-points[i][0],y-points[i][1]);if(distance<best){best=distance;corner=i;}}return corner>=0;}
            if(e.getActionMasked()==MotionEvent.ACTION_MOVE&&corner>=0){x=Math.max(image.left,Math.min(image.right,x));y=Math.max(image.top,Math.min(image.bottom,y));if(corner==0||corner==3)selection.left=Math.min(x,selection.right-minX);else selection.right=Math.max(x,selection.left+minX);if(corner==0||corner==1)selection.top=Math.min(y,selection.bottom-minY);else selection.bottom=Math.max(y,selection.top+minY);invalidate();return true;}
            if(e.getActionMasked()==MotionEvent.ACTION_UP||e.getActionMasked()==MotionEvent.ACTION_CANCEL){corner=-1;performClick();return true;}return false;
        }
        @Override public boolean performClick(){super.performClick();return true;}
    }
}
