package UDP;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Window extends JFrame {
    public String path = null;
    public String side = null;
    public Boolean initialized = false;

    public Window() {
        this.setTitle("User Datagram Protocol");
        getInfoGUI(this);
    }

    private void getInfoGUI(JFrame frame) {
        JPanel mainPanel = new JPanel();
        JPanel jbuttonPanel = new JPanel();
        JPanel startButtonPanel = new JPanel();
        JPanel filePathPanel = new JPanel();
        JPanel filePanel = new JPanel();
        JLabel fileLabel = new JLabel("Write the path of the file you want to send: ");
        JTextField filePath = new JTextField(15);
        JButton startButton = new JButton("Start");
        JRadioButton b1 = new JRadioButton("Sender");
        JRadioButton b2 = new JRadioButton("Server");
        ButtonGroup group1 = new ButtonGroup();
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setPreferredSize(new Dimension(400, 250));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jbuttonPanel.setLayout(new BoxLayout(jbuttonPanel, BoxLayout.X_AXIS));
        jbuttonPanel.setAlignmentX(JPanel.CENTER_ALIGNMENT);
        startButtonPanel.setLayout(new BoxLayout(startButtonPanel, BoxLayout.X_AXIS));
        startButtonPanel.setAlignmentX(JPanel.CENTER_ALIGNMENT);
        filePathPanel.setLayout(new BoxLayout(filePathPanel, BoxLayout.Y_AXIS));
        b1.setSelected(true);
        b1.setActionCommand("Sender");
        b1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                filePanel.setVisible(true);
            }
        });

        b2.setActionCommand("Server");
        b2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                filePanel.setVisible(false);
            }
        });

        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(!filePath.getText().isEmpty() && b1.isSelected()) {
                    side = b1.getActionCommand();
                    path = filePath.getText();
                    initialized = true;

                } else if(b2.isSelected()){
                    side = b2.getActionCommand();
                    initialized = true;
                }
            }
        });

        group1.add(b1);
        group1.add(b2);
        jbuttonPanel.add(Box.createHorizontalGlue());
        jbuttonPanel.add(b1);
        jbuttonPanel.add(b2);
        jbuttonPanel.add(Box.createHorizontalGlue());
        startButtonPanel.add(Box.createHorizontalGlue());
        startButtonPanel.add(startButton);
        startButtonPanel.add(Box.createHorizontalGlue());
        filePathPanel.add(fileLabel);
        filePathPanel.add(filePath);
        filePanel.add(filePathPanel);
        mainPanel.add(Box.createVerticalGlue());
        mainPanel.add(jbuttonPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(1, 40)));
        mainPanel.add(filePanel);
        mainPanel.add(Box.createVerticalGlue());
        mainPanel.add(startButtonPanel);
        mainPanel.add(Box.createVerticalGlue());
        frame.getContentPane().add(mainPanel);

        frame.setLocation(dim.width / 2 - frame.getSize().width / 2, dim.height / 2 - frame.getSize().height / 2);
        frame.pack();
        frame.setVisible(true);
    }
}
