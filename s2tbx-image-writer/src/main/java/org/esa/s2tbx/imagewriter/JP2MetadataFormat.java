package org.esa.s2tbx.imagewriter;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadataFormatImpl;

/**
 * Created by rdumitrascu on 11/21/2016.
 */
public class JP2MetadataFormat extends IIOMetadataFormatImpl {

    private static JP2MetadataFormat theInstance = null;

    JP2MetadataFormat(String formatName, int childPolicy) {
        super(formatName, childPolicy);

    }

    void addStreamElements(String parentName) {
        //TODO
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