package io.gameover.utilities.pixeleditor.utils;

import java.awt.Color;

/**
 * Utilities class for all color conversions.
 */
public final class ColorUtils {

    private ColorUtils(){}


    /**
     * Convert RGB values (between 0 and 255 integers) to HSV.
     * The hue (H) varies on the 360-degree hue circle (also known as the color wheel),
     * red being 0, yellow 60, green 120, cyan 180, blue 240,
     * and magenta 300. The S and V range from 0 to 1. Here is the source code of the RGB-to-HSV converter function.
     * @param rgb rgb values
     * @return a float array
     */
    public static float[] convertRGBToHSV(int ... rgb){
        int index = rgb.length-3;
        float[] hsv = new float[3];
        Color.RGBtoHSB(rgb[index], rgb[index+1], rgb[index+2], hsv);
        return hsv;
    }

    public static int convertHSVToRGBAsInt(float ... hsv){
        return Color.HSBtoRGB(hsv[0], hsv[1], hsv[2]);
    }
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

    public static Color overlayWithColor(Color original, int c) {
        int[] rgb = overlayWithColor(convertToColorAsInt(original), c);
        return new Color(rgb[0], rgb[1], rgb[2]);
    }

    public static int[] overlayWithColor(int original, int c) {
        int[] rgb = ColorUtils.extractRGB(original);
        int[] argb = ColorUtils.extractARGB(c);
        double alpha = computeAlphaMultiplicator(argb[0]);
        return new int[]{
                (int) (rgb[0]*(1.0d-alpha)+argb[1]*alpha),
                (int) (rgb[1]*(1.0d-alpha)+argb[2]*alpha),
                (int) (rgb[2]*(1.0d-alpha)+argb[3]*alpha)
        };
    }
}
