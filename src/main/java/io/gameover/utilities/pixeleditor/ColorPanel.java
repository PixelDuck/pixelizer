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
import javax.swing.SwingUtilities;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.lang.management.ManagementFactory;

/**
 * A panel to display a color.
 */
public class ColorPanel extends JPanel{

    private final static String INFO = "<html>Color: %s<br>Count: %d<br>Left click to select color<br>Right click to replace color</html>";
    public static final String ALPHA = "ALPHA";
    public static final String MORE_TAHN_1K = "1k";

    private int c;
    private int count;
    private final static int Y=15;


    public ColorPanel(){
        setPreferredSize(new Dimension(16,16));
        setDoubleBuffered(true);
        setToolTipText("");
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        Font f = g.getFont().deriveFont(c>999?8f:10f);
        FontMetrics fontMetrics = getFontMetrics(f);
        String lbl = MORE_TAHN_1K;
        if(count<1000){
            lbl = ""+count;
        }
        int w = fontMetrics.stringWidth(lbl);
        int x = (16-w)/2-1;
        if(x<0)
            x = 0;
        if(c!=Frame.NO_COLOR_AS_INT){
            g.setColor(new Color(c));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(ColorUtils.convertRGBToHSV(ColorUtils.extractRGB(c))[2] < 0.5f ? Color.white : Color.black);
        } else {
            g.setColor(Color.DARK_GRAY);
            g.fillRect(0, 0, getWidth() / 2, getHeight() / 2);
            g.fillRect(getWidth()/2, getHeight()/2, getWidth()/2, getHeight()/2);
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(0, getHeight() / 2, getWidth() / 2, getHeight() / 2);
            g.fillRect(getWidth()/2, 0, getWidth()/2, getHeight()/2);
            g.setColor(Color.white);
        }
            g2d.drawString(lbl, x, Y);
        g2d.dispose();
        Toolkit.getDefaultToolkit().sync();
    }

    public int getColor() {
        return c;
    }

    public void setColorAndCount(int c, int count) {
        this.count = count;
        this.c = c;
        String value = ALPHA;
        if(c!=Frame.NO_COLOR_AS_INT){
            String hexa = Integer.toHexString(c);
            value = "#"+hexa.substring(hexa.length()-6);
        }
        setToolTipText(String.format(INFO, value, count));
    }
}
