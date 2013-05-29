/*
 * Copyright © 2013, Olivier MARTIN, aka ekki77
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * The Software is provided "as is", without warranty of any kind, express or implied, including but not limited to the warranties of merchantability, fitness for a particular purpose and noninfringement. In no event shall the authors or copyright holders X be liable for any claim, damages or other liability, whether in an action of contract, tort or otherwise, arising from, out of or in connection with the software or the use or other dealings in the Software.
 *
 * Except as contained in this notice, the name of the copyright holders shall not be used in advertising or otherwise to promote the sale, use or other dealings in this Software without prior written authorization from the copyright holders.
 */

package io.gameover.utilities.pixeleditor;

import io.gameover.utilities.pixeleditor.utils.ColorUtils;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

/**
 * Panel to display animation
 */
public class AnimationImagePanel extends JPanel {
    private final Pixelizer parent;
    private BufferedImage image;
    private Timer timer;
    private int index;

    public AnimationImagePanel (final Pixelizer parent, int fps) {
        this.parent = parent;
        image = new BufferedImage(40+Frame.NB_PIXELS*2, 40+Frame.NB_PIXELS*2, BufferedImage.TYPE_INT_ARGB);
        setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        timer = new Timer(1000/fps, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                index++;
                if(index>=parent.getPixelFrames().size())
                    index = 0;
                repaint();
            }
        });
        timer.start();
    }

    public void paint(Graphics g) {
        super.paint(g);

        Graphics2D g2d = (Graphics2D) g;

        Frame frame = this.parent.getPixelFrames().get(index);

        Graphics2D gi2d = (Graphics2D) image.getGraphics();
        for(int i=0; i<image.getWidth()/4+1; i++){
            for(int j=0; j<image.getHeight()/4+1; j++){
                Color c = i%2==j%2?Color.GRAY:Color.lightGray;
                gi2d.setColor(c);
                gi2d.fillRect(i*4, j*4, 4, 4);
            }
        }
        gi2d.dispose();
        for (int i = 0; i < Frame.NB_PIXELS; i++) {
            for (int j = 0; j < Frame.NB_PIXELS; j++) {
                int c = frame.getColor(i, j);
//                if(c!=NO_COLOR_AS_INT){
                    int[] rgb = ColorUtils.overlayWithColor(image.getRGB(i * 2 + 20, j * 2 + 20), c);
                    int irgb = ColorUtils.convertToColorAsInt(rgb);
                    image.setRGB(i*2 + 20, j*2 + 20, irgb);
                    image.setRGB(i*2+1 + 20, j*2 + 20, irgb);
                    image.setRGB(i*2+1 + 20, j*2+1 + 20, irgb);
                    image.setRGB(i*2 + 20, j*2+1 + 20, irgb);
                }
//            }
        }
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        Toolkit.getDefaultToolkit().sync();
    }
}
