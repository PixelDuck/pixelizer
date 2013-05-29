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

import java.awt.GridBagConstraints;
import java.awt.Insets;

/**
 * Layout utils.
 */
public final class LayoutUtils {

    private LayoutUtils(){

    }

    public static GridBagConstraints xyi(int x, int y, double wx, double wy, Insets i){
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx=x;
        gbc.gridy=y;
        gbc.weightx = wx;
        gbc.weighty = wy;
        gbc.insets=i;
        gbc.fill = 1;
        return gbc;
    }

    public static GridBagConstraints xywi(int x, int y, int gw, double wx, double wy, Insets i){
        GridBagConstraints gbc = xyi(x, y, wx, wy, i);
        gbc.gridwidth=gw;
        return gbc;
    }


}
