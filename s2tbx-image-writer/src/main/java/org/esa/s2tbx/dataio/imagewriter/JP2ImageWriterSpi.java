package org.esa.s2tbx.dataio.imagewriter;

import javax.imageio.IIOException;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;
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
        super(JP2FormatConstants._vendor, JP2FormatConstants._version, JP2FormatConstants._names, JP2FormatConstants._suffixes, JP2FormatConstants._MIMEtypes,
                JP2FormatConstants._writerClassName,  new Class[] { File.class }, JP2FormatConstants._readerSpiNames,
                JP2FormatConstants._supportsStandardStreamMetadataFormat, JP2FormatConstants._nativeStreamMetadataFormatName,
                JP2FormatConstants._nativeStreamMetadataFormatClassName, JP2FormatConstants._extraStreamMetadataFormatNames,
                JP2FormatConstants._extraStreamMetadataFormatClassNames, JP2FormatConstants._supportsStandardImageMetadataFormat,
                JP2FormatConstants._nativeImageMetadataFormatName, JP2FormatConstants._nativeImageMetadataFormatClassName,
                JP2FormatConstants._extraImageMetadataFormatNames, JP2FormatConstants._extraImageMetadataFormatClassNames);
    }

    @Override
    public String getDescription(Locale locale) {
        return "Sentinel 2 Toolbox JPEG2000 Image Writer";
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
        // Ensure bitDepth is between 1 and 16
        if (bitDepth < 1 || bitDepth > 16) {
            logger.warning("wrong bitDepth for input image");
            return false;
        }
        return true;
    }

    @Override
    public ImageWriter createWriterInstance(Object extension) throws IIOException {
        return new JP2ImageWriter();
    }

}
