package com.lamontlabs.quantravision.next;

import android.app.*;
import android.content.*;
import android.content.pm.ServiceInfo;
import android.graphics.*;
import android.hardware.display.*;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.*;
import android.os.*;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.*;
import android.widget.*;
import java.util.concurrent.atomic.AtomicBoolean;

/** One consent token, one virtual display, explicit tap-to-capture, explicit stop. */
public final class CaptureService extends Service {
    public static volatile boolean running;
    private static final String STOP="com.lamontlabs.quantravision.next.STOP";
    private final Handler main=new Handler(Looper.getMainLooper());
    private final AtomicBoolean requested=new AtomicBoolean();
    private HandlerThread thread;private Handler frames;
    private MediaProjection projection;private MediaProjection.Callback callback;
    private VirtualDisplay display;private ImageReader reader;
    private WindowManager wm;private View overlay;private TextView scan;
    private WindowManager.LayoutParams params;
    private volatile boolean stopping;
    private volatile long earliest;
    private int width,height,dpi;
    private final Runnable timeout=()->{if(requested.getAndSet(false)){showControl();Toast.makeText(this,"No screen frame arrived. Try again, or import a screenshot.",Toast.LENGTH_LONG).show();}};

    @Override public void onCreate(){super.onCreate();wm=(WindowManager)getSystemService(WINDOW_SERVICE);thread=new HandlerThread("chart-capture");thread.start();frames=new Handler(thread.getLooper());}
    @Override public int onStartCommand(Intent intent,int flags,int id) {
        if(intent==null||STOP.equals(intent.getAction())){stopSelf();return START_NOT_STICKY;}
        if(projection!=null)return START_NOT_STICKY;
        Intent data=Build.VERSION.SDK_INT>=33?intent.getParcelableExtra("result_data",Intent.class):intent.getParcelableExtra("result_data");
        int code=intent.getIntExtra("result_code",Activity.RESULT_CANCELED);
        if(code!=Activity.RESULT_OK||data==null||!Settings.canDrawOverlays(this)){stopSelf();return START_NOT_STICKY;}
        try {
            NotificationManager notifications=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            notifications.createNotificationChannel(new NotificationChannel("screen_capture","Screen capture",NotificationManager.IMPORTANCE_LOW));
            PendingIntent open=PendingIntent.getActivity(this,0,new Intent(this,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent stop=PendingIntent.getService(this,1,new Intent(this,CaptureService.class).setAction(STOP),PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
            Notification notification=new Notification.Builder(this,"screen_capture").setSmallIcon(R.drawable.ic_launcher).setContentTitle("QuantraVision screen scan")
                    .setContentText("Tap Scan on the floating control. Stop when you’re done.").setOngoing(true).setContentIntent(open).addAction(new Notification.Action.Builder(null,"Stop sharing",stop).build()).build();
            if(Build.VERSION.SDK_INT>=29)startForeground(40,notification,ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);else startForeground(40,notification);
            projection=((MediaProjectionManager)getSystemService(MEDIA_PROJECTION_SERVICE)).getMediaProjection(code,data);
            if(projection==null)throw new IllegalStateException("Screen permission was not granted");
            callback=new MediaProjection.Callback(){
                @Override public void onStop(){main.post(()->stopSelf());}
                @Override public void onCapturedContentResize(int w,int h){frames.post(()->resize(w,h));}
            };
            projection.registerCallback(callback,frames);
            DisplayMetrics metrics=getResources().getDisplayMetrics();int w,h;
            if(Build.VERSION.SDK_INT>=30){Rect bounds=wm.getMaximumWindowMetrics().getBounds();w=bounds.width();h=bounds.height();}
            else {DisplayMetrics real=new DisplayMetrics();wm.getDefaultDisplay().getRealMetrics(real);w=real.widthPixels;h=real.heightPixels;}
            float factor=Math.min(1,1600f/Math.max(w,h));width=Math.max(1,Math.round(w*factor));height=Math.max(1,Math.round(h*factor));dpi=Math.max(1,Math.round(metrics.densityDpi*factor));
            reader=makeReader(width,height);
            display=projection.createVirtualDisplay("QuantraVision single-frame capture",width,height,dpi,DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,reader.getSurface(),null,frames);
            if(display==null)throw new IllegalStateException("Android did not create a capture display");
            addControl();running=true;
        } catch(Exception e){Toast.makeText(this,"Could not start screen capture. Please approve a new session and try again.",Toast.LENGTH_LONG).show();stopSelf();}
        return START_NOT_STICKY;
    }
    private ImageReader makeReader(int w,int h) {
        ImageReader next=ImageReader.newInstance(w,h,PixelFormat.RGBA_8888,2);next.setOnImageAvailableListener(this::onFrame,frames);return next;
    }
    private void resize(int w,int h) {
        if(stopping||display==null||w<1||h<1)return;
        float factor=Math.min(1,1600f/Math.max(w,h));int nw=Math.max(1,Math.round(w*factor)),nh=Math.max(1,Math.round(h*factor));
        if(nw==width&&nh==height)return;
        ImageReader old=reader;
        try{ImageReader next=makeReader(nw,nh);display.resize(nw,nh,dpi);display.setSurface(next.getSurface());reader=next;width=nw;height=nh;if(old!=null)old.close();}
        catch(Exception e){main.post(()->stopSelf());}
    }
    private void onFrame(ImageReader source) {
        Image image=null;Bitmap padded=null,bitmap=null;
        try {
            if(stopping)return;image=source.acquireLatestImage();if(image==null)return;
            if(!requested.get()||SystemClock.uptimeMillis()<earliest)return;
            if(!requested.compareAndSet(true,false))return;
            android.util.Log.i("QV.Capture","Reading requested frame "+image.getWidth()+"x"+image.getHeight());
            Image.Plane plane=image.getPlanes()[0];int stride=plane.getRowStride()/plane.getPixelStride();
            padded=Bitmap.createBitmap(stride,image.getHeight(),Bitmap.Config.ARGB_8888);padded.copyPixelsFromBuffer(plane.getBuffer());
            bitmap=Bitmap.createBitmap(padded,0,0,image.getWidth(),image.getHeight());
            String capture=((QuantraApplication)getApplication()).session.store.writeInput(bitmap);
            getSharedPreferences("app",0).edit().putString("pending_capture",capture).commit();
            main.post(()->{
                main.removeCallbacks(timeout);if(stopping)return;showControl();
                Intent open=new Intent(this,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP).putExtra("capture_id",capture);
                main.postDelayed(()->{if(!stopping)try{startActivity(open);}catch(Exception e){Toast.makeText(this,"Open QuantraVision to view the captured chart.",Toast.LENGTH_LONG).show();}},150);
            });
        } catch(Exception e){android.util.Log.w("QV.Capture","Could not read screen frame",e);requested.set(false);main.post(()->{main.removeCallbacks(timeout);showControl();Toast.makeText(this,"Screen capture failed. Try another frame or import a screenshot.",Toast.LENGTH_LONG).show();});}
        finally {if(image!=null)image.close();if(bitmap!=null&&bitmap!=padded)bitmap.recycle();if(padded!=null)padded.recycle();}
    }
    private void capture() {
        if(stopping||requested.get())return;
        if(((QuantraApplication)getApplication()).session.busy){Toast.makeText(this,"Let the current analysis finish first.",Toast.LENGTH_SHORT).show();return;}
        if(overlay!=null)overlay.setVisibility(View.INVISIBLE);
        earliest=Long.MAX_VALUE;requested.set(true);main.removeCallbacks(timeout);main.postDelayed(timeout,5000);
        // Static screens need not emit another damage frame. Reattach the existing
        // display's surface after the control is hidden; never reuse the consent token.
        frames.postDelayed(()->{
            if(stopping||!requested.get()||display==null||reader==null)return;
            try {
                display.setSurface(null);
                try(Image previous=reader.acquireLatestImage()){ }
                earliest=SystemClock.uptimeMillis();display.setSurface(reader.getSurface());
                android.util.Log.i("QV.Capture","Requested a fresh screen frame");
            } catch(Exception e){android.util.Log.w("QV.Capture","Could not refresh capture surface",e);requested.set(false);main.post(()->{main.removeCallbacks(timeout);showControl();Toast.makeText(this,"Could not refresh the screen. Start a new capture session.",Toast.LENGTH_LONG).show();});}
        },160);
    }
    private void showControl(){if(overlay!=null&&!stopping)overlay.setVisibility(View.VISIBLE);}
    private void addControl() {
        LinearLayout control=Ui.row(this);control.setPadding(Ui.dp(this,4),Ui.dp(this,4),Ui.dp(this,4),Ui.dp(this,4));control.setBackground(Ui.shape(Ui.CARD,Ui.LINE,40,this));control.setElevation(Ui.dp(this,8));
        TextView handle=Ui.text(this,"⋮",24,Ui.MUTED,true);handle.setGravity(Gravity.CENTER);handle.setContentDescription("Move scan control");control.addView(handle,new LinearLayout.LayoutParams(Ui.dp(this,28),Ui.dp(this,46)));
        scan=Ui.text(this,"Scan",15,Ui.BG,true);scan.setGravity(Gravity.CENTER);scan.setBackground(Ui.shape(Ui.LIME,0,30,this));scan.setContentDescription("Capture chart now");scan.setOnClickListener(v->capture());control.addView(scan,new LinearLayout.LayoutParams(Ui.dp(this,94),Ui.dp(this,46)));
        TextView stop=Ui.text(this,"×",24,Ui.INK,false);stop.setGravity(Gravity.CENTER);stop.setContentDescription("Stop screen sharing");stop.setOnClickListener(v->stopSelf());control.addView(stop,new LinearLayout.LayoutParams(Ui.dp(this,46),Ui.dp(this,46)));
        params=new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,PixelFormat.TRANSLUCENT);
        params.gravity=Gravity.TOP|Gravity.RIGHT;params.x=Ui.dp(this,12);params.y=Ui.dp(this,160);
        handle.setOnTouchListener(new View.OnTouchListener(){float x,y;int px,py;
            @Override public boolean onTouch(View v,MotionEvent e){switch(e.getActionMasked()){
                case MotionEvent.ACTION_DOWN:x=e.getRawX();y=e.getRawY();px=params.x;py=params.y;return true;
                case MotionEvent.ACTION_MOVE:params.x=Math.max(0,Math.min(getResources().getDisplayMetrics().widthPixels-Ui.dp(CaptureService.this,180),px-Math.round(e.getRawX()-x)));params.y=Math.max(Ui.dp(CaptureService.this,30),Math.min(getResources().getDisplayMetrics().heightPixels-Ui.dp(CaptureService.this,100),py+Math.round(e.getRawY()-y)));if(overlay!=null)wm.updateViewLayout(overlay,params);return true;
                case MotionEvent.ACTION_UP:v.performClick();return true;default:return false;
            }} });
        overlay=control;wm.addView(overlay,params);
    }
    @Override public void onDestroy() {
        stopping=true;running=false;requested.set(false);main.removeCallbacks(timeout);
        if(overlay!=null){try{wm.removeView(overlay);}catch(Exception ignored){}overlay=null;}
        if(frames!=null)frames.post(()->{
            if(display!=null){display.release();display=null;}if(reader!=null){reader.close();reader=null;}
            if(projection!=null){try{projection.unregisterCallback(callback);projection.stop();}catch(Exception ignored){}projection=null;}
            thread.quitSafely();
        });
        stopForeground(STOP_FOREGROUND_REMOVE);super.onDestroy();
    }
    @Override public IBinder onBind(Intent intent){return null;}
}
