/**
 * Author: Yegor Kozubenko, Tasnim Anowar
 * Purpose of this file is to Start the GUI application
 * that will connect with AtSign Secondary Server and 
 * show the current status of Door and the value of
 * Rotary Encoder
 */
package app.opensesame;

import javax.swing.SwingUtilities;

import org.atsign.client.api.AtClient;
import org.atsign.common.AtSign;
import org.atsign.common.KeyBuilders;
import org.atsign.common.Keys.SharedKey;

public class App {
    public static void main(String[] args) throws Exception {

        // Initialize AtSign's
        AtSign app = new AtSign("@batmanariesbanh");
        AtSign chip = new AtSign("@moralbearbanana");
        // Initialize AtClient
        AtClient atClient = AtClient.withRemoteSecondary("root.atsign.org:64", app);

        // Initialize AtKeys

        SharedKey appEvents = new KeyBuilders.SharedKeyBuilder(app, chip).key("app_e").build();

        SharedKey eventBus = new KeyBuilders.SharedKeyBuilder(chip,
                app).key("event_bus").build();

        SharedKey doorStatus = new KeyBuilders.SharedKeyBuilder(chip,
                app).key("door_status").build();

        SharedKey reValue = new KeyBuilders.SharedKeyBuilder(chip,
                app).key("re_value").build();

        // Start the UI Thread on AWT event-dispatching thread
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Ui ui = new Ui();
                ui.initialize(atClient, appEvents, eventBus, doorStatus, reValue);
            }
        });
    }
}