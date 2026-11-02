package com.lamontlabs.quantravision.benchmark;
import com.lamontlabs.quantravision.next.engine.ChartEngine;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.*;
import java.util.*;
public final class ScanImage {
    static String quote(String s){return "\""+s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\r","\\r")+"\"";}
    public static void main(String[] args)throws Exception{
        String mode=args[0];ChartEngine engine=new ChartEngine();
        for(int n=1;n<args.length;n++){
            BufferedImage image=ImageIO.read(new File(args[n]));if(image==null)throw new IOException("Unreadable evaluation input");int w=image.getWidth(),h=image.getHeight();int[] px=image.getRGB(0,0,w,h,null,0,w);ChartEngine.Result result;
            if("trace".equals(mode)){try{result=(ChartEngine.Result)ChartEngine.class.getMethod("analyzeTrace",int.class,int.class,int[].class).invoke(engine,w,h,px);}catch(NoSuchMethodException absent){System.out.println("{\"readable\":false,\"unsupported_mode\":true,\"count\":0,\"matches\":[],\"problem\":\"Baseline has no trace reader\"}");continue;}}else result=engine.analyze(w,h,px);
            int count=result.candles.size();try{count=(Integer)result.getClass().getMethod("observationCount").invoke(result);}catch(ReflectiveOperationException ignored){}
            StringBuilder out=new StringBuilder("{\"readable\":").append(result.readable()).append(",\"count\":").append(count).append(",\"matches\":[");boolean first=true;
            for(ChartEngine.Match m:result.matches){if(!first)out.append(',');first=false;int last=m.id.lastIndexOf('_'),previous=m.id.lastIndexOf('_',last-1);out.append("{\"kind\":").append(quote(m.id.substring(0,previous))).append(",\"box\":[").append(m.left).append(',').append(m.top).append(',').append(m.right).append(',').append(m.bottom).append("],\"points\":[");boolean fp=true;for(ChartEngine.Point p:m.points){if(!fp)out.append(',');fp=false;out.append('[').append(p.x).append(',').append(p.y).append(']');}out.append("]}");}
            out.append("],\"trace_points\":[");try{boolean firstPoint=true;for(Object v:(List<?>)result.getClass().getField("tracePoints").get(result)){ChartEngine.Point point=(ChartEngine.Point)v;if(!firstPoint)out.append(',');firstPoint=false;out.append('[').append(point.x).append(',').append(point.y).append(']');}}catch(ReflectiveOperationException ignored){}
            out.append("],\"problem\":").append(result.problem==null?"null":quote(result.problem)).append('}');System.out.println(out);
        }
    }
}
