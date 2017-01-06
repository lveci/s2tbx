package org.esa.s2tbx.dataio.imagewriter;



import com.sun.javafx.collections.MappingChange;
import org.esa.snap.core.datamodel.*;
import org.geotools.coverage.grid.io.imageio.geotiff.GeoTiffException;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.referencing.CRS;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.geotools.referencing.factory.ReferencingFactoryContainer;
import org.geotools.referencing.operation.DefaultCoordinateOperationFactory;
import org.opengis.geometry.DirectPosition;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.cs.CartesianCS;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransformFactory;
import org.opengis.referencing.operation.TransformException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataFormat;
import javax.imageio.metadata.IIOMetadataNode;
import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.Locale;


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

    private final double EARTH_RADIUS =  6378.137;

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(JP2Metadata.class.getName());

    JP2Metadata(boolean isStream) {
        super(true,  // Supports standard format
                JP2FormatConstants._nativeImageMetadataFormatName,  // and a native format
                JP2FormatConstants._nativeImageMetadataFormatClassName,
                null, null);  // No other formats
        // But if we are stream metadata, adjust the variables
        this.isStream = isStream;
        if (isStream) {
            nativeMetadataFormatName = JP2FormatConstants._nativeStreamMetadataFormatName;
            nativeMetadataFormatClassName =
                    JP2FormatConstants._nativeStreamMetadataFormatClassName;
        }
    }

    /**
     *
     * Constructs a default stream <code>JP2Metadata</code> object appropriate
     * for the given write parameters.
     */
    JP2Metadata(ImageWriteParam param, JP2ImageWriter writer) {
        this(true);
    }

    /**
     * Constructs a default image <code>JPEGMetadata</code> object appropriate
     * for the given image type and write parameters.
     */
    JP2Metadata(ImageTypeSpecifier imageType,ImageWriteParam param, JP2ImageWriter writer) {
        this(false);
    }


    public void createJP2Metadata(GeoCoding geoCoding, int width, int height){
        this.jp2resources.setImageHeight(height);
        this.jp2resources.setImageWidth(width);
        createJP2Metada(geoCoding, width, height);
    }

    /**
     *
     *Function that computes the UTM coordinates(Easting and Northing) of the Origin Point, the UTM zone and the
     *offset that each pixel represents on the world map
     */
    private void createJP2Metada(GeoCoding geoCoding, int width, int height) {

        double latitude = geoCoding.getGeoPos(new PixelPos(0.5f, 0.5f), null).getLat();
        double longitude =  geoCoding.getGeoPos(new PixelPos(0.5f, 0.5f), null).getLon();
        Deg2UTM pos =new Deg2UTM(latitude, longitude);
        int geoLocationZone = 0;
        if(latitude>=0) {
            geoLocationZone = Integer.parseInt("326" + pos.Zone);
        }
        else{
            geoLocationZone = Integer.parseInt("327" + pos.Zone);
        }

        this.jp2resources.setEpsgNumber(geoLocationZone);
        this.jp2resources.setPoint2D( pos.Easting +"", pos.Northing+"");

        GeoPos geoPos1 = geoCoding.getGeoPos(new PixelPos(0.5f, 0.5f), null);
        GeoPos geoPos2 = geoCoding.getGeoPos(new PixelPos(0.5f, 1.5f), null);

        double distance = computeDistance(geoPos1.getLat(),geoPos2.getLat(),geoPos1.getLon(), geoPos2.getLon());
        this.jp2resources.setStepX(distance);
        this.jp2resources.setStepY(distance);

    }

    /**
     * Computes the distance in meters between 2 coordinates
     * @param point1Lat latitude of the first point
     * @param point2Lat latitude of the second point
     * @param point1Long longitude of the first point
     * @param point2Long longitude of the second point
     * @return the distance in meters between 2 coordinates
     */
    private double computeDistance(double point1Lat, double point2Lat, double point1Long, double point2Long ) {
        double dLat = point2Lat * Math.PI / 180 - point1Lat * Math.PI / 180;
        double dLon = point2Long * Math.PI / 180 - point1Long * Math.PI / 180;
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(point1Lat * Math.PI / 180) * Math.cos(point2Lat * Math.PI / 180) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c  = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = EARTH_RADIUS * c;
        return d*1000;
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
        if (formatName.equals(JP2FormatConstants._nativeStreamMetadataFormatName)) {
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
        JP2MetadataResources localResource = this.jp2resources;
        root = new IIOMetadataNode(JP2FormatConstants._nativeStreamMetadataFormatName);
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
        IIOMetadataNode limitsNode = new IIOMetadataNode("limits");
        rectifiedGridSequence.appendChild(limitsNode);
        IIOMetadataNode gridEnvelopeNode = new IIOMetadataNode("GridEnvelope");
        limitsNode.appendChild(gridEnvelopeNode);
        IIOMetadataNode imageLimitsNode = new IIOMetadataNode("high");
        appendImageLimitsAttributes(imageLimitsNode,localResource.getImageHeight(), localResource.getImageWidth());
        gridEnvelopeNode.appendChild(imageLimitsNode);
    }

    /**
     *
     * @param imageLimitsNode
     * @param imageHeight
     * @param imageWidth
     */
    private void appendImageLimitsAttributes(IIOMetadataNode imageLimitsNode, int imageHeight, int imageWidth) {
        imageLimitsNode.setAttribute("imageHeight", imageHeight+"");
        imageLimitsNode.setAttribute("imageWidth", imageWidth+"");
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
    private void appendOffsetVectorAttributes(IIOMetadataNode offsetVectorNode, double stepX, double stepY) {
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

    @Override
    public void mergeTree(String formatName, Node root) throws IIOInvalidTreeException {
        if (formatName == null) {
            throw new IllegalArgumentException("null formatName!");
        }
        if (root == null) {
            throw new IllegalArgumentException("null root!");
        }
        if( (formatName.equals(JP2FormatConstants._nativeStreamMetadataFormatName))) {
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
        if (name !=  JP2FormatConstants._nativeStreamMetadataFormatName) {
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
                        ("fileStructureType" + " attribute not found", node);
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
            }else if (name.equals("limits")) {
                mergeLimitsNode(localNode.getFirstChild(), true);
            }else {
                throw new IIOInvalidTreeException("Invalid node: " + name, localNode);
            }
        }
    }

    /**
     *
     * @param localNode
     * @param required
     */
    private void mergeLimitsNode(Node localNode, boolean required)throws IIOInvalidTreeException  {
        Node node = localNode.getFirstChild();
        NamedNodeMap attributes = node.getAttributes();
        String valueImageWidthString = attributes.getNamedItem("imageWidth").getNodeValue();
        String valueImageHeightString = attributes.getNamedItem("imageHeight").getNodeValue();
        if ((valueImageWidthString == null)||(valueImageHeightString==null)){
            if (required) {
                throw new IIOInvalidTreeException
                        ("image width, height attributes not found", node);
            }
        }else{
            jp2resources.setImageHeight(Integer.parseInt(valueImageHeightString));
            jp2resources.setImageWidth(Integer.parseInt(valueImageWidthString));
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
                        ("Offset attributes not found", node);
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
                        ("point attributes not found", node);
            }
        }else{
            jp2resources.setPoint2D(valueLatCoordinateString,valueLongCoordinateString );
        }
    }

    /**
     * Clatt that transforms the Latitude and Longitude of the Origin Point in UTM coordinates
     */
    private class Deg2UTM
    {
        double Easting;
        double Northing;
        int Zone;
        char Letter;
        private  Deg2UTM(double Lat,double Lon)
        {
            Zone= (int) Math.floor(Lon/6+31);
            if (Lat<-72)
                Letter='C';
            else if (Lat<-64)
                Letter='D';
            else if (Lat<-56)
                Letter='E';
            else if (Lat<-48)
                Letter='F';
            else if (Lat<-40)
                Letter='G';
            else if (Lat<-32)
                Letter='H';
            else if (Lat<-24)
                Letter='J';
            else if (Lat<-16)
                Letter='K';
            else if (Lat<-8)
                Letter='L';
            else if (Lat<0)
                Letter='M';
            else if (Lat<8)
                Letter='N';
            else if (Lat<16)
                Letter='P';
            else if (Lat<24)
                Letter='Q';
            else if (Lat<32)
                Letter='R';
            else if (Lat<40)
                Letter='S';
            else if (Lat<48)
                Letter='T';
            else if (Lat<56)
                Letter='U';
            else if (Lat<64)
                Letter='V';
            else if (Lat<72)
                Letter='W';
            else
                Letter='X';
            Easting=0.5*Math.log((1+Math.cos(Lat*Math.PI/180)*Math.sin(Lon*Math.PI/180-(6*Zone-183)*Math.PI/180))/(1-Math.cos(Lat*Math.PI/180)*Math.sin(Lon*Math.PI/180-(6*Zone-183)*Math.PI/180)))*0.9996*6399593.62/Math.pow((1+Math.pow(0.0820944379, 2)*Math.pow(Math.cos(Lat*Math.PI/180), 2)), 0.5)*(1+ Math.pow(0.0820944379,2)/2*Math.pow((0.5*Math.log((1+Math.cos(Lat*Math.PI/180)*Math.sin(Lon*Math.PI/180-(6*Zone-183)*Math.PI/180))/(1-Math.cos(Lat*Math.PI/180)*Math.sin(Lon*Math.PI/180-(6*Zone-183)*Math.PI/180)))),2)*Math.pow(Math.cos(Lat*Math.PI/180),2)/3)+500000;
            Easting=Math.round(Easting*100)*0.01;
            Northing = (Math.atan(Math.tan(Lat*Math.PI/180)/Math.cos((Lon*Math.PI/180-(6*Zone -183)*Math.PI/180)))-Lat*Math.PI/180)*0.9996*6399593.625/Math.sqrt(1+0.006739496742*Math.pow(Math.cos(Lat*Math.PI/180),2))*(1+0.006739496742/2*Math.pow(0.5*Math.log((1+Math.cos(Lat*Math.PI/180)*Math.sin((Lon*Math.PI/180-(6*Zone -183)*Math.PI/180)))/(1-Math.cos(Lat*Math.PI/180)*Math.sin((Lon*Math.PI/180-(6*Zone -183)*Math.PI/180)))),2)*Math.pow(Math.cos(Lat*Math.PI/180),2))+0.9996*6399593.625*(Lat*Math.PI/180-0.005054622556*(Lat*Math.PI/180+Math.sin(2*Lat*Math.PI/180)/2)+4.258201531e-05*(3*(Lat*Math.PI/180+Math.sin(2*Lat*Math.PI/180)/2)+Math.sin(2*Lat*Math.PI/180)*Math.pow(Math.cos(Lat*Math.PI/180),2))/4-1.674057895e-07*(5*(3*(Lat*Math.PI/180+Math.sin(2*Lat*Math.PI/180)/2)+Math.sin(2*Lat*Math.PI/180)*Math.pow(Math.cos(Lat*Math.PI/180),2))/4+Math.sin(2*Lat*Math.PI/180)*Math.pow(Math.cos(Lat*Math.PI/180),2)*Math.pow(Math.cos(Lat*Math.PI/180),2))/3);
            if (Letter<'M')
                Northing = Northing + 10000000;
            Northing=Math.round(Northing*100)*0.01;
        }
    }

    @Override
    public void reset() {
        this.jp2resources = new JP2MetadataResources();
    }
}
