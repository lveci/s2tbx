package org.esa.s2tbx.imagewriter;

import javax.imageio.metadata.IIOMetadata;
import javax.xml.stream.*;
import java.awt.image.RenderedImage;
import java.io.FileOutputStream;
import java.util.logging.Logger;


/**
 * Class that prints the JP2 XML header
 * Created by rdumitrascu on 11/15/2016.
 */
public class JP2XmlGenerator {
    private final FileOutputStream fileOutputStream;
    private final IIOMetadata imgMetadata;
    private final RenderedImage renderedImage;
    private String srsName;
    private final static Logger logger = Logger.getLogger(JP2ImageWriterSpi.class.getName());
    private int depth=0;

    /**
     *
     * @param fileOutputStream
     * @param img
     * @param metadata
     * @param srsName
     * @throws XMLStreamException
     */
    public  JP2XmlGenerator(FileOutputStream fileOutputStream, RenderedImage img, IIOMetadata metadata,String srsName )throws XMLStreamException{
        if(fileOutputStream == null){
            logger.warning("no fileOutputStream has been set");
            throw new IllegalArgumentException();
        }
        if(img ==null){
            logger.warning("no img has been received");
            throw new IllegalArgumentException();
        }
       /* if(metadata == null){
            logger.warning("no metadata has been received");
            throw new IllegalArgumentException();
        }*/
        this.fileOutputStream = fileOutputStream;
        this.imgMetadata = metadata;
        this.renderedImage = img;
        this.srsName = srsName;
        xmlJP2WriteStream(fileOutputStream);

    }

    /**
     *
     * @param fileOutputStream
     * @throws XMLStreamException
     */
    private void xmlJP2WriteStream(FileOutputStream fileOutputStream ) throws XMLStreamException {

        XMLOutputFactory outputFactory = XMLOutputFactory.newFactory();
        XMLStreamWriter tmpWriter = outputFactory.createXMLStreamWriter(fileOutputStream);
        writeStartDocument(tmpWriter);
        tmpWriter.flush();
        tmpWriter.close();
    }

    /**
     *
     * @param tmpWriter
     * @throws XMLStreamException
     */
    private void writeStartDocument(XMLStreamWriter tmpWriter) throws XMLStreamException {
        tmpWriter.writeStartDocument("UTF-8", "1.0");
        tmpWriter.writeCharacters("\n");
        tmpWriter.writeStartElement("gml:FeatureCollection");
        tmpWriter.writeNamespace("gml","http://www.opengis.net/gml");
        tmpWriter.writeNamespace("xsi","http://www.w3.org/2001/XMLSchema-instance");
        tmpWriter.writeAttribute("http://www.w3.org/2001/XMLSchema-instance","schemaLocation","http://www.opengeospatial.net/gml http://schemas.opengis.net/gml/3.1.1/profiles/gmlJP2Profile/1.0.0/gmlJP2Profile.xsd");
        writeFutureMemberTags(tmpWriter);
        tmpWriter.writeEndDocument();
        tmpWriter.writeCharacters("\n");

    }

    /**
     *
     * @param tmpWriter
     * @throws XMLStreamException
     */
    private void writeFutureMemberTags(XMLStreamWriter tmpWriter) throws XMLStreamException {
        tmpWriter.writeStartElement("gml:featureMember");
        tmpWriter.writeStartElement("gml:FeatureCollection");
        tmpWriter.writeStartElement("gml:featureMember");
        writeRectifiedGrid(tmpWriter);
        writeEndElement(3,tmpWriter);
    }

    /**
     *
     * @param tmpWriter
     * @throws XMLStreamException
     */
    private void writeRectifiedGrid(XMLStreamWriter tmpWriter) throws XMLStreamException{
        tmpWriter.writeStartElement("gml:RectifiedGridCoverage");
        tmpWriter.writeAttribute("dimension","2"); //TODO
        tmpWriter.writeAttribute("gml:id","RGC0001"); //TODO
        tmpWriter.writeStartElement("gml:rectifiedGridDomain");
        tmpWriter.writeStartElement("gml:RectifiedGrid");
        tmpWriter.writeAttribute("dimension","2"); //TODO
        tmpWriter.writeStartElement("gml:limits");
        writeGridEnvelope(tmpWriter);
        writeAxis(tmpWriter, "x");
        writeAxis(tmpWriter, "y");
        writeOrigin(tmpWriter);
        writeOffsetVector(tmpWriter, 60, 0);//TODO
        writeOffsetVector(tmpWriter, 0, -60);//TODO
        writeEndElement(2,tmpWriter);
        writeRangeSet(tmpWriter);

    }

    /**
     *
     * @param tmpWriter
     * @throws XMLStreamException
     */
   private void  writeGridEnvelope(XMLStreamWriter tmpWriter)throws XMLStreamException{
       tmpWriter.writeStartElement("gml:GridEnvelope");
       writeImageLimits(tmpWriter, 1, 1, "low");
       writeImageLimits(tmpWriter, this.renderedImage.getWidth(), this.renderedImage.getHeight(), "high");
       writeEndElement(2,tmpWriter);
   }

    /**
     *
     * @param tmpWriter
     * @param x
     * @param y
     * @param position
     * @throws XMLStreamException
     */
    private void writeImageLimits(XMLStreamWriter tmpWriter, int x, int y, String position)throws XMLStreamException{
        tmpWriter.writeStartElement("gml:" + position);
        tmpWriter.writeCharacters(x + " " + y);
        tmpWriter.writeEndElement();
    }

    /**
     *
     * @param tmpWriter
     * @throws XMLStreamException
     */
    private void writeRangeSet(XMLStreamWriter tmpWriter)throws XMLStreamException {
        tmpWriter.writeStartElement("gml:rangeSet");
        tmpWriter.writeStartElement("gml:File");
        tmpWriter.writeStartElement("gml:fileName");
        tmpWriter.writeCharacters("gmljp2://codestream/0"); //TODO
        tmpWriter.writeEndElement();
        tmpWriter.writeStartElement("gml:fileStructure");
        tmpWriter.writeCharacters("Record Interleaved"); //TODO
        writeEndElement(3,tmpWriter);
    }

    /**
     *
     * @param tmpWriter
     * @throws XMLStreamException
     */
    private void writeOrigin(XMLStreamWriter tmpWriter) throws XMLStreamException{
        tmpWriter.writeStartElement("gml:origin");
        tmpWriter.writeStartElement("gml:Point");
        tmpWriter.writeAttribute("gml:id","P0001"); //TODO
        tmpWriter.writeAttribute("srsName",srsName); //TODO
        tmpWriter.writeStartElement("gml:pos");
        tmpWriter.writeCharacters("500010 3800010"); //TODO
        writeEndElement(3,tmpWriter);
    }

    /**
     *
     * @param tmpWriter
     * @param Off1
     * @param Off2
     * @throws XMLStreamException
     */
    private void writeOffsetVector(XMLStreamWriter tmpWriter, int Off1, int Off2 ) throws XMLStreamException{
        tmpWriter.writeStartElement("gml:offsetVector");
        tmpWriter.writeAttribute("srsName",srsName); //TODO
        tmpWriter.writeCharacters(Off1 + " " + Off2); //TODO
        tmpWriter.writeEndElement();
    }

    /**
     *
     * @param tmpWriter
     * @param axis
     * @throws XMLStreamException
     */
    private void writeAxis(XMLStreamWriter tmpWriter, String axis)throws XMLStreamException  {
        tmpWriter.writeStartElement("gml:axisName");
        tmpWriter.writeCharacters(axis);
        tmpWriter.writeEndElement();
    }

    /**
     *
     * @param numberOfElementsToClose
     * @param tmpXMLWriter
     * @throws XMLStreamException
     */
    private  void writeEndElement(int numberOfElementsToClose,XMLStreamWriter tmpXMLWriter) throws XMLStreamException {
        for(int index=0; index<numberOfElementsToClose;index++){
            tmpXMLWriter.writeEndElement();
        }
    }
}
