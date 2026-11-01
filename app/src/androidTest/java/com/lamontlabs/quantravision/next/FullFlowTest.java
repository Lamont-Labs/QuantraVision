package com.lamontlabs.quantravision.next;

import android.content.*;
import android.graphics.Bitmap;
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
        Bitmap b=Bitmap.createBitmap(SamplePixels.create(true,true),SamplePixels.WIDTH,SamplePixels.HEIGHT,Bitmap.Config.ARGB_8888);
        try(OutputStream out=context.getContentResolver().openOutputStream(uri)){assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,out));}b.recycle();
        ContentValues published=new ContentValues();published.put(MediaStore.Images.Media.IS_PENDING,0);context.getContentResolver().update(uri,published,null,null);
        try{
            tap("Import chart");UiObject2 list=device.wait(Until.findObject(By.desc("List view")),3000);if(list!=null)list.click();UiObject2 image=awaitPublishedChart(5000);
            if(image==null){UiObject2 roots=device.findObject(By.desc("Show roots"));if(roots!=null)roots.click();UiObject2 images=device.wait(Until.findObject(By.text("Images")),3000);if(images!=null)images.click();UiObject2 folder=device.wait(Until.findObject(By.text("QuantraVision")),3000);if(folder!=null)folder.click();}
            Proof.save("picker-before-selection");boolean selected=false;
            for(int attempt=0;attempt<3&&!selected;attempt++){
                try{UiObject2 current=awaitPublishedChart(10000);if(current!=null){current.click();selected=true;}}
                catch(StaleObjectException refresh){device.waitForIdle();}
            }
            assertTrue("Could not select the image after Android’s picker refreshed",selected);
            assertTrue(device.wait(Until.hasObject(By.desc("Analyze chart")),15000));analyzeReady();assertEquals("Imported image",session.source);assertEquals(44,session.result.candles.size());Proof.save("10-imported-light-chart");
        }finally{context.getContentResolver().delete(uri,null,null);}
    }
    private UiObject2 awaitPublishedChart(long timeout){
        long deadline=android.os.SystemClock.uptimeMillis()+timeout;
        do{UiObject2 row=device.findObject(By.textContains("QV-test-chart"));if(row==null)row=device.findObject(By.descStartsWith("QV-test-chart.png"));if(row!=null)return row;android.os.SystemClock.sleep(100);}while(android.os.SystemClock.uptimeMillis()<deadline);
        return null;
    }
}
