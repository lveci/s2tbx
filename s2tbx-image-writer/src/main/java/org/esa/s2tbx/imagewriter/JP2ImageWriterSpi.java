package org.esa.s2tbx.imagewriter;

import javax.imageio.IIOException;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;
import java.awt.image.BufferedImage;
import java.awt.image.SampleModel;
import java.io.File;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * Jp2ImageWriterSpi class
 *
 * Created by rdumitrascu on 11/23/2016.
 */
public class JP2ImageWriterSpi extends ImageWriterSpi {
    private static final Logger logger = Logger.getLogger(JP2ImageWriterSpi.class.getName());

    public JP2ImageWriterSpi() {
        super(JP2Format._vendor,JP2Format._version,JP2Format._names, JP2Format._suffixes, JP2Format._MIMEtypes,
                JP2Format. _writerClassName,  new Class[] { File.class }, JP2Format._readerSpiNames,
                JP2Format. _supportsStandardStreamMetadataFormat, JP2Format._nativeStreamMetadataFormatName,
                JP2Format._nativeStreamMetadataFormatClassName, JP2Format._extraStreamMetadataFormatNames,
                JP2Format._extraStreamMetadataFormatClassNames, JP2Format._supportsStandardImageMetadataFormat,
                JP2Format._nativeImageMetadataFormatName, JP2Format._nativeImageMetadataFormatClassName,
                JP2Format._extraImageMetadataFormatNames, JP2Format. _extraImageMetadataFormatClassNames);
    }

    /**
     *
     * @param locale
     * @return
     */
    @Override
    public String getDescription(Locale locale) {
        return "Standard JPEG2000 Image Writer";
    }

    /**
     * Determine if the input image can be encoded
     * @param type
     * @return
     */
    public boolean canEncodeImage(ImageTypeSpecifier type){
        int bands = type.getNumBands();
         if(bands != 1 && bands != 3){
             logger.warning("Wrong number of bands");
             return false;
         }
        SampleModel sampleModel = type.getSampleModel();
        // Find the maximum bit depth across all channels
        int[] sampleSize = sampleModel.getSampleSize();
        int bitDepth = sampleSize[0];
        for (int i = 1; i < sampleSize.length; i++) {
            if (sampleSize[i] > bitDepth) {
                bitDepth = sampleSize[i];
            }
        }
        // Ensure bitDepth is between 1 and 8
        if (bitDepth < 1 || bitDepth > 16) {
            logger.warning("wrong bitDepth for input image");
            return false;
        }
        return true;
    }

    /**
     *
     * @param extension
     * @return returns a new instance of ImageWriterPlugin
     * @throws IIOException
     */
    @Override
    public ImageWriter createWriterInstance(Object extension) throws IIOException {
        return new ImageWriterPlugin(this);
    }

}
