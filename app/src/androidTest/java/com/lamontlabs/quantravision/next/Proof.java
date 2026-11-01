package com.lamontlabs.quantravision.next;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;
import java.io.File;
public final class Proof {
    public static void save(String name) throws Exception {
        File folder=new File(InstrumentationRegistry.getInstrumentation().getTargetContext().getExternalFilesDir(null),"proof");if(!folder.isDirectory())folder.mkdirs();
        UiDevice device=UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        if(!device.takeScreenshot(new File(folder,name+".png")))throw new AssertionError("Screenshot failed: "+name);
        device.dumpWindowHierarchy(new File(folder,name+".xml"));
    }
}
