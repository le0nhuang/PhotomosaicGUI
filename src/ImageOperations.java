import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.awt.image.BufferedImage;

public class ImageOperations {
    private final List<String> mosaicImages;
    private final BufferedImage image;

    public ImageOperations(List<String> mosaicImages, BufferedImage image){
        this.mosaicImages = mosaicImages;
        this.image = image;
    }

    public BufferedImage photomosaicOperationClearer(int number) throws IOException{
        List<BufferedImage> listOfImages = convertToSquare(mosaicImages);
        Map<BufferedImage, Color> averageColor = averageColors(listOfImages);
        averageColor = scaleImages(averageColor, number);
        BufferedImage baseImage = trimImage(image, number);

        Image temp = baseImage.getScaledInstance(baseImage.getWidth() * 5, baseImage.getHeight() * 5, Image.SCALE_SMOOTH);
        baseImage = new BufferedImage(baseImage.getWidth() * 5, baseImage.getHeight() * 5, BufferedImage.TYPE_INT_ARGB);

        Graphics2D pen = baseImage.createGraphics();
        pen.drawImage(temp, 0, 0, null);
        pen.dispose();

        return mergedPhotomosaic(baseImage, averageColor, number);
    }

   public BufferedImage photomosaicOperation(int number) throws IOException {
        List<BufferedImage> listOfImages = convertToSquare(mosaicImages);
        Map<BufferedImage, Color> averageColor = averageColors(listOfImages);
        averageColor = scaleImages(averageColor, number);
        BufferedImage baseImage = trimImage(image, number);
        return mergedPhotomosaic(baseImage, averageColor, number);
   }

    /**
     * Returns the average RGB color of a BufferedImage image
     */

    private static Color averageColor(BufferedImage image) {
        long red = 0, green = 0, blue = 0;
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                Color pixel = new Color(image.getRGB(x, y));
                red = red + pixel.getRed();
                green = green + pixel.getGreen();
                blue = blue + pixel.getBlue();
            }
        }
        int numPixels = image.getWidth() * image.getHeight();
        return new Color((int) red / numPixels, (int) green / numPixels, (int) blue / numPixels);
    }

    private static BufferedImage mergedPhotomosaic(BufferedImage bi, Map<BufferedImage, Color> map, int number){
        BufferedImage canvas = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for(int width = 0; width < canvas.getWidth(); width += number){
            for(int height = 0; height < canvas.getHeight(); height += number){
                BufferedImage temp = bi.getSubimage(width, height, number, number);
                BufferedImage bestImage = bestImage(temp, map);
                for(int xx = width; xx < bestImage.getWidth() + width; xx++){
                    for(int yy = height; yy < bestImage.getHeight() + height; yy++){
                        int x = xx - width;
                        int y = yy - height;
                        canvas.setRGB(xx, yy, bestImage.getRGB(x, y));
                    }
                }
            }
        }
        return canvas;
    }

    /**
     * Returns a sub-image of BufferedImage bi with width and height divisible by 10.
     */

    private static BufferedImage trimImage(BufferedImage bi, int number){
        int height = bi.getHeight() - bi.getHeight() % number;
        int width = bi.getWidth() - bi.getWidth() % number;
        return bi.getSubimage(0, 0, width, height);
    }

    /**
     * Returns a map mapping a BufferedImage to its average color
     * Requires list is a list of square BufferedImages
     */

    private static Map<BufferedImage, Color> averageColors(List<BufferedImage> list){
        Map<BufferedImage , Color> x = new HashMap<>();
        for(BufferedImage bi : list){
            x.put(bi, averageColor(bi));
        }
        return x;
    }

    /**
     * Returns a list of square BufferedImages
     * Requires: list is a list of valid file paths to images
     */
    private static List<BufferedImage> convertToSquare(List<String> list) throws IOException {
        List<BufferedImage> x = new ArrayList<>();
        for(String s : list){
            File file = new File(s);
            BufferedImage image = ImageIO.read(file);
            if(image.getWidth() != image.getHeight()){
                if(image.getWidth() < image.getHeight()){
                    image = image.getSubimage(0, 0, image.getWidth(), image.getWidth());
                }
                else{
                    image = image.getSubimage(0, 0, image.getHeight(), image.getHeight());
                }
            }
            x.add(image);
        }
        return x;
    }

    //TODO: Rewrite Spec

    /**
     * Calculates the difference between Colors a and b by taking the
     * sum of the absolute values in difference between RGB value.
     */

    private static int colorDifference(Color a, Color b){
        int difference = 0;
        difference = difference + Math.abs(a.getRed() - b.getRed());
        difference = difference + Math.abs(a.getBlue() - b.getBlue());
        difference = difference + Math.abs(a.getGreen() - b.getGreen());
        return difference;
    }

    /**
     * Returns the square image in the Map that is closest in average color value to
     * BufferedImage bi
     */

    private static BufferedImage bestImage(BufferedImage bi, Map<BufferedImage, Color> map){
        assert !map.isEmpty();
        Color best = averageColor(bi);
        int diff = Integer.MAX_VALUE;
        BufferedImage temp = null; 
        for(BufferedImage image : map.keySet()){
            int colorDiff = colorDifference(map.get(image), best);
            if(colorDiff < diff){
                diff = colorDiff; 
                temp = image; 
            }
        }
        return temp;
    }

    /**
     * Scales all the images in map to have height and width "factor"
     *
     * Requires: all the BufferedImages in map are square.
     */

    private static Map<BufferedImage, Color> scaleImages(Map<BufferedImage, Color> map, int factor) {
        Map<BufferedImage, Color> newMap = new HashMap<>();
        for(BufferedImage bi : map.keySet()){
            BufferedImage newIm = resize(bi, factor);
            Color color = map.get(bi);
            newMap.put(newIm, color);
        }
        return newMap;
    }

    /**
     * Helper method for scaleImages method.
     */

    private static BufferedImage resize(BufferedImage bi, int factor){
        Image temp = bi.getScaledInstance(factor, factor, Image.SCALE_SMOOTH);
        BufferedImage newImage = new BufferedImage(factor, factor, BufferedImage.TYPE_INT_ARGB);

        Graphics2D pen = newImage.createGraphics();
        pen.drawImage(temp, 0, 0, null);
        pen.dispose();

        return newImage;
    }

}