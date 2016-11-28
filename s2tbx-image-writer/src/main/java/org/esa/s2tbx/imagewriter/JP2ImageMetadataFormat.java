package org.esa.s2tbx.imagewriter;

import com.sun.imageio.plugins.jpeg.JPEG;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadataFormat;

/**
 * Created by rdumitrascu on 11/28/2016.
 */
public class JP2ImageMetadataFormat  extends JP2MetadataFormat {
    private static JP2ImageMetadataFormat theInstance = null;

    JP2ImageMetadataFormat() {
        super(JP2Format._nativeImageMetadataFormatName,
                CHILD_POLICY_ALL);
        //ToDO
    }
    public boolean canNodeAppear(String elementName,
                                 ImageTypeSpecifier imageType) {

        //TODO
        return false;
    }

    public static synchronized IIOMetadataFormat getInstance() {
        if (theInstance == null) {
            theInstance = new JP2ImageMetadataFormat();
        }
        return theInstance;
    }
}
