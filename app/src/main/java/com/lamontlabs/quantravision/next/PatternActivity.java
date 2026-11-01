package com.lamontlabs.quantravision.next;

import android.app.Activity;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;

public final class PatternActivity extends Activity {
    @Override public void onCreate(Bundle state){super.onCreate(state);PatternCatalog.Pattern p=PatternCatalog.get(getIntent().getStringExtra("pattern"));if(p==null){finish();return;}LinearLayout root=Ui.column(this);root.setBackgroundColor(Ui.BG);root.setFitsSystemWindows(true);ScrollView scroll=new ScrollView(this);LinearLayout body=Ui.column(this);body.setPadding(d(24),d(22),d(24),d(32));scroll.addView(body);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));TextView back=Ui.text(this,"‹  Pattern atlas",13,Ui.LIME,true);back.setPadding(0,d(12),0,d(20));back.setContentDescription("Back to atlas");back.setOnClickListener(v->finish());back.setPadding(d(24),d(18),d(24),d(18));root.addView(back,0,new LinearLayout.LayoutParams(-1,-2));Ui.add(body,Ui.eyebrow(this,p.family.toUpperCase(java.util.Locale.ROOT)+" / FIELD NOTES"));Ui.gap(body,12);Ui.add(body,Ui.display(this,p.title,38));Ui.gap(body,14);Ui.add(body,Ui.text(this,p.summary,15,Ui.MUTED,false));Ui.gap(body,24);LinearLayout diagram=Ui.card(this);diagram.addView(new PatternGlyph(this,p),new LinearLayout.LayoutParams(-1,d(160)));Ui.gap(diagram,12);Ui.add(diagram,Ui.mono(this,"SCHEMATIC · NOT MARKET DATA",9,Ui.MUTED));Ui.add(body,diagram);section(body,"WHAT THE ENGINE CHECKS",p.criteria);section(body,"WHAT REMAINS UNKNOWN",p.limits);section(body,"REAL-CHART EVIDENCE",EvidenceActivity.coverage(this,p.id));Ui.add(body,Ui.button(this,"Open evidence ledger",null,false,()->startActivity(new Intent(this,EvidenceActivity.class))));Ui.gap(body,12);Ui.add(body,Ui.button(this,"Read the chart reference",null,false,()->{try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(p.source)));}catch(ActivityNotFoundException ignored){}}));setContentView(root);}
    private int d(float n){return Ui.dp(this,n);}
    private void section(LinearLayout body,String title,String text){Ui.gap(body,27);Ui.add(body,Ui.eyebrow(this,title));Ui.gap(body,11);Ui.add(body,Ui.text(this,text,14,Ui.MUTED,false));Ui.gap(body,14);}
}
