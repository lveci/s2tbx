/*
 *
 *  * Copyright (C) 2016 CS ROMANIA
 *  *
 *  * This program is free software; you can redistribute it and/or modify it
 *  * under the terms of the GNU General Public License as published by the Free
 *  * Software Foundation; either version 3 of the License, or (at your option)
 *  * any later version.
 *  * This program is distributed in the hope that it will be useful, but WITHOUT
 *  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  * more details.
 *  *
 *  * You should have received a copy of the GNU General Public License along
 *  *  with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package org.esa.s2tbx.radiometry;

import com.bc.ceres.core.ProgressMonitor;
import org.esa.s2tbx.radiometry.annotations.BandParameter;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.gpf.OperatorException;
import org.esa.snap.core.gpf.OperatorSpi;
import org.esa.snap.core.gpf.Tile;
import org.esa.snap.core.gpf.annotations.OperatorMetadata;
import org.esa.snap.core.gpf.annotations.Parameter;
import org.esa.snap.engine_utilities.eo.Constants;

import java.awt.*;
import java.util.Map;

@OperatorMetadata(
        alias = "NdwiOp",
        version="1.0",
        category = "Optical/Thematic Land Processing/Water Radiometric Indices",
        description = "The Normalized Difference Water Index was developed for the extraction of water features",
        authors = "Dragos Mihailescu",
        copyright = "Copyright (C) 2016 by CS ROMANIA")
public class NdwiOp extends BaseIndexOp{

    // constants
    public static final String BAND_NAME = "ndwi";

    @Parameter(label = "MIR factor", defaultValue = "1.0F", description = "The value of the MIR source band is multiplied by this value.")
    private float mirFactor;

    @Parameter(label = "NIR factor", defaultValue = "1.0F", description = "The value of the NIR source band is multiplied by this value.")
    private float nirFactor;

    @Parameter(label = "MIR source band",
            description = "The mid-infrared band for the NDWI computation. If not provided," +
                    " the operator will try to find the best fitting band.",
            rasterDataNodeType = Band.class)
    @BandParameter(minWavelength = 3000, maxWavelength = 8000)
    private String mirSourceBandStr;

    @Parameter(label = "NIR source band",
            description = "The near-infrared band for the NDWI computation. If not provided," +
                    " the operator will try to find the best fitting band.",
            rasterDataNodeType = Band.class)
    @BandParameter(minWavelength = 800, maxWavelength = 900)
    private String nirSourceBandStr;

    public NdwiOp() {
        super();
        this.lowValueThreshold = -1f;
        this.highValueThreshold = 1f;
    }

    @Override
    public String getBandName() {
        return BAND_NAME;
    }

    @Override
    public void computeTileStack(Map<Band, Tile> targetTiles, Rectangle rectangle, ProgressMonitor pm) throws OperatorException {

        pm.beginTask("Computing NDWI", rectangle.height);
        try {
            final int xMin = rectangle.x;
            final int xMax = rectangle.x + rectangle.width;
            final int yMin = rectangle.y;
            final int yMax = rectangle.y + rectangle.height;

            final Band mirSourceBand = sourceProduct.getBand(mirSourceBandStr);
            final double mirBandNoDataValue = mirSourceBand.scale(mirSourceBand.getNoDataValue());
            final Tile mirTile = getSourceTile(mirSourceBand, rectangle);

            final Band nirSourceBand = sourceProduct.getBand(nirSourceBandStr);
            final double nirBandNoDataValue = nirSourceBand.scale(nirSourceBand.getNoDataValue());
            final Tile nirTile = getSourceTile(nirSourceBand, rectangle);

            final Band ndwiBand = targetProduct.getBand(BAND_NAME);
            ndwiBand.setNoDataValueUsed(true);
            ndwiBand.setNoDataValue(Constants.NO_DATA_VALUE);
            final Tile ndwiTile = targetTiles.get(ndwiBand);

            final Band ndwiFlagsBand = targetProduct.getBand(FLAGS_BAND_NAME);
            //ndwiFlagsBand.setNoDataValueUsed(true);
            //ndwiFlagsBand.setNoDataValue(Constants.NO_DATA_VALUE);
            final Tile ndwiFlagsTile = targetTiles.get(ndwiFlagsBand);

            double ndwiValue;

            for (int y = yMin; y < yMax; y++) {
                for (int x = xMin; x < xMax; x++) {

                    double nir = nirTile.getSampleDouble(x, y);
                    double mir = mirTile.getSampleDouble(x, y);
                    if (nir != nirBandNoDataValue && mir != mirBandNoDataValue) {
                        nir = nirFactor * nir;
                        mir = mirFactor * mir;
                        ndwiValue = (nir - mir) / (nir + mir);
                        ndwiTile.setSample(x, y, computeFlag(x, y, (float)ndwiValue, ndwiFlagsTile));
                    } else {
                        ndwiTile.setSample(x, y, computeFlag(x, y, (float)Constants.NO_DATA_VALUE, ndwiFlagsTile));
                    }
                }
                checkForCancellation();
                pm.worked(1);
            }
        } finally {
            pm.done();
        }
    }

    public static class Spi extends OperatorSpi {

        public Spi() {
            super(NdwiOp.class);
        }

    }

}
