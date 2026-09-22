package GraphicClasses.CustomPanels;

import GraphicClasses.Point;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * This class draws a 4 point shape to the screen using pixel points. This can be shaded in or filled with an image.
 */
public class QuadrilateralPanel extends JPanel {
    private Point[] points;
    private Color borderColor;
    private Color fillColor;
    private int thickness;

    private String imagePath = "";
    private boolean usesImage = false;
    private final String DEFAULT_IMAGE = "/images/DebugImage.png";
    private boolean imageWarp = true;

    public QuadrilateralPanel(Point[] points, Color fillColor) {
        this.points = points;
        this.fillColor = fillColor;
        this.borderColor = null;
        this.thickness = 0;

        this.setBounds(getSmallestX(),getSmallestY(), getLargestX()-getSmallestX(), getLargestY()-getSmallestY());
        this.setOpaque(false);
    }

    public QuadrilateralPanel(Point[] points, Color fillColor, Color borderColor) {
        this(points, fillColor, borderColor, 1);
    }

    public QuadrilateralPanel(Point[] points, Color fillColor, Color borderColor, int thickness) {
        this(points, fillColor);
        this.borderColor = borderColor;
        this.thickness = thickness;

        // Pad the bounds so the outside half of the border stroke isn't clipped
        int newX = getSmallestX() - thickness;
        int newY = getSmallestY() - thickness;
        int newW = (getLargestX() - getSmallestX()) + (thickness * 2);
        int newH = (getLargestY() - getSmallestY()) + (thickness * 2);

        this.setBounds(newX, newY, newW, newH);
    }

    public QuadrilateralPanel(Point[] points, String imagePath) {
        this.points = points;
        this.imagePath = imagePath;
        this.usesImage = true;

        this.setBounds(getSmallestX(),getSmallestY(), getLargestX()-getSmallestX(), getLargestY()-getSmallestY());
        this.setOpaque(false);
    }

    public QuadrilateralPanel(Point[] points, String imagePath, Color borderColor) {
        this(points, imagePath, borderColor, 1);
    }

    public QuadrilateralPanel(Point[] points, String imagePath, Color borderColor, int thickness) {
        this(points, imagePath);
        this.borderColor = borderColor;
        this.thickness = thickness;

        // Pad the bounds so the outside half of the border stroke isn't clipped
        int newX = getSmallestX() - thickness;
        int newY = getSmallestY() - thickness;
        int newW = (getLargestX() - getSmallestX()) + (thickness * 2);
        int newH = (getLargestY() - getSmallestY()) + (thickness * 2);

        this.setBounds(newX, newY, newW, newH);
    }

    public void setImageWarp(boolean val) {
        this.imageWarp = val;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D customGraphic = (Graphics2D) g;
        customGraphic.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        customGraphic.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        customGraphic.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);


        int panelW = getWidth();
        int panelH = getHeight();
        int panelX = getSmallestX() - thickness;
        int panelY = getSmallestY() - thickness;

        int[] localX = new int[4];
        int[] localY = new int[4];
        for (int i = 0; i < 4; i++) {
            // Shifting by panelX and panelY moves the shape inward,
            // leaving a "padding" border of empty space around the entire polygon!
            localX[i] = points[i].getX() - panelX;
            localY[i] = points[i].getY() - panelY;
        }
        Polygon quadShape = new Polygon(localX, localY, 4);

        if (this.usesImage && this.imageWarp) {
            Image currImage;
            java.net.URL userImg = this.getClass().getResource(imagePath);
            if (userImg != null) {
                currImage = new ImageIcon(userImg).getImage();
            } else {
                System.err.println("Image not found! Defaulting to debug image.");
                userImg = this.getClass().getResource(DEFAULT_IMAGE);
                if (userImg == null) {
                    System.err.println("Default image not found!");
                    System.exit(1);
                }
                currImage = new ImageIcon(userImg).getImage();
            }

            ImageIcon icon = new ImageIcon(currImage);
            int imgW = icon.getIconWidth();
            int imgH = icon.getIconHeight();

            if (imgW <= 0 || imgH <= 0) return;

            BufferedImage srcBuff = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D srcGraphics = srcBuff.createGraphics();
            srcGraphics.drawImage(currImage, 0, 0, null);
            srcGraphics.dispose();

            BufferedImage outputBuff = new BufferedImage(panelW, panelH, BufferedImage.TYPE_INT_ARGB);

            double[] matrix = computePerspectiveMatrix(imgW, imgH, localX, localY);

            if (matrix != null) {
                double m00 = matrix[0], m01 = matrix[1], m02 = matrix[2];
                double m10 = matrix[3], m11 = matrix[4], m12 = matrix[5];
                double m20 = matrix[6], m21 = matrix[7], m22 = matrix[8];

                for (int y = 0; y < panelH; y++) {
                    for (int x = 0; x < panelW; x++) {
                        if (quadShape.contains(x, y)) {
                            double denominator = m20 * x + m21 * y + m22;
                            if (Math.abs(denominator) > 1e-10) {
                                double srcX = (m00 * x + m01 * y + m02) / denominator;
                                double srcY = (m10 * x + m11 * y + m12) / denominator;

                                if (srcX >= 0 && srcX < imgW && srcY >= 0 && srcY < imgH) {
                                    int pixelColor = srcBuff.getRGB((int) srcX, (int) srcY);
                                    outputBuff.setRGB(x, y, pixelColor);
                                }
                            }
                        }
                    }
                }
            }
            customGraphic.drawImage(outputBuff, 0, 0, null);

        } else if (this.usesImage && !this.imageWarp) {
            Image currImage;
            java.net.URL userImg = this.getClass().getResource(imagePath);
            if (userImg != null) {
                currImage = new ImageIcon(userImg).getImage();
            } else {
                System.err.println("Image not found! Defaulting to debug image.");
                userImg = this.getClass().getResource(DEFAULT_IMAGE);
                if (userImg == null) {
                    System.err.println("Default image not found!");
                    System.exit(1);
                }
                currImage = new ImageIcon(userImg).getImage();
            }

            customGraphic.setClip(quadShape);

            customGraphic.drawImage(currImage, 0, 0, panelW, panelH, null);

            customGraphic.setClip(null);
        } else {
            customGraphic.setColor(this.fillColor);
            customGraphic.fillPolygon(quadShape);
        }

        if (thickness > 0 && borderColor != null) {
            customGraphic.setColor(this.borderColor);
            customGraphic.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            customGraphic.drawPolygon(quadShape);
        }
    }

    private int getSmallestX() {
        int smallX = Integer.MAX_VALUE;
        for (Point p : points) {
            if (smallX > p.getX()) {
                smallX = p.getX();
            }
        }

        return smallX;
    }

    private int getSmallestY() {
        int smallY = Integer.MAX_VALUE;
        for (Point p : points) {
            if (smallY > p.getY()) {
                smallY = p.getY();
            }
        }

        return smallY;
    }

    private int getLargestX() {
        int largeX = 0;
        for (Point p : points) {
            if (largeX < p.getX()) {
                largeX = p.getX();
            }
        }
        return largeX;
    }

    private int getLargestY() {
        int largeY = 0;
        for (Point p : points) {
            if (largeY < p.getY()) {
                largeY = p.getY();
            }
        }
        return largeY;
    }

    private double[] computePerspectiveMatrix(double w, double h, int[] lx, int[] ly) {
        // Source standard texture anchor maps
        double u0 = 0,   v0 = 0;
        double u1 = w-1, v1 = 0;
        double u2 = w-1, v2 = h-1;
        double u3 = 0,   v3 = h-1;

        // Destination target canvas panel paths
        double x0 = lx[0], y0 = ly[0];
        double x1 = lx[1], y1 = ly[1];
        double x2 = lx[2], y2 = ly[2];
        double x3 = lx[3], y3 = ly[3];

        // System matrix equations array allocation setup
        double[][] A = {
                {x0, y0, 1,  0,  0, 0, -x0*u0, -y0*u0, u0},
                { 0,  0, 0, x0, y0, 1, -x0*v0, -y0*v0, v0},
                {x1, y1, 1,  0,  0, 0, -x1*u1, -y1*u1, u1},
                { 0,  0, 0, x1, y1, 1, -x1*v1, -y1*v1, v1},
                {x2, y2, 1,  0,  0, 0, -x2*u2, -y2*u2, u2},
                { 0,  0, 0, x2, y2, 1, -x2*v2, -y2*v2, v2},
                {x3, y3, 1,  0,  0, 0, -x3*u3, -y3*u3, u3},
                { 0,  0, 0, x3, y3, 1, -x3*v3, -y3*v3, v3}
        };

        // Standard Gaussian Elimination implementation to solve for matrix transformation steps
        int n = 8;
        for (int i = 0; i < n; i++) {
            int maxRow = i;
            for (int k = i + 1; k < n; k++) {
                if (Math.abs(A[k][i]) > Math.abs(A[maxRow][i])) {
                    maxRow = k;
                }
            }
            double[] temp = A[i];
            A[i] = A[maxRow];
            A[maxRow] = temp;

            if (Math.abs(A[i][i]) < 1e-10) return null; // Singular matrix safety drop

            for (int k = i + 1; k < n; k++) {
                double factor = A[k][i] / A[i][i];
                for (int j = i; j <= n; j++) {
                    A[k][j] -= factor * A[i][j];
                }
            }
        }

        double[] res = new double[8];
        for (int i = n - 1; i >= 0; i--) {
            double sum = 0;
            for (int j = i + 1; j < n; j++) {
                sum += A[i][j] * res[j];
            }
            res[i] = (A[i][n] - sum) / A[i][i];
        }

        // Return full 3x3 projective values mapping layout
        return new double[]{res[0], res[1], res[2], res[3], res[4], res[5], res[6], res[7], 1.0};
    }
}