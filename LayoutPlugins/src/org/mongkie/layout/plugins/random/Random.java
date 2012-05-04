package org.mongkie.layout.plugins.random;

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
public class Random extends LayoutBuilder<RandomILayout> {

    private final LayoutUI ui = new LayoutUI();

    @Override
    public String getName() {
        return NbBundle.getMessage(RandomILayout.class, "name");
    }

    @Override
    public UI<RandomILayout> getUI() {
        return ui;
    }

    @Override
    protected RandomILayout buildLayout() {
        return new RandomILayout(this);
    }

    private static class LayoutUI implements UI<RandomILayout> {

        @Override
        public String getDescription() {
            return NbBundle.getMessage(RandomILayout.class, "description");
        }

        @Override
        public Icon getIcon() {
            return null;
        }

        @Override
        public JPanel getSettingPanel(RandomILayout layout) {
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
