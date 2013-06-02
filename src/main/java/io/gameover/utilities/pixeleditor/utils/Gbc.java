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
public class Gbc {
    GridBagConstraints gbc;

    public Gbc(int x, int y){
        gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.weightx = 0d;
        gbc.weighty = 0d;
        gbc.insets=new Insets(2,2,2,2);
        gbc.anchor = GridBagConstraints.NORTHWEST;
    }

    public GridBagConstraints toGbc(){
        return gbc;
    }

    public Gbc wxy(double wx, double wy){
        gbc.weightx = wx;
        gbc.weighty = wy;
        return this;
    }

    public Gbc i(Insets i){
        gbc.insets=i;
        return this;
    }

    public Gbc f(int fill){
          gbc.fill = fill;
        return this;
    }

    public Gbc a(int anchor){
        gbc.anchor = anchor;
        return this;
    }

    public Gbc ar(){
        return a(GridBagConstraints.EAST);
    }

    public Gbc al(){
        return a(GridBagConstraints.WEST);
    }

    public Gbc atl(){
        return a(GridBagConstraints.NORTHWEST);
    }

    public Gbc atr(){
        return a(GridBagConstraints.NORTHEAST);
    }

    public Gbc gw(int gridWidth){
        gbc.gridwidth = gridWidth;
        return this;
    }

    public Gbc gh(int gridHeight){
        gbc.gridheight = gridHeight;
        return this;
    }

}
