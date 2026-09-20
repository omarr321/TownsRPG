package roomClass.roomParts;

public class Color {
    int red;
    int green;
    int blue;

    public Color(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }
    public Color(Room_Colors roomColors) {
        this.red = roomColors.getRed();
        this.green = roomColors.getGreen();
        this.blue = roomColors.getBlue();
    }

    public void setRed(int red) {
        if (red < 0 || red > 255) {
            System.out.println("Invalid Color range for Red (0-255 included): " + red);
        } else {
            this.red = red;
        }
    }
    public void setGreen(int green) {
        if (green < 0 || green > 255) {
            System.out.println("Invalid Color range for Green (0-255 included): " + green);
        } else {
            this.green = green;
        }
    }
    public void setBlue(int blue) {
        if (blue < 0 || blue > 255) {
            System.out.println("Invalid Color range for Red (0-255 included): " + blue);
        } else {
            this.blue = blue;
        }
    }

    public int[] getColor(){
        return new int[]{red,green,blue};
    }

    public enum Room_Colors {
        NONE(0,0,0),
        RED(145,58,58),
        GREEN(62,122,50),
        YELLOW(207,184,10),
        BLUE(57,72,204),
        SKY_BLUE(107,200,207),
        WHITE(255,255,255),
        TAN(194,160,107);

        private final int red;
        private final int green;
        private final int blue;
        private Room_Colors(int red, int green, int blue) {
            this.red = red;
            this.green = green;
            this.blue = blue;
        }
        public int getRed() {
            return red;
        }
        public int getGreen() {
            return green;
        }
        public int getBlue() {
            return blue;
        }
        }
}
