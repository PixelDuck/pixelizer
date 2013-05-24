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

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created with IntelliJ IDEA.
 * User: MRS.OMARTIN
 * Date: 23/05/13
 * Time: 07:44
 * To change this template use File | Settings | File Templates.
 */
public class AnimationFrameContextMenu extends JPopupMenu{

    public static final String DELETE = "DELETE";
    public static final String INSERT_LEFT = "INSERT_LEFT";
    public static final String INSERT_RIGHT = "INSERT_RIGHT";
    public static final String MOVE_LEFT = "MOVE_LEFT";
    public static final String MOVE_RIGHT = "MOVE_RIGHT";

    private final Pixelizer parent;
    private final int frameIndex;

    public AnimationFrameContextMenu(Pixelizer parent, int frameIndex) {
        this.parent = parent;
        this.frameIndex = frameIndex;
        InternalActionListener actionListener = new InternalActionListener(this);

        JMenuItem deleteFrameMI = new JMenuItem("Delete frame");
        deleteFrameMI.setActionCommand(DELETE);
        deleteFrameMI.addActionListener(actionListener);
        add(deleteFrameMI);

        JMenuItem insertLeftMI = new JMenuItem("Insert left");
        insertLeftMI.setActionCommand(INSERT_LEFT);
        insertLeftMI.addActionListener(actionListener);
        add(insertLeftMI);

        JMenuItem insertRightMI = new JMenuItem("Insert right");
        insertRightMI.setActionCommand(INSERT_RIGHT);
        insertRightMI.addActionListener(actionListener);
        add(insertRightMI);

        if(frameIndex>0){
            JMenuItem moveLeftMI = new JMenuItem("Move left");
            moveLeftMI.setActionCommand(MOVE_LEFT);
            moveLeftMI.addActionListener(actionListener);
            add(moveLeftMI);
        }

        if(frameIndex<parent.getPixelFrames().size()-1){
            JMenuItem moveRightMI = new JMenuItem("Move right");
            moveRightMI.setActionCommand(MOVE_RIGHT);
            moveRightMI.addActionListener(actionListener);
            add(moveRightMI);
        }
    }

    private class InternalActionListener implements ActionListener {
        private AnimationFrameContextMenu parent;

        InternalActionListener(AnimationFrameContextMenu parent) {
            this.parent = parent;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals(DELETE)) {
                this.parent.deleteFrame();
            } else if (e.getActionCommand().equals(INSERT_LEFT)) {
                this.parent.insertLeft();
            } else if (e.getActionCommand().equals(INSERT_RIGHT)) {
                this.parent.insertRight();
            } else if(e.getActionCommand().equals(MOVE_LEFT)){
                this.parent.moveLeft();
            } else if(e.getActionCommand().equals(MOVE_RIGHT)){
                this.parent.moveRight();
            }
        }
    }

    private void moveRight() {
        this.parent.moveFrameRight(frameIndex);
    }

    private void moveLeft() {
        this.parent.moveFrameLeft(frameIndex);
    }

    private void insertRight() {
        this.parent.insertFrameRight(frameIndex);
    }

    private void insertLeft() {
        this.parent.insertFrameLeft(frameIndex);
    }

    private void deleteFrame() {
        this.parent.removeFrame(frameIndex);
    }
}
