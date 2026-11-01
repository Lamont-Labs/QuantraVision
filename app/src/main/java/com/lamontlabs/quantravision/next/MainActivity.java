package com.lamontlabs.quantravision.next;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.media.projection.*;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.text.InputFilter;
import android.view.*;
import android.widget.*;
import com.lamontlabs.quantravision.next.engine.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.*;

public final class MainActivity extends Activity {
    private static final int PICK=100,CROP=101,OVERLAY=102,PROJECTION=103,NOTIFICATIONS=104;
    private AppSession session;
    private LinearLayout root,body;
    private ScrollView scroll;
    private int tab=0,selectedMatch=0;
    private final int[] positions=new int[5];
    private String atlasQuery="",atlasFamily="All";
    private TextView studyTitle;
    private String pendingCapture;
    private boolean pendingCrop;
    private final Runnable sessionObserver=this::changed;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);session=((QuantraApplication)getApplication()).session;
        if(state!=null){atlasQuery=state.getString("atlasQuery","");atlasFamily=state.getString("atlasFamily","All");selectedMatch=state.getInt("match",0);tab=Math.max(0,Math.min(4,state.getInt("tab",0)));int[] p=state.getIntArray("positions");if(p!=null)System.arraycopy(p,0,positions,0,Math.min(p.length,5));}
        getWindow().setStatusBarColor(Ui.BG);getWindow().setNavigationBarColor(Ui.BG);
        if(Build.VERSION.SDK_INT>=30)getWindow().setDecorFitsSystemWindows(false);

        render();receive(getIntent());
    }
    @Override protected void onStart(){super.onStart();session.observe(sessionObserver);String id=getPreferences().getString("pending_capture","");if(!id.isEmpty()&&!id.equals(session.lastReceivedCapture)){pendingCapture=id;pendingCrop=false;}changed();}
    @Override protected void onStop(){session.stopObserving(sessionObserver);super.onStop();}
    @Override protected void onNewIntent(Intent intent){super.onNewIntent(intent);setIntent(intent);receive(intent);}
    @Override protected void onSaveInstanceState(Bundle state){if(scroll!=null)positions[tab]=scroll.getScrollY();state.putInt("tab",tab);state.putInt("match",selectedMatch);state.putString("atlasQuery",atlasQuery);state.putString("atlasFamily",atlasFamily);state.putIntArray("positions",positions);super.onSaveInstanceState(state);}
    private void changed() {
        if(pendingCapture!=null&&!session.busy){String id=pendingCapture;boolean crop=pendingCrop;pendingCapture=null;tab=1;positions[1]=0;if(!crop){session.lastReceivedCapture=id;getPreferences().edit().remove("pending_capture").commit();}session.importCapture(id,crop);return;}
        render();
    }
    private void receive(Intent intent) {
        if(intent==null)return;
        String evaluation=intent.getStringExtra("evaluation_asset");if(evaluation!=null){intent.removeExtra("evaluation_asset");if(!session.busy){tab=1;positions[1]=0;selectedMatch=0;session.importEvaluation(evaluation);}return;}
        String capture=intent.getStringExtra("capture_id");
        if(capture!=null){intent.removeExtra("capture_id");if(!capture.equals(session.lastReceivedCapture)){pendingCapture=capture;pendingCrop=false;positions[1]=0;changed();}return;}
        Uri uri=null;
        if(Intent.ACTION_SEND.equals(intent.getAction())){android.os.Parcelable shared=Build.VERSION.SDK_INT>=33?intent.getParcelableExtra(Intent.EXTRA_STREAM,Uri.class):intent.getParcelableExtra(Intent.EXTRA_STREAM);if(shared instanceof Uri)uri=(Uri)shared;}
        else if(Intent.ACTION_VIEW.equals(intent.getAction()))uri=intent.getData();
        if(uri!=null){intent.setAction(Intent.ACTION_MAIN);openUri(uri);}
    }
    private void openUri(Uri uri) {
        if(!"content".equals(uri.getScheme())){toast("Choose an image through your phone’s file picker.");return;}
        if(session.busy){toast("Wait for the current operation to finish.");return;}
        tab=1;positions[1]=0;selectedMatch=0;session.importUri(uri);
    }
    private void navigate(int destination){if(scroll!=null)positions[tab]=scroll.getScrollY();tab=destination;render();}
    private void render() {
        if(isFinishing()||isDestroyed())return;
        root=Ui.column(this);root.setBackgroundColor(Ui.BG);
        if(Build.VERSION.SDK_INT>=30)root.setOnApplyWindowInsetsListener((v,insets)->{android.graphics.Insets bars=insets.getInsets(WindowInsets.Type.systemBars());v.setPadding(bars.left,bars.top,bars.right,bars.bottom);return insets;});
        else root.setFitsSystemWindows(true);
        LinearLayout header=Ui.row(this);header.setPadding(dp(22),dp(16),dp(22),dp(16));header.addView(new Ui.Icon(this,"logo",Ui.LIME),new LinearLayout.LayoutParams(dp(25),dp(25)));TextView brand=Ui.text(this,"QuantraVision",16,Ui.INK,true);LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(0,-2,1);bp.leftMargin=dp(10);header.addView(brand,bp);header.addView(Ui.mono(this,"STUDIO / 4.1",9,Ui.LIME));header.setContentDescription("Settings tab");header.setOnClickListener(v->navigate(4));root.addView(header);root.addView(Ui.line(this));
        scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setClipToPadding(false);scroll.setVerticalScrollBarEnabled(false);
        final int visibleTab=tab;scroll.setOnScrollChangeListener((v,x,y,oldX,oldY)->positions[visibleTab]=y);
        body=Ui.column(this);body.setPadding(dp(22),dp(24),dp(22),dp(30));scroll.addView(body,new ScrollView.LayoutParams(-1,-2));root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        switch(tab){case 0:overview();break;case 1:analyze();break;case 2:atlas();break;case 3:journal();break;default:settings();}
        if(tab==1&&session.bitmap!=null&&session.result==null&&!session.busy){LinearLayout action=Ui.column(this);action.setPadding(dp(22),dp(10),dp(22),dp(12));Ui.add(action,Ui.button(this,"Analyze chart","scan",true,()->{positions[1]=0;session.analyze();}));root.addView(action);}
        root.addView(Ui.line(this));root.addView(nav(),new LinearLayout.LayoutParams(-1,dp(68)));setContentView(root);
        int y=positions[tab];ScrollView currentScroll=scroll;currentScroll.post(()->currentScroll.scrollTo(0,y));
    }
    private View nav() {
        LinearLayout nav=Ui.row(this);nav.setPadding(dp(5),0,dp(5),dp(5));String[] labels={"Workspace","Analyze","Atlas","Journal","More"},icons={"overview","scan","library","journal","settings"};
        for(int i=0;i<5;i++){final int index=i;LinearLayout item=Ui.column(this);item.setGravity(Gravity.CENTER);item.setContentDescription(labels[i]+" tab");item.setClickable(true);item.setFocusable(true);item.setSelected(i==tab);View mark=new View(this);mark.setBackgroundColor(i==tab?Ui.LIME:Ui.BG);LinearLayout.LayoutParams marker=new LinearLayout.LayoutParams(dp(26),dp(2));marker.bottomMargin=dp(12);item.addView(mark,marker);item.addView(new Ui.Icon(this,icons[i],i==tab?Ui.LIME:Ui.MUTED),new LinearLayout.LayoutParams(dp(20),dp(20)));Ui.gap(item,6);TextView label=Ui.text(this,labels[i],9.5f,i==tab?Ui.INK:Ui.MUTED,i==tab);item.addView(label);item.setOnClickListener(v->navigate(index));nav.addView(item,new LinearLayout.LayoutParams(0,-1,1));}return nav;
    }
    private void overview() {
        Ui.add(body,Ui.eyebrow(this,"YOUR CHART STUDIES"));Ui.gap(body,11);Ui.add(body,Ui.display(this,"The workspace.",39));Ui.gap(body,10);Ui.add(body,Ui.text(this,"A closer reading of what is on your chart.",13,Ui.MUTED,false));Ui.gap(body,25);
        if(session.bitmap==null){LinearLayout well=Ui.card(this);Ui.add(well,Ui.mono(this,"01 / START A STUDY",10,Ui.LIME));Ui.gap(well,22);Ui.add(well,Ui.display(this,"Bring your own chart.",27));Ui.gap(well,12);Ui.add(well,Ui.text(this,"Import a screenshot with at least 8 clear candles, or capture the chart on your screen.",13,Ui.MUTED,false));Ui.gap(well,20);Ui.add(well,Ui.mono(this,"PNG · JPG · WEBP",9,Ui.MUTED));Ui.add(body,well);}else{LinearLayout current=Ui.card(this);Ui.add(current,Ui.mono(this,session.source.startsWith("Sample")?"GENERATED EXAMPLE":"CURRENT STUDY",9,Ui.LIME));Ui.gap(current,14);ChartView chart=new ChartView(this,session.bitmap,session.result);chart.setInteractive(false);current.addView(chart,new LinearLayout.LayoutParams(-1,dp(140)));Ui.gap(current,12);Ui.add(current,Ui.text(this,session.result==null?"Continue this study":session.result.matches.isEmpty()?"Chart read · no matching rule":session.result.matches.get(0).title,15,Ui.INK,true));current.setContentDescription("Open current study");current.setOnClickListener(v->navigate(1));Ui.add(body,current);}
        Ui.gap(body,16);Ui.add(body,Ui.button(this,"Import screenshot","import",true,this::pick));Ui.gap(body,10);Ui.add(body,Ui.button(this,"Scan another app","screen",false,this::startCapture));Ui.gap(body,28);
        sectionRow("The pattern atlas","Explore all",()->navigate(2));Ui.gap(body,13);LinearLayout previews=Ui.row(this);addTile(previews,PatternCatalog.ALL.get(0),true);addTile(previews,PatternCatalog.ALL.get(2),false);Ui.add(body,previews);Ui.gap(body,24);
        java.util.List<AppStore.Record> entries=session.store.list();sectionRow("Recent studies",entries.isEmpty()?"":"View all",()->navigate(3));Ui.gap(body,12);if(entries.isEmpty())Ui.add(body,Ui.text(this,"Your completed studies and notes will appear here.",12,Ui.MUTED,false));else for(int i=0;i<Math.min(2,entries.size());i++){Ui.add(body,entry(entries.get(i)));Ui.gap(body,10);}
        Ui.gap(body,20);TextView sample=Ui.text(this,"Explore a generated example  ↗",12,Ui.LIME,true);sample.setPadding(0,dp(12),0,dp(12));sample.setContentDescription("Try sample chart");sample.setOnClickListener(v->sample());Ui.add(body,sample);Ui.gap(body,6);Ui.add(body,Ui.mono(this,"ON DEVICE / NO ACCOUNT REQUIRED",8,Ui.MUTED));
    }
    private void sectionRow(String title,String action,Runnable click){LinearLayout row=Ui.row(this);row.addView(Ui.text(this,title,17,Ui.INK,true),new LinearLayout.LayoutParams(0,-2,1));TextView link=Ui.text(this,action,11,Ui.LIME,true);link.setPadding(dp(9),dp(10),0,dp(10));link.setOnClickListener(v->click.run());row.addView(link);Ui.add(body,row);}
    private void addTile(LinearLayout row,PatternCatalog.Pattern pattern,boolean margin){LinearLayout tile=Ui.card(this);tile.setMinimumHeight(dp(174));tile.setPadding(dp(14),dp(14),dp(14),dp(14));tile.addView(new PatternGlyph(this,pattern),new LinearLayout.LayoutParams(-1,dp(60)));Ui.gap(tile,12);Ui.add(tile,Ui.text(this,pattern.title,13,Ui.INK,true));Ui.gap(tile,7);Ui.add(tile,Ui.mono(this,pattern.family.toUpperCase(Locale.ROOT)+" / SCHEMATIC",8,Ui.MUTED));tile.setContentDescription("Open pattern "+pattern.title);tile.setOnClickListener(v->openPattern(pattern.id));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1);if(margin)lp.rightMargin=dp(10);row.addView(tile,lp);}
    private void openPattern(String id){startActivity(new Intent(this,PatternActivity.class).putExtra("pattern",id));}
    private void atlas(){Ui.add(body,Ui.eyebrow(this,"12 IMPLEMENTED RULES"));Ui.gap(body,11);Ui.add(body,Ui.display(this,"The pattern atlas.",38));Ui.gap(body,10);Ui.add(body,Ui.text(this,"Understand the shape. Inspect the evidence.",13,Ui.MUTED,false));Ui.gap(body,23);EditText search=new EditText(this);search.setSingleLine(true);search.setTextSize(14);search.setTextColor(Ui.INK);search.setHintTextColor(Ui.MUTED);search.setHint("Search formations");search.setContentDescription("Search patterns");search.setBackground(Ui.shape(Ui.CARD,Ui.LINE,7,this));search.setPadding(dp(14),dp(12),dp(14),dp(12));search.setText(atlasQuery);Ui.add(body,search);Ui.gap(body,13);LinearLayout filters=Ui.row(this);for(String family:new String[]{"All","Structure","Candle"}){TextView filter=Ui.text(this,family,12,family.equals(atlasFamily)?Ui.INK:Ui.MUTED,true);filter.setPadding(dp(14),dp(10),dp(14),dp(10));filter.setBackground(Ui.shape(family.equals(atlasFamily)?Ui.CARD2:Ui.BG,0,6,this));filter.setContentDescription(family+" patterns");filter.setOnClickListener(v->{atlasFamily=family;positions[2]=0;render();});filters.addView(filter);}Ui.add(body,filters);Ui.gap(body,16);LinearLayout grid=Ui.column(this);Ui.add(body,grid);fillAtlas(grid);search.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence a,int b,int c,int d){}public void onTextChanged(CharSequence value,int start,int before,int count){atlasQuery=value.toString();fillAtlas(grid);}public void afterTextChanged(android.text.Editable e){}});Ui.gap(body,18);Ui.add(body,Ui.text(this,"Schematics explain geometry. Recognition performance is recorded separately for each rule.",11,Ui.MUTED,false));}
    private void fillAtlas(LinearLayout grid){grid.removeAllViews();LinearLayout row=null;int found=0;String query=atlasQuery.toLowerCase(Locale.ROOT).trim();for(PatternCatalog.Pattern p:PatternCatalog.ALL){if(!"All".equals(atlasFamily)&&!p.family.equals(atlasFamily))continue;if(!query.isEmpty()&&!(p.title+" "+p.summary).toLowerCase(Locale.ROOT).contains(query))continue;if(found%2==0){row=Ui.row(this);row.setGravity(Gravity.TOP);Ui.add(grid,row);Ui.gap(grid,10);}addTile(row,p,found%2==0);found++;}if(found%2==1)row.addView(new View(this),new LinearLayout.LayoutParams(0,1,1));if(found==0)Ui.add(grid,Ui.text(this,"No formations match this search.",14,Ui.MUTED,false));}
    private void analyze() {
        String title=session.result==null?"Analyze a chart":session.result.matches.isEmpty()?"Chart read":session.result.matches.get(0).title;
        String subtitle=session.result==null?"Inspect the original image and its measured geometry.":session.result.observationCount()+("trace".equals(session.result.inputType)?" trace samples read":" candles read")+" · "+session.result.matches.size()+" visual "+(session.result.matches.size()==1?"match":"matches");
        pageHeader(session.result==null?"CHART LAB":"ANALYSIS SAVED",title,subtitle);
        if(session.error!=null) {
            Ui.gap(body,14);LinearLayout error=Ui.card(this);Ui.add(error,Ui.text(this,"Let’s get a clearer chart",18,Ui.INK,true));Ui.gap(error,8);Ui.add(error,Ui.text(this,session.error,13,Ui.MUTED,false));
            if(session.bitmap!=null){Ui.gap(error,14);Ui.add(error,Ui.button(this,"Adjust crop","crop",false,this::crop));}Ui.add(body,error);
        }

        if(session.bitmap!=null&&session.result==null&&!session.busy){LinearLayout modes=Ui.row(this);TextView candles=toolbar(modes,"Candles","Read candles",()->{session.inputType="candles";render();});TextView trace=toolbar(modes,"Price trace · preview","Read trace",()->{session.inputType="trace";render();});boolean isTrace="trace".equals(session.inputType);candles.setSelected(!isTrace);trace.setSelected(isTrace);candles.setBackground(Ui.shape(!isTrace?Ui.CARD2:Ui.BG,0,6,this));trace.setBackground(Ui.shape(isTrace?Ui.CARD2:Ui.BG,0,6,this));Ui.add(body,modes);Ui.gap(body,9);Ui.add(body,Ui.text(this,isTrace?"Experimental: one price plot without overlays. Pixel samples are stored separately from candles.":"Candlestick reader · red/green or red/teal bodies with clear wicks.",11,Ui.MUTED,false));Ui.gap(body,17);}
        if(session.bitmap==null) {
            LinearLayout empty=Ui.card(this);Ui.Icon icon=new Ui.Icon(this,"scan",Ui.LIME);LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(dp(46),dp(46));ip.gravity=Gravity.CENTER;empty.addView(icon,ip);Ui.gap(empty,20);
            Ui.add(empty,Ui.text(this,"Bring your own chart",23,Ui.INK,true));Ui.gap(empty,9);Ui.add(empty,Ui.text(this,"Use a clear screenshot with at least 8 red/green or red/teal candles. Light and dark backgrounds are supported.",14,Ui.MUTED,false));Ui.gap(empty,22);
            Ui.add(empty,Ui.button(this,"Import chart","import",true,this::pick));Ui.gap(empty,10);Ui.add(empty,Ui.button(this,"Scan another app","screen",false,this::startCapture));Ui.add(body,empty);Ui.gap(body,12);Ui.add(body,Ui.button(this,"Try sample chart","play",false,this::sample));
        } else {
            LinearLayout imageCard=Ui.card(this);LinearLayout meta=Ui.row(this);meta.addView(Ui.pill(this,session.source.startsWith("Sample")?"GENERATED EXAMPLE":session.source.startsWith("Evaluation")?"EVALUATION IMAGE":"YOUR IMAGE",Ui.TEAL));
            TextView dims=Ui.text(this,session.bitmap.getWidth()+" × "+session.bitmap.getHeight(),11,Ui.MUTED,false);dims.setGravity(Gravity.RIGHT);meta.addView(dims,new LinearLayout.LayoutParams(0,-2,1));Ui.add(imageCard,meta);Ui.gap(imageCard,14);
            ChartView preview=new ChartView(this,session.bitmap,session.result);
            if(session.result!=null && !session.result.matches.isEmpty()){selectedMatch=Math.min(selectedMatch,session.result.matches.size()-1);preview.select(selectedMatch);}
            imageCard.addView(preview,new LinearLayout.LayoutParams(-1,dp(chartHeight())));Ui.gap(imageCard,12);
            LinearLayout controls=Ui.row(this);toolbar(controls,"Crop","Adjust crop",this::crop);toolbar(controls,"Fit","Fit chart",preview::resetTransform);if(session.result!=null){TextView outlines=toolbar(controls,"Outlines","Toggle outlines",()->{});outlines.setOnClickListener(v->{boolean show=!outlines.isSelected();outlines.setSelected(show);preview.annotations(show);outlines.setTextColor(show?Ui.LIME:Ui.MUTED);});outlines.setSelected(true);TextView full=toolbar(controls,"Full image","Show full image",()->{});full.setOnClickListener(v->{boolean show=!full.isSelected();full.setSelected(show);preview.focus(!show);preview.resetTransform();full.setText(show?"Focus":"Full image");});}else toolbar(controls,"Replace","Replace image",this::pick);Ui.add(imageCard,controls);Ui.add(body,imageCard);Ui.gap(body,16);

            if(session.result!=null)results(preview);
        }
        if(session.busy)progress();
        Ui.gap(body,24);Ui.add(body,Ui.text(this,"Visual observations only. Prices, volume and candle times cannot be read reliably from image geometry.",11,Ui.MUTED,false));
    }
    private int chartHeight(){if(session.bitmap==null)return 200;float aspect=session.bitmap.getHeight()/(float)session.bitmap.getWidth();if(session.result!=null&&!session.result.candles.isEmpty()){int l=session.bitmap.getWidth(),t=session.bitmap.getHeight(),r=0,b=0;for(ChartEngine.Candle c:session.result.candles){l=Math.min(l,c.left);t=Math.min(t,c.top);r=Math.max(r,c.right);b=Math.max(b,c.bottom);}aspect=(b-t+32f)/(r-l+32f);}float width=getResources().getDisplayMetrics().widthPixels/getResources().getDisplayMetrics().density-80;return Math.max(180,Math.min(320,Math.round(width*aspect)));}
    private TextView toolbar(LinearLayout row,String label,String description,Runnable click){TextView v=Ui.text(this,label,11,Ui.LIME,true);v.setContentDescription(description);v.setPadding(0,dp(14),0,dp(10));v.setMinimumHeight(dp(48));v.setGravity(Gravity.CENTER);v.setOnClickListener(a->click.run());row.addView(v,new LinearLayout.LayoutParams(0,-2,1));return v;}
    private void progress() {
        Ui.gap(body,16);LinearLayout box=Ui.card(this),row=Ui.row(this);ProgressBar spinner=new ProgressBar(this);spinner.setIndeterminateTintList(android.content.res.ColorStateList.valueOf(Ui.LIME));row.addView(spinner,new LinearLayout.LayoutParams(dp(24),dp(24)));
        TextView message=Ui.text(this,session.progress,14,Ui.INK,true);LinearLayout.LayoutParams mp=new LinearLayout.LayoutParams(0,-2,1);mp.leftMargin=dp(14);row.addView(message,mp);Ui.add(box,row);
        if(!session.committing()){Ui.gap(box,14);Ui.add(box,Ui.button(this,"Cancel operation","close",false,session::cancel));}Ui.add(body,box);
    }
    private void results(ChartView preview) {
        ChartEngine.Result result=session.result;
        LinearLayout summary=Ui.row(this);summary.addView(Ui.pill(this,result.observationCount()+("trace".equals(result.inputType)?" TRACE SAMPLES":" CANDLES READ"),Ui.TEAL));TextView elapsed=Ui.text(this,result.elapsedMs+" ms",11,Ui.MUTED,false);elapsed.setGravity(Gravity.RIGHT);summary.addView(elapsed,new LinearLayout.LayoutParams(0,-2,1));Ui.add(body,summary);Ui.gap(body,20);
        Ui.add(body,Ui.text(this,result.matches.isEmpty()?"Chart read. No rule matched.":result.matches.size()+" visual "+(result.matches.size()==1?"match":"matches"),23,Ui.INK,true));Ui.gap(body,9);
        Ui.add(body,Ui.text(this,result.matches.isEmpty()?"The visible geometry was measured, but did not satisfy the supported formation rules. This is a valid analysis, not a detector error.":"Tap a formation to inspect its outline and the geometry behind it.",13,Ui.MUTED,false));Ui.gap(body,16);
        for(int i=0;i<result.matches.size();i++) {
            ChartEngine.Match match=result.matches.get(i);final int index=i;LinearLayout card=Ui.card(this);LinearLayout head=Ui.row(this);
            PatternCatalog.Pattern pattern=PatternCatalog.get(PatternCatalog.key(match.id));if(pattern!=null)head.addView(new PatternGlyph(this,pattern),new LinearLayout.LayoutParams(dp(46),dp(36)));TextView title=Ui.text(this,match.title,16,Ui.INK,true);LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(0,-2,1);tp.leftMargin=dp(12);head.addView(title,tp);head.addView(new Ui.Icon(this,"arrow",Ui.LIME),new LinearLayout.LayoutParams(dp(19),dp(19)));Ui.add(card,head);Ui.gap(card,12);
            Ui.add(card,Ui.text(this,match.description,12,Ui.MUTED,false));Ui.gap(card,8);TextView reference=Ui.text(this,"Detection criteria & evidence  ↗",11,Ui.LIME,true);reference.setPadding(0,dp(12),0,dp(8));reference.setOnClickListener(v->openPattern(PatternCatalog.key(match.id)));Ui.add(card,reference);card.setContentDescription("Inspect "+match.title);card.setOnClickListener(v->{selectedMatch=index;preview.select(index);if(studyTitle!=null)studyTitle.setText(match.title);scroll.smoothScrollTo(0,0);});Ui.add(body,card);Ui.gap(body,10);
        }
        if(session.record!=null) {
            Ui.gap(body,6);Ui.add(body,Ui.text(this,"SAVED TO YOUR JOURNAL",10,Ui.TEAL,true));Ui.gap(body,12);
            Ui.add(body,Ui.button(this,"Share annotated chart","share",true,this::shareChart));Ui.gap(body,10);
            Ui.add(body,Ui.button(this,session.record.note.isEmpty()?"Add a note":"Edit note",null,false,this::editNote));
            if(!session.record.note.isEmpty()){Ui.gap(body,12);Ui.add(body,Ui.text(this,session.record.note,14,Ui.INK,false));}
        }
        Ui.gap(body,10);Ui.add(body,Ui.button(this,"Analyze another chart","import",false,()->{session.reset();positions[1]=0;pick();}));
    }
    private void journal() {
        java.util.List<AppStore.Record> entries=session.store.list();pageHeader("YOUR WORKSPACE","The journal.",entries.size()+" saved "+(entries.size()==1?"analysis":"analyses")+" · stored on this phone");
        if(entries.isEmpty()) {
            LinearLayout empty=Ui.card(this);Ui.add(empty,Ui.text(this,"A record of what you saw",23,Ui.INK,true));Ui.gap(empty,10);Ui.add(empty,Ui.text(this,"Completed analyses appear here with their original image, outlines, and your notes.",14,Ui.MUTED,false));Ui.gap(empty,22);Ui.add(empty,Ui.button(this,"Try sample chart","play",true,this::sample));Ui.add(body,empty);
        } else for(AppStore.Record r:entries) {Ui.add(body,entry(r));Ui.gap(body,12);}
    }
    private View entry(AppStore.Record record) {
        LinearLayout card=Ui.card(this);LinearLayout row=Ui.row(this);ImageView image=new ImageView(this);image.setScaleType(ImageView.ScaleType.CENTER_CROP);image.setImageBitmap(session.store.thumbnail(record));image.setBackground(Ui.shape(Ui.BG,0,10,this));image.setClipToOutline(true);row.addView(image,new LinearLayout.LayoutParams(dp(62),dp(62)));
        LinearLayout labels=Ui.column(this);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1);lp.leftMargin=dp(14);row.addView(labels,lp);
        String title=record.result.matches.isEmpty()?"No matching formation":record.result.matches.get(0).title;
        Ui.add(labels,Ui.text(this,title,15,Ui.INK,true));Ui.gap(labels,6);Ui.add(labels,Ui.text(this,date(record.created),11,Ui.MUTED,false));Ui.gap(labels,4);Ui.add(labels,Ui.text(this,record.result.observationCount()+("trace".equals(record.result.inputType)?" samples · ":" candles · ")+record.result.matches.size()+(record.result.matches.size()==1?" match":" matches"),11,Ui.TEAL,false));
        Ui.Icon arrow=new Ui.Icon(this,"arrow",Ui.MUTED);row.addView(arrow,new LinearLayout.LayoutParams(dp(20),dp(20)));Ui.add(card,row);
        if(!record.note.isEmpty()){Ui.gap(card,12);TextView note=Ui.text(this,record.note,12,Ui.MUTED,false);note.setMaxLines(2);Ui.add(card,note);}
        card.setContentDescription("Open analysis "+record.id);card.setOnClickListener(v->{if(session.busy){toast("Please wait for the current operation.");return;}tab=1;positions[1]=0;selectedMatch=0;session.open(record);});
        card.setOnLongClickListener(v->{delete(record);return true;});return card;
    }
    private void settings() {
        pageHeader("YOUR WORKSPACE","Preferences.","Your settings, evidence and data.");LinearLayout preferences=Ui.card(this);
        Ui.add(preferences,switchRow("Annotations on exports","Include measured outlines when sharing a chart.","annotations",true));Ui.gap(preferences,18);Ui.add(preferences,Ui.line(this));Ui.gap(preferences,18);Ui.add(preferences,switchRow("Touch feedback","A light haptic response on action buttons.","haptics",true));Ui.add(body,preferences);Ui.gap(body,24);
        Ui.add(body,Ui.eyebrow(this,"YOUR DATA"));Ui.gap(body,12);Ui.add(body,Ui.button(this,"Export journal as JSON","share",false,this::exportJournal));Ui.gap(body,10);Ui.add(body,Ui.button(this,"Clear journal",null,false,this::clearJournal));Ui.gap(body,24);
        LinearLayout privacy=Ui.card(this);Ui.add(privacy,Ui.pill(this,"LOCAL BY DESIGN",Ui.TEAL));Ui.gap(privacy,12);Ui.add(privacy,Ui.text(this,"Your charts stay yours",20,Ui.INK,true));Ui.gap(privacy,10);Ui.add(privacy,Ui.text(this,"Analysis runs on this phone. QuantraVision has no internet permission. Imports and screen captures are stored privately; sharing happens only when you choose it.",13,Ui.MUTED,false));Ui.add(body,privacy);Ui.gap(body,14);
        Ui.add(body,Ui.button(this,"Pattern atlas","library",false,()->navigate(2)));Ui.gap(body,10);Ui.add(body,Ui.button(this,"Evidence ledger",null,false,()->startActivity(new Intent(this,EvidenceActivity.class))));Ui.gap(body,10);Ui.add(body,Ui.button(this,"Try sample chart","play",false,this::sample));Ui.gap(body,10);Ui.add(body,Ui.button(this,"How to scan a chart",null,false,this::help));Ui.gap(body,24);Ui.add(body,Ui.text(this,"QuantraVision Studio 4.1 · Preview\nAndroid 8+ · local geometry engine\nJournal keeps the latest "+AppStore.LIMIT+" analyses.",11,Ui.MUTED,false));
    }
    private View switchRow(String title,String description,String key,boolean fallback) {
        LinearLayout row=Ui.row(this),labels=Ui.column(this);Ui.add(labels,Ui.text(this,title,15,Ui.INK,true));Ui.gap(labels,6);Ui.add(labels,Ui.text(this,description,12,Ui.MUTED,false));row.addView(labels,new LinearLayout.LayoutParams(0,-2,1));
        Switch control=new Switch(this);control.setContentDescription(title);control.setChecked(getPreferences().getBoolean(key,fallback));control.setOnCheckedChangeListener((v,checked)->getPreferences().edit().putBoolean(key,checked).commit());LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-2,-2);cp.leftMargin=dp(16);row.addView(control,cp);return row;
    }
    private void pageHeader(String eyebrow,String title,String description){Ui.add(body,Ui.eyebrow(this,eyebrow));Ui.gap(body,10);studyTitle=Ui.display(this,title,36);Ui.add(body,studyTitle);Ui.gap(body,10);Ui.add(body,Ui.text(this,description,13,Ui.MUTED,false));Ui.gap(body,24);}
    private android.content.SharedPreferences getPreferences(){return getSharedPreferences("app",0);}
    private int dp(float n){return Ui.dp(this,n);}
    private void toast(String value){Toast.makeText(this,value,Toast.LENGTH_SHORT).show();}
    private String date(long value){return DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.SHORT).format(new Date(value));}
    private boolean available(){if(session.busy){toast("Please wait for the current operation.");return false;}return true;}
    private void pick(){if(!available())return;Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("image/*").addCategory(Intent.CATEGORY_OPENABLE);try{startActivityForResult(intent,PICK);}catch(ActivityNotFoundException e){toast("No image picker is installed on this phone.");}}
    private void sample(){if(!available())return;tab=1;positions[1]=0;selectedMatch=0;session.sample();}
    private void crop(){if(!available()||session.bitmap==null)return;try{String id=session.store.writeInput(session.bitmap);startActivityForResult(new Intent(this,CropActivity.class).putExtra("image_id",id),CROP);}catch(Exception e){toast("Could not open the crop editor.");}}
    private void editNote(){if(session.record==null||!available())return;AppStore.Record record=session.record;EditText input=new EditText(this);input.setText(record.note);input.setTextColor(Ui.INK);input.setHint("What did you notice?");input.setMinLines(3);input.setMaxLines(8);input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(500)});input.setPadding(dp(24),dp(16),dp(24),dp(16));new AlertDialog.Builder(this).setTitle("Analysis note").setView(input).setNegativeButton("Cancel",null).setPositiveButton("Save note",(d,w)->{try{session.record=session.store.updateNote(record,input.getText().toString().trim());render();}catch(Exception e){toast("Could not save your note.");}}).show();}
    private void delete(AppStore.Record record){if(!available())return;new AlertDialog.Builder(this).setTitle("Delete this analysis?").setMessage("Its saved image, outlines and note will be removed from this phone.").setNegativeButton("Keep",null).setPositiveButton("Delete",(d,w)->{try{session.store.delete(record.id);if(session.record!=null&&session.record.id.equals(record.id)){session.record=null;session.result=null;}render();}catch(Exception e){toast("Could not delete this entry.");}}).show();}
    private void clearJournal(){if(!available())return;new AlertDialog.Builder(this).setTitle("Clear your journal?").setMessage("This removes all saved chart images, analyses and notes from this app.").setNegativeButton("Keep journal",null).setPositiveButton("Clear journal",(d,w)->{try{session.store.clear();session.reset();render();toast("Journal cleared");}catch(Exception e){toast("The journal could not be cleared.");}}).show();}
    private void shareChart(){if(session.bitmap==null||session.result==null||!available())return;try{Bitmap out=ChartView.export(session.bitmap,session.result,getPreferences().getBoolean("annotations",true),session.source);File f=ShareProvider.newFile(this,"png");try(FileOutputStream stream=new FileOutputStream(f)){if(!out.compress(Bitmap.CompressFormat.PNG,100,stream))throw new IOException();}out.recycle();share(f,"image/png");}catch(Exception e){toast("Could not prepare the chart export.");}}
    private void exportJournal(){if(!available())return;try{File f=ShareProvider.newFile(this,"json");try(FileOutputStream out=new FileOutputStream(f)){out.write(session.store.export().toString(2).getBytes(StandardCharsets.UTF_8));}share(f,"application/json");}catch(Exception e){toast("Could not prepare the journal export.");}}
    private void share(File file,String type){Uri uri=ShareProvider.uri(this,file);Intent intent=new Intent(Intent.ACTION_SEND).setType(type).putExtra(Intent.EXTRA_STREAM,uri).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);intent.setClipData(ClipData.newRawUri("QuantraVision export",uri));try{startActivity(Intent.createChooser(intent,"Share from QuantraVision"));}catch(ActivityNotFoundException e){toast("No compatible sharing app is installed.");}}
    private void supported(){new AlertDialog.Builder(this).setTitle("Supported visual formations").setMessage("STRUCTURES\nDouble top · Double bottom\nHead & shoulders · Inverse head & shoulders\nAscending · Descending · Symmetrical triangles\n\nCANDLE SHAPES\nDoji · Long upper/lower wick\nBullish/bearish engulfing bodies\n\nThese are geometric observations. Context, volume and a real price feed are required for trading interpretation.").setPositiveButton("Understood",null).show();}
    private void help(){new AlertDialog.Builder(this).setTitle("A clearer chart works better").setMessage("IMPORT\nChoose a clear PNG, JPG or WebP screenshot. Crop out toolbars and leave at least 8 complete candles.\n\nLIVE SCREEN\nAllow the floating control, approve Android screen capture, then open your chart app. Tap Scan in the floating control. Stop sharing from the control or Android’s notification.\n\nPALETTES\nFilled or hollow red/green and red/teal bodies with colored or gray wicks, on light or dark backgrounds.\n\nPRICE TRACE PREVIEW\nFor line or OHLC-bar plots, crop to the price panel and select Price trace. Annotations can confuse the reader; inspect the traced path. It measures pixels without deriving candle bodies, prices or OHLC.\n\nImages blocked by another app’s secure-screen setting cannot be captured. Use image import when available.").setPositiveButton("Got it",null).show();}
    private void startCapture() {
        if(!available())return;
        if(CaptureService.running){new AlertDialog.Builder(this).setTitle("Screen sharing is active").setMessage("Open your chart app and tap the floating Scan control, or stop this session.").setNegativeButton("Keep sharing",null).setPositiveButton("Stop sharing",(d,w)->{stopService(new Intent(this,CaptureService.class));toast("Screen sharing stopped");}).show();return;}
        if(!Settings.canDrawOverlays(this)){new AlertDialog.Builder(this).setTitle("Allow the floating scan control").setMessage("Android will open a permission screen. Enable QuantraVision so you can scan while viewing another app.").setNegativeButton("Later",null).setPositiveButton("Open settings",(d,w)->{try{startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getPackageName())),OVERLAY);}catch(ActivityNotFoundException e){toast("Overlay settings are unavailable on this phone.");}}).show();return;}
        if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},NOTIFICATIONS);return;}
        projectionConsent();
    }
    private void projectionConsent(){MediaProjectionManager manager=(MediaProjectionManager)getSystemService(MEDIA_PROJECTION_SERVICE);Intent intent=Build.VERSION.SDK_INT>=34?manager.createScreenCaptureIntent(MediaProjectionConfig.createConfigForDefaultDisplay()):manager.createScreenCaptureIntent();startActivityForResult(intent,PROJECTION);}
    @Override public void onRequestPermissionsResult(int request,String[] permissions,int[] grants){super.onRequestPermissionsResult(request,permissions,grants);if(request==NOTIFICATIONS)projectionConsent();}
    @Override protected void onActivityResult(int request,int result,Intent data) {
        super.onActivityResult(request,result,data);
        if(request==PICK&&result==RESULT_OK&&data!=null&&data.getData()!=null)openUri(data.getData());
        if(request==CROP&&result==RESULT_OK&&data!=null){pendingCapture=data.getStringExtra("crop_id");pendingCrop=true;changed();}
        if(request==OVERLAY){if(Settings.canDrawOverlays(this))startCapture();else toast("You can still analyze imported images without overlay permission.");}
        if(request==PROJECTION&&result==RESULT_OK&&data!=null){Intent service=new Intent(this,CaptureService.class).putExtra("result_code",result).putExtra("result_data",data);startForegroundService(service);toast("Open your chart app, then tap Scan in the floating control.");}
    }
    @Override public void onBackPressed(){if(tab!=0){navigate(0);return;}super.onBackPressed();}
}
