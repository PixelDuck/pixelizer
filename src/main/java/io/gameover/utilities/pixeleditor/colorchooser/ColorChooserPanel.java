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
import io.gameover.utilities.pixeleditor.utils.Gbc;
import io.gameover.utilities.pixeleditor.utils.LayoutUtils;
import io.gameover.utilities.pixeleditor.utils.Observable;
import io.gameover.utilities.pixeleditor.utils.Observer;

import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.MaskFormatter;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.Format;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel to select a color from an HSV color gradients
 */
public class ColorChooserPanel extends JPanel implements Observable<ColorChangeEvent>{

    public static final String HEXA_CHANGED = "hexaChanged";
    private final SaturationAndValueGradientPanel svPanel;
    private final HueGradientPanel hPanel;
    private final JFormattedTextField hexaTF;
    private float hue;
    private float saturation;
    private float value;
    private List<JTextField> rgbhsvTFList = new ArrayList<>(6);
    private List<Observer<ColorChangeEvent>> observers = new ArrayList<>();

    public ColorChooserPanel(){
        super(new GridBagLayout());
        this.svPanel = new SaturationAndValueGradientPanel();
        this.hPanel = new HueGradientPanel();
        add(svPanel, LayoutUtils.xyhi(1, 1, 9, 0d, 0d, new Insets(0,0,0,2)));
        add(hPanel, LayoutUtils.xyhi(2, 1, 8, 0d, 0d, new Insets(0, 2, 0, 2)));
        add(new JLabel("H"), new Gbc(3, 1).i(new Insets(0, 2, 4, 2)).ar().toGbc());
        MaskFormatter mask = null;
        try {
            mask = new MaskFormatter("'#HHHHHH");
        } catch (ParseException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        this.hexaTF = new JFormattedTextField(mask);
        hexaTF.setColumns(7);
        hexaTF.setActionCommand(HEXA_CHANGED);
        hexaTF.addActionListener(new IntenalActionListener(this));
        //hexaTF.setPreferredSize(new Dimension(125,25));
        add(hexaTF, LayoutUtils.xyi(4, 1, 0d, 0d, new Insets(0, 2, 4, 2)));
        add(new JLabel("R"), new Gbc(3, 2).i(new Insets(0, 2, 1, 2)).ar().toGbc());
        add(createColorValueField(true), LayoutUtils.xyi(4, 2, 0d, 0d, new Insets(0,2,1,2)));
        add(new JLabel("G"), new Gbc(3, 3).i(new Insets(0, 2, 1, 2)).ar().toGbc());
        add(createColorValueField(true), LayoutUtils.xyi(4, 3, 0d, 0d, new Insets(0,2,1,2)));
        add(new JLabel("B"), new Gbc(3, 4).i(new Insets(0, 2, 4, 2)).ar().toGbc());
        add(createColorValueField(true), LayoutUtils.xyi(4, 4, 0d, 0d, new Insets(0, 2, 4, 2)));
        add(new JLabel("H"), new Gbc(3, 5).i(new Insets(0, 2, 1, 2)).ar().toGbc());
        add(createColorValueField(false), LayoutUtils.xyi(4, 5, 0d, 0d, new Insets(0, 2, 1, 2)));
        add(new JLabel("S"), new Gbc(3, 6).i(new Insets(0, 2, 1, 2)).ar().toGbc());
        add(createColorValueField(false), LayoutUtils.xyi(4, 6, 0d, 0d, new Insets(0, 2, 1, 2)));
        add(new JLabel("V"), new Gbc(3, 7).i(new Insets(0, 2, 1, 2)).ar().toGbc());
        add(createColorValueField(false), LayoutUtils.xyi(4, 7, 0d, 0d, new Insets(0, 2, 1, 2)));
        this.svPanel.addObserver(new InternalSaturationAndValueObserver(this));
        this.hPanel.addObserver(new InternalHueObserver(this));
        addObserver(new InternalColorChangeObserver(this));
        setColor(255, 0, 0);
    }

    @Override
    public void addObserver(Observer<ColorChangeEvent> o) {
        this.observers.add(o);
    }

    @Override
    public void removeObserver(Observer<ColorChangeEvent> o) {
        this.observers.remove(o);
    }


    private JTextField createColorValueField(boolean rgb){
        JTextField tf = new JTextField(rgb?3:4);
        tf.setEditable(false);
        rgbhsvTFList.add(tf);
        return tf;
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

    public String getColorAsHexa(){
        int rgb = getColor();
        String h = Integer.toHexString(rgb);
        System.out.println("h "+h);
        return h.substring(h.length()-6);
    }


    public void setColor(int ... argb){
        float[] hsv = ColorUtils.convertRGBToHSV(argb);
        this.hue = hsv[0];
        this.saturation = hsv[1];
        this.value = hsv[2];
        this.hPanel.setHue(hue);
        this.svPanel.setColor(hue, saturation, value);
        this.hexaTF.setValue("#"+getColorAsHexa());
        updateColorFields();
                updateUI();
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
        this.hexaTF.setValue("#"+getColorAsHexa());
        updateColorFields();
        updateUI();
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
        float[] hsv = ColorUtils.convertRGBToHSV(ColorUtils.extractRGB(color));
        this.hue = hsv[0];
        this.saturation = hsv[1];
        this.value = hsv[2];
        this.svPanel.setHue(hue);
        this.svPanel.setSaturationAndValue(saturation, value);
        this.hPanel.setHue(hue);
        updateColorFields();
        updateUI();
    }

    private void updateColorFields() {
        int[] rgb = ColorUtils.extractRGB(getColor());
        rgbhsvTFList.get(0).setText(""+rgb[0]);
        rgbhsvTFList.get(1).setText(""+rgb[1]);
        rgbhsvTFList.get(2).setText(""+rgb[2]);
        rgbhsvTFList.get(3).setText(""+Math.round(hue*1000f)/1000f);
        rgbhsvTFList.get(4).setText(""+Math.round(saturation*1000f)/1000f);
        rgbhsvTFList.get(5).setText(""+Math.round(value*1000f)/1000f);
    }

    private void fireSaturationAndValueChanged(float saturation, float value) {
        this.saturation = saturation;
        this.value = value;
        this.hexaTF.setValue("#"+getColorAsHexa());
        updateColorFields();
        updateUI();
    }

    private class IntenalActionListener implements ActionListener {
        private final ColorChooserPanel parent;

        public IntenalActionListener(ColorChooserPanel colorChooserPanel) {
            this.parent = colorChooserPanel;
        }


        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getActionCommand().equals(HEXA_CHANGED)){
                String hexa = (String) this.parent.hexaTF.getValue();
                if(hexa.trim().length()==7){
                    Color color = Color.decode(hexa.trim());
                    ColorChangeEvent evt = new ColorChangeEvent(this.parent, color.getRGB());
                    for(Observer<ColorChangeEvent> o : observers){
                        o.notify(evt);
                    }
                }
            }
        }
    }

    private class InternalColorChangeObserver implements Observer<ColorChangeEvent> {
        private final ColorChooserPanel parent;

        public InternalColorChangeObserver(ColorChooserPanel colorChooserPanel) {
            this.parent = colorChooserPanel;
        }

        @Override
        public void notify(ColorChangeEvent event) {
            this.parent.fireColorChanged(event.getColor());
        }
    }
}

