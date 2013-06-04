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

package io.gameover.utilities.pixeleditor.colorchooser;

import io.gameover.utilities.pixeleditor.utils.ColorUtils;
import io.gameover.utilities.pixeleditor.utils.Observable;
import io.gameover.utilities.pixeleditor.utils.Observer;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * A panel to display a color selection with HSV values for Hue.
 * HSV transformation is not linear from  RGB so it could be possible that the value chosen is not available in the HSV mode.
 *
 * see http://stackoverflow.com/questions/7896280/converting-from-hsv-hsb-in-java-to-rgb-without-using-java-awt-color-disallowe
 */
public class HueGradientPanel extends JPanel implements Observable<HueChangeEvent>{

    private static final float SATURATION = 0.95f;
    private static final float VALUE = 0.95f;
    private static final int MARGIN = 3;
    private static final int HEIGHT = 180;
    private static final float HEIGHT_AS_FLOAT = (float)HEIGHT;
    private static final int WIDTH = 20;
    private static final Color backgroundColor = new JPanel().getBackground();

    private float hue;
    private BufferedImage gradientImage;
    private List<Observer<HueChangeEvent>> observers = new ArrayList<>();

    public HueGradientPanel() {
        setPreferredSize(new Dimension
                (WIDTH + 5 * 2, HEIGHT + 5 * 2));
        setDoubleBuffered(true);
        addMouseListener(new InternalMouseListener(this));
    }

    public float getHue(){
        return hue;
    }

    public void setHue(float hue){
        this.hue = hue;
        repaint();
    }

    private BufferedImage getBufferedImage(){
        if(gradientImage==null){
            gradientImage = new BufferedImage(WIDTH, HEIGHT+1, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = (Graphics2D)gradientImage.getGraphics();
            for(int j=0; j<=HEIGHT; j++){
                float fj = j;
                g2d.setPaint(new Color(ColorUtils.convertHSVToRGBAsInt((fj/HEIGHT) % 1.0f, SATURATION, VALUE)));
                g2d.drawLine(0, j, WIDTH, j);
            }
            g2d.dispose();
        }
        return gradientImage;
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setPaint(backgroundColor);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.drawImage(getBufferedImage(), MARGIN, MARGIN, null);
        g2d.setPaint(Color.BLACK);
        g2d.drawRect(MARGIN-2, MARGIN-2, WIDTH+3 , HEIGHT+4);
        int ARROW_WIDTH = 10;
        int ax = MARGIN+WIDTH+2;
        int ay = (int) (MARGIN + (hue * HEIGHT));
        g2d.fillPolygon(new int[]{ax, ax+ARROW_WIDTH, ax+ARROW_WIDTH},  new int[]{ay, ay-ARROW_WIDTH*2/3, ay+ARROW_WIDTH*2/3}, 3);
    }

    public static void main(String[] args){
        JFrame f = new JFrame();
        f.setContentPane(new HueGradientPanel());
        f.setSize(new Dimension(WIDTH + (5 * 2), HEIGHT + (5 * 2)));
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.setVisible(true);
    }

    @Override
    public void addObserver(Observer<HueChangeEvent> o) {
        this.observers.add(o);
    }

    @Override
    public void removeObserver(Observer<HueChangeEvent> o) {
        this.observers.remove(o);
    }

    private static class InternalMouseListener extends MouseAdapter {
        private final HueGradientPanel parent;
        private boolean mouseEntered;

        public InternalMouseListener(HueGradientPanel parent){
            this.parent = parent;
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            this.mouseEntered = true;
        }

        @Override
        public void mouseExited(MouseEvent e) {
            this.mouseEntered = false;
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if(mouseEntered){
                parent.updateHue(e.getY());
            }
        }
    }

    private void updateHue(float y) {
        float h = (y-MARGIN)/HEIGHT_AS_FLOAT;
        this.hue = h;
        repaint();

        int rgb = ColorUtils.convertHSVToRGBAsInt(hue, SATURATION, VALUE);
        float[] hsv = ColorUtils.convertRGBToHSV(ColorUtils.extractRGB(rgb));

        if(observers.size()>0){
            for (Observer<HueChangeEvent> observer : observers) {
                observer.notify(new HueChangeEvent(h));
            }
        }
    }

}
