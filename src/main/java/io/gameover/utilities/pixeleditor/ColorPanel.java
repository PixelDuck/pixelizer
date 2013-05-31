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
import java.awt.Dimension;
import java.awt.Graphics;

/**
 * A panel to display a color.
 */
public class ColorPanel extends JPanel{

    private int c;

    public ColorPanel(){
        setPreferredSize(new Dimension(10,10));
        setDoubleBuffered(true);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if(c!=Frame.NO_COLOR_AS_INT){
            g.setColor(new Color(c));
            g.fillRect(0, 0, getWidth(), getHeight());
        } else {
            g.setColor(Color.DARK_GRAY);
            g.fillRect(0, 0, getWidth()/2, getHeight()/2);
            g.fillRect(getWidth()/2, getHeight()/2, getWidth()/2, getHeight()/2);
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(0, getHeight()/2, getWidth()/2, getHeight()/2);
            g.fillRect(getWidth()/2, 0, getWidth()/2, getHeight()/2);
        }
    }

    public int getColor() {
        return c;
    }

    public void setColor(int c) {
        this.c = c;
    }
}
