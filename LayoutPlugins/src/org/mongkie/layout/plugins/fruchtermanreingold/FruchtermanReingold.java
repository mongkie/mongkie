package org.mongkie.layout.plugins.fruchtermanreingold;

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
public class FruchtermanReingold extends LayoutBuilder<FruchtermanReingoldILayout> {

    private final LayoutUI ui = new LayoutUI();

    @Override
    public String getName() {
        return NbBundle.getMessage(FruchtermanReingoldILayout.class, "name");
    }

    @Override
    public UI<FruchtermanReingoldILayout> getUI() {
        return ui;
    }

    @Override
    protected FruchtermanReingoldILayout buildLayout() {
        return new FruchtermanReingoldILayout(this);
    }

    private static class LayoutUI implements UI<FruchtermanReingoldILayout> {

        @Override
        public String getDescription() {
            return NbBundle.getMessage(FruchtermanReingoldILayout.class, "description");
        }

        @Override
        public Icon getIcon() {
            return null;
        }

        @Override
        public JPanel getSettingPanel(FruchtermanReingoldILayout layout) {
            return null;
        }

        @Override
        public int getQualityRank() {
            return 3;
        }

        @Override
        public int getSpeedRank() {
            return 2;
        }
    }
}
