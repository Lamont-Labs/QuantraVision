package com.lamontlabs.quantravision.next;

import android.content.*;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.os.Build;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.uiautomator.*;
import com.lamontlabs.quantravision.next.engine.SamplePixels;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
import java.io.OutputStream;

@RunWith(AndroidJUnit4.class)
public final class FullFlowTest extends CompatibilityFlowTest {
    @Test public void realAndroidFilePickerImportsAndAnalyzesAnImage() throws Exception {
        assertTrue(Build.VERSION.SDK_INT>=29);
        ContentValues values=new ContentValues();values.put(MediaStore.Images.Media.DISPLAY_NAME,"QV-test-chart.png");values.put(MediaStore.Images.Media.MIME_TYPE,"image/png");values.put(MediaStore.Images.Media.RELATIVE_PATH,"Pictures/QuantraVision");values.put(MediaStore.Images.Media.IS_PENDING,1);values.put(MediaStore.Images.Media.DATE_ADDED,System.currentTimeMillis()/1000);values.put(MediaStore.Images.Media.DATE_MODIFIED,System.currentTimeMillis()/1000);values.put(MediaStore.Images.Media.DATE_TAKEN,System.currentTimeMillis());
        Uri uri=context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);assertNotNull(uri);
        Bitmap b;try(java.io.InputStream source=context.getAssets().open("evaluation/nifty-qtstalker.png")){b=BitmapFactory.decodeStream(source);}assertNotNull(b);
        try(OutputStream out=context.getContentResolver().openOutputStream(uri)){assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,out));}b.recycle();
        ContentValues published=new ContentValues();published.put(MediaStore.Images.Media.IS_PENDING,0);context.getContentResolver().update(uri,published,null,null);
        try{
            tap("Import chart");pickerClick(By.desc("List view"),3000);UiObject2 image=awaitPublishedChart(5000);
            if(image==null){pickerClick(By.desc("Show roots"),3000);pickerClick(By.text("Images"),5000);pickerClick(By.text("QuantraVision"),5000);}
            Proof.save("picker-before-selection");boolean selected=false;
            for(int attempt=0;attempt<3&&!selected;attempt++){
                try{UiObject2 current=awaitPublishedChart(10000);if(current!=null){current.click();selected=true;}}
                catch(StaleObjectException refresh){device.waitForIdle();}
            }
            assertTrue("Could not select the image after Android’s picker refreshed",selected);
            assertTrue(device.wait(Until.hasObject(By.desc("Analyze chart")),15000));tap("Analyze chart");waitForIdleWork();assertNotNull("Real image analysis was not saved: "+session.error,session.record);assertNotNull(session.result);assertTrue(session.result.readable());assertEquals("Imported image",session.source);assertEquals(1366,session.result.width);assertTrue("Real hollow-candle image lost its series",session.result.candles.size()>=80);Proof.save("10-imported-real-chart");Proof.input("real-picker-input",session.bitmap);
        }finally{context.getContentResolver().delete(uri,null,null);}
    }
    private UiObject2 awaitPublishedChart(long timeout){
        long deadline=android.os.SystemClock.uptimeMillis()+timeout;
        do{UiObject2 row=device.findObject(By.textContains("QV-test-chart"));if(row==null)row=device.findObject(By.descStartsWith("QV-test-chart.png"));if(row!=null)return row;android.os.SystemClock.sleep(100);}while(android.os.SystemClock.uptimeMillis()<deadline);
        return null;
    }
    private boolean pickerClick(BySelector selector,long timeout){
        long deadline=android.os.SystemClock.uptimeMillis()+timeout;
        do{
            try{UiObject2 control=device.findObject(selector);if(control!=null){control.click();device.waitForIdle();return true;}}
            catch(StaleObjectException refresh){device.waitForIdle();}
            android.os.SystemClock.sleep(100);
        }while(android.os.SystemClock.uptimeMillis()<deadline);
        return false;
    }
}
