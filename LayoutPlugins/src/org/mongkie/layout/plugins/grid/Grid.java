package org.mongkie.layout.plugins.grid;

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
public class Grid extends LayoutBuilder<GridILayout> {

    private final LayoutUI ui = new LayoutUI();

    @Override
    public String getName() {
        return NbBundle.getMessage(GridILayout.class, "name");
    }

    @Override
    public UI<GridILayout> getUI() {
        return ui;
    }

    @Override
    protected GridILayout buildLayout() {
        return new GridILayout(this);
    }

    private static class LayoutUI implements UI<GridILayout> {

        @Override
        public String getDescription() {
            return NbBundle.getMessage(GridILayout.class, "description");
        }

        @Override
        public Icon getIcon() {
            return null;
        }

        @Override
        public JPanel getSettingPanel(GridILayout layout) {
            return null;
        }

        @Override
        public int getQualityRank() {
            return 2;
        }

        @Override
        public int getSpeedRank() {
            return 4;
        }
    }
}
