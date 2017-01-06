package org.esa.s2tbx.dataio.imagewriter;

import com.bc.ceres.core.ProgressMonitor;
import com.sun.media.jai.codec.ByteArraySeekableStream;
import org.esa.s2tbx.dataio.jp2.JP2ProductReader;
import org.esa.s2tbx.dataio.jp2.JP2ProductReaderPlugin;
import org.esa.snap.core.dataio.ProductIO;
import org.esa.snap.core.datamodel.*;
import org.esa.snap.core.image.ImageManager;
import org.esa.snap.core.util.io.FileUtils;
import org.esa.snap.dataio.geotiff.GeoTiffProductReader;
import org.geotools.referencing.CRS;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNotSame;

/**
 * Created by rdumitrascu on 1/6/2017.
 */
public class JP2ProdctWriteReadTest {

    private final static File TEST_DIR = new File("test_data");

    private static final String WGS_84_UTM_ZONE_31N = "EPSG:32631";

    private JP2ProductReader reader;
    private Product outProduct;
    private ByteArrayOutputStream outputStream;
    private File location;
    @Before
    public void setup() {
        if (!TEST_DIR.mkdirs()) {
            fail("unable to create test directory");
        }
        outputStream = new ByteArrayOutputStream();
        final int width = 14;
        final int height = 14;
        outProduct = new Product("Name", "JPEG-2000", width, height);
        final Band bandInt16 = outProduct.addBand("band_1", ProductData.TYPE_INT16);
        bandInt16.setDataElems(createShortData(getProductSize(), 23));
        ImageManager.getInstance().getSourceImage(bandInt16, 0);
    }

    @After
    public void tearDown() {
        if (!FileUtils.deleteTree(TEST_DIR)) {
            fail("unable to delete test directory");
        }
    }
    @Test
    public void testWriteReadBeamMetadata() throws IOException {
        final Band expectedBand = outProduct.getBand("band_1");
        expectedBand.setLog10Scaled(false);
        expectedBand.setNoDataValueUsed(false);

        final Product inProduct = writeReadProduct();
        assertEquals(outProduct.getName(), inProduct.getName());
        assertEquals(outProduct.getProductType(), inProduct.getProductType());
        assertEquals(outProduct.getNumBands(), inProduct.getNumBands());

        final Band actualBand = inProduct.getBandAt(0);
        assertEquals(expectedBand.getName(), actualBand.getName());
        assertEquals(expectedBand.getDataType(), actualBand.getDataType());
        assertEquals(expectedBand.isLog10Scaled(), actualBand.isLog10Scaled());
        assertEquals(expectedBand.isNoDataValueUsed(), actualBand.isNoDataValueUsed());

    }

    @Test
    public void testWriteReadVirtualBandIsNotExcludedInProduct() throws IOException {
        final VirtualBand virtualBand = new VirtualBand("band_2", ProductData.TYPE_UINT8,
                outProduct.getSceneRasterWidth(),
                outProduct.getSceneRasterHeight(), "X * Y");
        outProduct.addBand(virtualBand);
        final Product inProduct = writeReadProduct();

        try {
            assertEquals(2, inProduct.getNumBands());
            assertNotNull(inProduct.getBand("band_2"));
        } finally {
            inProduct.dispose();
        }
    }

    @Test
    public void testWriteReadUTMProjection() throws IOException, TransformException, FactoryException {
        setGeoCoding(outProduct, WGS_84_UTM_ZONE_31N);

        final Product inProduct = writeReadProduct();
        try {
            assertEquals(outProduct.getName(), inProduct.getName());
            assertEquals(outProduct.getProductType(), inProduct.getProductType());
            assertEquals(outProduct.getNumBands(), inProduct.getNumBands());
            assertEquals(outProduct.getBandAt(0).getName(), inProduct.getBandAt(0).getName());
            assertEquals(outProduct.getBandAt(0).getDataType(), inProduct.getBandAt(0).getDataType());
            assertEquals(outProduct.getBandAt(0).getScalingFactor(), inProduct.getBandAt(0).getScalingFactor(), 1.0e-6);
            assertEquals(outProduct.getBandAt(0).getScalingOffset(), inProduct.getBandAt(0).getScalingOffset(), 1.0e-6);
            assertEquals(location, inProduct.getFileLocation());
            assertNotNull(inProduct.getSceneGeoCoding());
            assertEquality(outProduct.getSceneGeoCoding(), inProduct.getSceneGeoCoding(), 2.0e-5f);
        } finally {
            inProduct.dispose();
        }
    }

    private void assertEquality(final GeoCoding gc1, final GeoCoding gc2, float accuracy) {
        assertNotNull(gc2);
        assertEquals(gc1.canGetGeoPos(), gc2.canGetGeoPos());
        assertEquals(gc1.isCrossingMeridianAt180(), gc2.isCrossingMeridianAt180());

        if (gc1 instanceof CrsGeoCoding) {
            assertEquals(CrsGeoCoding.class, gc2.getClass());
            CRS.equalsIgnoreMetadata(gc1, gc2);
        } else if (gc1 instanceof TiePointGeoCoding) {
            assertEquals(TiePointGeoCoding.class, gc2.getClass());
        }

        final int width = outProduct.getSceneRasterWidth();
        final int height = outProduct.getSceneRasterHeight();
        GeoPos geoPos1 = null;
        GeoPos geoPos2 = null;
        final String msgPattern = "%s at [%d,%d] is not equal:";
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                final PixelPos pixelPos = new PixelPos(i, j);
                geoPos1 = gc1.getGeoPos(pixelPos, geoPos1);
                geoPos2 = gc2.getGeoPos(pixelPos, geoPos2);
                assertEquals(String.format(msgPattern, "Latitude", i, j), geoPos1.lat, geoPos2.lat, accuracy);
                assertEquals(String.format(msgPattern, "Longitude", i, j), geoPos1.lon, geoPos2.lon, accuracy);
            }
        }
    }

    private static void setGeoCoding(Product product, String epsgCode) throws FactoryException, TransformException {
        final CoordinateReferenceSystem crs = CRS.decode(epsgCode, true);
        final Rectangle imageBounds = new Rectangle(product.getSceneRasterWidth(), product.getSceneRasterHeight());
        final AffineTransform imageToMap = new AffineTransform();
        imageToMap.translate(0.7, 0.8);
        imageToMap.scale(0.9, -0.8);
        imageToMap.translate(-0.5, -0.6);
        product.setSceneGeoCoding(new CrsGeoCoding(crs, imageBounds, imageToMap));
    }

    private static short[] createShortData(final int size, final int offset) {
        final short[] shorts = new short[size];
        for (int i = 0; i < shorts.length; i++) {
            shorts[i] = (short) (i + offset);
        }
        return shorts;
    }

    private static byte[] createByteData(final int size, final int offset) {
        final byte[] bytes = new byte[size];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) (i + offset);
        }
        return bytes;
    }

    private int getProductSize() {
        final int w = outProduct.getSceneRasterWidth();
        final int h = outProduct.getSceneRasterHeight();
        return w * h;
    }

    private Product writeReadProduct() throws IOException {
        location = new File(TEST_DIR, "test_product.jp2");

        final String JP2FormatName = JP2FormatConstants._formatNames[0];
        ProductIO.writeProduct(outProduct, location.getAbsolutePath(), JP2FormatName);

        return ProductIO.readProduct(location, JP2FormatName);
    }

}
