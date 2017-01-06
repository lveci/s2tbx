package org.esa.s2tbx.dataio.imagewriter;

import javax.imageio.metadata.IIOMetadata;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.awt.geom.Point2D;
import java.awt.image.RenderedImage;
import java.io.FileOutputStream;
import java.io.StringWriter;

/**
 * Class containing all the resources needed for the JP2metadata
 *
 * Created by rdumitrascu on 11/30/2016.
 */
public class JP2MetadataResources {


    private String fileStructureType = "Record Interleaved";
    private String fileName = "gmljp2://codestream/0";
    private double stepX;
    private double stepY;
    private Point2D.Double origin = null;
    private int imageWidth;
    private int imageHeight;
    private int epsgNumber;

    public JP2MetadataResources(){

    }
    /**
     * Sets the coordinates of the origin
     * @param x easting coordinate
     * @param y northing coordinate
     */
    public void setPoint2D(String x, String y) {
        this.origin = new Point2D.Double(Double.parseDouble(x), Double.parseDouble(y));
    }

    /**
     *
     * @return returns a Point2D of the origin
     */
    public Point2D getPoint(){
        return this.origin;
    }

    /**
     * Sets the spep that each pixel represents on the worldMap in meters
     * @param x the step of each pixel on the x axis
     */
    public void setStepX(double x){
        this.stepX = x;
    }

    /**
     *
     * @return step of each pixel on the x axis
     */
    public double getStepX(){
        return this.stepX;
    }

    /**
     * Sets the spep that each pixel represents on the worldMap in meters
     * @param y the step of each pixel on the y axis
     */
    public void setStepY(double y){
        this.stepY = y;
    }

    /**
     *
     * @return step of each pixel on the y axis
     */
    public double getStepY(){
        return this.stepY;
    }

    /**
     * sets the file structure type of each Jpeg2000 file
     * @param fileStructureType
     */
    public void setFileStructureType(String fileStructureType){
        this.fileStructureType = fileStructureType;
    }

    /**
     *
     * @return
     */
    public String getFileStructureType(){
        return this.fileStructureType;
    }

    /**
     *
     * @param fileName
     */
    public void setFileName(String fileName){
        this.fileName = fileName;
    }

    /**
     *
     * @return
     */
    public String getFileName(){
        return this.fileName;
    }

    /**
     *
     * @return the width in pixels of the rendered image
     */
    public int getImageWidth() {
        return imageWidth;
    }

    /**
     *
     * @param imageWidth the width in pixels of the rendered image
     */
    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    /**
     *
     * @return the height in pixels of the rendered image
     */
    public int getImageHeight() {
        return imageHeight;
    }

    /**
     *
     * @param imageHeight the height in pixels of the rendered image
     */
    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }

    /**
     *
     * returns the EPSG code of the UTM zone
     * @return  the EPSG code of the UTM zone where the origin of the product is located
     */
    public int getEpsgNumber() {
        return epsgNumber;
    }

    /**
     *
     * Sets the EPSG code of the UTM zone
     * @param epsgNumber the EPSG code of the UTM zone where the origin of the product is located
     */
    public void setEpsgNumber(int epsgNumber) {
        this.epsgNumber = epsgNumber;
    }
}
