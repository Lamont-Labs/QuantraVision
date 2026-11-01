package com.lamontlabs.quantravision.next;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;
import java.io.File;
public final class Proof {
    public static void input(String name,android.graphics.Bitmap bitmap) throws Exception {File folder=new File(InstrumentationRegistry.getInstrumentation().getTargetContext().getExternalFilesDir(null),"proof");folder.mkdirs();try(java.io.FileOutputStream out=new java.io.FileOutputStream(new File(folder,name+".png"))){if(!bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,out))throw new AssertionError("Could not preserve captured input");}}
    public static void save(String name) throws Exception {
        File folder=new File(InstrumentationRegistry.getInstrumentation().getTargetContext().getExternalFilesDir(null),"proof");if(!folder.isDirectory())folder.mkdirs();
        UiDevice device=UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();device.waitForIdle();
        // Accessibility can report the new tree before SurfaceFlinger presents it.
        android.os.SystemClock.sleep(250);
        if(!device.takeScreenshot(new File(folder,name+".png")))throw new AssertionError("Screenshot failed: "+name);
        device.dumpWindowHierarchy(new File(folder,name+".xml"));
    }
}
