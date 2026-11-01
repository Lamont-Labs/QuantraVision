package com.lamontlabs.quantravision.next;

import android.content.Context;
import android.graphics.*;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import com.lamontlabs.quantravision.next.engine.ChartEngine;
import com.lamontlabs.quantravision.next.engine.SamplePixels;
import java.io.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/** One serialized work owner survives activity recreation without retaining an Activity. */
public final class AppSession {
    private final Context app;
    public final AppStore store;
    private final ExecutorService executor=Executors.newSingleThreadExecutor();
    private final Handler main=new Handler(Looper.getMainLooper());
    public Bitmap bitmap;
    public AppStore.Record record;
    public ChartEngine.Result result;
    public String source="",error,progress="",inputType="candles";
    public String lastReceivedCapture="";
    public volatile boolean busy;
    private AtomicInteger token;
    private Runnable observer;
    AppSession(Context app) {
        this.app=app.getApplicationContext();store=new AppStore(app);
        try {
            String id=store.lastId();if(!id.isEmpty()){record=store.read(id);if(record!=null){bitmap=store.image(record);result=record.result;source=record.source;inputType=result.inputType;}}
        }catch(Exception ignored){record=null;}
        if(bitmap==null){bitmap=store.draft();if(bitmap!=null)source=store.draftSource();}
    }
    public void observe(Runnable observer){this.observer=observer;}
    public void stopObserving(Runnable departing){if(observer==departing)observer=null;}
    private void changed(){if(observer!=null)observer.run();}
    public void cancel(){if(token!=null && token.compareAndSet(0,1)){progress="Stopping…";changed();}}
    public boolean committing(){return token!=null&&token.get()==2;}
    private interface Job { Completed run(AtomicInteger token) throws Exception; }
    private static final class Completed {
        final Bitmap bitmap;final String source;final ChartEngine.Result result;final AppStore.Record record;
        final String problem;
        Completed(Bitmap b,String s,ChartEngine.Result r,AppStore.Record rec){this(b,s,r,rec,null);}
        Completed(Bitmap b,String s,ChartEngine.Result r,AppStore.Record rec,String problem){bitmap=b;source=s;result=r;record=rec;this.problem=problem;}
    }
    private static void check(AtomicInteger token){if(token.get()==1||Thread.currentThread().isInterrupted())throw new CancellationException();}
    private void work(String message,Job job) {
        if(busy)return;busy=true;error=null;progress=message;AtomicInteger active=new AtomicInteger(0);token=active;changed();
        executor.execute(()-> {
            Completed completed=null;String failure=null;
            try{completed=job.run(active);}catch(CancellationException ignored){}catch(Exception e){failure=e.getMessage()==null?"The operation could not finish. Please try again.":e.getMessage();}
            Completed value=completed;String problem=failure;
            main.post(()->{
                if(active.get()!=1 && value!=null){bitmap=value.bitmap;source=value.source;result=value.result;record=value.record;}
                if(active.get()!=1)error=value!=null?value.problem:problem;
                token=null;progress="";busy=false;changed();
            });
        });
    }
    public void importUri(Uri uri) {
        inputType="candles";
        work("Opening your image…",token->{Bitmap b=decode(uri);check(token);store.saveDraft(b,"Imported image");return new Completed(b,"Imported image",null,null);});
    }
    public void importEvaluation(String asset){
        if(!"oncs-hammer.png".equals(asset)&&!"nifty-qtstalker.png".equals(asset))return;inputType="candles";
        work("Opening evaluation image…",token->{Bitmap b;try(InputStream in=app.getAssets().open("evaluation/"+asset)){b=BitmapFactory.decodeStream(in);}if(b==null)throw new IOException("Evaluation image could not be opened");b=bounded(b);check(token);String origin="Evaluation · "+asset;store.saveDraft(b,origin);return new Completed(b,origin,null,null);});
    }
    public void importCapture(String id,boolean crop) {
        if(!crop)inputType="candles";
        work(crop?"Applying crop…":"Analyzing screen capture…",token->{
            File f=store.input(id);Bitmap b=BitmapFactory.decodeFile(f.getPath());if(b==null)throw new IOException("The captured image is unavailable. Try again.");
            b=bounded(b);check(token);String s=crop?source+" · cropped":"Live screen";
            if(crop){if(!token.compareAndSet(0,2))throw new CancellationException();store.saveDraft(b,s);f.delete();return new Completed(b,s,null,null);}
            ChartEngine.Result measured=measure(b,token);check(token);
            if(!token.compareAndSet(0,2))throw new CancellationException();
            if(!measured.readable()){store.saveDraft(b,s);f.delete();return new Completed(b,s,null,null,measured.problem);}
            AppStore.Record saved=store.save(b,measured,s);f.delete();return new Completed(b,s,measured,saved);
        });
    }
    public void sample() {
        inputType="candles";
        work("Preparing sample chart…",token->{
            Bitmap b=Bitmap.createBitmap(SamplePixels.WIDTH,SamplePixels.HEIGHT,Bitmap.Config.ARGB_8888);
            b.setPixels(SamplePixels.create(false,false),0,SamplePixels.WIDTH,0,0,SamplePixels.WIDTH,SamplePixels.HEIGHT);
            Canvas canvas=new Canvas(b);Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);p.setColor(0xff9eafc6);p.setTextSize(16);p.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));
            canvas.drawText("SAMPLE CHART  /  GENERATED DEMONSTRATION",24,27,p);
            check(token);store.saveDraft(b,"Sample chart");return new Completed(b,"Sample chart",null,null);
        });
    }
    public void analyze() {
        if(bitmap==null||busy)return;Bitmap image=bitmap;String origin=source;
        work("trace".equals(inputType)?"Reading the price trace…":"Reading candle geometry…",token->{
            ChartEngine.Result measured=measure(image,token);check(token);
            if(!measured.readable())throw new IOException(measured.problem);
            if(!token.compareAndSet(0,2))throw new CancellationException();
            AppStore.Record saved=store.save(image,measured,origin);return new Completed(image,origin,measured,saved);
        });
    }
    public void open(AppStore.Record saved) {
        inputType=saved.result.inputType;
        work("Opening saved analysis…",token->{Bitmap b=store.image(saved);check(token);store.remember(saved);return new Completed(b,saved.source,saved.result,saved);});
    }
    public void reset() {
        if(busy)return;bitmap=null;record=null;result=null;error=null;source="";inputType="candles";
        app.getSharedPreferences("app",0).edit().remove("last_record").remove("draft_source").commit();new File(app.getFilesDir(),"draft.png").delete();changed();
    }
    private ChartEngine.Result measure(Bitmap image,AtomicInteger token) {
        int w=image.getWidth(),h=image.getHeight();int[] pixels=new int[w*h];image.getPixels(pixels,0,w,0,0,w,h);
        return "trace".equals(inputType)?new ChartEngine().analyzeTrace(w,h,pixels,()->check(token)):new ChartEngine().analyze(w,h,pixels,()->check(token));
    }
    private Bitmap decode(Uri uri) throws IOException {
        BitmapFactory.Options bounds=new BitmapFactory.Options();bounds.inJustDecodeBounds=true;
        try(InputStream in=app.getContentResolver().openInputStream(uri)){if(in==null)throw new IOException("This image could not be opened");BitmapFactory.decodeStream(in,null,bounds);}
        if(bounds.outWidth<=0||bounds.outHeight<=0)throw new IOException("Unsupported image. Choose a PNG, JPG or WebP chart screenshot.");
        BitmapFactory.Options options=new BitmapFactory.Options();options.inPreferredConfig=Bitmap.Config.ARGB_8888;
        while(Math.max(bounds.outWidth,bounds.outHeight)/(options.inSampleSize==0?1:options.inSampleSize)>2000)
            options.inSampleSize=options.inSampleSize<=1?2:options.inSampleSize*2;
        try(InputStream in=app.getContentResolver().openInputStream(uri)){Bitmap b=BitmapFactory.decodeStream(in,null,options);if(b==null)throw new IOException("Image decoding failed");return bounded(b);}
        catch(SecurityException e){throw new IOException("Image access expired. Select the image again.",e);}
    }
    public static Bitmap bounded(Bitmap source) {
        int longest=Math.max(source.getWidth(),source.getHeight());if(longest<=1600)return source;
        float scale=1600f/longest;Bitmap b=Bitmap.createScaledBitmap(source,Math.max(1,Math.round(source.getWidth()*scale)),Math.max(1,Math.round(source.getHeight()*scale)),true);if(b!=source)source.recycle();return b;
    }
}
