package simpledb;

import java.io.IOException;

/**
 * The delete operator. Delete reads tuples from its child operator and removes
 * them from the table they belong to.
 */
public class Delete extends Operator {

    private static final long serialVersionUID = 1L;
    
    // ME
    private TransactionId tid;
    private DbIterator child;
    private boolean called = false;

    /**
     * Constructor specifying the transaction that this delete belongs to as
     * well as the child to read from.
     * 
     * @param t
     *            The transaction this delete runs in
     * @param child
     *            The child operator from which to read tuples for deletion
     */
    public Delete(TransactionId t, DbIterator child) {
        // DONE
    	this.tid = t;
    	this.child = child;
    }

    public TupleDesc getTupleDesc() {
        // DONE
    	return new TupleDesc(new Type[] {Type.INT_TYPE});
    }

    public void open() throws DbException, TransactionAbortedException {
       // DONE
    	child.open();
    	super.open();
    }

    public void close() {
        // DONE
    	super.close();
    	child.close();
    }

    public void rewind() throws DbException, TransactionAbortedException {
        // DONE
    	child.rewind();
    }

    /**
     * Deletes tuples as they are read from the child operator. Deletes are
     * processed via the buffer pool (which can be accessed via the
     * Database.getBufferPool() method.
     * 
     * @return A 1-field tuple containing the number of deleted records.
     * @see Database#getBufferPool
     * @see BufferPool#deleteTuple
     */
    protected Tuple fetchNext() throws TransactionAbortedException, DbException {
        // DONE
    	if (called)
    		return null;
    	
    	called = true;
    	
    	int numDeletes = 0;
    	BufferPool bp = Database.getBufferPool();
    	
    	while (child.hasNext()) {
    		bp.deleteTuple(tid, child.next());
    		numDeletes++;
    	}
    	
    	Tuple retTuple = new Tuple(getTupleDesc());
    	retTuple.setField(0, new IntField(numDeletes));
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
