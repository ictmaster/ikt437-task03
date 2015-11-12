import org.apache.jena.query.ResultSet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ResultWindow extends JFrame {
    public ResultWindow(String title, String output){
        JTextArea results = new JTextArea(output);
        JScrollPane jsp = new JScrollPane(results);
        this.setLayout(new BorderLayout());
        this.setTitle(title);
        this.setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
            }
        });


        this.add(jsp, BorderLayout.CENTER);
        this.add(closeBtn, BorderLayout.SOUTH);


    }
}
