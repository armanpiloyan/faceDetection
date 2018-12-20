import ij.process.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import javax.imageio.ImageIO;

public class LoadHistograms extends Component {

    public static Map<String, Map<Integer, Integer>> averageHistogram = new HashMap<>();

    public double bottom(int x) {
        return 0.9848 * x - 6.7474;
    }

    public double top(int x) {
        return -0.0026 * x * x + 1.5713 * x + 14.8;
    }

    public Map<String, Map<Integer, Integer>> generateHist(BufferedImage ip) {
        Map<String, Map<Integer, Integer>> histogram = new HashMap<>();
        int width = ip.getWidth();
        int height = ip.getHeight();
        int r, g, b;

        double rb;
        Color color;

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                color = new Color(ip.getRGB(col, row));
                r = color.getRed();
                g = color.getGreen();
                b = color.getBlue();
                rb = (r + b) / 2.;
                if (b < g && g < r && rb >= bottom(g) && rb <= top(g)) {
                    increment(histogram, "red", r);
                    increment(histogram, "green", g);
                    increment(histogram, "blue", b);
                }
            }
        }
        return histogram;
    }

    public void increment(Map<String, Map<Integer, Integer>> histogram, String color, int pixelValue) {
        if (histogram.containsKey(color))
            histogram.get(color).merge(pixelValue, 1, Integer::sum);
        else
            histogram.put(color, new HashMap<Integer, Integer>() {{
                put(pixelValue, 1);
            }});
    }


    public void loadHistograms() {
        List<Map<String, Map<Integer, Integer>>> histograms = new ArrayList<>();
        IntStream.range(1, 21).forEach(i -> {
            try {
                BufferedImage image = ImageIO.read(new File("/Users/armanmac/Documents/imageJ/src/main/resources/faceTrain/" + i + ".jpg"));
                histograms.add(generateHist(image));
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        });

        averageHistogram.put("red", new HashMap<>());
        averageHistogram.put("green", new HashMap<>());
        averageHistogram.put("blue", new HashMap<>());

        IntStream.range(0, 256).forEach(value -> {

            int avgRed = (histograms.stream().map(hist -> hist.get("red").getOrDefault(value, 0)).mapToInt(i -> i).sum()) / histograms.size();
            averageHistogram.get("red").put(value, avgRed);

            int avgGreen = (histograms.stream().map(hist -> hist.get("green").getOrDefault(value, 0)).mapToInt(i -> i).sum()) / histograms.size();
            averageHistogram.get("green").put(value, avgGreen);

            int avgBlue = (histograms.stream().map(hist -> hist.get("blue").getOrDefault(value, 0)).mapToInt(i -> i).sum()) / histograms.size();
            averageHistogram.get("blue").put(value, avgBlue);
        });

        System.out.println("Average histogram generated!");

    }

    public double getWeightedValue(int[] histogram, int i) {
        int h = histogram[i];
        return Math.sqrt((double) (h));
    }

    public void equalize(ImageProcessor ip, int[] histogram) {
        ip.resetRoi();
        int max = 255;
        int range = 255;
        double sum;
        sum = getWeightedValue(histogram, 0);
        for (int i = 1; i < max; i++)
            sum += 2 * getWeightedValue(histogram, i);
        sum += getWeightedValue(histogram, max);
        double scale = range / sum;
        int[] newIm = new int[range + 1];
        newIm[0] = 0;
        sum = getWeightedValue(histogram, 0);
        for (int i = 1; i < max; i++) {
            double delta = getWeightedValue(histogram, i);
            sum += delta;
            newIm[i] = (int) Math.round(sum * scale);
            sum += delta;
        }
        newIm[max] = max;
        ip.applyTable(newIm);
    }


    public void run(ImageProcessor imageProcessor, int index) {

        loadHistograms();

        int width = imageProcessor.getWidth();
        int height = imageProcessor.getHeight();

        int r, g, b;

        ImageProcessor redComponent = imageProcessor.duplicate().convertToByte(false);
        ImageProcessor blueComponent = imageProcessor.duplicate().convertToByte(false);
        ImageProcessor greenComponent = imageProcessor.duplicate().convertToByte(false);
        ImageProcessor equalizedImage = imageProcessor.duplicate();

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {

                Color color = new Color(imageProcessor.getPixel(col, row));

                r = color.getRed();
                g = color.getGreen();
                b = color.getBlue();

                redComponent.putPixel(col, row, r);
                blueComponent.putPixel(col, row, b);
                greenComponent.putPixel(col, row, g);
            }
        }

        equalize(redComponent, averageHistogram.get("red").values().stream().mapToInt(i -> i).toArray());
        equalize(blueComponent, averageHistogram.get("blue").values().stream().mapToInt(i -> i).toArray());
        equalize(greenComponent, averageHistogram.get("green").values().stream().mapToInt(i -> i).toArray());

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                int rgb = ((redComponent.get(col, row) & 0xff) << 16) | ((greenComponent.get(col, row) & 0xff) << 8) | blueComponent.get(col, row) & 0xff;
                equalizedImage.putPixel(col, row, rgb);
            }
        }

        File result = new File("/Users/armanmac/Documents/imageJ/src/main/resources/equalizedSet/" + index + ".jpg");

        try {
            ImageIO.write(equalizedImage.getBufferedImage(), "jpg", result);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static void main(String[] foo) {
        IntStream.range(1, 21).forEach((i) -> {
            try {
                LoadHistograms loadHistograms = new LoadHistograms();
                BufferedImage bufferedImage = ImageIO.read(new File("/Users/armanmac/Documents/imageJ/src/main/resources/faceTrain/" + i + ".jpg"));
                ImageProcessor image = new ColorProcessor(bufferedImage);
                loadHistograms.run(image, i);


            } catch (IOException e) {
                e.printStackTrace();
            }
        });


    }
}