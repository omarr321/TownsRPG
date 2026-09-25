package GUI;

import GUI.Lighting.LightLayer;
import GUI.Lighting.LightMgmt;
import GUI.Lighting.LightPoint;
import GUI.Lighting.LightingLayerUI;
import Helper.Point;
import Helper.QuadShapeDrawer;
import GUI.CustomPanels.QuadrilateralPanel;
import GUI.CustomPanels.RectPanel;
import Helper.FontWrapper;
import Helper.GameSettings;
import roomClass.Room;
import roomClass.roomParts.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This class handles all the drawing to the screen and user input.
 */
public class DisplayMgmt {

    private static final FontWrapper tuffyBold = new FontWrapper("/fonts/Tuffy_Bold.ttf", "Tuffy Bold");
    private static final FontWrapper tuffyPlain = new FontWrapper("/fonts/Tuffy.ttf","Tuffy Plain");
    /**
     * This window asks for user graphic setting then moves to the game with the setting set.
     */
    public static class GraphicWindow extends JFrame {

        public GraphicWindow(){
            super("Graphics");

            GameSettings.fullScreen = true;
            GameSettings.screenWidth = 3840;
            GameSettings.screenHeight = 2160;

            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(550,300);
            setResizable(false);

            Font labelFont = tuffyPlain.getFont(24);
            Font dropdownFont = tuffyPlain.getFont(18);
            Font titleFont = tuffyBold.getFont(40);

            JLabel titleLabel = new JLabel("Graphical Options");
            titleLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 5));
            titleLabel.setFont(titleFont);
            titleLabel.setForeground(Color.BLACK);
            JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            titlePanel.add(titleLabel);

            JComboBox<String> fullscreenBox = new JComboBox<>(new String[]{"Yes", "No"});
            JComboBox<String> graphicsBox = new JComboBox<>(new String[]{"2560 X 1440","1920 X 1080", "1280 X 720"});
            fullscreenBox.setFont(dropdownFont);
            graphicsBox.setFont(dropdownFont);
            graphicsBox.setEnabled(false);

            JLabel fullscreenLabel = new JLabel("Do you want to run in fullscreen mode?");
            JLabel graphicsLabel = new JLabel("Window Resolution?");
            fullscreenLabel.setFont(labelFont);
            graphicsLabel.setFont(labelFont);

            fullscreenBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String fullscreenOption = (String) fullscreenBox.getSelectedItem();
                    if (fullscreenOption.equals("Yes")){
                        graphicsBox.setEnabled(false);
                        GameSettings.fullScreen = true;
                    } else if (fullscreenOption.equals("No")){
                        graphicsBox.setEnabled(true);
                        GameSettings.fullScreen = false;
                    }
                }
            });

            graphicsBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String resOption = (String) graphicsBox.getSelectedItem();
                    GameSettings.screenWidth = Integer.parseInt(resOption.split(" X ")[0]);
                    GameSettings.screenHeight = Integer.parseInt(resOption.split(" X ")[1]);
                }
            });

            JPanel rowFullscreenPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            rowFullscreenPanel.add(fullscreenLabel);
            rowFullscreenPanel.add(fullscreenBox);
            rowFullscreenPanel.setBorder(new EmptyBorder(5,5,5,5));

            JPanel rowGraphicsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            rowGraphicsPanel.add(graphicsLabel);
            rowGraphicsPanel.add(graphicsBox);
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
                    GraphicWindow.this.dispose();

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

            setLocationRelativeTo(null);
            add(mainPanel);

            setVisible(true);
        }
    }

    public static class DebugWindow extends JFrame {
        private final CardLayout cardLayout;
        private final JPanel cardContainer;
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

            cardLayout = new CardLayout();
            cardContainer = new JPanel(cardLayout);

            cardContainer.add(createDebugSquares(), "DEBUG_SQUARES");
            cardContainer.add(createDebugMain(), "DEBUG_MAIN");
            cardContainer.add(createDebugShapes(), "DEBUG_SHAPES");
            cardContainer.add(createDebugRoomTest(), "DEBUG_ROOM_TEST");
            cardContainer.add(createDebugLightRoomTest(false, false), "DEBUG_ROOM_LIGHT_TEST");
            cardContainer.add(createDebugLightRoomTest(true, true), "DEBUG_ROOM_BLOCKERS_TEST");

            setContentPane(cardContainer);
            switchToCard("DEBUG_MAIN");

            InputMap inputMap = this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            inputMap.put(KeyStroke.getKeyStroke("ESCAPE"), "quitAction");
            inputMap.put(KeyStroke.getKeyStroke("L"), "lightAction");

            ActionMap actionMap = this.getRootPane().getActionMap();
            actionMap.put("quitAction", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (currentCard.equals("DEBUG_MAIN")) {
                        GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(null);
                        DebugWindow.this.dispose();
                        new GraphicWindow();
                    } else {
                        switchToCard("DEBUG_MAIN");
                    }
                }
            });
            actionMap.put("lightAction",new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (currentCard.equals("DEBUG_ROOM_TEST")) {
                        switchToCard("DEBUG_ROOM_LIGHT_TEST");
                    } else if (currentCard.equals("DEBUG_ROOM_LIGHT_TEST")) {
                        switchToCard("DEBUG_ROOM_BLOCKERS_TEST");
                    } else if (currentCard.equals("DEBUG_ROOM_BLOCKERS_TEST")) {
                        switchToCard("DEBUG_ROOM_TEST");
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
            Font custFont = tuffyPlain.getFont(24);

            JButton squaresButt = new JButton("Draw Squares Test");
            squaresButt.setFont(custFont);
            squaresButt.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
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

            for (int i = 0; i < 50; i++) {
                RectPanel temp = new RectPanel(x, y, widthT, heightT, Color.red, Color.black, thickness);
                temp.setLayout(new GridBagLayout());

                JLabel tempLabel = new JLabel(thickness + "");
                tempLabel.setFont(tuffyPlain.getFont(20));
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

            RectPanel temp9 = new RectPanel(525, 600, 30, 45, "addas", Color.black, 3);
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

            QuadShapeDrawer tempDraw = new QuadShapeDrawer(new Helper.Point(10, 200));
            tempDraw.drawLine(0, 100);
            tempDraw.drawLine(70, 77);
            tempDraw.drawLine(190, 120);

            QuadrilateralPanel test = new QuadrilateralPanel(tempDraw.getPoints(), Color.ORANGE, Color.black, 2);
            panel.add(test);

            tempDraw = new QuadShapeDrawer(new Helper.Point(300, 200));
            tempDraw.drawLine(10, 90);
            tempDraw.drawLine(70, 77);
            tempDraw.drawLine(150, 120);
            test = new QuadrilateralPanel(tempDraw.getPoints(), Color.GRAY);
            panel.add(test);

            tempDraw = new QuadShapeDrawer(new Helper.Point(500, 200));
            tempDraw.drawLine(0, 100);
            tempDraw.drawLine(70, 77);
            tempDraw.drawLine(190, 120);

            test = new QuadrilateralPanel(tempDraw.getPoints(), "ssss");
            panel.add(test);

            tempDraw = new QuadShapeDrawer(new Helper.Point(700, 200));
            tempDraw.drawLine(10, 90);
            tempDraw.drawLine(70, 77);
            tempDraw.drawLine(150, 120);
            test = new QuadrilateralPanel(tempDraw.getPoints(), "sssss");
            panel.add(test);

            tempDraw = new QuadShapeDrawer(new Helper.Point(10, 400));
            tempDraw.drawLine(12, 178);
            tempDraw.drawLine(98, 134);
            tempDraw.drawLine(123, 120);
            test = new QuadrilateralPanel(tempDraw.getPoints(), "sssss", Color.black);
            panel.add(test);

            tempDraw = new QuadShapeDrawer(new Helper.Point(210, 400));
            tempDraw.drawLine(2, 300);
            tempDraw.drawLine(98, 134);
            tempDraw.drawLine(123, 120);
            test = new QuadrilateralPanel(tempDraw.getPoints(), "sssss", Color.black, 5);
            panel.add(test);

            tempDraw = new QuadShapeDrawer(new Helper.Point(610, 400));
            tempDraw.drawLine(2, 150);
            tempDraw.drawLine(98, 134);
            tempDraw.drawLine(123, 120);
            test = new QuadrilateralPanel(tempDraw.getPoints(), "sssss", Color.black, 4);
            panel.add(test);

            tempDraw = new QuadShapeDrawer(new Helper.Point(800, 400));
            tempDraw.drawLine(2, 150);
            tempDraw.drawLine(98, 134);
            tempDraw.drawLine(123, 120);
            test = new QuadrilateralPanel(tempDraw.getPoints(), "sssss", Color.black, 4);
            test.setImageWarp(false);
            panel.add(test);

            tempDraw = new QuadShapeDrawer(new Helper.Point(900, 200));
            tempDraw.drawLine(12, 178);
            tempDraw.drawLine(98, 134);
            tempDraw.drawLine(123, 120);
            test = new QuadrilateralPanel(tempDraw.getPoints(), "sssss", Color.black);
            test.setImageWarp(false);
            panel.add(test);

            return panel;
        }


        private JPanel createDebugRoomTest() {
            Room<RoomComponent> room = createDebugRoom();

            return room.putToScreen();
        }

        private Room<RoomComponent> createDebugRoom() {
            final double WALL_RATIO = 1.5/3.0;
            final double SC_PERCENT = 0.85;
            final int SC_ANGLE = 30;
            final int OFFSCREEN_MULI = 4;

            RoomPoints roomPt = new RoomPoints(WALL_RATIO, SC_PERCENT, SC_ANGLE, OFFSCREEN_MULI);

            Floor floor = new Floor("/images/LightTest.png");
            Ceiling ceiling = new Ceiling("/images/LightTest.png");
            Room<RoomComponent> room = new Room<>(floor, ceiling, roomPt);
            room.setWall(new Wall("/images/LightTest.png"), 0);
            room.setWall(new Wall("/images/LightTest.png"), 1);
            room.setWall(new Wall("/images/LightTest.png"), 2);
            room.setWall(new Wall("/images/LightTest.png"), 3);
            room.setLookingIndex(1);

            return room;
        }

        private JPanel createDebugLightRoomTest(boolean showBlockers, boolean showLights) {
            JPanel litPanel = new JPanel(new BorderLayout());

            //1 - Creates the debug room!
            Room<RoomComponent> room = createDebugRoom();

            //2 - Creates the lightLayer and passes it to the room so it can add its LightBlockers
            LightLayer lightLayer = new LightLayer(GameSettings.screenWidth, GameSettings.screenHeight);
            room.updateLightLayer(lightLayer);

            LightMgmt lightMgmt = new LightMgmt(lightLayer, new Color(80, 100, 180), .35f);
            lightMgmt.setReflectSpread(75);
            LightingLayerUI lightingUI = new LightingLayerUI(lightMgmt);
            lightingUI.setShowBlockers(showBlockers);
            lightingUI.setShowLights(showLights);
            JLayer<JComponent> litLayer = new JLayer<>(room.putToScreen(), lightingUI);

            Point ULC = new Point(Math.toIntExact(Math.round(GameSettings.screenWidth * .2)), Math.toIntExact(Math.round(GameSettings.screenHeight * .2)));
            Point LLC = new Point(Math.toIntExact(Math.round(GameSettings.screenWidth * .2)), Math.toIntExact(Math.round(GameSettings.screenHeight - GameSettings.screenHeight * .2)));
            Point URC = new Point(Math.toIntExact(Math.round(GameSettings.screenWidth - GameSettings.screenWidth * .2)), Math.toIntExact(Math.round(GameSettings.screenHeight * .2)));
            Point LRC = new Point(Math.toIntExact(Math.round(GameSettings.screenWidth - GameSettings.screenWidth * .2)), Math.toIntExact(Math.round(GameSettings.screenHeight - GameSettings.screenHeight * .2)));

            LightPoint orangePoint = new LightPoint(new Point(GameSettings.screenWidth/2, GameSettings.screenHeight/2), LightPoint.LightShape.CIRCLE, 600, 0, 0, .01f, Color.orange);
            lightMgmt.addLight(orangePoint);

            LightPoint whitePoint = new LightPoint(ULC, LightPoint.LightShape.SQUARE, 400, 0, 0, .6f, Color.white);
            lightMgmt.addLight(whitePoint);

            LightPoint greenPoint = new LightPoint(LLC, LightPoint.LightShape.SQUARE, 400, 0, 0, .4f, new Color(31, 219, 47));
            lightMgmt.addLight(greenPoint);

            orangePoint = new LightPoint(URC, LightPoint.LightShape.CIRCLE, 900, 0, 0, .50f, Color.red);
            lightMgmt.addLight(orangePoint);

            whitePoint = new LightPoint(LRC, LightPoint.LightShape.SQUARE, 400, 0, 0, .1f, Color.white);
            lightMgmt.addLight(whitePoint);


            litPanel.add(litLayer, BorderLayout.CENTER);
            return litPanel;
        }
    }

    public static class GameWindow extends JFrame {
        public GameWindow() {
            setTitle("Game Window");
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

            JButton squaresButt = new JButton("Close");
            Font custFont = tuffyPlain.getFont(24);
            squaresButt.setFont(custFont);
            squaresButt.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            });
            add(squaresButt);

            setVisible(true);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new GraphicWindow();
            }
        });
    }
}