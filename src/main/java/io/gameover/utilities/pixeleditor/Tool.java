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
    PEN("/img/pencil.png"),
    FILL("/img/paintcan.png"),
    CLEAR("/img/cross.png"),
    SELECT("/img/shape_square_edit.png"),
    MAGIC_WAND("/img/wand.png"),
    MOVE("/img/shape_handles.png");

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
