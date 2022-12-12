/*
 * 1998/2/7 (C) Copyright T.Kazawa (Digitune)
 */

package vavi.apps.ipm;

import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

import vavi.net.im.protocol.ipm.IpmPacket;
import vavi.net.im.protocol.ipm.Ipmessenger;
import vavi.net.im.protocol.ipm.event.CommunicationEvent;


/**
 * IP Messenger Config Dialog
 */
public class ConfigurationDialog extends JDialog {

    Ipmessenger ipmsg;

    /** 設定ファイル */
    private static Preferences userPrefs = Preferences.userNodeForPackage(Ipmessenger.class);

    /** */
    private static ResourceBundle rb = ResourceBundle.getBundle("vavi.net.im.protocol.ipm.resources");

    /** */
    private static ResourceBundle rrb = ResourceBundle.getBundle("runtime");

    public ConfigurationDialog(JFrame parent, Ipmessenger argipm) {
        super(parent, true);
        ipmsg = argipm;
        createWindow(parent);
    }

    private void createWindow(JFrame parent) {
        GridBagLayout gridBagLayout = new GridBagLayout();
        setLayout(gridBagLayout);

        JLabel label1 = new JLabel(rb.getString("setnickLabel"), JLabel.RIGHT);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(2, 2, 2, 2);
        gridBagLayout.setConstraints(label1, gbc);
        add(label1);

        JTextField textField1 = new JTextField();
        textField1.setText(userPrefs.get("nickName", rb.getString("nickName")));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 2, 2, 2);
        gridBagLayout.setConstraints(textField1, gbc);
        add(textField1);

        JLabel label2 = new JLabel(rb.getString("setgroupLabel"), JLabel.RIGHT);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(2, 2, 2, 2);
        gridBagLayout.setConstraints(label2, gbc);
        add(label2);

        JTextField textField2 = new JTextField(12);
        textField2.setText(userPrefs.get("groupName", rb.getString("groupName")));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 2, 2, 2);
        gridBagLayout.setConstraints(textField2, gbc);
        add(textField2);

        JComboBox<String> choice1 = new JComboBox<>();
        Map<String, String> groupcache = new HashMap<>();
        for (CommunicationEvent tmpevent : ipmsg.getUsers().values()) {
            IpmPacket tmppack = tmpevent.getPacket();
            if ((tmppack.getGroup() != null) && (groupcache.get(tmppack.getGroup()) == null)) {
                choice1.addItem(tmppack.getGroup());
                groupcache.put(tmppack.getGroup(), tmppack.getGroup());
            }
        }
        choice1.addItemListener(ie -> textField2.setText((String) choice1.getSelectedItem()));
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 2, 2, 2);
        gridBagLayout.setConstraints(choice1, gbc);
        add(choice1);

        JLabel label3 = new JLabel(rb.getString("setabsmsgLabel"), JLabel.RIGHT);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(2, 2, 2, 2);
        gridBagLayout.setConstraints(label3, gbc);
        add(label3);

        JTextField textField3 = new JTextField();
        textField3.setText(userPrefs.get("absenceMsg", rrb.getString("absenceMsg")));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 2, 2, 2);
        gridBagLayout.setConstraints(textField3, gbc);
        add(textField3);

        JLabel label4 = new JLabel(rb.getString("setbroadcastLabel"), JLabel.RIGHT);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(2, 2, 2, 2);
        gridBagLayout.setConstraints(label4, gbc);
        add(label4);

        JTextField textField4 = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 2, 2, 2);
        gridBagLayout.setConstraints(textField4, gbc);
        add(textField4);

        JList<String> list1 = new JList<>();
        list1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        StringTokenizer st1 = new StringTokenizer(userPrefs.get("broadcastAddr", rrb.getString("broadcastAddr")), ",");
        while (st1.hasMoreTokens()) {
            ((DefaultListModel<String>) list1.getModel()).addElement(st1.nextToken());
        }
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.gridheight = 3;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(2, 2, 2, 2);
        gridBagLayout.setConstraints(list1, gbc);
        add(list1);

        JButton button1 = new JButton();
        button1.setText(rb.getString("addbuttonLabel"));
        button1.addActionListener(ae -> {
            if (!textField4.getText().equals("")) {
                ListModel<String> listModel = list1.getModel();
                DefaultListModel<String> newListModel = new DefaultListModel<>();
                for (int i = 0; i < listModel.getSize(); i++) {
                    newListModel.addElement(listModel.getElementAt(i));
                }
                newListModel.addElement(textField4.getText());
                list1.setModel(newListModel);
                textField4.setText("");
            }
        });
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(2, 2, 2, 2);
        gridBagLayout.setConstraints(button1, gbc);
        add(button1);

        JButton button2 = new JButton();
        button2.setText(rb.getString("removebuttonLabel"));
        button2.addActionListener(ae -> {
            if (list1.getSelectedValue() != null) {
                textField4.setText(list1.getSelectedValue());
                list1.remove(list1.getSelectedIndex());
            }
        });
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(2, 2, 2, 2);
        gridBagLayout.setConstraints(button2, gbc);
        add(button2);

        JLabel label5 = new JLabel(rb.getString("setpasswdLabel"), JLabel.RIGHT);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(2, 2, 2, 2);
        gridBagLayout.setConstraints(label5, gbc);
        add(label5);

        JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayout(1, 0, 0, 0));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 2, 2, 2);
        gridBagLayout.setConstraints(panel1, gbc);
        add(panel1);

        JPasswordField textField5 = new JPasswordField();
        textField5.setEchoChar('*');
        panel1.add(textField5);

        JPasswordField textField6 = new JPasswordField();
        textField6.setEchoChar('*');
        panel1.add(textField6);

        JPasswordField textField7 = new JPasswordField();
        textField7.setEchoChar('*');
        panel1.add(textField7);

        JLabel label6 = new JLabel(rb.getString("setLogFilenameLabel"), JLabel.RIGHT);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(2, 2, 2, 2);
        gridBagLayout.setConstraints(label6, gbc);
        add(label6);

        JTextField textField8 = new JTextField();
        textField8.setText(userPrefs.get("logFilename", rrb.getString("logFilename")));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 7;
        gbc.gridwidth = 1;
        gbc.weightx = 2.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 2, 2, 2);
        gridBagLayout.setConstraints(textField8, gbc);
        add(textField8);

        JButton bbutton = new JButton();
        bbutton.setText(rb.getString("browseLabel"));
        bbutton.addActionListener(ae -> {
            FileDialog fd = new FileDialog(parent);
            fd.setVisible(true);
            if (fd.getFile() != null) {
                textField8.setText(fd.getDirectory() + fd.getFile());
            }
        });
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 7;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 2, 2, 2);
        gridBagLayout.setConstraints(bbutton, gbc);
        add(bbutton);

        JPanel panel2 = new JPanel();
        panel2.setLayout(new FlowLayout(FlowLayout.CENTER));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 2, 2, 2);
        gridBagLayout.setConstraints(panel2, gbc);
        add(panel2);

        JButton button3 = new JButton();
        button3.setText(rb.getString("okLabel"));
        button3.addActionListener(ae -> {
            if (!textField1.getText().equals("")) {
                userPrefs.put("nickName", textField1.getText());
            } else {
                userPrefs.put("nickName", "");
            }
            if (!textField2.getText().equals("")) {
                userPrefs.put("groupName", textField2.getText());
            } else {
                userPrefs.put("groupName", "");
            }
            if (!textField3.getText().equals("")) {
                userPrefs.put("absenceMsg", textField3.getText());
            } else {
                userPrefs.put("absenceMsg", "");
            }
            if (list1.getModel().getSize() != 0) {
                StringBuilder sb = new StringBuilder();
                sb.append(list1.getModel().getElementAt(0));
                for (int i = 1; i < list1.getModel().getSize(); i++) {
                    sb.append(",").append(list1.getModel().getElementAt(i));
                }
                userPrefs.put("broadcastAddr", new String(sb));
            } else {
                userPrefs.put("broadcastAddr", "");
            }
            try {
                String tmppass = userPrefs.get("password", rrb.getString("password"));
                if (textField5.getPassword() != null) {
                    if (MessageDigester.getMD5(new String(textField5.getPassword())).equals(tmppass)) {
                        if ((textField6.getPassword() != null) && (textField7.getPassword() != null)) {
                            if (new String(textField6.getPassword()).equals(new String(textField7.getPassword()))) {
                                userPrefs.put("password", MessageDigester.getMD5(new String(textField6.getPassword())));
                            }
                        }
                    }
                }
            } catch (MissingResourceException ex) {
                if ((textField6.getPassword() != null) && (textField7.getPassword() != null)) {
                    if (new String(textField6.getPassword()).equals(new String(textField7.getPassword()))) {
                        userPrefs.put("password", MessageDigester.getMD5(new String(textField6.getPassword())));
                    }
                }
            }
            if (!textField8.getText().equals("")) {
                userPrefs.put("logFilename", textField8.getText());
            } else {
                userPrefs.put("logFilename", "");
            }
            ipmsg.refreshList();
            dispose();
        });
        panel2.add(button3);

        JButton button4 = new JButton();
        button4.setText(rb.getString("cancelLabel"));
        button4.addActionListener(ae -> dispose());
        panel2.add(button4);
        pack();
        setTitle(rb.getString("confdlgName"));
        setResizable(false);

        Dimension sc = getToolkit().getScreenSize();
        Dimension sz = getSize();
        setLocation((sc.width / 2) - (sz.width / 2), (sc.height / 2) - (sz.height / 2));

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                dispose();
            }
        });
    }
}

/* */
