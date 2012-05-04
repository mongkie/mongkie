package org.mongkie.layout.plugins.forcedirected;

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
public class ForceDirected extends LayoutBuilder<ForceDirectedILayout> {

    private final LayoutUI ui = new LayoutUI();

    @Override
    public String getName() {
        return NbBundle.getMessage(ForceDirectedILayout.class, "name");
    }

    @Override
    public UI<ForceDirectedILayout> getUI() {
        return ui;
    }

    @Override
    protected ForceDirectedILayout buildLayout() {
        return new ForceDirectedILayout(this);
    }

    private static class LayoutUI implements UI<ForceDirectedILayout> {

        @Override
        public String getDescription() {
            return NbBundle.getMessage(ForceDirectedILayout.class, "description");
        }

        @Override
        public Icon getIcon() {
            return null;
        }

        @Override
        public JPanel getSettingPanel(ForceDirectedILayout layout) {
            return null;
        }

        @Override
        public int getQualityRank() {
            return 4;
        }

        @Override
        public int getSpeedRank() {
            return 2;
        }
    }
}
