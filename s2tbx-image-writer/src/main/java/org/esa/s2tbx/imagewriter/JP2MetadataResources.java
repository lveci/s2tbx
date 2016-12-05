package org.esa.s2tbx.imagewriter;

/**
 * Created by rdumitrascu on 11/30/2016.
 */
public class JP2MetadataResources {

    private String fileStructureType;
    private String fileName;

    public void addFileStructureType(String fileStructureType){
        this.fileStructureType = fileStructureType;
    }

    public String getFileStructureType(){
        return this.fileStructureType;
    }

    public void addFileName(String fileName){
        this.fileName = fileName;
    }

    public String getFileName(){
        return this.fileName;
    }
}
