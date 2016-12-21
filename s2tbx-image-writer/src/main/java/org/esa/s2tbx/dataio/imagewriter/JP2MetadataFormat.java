package org.esa.s2tbx.dataio.imagewriter;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadataFormatImpl;

/**
 * Created by rdumitrascu on 11/21/2016.
 */
public class JP2MetadataFormat extends IIOMetadataFormatImpl {

    /**
     *
     * @param formatName
     * @param childPolicy
     */
    JP2MetadataFormat(String formatName, int childPolicy) {
        super(formatName, childPolicy);
    }

    /**
     *
     * @param parentName
     */
    void addStreamElements(String parentName) {

        addElement("JP2Medatada", JP2FormatConstants._nativeStreamMetadataFormatName, CHILD_POLICY_CHOICE);

        addElement("FeatureCollection", parentName, CHILD_POLICY_CHOICE);

        addElement("FeatureMember", "FeatureCollection", CHILD_POLICY_CHOICE);

        addElement("RectifiedGridCoverage", "FeatureMember", CHILD_POLICY_SEQUENCE);

        addElement("rangeSet", "RectifiedGridCoverage", CHILD_POLICY_CHOICE);

        addElement("rectifiedGridDomain", "RectifiedGridCoverage", CHILD_POLICY_CHOICE);

        addElement("File", "rangeSet",CHILD_POLICY_SEQUENCE);

        addElement("fileName", "File",CHILD_POLICY_EMPTY);

        addElement("fileStructure", "File",CHILD_POLICY_EMPTY);

        addAttribute("fileName", "stringFileName",DATATYPE_STRING,true, "gmljp2://codestream/0" );

        addAttribute("fileStructure", "fileStructureType",DATATYPE_STRING,true, "Record Interleaved" );

        addElement("RectifiedGrid", "rectifiedGridDomain", CHILD_POLICY_SEQUENCE);

        addElement("limits", "RectifiedGrid", CHILD_POLICY_CHOICE);

        addElement("GridEnvelope", "limits", CHILD_POLICY_CHOICE);

        addElement("high", "GridEnvelope", CHILD_POLICY_CHOICE);

        addAttribute("high", "imageWidth", DATATYPE_INTEGER, true, null);

        addAttribute("high", "imageHeight", DATATYPE_INTEGER, true, null);

        addElement("offsetVector", "RectifiedGrid", CHILD_POLICY_EMPTY);

        addAttribute("offsetVector", "offsetValueX", DATATYPE_INTEGER, true, 0, Integer.MAX_VALUE);

        addAttribute("offsetVector", "offsetValueY", DATATYPE_INTEGER, true, 0, Integer.MAX_VALUE);

        addElement("origin", "RectifiedGrid", CHILD_POLICY_CHOICE);

        addElement("Point", "origin", CHILD_POLICY_CHOICE);

        addElement("pos", "Point", CHILD_POLICY_EMPTY);

        addAttribute("pos", "latCoordinate", DATATYPE_INTEGER, true, null);

        addAttribute("pos", "longCoordinate", DATATYPE_INTEGER, true, null);

    }

    @Override
    public boolean canNodeAppear(String elementName, ImageTypeSpecifier imageType) {
        // Just check if it appears in the format
        if (elementName.equals(getRootName())
                || elementName.equals("JP2Medatada")
                || elementName.equals("FeatureCollection")
                || elementName.equals("FeatureMember")
                || isInSubtree(elementName,"RectifiedGridCoverage" )){
            return true;
        }
        if(isInSubtree(elementName,"File" )){
            return true;
        }
        if(isInSubtree(elementName,"RectifiedGrid")){
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