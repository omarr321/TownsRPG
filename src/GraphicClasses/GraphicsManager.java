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

            GameSettings.fullScreen = true;
            GameSettings.screenWidth = 3840;
            GameSettings.screenHeight = 2160;

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
                        GameSettings.fullScreen = true;
                    } else if (fullscreenOption.equals("No")){
                        GraphicsBox.setEnabled(true);
                        GameSettings.fullScreen = false;
                    }
                }
            });

            GraphicsBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    resOption = (String) GraphicsBox.getSelectedItem();
                    GameSettings.screenWidth = Integer.parseInt(resOption.split(" X ")[0]);
                    GameSettings.screenHeight = Integer.parseInt(resOption.split(" X ")[1]);
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
                        new DebugWindow();

                    } else {
                        new GameWindow();
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
        private CardLayout cardLayout;
        private JPanel cardContainer;
        private String currentCard = "";

        public DebugWindow() {
            setTitle("Debug");
            if (GameSettings.fullScreen) {
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                GraphicsDevice gd = ge.getDefaultScreenDevice();
                gd.setFullScreenWindow(this);
                GameSettings.screenWidth = gd.getDisplayMode().getWidth();
                GameSettings.screenHeight = gd.getDisplayMode().getHeight();
            } else {
                getContentPane().setPreferredSize(new Dimension(GameSettings.screenWidth, GameSettings.screenHeight));
                setResizable(false);
                pack();
            }
            setLocationRelativeTo(null);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLayout(new GridBagLayout());

            cardLayout = new CardLayout();
            cardContainer = new JPanel(cardLayout);

            cardContainer.add(createDebugSquares(), "DEBUG_SQUARES");
            cardContainer.add(createDebugMain(), "DEBUG_MAIN");
            cardContainer.add(createDebugShapes(), "DEBUG_SHAPES");
            cardContainer.add(createDebugRoomTest(), "DEBUG_ROOM_TEST");

            setContentPane(cardContainer);
            switchToCard("DEBUG_MAIN");

            InputMap inputMap = this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            inputMap.put(KeyStroke.getKeyStroke("ESCAPE"), "quitAction");

            ActionMap actionMap = this.getRootPane().getActionMap();
            actionMap.put("quitAction", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (currentCard.equals("DEBUG_MAIN")) {
                        dispose();
                        new GraphicWindow();
                    } else {
                        switchToCard("DEBUG_MAIN");
                    }
                }
            });

            setVisible(true);
        }

        public void switchToCard(String card) {
            this.currentCard = card;
            this.cardLayout.show(cardContainer, card);
        }

        private JPanel createDebugMain() {
            JPanel panel = new JPanel();
            panel.setPreferredSize(new Dimension(GameSettings.screenWidth, GameSettings.screenHeight));
            FontWrapper Tuffy_Bold = new FontWrapper("/fonts/Tuffy_Bold.ttf", "Tuffy_Bold");
            Font custFont = Tuffy_Bold.getFont(Font.PLAIN, 24);

            JButton squaresButt = new JButton("Draw Squares Test");
            squaresButt.setFont(custFont);
            squaresButt.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    //dispose();
                    //new DrawSquaresWindow(width, height, fullscreen);
                    switchToCard("DEBUG_SQUARES");
                }
            });
            panel.add(squaresButt);

            squaresButt = new JButton("Draw Shapes Test");
            squaresButt.setFont(custFont);
            squaresButt.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    switchToCard("DEBUG_SHAPES");
                }
            });
            panel.add(squaresButt);

            squaresButt = new JButton("Draw Room Test");
            squaresButt.setFont(custFont);
            squaresButt.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    switchToCard("DEBUG_ROOM_TEST");
                }
            });
            panel.add(squaresButt);

            return panel;
        }

        private JPanel createDebugSquares() {
            JPanel panel = new JPanel();
            panel.setPreferredSize(new Dimension(GameSettings.screenWidth, GameSettings.screenHeight));
            panel.setLayout(null);

            int x = 10, y = 10, thickness = 1;
            int widthT = 100;
            int heightT = 100;

            FontWrapper Tuffy_Bold = new FontWrapper("/fonts/Tuffy_Bold.ttf", "Tuffy_Bold");
            for (int i = 0; i < 50; i++) {
                RectPanel temp = new RectPanel(x, y, widthT, heightT, Color.red, Color.black, thickness);
                temp.setLayout(new GridBagLayout());

                JLabel tempLabel = new JLabel(thickness + "");
                tempLabel.setFont(Tuffy_Bold.getFont(Font.PLAIN, 20));
                tempLabel.setForeground(Color.white);
                temp.add(tempLabel);

                panel.add(temp);

                x += widthT + 25;
                if (x + widthT >= GameSettings.screenWidth) {
                    x = 10;
                    y += heightT + 25;
                }
                thickness += 1;
            }
            return panel;
        }

        private JPanel createDebugShapes() {
            JPanel panel = new JPanel();
            panel.setPreferredSize(new Dimension(GameSettings.screenWidth, GameSettings.screenHeight));
            panel.setLayout(null);

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

            panel.add(temp);
            panel.add(temp2);
            panel.add(temp3);
            panel.add(temp4);
            panel.add(temp8);
            panel.add(temp5);
            panel.add(temp6);
            panel.add(temp7);

            panel.add(temp9);
            panel.add(temp10);
            panel.add(temp11);
            panel.add(temp12);
            panel.add(temp13);
            panel.add(temp14);
            panel.add(temp15);
            panel.add(temp16);

            QuadShapeDrawer tempDraw = new QuadShapeDrawer(new Point(10, 200));
            tempDraw.drawLine(0, 100);
            tempDraw.drawLine(70, 77);
            tempDraw.drawLine(190, 120);

            QuadrilateralPanel test = new QuadrilateralPanel(tempDraw.getPoints(), Color.ORANGE, Color.black, 2);
            panel.add(test);

            tempDraw = new QuadShapeDrawer(new Point(300, 200));
            tempDraw.drawLine(10, 90);
            tempDraw.drawLine(70, 77);
            tempDraw.drawLine(150, 120);
            test = new QuadrilateralPanel(tempDraw.getPoints(), Color.GRAY);
            panel.add(test);

            tempDraw = new QuadShapeDrawer(new Point(500, 200));
            tempDraw.drawLine(0, 100);
            tempDraw.drawLine(70, 77);
            tempDraw.drawLine(190, 120);

            test = new QuadrilateralPanel(tempDraw.getPoints(), "ssss");
            panel.add(test);

            tempDraw = new QuadShapeDrawer(new Point(700, 200));
            tempDraw.drawLine(10, 90);
            tempDraw.drawLine(70, 77);
            tempDraw.drawLine(150, 120);
            test = new QuadrilateralPanel(tempDraw.getPoints(), "sssss");
            panel.add(test);

            tempDraw = new QuadShapeDrawer(new Point(10, 400));
            tempDraw.drawLine(12, 178);
            tempDraw.drawLine(98, 134);
            tempDraw.drawLine(123, 120);
            test = new QuadrilateralPanel(tempDraw.getPoints(), "sssss", Color.black);
            panel.add(test);

            tempDraw = new QuadShapeDrawer(new Point(210, 400));
            tempDraw.drawLine(2, 300);
            tempDraw.drawLine(98, 134);
            tempDraw.drawLine(123, 120);
            test = new QuadrilateralPanel(tempDraw.getPoints(), "sssss", Color.black, 5);
            panel.add(test);

            tempDraw = new QuadShapeDrawer(new Point(610, 400));
            tempDraw.drawLine(2, 150);
            tempDraw.drawLine(98, 134);
            tempDraw.drawLine(123, 120);
            test = new QuadrilateralPanel(tempDraw.getPoints(), "sssss", Color.black, 4);
            panel.add(test);

            tempDraw = new QuadShapeDrawer(new Point(800, 400));
            tempDraw.drawLine(2, 150);
            tempDraw.drawLine(98, 134);
            tempDraw.drawLine(123, 120);
            test = new QuadrilateralPanel(tempDraw.getPoints(), "sssss", Color.black, 4);
            test.setImageWarp(false);
            panel.add(test);

            tempDraw = new QuadShapeDrawer(new Point(900, 200));
            tempDraw.drawLine(12, 178);
            tempDraw.drawLine(98, 134);
            tempDraw.drawLine(123, 120);
            test = new QuadrilateralPanel(tempDraw.getPoints(), "sssss", Color.black);
            test.setImageWarp(false);
            panel.add(test);

            return panel;
        }

        private JPanel createDebugRoomTest() {
            JPanel panel = new JPanel();
            panel.setPreferredSize(new Dimension(GameSettings.screenWidth, GameSettings.screenHeight));
            panel.setLayout(null);

            RectPanel backwall = new RectPanel(GameSettings.screenWidth/2-500, GameSettings.screenHeight/2-300, 1000, 600, "pppp", Color.black, 2);
            panel.add(backwall);

            return panel;
        }
    }

    public static class GameWindow extends JFrame {
        public GameWindow() {
            setTitle("Game Window");
            if (GameSettings.fullScreen) {
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                GraphicsDevice gd = ge.getDefaultScreenDevice();
                gd.setFullScreenWindow(this);
            } else {
                setSize(GameSettings.screenWidth, GameSettings.screenHeight);
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