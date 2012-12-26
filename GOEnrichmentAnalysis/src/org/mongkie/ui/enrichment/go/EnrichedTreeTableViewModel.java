package org.mongkie.ui.enrichment.go;

import org.mongkie.enrichment.go.EnrichedGoId;
import gobean.GoBranch;
import gobean.GoId;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import org.jdesktop.swingx.treetable.AbstractTreeTableModel;
import org.mongkie.enrichment.EnrichmentController;
import org.mongkie.enrichment.EnrichmentModel;
import org.mongkie.gobean.EnrichedResult;
import org.mongkie.ui.enrichment.go.util.UIUtilities;
import org.openide.util.Lookup;

public class EnrichedTreeTableViewModel extends AbstractTreeTableModel implements TableModel {
    
    public static final int GOID_COLUMN = 0;
    public static final int ADJUSTED_P_COLUMN = 5;
    private EnrichedResult result;
    private Map<GoId, EnrichedGoId> goId2EnrichedGoId;
    private List<EnrichedGoId> enrichedGoIds;
    private final Set<TableModelListener> listeners = Collections.synchronizedSet(new HashSet<TableModelListener>());
    private final String geneIdCol;
    
    public EnrichedTreeTableViewModel(EnrichedResult result, List<GoId> selectedGoIds) {
        super(new EnrichedGoId(GoBranch.GENE_ONTOLOGY.getGoId(), result,
                ((EnrichmentModel) Lookup.getDefault().lookup(EnrichmentController.class).getModel()).getGeneIDColumn()));
        this.geneIdCol = ((EnrichedGoId) getRoot()).getGeneIDColumn();
        this.result = result;
        this.goId2EnrichedGoId = makeGoId2EnrichedGoId(result.getChildCountMap().keySet());
        this.enrichedGoIds = selectEnrichedGoIds(selectedGoIds);
    }
    
    private Map<GoId, EnrichedGoId> makeGoId2EnrichedGoId(Set<GoId> allGoIds) {
        Map<GoId, EnrichedGoId> terms = new HashMap<GoId, EnrichedGoId>();
        for (GoId goId : allGoIds) {
            terms.put(goId, new EnrichedGoId(goId, result, geneIdCol));
        }
        return terms;
    }
    
    private List<EnrichedGoId> selectEnrichedGoIds(List<GoId> selectedGoIds) {
        List<EnrichedGoId> _enrichedGoIds = new ArrayList<EnrichedGoId>();
        for (GoId goId : selectedGoIds) {
            _enrichedGoIds.add(setColor(goId2EnrichedGoId.get(goId)));
        }
        return _enrichedGoIds;
    }
    
    private EnrichedGoId setColor(EnrichedGoId enrichedGoId) {
        float hue = result.getGoBranch(enrichedGoId.getGoId()).getHue();
        Double p = Double.valueOf(enrichedGoId.getP());
        float saturation = UIUtilities.getSaturationFromP(p);
        enrichedGoId.setColor(new Color(Color.HSBtoRGB(hue, saturation, 1.f)));
        return enrichedGoId;
    }
    
    @Override
    public int getChildCount(Object parent) {
        GoId parentGoId = ((EnrichedGoId) parent).getGoId();
        return result.childCount(parentGoId);
    }
    
    @Override
    public int getIndexOfChild(Object parent, Object child) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public EnrichedGoId getChild(Object parent, int index) {
        GoId parentGoId = ((EnrichedGoId) parent).getGoId();
        return goId2EnrichedGoId.get(result.getDownId(parentGoId, index));
    }
    
    @Override
    public int getRowCount() {
        return enrichedGoIds.size();
    }
    
    @Override
    public int getColumnCount() {
        return 7;
    }
    
    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return "GO id";
            case 1:
                return "branch";
            case 2:
                return "GO name";
            case 3:
                return "coverage (" + result.getTotalStudyCount() + " / " + result.getTotalPopCount() + " )";
            case 4:
                return "ratio";
            case 5:
                return "raw-P";
            case 6:
                return "adjusted-P";
            default:
                throw new RuntimeException("EnrichedTreeTableViewModel ::: Invalid column: " + column);
        }
    }
    
    @Override
    public Class<?> getColumnClass(int column) {
        switch (column) {
            case 0:
                return EnrichedGoId.class;
            case 1:
                return GoBranch.class;
            case 2:
                return String.class;
            case 3:
                return String.class;
            case 4:
                return Double.class;
            case 5:
                return Double.class;
            case 6:
                return Double.class;
            default:
                throw new RuntimeException("EnrichedTreeTableViewModel ::: Invalid column: " + column);
        }
    }
    
    @Override
    public Object getValueAt(Object node, int column) {
        EnrichedGoId enrichedGoId = (EnrichedGoId) node;
        return getValue(enrichedGoId, column);
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        EnrichedGoId enrichedGoId = enrichedGoIds.get(rowIndex);
        return getValue(enrichedGoId, columnIndex);
    }
    
    private Object getValue(EnrichedGoId enrichedGoId, int columnIndex) {
        GoId goId = enrichedGoId.getGoId();
        switch (columnIndex) {
            case 0:
                return enrichedGoId;
            case 1:
                return result.getGoBranch(goId);
            case 2:
                return result.getName(goId);
            case 3:
                int[] c1 = result.getCoverage(goId);
                return String.format("[ %d(%d) / %d(%d) ]", c1[0], c1[1], c1[2], c1[3]);
            case 4:
                int[] c2 = result.getCoverage(goId);
                return (double) c2[0] / (double) c2[2];
            case 5:
                return result.getRawP(goId);
            case 6:
                return result.getAdjustedP(goId);
            default:
                throw new RuntimeException("Invalid column: " + columnIndex);
        }
    }
    
    @Override
    public int getHierarchicalColumn() {
        return 0;
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
    
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public void addTableModelListener(TableModelListener l) {
        listeners.add(l);
    }
    
    @Override
    public void removeTableModelListener(TableModelListener l) {
        listeners.remove(l);
    }
}
