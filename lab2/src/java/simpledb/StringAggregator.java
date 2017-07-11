package simpledb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Knows how to compute some aggregate over a set of StringFields.
 */
public class StringAggregator implements Aggregator {

    private static final long serialVersionUID = 1L;
    
    // ME
    int gbfield;
    Type gbfieldtype;
    int afield;
    Op what;

    private ArrayList<Field> noGroup;
    private HashMap<Field, ArrayList<Field>> group;
    
    /**
     * Aggregate constructor
     * @param gbfield the 0-based index of the group-by field in the tuple, or NO_GROUPING if there is no grouping
     * @param gbfieldtype the type of the group by field (e.g., Type.INT_TYPE), or null if there is no grouping
     * @param afield the 0-based index of the aggregate field in the tuple
     * @param what aggregation operator to use -- only supports COUNT
     * @throws IllegalArgumentException if what != COUNT
     */

    public StringAggregator(int gbfield, Type gbfieldtype, int afield, Op what) {
        // DONE
    	this.gbfield = gbfield;
    	this.gbfieldtype = gbfieldtype;
    	this.afield = afield;
    	this.what = what;
    	
    	if (noGrouping()) {
    		// No grouping
    		noGroup = new ArrayList<Field>();
    	} else {
    		// Grouping
    		group = new HashMap<Field, ArrayList<Field>>();
    	}
    }

    /**
     * Merge a new tuple into the aggregate, grouping as indicated in the constructor
     * @param tup the Tuple containing an aggregate field and a group-by field
     */
    public void mergeTupleIntoGroup(Tuple tup) {
    	// DONE
    	if (noGrouping()) {
    		// No grouping
    		noGroup.add(tup.getField(afield));
    	} else {
    		// Grouping
    		Field groupField = tup.getField(gbfield);
    		
    		ArrayList<Field> aggs = group.get(groupField);
    		
    		if (aggs == null) {
    			aggs = new ArrayList<Field>();
    		}
    		
    		aggs.add(tup.getField(afield));
    		group.put(tup.getField(gbfield), aggs);
    	}
    }

    /**
     * Create a DbIterator over group aggregate results.
     *
     * @return a DbIterator whose tuples are the pair (groupVal,
     *   aggregateVal) if using group, or a single (aggregateVal) if no
     *   grouping. The aggregateVal is determined by the type of
     *   aggregate specified in the constructor.
     */
    public DbIterator iterator() {
    	// DONE
    	ArrayList<Tuple> tuples = new ArrayList<Tuple>();
    	TupleDesc td;
    	Tuple tuple;
    	int aggVal = 0;
    	
        if (noGrouping()) {
        	// No grouping
        	td = new TupleDesc(new Type[]{Type.INT_TYPE});
        	
        	aggVal = noGroup.size();
        	
        	tuple = new Tuple(td);
        	tuple.setField(0, new IntField(aggVal));
        	
        	tuples.add(tuple);
        } else {
        	// Grouping
        	td = new TupleDesc(new Type[]{gbfieldtype, Type.INT_TYPE});
        	
        	Iterator<Field> ki = group.keySet().iterator();
        	
        	while(ki.hasNext()) {
        		Field newField = ki.next();
        		
        		aggVal = group.get(newField).size();
        		
        		tuple = new Tuple(td);
            	tuple.setField(0, newField);
            	tuple.setField(1, new IntField(aggVal));
            	
            	tuples.add(tuple);
        	}
        	
        }
        
        return new TupleIterator(td, tuples);
    }
    
 // ME: convenient helper function
    private boolean noGrouping() {
    	return gbfield == Aggregator.NO_GROUPING;
    }

}
