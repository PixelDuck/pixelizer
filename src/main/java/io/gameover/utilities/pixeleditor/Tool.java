package io.gameover.utilities.pixeleditor;

import javax.swing.*;

/**
* Created with IntelliJ IDEA.
* User: MRS.OMARTIN
* Date: 14/05/13
* Time: 07:45
* To change this template use File | Settings | File Templates.
*/
public enum Tool {
    PEN("P"), SELECT("S"), FILL("F"), CLEAR("X"), MAGIC_WAND("W"), MOVE("M");

    private JToggleButton button;

    Tool(String label) {
        button = new JToggleButton(label);
        button.setActionCommand(name());
    }

    public JToggleButton getButton(){
        return button;
    }
}
