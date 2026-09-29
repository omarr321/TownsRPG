package GUI;

import GUI.Lighting.*;
import Helper.Point;
import Helper.QuadShapeDrawer;
import GUI.CustomPanels.QuadrilateralPanel;
import GUI.CustomPanels.RectPanel;
import Helper.FontWrapper;
import Helper.GameSettings;
import Engine.Interactions.FlagInteraction;
import RoomClasses.Room;
import RoomClasses.RoomObjects.BasicObj;
import RoomClasses.RoomObjects.InteractableObj;
import RoomClasses.roomParts.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.Objects;

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
                    if (Objects.equals(fullscreenOption, "Yes")){
                        graphicsBox.setEnabled(false);
                        GameSettings.fullScreen = true;
                    } else if (Objects.equals(fullscreenOption, "No")){
                        graphicsBox.setEnabled(true);
                        GameSettings.fullScreen = false;
                    }
                }
            });

            graphicsBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String resOption = (String) graphicsBox.getSelectedItem();
                    GameSettings.screenWidth = Integer.parseInt(Objects.requireNonNull(resOption).split(" X ")[0]);
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
            cardContainer.add(createDebugLightRoomTest(false, false, false), "DEBUG_ROOM_LIGHT_TEST");
            cardContainer.add(createDebugLightRoomTest(true, true, true), "DEBUG_ROOM_BLOCKERS_TEST");
            cardContainer.add(createBasicRoom(false, false, false), "BASIC_ROOM_TEST");
            cardContainer.add(createBasicRoom(true, true, true), "BASIC_ROOM_DEBUG_TEST");

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
                    } else if (currentCard.equals("BASIC_ROOM_TEST")) {
                        switchToCard("BASIC_ROOM_DEBUG_TEST");
                    } else if (currentCard.equals("BASIC_ROOM_DEBUG_TEST")) {
                        switchToCard("BASIC_ROOM_TEST");
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

            squaresButt = new JButton("Basic Room Test");
            squaresButt.setFont(custFont);
            squaresButt.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    switchToCard("BASIC_ROOM_TEST");
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
            JPanel roomJ = room.putToScreen();

            MouseAdapter clicking = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    for (InteractableObj obj : room.getInteractables()) {
                        Helper.Point[] corners = obj.getShapeCorners();
                        Polygon shape = new Polygon();
                        for (Helper.Point p : corners) {
                            shape.addPoint(p.getX(), p.getY());
                        }
                        if (shape.contains(e.getX(), e.getY())) {
                            obj.trigger();
                            break; // only trigger the one that was clicked
                        }
                    }
                }
            };
            roomJ.addMouseListener(clicking);

            return roomJ;
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
            room.setWall(new Wall<>("/images/LightTest.png"), 0);
            room.setWall(new Wall<>("/images/LightTest.png"), 1);
            room.setWall(new Wall<>("/images/LightTest.png"), 2);
            room.setWall(new Wall<>("/images/LightTest.png"), 3);
            room.setLookingIndex(1);

            QuadShapeDrawer testP = new QuadShapeDrawer(new Point(200, 200));
            testP.drawLine(0, 100);
            testP.drawLine(90, 300);
            testP.drawLine(180, 100);
            BasicObj testObj = new BasicObj(testP.getPoints(), "sss", false);
            testObj.addLightBlocker("obj", new LightBlocker(testObj, LightBlocker.LightTag.BLOCK));

            try{
                Wall<BasicObj> wall = (Wall<BasicObj>) room.getLookingWall();
                wall.addRoomObj("testObj", testObj);
            } catch (ClassCastException e) {
                System.err.println(e);
            }

            testP = new QuadShapeDrawer(new Point(1000, 400));
            testP.drawLine(0, 100);
            testP.drawLine(90, 300);
            testP.drawLine(180, 100);
            testObj = new BasicObj(testP.getPoints(), "sss", false);
            testObj.addLightBlocker("obj", new LightBlocker(testObj, LightBlocker.LightTag.REFLECT));
            testObj.getLightBlocker("obj").setReflectDist(20);

            try{
                Wall<BasicObj> wall = (Wall<BasicObj>) room.getLookingWall();
                wall.addRoomObj("testObj2", testObj);
            } catch (ClassCastException e) {
                System.err.println(e);
            }

            testP = new QuadShapeDrawer(new Point(1200, 500));
            testP.drawLine(0, 100);
            testP.drawLine(90, 300);
            testP.drawLine(180, 100);
            testObj = new BasicObj(testP.getPoints(), "sss", false);
            testObj.addLightBlocker("obj", new LightBlocker(testObj, LightBlocker.LightTag.LIT));
            testObj.getLightBlocker("obj").setLitBlend(.35f);

            try{
                Wall<BasicObj> wall = (Wall<BasicObj>) room.getLookingWall();
                wall.addRoomObj("testObj3", testObj);
            } catch (ClassCastException e) {
                System.err.println(e);
            }

            testP = new QuadShapeDrawer(new Point(500, 700));
            testP.drawLine(0, 50);
            testP.drawLine(90, 50);
            testP.drawLine(180, 50);
            InteractableObj testObj1 = new InteractableObj(testP.getPoints(), "sss", false);
            testObj1.addLightBlocker("obj", new LightBlocker(testObj1, LightBlocker.LightTag.REFLECT));
            testObj1.getLightBlocker("obj").setReflectDist(75);

            FlagInteraction temp = new FlagInteraction("This is a test", FlagInteraction.InteractionType.DIALOGUE);
            FlagInteraction temp2 = new FlagInteraction("", FlagInteraction.InteractionType.FLAG);
            temp.setNextTrigger(temp2);
            testObj1.setEntryPoint(temp);

            try{
                Wall<InteractableObj> wall = (Wall<InteractableObj>) room.getLookingWall();
                wall.addRoomObj("testObj4", testObj1);
            } catch (ClassCastException e) {
                System.err.println(e);
            }

            Point ULC = new Point(Math.toIntExact(Math.round(GameSettings.screenWidth * .2)), Math.toIntExact(Math.round(GameSettings.screenHeight * .2)));
            Point LLC = new Point(Math.toIntExact(Math.round(GameSettings.screenWidth * .2)), Math.toIntExact(Math.round(GameSettings.screenHeight - GameSettings.screenHeight * .2)));
            Point URC = new Point(Math.toIntExact(Math.round(GameSettings.screenWidth - GameSettings.screenWidth * .2)), Math.toIntExact(Math.round(GameSettings.screenHeight * .2)));
            Point LRC = new Point(Math.toIntExact(Math.round(GameSettings.screenWidth - GameSettings.screenWidth * .2)), Math.toIntExact(Math.round(GameSettings.screenHeight - GameSettings.screenHeight * .2)));

            LightPoint whitePoint = new LightPoint(ULC, LightPoint.LightShape.SQUARE, 400, 0, 0, .6f, Color.blue);
            room.getLookingWall().addLightPoint("wPoint", whitePoint);

            LightPoint greenPoint = new LightPoint(LLC, LightPoint.LightShape.SQUARE, 400, 0, 0, .4f, new Color(31, 219, 47));
            room.getLookingWall().addLightPoint("gPoint", greenPoint);

            LightPoint orangePoint = new LightPoint(URC, LightPoint.LightShape.CIRCLE, 900, 0, 0, .50f, Color.red);
            room.getLookingWall().addLightPoint("rPoint", orangePoint);

            whitePoint = new LightPoint(LRC, LightPoint.LightShape.SQUARE, 400, 0, 0, .4f, Color.blue);
            room.getLookingWall().addLightPoint("bPoint", whitePoint);

            LightPoint mouseLight = new LightPoint(new Point(0, 0), LightPoint.LightShape.CIRCLE, 100, 0, 0, .5f, Color.white);
            room.getLookingWall().addLightPoint("mouseLight", mouseLight);

            return room;
        }

        private Room<RoomComponent> createBasicRoom() {
            final double WALL_RATIO = 1.5/3.0;
            final double SC_PERCENT = 0.85;
            final int SC_ANGLE = 30;
            final int OFFSCREEN_MULI = 4;

            RoomPoints roomPt = new RoomPoints(WALL_RATIO, SC_PERCENT, SC_ANGLE, OFFSCREEN_MULI);

            Floor floor = new Floor("/images/WoodFloor.png");
            Ceiling ceiling = new Ceiling("/images/WoodFloor.png");
            Room<RoomComponent> room = new Room<>(floor, ceiling, roomPt);
            room.setWall(new Wall<>("/images/BrickWall.png"), 0);
            room.setWall(new Wall<>("/images/BrickWall.png"), 1);
            room.setWall(new Wall<>("/images/BrickWall.png"), 2);
            room.setWall(new Wall<>("/images/BrickWall.png"), 3);
            room.setLookingIndex(1);

            QuadShapeDrawer testP = new QuadShapeDrawer(new Point(200, 200));
            testP.drawLine(0, 100);
            testP.drawLine(90, 300);
            testP.drawLine(180, 100);
            BasicObj testObj = new BasicObj(testP.getPoints(), "/images/Crate.png", false);
            testObj.addLightBlocker("obj", new LightBlocker(testObj, LightBlocker.LightTag.BLOCK));

            try{
                Wall<BasicObj> wall = (Wall<BasicObj>) room.getLookingWall();
                wall.addRoomObj("testObj", testObj);
            } catch (ClassCastException e) {
                System.err.println(e);
            }

            testP = new QuadShapeDrawer(new Point(1000, 400));
            testP.drawLine(0, 100);
            testP.drawLine(90, 300);
            testP.drawLine(180, 100);
            testObj = new BasicObj(testP.getPoints(), "/images/LampTransparent.png", false);
            testObj.addLightBlocker("obj", new LightBlocker(testObj, LightBlocker.LightTag.LIT));
            testObj.getLightBlocker("obj").setReflectDist(20);

            try{
                Wall<BasicObj> wall = (Wall<BasicObj>) room.getLookingWall();
                wall.addRoomObj("testObj2", testObj);
            } catch (ClassCastException e) {
                System.err.println(e);
            }

            testP = new QuadShapeDrawer(new Point(1200, 500));
            testP.drawLine(0, 100);
            testP.drawLine(90, 300);
            testP.drawLine(180, 100);
            testObj = new BasicObj(testP.getPoints(), "/images/Crate.png", false);
            testObj.addLightBlocker("obj", new LightBlocker(testObj, LightBlocker.LightTag.LIT));
            testObj.getLightBlocker("obj").setLitBlend(.35f);

            try{
                Wall<BasicObj> wall = (Wall<BasicObj>) room.getLookingWall();
                wall.addRoomObj("testObj3", testObj);
            } catch (ClassCastException e) {
                System.err.println(e);
            }

            testP = new QuadShapeDrawer(new Point(500, 700));
            testP.drawLine(0, 50);
            testP.drawLine(90, 50);
            testP.drawLine(180, 50);
            InteractableObj testObj1 = new InteractableObj(testP.getPoints(), "/images/Crate.png", false);
            testObj1.addLightBlocker("obj", new LightBlocker(testObj1, LightBlocker.LightTag.REFLECT));
            testObj1.getLightBlocker("obj").setReflectDist(75);

            FlagInteraction temp = new FlagInteraction("This is a test", FlagInteraction.InteractionType.DIALOGUE);
            FlagInteraction temp2 = new FlagInteraction("", FlagInteraction.InteractionType.FLAG);
            temp.setNextTrigger(temp2);
            testObj1.setEntryPoint(temp);

            try{
                Wall<InteractableObj> wall = (Wall<InteractableObj>) room.getLookingWall();
                wall.addRoomObj("testObj4", testObj1);
            } catch (ClassCastException e) {
                System.err.println(e);
            }

            Point ULC = new Point(Math.toIntExact(Math.round(GameSettings.screenWidth * .2)), Math.toIntExact(Math.round(GameSettings.screenHeight * .2)));
            Point LLC = new Point(Math.toIntExact(Math.round(GameSettings.screenWidth * .2)), Math.toIntExact(Math.round(GameSettings.screenHeight - GameSettings.screenHeight * .2)));
            Point URC = new Point(Math.toIntExact(Math.round(GameSettings.screenWidth - GameSettings.screenWidth * .2)), Math.toIntExact(Math.round(GameSettings.screenHeight * .2)));
            Point LRC = new Point(Math.toIntExact(Math.round(GameSettings.screenWidth - GameSettings.screenWidth * .2)), Math.toIntExact(Math.round(GameSettings.screenHeight - GameSettings.screenHeight * .2)));

            LightPoint whitePoint = new LightPoint(ULC, LightPoint.LightShape.SQUARE, 400, 0, 0, .6f, Color.blue);
            room.getLookingWall().addLightPoint("wPoint", whitePoint);

            LightPoint greenPoint = new LightPoint(LLC, LightPoint.LightShape.SQUARE, 400, 0, 0, .4f, new Color(31, 219, 47));
            room.getLookingWall().addLightPoint("gPoint", greenPoint);

            LightPoint orangePoint = new LightPoint(URC, LightPoint.LightShape.CIRCLE, 900, 0, 0, .50f, Color.red);
            room.getLookingWall().addLightPoint("rPoint", orangePoint);

            whitePoint = new LightPoint(LRC, LightPoint.LightShape.SQUARE, 400, 0, 0, .4f, Color.blue);
            room.getLookingWall().addLightPoint("bPoint", whitePoint);

            LightPoint mouseLight = new LightPoint(new Point(0, 0), LightPoint.LightShape.CIRCLE, 100, 0, 0, .5f, Color.white);
            room.getLookingWall().addLightPoint("mouseLight", mouseLight);

            return room;
        }

        private JPanel createBasicRoom(boolean showBlockers, boolean showLights, boolean showInteractable) {
            JPanel litPanel = new JPanel(new BorderLayout());

            //1 - Creates the debug room.
            Room<RoomComponent> room = createBasicRoom();

            //2 - Creates the lightLayer and passes it to the room so it can add its LightBlockers
            LightLayer lightLayer = new LightLayer(GameSettings.screenWidth, GameSettings.screenHeight);
            room.updateLightLayer(lightLayer);

            //3 - Creates the lightMgmt and passes it to the room so it can add its Light Points
            LightMgmt lightMgmt = new LightMgmt(lightLayer, new Color(80, 100, 180), .55f);
            lightMgmt.setReflectSpread(80);
            room.updateLightMgmt(lightMgmt);

            //4 - Creating the light UI and passing in the LightMgmt
            LightingLayerUI lightingUI = new LightingLayerUI(lightMgmt);
            lightingUI.setShowBlockers(showBlockers);
            lightingUI.setShowLights(showLights);
            lightingUI.setShowInteractable(showInteractable);
            lightingUI.setInteractables(room::getInteractables);

            //5 - Creating the graphics for the room and applying the lightUI to it.
            JPanel roomPanel = room.putToScreen();
            JLayer<JComponent> litLayer = new JLayer<>(roomPanel, lightingUI);



            MouseAdapter followMouse = new MouseAdapter() {
                private final LightPoint mouseLight = room.getLookingWall().getLightPoint("mouseLight");
                @Override
                public void mouseMoved(MouseEvent e) { moveLight(e); }

                @Override
                public void mouseDragged(MouseEvent e) { moveLight(e); }

                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!lightMgmt.getLights().contains(mouseLight)) {
                        lightMgmt.addLight(mouseLight);
                    }
                    moveLight(e);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    lightMgmt.removeLight(mouseLight); // turn the light off when the mouse leaves
                    litLayer.repaint();
                }

                private void moveLight(MouseEvent e) {
                    Point free = lightLayer.nearestFreePoint(e.getX(), e.getY(), roomPanel.getWidth(), roomPanel.getHeight());
                    if (free == null) {
                        return; // nowhere free to put it
                    }
                    mouseLight.getLoc().setX(free.getX());
                    mouseLight.getLoc().setY(free.getY());
                    litLayer.repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    for (InteractableObj obj : room.getInteractables()) {
                        Helper.Point[] corners = obj.getShapeCorners();
                        Polygon shape = new Polygon();
                        for (Helper.Point p : corners) {
                            shape.addPoint(p.getX(), p.getY());
                        }
                        if (shape.contains(e.getX(), e.getY())) {
                            obj.trigger();
                            break; // only trigger the one that was clicked
                        }
                    }
                }
            };

            roomPanel.addMouseListener(followMouse);
            roomPanel.addMouseMotionListener(followMouse);

            // Place the light at the mouse as soon as this panel shows up, even if the mouse hasn't moved
            roomPanel.addHierarchyListener(new HierarchyListener() {
                private final LightPoint mouseLight = room.getLookingWall().getLightPoint("mouseLight");
                @Override
                public void hierarchyChanged(HierarchyEvent e) {
                    if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) == 0 || !roomPanel.isShowing()) {
                        return;
                    }
                    PointerInfo pointer = MouseInfo.getPointerInfo();
                    if (pointer == null) {
                        return; // no mouse available
                    }
                    java.awt.Point mouse = pointer.getLocation();              // screen coordinates
                    SwingUtilities.convertPointFromScreen(mouse, roomPanel);   // now panel coordinates

                    if (roomPanel.contains(mouse)) {
                        mouseLight.getLoc().setX(mouse.x);
                        mouseLight.getLoc().setY(mouse.y);
                        if (!lightMgmt.getLights().contains(mouseLight)) {
                            lightMgmt.addLight(mouseLight);
                        }
                    } else {
                        lightMgmt.removeLight(mouseLight); // mouse is outside, so no light yet
                    }
                    litLayer.repaint();
                }
            });

            litPanel.add(litLayer, BorderLayout.CENTER);
            return litPanel;
        }

        private JPanel createDebugLightRoomTest(boolean showBlockers, boolean showLights, boolean showInteractable) {
            JPanel litPanel = new JPanel(new BorderLayout());

            //1 - Creates the debug room.
            Room<RoomComponent> room = createDebugRoom();

            //2 - Creates the lightLayer and passes it to the room so it can add its LightBlockers
            LightLayer lightLayer = new LightLayer(GameSettings.screenWidth, GameSettings.screenHeight);
            room.updateLightLayer(lightLayer);

            //3 - Creates the lightMgmt and passes it to the room so it can add its Light Points
            LightMgmt lightMgmt = new LightMgmt(lightLayer, new Color(80, 100, 180), .15f);
            lightMgmt.setReflectSpread(80);
            room.updateLightMgmt(lightMgmt);

            //4 - Creating the light UI and passing in the LightMgmt
            LightingLayerUI lightingUI = new LightingLayerUI(lightMgmt);
            lightingUI.setShowBlockers(showBlockers);
            lightingUI.setShowLights(showLights);
            lightingUI.setShowInteractable(showInteractable);
            lightingUI.setInteractables(room::getInteractables);

            //5 - Creating the graphics for the room and applying the lightUI to it.
            JPanel roomPanel = room.putToScreen();
            JLayer<JComponent> litLayer = new JLayer<>(roomPanel, lightingUI);



            MouseAdapter followMouse = new MouseAdapter() {
                private final LightPoint mouseLight = room.getLookingWall().getLightPoint("mouseLight");
                @Override
                public void mouseMoved(MouseEvent e) { moveLight(e); }

                @Override
                public void mouseDragged(MouseEvent e) { moveLight(e); }

                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!lightMgmt.getLights().contains(mouseLight)) {
                        lightMgmt.addLight(mouseLight);
                    }
                    moveLight(e);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    lightMgmt.removeLight(mouseLight); // turn the light off when the mouse leaves
                    litLayer.repaint();
                }

                private void moveLight(MouseEvent e) {
                    Point free = lightLayer.nearestFreePoint(e.getX(), e.getY(), roomPanel.getWidth(), roomPanel.getHeight());
                    if (free == null) {
                        return; // nowhere free to put it
                    }
                    mouseLight.getLoc().setX(free.getX());
                    mouseLight.getLoc().setY(free.getY());
                    litLayer.repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    for (InteractableObj obj : room.getInteractables()) {
                        Helper.Point[] corners = obj.getShapeCorners();
                        Polygon shape = new Polygon();
                        for (Helper.Point p : corners) {
                            shape.addPoint(p.getX(), p.getY());
                        }
                        if (shape.contains(e.getX(), e.getY())) {
                            obj.trigger();
                            break; // only trigger the one that was clicked
                        }
                    }
                }
            };

            roomPanel.addMouseListener(followMouse);
            roomPanel.addMouseMotionListener(followMouse);

            // Place the light at the mouse as soon as this panel shows up, even if the mouse hasn't moved
            roomPanel.addHierarchyListener(new HierarchyListener() {
                private final LightPoint mouseLight = room.getLookingWall().getLightPoint("mouseLight");
                @Override
                public void hierarchyChanged(HierarchyEvent e) {
                    if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) == 0 || !roomPanel.isShowing()) {
                        return;
                    }
                    PointerInfo pointer = MouseInfo.getPointerInfo();
                    if (pointer == null) {
                        return; // no mouse available
                    }
                    java.awt.Point mouse = pointer.getLocation();              // screen coordinates
                    SwingUtilities.convertPointFromScreen(mouse, roomPanel);   // now panel coordinates

                    if (roomPanel.contains(mouse)) {
                        mouseLight.getLoc().setX(mouse.x);
                        mouseLight.getLoc().setY(mouse.y);
                        if (!lightMgmt.getLights().contains(mouseLight)) {
                            lightMgmt.addLight(mouseLight);
                        }
                    } else {
                        lightMgmt.removeLight(mouseLight); // mouse is outside, so no light yet
                    }
                    litLayer.repaint();
                }
            });

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
        SwingUtilities.invokeLater(GraphicWindow::new);
    }
}