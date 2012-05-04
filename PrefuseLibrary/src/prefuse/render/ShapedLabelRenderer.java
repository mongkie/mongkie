package prefuse.render;

import java.awt.Graphics2D;
import java.awt.Shape;
import prefuse.visual.VisualItem;

/**
 *
 * @author lunardo
 */
public class ShapedLabelRenderer extends AbstractShapeRenderer {

    private ShapeRenderer shapeRenderer;
    private LabelRenderer labelRenderer;
    private boolean shapeOn = false;

    public ShapedLabelRenderer() {

        shapeRenderer = new ShapeRenderer();

        labelRenderer = new LabelRenderer();
        labelRenderer.setRenderType(RENDER_TYPE_NONE);
    }

    public ShapedLabelRenderer(String textField, int size) {

        shapeRenderer = new ShapeRenderer(size);
        shapeRenderer.setRenderType(RENDER_TYPE_NONE);

        labelRenderer = new LabelRenderer(textField);
        labelRenderer.setRoundedCorner(8, 8);
    }

    public void activateShape(boolean shapeOn) {

        if (this.shapeOn == shapeOn) {
            return;
        }

        if (shapeOn) {
            shapeRenderer.setRenderType(RENDER_TYPE_DRAW_AND_FILL);
            labelRenderer.setRenderType(RENDER_TYPE_NONE);
        } else {
            shapeRenderer.setRenderType(RENDER_TYPE_NONE);
            labelRenderer.setRenderType(RENDER_TYPE_DRAW_AND_FILL);
        }

        this.shapeOn = shapeOn;
    }

    public void setTextField(String text) {
        labelRenderer.setTextField(text);
    }

    public boolean isShapeActivated() {
        return shapeOn;
    }

    @Override
    public void render(Graphics2D g, VisualItem item) {

        if (shapeOn) {
            shapeRenderer.render(g, item);
        }

        labelRenderer.render(g, item);
    }

    @Override
    protected Shape getRawShape(VisualItem item) {
        return (shapeOn) ? shapeRenderer.getRawShape(item) : labelRenderer.getRawShape(item);
    }
}