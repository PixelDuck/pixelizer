package io.gameover.utilities.pixeleditor;

import java.awt.Color;
import java.awt.Paint;

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
//        float[]drgb = new float[]{rgb[index]/255f, rgb[index+1]/255f, rgb[index+2]/255f};
//        float minRGB = Math.min(drgb[0], Math.min(drgb[1], drgb[2]));
//        float maxRGB = Math.max(drgb[0], Math.max(drgb[1], drgb[2]));
//
//        if (minRGB==maxRGB) {
//            // Black-gray-white
//            return new float[]{0, 0, minRGB};
//        } else {
//            // Colors other than black-gray-white:
//            float d = (drgb[0]==minRGB) ? drgb[1]-drgb[2] : ((drgb[2]==minRGB) ? drgb[0]-drgb[1] : drgb[2]-drgb[0]);
//            float h = (drgb[1]==minRGB) ? 3f : ((drgb[2]==minRGB) ? 1f : 5f);
//            float computedH = 60f*(h - d/(maxRGB - minRGB));
//            float computedS = (maxRGB - minRGB)/maxRGB;
//            float computedV = maxRGB;
//            return new float[]{computedH, computedS, computedV};
//        }
    }

    public static int convertHSVToRGBAsInt(float ... hsv){
        return Color.HSBtoRGB(hsv[0], hsv[1], hsv[2]);
        //return convertToColorAsInt(convertHSVToRGB(hsv));
    }

    //public static int[] convertHSVToRGB(float ... hsv){

//
//        float hue = hsv[0];
//        float saturation = hsv[1];
//        float value = hsv[2];
//        float r, g, b;
//
//        int h = (int)(hue * 6);
//        float f = hue * 6 - h;
//        float p = value * (1 - saturation);
//        float q = value * (1 - f * saturation);
//        float t = value * (1 - (1 - f) * saturation);
//
//        if (h == 0) {
//            r = value;
//            g = t;
//            b = p;
//        } else if (h == 1) {
//            r = q;
//            g = value;
//            b = p;
//        } else if (h == 2) {
//            r = p;
//            g = value;
//            b = t;
//        } else if (h == 3) {
//            r = p;
//            g = q;
//            b = value;
//        } else if (h == 4) {
//            r = t;
//            g = p;
//            b = value;
//        } else if(h == 5) {
//            r = value;
//            g = p;
//            b = q;
//        } else {
//            throw new RuntimeException("Something went wrong when converting from HSV to RGB. Input was " + hue + ", " + saturation + ", " + value+". H found: "+h);
//        }
//
//        return new int[]{
//            (int)(r * 255),
//                (int)(g * 255),
//                (int)(b * 255)};
//    }

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
