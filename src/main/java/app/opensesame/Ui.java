/**
 * Author: Yegor Kozubenko, Tasnim Anowar
 * Purpose of this file is to extend the JFrame class
 * with and add the worker thread that will poll the
 * AtSign Secondary Server to fetch and push values.
 */
package app.opensesame;

import javax.swing.*;

import org.apache.commons.lang3.ObjectUtils.Null;
import org.atsign.client.api.AtClient;
import org.atsign.common.Keys.SharedKey;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class Ui extends JFrame {
    private JPanel contentPane;
    private JLabel lblDoorImage;
    private JLabel lblDoorStatus;
    private JButton actionButton;
    private JSlider compReSlider;
    private JLabel lblReStatus;

    // Door States
    private final String[] doorStatusToString = new String[] { "opened", "closed", "opening", "closing" };
    // Images
    private ImageIcon[] doorStatusImages = new ImageIcon[4];

    // This function will create the JPanels with the required UI components and set
    // the
    // GUI visible and is responisble for starting a new worker thread that will
    // poll the
    // AtSign Server to get the latest changes and update the UI.
    public void initialize(AtClient atClient, SharedKey appEventsKey, SharedKey eventBusKey, SharedKey doorStatusKey,
            SharedKey reValueKey) {
        // Load Images
        for (int i = 0; i < 4; i++) {
            doorStatusImages[i] = new ImageIcon(getClass().getResource("/" + doorStatusToString[i] + ".png"));
        }

        Font f = new Font("serif", Font.PLAIN, 24);
        contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));

        // Top Panel
        JPanel topPanel = new JPanel();
        JLabel lblDoorStatusPreMessage = new JLabel("Door Status: ");
        lblDoorStatusPreMessage.setFont(f);
        lblDoorStatus = new JLabel("SYNCING");
        lblDoorStatus.setFont(f);
        topPanel.add(lblDoorStatusPreMessage);
        topPanel.add(lblDoorStatus);

        contentPane.add(topPanel);

        // Middle Panel
        lblDoorImage = new JLabel(doorStatusImages[2]);
        lblDoorImage.setSize(300, 300);

        JPanel midPanel = new JPanel();
        midPanel.add(lblDoorImage);

        contentPane.add(midPanel);

        JPanel botPanel = new JPanel();
        botPanel.setLayout(new BoxLayout(botPanel, BoxLayout.Y_AXIS));
        botPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        actionButton = new JButton("SYNCING");
        actionButton.setFont(f);
        actionButton.setAlignmentX(CENTER_ALIGNMENT);

        // Action Listener that will read the token from AtSign with eventBusKey and
        // and the said action based on the State of the door to AtSign appEventsKey
        actionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                String doorStatus = actionButton.getText();

                System.out.println(doorStatus);

                String event_id = "";

                switch (doorStatus) {
                    case "Open":
                        event_id = "2";
                        break;
                    case "Close":
                        event_id = "4";
                        break;
                    case "Halt":
                        event_id = "6";
                        break;
                }

                try {
                    String tkn = atClient.get(eventBusKey).get();
                    atClient.put(appEventsKey, event_id + "z" + tkn).get();
                } catch (Exception err) {
                    System.out.println(err.toString());
                }
            }
        });

        botPanel.add(actionButton);
        botPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        compReSlider = new JSlider(0, 5, 5);
        compReSlider.setMajorTickSpacing(1);
        compReSlider.setMinorTickSpacing(1);
        compReSlider.setPaintTicks(true);
        compReSlider.setPaintLabels(true);
        botPanel.add(compReSlider);
        botPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel reVal = new JPanel();
        JLabel reStatusPre = new JLabel("RE Value: ");
        reVal.add(reStatusPre);
        reStatusPre.setFont(f);
        reStatusPre.setAlignmentX(CENTER_ALIGNMENT);
        lblReStatus = new JLabel("5");
        lblReStatus.setFont(f);
        lblReStatus.setAlignmentX(CENTER_ALIGNMENT);
        reVal.add(lblReStatus);
        botPanel.add(reVal);
        botPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        botPanel.add(Box.createRigidArea(new Dimension(0, 60)));

        contentPane.add(botPanel);

        add(contentPane);

        setTitle("Garage Door Controller");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(380, 760);
        setMinimumSize(new Dimension(360, 580));
        setVisible(true);

        // The Worker Thread responisble to polling the AtSign Server.

        SwingWorker<Null, String[]> worker = new SwingWorker<Null, String[]>() {

            // Code to execute in the Worker Thread
            @Override
            protected Null doInBackground() throws Exception {
                while (true) {
                    Thread.sleep(2000);
                    String DoorStatus = atClient.get(doorStatusKey).get();
                    String reValue = atClient.get(reValueKey).get();

                    String[] ret = { DoorStatus, reValue };

                    publish(ret);
                }
            }

            // Process the results from the worker thread
            @Override
            protected void process(List<String[]> chunks) {
                String[] data = chunks.get(chunks.size() - 1);

                System.out.println("Chunks: " + chunks.size());

                try {
                    String doorStatus = doorStatusToString[Integer.parseInt(data[0])];
                    lblDoorStatus.setText(doorStatus);
                    lblDoorImage.setIcon(doorStatusImages[Integer.parseInt(data[0])]);
                    lblDoorImage.revalidate();
                    lblDoorImage.repaint();

                    switch (doorStatus) {
                        case "opened":
                            actionButton.setText("Close");
                            break;
                        case "closed":
                            actionButton.setText("Open");
                            break;
                        case "opening":
                        case "closing":
                            actionButton.setText("Halt");
                            break;
                    }

                } catch (Exception e) {
                    lblDoorStatus.setText(doorStatusToString[1]);
                    actionButton.setText("Open");
                }

                try {
                    compReSlider.setValue(Integer.parseInt(data[1]));
                    lblReStatus.setText(data[1]);
                } catch (Exception e) {
                    compReSlider.setValue(0);
                }
            }
        };

        worker.execute();
    }
}
