package aces.webctrl.cli.core;
import com.controlj.green.addonsupport.*;
import java.nio.file.*;
import java.security.SecureRandom;
import javax.servlet.*;
import java.util.*;
public class Initializer implements ServletContextListener {
  public final static SecureRandom RANDOM = new SecureRandom();
  /** Whether the operating system is Linux based */
  public final static boolean LINUX = System.getProperty("os.name").toLowerCase().contains("linux");
  /** Contains basic information about this addon */
  public volatile static AddOnInfo info = null;
  /** The name of this addon */
  private volatile static String name;
  /** Prefix used for constructing relative URL paths */
  private volatile static String prefix;
  /** Path to the private directory for this addon */
  private volatile static Path root;
  /** Path to WebCTRL's active system directory */
  public volatile static Path systemDir = null;
  /** Logger for this addon */
  private volatile static FileLogger logger;
  /** Becomes true when the servlet context is destroyed */
  public volatile static boolean stop = false;
  /** Stores references to Process threads */
  private final static HashMap<Long,ShellProcess> processes = new HashMap<>();
  /**
   * Entry point of this add-on.
   */
  @Override public void contextInitialized(ServletContextEvent sce){
    info = AddOnInfo.getAddOnInfo();
    name = info.getName();
    prefix = '/'+name+'/';
    root = info.getPrivateDir().toPath();
    logger = info.getDateStampLogger();
    systemDir = root.getParent().getParent().getParent();
  }
  /**
   * Kills the primary processing thread and releases all resources.
   */
  @Override public void contextDestroyed(ServletContextEvent sce){
    stop = true;
    boolean any = false;
    synchronized (processes){
      for (ShellProcess p : processes.values()){
        if (!p.isKilled()){
          p.getThread().interrupt();
          any|=true;
        }
      }
    }
    if (any){
      boolean cont = true;
      while (cont){
        try{ Thread.sleep(300L); }catch(Throwable t){}
        cont = false;
        for (ShellProcess p : processes.values()){
          if (!p.isKilled()){
            cont = true;
            break;
          }
        }
      }
    }
    try{ Thread.sleep(50L); }catch(Throwable t){}
  }
  public static void addProcess(ShellProcess t){
    if (stop){
      return;
    }
    synchronized (processes){
      if (stop){
        return;
      }
      ShellProcess p;
      Iterator<ShellProcess> i = processes.values().iterator();
      while (i.hasNext()){
        p = i.next();
        if (p.isKilled()){
          i.remove();
        }
      }
      processes.put(t.ID,t);
    }
  }
  public static ShellProcess getProcess(long id, String key){
    ShellProcess p;
    synchronized (processes){
      p = processes.get(id);
    }
    if (p==null || !p.KEY.equals(key)){
      return null;
    }
    return p;
  }
  /**
   * @return the name of this application.
   */
  public static String getName(){
    return name;
  }
  /**
   * @return the prefix used for constructing relative URL paths.
   */
  public static String getPrefix(){
    return prefix;
  }
  /**
   * Logs a message.
   */
  public synchronized static void log(String str){
    logger.println(str);
  }
  /**
   * Logs an error.
   */
  public synchronized static void log(Throwable t){
    logger.println(t);
  }
}