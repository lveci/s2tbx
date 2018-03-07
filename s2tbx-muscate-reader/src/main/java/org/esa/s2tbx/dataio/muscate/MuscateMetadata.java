package org.esa.s2tbx.dataio.muscate;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.esa.s2tbx.dataio.metadata.XmlMetadata;
import org.esa.s2tbx.dataio.metadata.XmlMetadataParser;
import org.esa.s2tbx.dataio.openjpeg.StackTraceUtils;
import org.esa.snap.core.datamodel.CrsGeoCoding;
import org.esa.snap.core.datamodel.MetadataAttribute;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.util.SystemUtils;
import org.geotools.graph.util.geom.Coordinate2D;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

import java.util.ArrayList;
import java.util.Arrays;

import static org.esa.snap.utils.DateHelper.parseDate;

/**
 * Created by obarrile on 26/01/2017.
 */
public class MuscateMetadata extends XmlMetadata {

    private ArrayList<MuscateImage> images;
    private ArrayList<MuscateMask> masks;
    private AnglesGrid[] viewingAnglesGrids;
    private AnglesGrid sunAnglesGrids;

    public static class MuscateMetadataParser extends XmlMetadataParser<MuscateMetadata> {

        public MuscateMetadataParser(Class metadataFileClass) {
            super(metadataFileClass);
        }

        @Override
        protected boolean shouldValidateSchema() {
            return false;
        }
    }

    public MuscateMetadata(String name) {
        super(name);
    }

    @Override
    public String getFileName() {
        return name;
    }

    @Override
    public String getProductName() {
        String name = getAttributeValue(MuscateConstants.PATH_SOURCE_ID, MuscateConstants.VALUE_NOT_AVAILABLE);
        rootElement.setDescription(name);
        return name;
    }

    public String getProductDescription() {
        String descr = getAttributeValue(MuscateConstants.PATH_SOURCE_DESCRIPTION, MuscateConstants.VALUE_NOT_AVAILABLE);
        if (MuscateConstants.VALUE_NOT_AVAILABLE.equals(descr)) {
            descr = getAttributeValue(MuscateConstants.PATH_SOURCE_ID, MuscateConstants.VALUE_NOT_AVAILABLE);
        }
        rootElement.setDescription(descr);
        return descr;
    }

    @Override
    public String getFormatName() {
        return getAttributeValue(MuscateConstants.PATH_METADATA_FORMAT, MuscateConstants.METADATA_MUSCATE);
    }

    @Override
    public String getMetadataProfile() {
        return getAttributeValue(MuscateConstants.PATH_METADATA_PROFILE, MuscateConstants.DISTRIBUTED);
    }

    @Override
    public int getRasterWidth() {
        if (width == 0) {
            String bestResolution = getBestResolutionString();
            try {
                width = getGeoposition(bestResolution).nCols;
            } catch (NumberFormatException e) {
                warn(MISSING_ELEMENT_WARNING, MuscateConstants.PATH_NCOLS);
            }
        }
        return width;
    }

    @Override
    public int getRasterHeight() {
        if (height == 0) {
            String bestResolution = getBestResolutionString();
            try {
                height = getGeoposition(bestResolution).nRows;
            } catch (NumberFormatException e) {
                warn(MISSING_ELEMENT_WARNING, MuscateConstants.PATH_NROWS);
            }
        }
        return height;
    }

    public double getBestResolution() {
        String bestResolutionId = getBestResolutionString();
        double bestResolution = 0;
        try {
            bestResolution = Math.abs(getGeoposition(bestResolutionId).xDim);
        } catch (NumberFormatException e) {
            warn(MISSING_ELEMENT_WARNING, MuscateConstants.PATH_NROWS);
        }
        return bestResolution;
    }

    @Override
    public String[] getRasterFileNames() {
        return getAttributeValues(MuscateConstants.PATH_IMAGE_FILE_LIST);
    }

    @Override
    public ProductData.UTC getProductStartTime() {
        //return the centerTime
        return getCenterTime();
    }

    @Override
    public ProductData.UTC getProductEndTime() {
        //return the centerTime
        return getCenterTime();
    }

    @Override
    public ProductData.UTC getCenterTime() {
        //computed the acquisition date
        String dateString = getAttributeValue(MuscateConstants.PATH_ACQUISITION_DATE, MuscateConstants.VALUE_NOT_DATE);
        return parseDate(dateString, MuscateConstants.DATE_FORMAT);
    }

    @Override
    public int getNumBands() {
        //compute the number of spectral bands
        int numBands = 0;
        String[] resolutionIds = getResolutionStrings();
        if (resolutionIds == null || resolutionIds.length == 0) {
            return numBands;
        }

        for (String resolutionId : resolutionIds) {
            numBands = numBands + getBandNames(resolutionId).size();
        }

        return numBands;
    }

    public synchronized ArrayList<MuscateImage> getImages() {
        if (images == null) {
            images = new ArrayList<>(13);
            MetadataElement imageListElement = getRootElement().getElement("Product_Organisation").getElement("Muscate_Product").getElement("Image_List");

            for (int i = 0; i < imageListElement.getNumElements(); i++) {
                MuscateImage muscateImage = new MuscateImage();
                MetadataElement imageElement = imageListElement.getElementAt(i);
                MetadataElement propertiesElement = imageElement.getElement("Image_Properties");
                muscateImage.nature = propertiesElement.getAttribute("NATURE").getData().getElemString();
                muscateImage.compression = propertiesElement.getAttribute("COMPRESSION").getData().getElemString();
                muscateImage.encoding = propertiesElement.getAttribute("ENCODING").getData().getElemString();
                muscateImage.endianness = propertiesElement.getAttribute("ENDIANNESS").getData().getElemString();
                muscateImage.format = propertiesElement.getAttribute("FORMAT").getData().getElemString();
                MetadataElement fileListElement = imageElement.getElement("Image_File_List");
                int numImages = fileListElement.getNumAttributes();
                for (int j = 0; j < numImages; j++) {
                    String path = fileListElement.getAttributeAt(j).getData().getElemString();
                    muscateImage.addMuscateImageFile(path);
                }
                images.add(muscateImage);
            }
        }
        return images;
    }

    public synchronized ArrayList<MuscateMask> getMasks() {
        if (masks == null) {
            masks = new ArrayList<>(13);
            MetadataElement maskListElement = getRootElement().getElement("Product_Organisation").getElement("Muscate_Product").getElement("Mask_List");

            for(int i=0 ; i< maskListElement.getNumElements(); i++) {
                MuscateMask muscateMask = new MuscateMask();
                MetadataElement imageElement = maskListElement.getElementAt(i);
                MetadataElement propertiesElement = imageElement.getElement("Mask_Properties");
                muscateMask.nature = propertiesElement.getAttribute("NATURE").getData().getElemString();
                muscateMask.encoding = propertiesElement.getAttribute("ENCODING").getData().getElemString();
                muscateMask.endianness = propertiesElement.getAttribute("ENDIANNESS").getData().getElemString();
                muscateMask.format = propertiesElement.getAttribute("FORMAT").getData().getElemString();
                MetadataElement fileListElement = imageElement.getElement("Mask_File_List");
                int numMasks = fileListElement.getNumAttributes();
                for (int j = 0 ; j < numMasks ; j++) {
                    String path = fileListElement.getAttributeAt(j).getData().getElemString();
                    muscateMask.addMuscateMaskFile(path);
                }
                masks.add(muscateMask);
            }

        }
        return masks;
    }


    public String getEPSG() {
        return getAttributeValue(MuscateConstants.PATH_CS_CODE, MuscateConstants.STRING_ZERO);
    }

    public String getAcquisitionDate() {
        return getAttributeValue(MuscateConstants.PATH_ACQUISITION_DATE, MuscateConstants.VALUE_NOT_DATE);
    }

    public String getDescription() {
        return getAttributeValue(MuscateConstants.PATH_DESCRIPTION, MuscateConstants.VALUE_NOT_AVAILABLE);
    }

    public Coordinate2D getUpperLeft() {
        double x = Double.parseDouble(getAttributeSiblingValue(MuscateConstants.PATH_GLOBAL_GEOPOSITIONING_POINT_NAME,"upperLeft",
                                                               MuscateConstants.PATH_GLOBAL_GEOPOSITIONING_POINT_X,MuscateConstants.STRING_ZERO));
        double y = Double.parseDouble(getAttributeSiblingValue(MuscateConstants.PATH_GLOBAL_GEOPOSITIONING_POINT_NAME,"upperLeft",
                                                               MuscateConstants.PATH_GLOBAL_GEOPOSITIONING_POINT_Y,MuscateConstants.STRING_ZERO));
        return new Coordinate2D(x, y);
    }

    public CrsGeoCoding getCrsGeoCoding () {
        Coordinate2D coordUpperLeft = getUpperLeft();
        CrsGeoCoding crsGeoCoding = null;
        String EPSG = getEPSG();
        try {
            crsGeoCoding = new CrsGeoCoding(CRS.decode("EPSG:" + EPSG),
                                            getRasterWidth(),
                                            getRasterHeight(),
                                            coordUpperLeft.x,
                                            coordUpperLeft.y,
                                            getBestResolution(),
                                            getBestResolution(),
                                            0.0, 0.0);
        } catch (FactoryException e) {
            e.printStackTrace();
        } catch (TransformException e) {
            e.printStackTrace();
        }
        return crsGeoCoding;
    }


    public double getWVCQuantificationValue() {
        String string = getAttributeValue(MuscateConstants.PATH_WVC_QUANTIFICATION,MuscateConstants.DEFAULT_WVC_QUANTIFICATION);
        double quantification;
        try {
            quantification = Double.parseDouble(string);
        } catch (Exception e) {
            quantification = Double.parseDouble(MuscateConstants.DEFAULT_WVC_QUANTIFICATION);
        }
        return quantification;
    }

    public double getWVCNoDataValue() {
        //TODO try to read it from metadata because it could change...
        return Double.parseDouble(MuscateConstants.DEFAULT_WVC_NODATA);
    }

    public double getAOTQuantificationValue() {
        String string = getAttributeValue(MuscateConstants.PATH_AOT_QUANTIFICATION, MuscateConstants.DEFAULT_AOT_QUANTIFICATION);
        double quantification;
        try {
            quantification = Double.parseDouble(string);
        } catch (Exception e) {
            quantification = Double.parseDouble(MuscateConstants.DEFAULT_AOT_QUANTIFICATION);
        }
        return quantification;
    }

    public double getAOTNoDataValue() {
        //TODO try to read it from metadata because it could change...
        return Double.parseDouble(MuscateConstants.DEFAULT_AOT_NODATA);
    }

    public double getReflectanceQuantificationValue() {
        String string = getAttributeValue(MuscateConstants.PATH_REFLECTANCE_QUANTIFICATION, MuscateConstants.DEFAULT_REFLECTANCE_QUANTIFICATION);
        double quantification;
        try {
            quantification = Double.parseDouble(string);
        } catch (Exception e) {
            quantification = Double.parseDouble(MuscateConstants.DEFAULT_REFLECTANCE_QUANTIFICATION);
        }
        return quantification;
    }

    public double getReflectanceNoDataValue() {
        //TODO try to read it from metadata because it could change...
        return Double.parseDouble(MuscateConstants.DEFAULT_REFLECTANCE_NODATA);
    }


    public String[] getResolutionStrings() {
        return getAttributeValues(MuscateConstants.PATH_GEOPOSITIONING_ID);
    }

    public ArrayList<String> getBandNames(String resolutionId) {

        ArrayList<String> bands = new ArrayList<>(13);
        MetadataElement bandGroupListElement = getRootElement().getElement("Product_Characteristics").getElement("Band_Group_List");

        for (int i = 0; i < bandGroupListElement.getNumElements(); i++) {
            MetadataElement element = bandGroupListElement.getElementAt(i);
            if (element.getAttribute("group_id").getData().getElemString().equals(resolutionId)) {
                MetadataElement bandListElement = element.getElement("Band_List");
                for (int j = 0; j < bandListElement.getNumAttributes(); j++) {
                    if (bandListElement.getAttributeAt(j).getName().equals("BAND_ID")) {
                        bands.add(bandListElement.getAttributeAt(j).getData().getElemString());
                    }
                }
            }
        }
        return bands;
    }


    public String[] getOrderedBandNames(String resolutionId) {

        ArrayList<String> bands = new ArrayList<>(13);
        MetadataElement bandGroupListElement = getRootElement().getElement("Product_Characteristics").getElement("Band_Group_List");

        for (int i = 0; i < bandGroupListElement.getNumElements(); i++) {
            MetadataElement element = bandGroupListElement.getElementAt(i);
            if (element.getAttribute("group_id").getData().getElemString().equals(resolutionId)) {
                MetadataElement bandListElement = element.getElement("Band_List");
                for (int j = 0; j < bandListElement.getNumAttributes(); j++) {
                    if (bandListElement.getAttributeAt(j).getName().equals("BAND_ID")) {
                        bands.add(bandListElement.getAttributeAt(j).getData().getElemString());
                    }
                }
            }
        }
        int totalBands = bands.size();
        String[] bandStrings = new String[totalBands];
        int index = 0;

        for (int i = 0; i < bandGroupListElement.getNumElements(); i++) {
            MetadataElement element = bandGroupListElement.getElementAt(i);
            if (element.getAttribute("group_id").getData().getElemString().equals(resolutionId)) {
                MetadataElement bandListElement = element.getElement("Band_List");
                for (int j = 0; j < bandListElement.getNumAttributes(); j++) {
                    if (bandListElement.getAttributeAt(j).getName().equals("BAND_ID")) {
                        bandStrings[index] = bandListElement.getAttributeAt(j).getData().getElemString();
                        index++;
                    }
                }
            }
        }

        return bandStrings;
    }

    public ArrayList<String> getBandNames() {

        ArrayList<String> bands = new ArrayList<>(17);
        MetadataElement bandGlobalListElement = getRootElement().getElement("Product_Characteristics").getElement("Band_Global_List");
        for (int j = 0; j < bandGlobalListElement.getNumAttributes(); j++) {
            if (bandGlobalListElement.getAttributeAt(j).getName().equals("BAND_ID")) {
                bands.add(bandGlobalListElement.getAttributeAt(j).getData().getElemString());
            }
        }
        return bands;
    }

    public String getBestResolutionString() {
        String[] resolutions = getResolutionStrings();
        String bestResolutionString = "";
        double minXDim = Double.MAX_VALUE;
        for (String resolution : resolutions) {
            Geoposition geopositionTemp = getGeoposition(resolution);
            if (Math.abs(geopositionTemp.xDim) < minXDim) {
                minXDim = Math.abs(geopositionTemp.xDim);
                bestResolutionString = geopositionTemp.id;
            }
        }
        return bestResolutionString;
    }

    public float getSolarIrradiance(String bandID) {
        float value = Float.parseFloat(getAttributeSiblingValue(MuscateConstants.PATH_SPECTRAL_BAND_INFORMATION_BAND, bandID,
                                                                MuscateConstants.PATH_SPECTRAL_BAND_INFORMATION_IRRADIANCE, MuscateConstants.STRING_ZERO));
        return value;
    }

    public float getCentralWavelength(String bandID) {
        float value = Float.parseFloat(getAttributeSiblingValue(MuscateConstants.PATH_SPECTRAL_BAND_INFORMATION_BAND, bandID,
                                                                MuscateConstants.PATH_SPECTRAL_BAND_INFORMATION_CENTRAL_WAVELENGTH, MuscateConstants.STRING_ZERO));
        return value;
    }

    public Geoposition getGeoposition(String resolution) {
        Geoposition geoposition = new Geoposition();
        try {
            geoposition.id = resolution;

            geoposition.ulx = Float.parseFloat(getAttributeSiblingValue(MuscateConstants.PATH_GEOPOSITIONING_ID, resolution,
                                                                        MuscateConstants.PATH_GEOPOSITIONING_ULX, null));
            geoposition.uly = Float.parseFloat(getAttributeSiblingValue(MuscateConstants.PATH_GEOPOSITIONING_ID, resolution,
                                                                        MuscateConstants.PATH_GEOPOSITIONING_ULY, null));
            geoposition.xDim = Float.parseFloat(getAttributeSiblingValue(MuscateConstants.PATH_GEOPOSITIONING_ID, resolution,
                                                                         MuscateConstants.PATH_GEOPOSITIONING_XDIM, null));
            geoposition.yDim = Float.parseFloat(getAttributeSiblingValue(MuscateConstants.PATH_GEOPOSITIONING_ID, resolution,
                                                                         MuscateConstants.PATH_GEOPOSITIONING_YDIM, null));
            geoposition.nRows = Integer.parseInt(getAttributeSiblingValue(MuscateConstants.PATH_GEOPOSITIONING_ID, resolution,
                                                                          MuscateConstants.PATH_GEOPOSITIONING_NROWS, null));
            geoposition.nCols = Integer.parseInt(getAttributeSiblingValue(MuscateConstants.PATH_GEOPOSITIONING_ID, resolution,
                                                                          MuscateConstants.PATH_GEOPOSITIONING_NCOLS, null));

        } catch (Exception e) {
            warn(MISSING_ELEMENT_WARNING, "No geoposition found");
            return null;
        }
        return geoposition;
    }

    public class Geoposition {
        public String id;
        public float ulx;
        public float uly;
        public float xDim;
        public float yDim;
        public int nRows;
        public int nCols;
    }

    public AnglesGrid getSunAnglesGrid() {
        if(sunAnglesGrids == null) {
            MetadataElement geometricElement = rootElement.getElement("Geometric_Informations");
            if (geometricElement == null) {
                return null;
            }
            MetadataElement tileAnglesElement = geometricElement.getElement("Angles_Grids_List");
            if (tileAnglesElement == null) {
                return null;
            }

            MetadataElement sunAnglesListElement = tileAnglesElement.getElement("Sun_Angles_Grids");
            if (sunAnglesListElement == null) {
                return null;
            }

            MetadataElement azimuthElement = sunAnglesListElement.getElement("Azimuth");
            if (azimuthElement == null) {
                return null;
            }
            MetadataElement valuesAzimuthElement = azimuthElement.getElement("Values_List");
            if (valuesAzimuthElement == null) {
                return null;
            }
            MetadataAttribute[] azAnglesAttributes = valuesAzimuthElement.getAttributes();
            if (azAnglesAttributes == null) {
                return null;
            }

            MetadataElement zenithElement = sunAnglesListElement.getElement("Zenith");
            if (zenithElement == null) {
                return null;
            }
            MetadataElement valuesZenithElement = zenithElement.getElement("Values_List");
            if (valuesZenithElement == null) {
                return null;
            }
            MetadataAttribute[] zenAnglesAttributes = valuesZenithElement.getAttributes();
            if (zenAnglesAttributes == null) {
                return null;
            }

            int nRows = azAnglesAttributes.length;
            if (nRows != zenAnglesAttributes.length) {
                return null;
            }
            String[] azAnglesString = new String[nRows];
            String[] zenAnglesString = new String[nRows];
            for (int i = 0; i < nRows; i++) {
                azAnglesString[i] = azAnglesAttributes[i].getData().toString();
                zenAnglesString[i] = zenAnglesAttributes[i].getData().toString();
            }
            sunAnglesGrids = wrapAngles(zenAnglesString, azAnglesString);
            sunAnglesGrids.setBandId("SUN");
            sunAnglesGrids.setResX(Float.parseFloat(zenithElement.getAttributeString("COL_STEP")));
            sunAnglesGrids.setResY(Float.parseFloat(zenithElement.getAttributeString("ROW_STEP")));
        }

        return sunAnglesGrids;
    }

    public AnglesGrid[] getViewingAnglesGrid() {
        if(viewingAnglesGrids == null) {
            MetadataElement geometricElement = rootElement.getElement("Geometric_Informations");
            if (geometricElement == null) {
                return null;
            }
            MetadataElement tileAnglesElement = geometricElement.getElement("Angles_Grids_List");
            if (tileAnglesElement == null) {
                return null;
            }

            MetadataElement viewingAnglesListElement = tileAnglesElement.getElement("Viewing_Incidence_Angles_Grids_List");
            if (viewingAnglesListElement == null) {
                return null;
            }

            viewingAnglesGrids = wrapStandardViewingAngles(viewingAnglesListElement);
        }

        return viewingAnglesGrids;
    }

    public AnglesGrid getViewingAnglesGrid(String bandId) {
        AnglesGrid[] anglesGrids = getViewingAnglesGrid();
        AnglesGrid bandAngleGrid = new AnglesGrid();
        int gridWidth = 0, gridHeight = 0;
        float resX = 0.0f, resY = 0.0f;
        float[] azimuth = null;
        float[] zenith = null;
        for(AnglesGrid anglesGrid : anglesGrids) {
            if(!anglesGrid.getBandId().equals(bandId)) {
                continue;
            }
            if(gridWidth == 0 || gridHeight == 0) {
                gridHeight = anglesGrid.getHeight();
                gridWidth = anglesGrid.getWidth();
                resX= anglesGrid.getResX();
                resY= anglesGrid.getResY();
                zenith = new float[gridWidth * gridHeight];
                azimuth = new float[gridWidth * gridHeight];
                Arrays.fill(zenith, Float.NaN);
                Arrays.fill(azimuth, Float.NaN);
            }
            for (int index = 0; index < gridHeight * gridWidth; index++) {
                try {
                    if (isValidAngle(anglesGrid.getZenith()[index])) {
                        zenith[index] = anglesGrid.getZenith()[index];
                    }

                    if (isValidAngle(anglesGrid.getAzimuth()[index])) {
                        azimuth[index] = anglesGrid.getAzimuth()[index];
                    }
                } catch (Exception e) {
                    logger.severe(StackTraceUtils.getStackTrace(e));
                }

            }
        }
        bandAngleGrid.setHeight(gridHeight);
        bandAngleGrid.setWidth(gridWidth);
        bandAngleGrid.setAzimuth(azimuth);
        bandAngleGrid.setZenith(zenith);
        bandAngleGrid.setBandId(bandId);
        bandAngleGrid.setDetectorId("ALL");
        bandAngleGrid.setResX(resX);
        bandAngleGrid.setResY(resY);

        return bandAngleGrid;
    }

    public AnglesGrid getMeanViewingAnglesGrid() {

        ArrayList<AnglesGrid> viewingAnglesGrids = new ArrayList<>();
        for(String bandId : getBandNames()) {
            AnglesGrid bandAngleGrid = getViewingAnglesGrid(bandId);
            if(bandAngleGrid == null) {
                continue;
            }
            viewingAnglesGrids.add(bandAngleGrid);
        }

        if(viewingAnglesGrids == null || viewingAnglesGrids.size() < 1) {
            return null;
        }
        int width = viewingAnglesGrids.get(0).getWidth();
        int height = viewingAnglesGrids.get(0).getHeight();

        //check size
        for(AnglesGrid angleGrid : viewingAnglesGrids) {
            if(angleGrid.getWidth() != width || angleGrid.getHeight() != height) {
                return null;
            }
        }

        float[] zenith = new float[width * height];
        float[] azimuth = new float[width * height];

        Arrays.fill(zenith,Float.NaN);
        Arrays.fill(azimuth,Float.NaN);

        //compute means
        for(int i = 0 ; i < width * height ; i++) {
            int countZenith = 0;
            float sumZenith = 0.0f;
            int countAzimuth = 0;
            float sumAzimuth = 0.0f;
            for(AnglesGrid angleGrid : viewingAnglesGrids) {
                if(Float.isFinite(angleGrid.getZenith()[i])) {
                    countZenith++;
                    sumZenith = sumZenith + angleGrid.getZenith()[i];
                }
                if(Float.isFinite(angleGrid.getAzimuth()[i])) {
                    countAzimuth++;
                    sumAzimuth = sumAzimuth + angleGrid.getAzimuth()[i];
                }
            }
            if(countZenith > 0) {
                zenith[i] = sumZenith/countZenith;
            }
            if(countAzimuth > 0) {
                azimuth[i] = sumAzimuth/countAzimuth;
            }
        }

        AnglesGrid meanViewingAnglesGrid = new AnglesGrid();

        meanViewingAnglesGrid.setHeight(height);
        meanViewingAnglesGrid.setWidth(width);
        meanViewingAnglesGrid.setAzimuth(azimuth);
        meanViewingAnglesGrid.setZenith(zenith);
        meanViewingAnglesGrid.setBandId("MEAN");
        meanViewingAnglesGrid.setResX(viewingAnglesGrids.get(0).getResX());
        meanViewingAnglesGrid.setResY(viewingAnglesGrids.get(0).getResY());
        return meanViewingAnglesGrid;
    }

    private boolean isValidAngle(float value) {
        return !Float.isNaN(value) && !Float.isInfinite(value);
    }

    public static class AnglesGrid {
        private String bandId;
        private String detectorId;
        private float[] zenith;
        private float[] azimuth;
        private int width, height;
        private float resX, resY;

        public String getBandId() {
            return bandId;
        }

        public void setBandId(String bandId) {
            this.bandId = bandId;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public float getResX() {
            return resX;
        }

        public void setResX(float resX) {
            this.resX = resX;
        }

        public float getResY() {
            return resY;
        }

        public void setResY(float resY) {
            this.resY = resY;
        }

        public String getDetectorId() {
            return detectorId;
        }

        public void setDetectorId(String detectorId) {
            this.detectorId = detectorId;
        }

        public float[] getZenith() {
            return zenith;
        }

        public void setZenith(float[] zenith) {
            this.zenith = zenith;
        }

        public float[] getAzimuth() {
            return azimuth;
        }

        public void setAzimuth(float[] azimuth) {
            this.azimuth = azimuth;
        }

        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
        }
    }

    public static AnglesGrid[] wrapStandardViewingAngles(MetadataElement tileAnglesMetadataElement) {
        ArrayList<AnglesGrid> anglesGrids = new ArrayList<>();
        for (MetadataElement viewingAnglesGridsElement : tileAnglesMetadataElement.getElements()) {
            if (!viewingAnglesGridsElement.getName().equals("Band_Viewing_Incidence_Angles_Grids_List")) {
                continue;
            }
            for (MetadataElement viewingAnglesElement : viewingAnglesGridsElement.getElements()) {
                if (!viewingAnglesElement.getName().equals("Viewing_Incidence_Angles_Grids")) {
                    continue;
                }

                MetadataElement azimuthElement = viewingAnglesElement.getElement("Azimuth");
                if (azimuthElement == null) continue;
                MetadataElement valuesAzimuthElement = azimuthElement.getElement("Values_List");
                if (valuesAzimuthElement == null) continue;
                MetadataAttribute[] azAnglesAttributes = valuesAzimuthElement.getAttributes();
                if (azAnglesAttributes == null) continue;

                MetadataElement zenithElement = viewingAnglesElement.getElement("Zenith");
                if (zenithElement == null) continue;
                MetadataElement valuesZenithElement = zenithElement.getElement("Values_List");
                if (valuesZenithElement == null) continue;
                MetadataAttribute[] zenAnglesAttributes = valuesZenithElement.getAttributes();
                if (zenAnglesAttributes == null) continue;

                int nRows = azAnglesAttributes.length;
                if (nRows != zenAnglesAttributes.length) {
                    continue;
                }
                String[] azAnglesString = new String[nRows];
                String[] zenAnglesString = new String[nRows];
                for (int i = 0; i < nRows; i++) {
                    azAnglesString[i] = azAnglesAttributes[i].getData().toString();
                    zenAnglesString[i] = zenAnglesAttributes[i].getData().toString();
                }
                AnglesGrid anglesGrid = wrapAngles(zenAnglesString, azAnglesString);
                anglesGrid.setBandId(viewingAnglesGridsElement.getAttributeString("band_id"));
                anglesGrid.setDetectorId(viewingAnglesElement.getAttributeString("detector_id"));
                anglesGrid.setResX(Float.parseFloat(zenithElement.getAttributeString("COL_STEP")));
                anglesGrid.setResY(Float.parseFloat(zenithElement.getAttributeString("ROW_STEP")));
                anglesGrids.add(anglesGrid);
            }
        }
        return anglesGrids.toArray(new AnglesGrid[anglesGrids.size()]);
    }

    public static AnglesGrid wrapAngles (String[] valuesZenith, String[] valuesAzimuth) {
        AnglesGrid anglesGrid = null;

        if(valuesAzimuth == null || valuesZenith == null) {
            return null;
        }
        int nRows = valuesZenith.length;
        int nCols = valuesZenith[0].split(" ").length;

        if(nRows != valuesAzimuth.length || nCols != valuesAzimuth[0].split(" ").length) {
            return null;
        }

        anglesGrid = new AnglesGrid();
        anglesGrid.setHeight(nRows);
        anglesGrid.setWidth(nCols);
        anglesGrid.setAzimuth(new float[nRows*nCols]);
        anglesGrid.setZenith(new float[nRows*nCols]);

        for (int rowindex = 0; rowindex < nRows; rowindex++) {
            String[] zenithSplit = valuesZenith[rowindex].split(" ");
            String[] azimuthSplit = valuesAzimuth[rowindex].split(" ");
            if(zenithSplit == null || azimuthSplit == null || zenithSplit.length != nCols ||azimuthSplit.length != nCols) {
                SystemUtils.LOG.severe("zenith and azimuth array length differ in line " + rowindex + " - " + valuesZenith[rowindex] + " - " + valuesAzimuth[rowindex]);
                return null;
            }
            for (int colindex = 0; colindex < nCols; colindex++) {
                anglesGrid.getZenith()[rowindex*nCols + colindex] = parseFloat(zenithSplit[colindex]);
                anglesGrid.getAzimuth()[rowindex*nCols + colindex] = parseFloat(azimuthSplit[colindex]);
            }
        }
        return anglesGrid;
    }

    static float parseFloat(String s) {
        if ("INF".equals(s)) {
            return Float.POSITIVE_INFINITY;
        } else if ("-INF".equals(s)) {
            return Float.NEGATIVE_INFINITY;
        } else {
            return Float.parseFloat(s);
        }
    }
}
