package simpledb;

/** Unique identifier for HeapPage objects. */
public class HeapPageId implements PageId {

	// ME
	private int tableId;
	private int pgNo;
	
    /**
     * Constructor. Create a page id structure for a specific page of a
     * specific table.
     *
     * @param tableId The table that is being referenced
     * @param pgNo The page number in that table.
     */
    public HeapPageId(int tableId, int pgNo) {
        // DONE
    	this.tableId = tableId;
    	this.pgNo = pgNo;
    }

    /** @return the table associated with this PageId */
    public int getTableId() {
        // DONE
    	return tableId;
    }

    /**
     * @return the page number in the table getTableId() associated with
     *   this PageId
     */
    public int pageNumber() {
        // DONE
    	return pgNo;
    }

    /**
     * @return a hash code for this page, represented by the concatenation of
     *   the table number and the page number (needed if a PageId is used as a
     *   key in a hash table in the BufferPool, for example.)
     * @see BufferPool
     */
    public int hashCode() {
        // DONE
    	// apparently the concatenation of the tableId and pgNo is too long for an int
    	// so we use Long instead, and take its hash so we return an int
    	
    	//return Integer.parseInt(Integer.toString(tableId) + Integer.toString(pgNo));
    	return Long.valueOf(Integer.toString(tableId) + Integer.toString(pgNo)).hashCode();
    }

    /**
     * Compares one PageId to another.
     *
     * @param o The object to compare against (must be a PageId)
     * @return true if the objects are equal (e.g., page numbers and table
     *   ids are the same)
     */
    public boolean equals(Object o) {
        // DONE
    	if (!(o instanceof HeapPageId))
    		return false;
    	
    	HeapPageId hpid = (HeapPageId)o;
    	return tableId == hpid.tableId && pgNo == hpid.pgNo;
    }

    /**
     *  Return a representation of this object as an array of
     *  integers, for writing to disk.  Size of returned array must contain
     *  number of integers that corresponds to number of args to one of the
     *  constructors.
     */
    public int[] serialize() {
        int data[] = new int[2];

        data[0] = getTableId();
        data[1] = pageNumber();

        return data;
    }

}
