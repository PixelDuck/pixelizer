package io.gameover.utilities.pixeleditor;

import javax.swing.*;
import java.awt.Dimension;

/**
* Created with IntelliJ IDEA.
* User: MRS.OMARTIN
* Date: 14/05/13
* Time: 07:45
* To change this template use File | Settings | File Templates.
*/
public enum Tool {
    PEN("/draw-freehand.png"),
    FILL("/color-fill.png"),
    CLEAR("/draw-eraser.png"),
    SELECT("/select-continuous-area.png"),
    MAGIC_WAND("/select-lasso.png"),
    MOVE("/transform-move.png");

    private JToggleButton button;

    Tool(String imgIcon) {
        button = new JToggleButton(new ImageIcon(getClass().getResource(imgIcon)));
        button.setPreferredSize(new Dimension(20,20));
        button.setActionCommand(name());
    }

    public JToggleButton getButton(){
        return button;
    }
}
