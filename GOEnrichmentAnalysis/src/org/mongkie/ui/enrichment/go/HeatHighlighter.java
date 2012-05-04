package org.mongkie.ui.enrichment.go;

import java.awt.Color;
import java.awt.Component;
import org.jdesktop.swingx.decorator.AbstractHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import gobean.GoBranch;
import org.mongkie.gobean.EnrichedResult;
import org.mongkie.enrichment.go.EnrichedGoId;
import org.mongkie.ui.enrichment.go.util.UIUtilities;
import static org.jdesktop.swingx.color.ColorUtil.blend;

public class HeatHighlighter extends AbstractHighlighter {

    private int pColumn;
    private EnrichedResult result;

    public HeatHighlighter(int pColumn, EnrichedResult result) {
        super(null);
        this.pColumn = pColumn;
        this.result = result;
    }

    public HeatHighlighter(HighlightPredicate predicate, int pColumn, EnrichedResult result) {
        super(predicate);
        this.pColumn = pColumn;
        this.result = result;
    }

    @Override
    protected Component doHighlight(Component renderer, ComponentAdapter adapter) {
        applyBackground(renderer, adapter);
        applyForeground(renderer, adapter);
        return renderer;
    }

    private float getHue(ComponentAdapter adapter) {
        Object value = adapter.getValue(EnrichedTreeTableViewModel.GOID_COLUMN);
        EnrichedGoId enrichedGoId = (EnrichedGoId) value;
        GoBranch branch = result.getGoBranch(enrichedGoId.getGoId());
        return branch.getHue();
    }

    private float getSaturation(ComponentAdapter adapter) {
        Object value = adapter.getValue(pColumn);
        Double p;
        if (null == value) {
            p = Double.NaN;
        } else {
            p = Double.valueOf((Double) value);
        }
        return UIUtilities.getSaturationFromP(p);
    }

    protected void applyBackground(Component renderer, ComponentAdapter adapter) {
        if (adapter.isSelected()) {
            renderer.setForeground(Color.BLACK);
        } else {
            renderer.setBackground(blend(renderer.getBackground(), getColor(adapter)));
        }
    }

    protected void applyForeground(Component renderer, ComponentAdapter adapter) {
        if (adapter.isSelected()) {
            renderer.setForeground(blend(renderer.getForeground(), getColor(adapter)));
        } else {
            renderer.setForeground(Color.BLACK);
        }
    }

    private Color getColor(ComponentAdapter adapter) {
        float hue = getHue(adapter);
        float saturation = getSaturation(adapter);
        float brightness = 1.f;
        return new Color(Color.HSBtoRGB(hue, saturation, brightness));
    }
}
