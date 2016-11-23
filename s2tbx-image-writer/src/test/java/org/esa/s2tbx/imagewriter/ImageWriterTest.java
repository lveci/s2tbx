package org.esa.s2tbx.imagewriter;

import junit.framework.TestCase;
import org.esa.snap.utils.TestUtil;
import org.junit.Test;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.spi.ImageReaderWriterSpi;
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

        BufferedImage bi = ImageIO.read(file);
        IIOImage img = new IIOImage(bi,null,null);
        JP2ImageWriterSpi jp2Spi = new JP2ImageWriterSpi();
        if(jp2Spi.canEncodeImage(bi)) {
            File f = new File(System.getProperty("user.home") + "/Desktop"+"\\1_8bit_component_256entrypalette_1.jp2");
            ImageWriterPlugin imgWriter = new ImageWriterPlugin(jp2Spi);
            imgWriter.setOutput(f);
            imgWriter.setNumberResolution(10);
            imgWriter.write(null, img, null);
        }
    }
    @Test
    public void testImageWIthBitDepthToHigh()throws IOException{


        BufferedImage buffImg = new BufferedImage(240, 240, BufferedImage.TYPE_INT_RGB);
        IIOImage img = new IIOImage(buffImg,null,null);
        JP2ImageWriterSpi jp2Spi = new JP2ImageWriterSpi();
        if(jp2Spi.canEncodeImage(buffImg)){
            File f = new File(System.getProperty("user.home") + "/Desktop"+"\\1_8bit_component_256entrypalette_3.jp2");
            ImageWriterPlugin imgWriter = new ImageWriterPlugin(jp2Spi);
            imgWriter.setOutput(f);
            imgWriter.setNumberResolution(10);
            imgWriter.write(null,img,null);
        }

    }
    @Test
    public void testImageWriterWithDefaultResolution() throws IOException {

        final File file = TestUtil.getTestFile(productsFolder + "1_8bit_component_256entrypalette.tif");
        BufferedImage bi = ImageIO.read(file);
        IIOImage img = new IIOImage(bi,null,null);
        JP2ImageWriterSpi jp2Spi = new JP2ImageWriterSpi();
        if(jp2Spi.canEncodeImage(bi)) {
            final File f = new File(System.getProperty("user.home") + "/Desktop"+"\\1_8bit_component_256entrypalette_2.jp2");
            ImageWriterPlugin imgWriter = new ImageWriterPlugin(jp2Spi);
            imgWriter.setOutput(f);
            imgWriter.write(null, img, null);
        }
    }
    @Test
    public void testImageWriterWithMultipleBands() throws IOException {

        final File file = TestUtil.getTestFile(productsFolder + "3_8bit_components_srgb.tif");

        BufferedImage bi = ImageIO.read(file);
        IIOImage img = new IIOImage(bi,null,null);
        JP2ImageWriterSpi jp2Spi = new JP2ImageWriterSpi();
        if(jp2Spi.canEncodeImage(bi)) {
            File f = new File(System.getProperty("user.home") + "/Desktop"+"\\3_8bit_components_srgb_1.jp2");
            ImageWriterPlugin imgWriter = new ImageWriterPlugin(jp2Spi);
            imgWriter.setOutput(f);
            imgWriter.setNumberResolution(10);
            imgWriter.write(null, img, null);
        }
    }
    @Test
    public void testImageWriterWithSetNumResolutions() throws IOException {

        final File file = TestUtil.getTestFile(productsFolder + "3_8bit_components_srgb.tif");;

        BufferedImage bi = ImageIO.read(file);
        IIOImage img = new IIOImage(bi,null,null);
        JP2ImageWriterSpi jp2Spi = new JP2ImageWriterSpi();
        if(jp2Spi.canEncodeImage(bi)) {
            File f = new File(System.getProperty("user.home") + "/Desktop"+"\\3_8bit_components_srgb_2.jp2");
            ImageWriterPlugin imgWriter = new ImageWriterPlugin(jp2Spi);
            imgWriter.setOutput(f);
            imgWriter.setNumberResolution(6);
            imgWriter.write(null, img, null);
        }
    }
}
