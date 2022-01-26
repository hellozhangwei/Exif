
import javax.imageio.ImageIO
import java.awt.AlphaComposite
import java.awt.Color
import java.awt.Font
import java.awt.FontMetrics
import java.awt.Graphics2D
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffField
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants

import java.nio.file.Files
import java.nio.file.attribute.FileTime;

/**
 * see https://github.com/apache/commons-imaging/blob/master/src/test/java/org/apache/commons/imaging/examples/MetadataExample.java
 * gradle run --args="/Users/weizhang/Downloads/DSC_0001.JPG foo2r"
 * gradle run --args="/Users/weizhang/Downloads/pic/source /Users/weizhang/Downloads/pic/dest"
 */
public class MetadataExample {
    static void main(String[] args) {
        if(args?.length<2) {
            println "Please run with gradle run --args=\"[source path or file name] [dest folder name]\" "
            return
        }

        def sourceFile = args[0]
        def destFolder = args[1]

        process(sourceFile, destFolder)
    }

    static process(String sourceFile, String destFolder) {
        def file = new File(sourceFile)
        if(!file.exists()) {
            println "The soruce file or path ${sourceFile} dose not exist!"
            return
        }

        if(!(new File(destFolder).exists())) {
            println "The dest folder ${destFolder} dose not exist!"
            return
        }

        if(file.isDirectory()) {
            def listFiles = file.listFiles()
            listFiles.each { File it->
                addDateWatermark(it, new File(destFolder + "/" + it.getName()))
            }
        } else {
            addDateWatermark(file, new File(destFolder + "/" + file.getName()))
        }
    }


    static void addDateWatermark(File sourceImageFile, File destImageFile) {

        def text = getDateTime(sourceImageFile)

        if(!text) {
            return
        }

        try {
            BufferedImage sourceImage = ImageIO.read(sourceImageFile);
            Graphics2D g2d = (Graphics2D) sourceImage.getGraphics();

            AlphaComposite alphaChannel = AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 1f);

            g2d.setComposite(alphaChannel);
            g2d.setColor(Color.RED);
            g2d.setFont(new Font("Arial", Font.BOLD, 64));

            FontMetrics fontMetrics = g2d.getFontMetrics();
            Rectangle2D rect = fontMetrics.getStringBounds(text, g2d);

            int centerX = sourceImage.getWidth() -700;
            int centerY = sourceImage.getHeight()-100;

            // paints the textual watermark
            g2d.drawString(text, centerX, centerY);

            ImageIO.write(sourceImage, "jpg", destImageFile);
            g2d.dispose();

            System.out.println("Added watermark to the ${destImageFile}");

        } catch (IOException ex) {
            System.err.println(ex);
        }
    }

    static String getDateTime(final File file) throws ImageReadException, IOException {
        final ImageMetadata metadata = Imaging.getMetadata(file);

        if (metadata instanceof JpegImageMetadata) {
            final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata

            final TiffField field = jpegMetadata.findEXIFValueWithExactMatch(TiffTagConstants.TIFF_TAG_DATE_TIME)

            if (field == null) {
                return ""
            }
            return field.getValueDescription().replaceAll("'", "")
        } else {
            FileTime creationTime = (FileTime) Files.getAttribute(file.toPath(), "creationTime")
            return creationTime.toString()
        }

    }

}