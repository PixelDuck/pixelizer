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

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A panel to display a color selection with HSV values for Saturation and value.
 * HSV transformation is not linear from  RGB so it could be possible that the value chosen is not available in the HSV mode.
 *
 * see http://stackoverflow.com/questions/7896280/converting-from-hsv-hsb-in-java-to-rgb-without-using-java-awt-color-disallowe
 */
public class SaturationAndValueGradientPanel extends JPanel implements Observable<SaturationAndValueChangeEvent>{

    private static final int CIRCLE_RADIUS = 5;
    private static final int PANEL_SIZE = 180;
    private static final float PANEL_SIZE_AS_FLOAT = (float)PANEL_SIZE;
    private static final int MARGIN = 3;

    private final Color backgroundColor;
    private float hue;
    private float saturation;
    private float value;
    private Map<Float, BufferedImage> svImagePerHueValueMap = new HashMap<>();
    private List<Observer<SaturationAndValueChangeEvent>> observers = new ArrayList<>();
    private Color circleColor;

    public SaturationAndValueGradientPanel() {
        setPreferredSize(new Dimension
                (PANEL_SIZE + MARGIN * 2, PANEL_SIZE + MARGIN * 2));
        setDoubleBuffered(true);
        this.backgroundColor = getBackground();
        addMouseListener(new InternalMouseListener(this));
    }

    @Override
    public synchronized void addObserver(Observer<SaturationAndValueChangeEvent> o) {
        observers.add(o);
    }

    @Override
    public void removeObserver(Observer<SaturationAndValueChangeEvent> o) {
        observers.remove(o);
    }

    public float getHue(){
        return hue;
    }

    public void setHue(float hue){
        this.hue = hue;
        repaint();
    }

    public void setColor(float hue, float saturation, float value){
        setHue(hue);
        setSaturationAndValue(saturation, value);
    }

    private BufferedImage getBufferedImage(){
        float h = getHue();
        BufferedImage bi =this.svImagePerHueValueMap.get(h);
        if(bi==null){
            bi = new BufferedImage(PANEL_SIZE +1, PANEL_SIZE +1, BufferedImage.TYPE_INT_ARGB);
            int[] rgb = ColorUtils.extractRGB(ColorUtils.convertHSVToRGBAsInt(h, 0.95f, 095f));
            for(int i=0; i<= PANEL_SIZE; i++){
                for(int j=0; j<= PANEL_SIZE; j++){
                    float s = (float)i / PANEL_SIZE_AS_FLOAT;
                    float v = (float)j / PANEL_SIZE_AS_FLOAT;
                    int c = ColorUtils.convertHSVToRGBAsInt(h, s, v);

                    bi.setRGB(i, PANEL_SIZE -j, c);
                }
            }
            svImagePerHueValueMap.put(h, bi);
        }
        return bi;
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
        g2d.drawRect(MARGIN-2, MARGIN-2, PANEL_SIZE+4, PANEL_SIZE+4);
        g2d.setStroke(new BasicStroke(1f));
        g2d.setPaint(circleColor);
        int px = MARGIN+ (int) (saturation * PANEL_SIZE);
        int py = MARGIN+ (int) (PANEL_SIZE - value * PANEL_SIZE);
        g2d.drawLine(px, py, px, py);
        g2d.drawArc(
                px -CIRCLE_RADIUS,
                py -CIRCLE_RADIUS,
                CIRCLE_RADIUS*2,
                CIRCLE_RADIUS*2,
                0,
                360);
    }

    private static class InternalMouseListener extends MouseAdapter{
        private final SaturationAndValueGradientPanel parent;
        private boolean mouseEntered;

        public InternalMouseListener(SaturationAndValueGradientPanel parent){
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
                parent.updateSaturationAndValue(e.getX(), e.getY());
            }
        }
    }

    private void updateSaturationAndValue(int x, int y) {
        float fx = (float)x, fy = (float)y;
        float fm = (float)MARGIN;
        setSaturationAndValue(
                (fx-fm)/PANEL_SIZE_AS_FLOAT,
                (PANEL_SIZE_AS_FLOAT - (fy-fm))/PANEL_SIZE_AS_FLOAT);
        repaint();
        if(observers.size()>0){
            for (Observer<SaturationAndValueChangeEvent> observer : observers) {
                observer.notify(new SaturationAndValueChangeEvent(saturation, value));
            }
        }
    }

    public void setSaturationAndValue(float saturation, float value) {
        this.saturation = saturation;
        this.value = value;
        float v = 0f;
        if(value<0.5f){
            v = 1f;
        }
        this.circleColor = new Color(ColorUtils.convertHSVToRGBAsInt(0.5f, 0f, v));
        repaint();
    }
}
