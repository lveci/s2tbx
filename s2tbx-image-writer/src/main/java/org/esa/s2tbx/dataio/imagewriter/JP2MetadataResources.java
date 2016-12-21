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
    private int stepX;
    private int stepY;
    private Point2D.Double origin = null;
    private int imageWidth;
    private int imageHeight;
    private int epsgNumber;

    public JP2MetadataResources(){

    }
    /**
     *
     * @param x latitude coordinate
     * @param y longitude coordinate
     */
    public void setPoint2D(String x, String y) {
       this.origin = new Point2D.Double(Double.parseDouble(x), Double.parseDouble(y));
    }

    /**
     *
     * @return
     */
    public Point2D getPoint(){
        return this.origin;
    }

    /**
     *
     * @param x
     */
    public void setStepX(int x){
        this.stepX = x;
    }

    /**
     *
     * @return
     */
    public int getStepX(){
        return this.stepX;
    }

    /**
     *
     * @param y
     */
    public void setStepY(int y){
        this.stepY = y;
    }

    /**
     *
     * @return
     */
    public int getStepY(){
        return this.stepY;
    }

    /**
     *
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

    public int getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }

    public int getEpsgNumber() {
        return epsgNumber;
    }

    public void setEpsgNumber(int epsgNumber) {
        this.epsgNumber = epsgNumber;
    }
}
