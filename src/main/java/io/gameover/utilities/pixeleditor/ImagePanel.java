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
import io.gameover.utilities.pixeleditor.utils.SwingUtils;

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

    private static final int PIXEL_SIZE = 10;
    private static final int PANEL_MARGIN = 1;
    private static final int PIXEL_MARGIN = 1;

    private final Cursor precisionCursor;
    private Pixelizer parent;

    public ImagePanel(Pixelizer parent) {
        this.parent = parent;
        setDoubleBuffered(true);
        this.precisionCursor = SwingUtils.createCursor("/cursors/precision.png", 16, 16, "precision");
        InternalMouseListener mouseListener = new InternalMouseListener(this.parent);
        addMouseListener(mouseListener);
        addMouseMotionListener(mouseListener);
    }

    protected Frame getFrame(){
        return parent.getCurrentFrame();
    }

    public void paint(Graphics g) {
        super.paint(g);

        Graphics2D g2d = (Graphics2D) g;
        draw(g2d, new MainDrawCallable());
        draw(g2d, new SelectionDrawCallable());
        g2d.dispose();
        Toolkit.getDefaultToolkit().sync();
    }

    private void draw(Graphics2D g2d, DrawCallable callable){
        for (int i = 0; i < Frame.NB_PIXELS; i++) {
            for (int j = 0; j < Frame.NB_PIXELS; j++) {
                int x = PANEL_MARGIN + i * (PIXEL_SIZE + PIXEL_MARGIN);
                int y = PANEL_MARGIN + j * (PIXEL_SIZE + PIXEL_MARGIN);
                callable.draw(g2d, i, j, x, y, this);
            }
        }
    }

    private static interface DrawCallable{
        public void draw(Graphics2D g2d, int i, int j, int x, int y, ImagePanel parent);
    }

    private static class MainDrawCallable implements DrawCallable{
        public void draw(Graphics2D g2d, int i, int j, int x, int y, ImagePanel parent){
            int c = parent.getFrame().getColor(i, j);
            int half = PIXEL_SIZE / 2;
            Color bc = ColorUtils.overlayWithColor(Color.DARK_GRAY, c);
            if(parent.parent.isSelected(i, j)){
                bc = ColorUtils.overlayWithColor(bc, 0x550000FF);
            }
            g2d.setPaint(bc);
            g2d.fillRect(x, y, half, half);
            g2d.fillRect(x+half, y+half, half, half);
            bc = ColorUtils.overlayWithColor(Color.LIGHT_GRAY, c);
            if(parent.parent.isSelected(i, j)){
                bc = ColorUtils.overlayWithColor(bc, 0x550000FF);
            }
            g2d.setPaint(bc);
            g2d.fillRect(x+half, y, half, half);
            g2d.fillRect(x, y+half, half, half);
            g2d.setPaint(Color.black);
            g2d.drawRect(x-1, y-1, PIXEL_SIZE+1, PIXEL_SIZE+1);
        }
    }

    private static class SelectionDrawCallable implements DrawCallable{
        public void draw(Graphics2D g2d, int i, int j, int x, int y, ImagePanel parent){
            if(parent.parent.isSelected(i, j)){
                g2d.setPaint(Color.white);
                g2d.drawRect(x-1, y-1, PIXEL_SIZE+1, PIXEL_SIZE+1);
            }
        }
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
                if(xy[0]<Frame.NB_PIXELS && xy[1]<Frame.NB_PIXELS){
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
            if(xy[0]<Frame.NB_PIXELS && xy[1]<Frame.NB_PIXELS){
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
            return new int[] {(x-PANEL_MARGIN) / (PIXEL_SIZE+PIXEL_MARGIN),
                    (y-PANEL_MARGIN) / (PIXEL_SIZE+PIXEL_MARGIN)};
        }
    }

}
