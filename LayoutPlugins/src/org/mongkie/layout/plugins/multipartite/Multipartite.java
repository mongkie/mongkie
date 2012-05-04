package org.mongkie.layout.plugins.multipartite;

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
public class Multipartite extends LayoutBuilder<MultipartiteILayout> {

    private final LayoutUI ui = new LayoutUI();

    @Override
    public String getName() {
        return NbBundle.getMessage(MultipartiteILayout.class, "name");
    }

    @Override
    public UI<MultipartiteILayout> getUI() {
        return ui;
    }

    @Override
    protected MultipartiteILayout buildLayout() {
        return new MultipartiteILayout(this);
    }

    private static class LayoutUI implements UI<MultipartiteILayout> {

        @Override
        public String getDescription() {
            return NbBundle.getMessage(MultipartiteILayout.class, "description");
        }

        @Override
        public Icon getIcon() {
            return null;
        }

        @Override
        public JPanel getSettingPanel(MultipartiteILayout layout) {
            return null;
        }

        @Override
        public int getQualityRank() {
            return 3;
        }

        @Override
        public int getSpeedRank() {
            return 4;
        }
    }
}
