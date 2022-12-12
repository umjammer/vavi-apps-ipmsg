/*
 * 1998/01/27 (C) Copyright T.Kazawa (Digitune)
 */

package vavi.apps.ipm;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.prefs.Preferences;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import vavi.net.im.protocol.ipm.Ipmessenger;
import vavi.net.im.protocol.ipm.IpmPacket;
import vavi.net.im.protocol.ipm.Ipmessenger.Constant;
import vavi.net.im.protocol.ipm.event.CommunicationEvent;
import vavi.net.im.protocol.ipm.event.IpmEvent;
import vavi.net.im.protocol.ipm.event.IpmListener;


/**
 * IP Messenger Main Window
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 2005/08/02 nsano initial version <br>
 */
public class MainWindow extends JFrame {

    public Ipmessenger ipmsg;

    private JButton send;
    private JButton refresh;
    private JButton conf;
    private JButton exit;

    private JList<String> memberlist;
    private JCheckBox absence;
    private JCheckBox broadcast;
    private JComboBox<String> groups;

    private Map<String, CommunicationEvent> NAMEtoINFO = new HashMap<>();
    private Map<String, CommunicationEvent> ADDRtoINFO = new HashMap<>();

    private boolean refreshing = false;
    private boolean received = false;

    /** 設定ファイル */
    private static Preferences userPrefs = Preferences.userNodeForPackage(Ipmessenger.class);

    /** */
    private static ResourceBundle rb = ResourceBundle.getBundle("vavi.net.im.protocol.ipm.resources");

    /** */
    private static ResourceBundle rrb = ResourceBundle.getBundle("runtime");

    /** */
    public MainWindow() {
        ipmsg = new Ipmessenger();
        ipmsg.addIPMListener(ipmListener);
        createWindow();
        ipmsg.entry();
    }

    /** */
    private void createWindow() {
        setVisible(false);
        setTitle(rb.getString("appName"));
        setLayout(new BorderLayout());
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                exitAction();
            }

            public void windowClosed(WindowEvent we) {
                System.exit(0);
            }
        });

        JPanel p1 = new JPanel(new FlowLayout(FlowLayout.CENTER));
        add("North", p1);
        send = new JButton();
        send.setText(rb.getString("sendLabel"));
        send.addActionListener(ae -> sendAction());
        p1.add(send);
        refresh = new JButton();
        refresh.setText(rb.getString("refreshLabel"));
        refresh.addActionListener(ae -> refreshAction());
        p1.add(refresh);
        conf = new JButton();
        conf.setText(rb.getString("configLabel"));
        conf.addActionListener(ae -> confAction());
        p1.add(conf);
        exit = new JButton();
        exit.setText(rb.getString("exitLabel"));
        exit.addActionListener(ae -> exitAction());
        p1.add(exit);

        JPanel p5 = new JPanel(new BorderLayout());
        add("Center", p5);

        JPanel p6 = new JPanel(new GridLayout(1, 0, 0, 0));
        p5.add("North", p6);

        JButton sortuser = new JButton(rb.getString("sortUserLabel"));
        sortuser.addActionListener(ae -> sortKeyChanged("u"));
        p6.add(sortuser);

        JButton sortgroup = new JButton(rb.getString("sortGroupLabel"));
        sortgroup.addActionListener(ae -> sortKeyChanged("g"));
        p6.add(sortgroup);

        JButton sorthost = new JButton(rb.getString("sortHostLabel"));
        sorthost.addActionListener(ae -> sortKeyChanged("h"));
        p6.add(sorthost);
        memberlist = new JList<>();
        memberlist.addListSelectionListener(event -> sendAction());
        p5.add("Center", memberlist);

        JPanel p2 = new JPanel(new BorderLayout(5, 5));
        add("South", p2);

        JPanel p3 = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
        p2.add("West", p3);
        broadcast = new JCheckBox(rb.getString("broadcastLabel"));
        broadcast.addItemListener(ie -> memberlist.setEnabled(!broadcast.isSelected()));
        p3.add(broadcast);
        groups = new JComboBox<>();
        groups.addItem(rb.getString("allName"));
        groups.addItemListener(ie -> {
            received = true;
            if (!refreshing) {
                refreshing = true;
                new RefreshList().start();
            }
        });
        p2.add("Center", groups);

        JPanel p4 = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
        p2.add("East", p4);
        absence = new JCheckBox(rb.getString("absenceLabel"));
        absence.setSelected(Boolean.valueOf(rb.getString("absenceState")));
        absence.addItemListener(ie -> {
            userPrefs.put("absenceState", new Boolean(absence.isSelected()).toString());
            ipmsg.absenceStateChanged();
        });
        p4.add(absence);
        int w = userPrefs.getInt("mainSizeX", Integer.parseInt(rrb.getString("mainSizeX")));
        int h = userPrefs.getInt("mainSizeY", Integer.parseInt(rrb.getString("mainSizeY")));
        setSize(w, h);
        int x = userPrefs.getInt("mainX", Integer.parseInt(rrb.getString("mainX")));
        int y = userPrefs.getInt("mainY", Integer.parseInt(rrb.getString("mainY")));
        setLocation(x, y);
        setIconImage(getToolkit().getImage(getClass().getResource("/images/ipmsg.gif")));
    }

    /** */
    @SuppressWarnings("unused")
    private Comparator<CommunicationEvent> comparator = new Comparator<CommunicationEvent>() {
        /** */
        private String getKey(CommunicationEvent ipmce) {
            String tmpkey = rb.getString("sortKey");
            StringBuilder strbuf = new StringBuilder();
            for (int i = 0; i < tmpkey.length(); i++) {
                switch (tmpkey.charAt(i)) {
                case 'u':
                    String tmpuser;
                    if (ipmce.getPacket().getExtra() == null) {
                        tmpuser = ipmce.getPacket().getUser();
                    } else {
                        tmpuser = ipmce.getPacket().getExtra();
                    }
                    strbuf.append(tmpuser);
                    break;
                case 'g':
                    if (ipmce.getPacket().getGroup() != null) {
                        strbuf.append(ipmce.getPacket().getGroup());
                    } else {
                        strbuf.append("  ");
                    }
                    break;
                case 'h':
                    strbuf.append(ipmce.getPacket().getHost());
                    break;
                }
            }
            return new String(strbuf);
        }

        /** */
        public int compare(CommunicationEvent ipmce1, CommunicationEvent ipmce2) {
            return getKey(ipmce1).compareTo(getKey(ipmce2));
        }
    };

    private synchronized void sortKeyChanged(String ckey) {
        String tmpkey = userPrefs.get("sortKey", rb.getString("sortKey"));
        tmpkey = tmpkey.replace(ckey, "");
        userPrefs.put("sortKey", ckey + tmpkey);
        received = true;
        if (!refreshing) {
            refreshing = true;
            new RefreshList().start();
        }
    }

    private void sendAction() {
        CommunicationEvent[] members;
        if (broadcast.isSelected() && groups.getSelectedItem().equals(rb.getString("allName"))) {
            members = null;
        } else if (broadcast.isSelected()) {
            Object[] strmembers = memberlist.getSelectedValues();
            members = new CommunicationEvent[strmembers.length];
            for (int i = 0; i < members.length; i++) {
                members[i] = NAMEtoINFO.get(strmembers[i]);
            }
        } else {
            Object[] strmembers = memberlist.getSelectedValues();
            if (strmembers.length == 0) {
                return;
            }
            members = new CommunicationEvent[strmembers.length];
            for (int i = 0; i < members.length; i++) {
                members[i] = NAMEtoINFO.get(strmembers[i]);
            }
        }

        SendDialog sd = new SendDialog(this, ipmsg, members);
        sd.setVisible(true);
    }

    private void refreshAction() {
        ipmsg.refreshList();
    }

    private void confAction() {
        ConfigurationDialog cd = new ConfigurationDialog(this, ipmsg);
        cd.setVisible(true);
    }

    private void exitAction() {
        Dimension size = getSize();
        userPrefs.put("mainSizeX", Integer.toString(size.width));
        userPrefs.put("mainSizeY", Integer.toString(size.height));

        Point location = getLocation();
        userPrefs.put("mainX", Integer.toString(location.x));
        userPrefs.put("mainY", Integer.toString(location.y));
        ipmsg.exit();
        dispose();
    }

    private class RefreshList extends Thread {
        public void run() {
            String tmpgroup = (String) groups.getSelectedItem();
            while (received) {
                received = false;
                try {
                    sleep(500);
                } catch (InterruptedException ex) {
                }
                NAMEtoINFO = new HashMap<>();
                ADDRtoINFO = new HashMap<>();

                Map<String, String> groupcache = new HashMap<>();
                memberlist.removeAll();
                groups.removeAll();
                groups.addItem(rb.getString("allName"));

                Iterator<CommunicationEvent> members = ipmsg.getUsers().values().iterator();
                SortedSet<CommunicationEvent> tmpvec = new TreeSet<>();
                while (members.hasNext()) {
                    CommunicationEvent tmpevent = members.next();
                    tmpvec.add(tmpevent);
                }
                for (CommunicationEvent tmpevent : tmpvec) {
                    IpmPacket tmppack = tmpevent.getPacket();
                    if ((tmppack.getGroup() != null) && (groupcache.get(tmppack.getGroup()) == null)) {
                        groups.addItem(tmppack.getGroup());
                        groupcache.put(tmppack.getGroup(), tmppack.getGroup());
                    }
                    if (!tmpgroup.equals(rb.getString("allName"))) {
                        if (tmppack.getGroup() == null) {
                            continue;
                        } else if (!tmppack.getGroup().equals(tmpgroup)) {
                            continue;
                        }
                    }

                    String tmpstr = ipmsg.makeListString(tmppack);
                    ListModel<String> listModel = memberlist.getModel();
                    DefaultListModel<String> newListModel = new DefaultListModel<>();
                    for (int i = 0; i < listModel.getSize(); i++) {
                        newListModel.addElement(listModel.getElementAt(i));
                    }
                    newListModel.addElement(tmpstr);
                    memberlist.setModel(newListModel);
                    NAMEtoINFO.put(tmpstr, tmpevent);
                    ADDRtoINFO.put(tmpevent.getAddress().toString(), tmpevent);
                }
                if (memberlist.getModel().getSize() == 0) {
                    tmpgroup = rb.getString("allName");
                    refreshing = true;
                } else {
                    groups.setSelectedItem(tmpgroup);
                }
            }
            refreshing = false;
        }
    }

    private IpmListener ipmListener = new IpmListener() {
        public synchronized void eventOccured(IpmEvent ipme) {
            switch (ipme.getID()) {
            case UPDATE_LIST:
                received = true;
                if (!refreshing) {
                    refreshing = true;
                    new RefreshList().start();
                }
                break;
            case RECEIVE_MESSAGE:
                if (!ipmsg.lessThanReceiveMax()) {
                    System.err.println("too many receive dialog.");
                    break;
                }
                ipmsg.increaseReceiveCount();
                getToolkit().beep();
    
                ReceiveDialog rd = new ReceiveDialog(MainWindow.this, ipmsg, ADDRtoINFO.get(ipme.getAddress().toString()), ipme);
                rd.setVisible(true);
                break;
            case READ_MESSAGE:
                getToolkit().beep();
    
                String tmpname = "";
                CommunicationEvent tmpipmce = ADDRtoINFO.get(ipme.getAddress().toString());
                IpmPacket tmppack;
                if (tmpipmce != null) {
                    tmppack = tmpipmce.getPacket();
                    tmpname = ipmsg.makeListString(tmppack);
                } else {
                    tmpname = ipme.getPacket().getUser();
                }
    
                StringBuilder strbuf = new StringBuilder();
                strbuf.append(rb.getString("readMsg")).append("\n");
                strbuf.append(ipmsg.makeDateString(ipme.getDate()));
    
                JOptionPane.showMessageDialog(MainWindow.this, tmpname, new String(strbuf), JOptionPane.ERROR_MESSAGE);
                break;
            case DELETE_MESSAGE:
                getToolkit().beep();
                tmpname = "";
                tmpipmce = ADDRtoINFO.get(ipme.getAddress().toString());
                if (tmpipmce != null) {
                    tmppack = tmpipmce.getPacket();
                    tmpname = ipmsg.makeListString(tmppack);
                } else {
                    tmpname = ipme.getPacket().getUser();
                }
                strbuf = new StringBuilder();
                strbuf.append(rb.getString("deleteMsg")).append("\n");
                strbuf.append(ipmsg.makeDateString(ipme.getDate()));
                JOptionPane.showMessageDialog(MainWindow.this, strbuf, tmpname, JOptionPane.INFORMATION_MESSAGE);
                break;
            case CANNOT_SEND_MESSAGE:
                int r = JOptionPane.showConfirmDialog(MainWindow.this, rb.getString("retryMsg"), rb.getString("appName"), JOptionPane.YES_NO_OPTION);
                if (r == JOptionPane.YES_OPTION) {
                    try {
                        long flag = ipme.getPacket().getCommand() & Constant.OPTMASK;
                        InetSocketAddress[] tmpaddr = new InetSocketAddress[1];
                        tmpaddr[0] = ipme.getAddress();
                        ipmsg.sendMessage(tmpaddr, ipme.getPacket().getExtra(), flag);
                    } catch (IOException e) {
                        e.printStackTrace(); // TODO
                    }
                }
                break;
            }
        }
    };

    /** */
    public static void main(String[] args) {
        MainWindow mainwin = new MainWindow();
        mainwin.setVisible(true);
    }
}

/* */
