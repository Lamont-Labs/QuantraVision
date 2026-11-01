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
        ContentValues values=new ContentValues();values.put(MediaStore.Images.Media.DISPLAY_NAME,"QV-test-chart.png");values.put(MediaStore.Images.Media.MIME_TYPE,"image/png");values.put(MediaStore.Images.Media.RELATIVE_PATH,"Pictures/QuantraVision");
        Uri uri=context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);assertNotNull(uri);
        Bitmap b=Bitmap.createBitmap(SamplePixels.create(true,true),SamplePixels.WIDTH,SamplePixels.HEIGHT,Bitmap.Config.ARGB_8888);
        try(OutputStream out=context.getContentResolver().openOutputStream(uri)){assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,out));}b.recycle();
        try{
            tap("Import chart");UiObject2 image=device.wait(Until.findObject(By.textContains("QV-test-chart")),10000);
            if(image==null){UiObject2 images=device.findObject(By.text("Images"));if(images!=null)images.click();image=device.wait(Until.findObject(By.textContains("QV-test-chart")),5000);}
            assertNotNull("The published image was not offered by Android’s picker",image);image.click();
            assertTrue(device.wait(Until.hasObject(By.desc("Analyze chart")),15000));analyzeReady();assertEquals("Imported image",session.source);assertEquals(44,session.result.candles.size());Proof.save("10-imported-light-chart");
        }finally{context.getContentResolver().delete(uri,null,null);}
    }
}
