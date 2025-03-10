package aces.webctrl.cli.core;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
public class ShellProcess {
  private final static AtomicLong ID_GENERATOR = new AtomicLong(0);
  public final long ID = ID_GENERATOR.incrementAndGet();
  public final String KEY = getRandomKey();
  private volatile Process p = null;
  private volatile Thread t = null;
  private volatile BufferedWriter writer = null;
  private volatile BufferedReader reader = null;
  private volatile StringBuilder buffer = new StringBuilder(128);
  private volatile long timeout;
  private volatile long maxTimeout = System.currentTimeMillis()+86400000L;
  private final Result<Boolean> result = new Result<>();
  private volatile boolean killed = true;
  private volatile boolean stop = false;
  public ShellProcess() throws Throwable {
    final ProcessBuilder pb = new ProcessBuilder(Initializer.LINUX?new String[]{"/bin/bash", "-v"}:new String[]{"cmd.exe", "/k"});
    pb.directory(Initializer.systemDir.toFile());
    pb.redirectErrorStream(true);
    p = pb.start();
    writer = new BufferedWriter(new OutputStreamWriter(p.getOutputStream(), StandardCharsets.UTF_8));
    reader = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8));
    resetTimeout();
    t = new Thread(){
      @Override public void run(){
        try{
          try{
            int ch = 0;
            long l;
            do {
              if (Initializer.stop || stop){
                break;
              }
              if (p.isAlive()){
                while (reader.ready()){
                  ch = reader.read();
                  if (ch==-1){
                    break;
                  }
                  append((char)ch);
                }
                setResult();
                if (ch==-1){
                  break;
                }
                Thread.sleep(500L);
              }else{
                while (reader.ready()){
                  ch = reader.read();
                  if (ch==-1){
                    break;
                  }
                  append((char)ch);
                }
                setResult();
                break;
              }
              l = System.currentTimeMillis();
            } while (l<timeout && l<maxTimeout);
          }finally{
            if (p.isAlive()){
              try{
                p.destroy();
                if (!p.waitFor(2000L, TimeUnit.MILLISECONDS)){
                  p.destroyForcibly();
                  p.waitFor(2000L, TimeUnit.MILLISECONDS);
                }
              }catch(InterruptedException e){
                p.destroyForcibly();
              }
            }
          }
        }catch(Throwable e){}finally{
          try{ reader.close(); }catch(Throwable e){}
          try{ writer.close(); }catch(Throwable e){}
          result.setResult(false);
          killed = true;
        }
      }
    };
    killed = false;
    Initializer.addProcess(this);
    if (Initializer.stop){
      killed = true;
    }else{
      try{
        t.start();
      }catch(Throwable e){
        killed = true;
      }
    }
  }
  public void softKill(){
    stop = true;
  }
  public boolean isKilled(){
    return killed;
  }
  public Thread getThread(){
    return t;
  }
  private void setResult(){
    synchronized (buffer){
      if (buffer.length()>0){
        result.setResult(true);
      }
    }
  }
  public boolean waitForContent(long expiry) throws InterruptedException {
    return killed || result.waitForResult(expiry);
  }
  public String getContent(){
    try{
      synchronized (buffer){
        final String s = buffer.toString();
        buffer.setLength(0);
        result.reset();
        return s;
      }
    }finally{
      resetTimeout();
    }
  }
  public void writeLine(String s) throws Throwable {
    writer.write(s);
    writer.newLine();
    writer.flush();
    resetTimeout();
  }
  private void append(char ch){
    synchronized (buffer){
      buffer.append(ch);
    }
  }
  private void resetTimeout(){
    timeout = System.currentTimeMillis()+600000L;
  }
  private static String getRandomKey(){
    final byte[] b = new byte[32];
    Initializer.RANDOM.nextBytes(b);
    return Utility.bytesToHex(b,0,b.length);
  }
}