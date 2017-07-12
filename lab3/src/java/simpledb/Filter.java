package simpledb;

import java.util.*;

/**
 * Filter is an operator that implements a relational select.
 */
public class Filter extends Operator {

    private static final long serialVersionUID = 1L;
    
    // ME
    private Predicate p;
    private DbIterator child;

    /**
     * Constructor accepts a predicate to apply and a child operator to read
     * tuples to filter from.
     * 
     * @param p
     *            The predicate to filter tuples with
     * @param child
     *            The child operator
     */
    public Filter(Predicate p, DbIterator child) {
        // DONE
    	this.p = p;
    	this.child = child;
    }

    public Predicate getPredicate() {
        // DONE
    	return  p;
    }

    public TupleDesc getTupleDesc() {
        // DONE
    	return child.getTupleDesc();
    }

    public void open() throws DbException, NoSuchElementException,
            TransactionAbortedException {
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
     * AbstractDbIterator.readNext implementation. Iterates over tuples from the
     * child operator, applying the predicate to them and returning those that
     * pass the predicate (i.e. for which the Predicate.filter() returns true.)
     * 
     * @return The next tuple that passes the filter, or null if there are no
     *         more tuples
     * @see Predicate#filter
     */
    protected Tuple fetchNext() throws NoSuchElementException,
            TransactionAbortedException, DbException {
        // DONE
    	Tuple tuple;
    	while (child.hasNext()) {
    		tuple = child.next();
    		if (p.filter(tuple))
    			return tuple;
    	}
    	
    	return null;
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
