package io.gameover.utilities.pixeleditor;

import io.gameover.utilities.pixeleditor.utils.ColorUtils;
import junit.framework.Assert;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: MRS.OMARTIN
 * Date: 16/05/13
 * Time: 07:56
 * To change this template use File | Settings | File Templates.
 */
public class UtilsTest {

    @Test
    public void testExtractRGB(){
        int[] argb = ColorUtils.extractRGB(0x00FF0000);
        Assert.assertEquals(255, argb[0]);
        Assert.assertEquals(0, argb[1]);
        Assert.assertEquals(0, argb[2]);

        argb = ColorUtils.extractRGB(0x0000FF00);
        Assert.assertEquals(0, argb[0]);
        Assert.assertEquals(255, argb[1]);
        Assert.assertEquals(0, argb[2]);

        argb = ColorUtils.extractRGB(0x000000FF);
        Assert.assertEquals(0, argb[0]);
        Assert.assertEquals(0, argb[1]);
        Assert.assertEquals(255, argb[2]);


    }

    @Test
    public void testExtractARGB(){
        System.out.println("0xFFFFFFFF:"+0xffffffff);
        int[] argb = ColorUtils.extractARGB(0xffffffff);
        Assert.assertEquals(255, argb[0]);
        Assert.assertEquals(255, argb[1]);
        Assert.assertEquals(255, argb[2]);
        Assert.assertEquals(255, argb[3]);
    }

    @Test
    public void testConvert(){
        int[] argb = ColorUtils.extractARGB(16777215);
        for(int i=0;i<argb.length;i++){
            System.out.print(argb[i]+",");
        }
        System.out.println();

        argb = ColorUtils.extractARGB(0);
        for(int i=0;i<argb.length;i++){
            System.out.print(argb[i]+",");
        }
        System.out.println();
    }
}
