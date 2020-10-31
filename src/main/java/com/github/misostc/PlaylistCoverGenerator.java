package com.github.misostc;

import org.jdesktop.swingx.graphics.BlendComposite;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.Random;

public class PlaylistCoverGenerator {

    public static final int SIZE = 600;

    public byte[] getImageJPG(long seed) {
        Random random = new Random(seed);
        BufferedImage bufferedImage = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bufferedImage.createGraphics();

        createGradient(random, g2d);

        try {
            g2d.setComposite(BlendComposite.Overlay);
            BufferedImage newImage = loadImageResource("logo-overlay-grey.png");
            // draw 2 times for better effect
            g2d.drawImage(newImage, 0, 0, null);
            g2d.drawImage(newImage, 0, 0, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return toJpegBytes(bufferedImage);
    }

    private BufferedImage loadImageResource(String fileNameOnClasspath) throws IOException {
        URL input = Objects.requireNonNull(getClass().getClassLoader().getResource(fileNameOnClasspath));
        BufferedImage originalImage = ImageIO.read(input);

        // needed to convert to compatible color model
        BufferedImage newImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < originalImage.getWidth(); x++) {
            for (int y = 0; y < originalImage.getHeight(); y++) {
                newImage.setRGB(x, y, originalImage.getRGB(x, y));
            }
        }
        return newImage;
    }

    private void createGradient(Random random, Graphics2D g2d) {
        float baseHue = random.nextFloat();
        Color baseColor = Color.getHSBColor(baseHue, 1.0f, 0.7f);
        Color complementaryColor = Color.getHSBColor(shiftHue(baseHue, random.nextFloat()), 1.0f, 0.7f);
        GradientPaint twoColorGradient = new GradientPaint(SIZE, 0f, baseColor, 0, SIZE, complementaryColor);
        g2d.setPaint(twoColorGradient);
        g2d.fillRect(0, 0, SIZE, SIZE);
    }

    private byte[] toJpegBytes(BufferedImage bufferedImage) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            JPEGImageWriteParam jpegParams = new JPEGImageWriteParam(null);
            jpegParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            jpegParams.setCompressionQuality(1f);
            final ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
            writer.setOutput(new MemoryCacheImageOutputStream(byteArrayOutputStream));
            writer.write(null, new IIOImage(bufferedImage, null, null), jpegParams);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return byteArrayOutputStream.toByteArray();
    }

    private float shiftHue(float baseHue, float shift) {
        float result = baseHue + shift;
        if (result > 1.0f) {
            result -= 1.0f;
        }
        return result;
    }
}
