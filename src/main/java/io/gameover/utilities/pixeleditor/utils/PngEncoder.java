package io.gameover.utilities.pixeleditor.utils;

import io.gameover.utilities.pixeleditor.Pixelizer;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Utilties to save an animation as a flat file.
 */
public final class PngEncoder implements Encoder{

    public PngEncoder(){

    }

    @Override
    public void saveImage(File f, List<Pixelizer.Frame> frames) throws IOException {
        int width = frames.get(0).getWidth();
        int height = frames.get(0).getHeight();
        BufferedImage bImg = new BufferedImage(width *frames.size(), height, BufferedImage.TYPE_INT_ARGB);
        for (int c = 0, nbFrames = frames.size(); c < nbFrames; c++) {
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    bImg.setRGB(i+c*nbFrames, j, frames.get(c).getColor(i, j));
                }
            }
        }
        ImageIO.write(bImg, "png", f);
    }
}

