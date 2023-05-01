package app.opensesame;

import javax.swing.SwingUtilities;

import org.atsign.client.api.AtClient;
import org.atsign.common.AtSign;
import org.atsign.common.KeyBuilders;
import org.atsign.common.Keys.SharedKey;

public class App {
    public static void main(String[] args) throws Exception {

        AtSign app = new AtSign("@batmanariesbanh");
        AtSign chip = new AtSign("@moralbearbanana");
        AtClient atClient = AtClient.withRemoteSecondary("root.atsign.org:64", app);

        SharedKey appEvents = new KeyBuilders.SharedKeyBuilder(app, chip).key("app_e").build();

        SharedKey eventBus = new KeyBuilders.SharedKeyBuilder(chip,
                app).key("event_bus").build();

        SharedKey doorStatus = new KeyBuilders.SharedKeyBuilder(chip,
                app).key("door_status").build();

        SharedKey reValue = new KeyBuilders.SharedKeyBuilder(chip,
                app).key("re_value").build();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Ui ui = new Ui();
                ui.initialize(atClient, appEvents, eventBus, doorStatus, reValue);
            }
        });
    }
}