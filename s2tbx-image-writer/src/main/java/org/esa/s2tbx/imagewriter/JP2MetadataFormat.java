package org.esa.s2tbx.imagewriter;

import com.sun.imageio.plugins.jpeg.JPEG;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadataFormatImpl;

/**
 * Created by rdumitrascu on 11/21/2016.
 */
public class JP2MetadataFormat extends IIOMetadataFormatImpl {

    private static JP2MetadataFormat theInstance = null;

    JP2MetadataFormat(String formatName, int childPolicy) {
        super(formatName, childPolicy);

        addElement("JP2Medatada", JP2Format._nativeStreamMetadataFormatName, CHILD_POLICY_CHOICE);

        addStreamElements("JP2Medatada");

    }

    void addStreamElements(String parentName) {

        addElement("FeatureCollection", parentName, CHILD_POLICY_CHOICE);

        addElement("FeatureMember", "FeatureCollection", CHILD_POLICY_CHOICE);

        addElement("RectifiedGridCoverage", "FeatureMember", 2,2);

        addElement("rangeSet", "RectifiedGridCoverage", CHILD_POLICY_CHOICE);

        addElement("rectifiedGridDomain", "RectifiedGridCoverage", CHILD_POLICY_CHOICE);

        addElement("File", "rangeSet", 2,2);

        addElement("fileName", "File",CHILD_POLICY_EMPTY);

        addElement("fileStructure", "File",CHILD_POLICY_EMPTY);

        addAttribute("fileName", "stringFileName",DATATYPE_STRING,true, "gmljp2://codestream/0" );

        addAttribute("fileStructure", "fileStructureType",DATATYPE_STRING,true, "Record Interleaved" );

        addElement("RectifiedGrid", "rectifiedGridDomain", 2,2);

        addElement("offsetVector", "RectifiedGrid", CHILD_POLICY_EMPTY);

        addAttribute("offsetVector", "offsetValue", DATATYPE_INTEGER, true, 0, Integer.MAX_VALUE);

        addElement("origin", "RectifiedGrid", CHILD_POLICY_CHOICE);

        addElement("Point", "origin", CHILD_POLICY_CHOICE);

        addAttribute("Point", "mglId", DATATYPE_STRING, true,"P0001");

        addElement("pos", "Point", CHILD_POLICY_EMPTY);

        addAttribute("pos", "latCoordinate", DATATYPE_INTEGER, true, Integer.MIN_VALUE, Integer.MAX_VALUE);

        addAttribute("pos", "longCoordinate", DATATYPE_INTEGER, true, Integer.MIN_VALUE, Integer.MAX_VALUE);

    }

    /**
     *
     * @param elementName
     * @param imageType
     * @return
     */
        @Override
    public boolean canNodeAppear(String elementName, ImageTypeSpecifier imageType) {
         // Just check if it appears in the format
         if (isInSubtree(elementName, getRootName())){
             return true;
         }
         return false;
     }

    /**
     * Returns <code>true</code> if the named element occurs in the
     * subtree of the format starting with the node named by
     * <code>subtreeName</code>, including the node
     * itself.  <code>subtreeName</code> may be any node in
     * the format.  If it is not, an
     * <code>IllegalArgumentException</code> is thrown.
     */
    protected boolean isInSubtree(String elementName,
                                  String subtreeName) {
        if (elementName.equals(subtreeName)) {
            return true;
        }
        String [] children = getChildNames(elementName);
        for (int i=0; i < children.length; i++) {
            if (isInSubtree(elementName, children[i])) {
                return true;
            }
        }
        return false;
    }

}