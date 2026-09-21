package GraphicClasses;

import GraphicClasses.CustomPanels.QuadShapeDrawer;
import GraphicClasses.CustomPanels.QuadrilateralPanel;
import GraphicClasses.CustomPanels.RectPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This class handles all the drawing to the screen and user input.
 */
public class GraphicsManager{

    /**
     * This window asks for user graphic setting then moves to the game with the setting set.
     */
    public static class GraphicWindow extends JFrame {
        private static String fullscreenOption = "Yes";
        private static String resOption = "3840 X 2160";

        public GraphicWindow(){
            JFrame frame = new JFrame("Graphics");

            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(550,300);
            frame.setResizable(false);

            FontWrapper Tuffy_Bold = new FontWrapper("/fonts/Tuffy_Bold.ttf", "Tuffy_Bold");

            Font labelFont = Tuffy_Bold.getFont(Font.PLAIN, 24);
            Font dropdownFont = Tuffy_Bold.getFont(Font.PLAIN, 18);
            Font titleFont = Tuffy_Bold.getFont(Font.BOLD, 40);

            JLabel titleLabel = new JLabel("Graphical Options");
            titleLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 5));
            titleLabel.setFont(titleFont);
            titleLabel.setForeground(Color.BLACK);
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
                    frame.dispose();

                    if ((e.getModifiers() & ActionEvent.CTRL_MASK) != 0) {
                        new DebugWindow(1280,720,false);
                    } else {
                        if(fullscreenOption.equals("Yes")){
                            new GameWindow(0,0,true);
                        } else {
                            //"3840 X 2160", "2560 X 1440","1920 X 1080", "1280 X 720"
                            switch (resOption) {
                                case "3840 X 2160":
                                    new GameWindow(3840,2160,false);
                                    break;
                                case "2560 X 1440":
                                    new GameWindow(2560,1440,false);
                                    break;
                                case "1920 X 1080":
                                    new GameWindow(1920,1080,false);
                                    break;
                                case "1280 X 720":
                                    new GameWindow(1280,720,false);
                                    break;
                                default:
                                    System.err.println("Invaild Screen Size!");
                                    System.exit(1);
                            }
                        }
                    }
                }
            });
            JPanel rowButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            rowButtonPanel.add(confirmButton);
            mainPanel.add(rowButtonPanel);

            frame.setLocationRelativeTo(null);
            frame.add(mainPanel);
            frame.setVisible(true);
        }
    }

    public static class DebugWindow extends JFrame {
        public DebugWindow(int width, int height, boolean fullscreen) {
            setTitle("Debug Window");
            if (fullscreen) {
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                GraphicsDevice gd = ge.getDefaultScreenDevice();
                gd.setFullScreenWindow(this);
            } else {
                setSize(width, height);
                setResizable(false);
            }
            setLocationRelativeTo(null);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            setLayout(new GridBagLayout());

            FontWrapper Tuffy_Bold = new FontWrapper("/fonts/Tuffy_Bold.ttf", "Tuffy_Bold");
            Font custFont = Tuffy_Bold.getFont(Font.PLAIN, 24);

            JButton squaresButt = new JButton("Draw Squares Test");
            squaresButt.setFont(custFont);
            squaresButt.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                   dispose();
                   new DrawSquaresWindow(width, height, fullscreen);
                }
            });
            add(squaresButt);

            squaresButt = new JButton("Draw Shapes Test");
            squaresButt.setFont(custFont);
            squaresButt.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dispose();
                    new DrawCustomWindow(width, height, fullscreen);
                }
            });
            add(squaresButt);

            setVisible(true);
        }
    }
    public static class DrawSquaresWindow extends JFrame {
        public DrawSquaresWindow(int width, int height, boolean fullscreen) {
            setTitle("Drawing Squares");
            if (fullscreen) {
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                GraphicsDevice gd = ge.getDefaultScreenDevice();
                gd.setFullScreenWindow(this);
            } else {
                setSize(width, height);
                setResizable(false);
            }
            setLocationRelativeTo(null);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            setLayout(null);

            int x = 10, y = 10, thickness = 1;
            int widthT = 100;
            int heightT = 100;

            FontWrapper Tuffy_Bold = new FontWrapper("/fonts/Tuffy_Bold.ttf", "Tuffy_Bold");
            for (int i = 0; i <50; i++) {
                RectPanel temp = new RectPanel(x, y, widthT, heightT, Color.red, Color.black, thickness);
                temp.setLayout(new GridBagLayout());

                JLabel tempLabel = new JLabel(thickness + "");
                tempLabel.setFont(Tuffy_Bold.getFont(Font.PLAIN, 20));
                tempLabel.setForeground(Color.white);
                temp.add(tempLabel);

                add(temp);

                x += widthT+25;
                if (x+widthT >= width) {
                    x = 10;
                    y += heightT+25;
                }
                thickness += 1;
            }

            setVisible(true);
        }
    }

    public static class DrawCustomWindow extends JFrame {
        public DrawCustomWindow(int width, int height, boolean fullscreen) {
            setTitle("Drawing Custom sizes and shapes");
            if (fullscreen) {
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                GraphicsDevice gd = ge.getDefaultScreenDevice();
                gd.setFullScreenWindow(this);
            } else {
                setSize(width, height);
                setResizable(false);
            }
            setLocationRelativeTo(null);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            setLayout(null);

            RectPanel temp = new RectPanel(5, 5, 30, 45, Color.red, Color.black, 3);
            RectPanel temp2 = new RectPanel(75, 5, 24, 55, Color.green, Color.black, 2);
            RectPanel temp3 = new RectPanel(150, 5, 35, 25, Color.yellow, Color.black, 6);
            RectPanel temp4 = new RectPanel(225, 5, 10, 122, Color.blue);
            RectPanel temp8 = new RectPanel(525, 5, 70, 70, Color.red, Color.black, 2);
            RectPanel temp5 = new RectPanel(300, 5, 70, 78, Color.green, Color.black, 6);
            RectPanel temp6 = new RectPanel(375, 5, 56, 33, Color.yellow, Color.black, 1);
            RectPanel temp7 = new RectPanel(450, 5, 44, 12, Color.blue, Color.black, 10);

            RectPanel temp9 = new RectPanel(525, 5, 30, 45, "addas", Color.black, 3);
            RectPanel temp10 = new RectPanel(600, 5, 24, 55, "addas", Color.black, 2);
            RectPanel temp11 = new RectPanel(675, 5, 35, 25, "addas", Color.black, 6);
            RectPanel temp12 = new RectPanel(750, 5, 10, 122, "addas");
            RectPanel temp13 = new RectPanel(825, 5, 70, 70, "addas", Color.black, 2);
            RectPanel temp14 = new RectPanel(900, 5, 70, 78, "addas", Color.black, 6);
            RectPanel temp15 = new RectPanel(975, 5, 56, 33, "addas", Color.black, 1);
            RectPanel temp16 = new RectPanel(1050, 5, 44, 12, "addas", Color.black, 10);

            add(temp);
            add(temp2);
            add(temp3);
            add(temp4);
            add(temp8);
            add(temp5);
            add(temp6);
            add(temp7);

            add(temp9);
            add(temp10);
            add(temp11);
            add(temp12);
            add(temp13);
            add(temp14);
            add(temp15);
            add(temp16);

            QuadShapeDrawer tempDraw = new QuadShapeDrawer(new Point(10, 200));
            tempDraw.drawLine(0, 100);
            tempDraw.drawLine(70, 77);
            tempDraw.drawLine(190, 120);

            QuadrilateralPanel test = new QuadrilateralPanel(tempDraw.getPoints(), Color.ORANGE, Color.black, 2);
            add(test);

            tempDraw = new QuadShapeDrawer(new Point(300, 200));
            tempDraw.drawLine(10, 90);
            tempDraw.drawLine(70, 77);
            tempDraw.drawLine(150, 120);
            test = new QuadrilateralPanel(tempDraw.getPoints(), Color.GRAY);
            add(test);

            tempDraw = new QuadShapeDrawer(new Point(500, 200));
            tempDraw.drawLine(0, 100);
            tempDraw.drawLine(70, 77);
            tempDraw.drawLine(190, 120);

            test = new QuadrilateralPanel(tempDraw.getPoints(), "ssss");
            add(test);

            tempDraw = new QuadShapeDrawer(new Point(700, 200));
            tempDraw.drawLine(10, 90);
            tempDraw.drawLine(70, 77);
            tempDraw.drawLine(150, 120);
            test = new QuadrilateralPanel(tempDraw.getPoints(), "sssss");
            add(test);

            tempDraw = new QuadShapeDrawer(new Point(10, 400));
            tempDraw.drawLine(12, 178);
            tempDraw.drawLine(98, 134);
            tempDraw.drawLine(123, 120);
            test = new QuadrilateralPanel(tempDraw.getPoints(), "sssss", Color.black);
            add(test);

            tempDraw = new QuadShapeDrawer(new Point(210, 400));
            tempDraw.drawLine(2, 300);
            tempDraw.drawLine(98, 134);
            tempDraw.drawLine(123, 120);
            test = new QuadrilateralPanel(tempDraw.getPoints(), "sssss", Color.black, 5);
            add(test);

            tempDraw = new QuadShapeDrawer(new Point(610, 400));
            tempDraw.drawLine(2, 150);
            tempDraw.drawLine(98, 134);
            tempDraw.drawLine(123, 120);
            test = new QuadrilateralPanel(tempDraw.getPoints(), "sssss", Color.black, 4);
            add(test);


            setVisible(true);
        }
    }

    public static class GameWindow extends JFrame {
        public GameWindow(int width, int height, boolean fullscreen) {
            setTitle("Game Window");
            if (fullscreen) {
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                GraphicsDevice gd = ge.getDefaultScreenDevice();
                gd.setFullScreenWindow(this);
            } else {
                setSize(width, height);
                setResizable(false);
            }
            setLocationRelativeTo(null);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            JButton squaresButt = new JButton("Close");
            FontWrapper Tuffy_Bold = new FontWrapper("/fonts/Tuffy_Bold.ttf", "Tuffy_Bold");
            Font custFont = Tuffy_Bold.getFont(Font.PLAIN, 24);
            squaresButt.setFont(custFont);
            squaresButt.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            });
            add(squaresButt);

            setLayout(new GridBagLayout());
            setVisible(true);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GraphicWindow());
    }
}