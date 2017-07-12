package simpledb;

/**
 * Inserts tuples read from the child operator into the tableid specified in the
 * constructor
 */
public class Insert extends Operator {

    private static final long serialVersionUID = 1L;
    
    // ME
    private TransactionId tid;
    private DbIterator child;
    private int tableid;
    private boolean called = false;

    /**
     * Constructor.
     * 
     * @param t
     *            The transaction running the insert.
     * @param child
     *            The child operator from which to read tuples to be inserted.
     * @param tableid
     *            The table in which to insert tuples.
     * @throws DbException
     *             if TupleDesc of child differs from table into which we are to
     *             insert.
     */
    public Insert(TransactionId t,DbIterator child, int tableid)
            throws DbException {
        // DONE
    	if (!child.getTupleDesc().equals(Database.getCatalog().getTupleDesc(tableid)))
    		throw new DbException("TupleDesc of child differs from table to be inserted.");
    	
    	this.tid = t;
    	this.child = child;
    	this.tableid = tableid;
    }

    public TupleDesc getTupleDesc() {
    	// DONE
        return new TupleDesc(new Type[] {Type.INT_TYPE});
    }

    public void open() throws DbException, TransactionAbortedException {
        child.open();
        super.open();
    }

    public void close() {
    	super.close();
        child.close();
    }

    public void rewind() throws DbException, TransactionAbortedException {
        child.rewind();
    }

    /**
     * Inserts tuples read from child into the tableid specified by the
     * constructor. It returns a one field tuple containing the number of
     * inserted records. Inserts should be passed through BufferPool. An
     * instances of BufferPool is available via Database.getBufferPool(). Note
     * that insert DOES NOT need check to see if a particular tuple is a
     * duplicate before inserting it.
     * 
     * @return A 1-field tuple containing the number of inserted records, or
     *         null if called more than once.
     * @see Database#getBufferPool
     * @see BufferPool#insertTuple
     */
    protected Tuple fetchNext() throws TransactionAbortedException, DbException {
        
    	if (called)
    		return null;
    	
    	called = true;
    	
    	int numWrites = 0;
    	BufferPool bp = Database.getBufferPool();
    	
    	while (child.hasNext()) {
    		bp.insertTuple(tid, tableid, child.next());
    		numWrites++;
    	}
    	
    	Tuple retTuple = new Tuple(getTupleDesc());
    	retTuple.setField(0, new IntField(numWrites));
    	return retTuple;
    }

    @Override
    public DbIterator[] getChildren() {
        // DONE
    	return new DbIterator[] {child};
    }

    @Override
    public void setChildren(DbIterator[] children) {
        // DONE
    	if (child != children[0])
    		child = children[0];
    }
}
