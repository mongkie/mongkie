package org.mongkie.layout.plugins.circle;

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
public class Circle extends LayoutBuilder<CircleILayout> {

    private final LayoutUI ui = new LayoutUI();

    @Override
    public String getName() {
        return NbBundle.getMessage(CircleILayout.class, "name");
    }

    @Override
    public UI<CircleILayout> getUI() {
        return ui;
    }

    @Override
    protected CircleILayout buildLayout() {
        return new CircleILayout(this);
    }

    private static class LayoutUI implements UI<CircleILayout> {

        @Override
        public String getDescription() {
            return NbBundle.getMessage(CircleILayout.class, "description");
        }

        @Override
        public Icon getIcon() {
            return null;
        }

        @Override
        public JPanel getSettingPanel(CircleILayout layout) {
            return null;
        }

        @Override
        public int getQualityRank() {
            return 1;
        }

        @Override
        public int getSpeedRank() {
            return 5;
        }
    }
}
