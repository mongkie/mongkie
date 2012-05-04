package prefuse.util.display;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import prefuse.Display;
import prefuse.util.GraphicsLib;
import prefuse.util.io.IOLib;
import prefuse.util.io.SimpleFileFilter;

/**
 * See http://goosebumps4all.net/34all/bb/showthread.php?tid=243
 * @author Marcus St&auml;nder (<a href="mailto:webmaster@msdevelopment.org">webmaster@msdevelopment.org</a>)
 * @author gentie
 */
public abstract class ImageExporter {

    /** The FileChooser to select the file to save to */
    private JFileChooser chooser = null;
    private String group;
    private String[] formatNames;

    //~--- Constructors -------------------------------------------------------
    protected ImageExporter(String group, String[] formatNames) {
        this.group = group;
        this.formatNames = Arrays.copyOf(formatNames, formatNames.length);
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String[] getFormatNames() {
        return formatNames;
    }

    //~--- Methods ------------------------------------------------------------
    /**
     * This method initiates the chooser components, detecting available image formats
     *
     */
    private void initializeChooser() {

        // Initialize the chooser
        chooser = new JFileChooser();
        chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        chooser.setDialogTitle("Export graph as image");
        chooser.setAcceptAllFileFilterUsed(false);

        HashSet<String> availableFormatSet = new HashSet<String>();
        for (int i = 0; i < formatNames.length; i++) {
            String format = formatNames[i].toLowerCase();
            if (!availableFormatSet.contains(format)) {
                availableFormatSet.add(format);
                chooser.setFileFilter(new SimpleFileFilter(format, format.toUpperCase() + " Image (*." + format + ")"));
            }
        }
        availableFormatSet.clear();
        availableFormatSet = null;
    }

    /**
     * This method lets the user select the target file and exports the <code>Display</code>
     *
     * @paran display the <code>Display</code> to export
     *
     */
    public void export(Display display) {

        // Initialize if needed
        if (chooser == null) {
            initializeChooser();
        }

        // open image save dialog
        File outFile = null;
        int returnVal = chooser.showSaveDialog(display);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            outFile = chooser.getSelectedFile();
        } else {
            return;
        }

        String format = ((SimpleFileFilter) chooser.getFileFilter()).getExtension();
        String ext = IOLib.getExtension(outFile);

        if (!format.equals(ext)) {
            outFile = new File(outFile.toString() + "." + format);
        }

        // Now save the image
        boolean exported = false;
        String errorMessage = null;
        try {
            OutputStream out = new BufferedOutputStream(new FileOutputStream(outFile));
            exported = export(display, out, format);
            out.flush();
            out.close();
        } catch (Exception ex) {
            exported = false;
            errorMessage = ex.getMessage();
            ex.printStackTrace();
        }

        // show result dialog on failure
        if (!exported) {
            JOptionPane.showMessageDialog(display, errorMessage, "Export Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean export(Display display, OutputStream out, String format)
            throws IOException, UnsupportedOperationException {

        // Now comes the nice part

        // Get the bounding box
        Rectangle2D imageBounds = display.getVisualization().getBounds(group);

        // Some little extra spacing
        GraphicsLib.expand(imageBounds, 10 + (int) (1 / display.getScale()));

        // Get a buffered image to draw into
        BufferedImage bufferImage = createBufferImage(display, (int) imageBounds.getWidth(), (int) imageBounds.getHeight());
        Graphics2D g2D = (bufferImage == null) ? createGraphics(imageBounds, format, out) : (Graphics2D) bufferImage.getGraphics();

        /*
         * Set up the display, render, then revert to normal settings
         */

        // The zoom point, zooming should not change anything else than the scale
        Point2D zoomPoint = new Point2D.Double(0, 0);

        // Get and remember the current scaling
        Double scale = display.getScale();

        // Change scale to normal (1)
        display.zoom(zoomPoint, 1 / scale);

        boolean isHighQuality = display.isHighQuality();
        display.setHighQuality(true);

        // Remember the current point
        Point2D currentPoint = new Point2D.Double(display.getDisplayX(), display.getDisplayY());

        // Now pan so the most left element is at the left side of the display and
        // the highest element is at the top.
        display.panToAbs(new Point2D.Double(imageBounds.getMinX() + display.getWidth() / 2,
                imageBounds.getMinY() + display.getHeight() / 2));

        // Now lets prefuse to the actual painting
        display.damageReport();
        display.paintDisplay(g2D, new Dimension((int) imageBounds.getWidth(), (int) imageBounds.getHeight()));

        // Undo the panning, zooming and reset the quality mode
        display.panToAbs(new Point2D.Double(currentPoint.getX() + display.getWidth() / 2,
                currentPoint.getY() + display.getHeight() / 2));
        display.setHighQuality(isHighQuality);
        display.zoom(zoomPoint, scale);    // also takes care of damage report

        // Save the image and return
        if (bufferImage == null) {
            finishDocument(g2D, format, out);
        } else {
            ImageIO.write(bufferImage, format, out);
        }

        return true;
    }

    protected abstract BufferedImage createBufferImage(Display display, int width, int height);

    protected abstract Graphics2D createGraphics(Rectangle2D imageBounds, String format, OutputStream out)
            throws IOException, UnsupportedOperationException;

    protected abstract void finishDocument(Graphics2D g2D, String format, OutputStream out)
            throws IOException, UnsupportedOperationException;
}
