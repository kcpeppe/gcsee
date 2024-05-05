package com.kodewerk.gcsee;

import javax.swing.*;
import java.awt.*;

public class TableDataJPanel extends JPanel {

    public TableDataJPanel(TableDataAggregation aggregation) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));


        String[] header = {"",""};
        String[][] body = aggregation.summary();
        JTable table = new JTable(body,header) {
            public boolean isCellEditable(int row, int column) {
                return false;
            };
        };
        add(Box.createRigidArea(new Dimension(5,20)));
        add(new JLabel("Summary"));
        add(Box.createRigidArea(new Dimension(5,5)));
        add(table);

        header = new String[]{"GC Cycle", "Occurrences", "Concurrent Duration", "Pause Time"};
        body = aggregation.events();

        table = new JTable(body,header) {
            public boolean isCellEditable(int row, int column) {
                return false;
            };
        };

        add(Box.createRigidArea(new Dimension(5,30)));
        add(new JLabel("GC Events Summary"));
        add(Box.createRigidArea(new Dimension(5,5)));
        add(table.getTableHeader());
        add(table);
    }
}
