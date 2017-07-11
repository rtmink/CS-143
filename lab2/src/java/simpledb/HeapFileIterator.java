package simpledb;
import java.util.*;

/**
 * HeapFileIterator iterates through the tuples of each page in HeapFile
 */
public class HeapFileIterator implements DbFileIterator {
	
	private TransactionId tid;
	private int numPages;
	private int tableId;
	private int pgNo;					// 0 by default
	private Iterator<Tuple>tupleItr;	// null by default
	private boolean open;				// false by default
	
	// Find the first page with tuples starting with particular page number
    private boolean loadPage(int startPgNo) 
    		throws DbException, TransactionAbortedException {
    	
    	while (startPgNo < numPages) {
    		HeapPage hp = (HeapPage)Database.getBufferPool().getPage(tid, new HeapPageId(tableId, startPgNo), Permissions.READ_WRITE);
        	tupleItr = hp.iterator();
        	
        	if (tupleItr.hasNext()) {
        		// update current page number with the page number of the new page
        		pgNo = startPgNo;
        		return true;
        	}
    		
        	// current page does not have any tuples, go to the next page
    		startPgNo++;
    	}
    	
    	// there are no pages with any tuples
    	tupleItr = null;
    	return false;
    }
    
	public HeapFileIterator(TransactionId tid, int tableId, int numPages) {
		this.tid = tid;
		this.tableId = tableId;
		this.numPages = numPages;
	}
	
    /**
     * Opens the iterator
     * @throws DbException when there are problems opening/accessing the database.
     */
    public void open()
        throws DbException, TransactionAbortedException {
    	
    	open = true;
    	
    	// find the first page with tuples starting at page 0
    	this.loadPage(0);
    }

    /** @return true if there are more tuples available. */
    public boolean hasNext()
        throws DbException, TransactionAbortedException {
    	
    	// here tupleItr should have already been initialized in the "open" method
    	// when tupleItr is null, there are no more pages with any tuples
    	if (!open || tupleItr == null)
    		return false;
    	
    	// check if current page has tuples
    	if (tupleItr.hasNext())
    		return true;
    	
    	// no more tuples in the current page
    	// check if the are more pages with tuples
    	return this.loadPage(++pgNo);
    }

    /**
     * Gets the next tuple from the operator (typically implementing by reading
     * from a child operator or an access method).
     *
     * @return The next tuple in the iterator.
     * @throws NoSuchElementException if there are no more tuples
     */
    public Tuple next()
        throws DbException, TransactionAbortedException, NoSuchElementException {
    	
    	if (!open || !this.hasNext())
    		throw new NoSuchElementException("Iterator not opened or there are no more tuples.");
    	
    	return tupleItr.next();
    }

    /**
     * Resets the iterator to the start.
     * @throws DbException When rewind is unsupported.
     */
    public void rewind() throws DbException, TransactionAbortedException {
    	this.close();
    	this.open();
    }

    /**
     * Closes the iterator.
     */
    public void close() {
    	open = false;
    	tupleItr = null;
    }
}
