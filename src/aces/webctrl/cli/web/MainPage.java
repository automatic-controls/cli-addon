package aces.webctrl.cli.web;
import aces.webctrl.cli.core.*;
import javax.servlet.http.*;
// maybe use these for an API eventually
//url:java-jwt-4.5.0.jar:https://repo1.maven.org/maven2/com/auth0/java-jwt/4.5.0/java-jwt-4.5.0.jar
//url:java-jwt-4.5.0-sources.jar:https://repo1.maven.org/maven2/com/auth0/java-jwt/4.5.0/java-jwt-4.5.0-sources.jar
public class MainPage extends ServletBase {
  @Override public void exec(final HttpServletRequest req, final HttpServletResponse res) throws Throwable {
    final String type = req.getParameter("type");
    if (type==null){
      final ShellProcess p = new ShellProcess();
      res.setContentType("text/html");
      res.getWriter().print(getHTML(req)
        .replace("__ID__", String.valueOf(p.ID))
        .replace("__KEY__", p.KEY)
        .replace("__LINUX__", String.valueOf(Initializer.LINUX))
      );
    }else{
      final String id = req.getParameter("id");
      final String key = req.getParameter("key");
      if (id==null || key==null){
        res.sendError(400, "Missing parameter.");
        return;
      }
      final ShellProcess p = Initializer.getProcess(Long.parseLong(id),key);
      if (p==null || p.isKilled()){
        res.sendError(404, "Process not found.");
        return;
      }
      switch (type){
        case "kill":{
          p.softKill();
          break;
        }
        case "get":{
          try{
            if (p.waitForContent(System.currentTimeMillis()+50000L)){
              if (Initializer.stop){
                res.sendError(500, "Add-on is shutting down.");
                return;
              }
              res.setContentType("text/plain");
              res.getWriter().write(p.getContent());
            }else{
              res.sendError(504, "Timeout waiting for content.");
            }
          }catch(InterruptedException e){
            res.sendError(500, "Interrupted while waiting for content.");
          }
          break;
        }
        case "set":{
          final String content = req.getParameter("content");
          if (content==null){
            res.sendError(400, "Missing parameter.");
            return;
          }
          try{
            p.writeLine(content.replaceAll("\n",System.lineSeparator()));
          }catch(Throwable t){
            Initializer.log(t);
            res.sendError(500, "Failed to write content.");
          }
          break;
        }
        default:{
          res.sendError(400, "Unrecognized type parameter.");
        }
      }
    }
  }
}