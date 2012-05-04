package org.mongkie.layout.plugins.radialtree;

import javax.swing.Icon;
import javax.swing.JPanel;
import org.mongkie.layout.spi.LayoutBuilder;
import org.mongkie.layout.spi.LayoutBuilder.UI;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author yjjang
 */
@ServiceProvider(service = LayoutBuilder.class)
public class RadialTree extends LayoutBuilder<RadialTreeILayout> {

    private final LayoutUI ui = new LayoutUI();

    @Override
    public String getName() {
        return NbBundle.getMessage(RadialTreeILayout.class, "name");
    }

    @Override
    public UI<RadialTreeILayout> getUI() {
        return ui;
    }

    @Override
    protected RadialTreeILayout buildLayout() {
        return new RadialTreeILayout(this);
    }

    private static class LayoutUI implements UI<RadialTreeILayout> {

        @Override
        public String getDescription() {
            return NbBundle.getMessage(RadialTreeILayout.class, "description");
        }

        @Override
        public Icon getIcon() {
            return null;
        }

        @Override
        public JPanel getSettingPanel(RadialTreeILayout layout) {
            return null;
        }

        @Override
        public int getQualityRank() {
            return 4;
        }

        @Override
        public int getSpeedRank() {
            return 3;
        }
    }
}
