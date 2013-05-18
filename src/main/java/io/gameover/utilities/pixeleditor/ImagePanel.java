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

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;

/**
 * Main panel to display image.
 */
public class ImagePanel extends JPanel {

    private Pixelizer parent;

    public ImagePanel(Pixelizer parent) {
        this.parent = parent;
        setDoubleBuffered(true);
        setSize(this.parent.getImageWidth(), this.parent.getImageHeight());
    }

    protected Pixelizer.Frame getFrame(){
        return parent.getCurrentFrame();
    }

    public void paint(Graphics g) {
        super.paint(g);

        Graphics2D g2d = (Graphics2D) g;
        int w = this.parent.getImageWidth();
        int h = this.parent.getImageHeight();

        for (int i = 0; i < parent.getNbPixels(); i++) {
            g2d.setPaint(Color.black);
            g2d.fillRect((i + 1) * (parent.getPixelSize() + parent.getMargin()), 0, parent.getMargin(), h);
            for (int j = 0; j < parent.getNbPixels(); j++) {
                int c = getFrame().getColor(i, j);
                g2d.setPaint(ColorUtils.overlayWithColor(Color.GRAY, c));
                g2d.fillRect(parent.getMargin() + i * (parent.getPixelSize() + parent.getMargin()), parent.getMargin()
                        + j * (parent.getPixelSize() + parent.getMargin()), parent.getPixelSize() / 2,
                        parent.getPixelSize() / 2);
                g2d.fillRect(parent.getMargin() + i * (parent.getPixelSize() + parent.getMargin())
                        + parent.getPixelSize() / 2, parent.getMargin() + j
                        * (parent.getPixelSize() + parent.getMargin()) + parent.getPixelSize() / 2,
                        parent.getPixelSize() / 2, parent.getPixelSize() / 2);
                g2d.setPaint(ColorUtils.overlayWithColor(Color.LIGHT_GRAY, c));
                g2d.fillRect(parent.getMargin() + i * (parent.getPixelSize() + parent.getMargin())
                        + parent.getPixelSize() / 2, parent.getMargin() + j
                        * (parent.getPixelSize() + parent.getMargin()), parent.getPixelSize() / 2,
                        parent.getPixelSize() / 2);
                g2d.fillRect(parent.getMargin() + i * (parent.getPixelSize() + parent.getMargin()), parent.getMargin()
                        + j * (parent.getPixelSize() + parent.getMargin()) + parent.getPixelSize() / 2,
                        parent.getPixelSize() / 2, parent.getPixelSize() / 2);
                g2d.setPaint(Color.black);
                g2d.fillRect(0, (j + 1) * (parent.getPixelSize() + parent.getMargin()), w,
                        parent.getMargin());
            }
        }
        g2d.setPaint(Color.black);
        g2d.fillRect(0, 0, w, parent.getMargin());
        g2d.fillRect(0, 0, parent.getMargin(), h);
        g2d.dispose();
        Toolkit.getDefaultToolkit().sync();
    }

}
