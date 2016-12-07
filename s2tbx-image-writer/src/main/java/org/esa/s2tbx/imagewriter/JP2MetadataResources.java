package org.esa.s2tbx.imagewriter;

import java.awt.geom.Point2D;

/**
 * Class containing all the resources needed for the JP2metadata
 *
 * Created by rdumitrascu on 11/30/2016.
 */
public class JP2MetadataResources {


    private String fileStructureType;
    private String fileName;
    private int stepX;
    private int stepY;
    private Point2D.Double origin =null;

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
}
