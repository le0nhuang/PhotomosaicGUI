import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

import java.awt.*;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Main {

    public static void main(String[] args) {
        // Creation of window must occur on Event Dispatch Thread.
        SwingUtilities.invokeLater(() -> createAndShowGUI());
    }

    private static void createAndShowGUI() {
        // Create frame.

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        JFrame frame = new JFrame("Photomosaic Creator");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setMaximumSize(screenSize);

        JLabel center = new JLabel();
        frame.add(center, BorderLayout.CENTER);

        //Button that does shit
        JButton button = new JButton("Create Photomosaic");
        frame.add(button, BorderLayout.SOUTH);

        JLabel baseIm = new JLabel();
        baseIm.setName("Invalid Upload Image");
        Dimension baseImDim = new Dimension(frame.getWidth(), frame.getHeight());
        baseIm.setMaximumSize(baseImDim);
        frame.add(baseIm);

        JButton baseButton = new JButton("Upload Base Image");
        JFileChooser imagesOnly = new JFileChooser();
        baseButton.addActionListener(e -> uploadProcedure(frame, imagesOnly, baseIm));

        JButton sourceIms = new JButton("Upload Mosaic Images");
        JFileChooser zipOnly = new JFileChooser();
        JLabel zip = new JLabel();
        zip.setName("Invalid Upload Zip");
        frame.add(zip, BorderLayout.LINE_END);
        sourceIms.addActionListener(e -> uploadZipProcedure(frame, zipOnly, zip));
        button.addActionListener(e ->
            {
                if(baseIm.getName().equals("Invalid Upload Image") || zip.getName().equals("Invalid Upload Zip")){
                    baseIm.setText("             INVALID INPUTS, PLEASE TRY AGAIN");
                }
                else{
                    Image mosaic = createPhotomosaic(zip, baseIm, 10);
                    try {
                        Image im = scaledInstance(mosaic, frame, baseIm);
                        baseIm.setIcon(new ImageIcon(im));
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        );

        JButton save = new JButton("Save Image");
        save.addActionListener(e -> {
            try {
                saveImage(createPhotomosaicClearer(zip, baseIm, 50), baseIm);
            } catch (IOException p) {
                System.out.println("exception");
            }
        });

        JPanel controls = new JPanel();
        controls.add(baseButton);
        controls.add(sourceIms);
        controls.add(save);
        frame.add(controls, BorderLayout.NORTH);

        frame.pack();
        frame.setSize(600, 400);
        frame.setVisible(true);

    }

    private static void saveImage(BufferedImage image, JLabel label) throws IOException {
        try {
            // retrieve image
            String pathname = label.getName().substring(0, label.getName().lastIndexOf('.')) + "_photomosaic" + ".png";
            BufferedImage bi = image;
            File outputfile = new File(pathname);
            ImageIO.write(bi, "png", outputfile);
        } catch (IOException e) {}
    }

    /**
     * Displays the completed Photomosaic on the GUI.
     */

    private static ImageIcon photomosaicOperation(JLabel zipLabel, JLabel baseIm, int number){
        BufferedImage image = createPhotomosaic(zipLabel, baseIm, number);
        ImageIcon icon = new ImageIcon(image);
        return icon;
    }

    /**
     * Converts the base image into a photomosaic with the images from the uploaded zip file.
     * This image will be clearer than the displayed image, as it will be scaled larger.
     */

    private static BufferedImage createPhotomosaicClearer(JLabel zipLabel, JLabel baseIm, int number){
        try{
            File zipFile = new File(zipLabel.getName());
            FileUnzip unzipper = new FileUnzip(zipFile);
            File base = new File(baseIm.getName());
            BufferedImage baseImage = ImageIO.read(base);
            List<String> unzipped = unzipper.unzip();
            ImageOperations operator = new ImageOperations(unzipped, baseImage);
            return operator.photomosaicOperationClearer(number);
        }
        catch(IOException e){
            return null;
        }
    }

    /**
     * Converts the base image into a photomosaic with the images from the uploaded zip file
     */

    private static BufferedImage createPhotomosaic(JLabel zipLabel, JLabel baseIm, int number){
        try{
            File zipFile = new File(zipLabel.getName());
            FileUnzip unzipper = new FileUnzip(zipFile);
            File base = new File(baseIm.getName());
            BufferedImage baseImage = ImageIO.read(base);
            List<String> unzipped = unzipper.unzip();
            ImageOperations operator = new ImageOperations(unzipped, baseImage);
            return operator.photomosaicOperation(number);
        }
        catch(IOException e){
            return null;
        }
    }

    /**
     * Reformats the frame after a zip file is uploaded.
     */

    private static void uploadZipProcedure(JFrame frame, JFileChooser zipOnly, JLabel zip) {
        mosaicWindow(zipOnly, zip);
        frame.pack();
        if(frame.getWidth() < 600){
            frame.setSize(600, frame.getHeight());
        }
        if(frame.getHeight() < 400){
            frame.setSize(frame.getWidth(), 400);
        }
    }

    /**
     * Reformats the frame after a base image is uploaded.
     */

    private static void uploadProcedure(JFrame frame, JFileChooser imagesOnly, JLabel baseIm){
        uploadWindow(imagesOnly, baseIm, frame);
        frame.pack();
        if(frame.getWidth() < 600){
            frame.setSize(600, frame.getHeight());
        }
        if(frame.getHeight() < 400){
            frame.setSize(frame.getWidth(), 400);
        }

    }

    /**
     * Creates a new popup window for the user to upload a base image
     */

    private static void uploadWindow(JFileChooser imFilter, JLabel label, JFrame frame) {
        JFrame baseFrame = new JFrame();
        baseFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        imFilter.setDialogTitle("Base Image Upload");
        //ImageFilter imageFilter = new ImageFilter("Images", ImageIO.getReaderFileSuffixes());
        FileFilter imageFilter = new FileNameExtensionFilter("Images Only", ImageIO.getReaderFileSuffixes());
        imFilter.setVisible(true);

        int result = imFilter.showSaveDialog(baseFrame);
        if(result == JFileChooser.APPROVE_OPTION){
            if(imageFilter.accept(imFilter.getSelectedFile())){
                label.removeAll();
                label.setText("");
                String fileName = imFilter.getSelectedFile().getAbsolutePath();
                label.setName(fileName);
                try {
                    Image im = ImageIO.read(new File(fileName));
                    Image newIm = scaledInstance(im, frame, label);
                    label.setIcon(new ImageIcon(newIm));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            else{
                label.removeAll();
                label.setIcon(new ImageIcon());
                label.setText("File type not accepted, please try again");
            }

        }
        else if (result == JFileChooser.CANCEL_OPTION) {
            baseFrame.dispatchEvent(new WindowEvent(baseFrame, WindowEvent.WINDOW_CLOSING));
        }
    }

    private static Image scaledInstance(Image im, JFrame frame, JLabel label) throws IOException {

        ImageIcon icon = new ImageIcon(im);
        label.setIcon(icon);
        frame.pack();
        if(im.getWidth(frame) > frame.getMaximumSize().getWidth()){
            if(im.getHeight(frame) > frame.getMaximumSize().getHeight()){
                double factor = im.getHeight(frame) < im.getWidth(frame) ? frame.getMaximumSize().getWidth()
                        / im.getWidth(frame) : frame.getMaximumSize().getHeight() / im.getHeight(frame);
                double newWidth = factor * im.getWidth(frame);
                double newHeight = factor * im.getHeight(frame);
                im = im.getScaledInstance((int) newWidth, (int) newHeight, Image.SCALE_DEFAULT);
            }
        }
        else if(im.getHeight(frame) > frame.getMaximumSize().getHeight()){
            double factor = (double) im.getHeight(frame) / frame.getMaximumSize().getHeight();
            double newWidth = factor * im.getWidth(frame);
            double newHeight = factor * im.getHeight(frame);
            im = im.getScaledInstance((int) newWidth, (int) newHeight, Image.SCALE_DEFAULT);
        }
        return im;
    }

    /**
     * Creates a pop up window for the user to upload a zip file of mosaic images
     */


    private static void mosaicWindow(JFileChooser zipOnly, JLabel label){
        JFrame mosFrame = new JFrame();
        mosFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        zipOnly.setDialogTitle("Mosaic Images Select");
        FileFilter zipFilter = new FileNameExtensionFilter("Zip Files",
                "zip");
        zipOnly.setFileFilter(zipFilter);
        zipOnly.setAcceptAllFileFilterUsed(false);
        zipOnly.setVisible(true);

        int result = zipOnly.showSaveDialog(mosFrame);
        if (result == JFileChooser.APPROVE_OPTION) {
            if(zipFilter.accept(zipOnly.getSelectedFile())){
                label.setText("    Mosaic Images: " + zipOnly.getSelectedFile().getName() + "    ");
                label.setName(zipOnly.getSelectedFile().getAbsolutePath());
            }
            else{
                label.setText("    File type not accepted    ");
            }
        }

        else if (result == JFileChooser.CANCEL_OPTION) {
            mosFrame.dispatchEvent(new WindowEvent(mosFrame, WindowEvent.WINDOW_CLOSING));
        }

    }

}