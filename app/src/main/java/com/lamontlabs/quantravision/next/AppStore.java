package com.lamontlabs.quantravision.next;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AtomicFile;
import com.lamontlabs.quantravision.next.engine.ChartEngine;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Each journal entry is an image plus an atomically committed measurement record. */
public final class AppStore {
    private final Context context;
    private final File folder;
    public static final int LIMIT=40;
    public static final class Record {
        public final String id,source,note;
        public final long created;
        public final ChartEngine.Result result;
        Record(String id,long created,String source,String note,ChartEngine.Result result) {
            this.id=id;this.created=created;this.source=source;this.note=note;this.result=result;
        }
    }
    AppStore(Context context) {
        this.context=context.getApplicationContext();folder=new File(context.getFilesDir(),"journal");
        if(!folder.isDirectory())folder.mkdirs();
    }
    private File file(String id,String suffix) throws IOException {
        if(!id.matches("[a-f0-9-]{36}") || !UUID.fromString(id).toString().equals(id))throw new IOException("Invalid record ID");
        return new File(folder,id+suffix);
    }
    public synchronized Record save(Bitmap bitmap,ChartEngine.Result result,String source) throws Exception {
        if(!result.readable())throw new IOException("Unreadable charts cannot be saved as analyses");
        String id=UUID.randomUUID().toString();Record record=new Record(id,System.currentTimeMillis(),source,"",result);
        File image=file(id,".png"),metadata=file(id,".json");
        try {
            writeBitmap(new AtomicFile(image),bitmap);
            writeJson(metadata,toJson(record));
        } catch(Exception error) { image.delete();new AtomicFile(metadata).delete();throw error; }
        List<Record> all=list();for(int i=LIMIT;i<all.size();i++)delete(all.get(i).id);
        context.getSharedPreferences("app",0).edit().putString("last_record",id).commit();
        return record;
    }
    public synchronized List<Record> list() {
        ArrayList<Record> result=new ArrayList<>();File[] files=folder.listFiles((f,n)->n.endsWith(".json"));
        if(files!=null)for(File f:files)try { Record r=read(f.getName().substring(0,36));if(r!=null)result.add(r); }catch(Exception ignored) { }
        result.sort((a,b)->Long.compare(b.created,a.created));return result;
    }
    public synchronized Record read(String id) throws Exception {
        File json=file(id,".json");if(!json.isFile()||!file(id,".png").isFile())return null;
        JSONObject data=new JSONObject(readText(new AtomicFile(json).openRead(),1_000_000));
        if(!"geometry-1".equals(data.getString("engine")))throw new IOException("Unsupported analysis format");
        int w=data.getInt("width"),h=data.getInt("height");
        if(w<1||h<1||(long)w*h>4_000_000)throw new IOException("Invalid image dimensions");
        ArrayList<ChartEngine.Candle> candles=new ArrayList<>();JSONArray cs=data.getJSONArray("candles");
        if(cs.length()<8||cs.length()>250)throw new IOException("Invalid candle count");
        for(int i=0;i<cs.length();i++) {
            JSONArray c=cs.getJSONArray(i);ChartEngine.Candle candle=new ChartEngine.Candle(c.getInt(0),c.getInt(1),c.getInt(2),c.getInt(3),c.getInt(4),c.getInt(5),c.getBoolean(6));
            if(candle.right>w||candle.bottom>h)throw new IOException("Candle outside image");candles.add(candle);
        }
        ArrayList<ChartEngine.Match> matches=new ArrayList<>();JSONArray ms=data.getJSONArray("matches");
        if(ms.length()>10)throw new IOException("Too many matches");
        for(int i=0;i<ms.length();i++) {
            JSONObject m=ms.getJSONObject(i);int first=m.getInt("first"),last=m.getInt("last");
            if(first<0||last<first||last>=candles.size())throw new IOException("Invalid match range");
            ArrayList<ChartEngine.Point> points=new ArrayList<>();JSONArray ps=m.getJSONArray("points");
            if(ps.length()>8)throw new IOException("Invalid outline");
            for(int k=0;k<ps.length();k++) {
                JSONArray p=ps.getJSONArray(k);float x=(float)p.getDouble(0),y=(float)p.getDouble(1);
                if(!Float.isFinite(x)||!Float.isFinite(y)||x<0||y<0||x>w||y>h)throw new IOException("Invalid outline point");
                points.add(new ChartEngine.Point(x,y));
            }
            matches.add(new ChartEngine.Match(m.getString("kind"),m.getString("title"),m.getString("description"),m.getString("family"),first,last,points,candles));
        }
        ChartEngine.Result measured=new ChartEngine.Result(w,h,candles,matches,null,data.optLong("elapsed_ms",0));
        return new Record(id,data.getLong("created"),data.optString("source","Imported chart"),data.optString("note",""),measured);
    }
    public Bitmap image(Record record) throws IOException {
        Bitmap b=BitmapFactory.decodeFile(file(record.id,".png").getPath());
        if(b==null||b.getWidth()!=record.result.width||b.getHeight()!=record.result.height)throw new IOException("The saved image is unavailable");
        return b;
    }
    public Bitmap thumbnail(Record record) {
        try { BitmapFactory.Options o=new BitmapFactory.Options();o.inSampleSize=4;return BitmapFactory.decodeFile(file(record.id,".png").getPath(),o); }
        catch(Exception e){return null;}
    }
    public synchronized Record updateNote(Record r,String note) throws Exception {
        Record edited=new Record(r.id,r.created,r.source,note.substring(0,Math.min(note.length(),500)),r.result);
        writeJson(file(r.id,".json"),toJson(edited));return edited;
    }
    public synchronized void delete(String id) throws IOException {
        new AtomicFile(file(id,".json")).delete();new AtomicFile(file(id,".png")).delete();
        if(id.equals(context.getSharedPreferences("app",0).getString("last_record","")))
            context.getSharedPreferences("app",0).edit().remove("last_record").commit();
    }
    public synchronized void clear() throws IOException {
        File[] files=folder.listFiles();if(files!=null)for(File f:files)if(f.isFile()&&!f.delete())throw new IOException("Could not remove a journal file");
        context.getSharedPreferences("app",0).edit().remove("last_record").commit();
    }
    public synchronized JSONObject export() throws Exception {
        JSONObject root=new JSONObject().put("app","QuantraVision").put("format",1).put("exported",System.currentTimeMillis());
        JSONArray entries=new JSONArray();for(Record r:list())entries.put(toJson(r));return root.put("analyses",entries);
    }
    public void saveDraft(Bitmap bitmap,String source) throws IOException {
        writeBitmap(new AtomicFile(new File(context.getFilesDir(),"draft.png")),bitmap);
        context.getSharedPreferences("app",0).edit().putString("draft_source",source).remove("last_record").commit();
    }
    public Bitmap draft() { return BitmapFactory.decodeFile(new File(context.getFilesDir(),"draft.png").getPath()); }
    public String draftSource() { return context.getSharedPreferences("app",0).getString("draft_source","Restored image"); }
    public String lastId() { return context.getSharedPreferences("app",0).getString("last_record",""); }
    public void remember(Record r) { context.getSharedPreferences("app",0).edit().putString("last_record",r.id).commit(); }
    public File input(String id) throws IOException {
        if(!id.matches("[a-f0-9-]{36}"))throw new IOException("Invalid capture ID");
        File inputs=new File(context.getCacheDir(),"incoming");if(!inputs.isDirectory())inputs.mkdirs();
        return new File(inputs,id+".png");
    }
    public String writeInput(Bitmap image) throws IOException {
        String id=UUID.randomUUID().toString();writeBitmap(new AtomicFile(input(id)),image);return id;
    }
    static void writeBitmap(AtomicFile target,Bitmap bitmap) throws IOException {
        FileOutputStream out=null;
        try { out=target.startWrite();if(!bitmap.compress(Bitmap.CompressFormat.PNG,100,out))throw new IOException("Image encoding failed");target.finishWrite(out); }
        catch(Exception e){if(out!=null)target.failWrite(out);throw new IOException("Could not save image",e);}
    }
    private static void writeJson(File file,JSONObject data) throws IOException {
        AtomicFile target=new AtomicFile(file);FileOutputStream out=null;
        try {out=target.startWrite();out.write(data.toString().getBytes(StandardCharsets.UTF_8));target.finishWrite(out);}
        catch(Exception e){if(out!=null)target.failWrite(out);throw new IOException("Could not save analysis",e);}
    }
    static String readText(InputStream source,int limit) throws IOException {
        try(InputStream in=source;ByteArrayOutputStream out=new ByteArrayOutputStream()) {
            byte[] b=new byte[4096];int n,total=0;
            while((n=in.read(b))!=-1){total+=n;if(total>limit)throw new IOException("File too large");out.write(b,0,n);}
            return new String(out.toByteArray(),StandardCharsets.UTF_8);
        }
    }
    private static JSONObject toJson(Record r) throws Exception {
        JSONObject data=new JSONObject().put("id",r.id).put("created",r.created).put("source",r.source).put("note",r.note)
                .put("engine",ChartEngine.VERSION).put("width",r.result.width).put("height",r.result.height).put("elapsed_ms",r.result.elapsedMs);
        JSONArray candles=new JSONArray();
        for(ChartEngine.Candle c:r.result.candles)candles.put(new JSONArray(Arrays.asList(c.left,c.right,c.top,c.bottom,c.bodyTop,c.bodyBottom,c.bullish)));
        JSONArray matches=new JSONArray();
        for(ChartEngine.Match m:r.result.matches) {
            int last=m.id.lastIndexOf('_'),previous=m.id.lastIndexOf('_',last-1);
            JSONObject value=new JSONObject().put("kind",m.id.substring(0,previous)).put("title",m.title).put("description",m.description)
                    .put("family",m.family).put("first",m.first).put("last",m.last);
            JSONArray points=new JSONArray();for(ChartEngine.Point p:m.points)points.put(new JSONArray(Arrays.asList(p.x,p.y)));
            matches.put(value.put("points",points));
        }
        return data.put("candles",candles).put("matches",matches);
    }
}
