package org.esa.s2tbx.s2msi.aerosol;

import com.bc.ceres.core.ProgressMonitor;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.FlagCoding;
import org.esa.snap.core.datamodel.Mask;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductNodeGroup;
import org.esa.snap.core.gpf.Operator;
import org.esa.snap.core.gpf.OperatorException;
import org.esa.snap.core.gpf.OperatorSpi;
import org.esa.snap.core.gpf.Tile;
import org.esa.snap.core.gpf.annotations.OperatorMetadata;
import org.esa.snap.core.gpf.annotations.Parameter;
import org.esa.snap.core.gpf.annotations.SourceProduct;
import org.esa.snap.core.gpf.annotations.TargetProduct;
import org.esa.snap.core.util.Guardian;
import org.esa.snap.core.util.ProductUtils;

import java.awt.Rectangle;

/**
 * Operator for upscaling in aerosol retrieval from S2 MSI.
 *
 * @author olafd
 */
@OperatorMetadata(alias = "AerosolRetrieval.S2.Upscaling",
        description = "Operator for upscaling in aerosol retrieval from S2 MSI.",
        authors = "Olaf Danne, Marco Zuehlke, Grit Kirches, Andreas Heckel",
        internal = true,
        version = "1.0",
        copyright = "(C) 2010, 2016 by University Swansea and Brockmann Consult")
public class S2AerosolUpscaleOp extends Operator {

    @SourceProduct
    private Product lowresProduct;

    @SourceProduct
    private Product hiresProduct;

    @TargetProduct
    private Product targetProduct;

    @Parameter(defaultValue = "9")
    private int scale;
    private int offset;
    private int sourceRasterWidth;
    private int sourceRasterHeight;

    @Override
    public void initialize() throws OperatorException {
        sourceRasterWidth = lowresProduct.getSceneRasterWidth();
        sourceRasterHeight = lowresProduct.getSceneRasterHeight();
        int targetWidth = hiresProduct.getSceneRasterWidth();
        int targetHeight = hiresProduct.getSceneRasterHeight();

        offset = scale / 2;

        String targetProductName = lowresProduct.getName();
        String targetProductType = lowresProduct.getProductType();
        targetProduct = new Product(targetProductName, targetProductType, targetWidth, targetHeight);
        targetProduct.setStartTime(hiresProduct.getStartTime());
        targetProduct.setEndTime(hiresProduct.getEndTime());
        targetProduct.setPointingFactory(hiresProduct.getPointingFactory());
        ProductUtils.copyMetadata(lowresProduct, targetProduct);
        ProductUtils.copyTiePointGrids(hiresProduct, targetProduct);
        ProductUtils.copyGeoCoding(hiresProduct, targetProduct);
        copyBands(lowresProduct, targetProduct);
        for (int i = 0; i < lowresProduct.getMaskGroup().getNodeCount(); i++) {
            Mask lowresMask = lowresProduct.getMaskGroup().get(i);
            targetProduct.getMaskGroup().add(lowresMask);
        }

        setTargetProduct(targetProduct);
    }

    @Override
    public void computeTile(Band targetBand, Tile targetTile, ProgressMonitor pm) throws OperatorException {
        Rectangle tarRec = targetTile.getRectangle();
        String targetBandName = targetBand.getName();
        final Rectangle srcRec = calcSourceRectangle(tarRec);

        Band sourceBand = lowresProduct.getBand(targetBandName);
        Tile sourceTile = getSourceTile(sourceBand, srcRec);

        if (!targetBand.isFlagBand()) {
            upscaleTileBilinear(sourceTile, targetTile, tarRec);
        } else {
            upscaleFlagCopy(sourceTile, targetTile, tarRec);
        }
    }

    private Rectangle calcSourceRectangle(Rectangle tarRec) {
        int srcX = (tarRec.x - offset) / scale;
        int srcY = (tarRec.y - offset) / scale;
        int srcWidth = tarRec.width / scale + 2;
        int srcHeight = tarRec.height / scale + 2;
        if (srcX >= sourceRasterWidth - 1) {
            srcX = sourceRasterWidth - 2;
            srcWidth = 2;
        }
        if (srcY >= sourceRasterHeight - 1) {
            srcY = sourceRasterHeight - 2;
            srcHeight = 2;
        }
        if (srcX + srcWidth > sourceRasterWidth) {
            srcWidth = sourceRasterWidth - srcX;
        }
        if (srcY + srcHeight > sourceRasterHeight) {
            srcHeight = sourceRasterHeight - srcY;
        }
        return new Rectangle(srcX, srcY, srcWidth, srcHeight);
    }

    private void copyBands(Product sourceProduct, Product targetProduct) {
        Guardian.assertNotNull("source", sourceProduct);
        Guardian.assertNotNull("target", targetProduct);
        ProductNodeGroup<FlagCoding> targetFCG = targetProduct.getFlagCodingGroup();

        for (int iBand = 0; iBand < sourceProduct.getNumBands(); iBand++) {
            Band sourceBand = sourceProduct.getBandAt(iBand);
            if (!targetProduct.containsBand(sourceBand.getName())) {
                Band targetBand = copyBandScl(sourceBand.getName(), sourceProduct, sourceBand.getName(), targetProduct);
                if (sourceBand.isFlagBand()) {
                    FlagCoding flgCoding = sourceBand.getFlagCoding();
                    if (!targetFCG.contains(flgCoding.getName())) {
                        ProductUtils.copyFlagCoding(flgCoding, targetProduct);
                    }
                    if (targetBand != null) {
                        targetBand.setSampleCoding(targetFCG.get(flgCoding.getName()));
                    }
                }
            }
        }
    }

    /**
     * Copies the named band from the source product to the target product.
     *
     * @param sourceBandName the name of the band to be copied.
     * @param sourceProduct  the source product.
     * @param targetBandName the name of the band copied.
     * @param targetProduct  the target product.
     * @return the copy of the band, or <code>null</code> if the sourceProduct does not contain a band with the given name.
     */
    private Band copyBandScl(String sourceBandName, Product sourceProduct,
                             String targetBandName, Product targetProduct) {
        Guardian.assertNotNull("sourceProduct", sourceProduct);
        Guardian.assertNotNull("targetProduct", targetProduct);

        if (sourceBandName == null || sourceBandName.length() == 0) {
            return null;
        }
        final Band sourceBand = sourceProduct.getBand(sourceBandName);
        if (sourceBand == null) {
            return null;
        }
        Band targetBand = new Band(targetBandName,
                                   sourceBand.getDataType(),
                                   targetProduct.getSceneRasterWidth(),
                                   targetProduct.getSceneRasterHeight());
        ProductUtils.copyRasterDataNodeProperties(sourceBand, targetBand);
        targetProduct.addBand(targetBand);
        return targetBand;
    }

    private void upscaleTileBilinear(Tile srcTile, Tile tarTile, Rectangle tarRec) {

        final int tarX = tarRec.x;
        final int tarY = tarRec.y;
        final int tarWidth = tarRec.width;
        final int tarHeight = tarRec.height;
        final float noData = (float) tarTile.getRasterDataNode().getGeophysicalNoDataValue();

        for (int iTarY = tarY; iTarY < tarY + tarHeight; iTarY++) {
            int iSrcY = (iTarY - offset) / scale;
            if (iSrcY >= srcTile.getMaxY()) {
                iSrcY = srcTile.getMaxY() - 1;
            }
            float yFrac = (float) (iTarY - offset) / scale - iSrcY;
            yFrac = Math.min(1f, Math.max(0f, yFrac));
            for (int iTarX = tarX; iTarX < tarX + tarWidth; iTarX++) {
                checkForCancellation();
                int iSrcX = (iTarX - offset) / scale;
                if (iSrcX >= srcTile.getMaxX()) {
                    iSrcX = srcTile.getMaxX() - 1;
                }
                float xFrac = (float) (iTarX - offset) / scale - iSrcX;
                xFrac = Math.min(1f, Math.max(0f, xFrac));

                float result = noData;
                try {
                    result = interpolateBilinear(srcTile, noData, xFrac, yFrac, iSrcX, iSrcY);
                } catch (Exception ex) {
                    System.err.println(iTarX + " / " + iTarY);
                    System.err.println(ex.getMessage());
                } finally {
                    tarTile.setSample(iTarX, iTarY, result);
                }
            }
        }
    }

    private float interpolateBilinear(Tile srcTile, float nodataValue, float xFrac, float yFrac, int x, int y) {
        float value = srcTile.getSampleFloat(x, y);
        if (Double.compare(nodataValue, value) == 0) {
            return nodataValue;
        }
        float result = (1.0f - xFrac) * (1.0f - yFrac) * value;

        value = srcTile.getSampleFloat(x + 1, y);
        if (Double.compare(nodataValue, value) == 0) {
            return nodataValue;
        }
        result += (xFrac) * (1.0f - yFrac) * value;

        value = srcTile.getSampleFloat(x, y + 1);
        if (Double.compare(nodataValue, value) == 0) {
            return nodataValue;
        }
        result += (1.0f - xFrac) * (yFrac) * value;

        value = srcTile.getSampleFloat(x + 1, y + 1);
        if (Double.compare(nodataValue, value) == 0) {
            return nodataValue;
        }
        result += (xFrac) * (yFrac) * value;

        return result;
    }

    private void upscaleFlagCopy(Tile srcTile, Tile tarTile, Rectangle tarRec) {

        final int tarX = tarRec.x;
        final int tarY = tarRec.y;
        final int tarWidth = tarRec.width;
        final int tarHeight = tarRec.height;

        for (int iTarY = tarY; iTarY < tarY + tarHeight; iTarY++) {
            // int iSrcY = (iTarY - offset) / scale;
            int iSrcY = (iTarY) / scale;
            if (iSrcY >= srcTile.getMaxY()) {
                iSrcY = srcTile.getMaxY() - 1;
            }
            for (int iTarX = tarX; iTarX < tarX + tarWidth; iTarX++) {
                checkForCancellation();
                // int iSrcX = (iTarX - offset) / scale;
                int iSrcX = (iTarX) / scale;
                if (iSrcX >= srcTile.getMaxX()) {
                    iSrcX = srcTile.getMaxX() - 1;
                }
                tarTile.setSample(iTarX, iTarY, srcTile.getSampleInt(iSrcX, iSrcY));
            }
        }
    }

    public static class Spi extends OperatorSpi {
        public Spi() {
            super(S2AerosolUpscaleOp.class);
        }
    }
}
