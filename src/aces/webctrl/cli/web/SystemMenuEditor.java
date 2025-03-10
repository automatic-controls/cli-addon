package aces.webctrl.cli.web;
import aces.webctrl.cli.core.*;
import com.controlj.green.addonsupport.access.*;
import com.controlj.green.addonsupport.web.menus.*;
public class SystemMenuEditor implements SystemMenuProvider {
  @Override public void updateMenu(Operator op, Menu menu){
    try{
      if (op.getPrivilegeSet(null).contains("administrator")){
        menu.addMenuEntry(MenuEntryFactory
          .newEntry("aces.webctrl.cli.WebTerminal")
          .display("Web Terminal")
          .action(Actions.openWindow(Initializer.getPrefix()+"index"))
          .create()
        );
      }
    }catch(Throwable t){
      Initializer.log(t);
    }
  }
}