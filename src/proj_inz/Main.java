package proj_inz;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.*;

public class Main extends JFrame {

    static String filePath;
    BufferedImage image;
    JLabel promptLabel;
    JTextField prompt;
    JButton promptButton;
    JFileChooser fileChooser;
    JButton loadButton;
    JButton toGrayscaleButton;
    JButton processingButton;
    JScrollPane scrollPane;
    JLabel imgLabel;

    public static int chooseMask() {  //panel wyboru opcji maski
        String[] buttons = {"3x3", "5x5", "9x9"};

        int value = JOptionPane.showOptionDialog(null, "Choose mask size", "Mask",
                JOptionPane.INFORMATION_MESSAGE, JOptionPane.QUESTION_MESSAGE, null, buttons, buttons[0]);

        switch (value) {
            case 0:
                return 3;
            case 1:
                return 5;

            case 2:
                return 9;

        }
        return -1;
    }

    public Main() {
        super("Image processing");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Container contentPane = getContentPane();
        JPanel inputPanel = new JPanel();
        promptLabel = new JLabel("Filename:");
        inputPanel.add(promptLabel);
        prompt = new JTextField(20);
        inputPanel.add(prompt);
        promptButton = new JButton("Browse");
        inputPanel.add(promptButton);
        contentPane.add(inputPanel, BorderLayout.NORTH);
        fileChooser = new JFileChooser();
        promptButton.addActionListener(
                new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int returnValue
                        = fileChooser.showOpenDialog(null);
                if (returnValue
                        == JFileChooser.APPROVE_OPTION) {
                    File selectedFile
                            = fileChooser.getSelectedFile();
                    if (selectedFile != null) {
                        prompt.setText(selectedFile.getAbsolutePath());
                    }
                }
            }
        }
        );

        imgLabel = new JLabel();
        scrollPane = new JScrollPane(imgLabel);
        scrollPane.setPreferredSize(new Dimension(700, 500));
        contentPane.add(scrollPane, BorderLayout.CENTER);

        JPanel outputPanel = new JPanel();
        loadButton = new JButton("Load");
        outputPanel.add(loadButton);
        loadButton.addActionListener(
                new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    String name = prompt.getText();
                    File file = new File(name);
                    if (file.exists()) {
                        filePath = file.getAbsolutePath();
                        image = ImageIO.read(file.toURL());
                        if (image == null) {
                            System.err.println("Invalid input file format");
                        } else {
                            imgLabel.setIcon(new ImageIcon(image));
                        }
                    } else {
                        System.err.println("Bad filename");
                    }
                } catch (MalformedURLException mur) {
                    System.err.println("Bad filename");
                } catch (IOException ioe) {
                    System.err.println("Error reading file");
                }
            }
        }
        );

        toGrayscaleButton = new JButton("Grayscale");
        outputPanel.add(toGrayscaleButton);
        toGrayscaleButton.addActionListener(
                new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                toGrayscale(image);
                imgLabel.setIcon(new ImageIcon(image));
            }
        });

        processingButton = new JButton("Processing");
        outputPanel.add(processingButton);
        processingButton.addActionListener(
                new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Processing(image);
                imgLabel.setIcon(new ImageIcon(image));
            }
        });

        contentPane.add(outputPanel, BorderLayout.SOUTH);
    }

    private static void Processing(BufferedImage img) {
      
        int w = img.getWidth(null);
        int h = img.getHeight(null);
        int maskSize = chooseMask();
      
        filter(img, maskSize, w, h);

    }

    private static void toGrayscale(BufferedImage img) {
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        ColorConvertOp op = new ColorConvertOp(cs, null);
        op.filter(img, img);

    }

    public static void filter(BufferedImage img, int maskSize, int width, int height) {

        int xMin, xMax, yMin, yMax;
        int[] a = new int[maskSize * maskSize];
        int[] red = new int[maskSize * maskSize]; //tablice w których zapisze warości pikseli
        int[] green = new int[maskSize * maskSize];
        int[] blue = new int[maskSize * maskSize];

        for (int x = 0; x < width; x++) { //przeglądanie całego obrazu
            for (int y = 0; y < height; y++) {
                int counter = 0;
                xMin = x - (maskSize / 2); // tworzę kwadracik z maski gdzie nasz piksel będzie pośrodku
                xMax = x + (maskSize / 2);
                yMin = y - (maskSize / 2);
                yMax = y + (maskSize / 2);

                int rgb;
                for (int r = yMin; r <= yMax; r++) { //przeglądanie obrazu maską
                    for (int c = xMin; c <= xMax; c++) {
                        if (r < 0 || r >= height || c < 0 || c >= width) { //pomijam gdyż chodzi o krawędzie obrazu

                            continue;
                        } else {

                            rgb = img.getRGB(c, r);
                            a[counter] = (rgb & 0xff000000) >>> 24;
                            red[counter] = (rgb >> 16) & 0xff;
                            green[counter] = (rgb >> 8) & 0xff;
                            blue[counter] = (rgb) & 0xFF;
                            counter++;
                        }
                    }

                }
                sortRGB(red, green, blue);
                int index = median(counter);
                int RGB = blue[index] | (green[index] << 8) | (red[index] << 16) | (a[index] << 24);
                img.setRGB(x, y, RGB);

            }
        }
        write(img);
    }

    public static void sortRGB(int[] red, int[] green, int[] blue) { // sortowanie wszystkich tablic z kolorami rgb
        HeapSort.sort(red);
        HeapSort.sort(green);
        HeapSort.sort(blue);

    }

    public static int median(int tabLenght) {  //metoda do uzyskania mediany wartości 

        int index;
        if (tabLenght % 2 == 0) {
            index = tabLenght / 2 - 1;
        } else {
            index = tabLenght / 2;
        }
  return index;

    }

    public static void write(BufferedImage img) //zapisywanie obrazu wynikowego 
    {

        int index = filePath.lastIndexOf('.');
        String path = filePath.substring(0, index) + "-output.png";

        File outputfile = new File(path);
        try {
            boolean b = ImageIO.write(img, "PNG", outputfile);

        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void main(String args[]) {
        JFrame frame = new Main();
        frame.pack();
        frame.show();
    }
}
