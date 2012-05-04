package prefuse.render;

import java.awt.Image;
import prefuse.util.io.IOLib;

/**
 *
 * @author lunardo
 */
public class DataIconLabelRenderer extends LabelRenderer {

    private final ImageFactory imageFactory = new ImageFactory();

    public DataIconLabelRenderer(String textField, String imageField) {
        super(textField, imageField);
        setImageFactory(imageFactory);
    }

    public ImageFactory addImage(String location, Image image) {
        imageFactory.addImage(location, image);
        return imageFactory;
    }

    public Image addImage(String location, Class clazz, String resourceName) {
        Image image = IOLib.getImage(clazz, resourceName);
        imageFactory.addImage(location, image);
        return image;
    }
}
