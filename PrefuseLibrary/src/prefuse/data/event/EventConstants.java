package prefuse.data.event;

import javax.swing.event.TableModelEvent;

/**
 * Constants used within prefuse data structure modification notifications.
 * 
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public interface EventConstants {

    /** Indicates a data insert operation. */
    public static final int INSERT = TableModelEvent.INSERT;
    public static final int INSERTED = TableModelEvent.INSERT + 1000;
    /** Indicates a data update operation. */
    public static final int UPDATE = TableModelEvent.UPDATE;
    /** Indicates a data delete operation. */
    public static final int DELETE = TableModelEvent.DELETE;
    /** Indicates an operation that affects all columns of a table. */
    public static final int ALL_COLUMNS = TableModelEvent.ALL_COLUMNS;
    public static final int NONE = Integer.MIN_VALUE;
} // end of interface EventConstants

