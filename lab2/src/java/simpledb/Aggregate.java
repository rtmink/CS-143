package simpledb;

import java.util.*;

/**
 * The Aggregation operator that computes an aggregate (e.g., sum, avg, max,
 * min). Note that we only support aggregates over a single column, grouped by a
 * single column.
 */
public class Aggregate extends Operator {

    private static final long serialVersionUID = 1L;
    
    // ME
    private DbIterator child;
    private DbIterator newChild;
    private int afield;
    private int gfield;
    private Aggregator.Op aop;
    
    // ME: Helper function
    private boolean noGrouping() {
    	return gfield == -1;
    }
    
    private Type getGroupFieldType()
    {
    	return noGrouping() ? null : child.getTupleDesc().getFieldType(gfield);
    }
    
    private Type getAggFieldType()
    {
    	return child.getTupleDesc().getFieldType(afield);
    }

    /**
     * Constructor.
     * 
     * Implementation hint: depending on the type of afield, you will want to
     * construct an {@link IntAggregator} or {@link StringAggregator} to help
     * you with your implementation of readNext().
     * 
     * 
     * @param child
     *            The DbIterator that is feeding us tuples.
     * @param afield
     *            The column over which we are computing an aggregate.
     * @param gfield
     *            The column over which we are grouping the result, or -1 if
     *            there is no grouping
     * @param aop
     *            The aggregation operator to use
     */
    public Aggregate(DbIterator child, int afield, int gfield, Aggregator.Op aop) {
		// DONE
    	this.child = child;
    	this.afield = afield;
    	this.gfield = gfield;
    	this.aop = aop;
    }

    /**
     * @return If this aggregate is accompanied by a groupby, return the groupby
     *         field index in the <b>INPUT</b> tuples. If not, return
     *         {@link simpledb.Aggregator#NO_GROUPING}
     * */
    public int groupField() {
		// DONE
    	return noGrouping() ? Aggregator.NO_GROUPING : gfield; 
    }

    /**
     * @return If this aggregate is accompanied by a group by, return the name
     *         of the groupby field in the <b>OUTPUT</b> tuples If not, return
     *         null;
     * */
    public String groupFieldName() {
		// DONE?
    	return noGrouping() ? null : child.getTupleDesc().getFieldName(gfield);
    }

    /**
     * @return the aggregate field
     * */
    public int aggregateField() {
    	// DONE?
    	return afield;
    }

    /**
     * @return return the name of the aggregate field in the <b>OUTPUT</b>
     *         tuples
     * */
    public String aggregateFieldName() {
		// DONE
    	return aop.toString() + child.getTupleDesc().getFieldName(afield);
    }

    /**
     * @return return the aggregate operator
     * */
    public Aggregator.Op aggregateOp() {
		// DONE
    	return aop;
    }

    public static String nameOfAggregatorOp(Aggregator.Op aop) {
    	// DONE
    	return aop.toString();
    }

    public void open() throws NoSuchElementException, DbException,
	    TransactionAbortedException {
		// DONE?
    	
    	child.open();
    	super.open();
    	
    	// First merge all the tuples
    	Type aType = getAggFieldType();
		Type gType = getGroupFieldType();
		
		if (aType == Type.INT_TYPE) {
			
			IntegerAggregator ia = new IntegerAggregator(gfield, gType, afield, aop);
			
			while (child.hasNext()) {
	    		Tuple tuple = child.next();
	    		ia.mergeTupleIntoGroup(tuple);
	    	}
			
			newChild = ia.iterator();
			
		} else if (aType == Type.STRING_TYPE) {
			
			StringAggregator sa = new StringAggregator(gfield, gType, afield, aop);
			
			while (child.hasNext()) {
	    		Tuple tuple = child.next();
	    		sa.mergeTupleIntoGroup(tuple);
	    	}
			
			newChild = sa.iterator();
		}
		
		// Do we need this?
		newChild.open();
    }

    /**
     * Returns the next tuple. If there is a group by field, then the first
     * field is the field by which we are grouping, and the second field is the
     * result of computing the aggregate, If there is no group by field, then
     * the result tuple should contain one field representing the result of the
     * aggregate. Should return null if there are no more tuples.
     */
    protected Tuple fetchNext() throws TransactionAbortedException, DbException {
		// DONE
    	while(newChild.hasNext())
    		return newChild.next();
    	
    	return null;
    }

    public void rewind() throws DbException, TransactionAbortedException {
		// DONE?
    	child.rewind();
    	newChild.rewind();
    }

    /**
     * Returns the TupleDesc of this Aggregate. If there is no group by field,
     * this will have one field - the aggregate column. If there is a group by
     * field, the first field will be the group by field, and the second will be
     * the aggregate value column.
     * 
     * The name of an aggregate column should be informative. For example:
     * "aggName(aop) (child_td.getFieldName(afield))" where aop and afield are
     * given in the constructor, and child_td is the TupleDesc of the child
     * iterator.
     */
    public TupleDesc getTupleDesc() {
    	// DONE?
    	TupleDesc newTD;
    	Type gType = getGroupFieldType();
    	
    	if (noGrouping()) {
    		// No grouping
    		newTD = new TupleDesc(new Type[]{Type.INT_TYPE}, new String[]{aggregateFieldName()});
    	} else {
    		// Grouping
    		newTD = new TupleDesc(new Type[]{gType, Type.INT_TYPE}, new String[]{groupFieldName(), aggregateFieldName()});
    	}
    	
    	return newTD;
    }

    public void close() {
		// DONE
    	super.close();
    	child.close();
    	newChild.close();
    }

    @Override
    public DbIterator[] getChildren() {
		// DONE
    	return new DbIterator[] {newChild};
    }

    @Override
    public void setChildren(DbIterator[] children) {
		// DONE
    	if (child != children[0])
    		child = children[0];
    }
    
}
