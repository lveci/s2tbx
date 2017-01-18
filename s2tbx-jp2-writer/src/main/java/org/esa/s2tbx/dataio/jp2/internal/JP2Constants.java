package org.esa.s2tbx.dataio.jp2.internal;

import java.io.File;

/**
 * A class containing JPEG2000-related constants and  definitions needed fot the JP2 Writer
 *
 * Created by Razvan Dumitrascu on 11/23/2016.
 */
public class JP2Constants {

    //JP2ProductWriterplugin constants
    public static final String DESCRIPTION = "JPEG-2000 Product";
    public static final String[] FILE_EXTENSIONS = {".jp2", ".jpeg2000"};
    public static final String[] FORMAT_NAMES = new String[] { "JPEG2000" };
    public static final Class[] OUTPUT_TYPES = new Class[] { String.class, File.class };
}
