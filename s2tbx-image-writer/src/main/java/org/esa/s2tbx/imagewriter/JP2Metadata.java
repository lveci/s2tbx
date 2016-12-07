package org.esa.s2tbx.imagewriter;



import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.imageio.ImageWriteParam;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataFormat;
import javax.imageio.metadata.IIOMetadataNode;
import java.awt.geom.Point2D;


/**
 * Created by rdumitrascu on 11/25/2016.
 */
public class JP2Metadata extends IIOMetadata{

    public JP2MetadataResources jp2resources  = new JP2MetadataResources();
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
    }

    // Return the singleton instance
    public IIOMetadataFormat getMetadataFormat(String formatName) {
        if (!formatName.equals(nativeMetadataFormatName)) {
            throw new IllegalArgumentException("Bad format name!");
        }
        return JP2StreamMetadataFormat.getInstance();
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public Node getAsTree(String formatName) {
        if (formatName == null) {
            throw new IllegalArgumentException("null formatName!");
        }
        if (formatName.equals(JP2Format._nativeStreamMetadataFormatName)) {
            return getNativeTree();
        }
        throw new IllegalArgumentException("Unsupported format name: "
                + formatName);
    }

    /**
     *
     * @return
     */
    private IIOMetadataNode getNativeTree(){
        IIOMetadataNode root = null;
        IIOMetadataNode top;
        JP2MetadataResources localResource = this.jp2resources;
        root = new IIOMetadataNode(JP2Format._nativeStreamMetadataFormatName);
        IIOMetadataNode header = new IIOMetadataNode("JP2Medatada");
        root.appendChild(header);
        appendHeaderNodes(header, localResource);
        return root;
    }

    /**
     *
     * @param header
     * @param localResource
     */
    private void appendHeaderNodes(IIOMetadataNode header, JP2MetadataResources localResource) {
        IIOMetadataNode featureCollectionNode = new IIOMetadataNode("FeatureCollection");
        header.appendChild(featureCollectionNode);
        IIOMetadataNode featureMemberNode = new IIOMetadataNode("FeatureMember");
        featureCollectionNode.appendChild(featureMemberNode);
        IIOMetadataNode rectifiedGridCoverageSequence = new IIOMetadataNode("RectifiedGridCoverage");
        featureMemberNode.appendChild(rectifiedGridCoverageSequence);
        IIOMetadataNode rangeSetNode =  new IIOMetadataNode("rangeSet");
        IIOMetadataNode rectifiedGridDomainNode =  new IIOMetadataNode("rectifiedGridDomain");
        rectifiedGridCoverageSequence.appendChild(rangeSetNode);
        rectifiedGridCoverageSequence.appendChild(rectifiedGridDomainNode);
        IIOMetadataNode fileNode =  new IIOMetadataNode("File");
        appendFileNodeAttributes(fileNode,localResource.getFileName(), localResource.getFileStructureType() );
        rangeSetNode.appendChild(fileNode);
        IIOMetadataNode rectifiedGridSequence =  new IIOMetadataNode("RectifiedGrid");
        rectifiedGridDomainNode.appendChild(rectifiedGridSequence);
        IIOMetadataNode offsetVectorNode =  new IIOMetadataNode("offsetVector");
        appendOffsetVectorAttributes(offsetVectorNode, localResource.getStepX(), localResource.getStepY());
        rectifiedGridSequence.appendChild(offsetVectorNode);
        IIOMetadataNode originNode =  new IIOMetadataNode("origin");
        rectifiedGridSequence.appendChild(originNode);
        IIOMetadataNode pointNode =  new IIOMetadataNode("Point");
        originNode.appendChild(pointNode);
        IIOMetadataNode posNode =  new IIOMetadataNode("pos");
        appendPosNodeAttributes(posNode,localResource.getPoint());
        pointNode.appendChild(posNode);
    }

    /**
     *
     * @param fileNode
     * @param fileName
     * @param fileStructureType
     */
    private void appendFileNodeAttributes(IIOMetadataNode fileNode, String fileName, String fileStructureType) {
       fileNode.setAttribute("stringFileName", fileName);
       fileNode.setAttribute("fileStructureType", fileStructureType) ;
    }

    /**
     *
     * @param offsetVectorNode
     * @param stepX
     * @param stepY
     */
    private void appendOffsetVectorAttributes(IIOMetadataNode offsetVectorNode, int stepX, int stepY) {
        offsetVectorNode.setAttribute("offsetValueX",stepX+"" );
        offsetVectorNode.setAttribute("offsetValueY",stepY+"" );
    }

    /**
     *
     * @param posNode
     * @param point
     */
    private void appendPosNodeAttributes(IIOMetadataNode posNode, Point2D point) {
        posNode.setAttribute("latCoordinate", point.getX()+"");
        posNode.setAttribute("longCoordinate", point.getY()+"");
    }

    /**
     *
     * @param formatName
     * @param root
     * @throws IIOInvalidTreeException
     */
    @Override
    public void mergeTree(String formatName, Node root) throws IIOInvalidTreeException {
        if (formatName == null) {
            throw new IllegalArgumentException("null formatName!");
        }
        if (root == null) {
            throw new IllegalArgumentException("null root!");
        }
        if( (formatName.equals(JP2Format._nativeStreamMetadataFormatName))) {
            mergeNativeTree(root);
        } else {
            throw  new IllegalArgumentException("Unsupported format name: "
                    + formatName);
        }
    }

    /**
     *
     * @param root
     * @throws IIOInvalidTreeException
     */
    private void mergeNativeTree(Node root) throws IIOInvalidTreeException {
        String name = root.getNodeName();
        if (name !=  JP2Format._nativeStreamMetadataFormatName) {
            throw new IIOInvalidTreeException("Invalid root node name: " + name,
                    root);
        }
        if (root.getChildNodes().getLength() != 1) { // JP2Medatada
            throw new IIOInvalidTreeException(
                    "JP2Medatada node must be present", root);
        }
        Node node = root;
        while (node != null) {
            if(node.getNodeName().equals("RectifiedGridCoverage")){
                mergeSequenceSubtree(root.getFirstChild());
            }
            node = node.getNextSibling();
        }
    }

    /**
     *
     * @param sequenceTree
     * @throws IIOInvalidTreeException
     */
    private void mergeSequenceSubtree(Node sequenceTree) throws IIOInvalidTreeException {
        NodeList children = sequenceTree.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            String name = node.getNodeName();
            if (name.equals("rangeSet")) {
                mergeRangeSetNode(node.getFirstChild());
            }else if (name.equals("rectifiedGridDomain")) {
                mergeRectifiedGridDomainNode(node.getFirstChild());
            }else {
                throw new IIOInvalidTreeException("Invalid node: " + name, node);
            }
        }
    }

    /**
     *
     * @param node
     * @throws IIOInvalidTreeException
     */
    private void mergeRangeSetNode(Node node) throws IIOInvalidTreeException {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node localNode = children.item(i);
            String name = node.getNodeName();
            if (name.equals("fileName")) {
                mergeFileNameNode(localNode, true);
            }else if (name.equals("fileStructure")) {
                mergeFileStructureNode(localNode, true);
            }else {
                throw new IIOInvalidTreeException("Invalid node: " + name, localNode);
            }
        }
    }

    /**
     *
     * @param node
     * @param required
     * @throws IIOInvalidTreeException
     */
    private void mergeFileNameNode(Node node, boolean required)throws IIOInvalidTreeException {

        NamedNodeMap attributes = node.getAttributes();
        String valueFileNameString = attributes.getNamedItem("stringFileName").getNodeValue();
        if (valueFileNameString == null) {
            if (required) {
                throw new IIOInvalidTreeException
                        ("stringFileName" + " attribute not found", node);
            }
        }else{
            jp2resources.setFileName(valueFileNameString);
        }
    }

    /**
     *
     * @param node
     * @param required
     * @throws IIOInvalidTreeException
     */
    private void mergeFileStructureNode(Node node, boolean required)throws IIOInvalidTreeException {
        NamedNodeMap attributes = node.getAttributes();
        String valuefileStructureTypeString = attributes.getNamedItem("fileStructureType").getNodeValue();
        if (valuefileStructureTypeString == null) {
            if (required) {
                throw new IIOInvalidTreeException
                        ("stringFileName" + " attribute not found", node);
            }
        }else{
            jp2resources.setFileStructureType(valuefileStructureTypeString);
        }
    }

    /**
     *
     * @param node
     * @throws IIOInvalidTreeException
     */
    private void mergeRectifiedGridDomainNode(Node node) throws IIOInvalidTreeException {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node localNode = children.item(i);
            String name = node.getNodeName();
            if (name.equals("origin")) {
                mergeOriginNode(localNode.getFirstChild(), true);
            }else if (name.equals("offsetVector")) {
                mergeoffsetVectoreNode(localNode, true);
            }else {
                throw new IIOInvalidTreeException("Invalid node: " + name, localNode);
            }
        }
    }

    /**
     *
     * @param node
     * @param required
     * @throws IIOInvalidTreeException
     */
    private void mergeoffsetVectoreNode(Node node, boolean required)throws IIOInvalidTreeException {
        NamedNodeMap attributes = node.getAttributes();
        String valueOffsetValueXString = attributes.getNamedItem("offsetValueX").getNodeValue();
        String valueOffsetValueYString = attributes.getNamedItem("offsetValueY").getNodeValue();
        if ((valueOffsetValueXString == null)||(valueOffsetValueYString==null)){
            if (required) {
                throw new IIOInvalidTreeException
                        ("stringFileName" + " attribute not found", node);
            }
        }else{
            jp2resources.setStepX(Integer.parseInt(valueOffsetValueXString));
            jp2resources.setStepY(Integer.parseInt(valueOffsetValueYString));
        }
    }

    /**
     *
     * @param localNode
     * @param required
     * @throws IIOInvalidTreeException
     */
    private void mergeOriginNode(Node localNode, boolean required)throws IIOInvalidTreeException {
        Node node = localNode.getFirstChild();
        NamedNodeMap attributes = node.getAttributes();
        String valueLatCoordinateString = attributes.getNamedItem("latCoordinate").getNodeValue();
        String valueLongCoordinateString = attributes.getNamedItem("longCoordinate").getNodeValue();
        if ((valueLatCoordinateString == null)||(valueLongCoordinateString==null)){
            if (required) {
                throw new IIOInvalidTreeException
                        ("stringFileName" + " attribute not found", node);
            }
        }else{
            jp2resources.setPoint2D(valueLatCoordinateString,valueLongCoordinateString );
        }
    }

    /**
     * resets JP2MetadataResources gfor the new metadata element
     */
    @Override
    public void reset() {

        this.jp2resources = new JP2MetadataResources();

    }

}
