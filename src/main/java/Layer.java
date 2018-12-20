import ij.process.ImageProcessor;


import java.awt.Color;

@SuppressWarnings("Duplicates")
public class Layer {

    //0-1
    public double bottom01(int x) {
        return 0.9848 * x - 6.7474;
    }

    public double top01(int x) {
        return -0.0009 * x * x + 1.1917 * x - 4.0146;
    }

    //1-2
    public double bottom12(int x) {
        return -0.0009 * x * x + 1.1917 * x - 4.0146;
    }

    public double top12(int x) {
        return -0.0011 * x * x + 1.2262 * x + 4.0264;
    }

    //2-3
    public double bottom23(int x) {
        return -0.0011 * x * x + 1.2262 * x + 4.0264;
    }

    public double top23(int x) {
        return -0.0013 * x * x + 1.2608 * x + 12.067;
    }

    //3-4
    public double bottom34(int x) {
        return -0.0013 * x * x + 1.2608 * x + 12.067;
    }

    public double top34(int x) {
        return -0.0026 * x * x + 1.5713 * x + 14.8;
    }

    //union
    private double bottom(int x) {
        return 0.9848 * x - 6.7474;
    }

    private double top(int x) {
        return -0.0026 * x * x + 1.5713 * x + 14.8;
    }

    public ImageProcessor run(ImageProcessor input) {
        ImageProcessor ip = input.duplicate();
        int width = ip.getWidth();
        int height = ip.getHeight();
        int r, g, b;

        double rb;
        Color color;

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                color = new Color(ip.getPixel(col, row));
                r = color.getRed();
                g = color.getGreen();
                b = color.getBlue();
                rb = (r + b) / 2.;
                if (b < g && g < r && rb >= bottom(g) && rb <= top(g)) {
                    ip.putPixel(col, row, 0); //BLACK
                } else
                    ip.putPixel(col, row, 16777215); //WHITE
            }
        }
        return ip;
    }

    public ImageProcessor run01(ImageProcessor input) {
        ImageProcessor ip = input.duplicate();
        int width = ip.getWidth();
        int height = ip.getHeight();
        int r, g, b;

        double rb;
        Color color;

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                color = new Color(ip.getPixel(col, row));
                r = color.getRed();
                g = color.getGreen();
                b = color.getBlue();
                rb = (r + b) / 2.;
                if (b < g && g < r && rb >= bottom01(g) && rb <= top01(g)) {
                    ip.putPixel(col, row, 0); //BLACK
                } else
                    ip.putPixel(col, row, 16777215); //WHITE
            }
        }
        return ip;
    }

    public ImageProcessor run12(ImageProcessor input) {
        ImageProcessor ip = input.duplicate();
        int width = ip.getWidth();
        int height = ip.getHeight();
        int r, g, b;

        double rb;
        Color color;

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                color = new Color(ip.getPixel(col, row));
                r = color.getRed();
                g = color.getGreen();
                b = color.getBlue();
                rb = (r + b) / 2.;
                if (b < g && g < r && rb >= bottom12(g) && rb <= top12(g)) {
                    ip.putPixel(col, row, 0); //BLACK
                } else
                    ip.putPixel(col, row, 16777215); //WHITE
            }
        }
        return ip;
    }

    public ImageProcessor run23(ImageProcessor input) {
        ImageProcessor ip = input.duplicate();
        int width = ip.getWidth();
        int height = ip.getHeight();
        int r, g, b;

        double rb;
        Color color;

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                color = new Color(ip.getPixel(col, row));
                r = color.getRed();
                g = color.getGreen();
                b = color.getBlue();
                rb = (r + b) / 2.;
                if (b < g && g < r && rb >= bottom23(g) && rb <= top23(g)) {
                    ip.putPixel(col, row, 0); //BLACK
                } else
                    ip.putPixel(col, row, 16777215); //WHITE
            }
        }
        return ip;
    }

    public ImageProcessor run34(ImageProcessor input) {
        ImageProcessor ip = input.duplicate();
        int width = ip.getWidth();
        int height = ip.getHeight();
        int r, g, b;

        double rb;
        Color color;

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                color = new Color(ip.getPixel(col, row));
                r = color.getRed();
                g = color.getGreen();
                b = color.getBlue();
                rb = (r + b) / 2.;
                if (b < g && g < r && rb >= bottom34(g) && rb <= top34(g)) {
                    ip.putPixel(col, row, 0); //BLACK
                } else
                    ip.putPixel(col, row, 16777215); //WHITE
            }
        }
        return ip;
    }


}