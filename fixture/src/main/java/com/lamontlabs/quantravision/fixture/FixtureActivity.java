package com.lamontlabs.quantravision.fixture;
import android.app.Activity;
import android.os.Bundle;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.*;
import com.lamontlabs.quantravision.next.engine.SamplePixels;
/** A separate UID proves that the captured chart belongs to another application. */
public final class FixtureActivity extends Activity {
    @Override public void onCreate(Bundle state){super.onCreate(state);LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(0xff080e19);root.setPadding(20,80,20,20);TextView title=new TextView(this);title.setText("Live capture test chart");title.setTextColor(Color.WHITE);title.setTextSize(22);root.addView(title);ImageView image=new ImageView(this);image.setScaleType(ImageView.ScaleType.FIT_CENTER);image.setImageBitmap(Bitmap.createBitmap(SamplePixels.create(false,true),SamplePixels.WIDTH,SamplePixels.HEIGHT,Bitmap.Config.ARGB_8888));root.addView(image,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);}
}
