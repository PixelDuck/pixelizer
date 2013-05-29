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
import io.gameover.utilities.pixeleditor.utils.LayoutUtils;
import io.gameover.utilities.pixeleditor.utils.Observer;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * Panel to select a color from an HSV color gradients
 */
public class ColorChooserPanel extends JPanel {

    private final SaturationAndValueGradientPanel svPanel;
    private final HueGradientPanel hPanel;
    private float hue;
    private float saturation;
    private float value;

    public ColorChooserPanel(){
        super(new GridBagLayout());
        Insets i = new Insets(0,0,0,0);
        this.svPanel = new SaturationAndValueGradientPanel();
        this.hPanel = new HueGradientPanel();
        add(svPanel, LayoutUtils.xyi(1, 1, 0d, 0d, new Insets(0,0,0,2)));
        add(hPanel, LayoutUtils.xyi(2, 1, 0d, 0d, new Insets(0,2,0,0)));
        this.svPanel.addObserver(new InternalSaturationAndValueObserver(this));
        this.hPanel.addObserver(new InternalHueObserver(this));
        setColor(255, 0, 0);
    }

    public static void main(String[] args){
        JFrame f = new JFrame();
        f.setContentPane(new ColorChooserPanel());
        f.setSize(new Dimension(800, 600));
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.setVisible(true);
    }

    public int getColor(){
        return ColorUtils.convertHSVToRGBAsInt(hue, saturation, value);
    }

    public void setColor(int ... argb){
        float[] hsv = ColorUtils.convertRGBToHSV(argb);
        this.hue = hsv[0];
        this.saturation = hsv[1];
        this.value = hsv[2];
        this.hPanel.setHue(hue);
        this.svPanel.setColor(hue, saturation, value);
    }

    private static class InternalHueObserver implements Observer<HueChangeEvent>{

        private final ColorChooserPanel parent;

        private InternalHueObserver(ColorChooserPanel parent) {
            this.parent = parent;
        }

        @Override
        public void notify(HueChangeEvent event) {
            this.parent.fireHueChanged(event.getHue());
        }
    }

    private void fireHueChanged(float hue) {
        this.hue = hue;
        this.svPanel.setHue(hue);
    }

    private static class InternalSaturationAndValueObserver implements Observer<SaturationAndValueChangeEvent>{

        private final ColorChooserPanel parent;

        private InternalSaturationAndValueObserver(ColorChooserPanel parent) {
            this.parent = parent;
        }

        @Override
        public void notify(SaturationAndValueChangeEvent event) {
            this.parent.fireSaturationAndValueChanged(event.getSaturation(), event.getValue());
        }
    }

    private static class InternalColorValueObserver implements Observer<ColorChangeEvent>{

        private final ColorChooserPanel parent;

        private InternalColorValueObserver(ColorChooserPanel parent) {
            this.parent = parent;
        }

        @Override
        public void notify(ColorChangeEvent event) {
            this.parent.fireColorChanged(event.getColor());
        }
    }

    private void fireColorChanged(int color) {
        //TODO
    }

    private void fireSaturationAndValueChanged(float saturation, float value) {
        this.saturation = saturation;
        this.value = value;
    }
}
