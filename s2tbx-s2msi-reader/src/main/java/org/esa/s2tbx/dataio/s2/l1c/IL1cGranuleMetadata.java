package org.esa.s2tbx.dataio.s2.l1c;

import org.esa.s2tbx.dataio.s2.S2Metadata;
import org.esa.s2tbx.dataio.s2.S2SpatialResolution;
import org.esa.snap.core.datamodel.MetadataElement;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

/**
 * Created by obarrile on 30/09/2016.
 */
public interface IL1cGranuleMetadata {

    S2Metadata.ProductCharacteristics getTileProductOrganization();
    Map<S2SpatialResolution, L1cMetadata.TileGeometry> getTileGeometries();
    String getTileID();
    String getHORIZONTAL_CS_CODE();
    String getHORIZONTAL_CS_NAME();
    int getAnglesResolution();


    L1cMetadata.AnglesGrid getSunGrid();
    L1cMetadata.AnglesGrid[] getViewingAnglesGrid();
    S2Metadata.MaskFilename[] getMasks(Path path);
    MetadataElement getMetadataElement();
    MetadataElement getSimplifiedMetadataElement();

    void updateName();
}
