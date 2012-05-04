package prefuse.util.display;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import net.sf.epsgraphics.ColorMode;
import net.sf.epsgraphics.EpsGraphics;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.Document;
import prefuse.Display;
import prefuse.Visualization;

/**
 *
 * @author gentie
 */
public class VectorExporter extends ImageExporter {

    private String title;

    public VectorExporter(String title) {
        this(Visualization.ALL_ITEMS, title);
    }

    public VectorExporter(String group, String title) {
        super(group, Format.names());
        this.title = title;
    }

    @Override
    protected BufferedImage createBufferImage(Display display, int width, int height) {
        return null;
    }

    @Override
    protected Graphics2D createGraphics(Rectangle2D imageBounds, String format, OutputStream out)
            throws IOException, UnsupportedOperationException {

        switch (Format.valueOf(format.toUpperCase())) {
            case EPS:
                EpsGraphics eps = new EpsGraphics(title, out,
                        0, 0, (int) imageBounds.getWidth(), (int) imageBounds.getHeight(),
                        ColorMode.COLOR_RGB);
                eps.setAccurateTextMode(true);
                return eps;
            case SVG:
                Document document = GenericDOMImplementation.getDOMImplementation().createDocument(
                        "http://www.w3.org/2000/svg", Format.SVG.toString(), null);
                SVGGraphics2D svg = new SVGGraphics2D(document);
                return svg;
            default:
                throw new UnsupportedOperationException("Unsupported format : " + format);
        }
    }

    @Override
    protected void finishDocument(Graphics2D g2D, String format, OutputStream out)
            throws IOException, UnsupportedOperationException {
        switch (Format.valueOf(format.toUpperCase())) {
            case EPS:
                EpsGraphics eps = (EpsGraphics) g2D;
                eps.flush();
                eps.close();
                break;
            case SVG:
                SVGGraphics2D svg = (SVGGraphics2D) g2D;
                Writer writer = new OutputStreamWriter(out);
                svg.stream(writer, true);
                writer.flush();
                writer.close();
                break;
            default:
                throw new UnsupportedOperationException("Unsupported format : " + format);
        }
    }

    public static enum Format {

        EPS, SVG;

        public static String[] names() {
            Format[] values = values();
            String[] names = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                names[i] = values[i].toString().toLowerCase();
            }
            return names;
        }
    }
}
