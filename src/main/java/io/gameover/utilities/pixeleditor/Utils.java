package io.gameover.utilities.pixeleditor;

import java.awt.Color;
import java.awt.Paint;

/**
 * TODO: pb selection couleur + alpha
 * TODO: fill
 */
public final class Utils {
    private Utils(){}


    public static int[] extractARGB(int c){
        int[] ret = new int[]{
                (c & 0xff000000) >>> 24,
                (c & 0x00ff0000) >> 16,
                (c & 0x0000ff00) >> 8,
                (c & 0x000000ff)};
        return ret;
    }

    public static int convertToColorAsInt(int ... argb){
        int c = 0xff000000;
        int rgbFirstIndex = 0;
        if(argb.length==4){
            rgbFirstIndex = 1;
            c += (argb[0] << 24)& 0xff000000;
        }
        c += (argb[rgbFirstIndex]<<16)& 0x00ff0000;
        c += (argb[rgbFirstIndex+1]<<8)& 0x0000ff00;
        c += (argb[rgbFirstIndex+2])& 0x000000ff;
        return c;
    }

    public static int convertToColorAsInt(Color color){
        return convertToColorAsInt(color.getAlpha(), color.getRed(), color.getGreen(), color.getBlue());
    }

    public static int[] extractRGB(int c){
        return new int[]{
                (c & 0x00FF0000)>>16,
                (c & 0x00FF00)>>8,
                (c & 0x000000FF)};
    }

    public static double computeAlphaMultiplicator(int a){
        return ((double)a)/255d;
    }

    public static Paint overlayWithColor(Color original, int c) {
        int[] rgb = overlayWithColor(convertToColorAsInt(original), c);
        return new Color(rgb[0], rgb[1], rgb[2]);
    }

    public static int[] overlayWithColor(int original, int c) {
        int[] rgb = Utils.extractRGB(original);
        int[] argb = Utils.extractARGB(c);
        double alpha = computeAlphaMultiplicator(argb[0]);
        return new int[]{
                (int) (rgb[0]*(1.0d-alpha)+argb[1]*alpha),
                (int) (rgb[1]*(1.0d-alpha)+argb[2]*alpha),
                (int) (rgb[2]*(1.0d-alpha)+argb[3]*alpha)
        };
    }
}
