package com.lamontlabs.quantravision.next;

import android.content.*;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import java.io.*;
import java.util.UUID;

/** Read-only, narrowly scoped exports; no arbitrary app files are exposed. */
public final class ShareProvider extends ContentProvider {
    public static File newFile(Context c,String extension) throws IOException {
        if(!extension.matches("png|json"))throw new IOException("Unsupported export");
        File folder=new File(c.getCacheDir(),"exports");if(!folder.isDirectory()&&!folder.mkdirs())throw new IOException("Export storage unavailable");
        File[] old=folder.listFiles();if(old!=null)for(File f:old)if(System.currentTimeMillis()-f.lastModified()>86_400_000L)f.delete();
        return new File(folder,UUID.randomUUID()+"."+extension);
    }
    public static Uri uri(Context c,File file){return new Uri.Builder().scheme("content").authority(c.getPackageName()+".exports").appendPath(file.getName()).build();}
    private File resolve(Uri uri) throws FileNotFoundException {
        String name=uri.getLastPathSegment();
        if(uri.getPathSegments().size()!=1||name==null||!name.matches("[a-f0-9-]{36}\\.(png|json)"))throw new FileNotFoundException("Invalid export");
        File file=new File(new File(getContext().getCacheDir(),"exports"),name);if(!file.isFile())throw new FileNotFoundException("Export expired");return file;
    }
    @Override public boolean onCreate(){return true;}
    @Override public String getType(Uri uri){return uri.getPath()!=null&&uri.getPath().endsWith(".json")?"application/json":"image/png";}
    @Override public ParcelFileDescriptor openFile(Uri uri,String mode) throws FileNotFoundException {
        if(!"r".equals(mode))throw new FileNotFoundException("Read only");return ParcelFileDescriptor.open(resolve(uri),ParcelFileDescriptor.MODE_READ_ONLY);
    }
    @Override public Cursor query(Uri uri,String[] projection,String selection,String[] args,String order) {
        try {File file=resolve(uri);String[] columns=projection==null?new String[]{OpenableColumns.DISPLAY_NAME,OpenableColumns.SIZE}:projection;
            MatrixCursor cursor=new MatrixCursor(columns);Object[] row=new Object[columns.length];for(int i=0;i<columns.length;i++)row[i]=OpenableColumns.SIZE.equals(columns[i])?file.length():OpenableColumns.DISPLAY_NAME.equals(columns[i])?"QuantraVision-"+file.getName():null;
            cursor.addRow(row);return cursor;
        }catch(FileNotFoundException e){return null;}
    }
    @Override public Uri insert(Uri uri,ContentValues values){throw new UnsupportedOperationException("Read only");}
    @Override public int update(Uri uri,ContentValues values,String selection,String[] args){throw new UnsupportedOperationException("Read only");}
    @Override public int delete(Uri uri,String selection,String[] args){throw new UnsupportedOperationException("Read only");}
}
