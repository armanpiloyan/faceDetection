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

/**
@author armanpiloyan
 */

public class LoadHistograms extends Component {

    public static Map<String, Map<Integer, Integer>> averageHistogram = new HashMap<>();

    public double bottom(int x) {
        return 0.9848 * x - 6.7474;
    }

    public double top(int x) {
        return -0.0026 * x * x + 1.5713 * x + 14.8;
    }

    public static double calculateSD(Double numArray[]) {
        double sum = 0.0, standardDeviation = 0.0;
        int length = numArray.length;

        for (double num : numArray) {
            sum += num;
        }

        double mean = sum / length;

        for (double num : numArray) {
            standardDeviation += Math.pow(num - mean, 2);
        }

        return Math.sqrt(standardDeviation / length);
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

        Layer layer = new Layer();

        IntStream.range(1, 21).forEach((i) -> {
            try {

                BufferedImage bufferedImage = ImageIO.read(new File("/Users/armanmac/Documents/imageJ/src/main/resources/equalizedSet/" + i + ".jpg"));
                ImageProcessor image = new ColorProcessor(bufferedImage);


                ImageProcessor res = layer.run(image);
                ImageProcessor res01 = layer.run01(image);
                ImageProcessor res12 = layer.run12(image);
                ImageProcessor res23 = layer.run23(image);
                ImageProcessor res34 = layer.run34(image);

                File result = new File("/Users/armanmac/Documents/imageJ/src/main/resources/layers/" + i + ".jpg");
                File result01 = new File("/Users/armanmac/Documents/imageJ/src/main/resources/layer01/" + i + ".jpg");
                File result12 = new File("/Users/armanmac/Documents/imageJ/src/main/resources/layer12/" + i + ".jpg");
                File result23 = new File("/Users/armanmac/Documents/imageJ/src/main/resources/layer23/" + i + ".jpg");
                File result34 = new File("/Users/armanmac/Documents/imageJ/src/main/resources/layer34/" + i + ".jpg");

                ImageIO.write(res.getBufferedImage(), "jpg", result);
                ImageIO.write(res01.getBufferedImage(), "jpg", result01);
                ImageIO.write(res12.getBufferedImage(), "jpg", result12);
                ImageIO.write(res23.getBufferedImage(), "jpg", result23);
                ImageIO.write(res34.getBufferedImage(), "jpg", result34);

            } catch (IOException e) {
                e.printStackTrace();
            }
        });


        IntStream.range(1, 21).forEach((i) -> {

            BufferedImage originalBlacked = null;
            try {
                originalBlacked = ImageIO.read(new File("/Users/armanmac/Documents/imageJ/src/main/resources/faceTrain/" + i + ".jpg"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            ImageProcessor im = new ColorProcessor(originalBlacked);
            ImageProcessor originBlacked = layer.run(im);


            BufferedImage bufferedImage = null;
            try {
                bufferedImage = ImageIO.read(new File("/Users/armanmac/Documents/imageJ/src/main/resources/equalizedSet/" + i + ".jpg"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            ImageProcessor image = new ColorProcessor(bufferedImage);

            int width = image.getWidth();
            int height = image.getHeight();


            BufferedImage bufferedImageBlack = null;

            try {
                bufferedImageBlack = ImageIO.read(new File("/Users/armanmac/Documents/imageJ/src/main/resources/layers/" + i + ".jpg"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            ImageProcessor imageBlack = new ColorProcessor(bufferedImageBlack);

            int sumX = 0;
            int sumY = 0;
            int n = 0;


            List<Double> xs = new ArrayList<>();
            List<Double> ys = new ArrayList<>();

            for (int row = 0; row < height; row++) {
                for (int col = 0; col < width; col++) {
                    Color color = ((ColorProcessor) originBlacked).getColor(col, row);
                    if (color.getBlue() == 0 && color.getGreen() == 0 && color.getRed() == 0) {
                        n++;
                        sumX += col;
                        sumY += row;
                        xs.add((double) col);
                        ys.add((double) row);
                    }
                }
            }

            int centerX = sumX / n;
            int centerY = sumY / n;

            Double[] xsArr = xs.toArray(new Double[xs.size()]);
            Double[] ysArr = ys.toArray(new Double[ys.size()]);

            int SDX = (int) Math.round(calculateSD(xsArr));
            int SDY = (int) Math.round(calculateSD(ysArr));

            System.out.println("Center detected: (" + centerX + "," + centerY + ")" + " with S.D. x: " + SDX + " with S.D. y: " + SDY);

            //standard deviation can be used in order to achieve better detection
            //however I used simple formula for square capturing
            //all we need to do in order to improve the result is to multiply row values by 1/SDY and by 1/SDX for column values respectfully
            //the result is not bad but it would be better if we had ideally cropped face images,however we do not live in vacuum world, thus,
            //I think the overall result is not bad
            for (int row = 0; row < height; row++) {
                for (int col = 0; col < width; col++) {
                    int val = (Math.abs(row - col + centerX - centerY) + Math.abs(row + col - centerX - centerY));
                    if (val > 80 && val < 90) {
                        image.putPixel(col, row, 16711680);
                    }

                }
            }

            File result = new File("/Users/armanmac/Documents/imageJ/src/main/resources/noseDetection/" + i + ".jpg");
            try {
                ImageIO.write(image.getBufferedImage(), "jpg", result);
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("Nose detected!");


        });
    }
}