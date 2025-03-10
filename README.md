# CLI Web Terminal

WebCTRL is a trademark of Automated Logic Corporation. Any other trademarks mentioned herein are the property of their respective owners.

This WebCTRL add-on permits operators with administrative privileges access to a web interface for the server's shell. If the WebCTRL server is running on Linux, then *Bash* is shown. If the WebCTRL server is running on Windows, then *Command Prompt* is shown. Note that you can access *PowerShell* from within *Command Prompt* as well.

## Usage

If your server requires signed add-ons, copy the authenticating certificate [*ACES.cer*](https://github.com/automatic-controls/addon-dev-script/blob/main/ACES.cer?raw=true) to the *./programdata/addons* directory of your *WebCTRL* installation folder. Install [*CLI_Web_Terminal.addon*](https://github.com/automatic-controls/cli-addon/releases/latest/download/CLI_Web_Terminal.addon) using the *WebCTRL* interface. Navigate to this add-on's main page (e.g, *localhost/CLI_Web_Terminal*) to access the server's shell. You may also click the **Web Terminal** option listed in the system menu in the top-right corner of WebCTRL. After installing the add-on, you must logout and login to see the system menu option.