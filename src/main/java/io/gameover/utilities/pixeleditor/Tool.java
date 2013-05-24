package io.gameover.utilities.pixeleditor;

import javax.swing.*;
import java.awt.Dimension;
import java.awt.event.KeyEvent;

/**
* Created with IntelliJ IDEA.
* User: MRS.OMARTIN
* Date: 14/05/13
* Time: 07:45
* To change this template use File | Settings | File Templates.
*/
public enum Tool {
    PEN("/draw-freehand.png", "<html>Key: 'P'<br>Left click to draw<br>Right click to select color<br>Middle click to delete</html>", KeyEvent.VK_P),
    FILL("/color-fill.png", "<html>Key: 'F'</html>", KeyEvent.VK_F),
    CLEAR("/draw-eraser.png", "<html>Key: 'X'</html>", KeyEvent.VK_X),
    SELECT("/select-continuous-area.png", "<html>Key: 'S'<br>Left click to select<br>Right click to deselect</html>", KeyEvent.VK_S),
    MAGIC_WAND("/select-lasso.png", "<html>Key: 'W'</html>", KeyEvent.VK_W),
    MOVE("/transform-move.png", "<html>Key: 'M'</html>", KeyEvent.VK_M);

    private JToggleButton button;
    private int keyStroke;

    Tool(String imgIcon, String tooltip, int keyStroke) {
        button = new JToggleButton(new ImageIcon(getClass().getResource(imgIcon)));
        if(tooltip!=null){
            button.setToolTipText(tooltip);
        }
        button.setPreferredSize(new Dimension(25,25));
        button.setActionCommand(name());
        this.keyStroke = keyStroke;
    }

    public JToggleButton getButton(){
        return button;
    }

    public int getKeyStroke() {
        return keyStroke;
    }
}
