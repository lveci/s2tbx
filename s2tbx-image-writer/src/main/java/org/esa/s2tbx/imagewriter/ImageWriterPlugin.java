package org.esa.s2tbx.imagewriter;

import org.esa.s2tbx.dataio.jp2.Box;
import org.esa.s2tbx.dataio.jp2.BoxReader;
import org.esa.s2tbx.dataio.jp2.BoxType;
import org.esa.s2tbx.dataio.openjp2.OpenJP2Encoder;
import sun.awt.image.SunWritableRaster;
import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.FileImageInputStream;
import javax.xml.stream.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * Created by rdumitrascu on 10/18/2016.
 */
public class ImageWriterPlugin extends ImageWriter {

    private static final int DEFAULT_NUMBER_OF_RESOLUTIONS = 1;
    private static final int XML_BOX_HEADER_TYPE = 0x786D6C20;
    private static final Logger logger = Logger.getLogger(ImageWriterPlugin.class.getName());
    private static final int[][] bandOffsets = {{0},
            {0, 1},
            {0, 1, 2},
            {0, 1, 2, 3}};

    private File fileOutput;
    private IIOImage sourceImage;
    private RenderedImage renderedImage;
    private Rectangle sourceRegion;
    private int numbResolution = DEFAULT_NUMBER_OF_RESOLUTIONS;
    private IIOMetadata metadata;
    private BoxReader boxReader;
    /**
     *
     */
    public ImageWriterPlugin() {
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
         //return new JP2MetadataFormat();
        return null;
    }

    @Override
    public IIOMetadata getDefaultImageMetadata(ImageTypeSpecifier imageType, ImageWriteParam param) {
        return null;
    }

    @Override
    public IIOMetadata convertStreamMetadata(IIOMetadata inData, ImageWriteParam param) {
       /* if (inData instanceof JP2MetadataFormat) {
            return inData;
        } else {
            return null;
        }*/
        return null;
    }

    @Override
    public IIOMetadata convertImageMetadata(IIOMetadata inData, ImageTypeSpecifier imageType, ImageWriteParam param) {
        return null;
    }

    /**
     * Appends a complete image stream containing a single image and
     * associated stream and image metadata and thumbnails to the
     * output.  Any necessary header information is included.  If the
     * output is an <code>ImageOutputStream</code>, its existing
     * contents prior to the current seek position are not affected,
     * and need not be readable or writable.
     *
     * @param streamMetadata an <code>IIOMetadata</code> object representing
     *                       stream metadata, or <code>null</code> to use default values.
     * @param image          an <code>IIOImage</code> object containing an
     *                       image, thumbnails, and metadata to be written.
     * @param param          an <code>ImageWriteParam</code>, or
     *                       <code>null</code> to use a default
     *                       <code>ImageWriteParam</code>.
     * @throws IllegalStateException         if the output has not
     *                                       been set.
     * @throws UnsupportedOperationException if <code>image</code>
     *                                       contains a <code>Raster</code> and <code>canWriteRasters</code>
     *                                       returns <code>false</code>.
     * @throws IllegalArgumentException      if <code>image</code> is
     *                                       <code>null</code>.
     * @throws IOException                   if an error occurs during writing.
     */
    @Override
    public void write(IIOMetadata streamMetadata, IIOImage image, ImageWriteParam param) throws IOException {
        if (image == null) {
            logger.info("No image has been received");
            throw new IllegalArgumentException("input image is null!");
        }
        //verify if the image output stream has been set
        if (fileOutput == null) {
            logger.info("Output has not been set");
            throw new IllegalStateException("Output has not been set!");
        }
        if (fileOutput.exists()) {
            try (RandomAccessFile file = new RandomAccessFile(this.fileOutput, "rws")) {
                file.setLength(0);
            } catch (IOException e) {
                logger.warning(e.getMessage());
            }
        }
        if (streamMetadata != null) {
            this.metadata = streamMetadata;
        }
        int headerSize = 0;
        this.sourceImage = image;
        this.renderedImage = sourceImage.getRenderedImage();
        this.sourceRegion = new Rectangle(0, 0, renderedImage.getWidth(), renderedImage.getHeight());
        int sourceXSubsampling = 1;
        int sourceYSubsampling = 1;
        int[] sourceBands = null;
        if (param != null) {
            if (param.getSourceRegion() != null) {
                sourceRegion = sourceRegion.intersection(param.getSourceRegion());
            }
            sourceXSubsampling = param.getSourceXSubsampling();
            sourceYSubsampling = param.getSourceYSubsampling();
            sourceBands = param.getSourceBands();
            int subsampleXOffset = param.getSubsamplingXOffset();
            int subsampleYOffset = param.getSubsamplingYOffset();
            sourceRegion.x += subsampleXOffset;
            sourceRegion.y += subsampleYOffset;
            sourceRegion.width -= subsampleXOffset;
            sourceRegion.height -= subsampleYOffset;
        }
        int renderedImageHeight = this.renderedImage.getHeight();
        int renderedImageWidth = this.renderedImage.getWidth();
        Raster imRas = renderedImage.getData(sourceRegion);
        int numBands = imRas.getNumBands();
        int[] bandSizes = imRas.getSampleModel().getSampleSize();
        int dataType = getDataType(bandSizes[0]);
        int lineSize = renderedImageWidth * numBands;
        DataBuffer buffer = getSpecificDataBuffer(dataType, lineSize);
        int[] bandOffs = bandOffsets[0];
        SampleModel sampleModel = new PixelInterleavedSampleModel(dataType, renderedImageWidth, renderedImageHeight, 1, renderedImageWidth, bandOffs);
        try {
            final WritableRaster wRaster = new SunWritableRaster(sampleModel, buffer, new Point(0, 0));
            try (OpenJP2Encoder jp2Encoder = new OpenJP2Encoder(renderedImage)) {
                Path outputStreamPath = FileSystems.getDefault().getPath(this.fileOutput.getPath());
                jp2Encoder.write(outputStreamPath, getNumberResolutions());
            } catch (Exception e) {
                logger.warning(e.getMessage());
            }
        } catch (Exception exp) {
            logger.warning(exp.getMessage());
        }
        try (FileImageInputStream file = new FileImageInputStream(this.fileOutput)) {
            boxReader = new BoxReader(file, file.length(), new BoxListener());
            boxReader.getFileLength();
            Box box =null;
            do {
                box = boxReader.readBox();
                if(box.getSymbol().equals("jp2c"))
                    headerSize = (int)box.getPosition();
            }
            while (!box.getSymbol().equals("jp2c")) ;
        } catch (IOException e) {
            logger.warning(e.getMessage());
        }
        File tempFile = File.createTempFile(this.fileOutput.getName(), ".tmp");
        if (fileOutput.exists()) {
            try (RandomAccessFile file = new RandomAccessFile(this.fileOutput, "rws")) {
                int fileLenght = (int)file.length();
                byte[] headerStream = new byte[headerSize];
                file.read(headerStream,0,headerSize);
                file.seek(0);
                try(RandomAccessFile temporatyFile = new RandomAccessFile(tempFile, "rws")){
                    byte[] ccStream = new byte[(int) file.length()];
                    file.seek(headerSize);
                    file.read(ccStream,headerSize, fileLenght-headerSize);
                    temporatyFile.write(ccStream,headerSize,ccStream.length-headerSize);
                } catch (IOException e) {
                    logger.warning(e.getMessage());
                }
                file.read(headerStream,0,headerSize);
                file.setLength(0);
                file.write(headerStream,0,headerSize);
                File tempXMLFile = File.createTempFile(this.fileOutput.getName(), ".xml");
                try (FileOutputStream fop = new FileOutputStream(tempXMLFile,true )){
                    JP2XmlGenerator.xmlJP2WriteStream(fop, this.renderedImage, this.metadata);
                } catch (XMLStreamException e) {
                    logger.warning(e.getMessage());
                }
                try(RandomAccessFile temporatyFile = new RandomAccessFile(tempXMLFile, "rws")){
                    byte[] xmlStream = new byte[(int) temporatyFile.length()];
                    temporatyFile.read(xmlStream,0,(int) temporatyFile.length());
                    file.writeInt(8 + xmlStream.length);
                    file.writeInt(XML_BOX_HEADER_TYPE);
                    file.write(xmlStream);
                }catch (IOException e) {
                    logger.warning(e.getMessage());
                }
                try(RandomAccessFile temporatyFile = new RandomAccessFile(tempFile, "rws")){
                    byte[] ccStream = new byte[(int) temporatyFile.length()];
                    temporatyFile.read(ccStream,0,(int) temporatyFile.length());
                    file.write(ccStream);
                }catch (IOException e) {
                    logger.warning(e.getMessage());
                }
            } catch (IOException e) {
                logger.warning(e.getMessage());
            }catch (Exception e) {
                logger.warning(e.getMessage());
            }
        }
        if(tempFile.exists()) {
            tempFile.delete();
        }
    }

    /**
     * @param bandSize the size of the band
     * @return returns the data Type for the specific band size
     * @throws IIOException
     */
    private int getDataType(final int bandSize) throws IIOException {
        switch (bandSize) {
            case 8:
                return DataBuffer.TYPE_BYTE;
            case 16:
                return DataBuffer.TYPE_USHORT;
            case 32:
                return DataBuffer.TYPE_INT;
            case 64:
                return DataBuffer.TYPE_DOUBLE;
            default:
                logger.warning("Sample size not standard ");
                throw new IIOException("Sample size must be 8, 16, 32 or 64");
        }
    }

    /**
     *  Returns a dataBuffer for the specific dataBufferSize
     * @param dataBufferSize the dataBuffer size
     * @param lineSize
     * @param <T>
     * @return
     * @throws IIOException
     */
    public <T extends DataBuffer> T getSpecificDataBuffer(final int dataBufferSize, int lineSize) throws IIOException {
        switch (dataBufferSize) {
            case DataBuffer.TYPE_BYTE:
                return (T) new DataBufferByte(lineSize);
            case DataBuffer.TYPE_USHORT:
                return (T) new DataBufferUShort(lineSize);
            case DataBuffer.TYPE_SHORT:
                return (T) new DataBufferUShort(lineSize);
            case DataBuffer.TYPE_INT:
            case DataBuffer.TYPE_FLOAT:
                return (T) new DataBufferInt(lineSize);
            case DataBuffer.TYPE_DOUBLE:
                return (T) new DataBufferDouble(lineSize);
            default:
                logger.warning("Sample size not standard ");
                throw new IIOException("Sample size must be 8, 16, 32 or 64");
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
}
