package io.gameover.utilities.pixeleditor.utils;

import io.gameover.utilities.pixeleditor.Frame;

import javax.imageio.ImageIO;
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
    public void saveImage(File file, List<Frame> frames) throws IOException {
        int nb = frames.size();
        int width = frames.get(0).getWidth();
        int height = frames.get(0).getHeight();
        BufferedImage bImg = new BufferedImage(width * nb, height, BufferedImage.TYPE_INT_ARGB);
        for (int c = 0; c < nb; c++) {
            Frame f = frames.get(c);
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    bImg.setRGB(i + c * width, j, f.getColor(i, j));
                }
            }
        }
        System.out.println(ImageUtils.toAlphaMask(bImg));
        ImageIO.write(bImg, "png", file);
    }
}

