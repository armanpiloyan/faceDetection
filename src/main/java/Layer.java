import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import ij.process.ByteProcessor;
import ij.IJ;
import ij.plugin.filter.Convolver;
import ij.plugin.filter.GaussianBlur;
import java.util.Arrays;
import java.awt.Color;


public class Layer implements PlugInFilter {

    public double bottom(int x) {
        return 0.9848 * x - 6.7474;
    }

    public double top(int x) {
        return -0.0026 * x * x + 1.5713 * x + 14.8;
    }

    public int setup(String args, ImagePlus im) {
        return DOES_RGB; // this plugin accepts 8-bit grayscale images
    }

    public void run(ImageProcessor ip) {
        int width = ip.getWidth();
        int height = ip.getHeight();
        int pixel, r, g, b;

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
                    // IJ.log(Integer.toString(row) + " " + Integer.toString(col));
                    ip.putPixel(col, row, 0); //BLACK
                }
                else
                    ip.putPixel(col, row, 16777215); //WHITE
            }
        }

    }

}