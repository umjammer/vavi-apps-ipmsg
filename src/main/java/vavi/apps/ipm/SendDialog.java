/*
 * 1998/01/30 (C) Copyright T.Kazawa (Digitune)
 */

package vavi.apps.ipm;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import vavi.net.im.protocol.ipm.Ipmessenger;
import vavi.net.im.protocol.ipm.Ipmessenger.Constant;
import vavi.net.im.protocol.ipm.event.CommunicationEvent;


/**
 * IP Messenger Send Dialog Class
 */
public class SendDialog extends JDialog {
    private Ipmessenger ipmsg;

    private CommunicationEvent[] users;

    private JTextArea body;

    private JCheckBox secret;

    private JCheckBox passwd;

    /** ê›íËÉtÉ@ÉCÉã */
    private static Preferences userPrefs = Preferences.userNodeForPackage(Ipmessenger.class);

    /** */
    private static ResourceBundle rb = ResourceBundle.getBundle("vavi.net.im.protocol.ipm.resources");

    public SendDialog(JFrame p, Ipmessenger i, CommunicationEvent[] e) {
        super(p, false);
        ipmsg = i;
        users = e;
        createWindow(p);
    }

    private void createWindow(final JFrame p) {
        setVisible(false);
        setTitle(rb.getString("senddlgName"));
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                exitAction();
            }
        });
        setLayout(new BorderLayout());

        JPanel p1 = new JPanel(new FlowLayout(FlowLayout.CENTER));
        add("North", p1);
        if (users == null) {
            JLabel to = new JLabel(rb.getString("toBroadcastLabel"));
            p1.add(to);
        } else if (users.length == 1) {
            JLabel to = new JLabel(rb.getString("toUnicastLabel"));
            p1.add(to);

            JLabel user = new JLabel(ipmsg.makeListString(users[0].getPacket()));
            p1.add(user);
        } else {
            JLabel to = new JLabel(rb.getString("toMulticastLabel"));
            p1.add(to);

            JComboBox choice = new JComboBox();
            for (int i = 0; i < users.length; i++) {
                choice.addItem(ipmsg.makeListString(users[i].getPacket()));
            }
            p1.add(choice);
        }
        body = new JTextArea();
        add("Center", body);

        JPanel p2 = new JPanel(new FlowLayout(FlowLayout.CENTER));
        add("South", p2);

        JButton send = new JButton(rb.getString("sendLabel"));
        send.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                try {
                    exitAction();

                    long flag = 0;
                    String suffix = "";
                    if (secret.isSelected()) {
                        flag |= Constant.SECRETOPT.getValue();;
                        suffix = "(" + userPrefs.get("secretLogFlag", rb.getString("secretLogFlag")) + ")";
                    }
                    if (passwd.isSelected()) {
                        flag |= Constant.PASSWORDOPT.getValue();
                        suffix = "(" + userPrefs.get("passwdLogFlag", rb.getString("passwdLogFlag")) + ")";
                    }

                    InetSocketAddress[] tmpaddrs = null;
                    String tostr;
                    if (users != null) {
                        tmpaddrs = new InetSocketAddress[users.length];
                        tmpaddrs[0] = users[0].getAddress();
                        tostr = "To: " + ipmsg.makeListString(users[0].getPacket());
                        for (int i = 1; i < users.length; i++) {
                            suffix = "(" + userPrefs.get("multicastLogFlag", rb.getString("multicastLogFlag")) + ") " + suffix;
                            tmpaddrs[i] = users[i].getAddress();
                            tostr = System.getProperty("line.separator", "\n") + "To: " + ipmsg.makeListString(users[i].getPacket());
                        }
                    } else {
                        tostr = "To: BROADCAST";
                        suffix = "(" + userPrefs.get("broadcastLogFlag", rb.getString("broadcastLogFlag")) + ") " + suffix;
                    }
                    ipmsg.sendMessage(tmpaddrs, body.getText(), flag);
                    ipmsg.writeLog(tostr, ipmsg.makeDateString(new Date(System.currentTimeMillis())) + " " + suffix, body.getText());
                } catch (IOException e) {
                    e.printStackTrace(); // TODO
                }
            }
        });
        p2.add(send);
        secret = new JCheckBox(rb.getString("secretLabel"));
        secret.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent ie) {
                if (secret.isSelected()) {
                    passwd.setEnabled(true);
                } else {
                    passwd.setSelected(false);
                    passwd.setEnabled(false);
                }
            }
        });
        secret.setSelected(userPrefs.getBoolean("secretState", Boolean.parseBoolean(rb.getString("secretState"))));
        p2.add(secret);
        passwd = new JCheckBox(rb.getString("passwdLabel"));
        passwd.setSelected(userPrefs.getBoolean("passwdState", Boolean.parseBoolean(rb.getString("passwdState"))));
        passwd.setEnabled(secret.isSelected());
        p2.add(passwd);

        JButton cancel = new JButton(rb.getString("cancelLabel"));
        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                exitAction();
            }
        });
        p2.add(cancel);
        try {
            int x = userPrefs.getInt("dlgSizeX", Integer.parseInt(rb.getString("dlgSizeX")));
            int y = userPrefs.getInt("dlgSizeY", Integer.parseInt(rb.getString("dlgSizeY")));
            setSize(x, y);
        } catch (MissingResourceException ex) {
            pack();
        }
        try {
            int x = userPrefs.getInt("senddlgX", Integer.parseInt(rb.getString("senddlgX")));
            int y = userPrefs.getInt("senddlgY", Integer.parseInt(rb.getString("senddlgY")));
            setLocation(x, y);
        } catch (MissingResourceException ex) {
            Dimension sc = getToolkit().getScreenSize();
            Dimension sz = getSize();
            setLocation((sc.width / 2) - sz.width, (sc.height / 2) - (sz.height / 2));
        }
    }

    private void exitAction() {
        if (userPrefs.getBoolean("resumeState", Boolean.parseBoolean("resumeState"))) {
            userPrefs.putBoolean("secretState", secret.isSelected());
            userPrefs.putBoolean("passwdState", passwd.isSelected());
        }

        Dimension size = getSize();
        userPrefs.putInt("dlgSizeX", size.width);
        userPrefs.putInt("dlgSizeY", size.height);

        Point location = getLocation();
        userPrefs.putInt("senddlgX", location.x);
        userPrefs.putInt("senddlgY", location.y);
        dispose();
    }

    public void setText(String text) {
        body.setText(text);
    }
}
