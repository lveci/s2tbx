package org.esa.s2tbx.imagewriter;

import javax.imageio.metadata.IIOMetadataFormat;

/**
 * Created by rdumitrascu on 11/28/2016.
 */
public class JP2StreamMetadataFormat extends JP2MetadataFormat {

    private static JP2StreamMetadataFormat theInstance = null;

    private JP2StreamMetadataFormat() {
        super(JP2Format._nativeStreamMetadataFormatName,
                CHILD_POLICY_SEQUENCE);
        addStreamElements(getRootName());
    }
    public static synchronized IIOMetadataFormat getInstance() {
        if (theInstance == null) {
            theInstance = new JP2StreamMetadataFormat();
        }
        return theInstance;
    }
}
