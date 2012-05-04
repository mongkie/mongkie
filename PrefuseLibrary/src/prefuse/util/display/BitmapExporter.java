package prefuse.util.display;

import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import javax.imageio.ImageIO;
import prefuse.Display;
import prefuse.Visualization;

/**
 *
 * @author gentie
 */
public class BitmapExporter extends ImageExporter {

    public BitmapExporter() {
        this(Visualization.ALL_ITEMS);
    }

    public BitmapExporter(String group) {
        super(group, ImageIO.getWriterFormatNames());
    }

    @Override
    protected BufferedImage createBufferImage(Display display, int width, int height) {

        BufferedImage bufferImage = null;

        if (!GraphicsEnvironment.isHeadless()) {
            try {
                bufferImage = (BufferedImage) display.createImage(width, height);
            } catch (Exception ex) {
                bufferImage = null;
            }
        }

        if (bufferImage == null) {
            return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        }

        return bufferImage;
    }

    @Override
    protected Graphics2D createGraphics(Rectangle2D imageBounds, String format, OutputStream out)
            throws IOException, UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void finishDocument(Graphics2D g2D, String format, OutputStream out)
            throws IOException, UnsupportedOperationException {
    }
}
