package org.esa.s2tbx.imagewriter;

import com.sun.media.jfxmedia.logging.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataFormatImpl;

/**
 * Created by rdumitrascu on 11/25/2016.
 */
public class JP2Metadata extends IIOMetadata{

    /**
     * Indicates whether this object represents stream or image
     * metadata.  Package-visible so the writer can see it.
     */
    final boolean isStream;

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(JP2Metadata.class.getName());


    JP2Metadata(boolean isStream) {
        super(true,  // Supports standard format
                JP2Format._nativeImageMetadataFormatName,  // and a native format
                JP2Format._nativeImageMetadataFormatClassName,
                null, null);  // No other formats
        // But if we are stream metadata, adjust the variables
        this.isStream = isStream;
        if (isStream) {
            nativeMetadataFormatName = JP2Format._nativeStreamMetadataFormatName;
            nativeMetadataFormatClassName =
                    JP2Format._nativeStreamMetadataFormatClassName;
        }
    }

    /**
     *
     * Constructs a default stream <code>JP2Metadata</code> object appropriate
     * for the given write parameters.
     * @param param
     * @param writer
     */
    JP2Metadata(ImageWriteParam param, ImageWriterPlugin writer) {
        this(true);

        //TODO
    }

    /**
     *
     * Constructs a default image <code>JP2Metadata</code> object appropriate
     * for the given image type and write parameters.
     * @param imageType
     * @param param
     * @param writer
     */
    JP2Metadata(ImageTypeSpecifier imageType,
                ImageWriteParam param,
                ImageWriterPlugin writer) {
        this(false);

        //TODO
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public Node getAsTree(String formatName) {
        return null;

        //TODO
    }

    @Override
    public void mergeTree(String formatName, Node root) throws IIOInvalidTreeException {
        if (formatName == null) {
            throw new IllegalArgumentException("null formatName!");
        }
        if (root == null) {
            throw new IllegalArgumentException("null root!");
        }
        if (isStream &&
                (formatName.equals(JP2Format._nativeStreamMetadataFormatName))) {
            mergeNativeTree(root);
        } else if (!isStream &&
                (formatName.equals(JP2Format._nativeImageMetadataFormatName))) {
            mergeNativeTree(root);
        } else if (!isStream &&
                (formatName.equals
                        (IIOMetadataFormatImpl.standardMetadataFormatName))) {
            mergeStandardTree(root);
        } else {
            throw  new IllegalArgumentException("Unsupported format name: "
                    + formatName);
        }
    }
    private void mergeNativeTree(Node root) throws IIOInvalidTreeException {
        String name = root.getNodeName();
        if (name != ((isStream) ? JP2Format._nativeStreamMetadataFormatName
                : JP2Format._nativeImageMetadataFormatName)) {
            throw new IIOInvalidTreeException("Invalid root node name: " + name,
                    root);
        }
        //TODO
        mergeSequenceSubtree(root.getFirstChild());

    }

    private void mergeSequenceSubtree(Node sequenceTree) throws IIOInvalidTreeException {
        NodeList children = sequenceTree.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            String name = node.getNodeName();
            //TODO
        }
        }

    private void mergeStandardTree(Node root) throws IIOInvalidTreeException {
        NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            String name = node.getNodeName();
        }

        //TODO
    }
    @Override
    public void reset() {

        //TODO


    }
}
