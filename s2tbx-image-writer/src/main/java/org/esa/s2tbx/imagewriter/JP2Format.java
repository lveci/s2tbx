package org.esa.s2tbx.imagewriter;

/**
 * A class containing JPEG2000-related constants and  definitions needed in instantiate
 * JP2ImageWriterSpi
 *
 * Created by rdumitrascu on 11/23/2016.
 */
public class JP2Format {

    // Spi initialization stuff
    public static final String _vendor = "s2tbx";
    public static final String _version = "1.0";
    // Names of the formats we can read or write
    public static final String [] _names = {"JPEG2000", "jpeg2000", "JP2", "jp2"};
    public static final String [] _suffixes = {"jp2", "jpeg2000"};
    public static final String [] _MIMEtypes = {"image/jpeg2000"};
    public static final String _nativeImageMetadataFormatName =
            "javax_imageio_jpeg_image_1.0";
    public static final String _nativeStreamMetadataFormatName =
            "javax_imageio_jpeg_stream_1.0";
    public static final String   _nativeImageMetadataFormatClassName =
            "org.esa.s2tbx.imagewriter.JP2ImageMetadataFormat";
    public static final String   _nativeStreamMetadataFormatClassName  =
            "org.esa.s2tbx.imagewriter.JP2StreamMetadataFormat";
    public static final boolean  _supportsStandardStreamMetadataFormat = true;
    public static final String[] _extraStreamMetadataFormatNames       = null;
    public static final String[] _extraStreamMetadataFormatClassNames  = null;
    public static final boolean  _supportsStandardImageMetadataFormat = true;
    public static final String[] _extraImageMetadataFormatNames       = null;
    public static final String[] _extraImageMetadataFormatClassNames  = null;
    public static final String   _writerClassName = "org.esa.s2tbx.imagewriter.ImageWriterPlugin";
    public static final String[] _readerSpiNames = null;

}
