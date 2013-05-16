package io.gameover.utilities.pixeleditor;

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
        int[] argb = Utils.extractRGB(0x00FF0000);
        Assert.assertEquals(255, argb[0]);
        Assert.assertEquals(0, argb[1]);
        Assert.assertEquals(0, argb[2]);

        argb = Utils.extractRGB(0x0000FF00);
        Assert.assertEquals(0, argb[0]);
        Assert.assertEquals(255, argb[1]);
        Assert.assertEquals(0, argb[2]);

        argb = Utils.extractRGB(0x000000FF);
        Assert.assertEquals(0, argb[0]);
        Assert.assertEquals(0, argb[1]);
        Assert.assertEquals(255, argb[2]);


    }

    @Test
    public void testExtractARGB(){
        System.out.println("0xFFFFFFFF:"+0xffffffff);
        int[] argb = Utils.extractARGB(0xffffffff);
        Assert.assertEquals(255, argb[0]);
        Assert.assertEquals(255, argb[1]);
        Assert.assertEquals(255, argb[2]);
        Assert.assertEquals(255, argb[3]);
    }
}
