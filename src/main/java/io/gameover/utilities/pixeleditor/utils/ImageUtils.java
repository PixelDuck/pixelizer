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

package io.gameover.utilities.pixeleditor.utils;

import io.gameover.utilities.pixeleditor.Frame;

import java.awt.image.BufferedImage;

/**
 * Created with IntelliJ IDEA.
 * User: MRS.OMARTIN
 * Date: 28/05/13
 * Time: 09:32
 * To change this template use File | Settings | File Templates.
 */
public final class ImageUtils {

    private ImageUtils(){

    }

    public static String toAlphaMask(BufferedImage image){
        //debugging import
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if(x%32==0){
                    sb.append("|");
                }
                sb.append(image.getRGB(x, y)== Frame.NO_COLOR_AS_INT?" ":"#");
            }
            sb.append('\n');
        }
        sb.append('\n');
        return sb.toString();
    }
}
