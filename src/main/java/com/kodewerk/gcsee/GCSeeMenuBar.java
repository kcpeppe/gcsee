package com.kodewerk.gcsee;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;

public class GCSeeMenuBar extends JMenuBar {

    private static final int MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
    private GCSee parent;

    private JMenu gcsee;
    private JMenuItem exitAction;

    private JMenu fileMenu;
    private JMenuItem openAction;

    private String lastDirectory;

    public GCSeeMenuBar(GCSee outerContext) {

        lastDirectory = System.getProperty("user.dir");
        parent = outerContext;

        gcsee = new JMenu("GCSee");
        super.add(gcsee);
        exitAction = new JMenuItem("Exit");
        exitAction.addActionListener(e -> parent.exit(0));
        gcsee.add(exitAction);

        fileMenu = new JMenu("File");
        super.add(fileMenu);
        openAction = new JMenuItem("Open");
        openAction.addActionListener(loadLogAction());
        fileMenu.add(openAction);

    }

//    private static JMenuItem menuItem(String title, char hotKeyBinding, int accelerator) {
//        JMenuItem item = new JMenuItem(title);
//        item.setMnemonic(hotKeyBinding);
//        if (accelerator != -1)
//            item.setAccelerator(KeyStroke.getKeyStroke(accelerator,MASK));
//        return item;
//    }

    public ActionListener loadLogAction() {
        return event -> {
            FileDialog dialog = new FileDialog(parent);
            dialog.setTitle("Open Log File");
            dialog.setDirectory(lastDirectory);
            dialog.setVisible(true);
            lastDirectory = dialog.getDirectory();
            File[] target = dialog.getFiles();
            if (target.length == 1)
                load(target[0]);
        };
    }

    private void load(File selectedFile) {
        try {
            parent.load(selectedFile);
        } catch (Throwable e) {
            JOptionPane.showMessageDialog(parent, e.getMessage(), "GCSee Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
