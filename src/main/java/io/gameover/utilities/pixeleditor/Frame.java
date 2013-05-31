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
import io.gameover.utilities.pixeleditor.utils.Utilities;
import javafx.util.Pair;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
* Created with IntelliJ IDEA.
* User: MRS.OMARTIN
* Date: 28/05/13
* Time: 08:24
* To change this template use File | Settings | File Templates.
*/
public class Frame {
    public static final int NO_COLOR_AS_INT = 0x00000000;
    public static final int NB_PIXELS = 32;


    private int[][] argb;

    public Frame(){
        reset();
    }

    public void reset() {
        argb = new int[NB_PIXELS][NB_PIXELS];
        for(int i=0; i< argb.length; i++){
            for(int j=0; j< argb[0].length; j++){
                argb[i][j] = NO_COLOR_AS_INT;
            }
        }
    }

    public List<Pair<Integer, Integer>> extractColors(){
        Map<Integer, Integer> m = new HashMap<>();
        for(int i=0; i< argb.length; i++){
            for(int j=0; j< argb[0].length; j++){
                int c = argb[i][j];
                if(!m.containsKey(c)){
                    m.put(c, 1);
                } else {
                    m.put(c, m.get(c)+1);
                }
            }
        }
        List<Pair<Integer, Integer>> ret = new ArrayList<>();
        for(Integer k : m.keySet()){
            ret.add(new Pair(k, m.get(k)));
        }
        Collections.sort(ret, new Comparator<Pair<Integer, Integer>>() {
            @Override
            public int compare(Pair<Integer, Integer> o1, Pair<Integer, Integer> o2) {
                return o2.getValue()-o1.getValue();
            }
        });
        return ret;
    }

    public int getColor(int i, int j){
        return argb[i][j];
    }

    public int getWidth(){
        return argb.length;
    }

    public int getHeight(){
        return argb[0].length;
    }

    public void setColor(int i, int j, int color){
        argb[i][j] = color;
    }

    public Frame clone(){
        Frame p = new Frame();
        p.argb = Utilities.copyArray(argb);
        return p;
    }

    public void fillColor(int x, int y, int newColor, int tolerance) {
        List<Point> points = findPoint(x, y, tolerance);
        for(Point p : points){
            setColor(p.x, p.y, newColor);
        }
    }

    public List<Point> findPoint(int x, int y, int tolerance) {
        int color = getColor(x, y);
        Set<Point> scanned = new HashSet<>();
        List<Point> ret = new ArrayList<>();
        findPointAux(x, y, color, tolerance, ret, scanned);
        return ret;
    }

    private void findPointAux(int x, int y, int color, int tolerance, List<Point> ret, Set<Point> scanned) {
        Point p = new Point(x, y);
        if(!scanned.contains(p)){
            scanned.add(p);
            if(x>=0 && x<argb.length
                    && y>=0 && y<argb[0].length
                    && isColorClosed(getColor(x,y), color, tolerance)){
                ret.add(p);
                findPointAux(x-1, y, color, tolerance, ret, scanned);
                findPointAux(x+1, y, color, tolerance, ret, scanned);
                findPointAux(x, y-1, color, tolerance, ret, scanned);
                findPointAux(x, y+1, color, tolerance, ret, scanned);
            }
        }
    }

    private boolean isColorClosed(int color1, int color2, int tolerance) {
        int t = 256*tolerance/100;
        int[] argb1 = ColorUtils.extractARGB(color1);
        int[] argb2 = ColorUtils.extractARGB(color2);
        boolean ok = true;
        for(int i=0; i<4;i++){
            ok &= ((argb1[i]-t) < argb2[i] && argb2[i]< (argb1[i]+t));
        }
        return ok;
    }

    public BufferedImage getAsBufferedImage(){
        return getAsBufferedImage(null);
    }

    public BufferedImage getAsBufferedImage(Color transparent){
        BufferedImage bi = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        for(int i=0; i<bi.getWidth(); i++){
            for(int j=0; j<bi.getHeight(); j++){
                if(transparent!=null && argb[i][j]==NO_COLOR_AS_INT){
                    bi.setRGB(i, j, transparent.getRGB());
                } else {
                    bi.setRGB(i, j, argb[i][j]);
                }
            }
        }
        return bi;
    }

    public String toStringAlpha(){
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                sb.append(getColor(x, y)==Frame.NO_COLOR_AS_INT?" ":"#");
            }
            sb.append('\n');
        }
        sb.append('\n');
        return sb.toString();
    }
}
