package org.esa.s2tbx.dataio.imagewriter;

import javax.imageio.metadata.IIOMetadataFormat;

/**
 * Created by rdumitrascu on 12/9/2016.
 */
public class JP2ImageMetadataFormat extends JP2MetadataFormat{

    private static JP2ImageMetadataFormat theInstance = null;

    JP2ImageMetadataFormat() {
        super(JP2FormatConstants._nativeImageMetadataFormatName,
                CHILD_POLICY_SEQUENCE);
        addStreamElements(getRootName());
    }
    public static synchronized IIOMetadataFormat getInstance() {
        if (theInstance == null) {
            theInstance = new JP2ImageMetadataFormat();
        }
        return theInstance;
    }
}
