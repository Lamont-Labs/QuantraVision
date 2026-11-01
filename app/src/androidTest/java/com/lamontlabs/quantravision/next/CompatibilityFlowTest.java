package com.lamontlabs.quantravision.next;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.net.Uri;
import android.os.Build;
import android.widget.EditText;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.*;
import com.lamontlabs.quantravision.next.engine.*;
import org.junit.*;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
import java.io.*;
import java.util.regex.Pattern;

@RunWith(AndroidJUnit4.class)
public class CompatibilityFlowTest {
    protected UiDevice device;
    protected Context context;
    protected AppSession session;
    protected Activity activity;
    protected static final String PKG="com.lamontlabs.quantravision.next";
    @Before public void startFresh() throws Exception {
        device=UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());device.wakeUp();device.setOrientationNatural();
        context=InstrumentationRegistry.getInstrumentation().getTargetContext();session=((QuantraApplication)context.getApplicationContext()).session;
        context.stopService(new Intent(context,CaptureService.class));waitForIdleWork();
        session.store.clear();context.getSharedPreferences("app",0).edit().clear().commit();
        InstrumentationRegistry.getInstrumentation().runOnMainSync(session::reset);
        launch();
    }
    protected void launch() {
        Intent intent=new Intent(context,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity=InstrumentationRegistry.getInstrumentation().startActivitySync(intent);
        long deadline=android.os.SystemClock.uptimeMillis()+15000;
        java.util.concurrent.atomic.AtomicBoolean focused=new java.util.concurrent.atomic.AtomicBoolean();
        do{InstrumentationRegistry.getInstrumentation().runOnMainSync(()->focused.set(activity.hasWindowFocus()&&activity.getWindow().getDecorView().isShown()));if(!focused.get())android.os.SystemClock.sleep(100);}while(!focused.get()&&android.os.SystemClock.uptimeMillis()<deadline);
        assertTrue("New application window did not receive focus",focused.get());
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();device.waitForIdle();
        assertTrue("Overview did not launch",device.wait(Until.hasObject(By.text("QuantraVision")),15000));
    }
    protected void waitForIdleWork() throws Exception {long deadline=System.currentTimeMillis()+15000;while(session!=null&&session.busy&&System.currentTimeMillis()<deadline)Thread.sleep(50);if(session!=null)assertFalse("Work did not finish",session.busy);}
    protected UiObject2 find(BySelector selector,boolean scroll) throws Exception {
        UiObject2 object=device.wait(Until.findObject(selector),2000);if(object!=null)return object;
        if(scroll)for(int i=0;i<9;i++){device.swipe(device.getDisplayWidth()/2,device.getDisplayHeight()*3/4,device.getDisplayWidth()/2,device.getDisplayHeight()/3,24);device.waitForIdle();object=device.findObject(selector);if(object!=null)return object;}
        throw new AssertionError("Missing control: "+selector);
    }
    protected void tap(String description) throws Exception {find(By.desc(description),true).click();device.waitForIdle();InstrumentationRegistry.getInstrumentation().waitForIdleSync();}
    protected void text(String value) throws Exception {find(By.text(Pattern.compile(Pattern.quote(value),Pattern.CASE_INSENSITIVE)),true).click();device.waitForIdle();InstrumentationRegistry.getInstrumentation().waitForIdleSync();}
    protected void sampleReady() throws Exception {tap("Try sample chart");assertTrue("Sample image did not load",device.wait(Until.hasObject(By.desc("Analyze chart")),15000));}
    protected void analyzeReady() throws Exception {tap("Analyze chart");assertTrue("Real double top was not detected",device.wait(Until.hasObject(By.text("Double top")),15000));waitForIdleWork();assertNotNull(session.record);assertTrue(session.result.readable());}

    @Test public void sampleToAnnotatedResultToPersistentJournal() throws Exception {
        Proof.save("01-overview");sampleReady();Proof.save("02-ready");analyzeReady();
        assertEquals(44,session.result.candles.size());assertEquals(1,session.result.matches.size());assertEquals("Sample chart",session.source);
        Proof.save("03-analysis");String id=session.record.id;
        AppStore coldStore=new AppStore(context);AppStore.Record restored=coldStore.read(id);
        assertNotNull(restored);assertEquals("Double top",restored.result.matches.get(0).title);assertEquals(44,restored.result.candles.size());assertNotNull(coldStore.image(restored));
        tap("Journal tab");assertTrue(device.wait(Until.hasObject(By.desc("Open analysis "+id)),5000));Proof.save("04-journal");
        launch();tap("Journal tab");tap("Open analysis "+id);assertTrue(device.wait(Until.hasObject(By.text("Double top")),10000));
    }
    @Test public void cropNotesAndSettingsPerformTheirActions() throws Exception {
        sampleReady();tap("Adjust crop");UiObject2 area=find(By.desc("Crop area. Drag its corners to resize."),false);Rect view=area.getVisibleBounds();
        float ratio=SamplePixels.HEIGHT/(float)SamplePixels.WIDTH;float width=Math.min(view.width(),view.height()/ratio),height=width*ratio;
        int x=Math.round(view.centerX()-width/2)+3,y=Math.round(view.centerY()-height/2)+3;
        assertTrue(device.drag(x,y,x+25,y+25,30));tap("Use crop");assertTrue(device.wait(Until.hasObject(By.desc("Analyze chart")),10000));waitForIdleWork();
        assertTrue("Crop did not change image bounds",session.bitmap.getWidth()<SamplePixels.WIDTH);Proof.save("05-cropped");analyzeReady();
        tap("Add a note");UiObject2 input=find(By.clazz(EditText.class),false);input.setText("Watching the two measured peaks.");text("Save note");assertTrue("Note edit did not finish",device.wait(Until.hasObject(By.desc("Edit note")),5000));InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        assertEquals("Watching the two measured peaks.",session.record.note);assertEquals(session.record.note,new AppStore(context).read(session.record.id).note);
        tap("Settings tab");UiObject2 annotations=find(By.desc("Annotations on exports"),false);assertTrue(annotations.isChecked());annotations.click();device.waitForIdle();InstrumentationRegistry.getInstrumentation().waitForIdleSync();assertFalse(context.getSharedPreferences("app",0).getBoolean("annotations",true));Proof.save("06-settings");
        File exports=new File(context.getCacheDir(),"exports");int jsonBefore=exports.listFiles((d,n)->n.endsWith(".json"))==null?0:exports.listFiles((d,n)->n.endsWith(".json")).length;tap("Export journal as JSON");assertTrue("No new journal export",exports.listFiles((d,n)->n.endsWith(".json")).length>jsonBefore);device.pressBack();
        tap("Analyze tab");int before=exports.listFiles((d,n)->n.endsWith(".png")).length;tap("Share annotated chart");assertTrue("No new image export was created",exports.listFiles((d,n)->n.endsWith(".png")).length>before);device.pressBack();
        tap("Journal tab");UiObject2 row=find(By.desc("Open analysis "+session.record.id),true);row.longClick();text("Delete");assertTrue(session.store.list().isEmpty());
    }
    @Test public void invalidSharedImageCannotCreateAResultOrJournalEntry() throws Exception {
        Bitmap blank=Bitmap.createBitmap(480,320,Bitmap.Config.ARGB_8888);blank.eraseColor(0xff17202a);File f=ShareProvider.newFile(context,"png");try(FileOutputStream out=new FileOutputStream(f)){blank.compress(Bitmap.CompressFormat.PNG,100,out);}blank.recycle();
        Intent intent=new Intent(Intent.ACTION_SEND).setClass(context,MainActivity.class).setType("image/png").putExtra(Intent.EXTRA_STREAM,ShareProvider.uri(context,f)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(intent);assertTrue(device.wait(Until.hasObject(By.desc("Analyze chart")),10000));tap("Analyze chart");assertTrue(device.wait(Until.hasObject(By.text("Let’s get a clearer chart")),10000));waitForIdleWork();
        assertNull(session.record);assertNull(session.result);assertTrue(session.store.list().isEmpty());Proof.save("07-rejected-image");
    }
    @Test public void liveScreenCaptureProducesAndSavesRealMeasurements() throws Exception {
        device.executeShellCommand("appops set "+PKG+" SYSTEM_ALERT_WINDOW allow");
        if(Build.VERSION.SDK_INT>=33)device.executeShellCommand("pm grant "+PKG+" android.permission.POST_NOTIFICATIONS");
        tap("Scan another app");BySelector start=By.text(Pattern.compile("(?i)(start now|start recording|start|share screen|share)"));
        UiObject2 consent=device.wait(Until.findObject(start),12000);assertNotNull("Android projection consent was not shown",consent);consent.click();
        assertTrue("Floating capture control did not appear",device.wait(Until.hasObject(By.desc("Capture chart now")),15000));
        context.startActivity(new Intent().setClassName("com.lamontlabs.quantravision.fixture","com.lamontlabs.quantravision.fixture.FixtureActivity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));assertTrue(device.wait(Until.hasObject(By.text("Live capture test chart")),10000));
        Proof.save("08-live-source");tap("Capture chart now");boolean shown=device.wait(Until.hasObject(By.text("Double top")),20000);assertTrue("Live capture did not reach analyzed results; busy="+session.busy+" error="+session.error+" source="+session.source+" pending="+context.getSharedPreferences("app",0).getString("pending_capture",""),shown);waitForIdleWork();
        assertEquals("Live screen",session.source);assertNotNull(session.record);Proof.save("09-live-result");Proof.input("live-input",session.bitmap);assertTrue("Expected nearly all 44 visible candles, read "+session.result.candles.size(),session.result.candles.size()>=40);
        tap("Stop screen sharing");long until=System.currentTimeMillis()+5000;while(CaptureService.running&&System.currentTimeMillis()<until)Thread.sleep(50);assertFalse("Capture service did not release the session",CaptureService.running);
    }
    @Test public void clearJournalAlsoRemovesAnUnreadableEntry() throws Exception {
        Bitmap image=Bitmap.createBitmap(SamplePixels.create(false,false),SamplePixels.WIDTH,SamplePixels.HEIGHT,Bitmap.Config.ARGB_8888);
        ChartEngine.Result measured=new ChartEngine().analyze(SamplePixels.WIDTH,SamplePixels.HEIGHT,SamplePixels.create(false,false));AppStore.Record saved=session.store.save(image,measured,"Test fixture");image.recycle();
        File folder=new File(context.getFilesDir(),"journal");try(FileOutputStream out=new FileOutputStream(new File(folder,saved.id+".json"))){out.write("not JSON".getBytes("UTF-8"));}
        assertTrue(session.store.list().isEmpty());assertTrue(new File(folder,saved.id+".png").isFile());tap("Settings tab");tap("Clear journal");text("Clear journal");assertEquals(0,folder.listFiles().length);
    }
    @After public void cleanup() throws Exception {context.stopService(new Intent(context,CaptureService.class));device.setOrientationNatural();}
}
