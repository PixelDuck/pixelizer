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
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Main panel to display image.
 */
public class ImagePanel extends JPanel{

    private final Cursor precisionCursor;
    private Pixelizer parent;

    public ImagePanel(Pixelizer parent) {
        this.parent = parent;
        setDoubleBuffered(true);
        setSize(this.parent.getImageWidth(), this.parent.getImageHeight());
        this.precisionCursor = SwingUtils.createCursor("/cursors/precision.png", 16, 16, "precision");
        InternalMouseListener mouseListener = new InternalMouseListener(this.parent);
        addMouseListener(mouseListener);
        addMouseMotionListener(mouseListener);
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
            //grid horizontal
//            g2d.setPaint(Color.black);
//            g2d.fillRect((i + 1) * (parent.getPixelSize() + parent.getMargin()), 0, parent.getMargin(), h);
            for (int j = 0; j < parent.getNbPixels(); j++) {
                int c = getFrame().getColor(i, j);
                g2d.setPaint(ColorUtils.overlayWithColor(Color.GRAY, c));
                int margin = parent.getMargin();
                int pixelSize = parent.getPixelSize();
                g2d.fillRect(margin + i * (pixelSize + margin), margin
                        + j * (pixelSize + margin), pixelSize / 2,
                        pixelSize / 2);
                g2d.fillRect(margin + i * (pixelSize + margin)
                        + pixelSize / 2, margin + j
                        * (pixelSize + margin) + pixelSize / 2,
                        pixelSize / 2, pixelSize / 2);
                g2d.setPaint(ColorUtils.overlayWithColor(Color.LIGHT_GRAY, c));
                g2d.fillRect(margin + i * (pixelSize + margin)
                        + pixelSize / 2, margin + j
                        * (pixelSize + margin), pixelSize / 2,
                        pixelSize / 2);
                g2d.fillRect(margin + i * (pixelSize + margin), margin
                        + j * (pixelSize + margin) + pixelSize / 2,
                        pixelSize / 2, pixelSize / 2);
                //grid vertical
//                if(i==0){
//                    g2d.setPaint(Color.black);
//                    g2d.fillRect(0, (j + 1) * (parent.getPixelSize() + parent.getMargin()), w,
//                            parent.getMargin());
//                }
                if(parent.isSelected(i, j)){
                    g2d.setPaint(Color.white);
                } else {
                    g2d.setPaint(Color.black);
                }
                g2d.drawRect(margin + i * (pixelSize + margin)-1, margin
                        + j * (pixelSize + margin) -1, pixelSize + margin, pixelSize + margin);
            }
        }
        g2d.setPaint(Color.black);
        g2d.fillRect(0, 0, w, parent.getMargin());
        g2d.fillRect(0, 0, parent.getMargin(), h);
        g2d.dispose();
        Toolkit.getDefaultToolkit().sync();
    }


    private class InternalMouseListener extends MouseAdapter {
        private Pixelizer parent;
        private boolean isMouseIn;
        private boolean isMousePressed;

        InternalMouseListener(Pixelizer parent) {
            this.parent = parent;
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            updateMouseCursor(e.getX(), e.getY());
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (isMouseIn && isMousePressed) {
                this.isMousePressed = false;
                doMouse(e);
            }
        }

        private void updateMouseCursor(int x, int y){
            if(isMouseIn){
                int[] xy = findPixelIndices(x, y);
                if(xy[0]<this.parent.getNbPixels() && xy[1]<this.parent.getNbPixels()){
                    setCursor(precisionCursor);
                } else {
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            } else {
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        }

        private void doMouse(MouseEvent e) {
            int[] xy = findPixelIndices(e.getX(), e.getY());
            if(xy[0]<this.parent.getNbPixels() && xy[1]<this.parent.getNbPixels()){
                boolean shiftPressed = (e.getModifiers() & KeyEvent.SHIFT_MASK) != 0;
                boolean ctrlPressed = (e.getModifiers() & KeyEvent.CTRL_MASK) != 0;
                boolean altPressed = (e.getModifiers() & KeyEvent.ALT_MASK) != 0;
                if(SwingUtilities.isLeftMouseButton(e)){
                    this.parent.doFirstActionTool(xy[0], xy[1], shiftPressed, ctrlPressed, altPressed);
                } else if(SwingUtilities.isRightMouseButton(e)){
                    this.parent.doSecondActionTool(xy[0], xy[1], shiftPressed, ctrlPressed, altPressed);
                } else if(SwingUtilities.isMiddleMouseButton(e)){
                    this.parent.doThirdActionTool(xy[0], xy[1], shiftPressed, ctrlPressed, altPressed);
                }
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            this.isMouseIn = true;
            updateMouseCursor(e.getX(), e.getY());
        }

        @Override
        public void mouseExited(MouseEvent e) {
            this.isMouseIn = false;
            updateMouseCursor(e.getX(), e.getY());
        }

        @Override
        public void mousePressed(MouseEvent e) {
            this.isMousePressed = true;
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (isMouseIn && isMousePressed) {
                doMouse(e);
            }
            updateMouseCursor(e.getX(), e.getY());
        }

        private int[] findPixelIndices(int x, int y) {
            return new int[] {x / (parent.getPixelSize()+ parent.getMargin()),
                    y / (parent.getPixelSize()+ parent.getMargin())};
        }
    }

}
