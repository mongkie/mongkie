package org.mongkie.layout.plugins.multipartite;

import java.util.ArrayList;
import java.util.List;
import org.mongkie.layout.spi.LayoutBuilder;
import org.mongkie.layout.spi.LayoutProperty;
import org.mongkie.layout.spi.PrefuseLayout;
import prefuse.action.layout.VerticalLinesLayout;
import static kobic.prefuse.Constants.*;

/**
 *
 * @author yjjang
 */
public class MultipartiteILayout extends PrefuseLayout<MultipartiteILayout, VerticalLinesLayout> {

    MultipartiteILayout(LayoutBuilder<MultipartiteILayout> builder) {
        super(builder);
    }

    @Override
    protected VerticalLinesLayout createPrefuseLayout() {
        return new VerticalLinesLayout(display, NODES, null, new String[0]);
    }

    @Override
    public LayoutProperty[] createProperties() {
        List<LayoutProperty> properties = new ArrayList<LayoutProperty>();
        return properties.toArray(new LayoutProperty[0]);
    }

    @Override
    public void resetPropertyValues() {
    }

    @Override
    protected void setLayoutParameters(VerticalLinesLayout layout) {
    }

    @Override
    protected boolean isRunOnce() {
        return true;
    }
}
