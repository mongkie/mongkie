package prefuse.controls;

import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.data.expression.NotPredicate;
import prefuse.data.tuple.DefaultTupleSet;
import prefuse.data.tuple.TupleSet;
import prefuse.util.ColorLib;
import prefuse.util.DataLib;
import prefuse.util.display.PaintListener;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.InGroupPredicate;

/**
 *
 * @author lunardo
 */
public class DragSelectionControl extends ControlAdapter implements PaintListener {
    
    private Point startPoint = new Point();
    private Point endPoint = new Point();
    private Point startAbsPoint = new Point();
    private Point endAbsPoint = new Point();
    private Rectangle selectionRect = new Rectangle();
    private Rectangle selectionAbsRect = new Rectangle();
    private boolean isDragging = false;
    private final Visualization v;
    private final TupleSet focusedTupleSet;
    private final String targetGroup;
    private boolean wasHighquality;
    private final DefaultTupleSet prevSelected = new DefaultTupleSet();
    private String activity;
    private int modifierMask = KeyEvent.CTRL_DOWN_MASK;
    
    public DragSelectionControl(Display d, String targetGroup, String activity) {
        this.v = d.getVisualization();
        this.targetGroup = targetGroup;
        this.focusedTupleSet = v.getFocusGroup(Visualization.FOCUS_ITEMS);
        d.addPaintListener(DragSelectionControl.this);
        this.activity = activity;
    }
    
    public DragSelectionControl(Display d, String targetGroup) {
        this(d, targetGroup, null);
    }
    
    public void setModifierMask(int modifierMask) {
        this.modifierMask = modifierMask;
    }
    
    @Override
    public void mousePressed(MouseEvent e) {
        
        Display d = (Display) e.getComponent();
        
        if (!isValid(d, e)) {
            prevSelected.clear();
            runActivity(v);
            return;
        }
        
        d.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        
        startPoint.setLocation(e.getPoint());
        endPoint.setLocation(e.getPoint());
        d.getAbsoluteCoordinate(startPoint, startAbsPoint);
        
        DataLib.removeTuples(focusedTupleSet, new NotPredicate(new InGroupPredicate(targetGroup)));
        prevSelected.set(focusedTupleSet);
        
        isDragging = true;
        wasHighquality = d.isHighQuality();
        d.setHighQuality(true);
        
        runActivity(v);
    }
    
    @Override
    public void mouseDragged(MouseEvent e) {
        
        Display d = (Display) e.getComponent();
        
        if (!isValid(d, e)) {
            return;
        }
        
        endPoint.setLocation(e.getPoint());
        d.getAbsoluteCoordinate(endPoint, endAbsPoint);
        selectionAbsRect.setRect(Math.min(startAbsPoint.x, endAbsPoint.x),
                Math.min(startAbsPoint.y, endAbsPoint.y),
                Math.abs(endAbsPoint.x - startAbsPoint.x),
                Math.abs(endAbsPoint.y - startAbsPoint.y));
        
        Iterator<VisualItem> targetItems =
                (targetGroup == null) ? v.visibleItems() : v.visibleItems(targetGroup);
        while (targetItems.hasNext()) {
            VisualItem item = targetItems.next();
            if (prevSelected.containsTuple(item)) {
                continue;
            }
            if (item.getBounds().intersects(selectionAbsRect)) {
                focusedTupleSet.addTuple(item);
            } else {
                focusedTupleSet.removeTuple(item);
            }
        }
        
        runActivity(v);
    }
    
    @Override
    public void mouseReleased(MouseEvent e) {
        
        Display d = (Display) e.getComponent();
        
        if (!isDragging) {
            return;
        }
        
        d.setCursor(Cursor.getDefaultCursor());
        
        isDragging = false;
        d.setHighQuality(wasHighquality);
        
//        runActivity(v);
        v.repaint();
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        Display d = (Display) e.getComponent();
        
        if (!isDragging
                || !KeyEvent.getKeyText(e.getKeyCode()).equals(KeyEvent.getKeyModifiersText(modifierMask >> 6))) {
            return;
        }
        
        isDragging = false;
        d.setHighQuality(wasHighquality);
        
//        runActivity(v);
        v.repaint();
    }
    
    protected boolean isValid(Display d, InputEvent e) {
        int onMask = MouseEvent.BUTTON1_DOWN_MASK | modifierMask;
        return (e.getModifiersEx() & onMask) == onMask && v == d.getVisualization();
    }
    
    protected void runActivity(Visualization v) {
        if (activity != null) {
            v.rerun(activity);
        }
    }
    
    @Override
    public void prePaint(Display d, Graphics2D g) {
    }
    
    @Override
    public void postPaint(Display d, Graphics2D g) {
        
        if (!isDragging) {
            return;
        }
        
        selectionRect.setRect(Math.min(startPoint.x, endPoint.x),
                Math.min(startPoint.y, endPoint.y),
                Math.abs(endPoint.x - startPoint.x),
                Math.abs(endPoint.y - startPoint.y));
        g.setColor(ColorLib.getColor(ColorLib.rgba(255, 0, 0, 100)));
        g.draw(selectionRect);
        g.setColor(ColorLib.getColor(ColorLib.rgba(255, 0, 0, 10)));
        g.fill(selectionRect);
    }
}
