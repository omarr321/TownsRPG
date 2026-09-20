import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class graphicsManager{
    private static String fullscreenOption = "Yes";
    private static String resOption = "3840 X 2160";
    public static void main(String[] args) {
        JFrame frame = new JFrame("Graphics");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500,300);
        frame.setResizable(false);

        Font labelFont = new Font(Font.SANS_SERIF,Font.BOLD,16);
        Font dropdownFont = new Font(Font.SANS_SERIF,Font.PLAIN,16);
        Font titleFont = new Font(Font.SANS_SERIF,Font.BOLD,24);

        JLabel titleLabel = new JLabel("Graphical Options");
        titleLabel.setFont(titleFont);
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.add(titleLabel);

        String[] FullscreenChoices = {"Yes","No"};
        String[] GraphicsChoices = {"3840 X 2160", "2560 X 1440","1920 X 1080", "1280 X 720"};

        JComboBox<String> FullscreenBox = new JComboBox<>(FullscreenChoices);
        JComboBox<String> GraphicsBox = new JComboBox<>(GraphicsChoices);
        FullscreenBox.setFont(dropdownFont);
        GraphicsBox.setFont(dropdownFont);
        GraphicsBox.setEnabled(false);

        JLabel FullscreenLabel = new JLabel("Do you want to run in fullscreen mode?");
        JLabel GraphicsLabel = new JLabel("Window Resolution?");
        FullscreenLabel.setFont(labelFont);
        GraphicsLabel.setFont(labelFont);

        FullscreenBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fullscreenOption = (String) FullscreenBox.getSelectedItem();
                if (fullscreenOption.equals("Yes")){
                    GraphicsBox.setEnabled(false);
                } else if (fullscreenOption.equals("No")){
                    GraphicsBox.setEnabled(true);
                }
            }
        });

        GraphicsBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resOption = (String) GraphicsBox.getSelectedItem();
            }
        });

        JPanel rowFullscreenPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        rowFullscreenPanel.add(FullscreenLabel);
        rowFullscreenPanel.add(FullscreenBox);
        rowFullscreenPanel.setBorder(new EmptyBorder(5,5,5,5));

        JPanel rowGraphicsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        rowGraphicsPanel.add(GraphicsLabel);
        rowGraphicsPanel.add(GraphicsBox);
        rowGraphicsPanel.setBorder(new EmptyBorder(5,5,5,5));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        mainPanel.add(titlePanel);
        mainPanel.add(rowFullscreenPanel);
        mainPanel.add(rowGraphicsPanel);

        JButton confirmButton = new JButton("Confirm");
        confirmButton.setFont(labelFont);
        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        JPanel rowButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        rowButtonPanel.add(confirmButton);
        mainPanel.add(rowButtonPanel);

        frame.add(mainPanel);
        frame.setVisible(true);
    }
}