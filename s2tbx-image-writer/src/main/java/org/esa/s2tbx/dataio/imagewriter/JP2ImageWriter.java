package org.esa.s2tbx.dataio.imagewriter;

import org.esa.s2tbx.dataio.jp2.Box;
import org.esa.s2tbx.dataio.jp2.BoxReader;
import org.esa.s2tbx.dataio.jp2.BoxType;
import org.esa.s2tbx.dataio.openjp2.OpenJP2Encoder;

import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.FileImageInputStream;
import javax.xml.stream.*;
import java.awt.image.*;
import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * Created by rdumitrascu on 10/18/2016.
 */
public class JP2ImageWriter extends ImageWriter {

    private static final int DEFAULT_NUMBER_OF_RESOLUTIONS = 1;
    private static final int XML_BOX_HEADER_TYPE = 0x786D6C20;
    private static final Logger logger = Logger.getLogger(JP2ImageWriter.class.getName());

    private File fileOutput;
    private IIOImage sourceImage;
    private RenderedImage renderedImage;
    private int numbResolution = DEFAULT_NUMBER_OF_RESOLUTIONS;
    private IIOMetadata imageMetadata;
    private BoxReader boxReader;
    private int headerSize;
    private JP2Metadata createdStreamMetadata;
    private JP2Metadata createdImageMetadata;

    /**
     *
     */
    public JP2ImageWriter() {
        super(null);
    }

    /**
     * Function to set the image output stream
     *
     * @param output
     */
    public void setOutput(Object output) {
        super.setOutput(output);
        if (output != null) {
            if (!(output instanceof File)) {
                throw new IllegalArgumentException
                        ("output not an File!");
            }
            this.fileOutput = (File) output;
        } else {
            this.fileOutput = null;
        }
    }

    /**
     * @return the <code>Object</code> that was specified using
     * <code>setOutput</code>, or <code>null</code>.
     * @see #setOutput
     */
    public Object getOutput() {

        return this.fileOutput;
    }

    @Override
    public IIOMetadata getDefaultStreamMetadata(ImageWriteParam param) {
        return new JP2Metadata(param, this);
    }

    @Override
    public IIOMetadata getDefaultImageMetadata(ImageTypeSpecifier imageType, ImageWriteParam param) {
        return new JP2Metadata(imageType, param, this);
    }

    @Override
    public IIOMetadata convertStreamMetadata(IIOMetadata inData, ImageWriteParam param) {

        if (inData instanceof JP2Metadata) {
            JP2Metadata jpegData = (JP2Metadata) inData;
            if (jpegData.isStream) {
                return inData;
            }
        }
        return null;
    }

    @Override
    public IIOMetadata convertImageMetadata(IIOMetadata inData, ImageTypeSpecifier imageType, ImageWriteParam param) {

        if (inData instanceof JP2Metadata) {
            JP2Metadata jpegData = (JP2Metadata) inData;
            // stream metadata can be covnerted to image metadata
            return inData;
        }
        return null;
    }

    @Override
    public ImageWriteParam getDefaultWriteParam() {
        return new ImageWriteParam(getLocale());
    }

    @Override
    public void write(IIOMetadata streamMetadata, IIOImage image, ImageWriteParam param) throws IOException {
        if (image == null) {
            logger.info("No image has been received");
            throw new IllegalArgumentException("input image is null!");
        }
        //verify if the image output stream has been set
        if (this.fileOutput == null) {
            logger.info("Output has not been set");
            throw new IllegalStateException("Output has not been set!");
        }
        if (this.fileOutput.exists()) {
            try (RandomAccessFile file = new RandomAccessFile(this.fileOutput, "rws")) {
                file.setLength(0);
            } catch (IOException e) {
                logger.warning(e.getMessage());
            }
        }
        if (streamMetadata != null) {
            this.createdStreamMetadata = (JP2Metadata)convertStreamMetadata(streamMetadata, null);
        }
        this.imageMetadata = image.getMetadata();
        if(this.imageMetadata != null){
            ImageTypeSpecifier type =
                    ImageTypeSpecifier.createFromRenderedImage(this.renderedImage);
            this.createdImageMetadata =
                    (JP2Metadata)convertImageMetadata(this.imageMetadata, type, null);
        }
        this.sourceImage = image;
        this.renderedImage = this.sourceImage.getRenderedImage();
        this.imageMetadata = this.sourceImage.getMetadata();
        try (OpenJP2Encoder jp2Encoder = new OpenJP2Encoder(renderedImage)) {
            Path outputStreamPath = FileSystems.getDefault().getPath(this.fileOutput.getPath());
            jp2Encoder.write(outputStreamPath, getNumberResolutions());
        } catch (Exception e) {
            logger.warning(e.getMessage());
        }
        if((this.createdStreamMetadata!=null)||(this.createdImageMetadata != null)) {
            this.headerSize= computeHeaderSize();
            if (this.fileOutput.exists()) {
                try (RandomAccessFile file = new RandomAccessFile(this.fileOutput, "rws")) {
                    int fileLenght = (int) file.length();
                    byte[] headerStream = new byte[this.headerSize];
                    file.read(headerStream, 0, this.headerSize);
                    file.seek(0);
                    byte[] ccStream = new byte[(int) file.length()-this.headerSize];
                    file.seek(headerSize);
                    file.read(ccStream, 0, fileLenght - this.headerSize);
                    file.setLength(0);
                    file.write(headerStream, 0, this.headerSize);
                    File tempXMLFile = File.createTempFile(this.fileOutput.getName(), ".xml");
                    try (FileOutputStream fop = new FileOutputStream(tempXMLFile, true)) {
                        XMLBoxWriter xmlWriter = new XMLBoxWriter();
                        if(this.createdStreamMetadata !=null) {
                            xmlWriter.writeStream(fop, this.createdStreamMetadata.jp2resources);
                        }else{
                            xmlWriter.writeStream(fop, this.createdImageMetadata.jp2resources);
                        }
                    } catch (XMLStreamException e) {
                        logger.warning(e.getMessage());
                    }
                    try (RandomAccessFile temporatyFile = new RandomAccessFile(tempXMLFile, "rws")) {
                        byte[] xmlStream = new byte[(int) temporatyFile.length()];
                        temporatyFile.read(xmlStream, 0, (int) temporatyFile.length());
                        file.writeInt(8 + xmlStream.length);
                        file.writeInt(XML_BOX_HEADER_TYPE);
                        file.write(xmlStream);

                    } catch (IOException e) {
                        logger.warning(e.getMessage());
                    }
                    file.write(ccStream);
                    if(tempXMLFile.exists()){
                        tempXMLFile.delete();
                    }
                } catch (IOException e) {
                    logger.warning(e.getMessage());
                } catch (Exception e) {
                    logger.warning(e.getMessage());
                }
            }
        }
    }

    /**
     * sets the number of resolutions for the image to be encoded
     * @param numResolutions the number of resolutions
     */
    public void setNumberResolution(int numResolutions) {
        this.numbResolution = numResolutions;
    }

    /**
     *
     * @return the number of resoltuions that the image has to be encoded with
     */
    public int getNumberResolutions() {
        return this.numbResolution;
    }

    private static class BoxListener implements BoxReader.Listener {
        @Override
        public void knownBoxSeen(Box box) {
            System.out.println("known box: " + BoxType.encode4b(box.getCode()));
        }
        @Override
        public void unknownBoxSeen(Box box) {
            System.out.println("unknown box: " + BoxType.encode4b(box.getCode()));
        }
    }

    /**
     * Searches for the location of the code continuous stream in the encoded image
     *
     * @return returns the size of the header from the original encoded image
     */
    private int computeHeaderSize(){
        int headerSize = 0;
        try (FileImageInputStream file = new FileImageInputStream(this.fileOutput)) {
            this.boxReader = new BoxReader(file, file.length(), new BoxListener());
            this.boxReader.getFileLength();
            Box box =null;
            do {
                box = this.boxReader.readBox();
                if(box.getSymbol().equals("jp2c"))
                    headerSize = (int)box.getPosition();
            }
            while (!box.getSymbol().equals("jp2c")) ;
        } catch (IOException e) {
            logger.warning(e.getMessage());
        }
        return headerSize;
    }

}
