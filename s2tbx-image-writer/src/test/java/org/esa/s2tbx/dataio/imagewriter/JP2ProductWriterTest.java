package org.esa.s2tbx.dataio.imagewriter;

import com.bc.ceres.core.ProgressMonitor;
import junit.framework.TestCase;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import java.io.File;
import java.io.IOException;


/**
 * Created by rdumitrascu on 1/5/2017.
 */
public class JP2ProductWriterTest extends TestCase {
    private static final String FILENAME = "temp.jp2";
    private JP2ProductWriter _productWriter;
    private Product _product;

    @Override
    protected void setUp() throws Exception {
        new File(FILENAME).delete();

        _productWriter = new JP2ProductWriter(new JP2ProductWriterPlugIn());

        _product = new Product("temp", "type", 3, 3);
        _product.addBand("band_01", ProductData.TYPE_UINT16);
        fillBandWithData(_product.getBand("band_01"), 1);
    }

    @Override
    protected void tearDown() throws Exception {
        _productWriter.close();
        new File(FILENAME).delete();
    }

    public void testJP2ProductWriterCreation() {
        final JP2ProductWriter productWriter = new JP2ProductWriter(new JP2ProductWriterPlugIn());
        assertNotNull(productWriter.getWriterPlugIn());
    }

    public void testThatStringIsAValidOutput() throws IOException {
        _productWriter.writeProductNodes(_product, FILENAME);
    }

    public void testThatFileIsAValidOutput() throws IOException {
        _productWriter.writeProductNodes(_product, new File(FILENAME));
    }

    public void testWriteProductNodes_ChangeFileSize() throws IOException {
        _productWriter.writeProductNodes(_product, FILENAME);
        assertTrue(new File(FILENAME).length() == 0);
        writeBand(_product);
        _productWriter.close();

        assertTrue(new File(FILENAME).length() > 0);
    }

    private void fillBandWithData(final Band band, final int start)throws IOException {
        final ProductData data = band.createCompatibleRasterData();
        for (int i = 0; i < band.getRasterWidth() * band.getRasterHeight(); i++) {
            data.setElemIntAt(i, start + i);
        }
        band.setData(data);
    }

    private void writeBand(final Product product) throws IOException {
        final int width = product.getSceneRasterWidth();
        final int height = product.getSceneRasterHeight();
        final Band[] bands = product.getBands();
        for (Band band : bands) {
            _productWriter.writeBandRasterData(band, 0, 0, width, height, band.getData(), ProgressMonitor.NULL);
        }
    }
}
