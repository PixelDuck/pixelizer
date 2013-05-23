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

import java.awt.Cursor;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;

/**
 * Created with IntelliJ IDEA.
 * User: MRS.OMARTIN
 * Date: 23/05/13
 * Time: 07:20
 * To change this template use File | Settings | File Templates.
 */
public final class SwingUtils {

    private SwingUtils(){

    }

    public static Cursor createCursor(String imgPath, int hotSpotX, int hotSpotY, String name){
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Image image = toolkit.getImage(SwingUtils.class.getResource(imgPath));
        return toolkit.createCustomCursor(image , new Point(16, 16), name);
    }
}
