package org.esa.s2tbx.imagewriter;

import junit.framework.TestCase;
import org.esa.snap.utils.TestUtil;
import org.junit.Test;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


/**
 * Created by rdumitrascu on 10/28/2016.
 */
public class ImageWriterTest extends TestCase {
    private String productsFolder = "_jp2" + File.separator;
    @Test
    public void testImageWriterWithOneBand()throws IOException{

        File file = TestUtil.getTestFile(productsFolder + "1_8bit_component_256entrypalette.tif");
        File f = new File(System.getProperty("user.home") + "/Desktop"+"\\1_8bit_component_256entrypalette_1.jp2");
        BufferedImage bi = ImageIO.read(file);
        IIOImage img = new IIOImage(bi,null,null);
        ImageWriterPlugin imgWriter = new ImageWriterPlugin();
        imgWriter.setOutput(f);
        imgWriter.setNumberResolution(10);
        imgWriter.write(null,img,null);
    }
    @Test
    public void testImageWIthBitDepthToHigh()throws IOException{

        File f = new File(System.getProperty("user.home") + "/Desktop"+"\\1_8bit_component_256entrypalette_3.jp2");
        BufferedImage buffImg = new BufferedImage(240, 240, BufferedImage.TYPE_INT_RGB);
        IIOImage img = new IIOImage(buffImg,null,null);
        ImageWriterPlugin imgWriter = new ImageWriterPlugin();
        imgWriter.setOutput(f);
        imgWriter.setNumberResolution(10);
        imgWriter.write(null,img,null);
    }
    @Test
    public void testImageWriterWithDefaultResolution() throws IOException {

        final File file = TestUtil.getTestFile(productsFolder + "1_8bit_component_256entrypalette.tif");
        final File f = new File(System.getProperty("user.home") + "/Desktop"+"\\1_8bit_component_256entrypalette_2.jp2");
        BufferedImage bi = ImageIO.read(file);
        IIOImage img = new IIOImage(bi,null,null);
        ImageWriterPlugin imgWriter = new ImageWriterPlugin();
        imgWriter.setOutput(f);
        imgWriter.write(null,img,null);
    }
    @Test
    public void testImageWriterWithMultipleBands() throws IOException {

        final File file = TestUtil.getTestFile(productsFolder + "3_8bit_components_srgb.tif");
        File f = new File(System.getProperty("user.home") + "/Desktop"+"\\3_8bit_components_srgb_1.jp2");
        BufferedImage bi = ImageIO.read(file);
        IIOImage img = new IIOImage(bi,null,null);
        ImageWriterPlugin imgWriter = new ImageWriterPlugin();
        imgWriter.setOutput(f);
        imgWriter.setNumberResolution(10);
        imgWriter.write(null,img,null);
    }
    @Test
    public void testImageWriterWithSetNumResolutions() throws IOException {

        final File file = TestUtil.getTestFile(productsFolder + "3_8bit_components_srgb.tif");;
        File f = new File(System.getProperty("user.home") + "/Desktop"+"\\3_8bit_components_srgb_2.jp2");
        BufferedImage bi = ImageIO.read(file);
        IIOImage img = new IIOImage(bi,null,null);
        ImageWriterPlugin imgWriter = new ImageWriterPlugin();
        imgWriter.setOutput(f);
        imgWriter.setNumberResolution(6);
        imgWriter.write(null,img,null);
    }
}
