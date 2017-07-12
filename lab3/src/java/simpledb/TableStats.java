package simpledb;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TableStats represents statistics (e.g., histograms) about base tables in a
 * query. 
 * 
 * This class is not needed in implementing lab1 and lab2.
 */
public class TableStats {

    private static final ConcurrentHashMap<String, TableStats> statsMap = new ConcurrentHashMap<String, TableStats>();

    static final int IOCOSTPERPAGE = 1000;

    public static TableStats getTableStats(String tablename) {
        return statsMap.get(tablename);
    }

    public static void setTableStats(String tablename, TableStats stats) {
        statsMap.put(tablename, stats);
    }
    
    public static void setStatsMap(HashMap<String,TableStats> s)
    {
        try {
            java.lang.reflect.Field statsMapF = TableStats.class.getDeclaredField("statsMap");
            statsMapF.setAccessible(true);
            statsMapF.set(null, s);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    public static Map<String, TableStats> getStatsMap() {
        return statsMap;
    }

    public static void computeStatistics() {
        Iterator<Integer> tableIt = Database.getCatalog().tableIdIterator();

        System.out.println("Computing table stats.");
        while (tableIt.hasNext()) {
            int tableid = tableIt.next();
            TableStats s = new TableStats(tableid, IOCOSTPERPAGE);
            setTableStats(Database.getCatalog().getTableName(tableid), s);
        }
        System.out.println("Done.");
    }

    /**
     * Number of bins for the histogram. Feel free to increase this value over
     * 100, though our tests assume that you have at least 100 bins in your
     * histograms.
     */
    static final int NUM_HIST_BINS = 100; 
    
    // ME
    private int ioCostPerPage;
    private DbFile file;
    private int numTuples;
    private IntHistogram[] intHistograms;
    private StringHistogram[] stringHistograms;
    private TupleDesc td;
    //private DbFileIterator itr;
    
    /**
     * Create a new TableStats object, that keeps track of statistics on each
     * column of a table
     * 
     * @param tableid
     *            The table over which to compute statistics
     * @param ioCostPerPage
     *            The cost per page of IO. This doesn't differentiate between
     *            sequential-scan IO and disk seeks.
     */
    public TableStats(int tableid, int ioCostPerPage) {
        // For this function, you'll have to get the
        // DbFile for the table in question,
        // then scan through its tuples and calculate
        // the values that you need.
        // You should try to do this reasonably efficiently, but you don't
        // necessarily have to (for example) do everything
        // in a single scan of the table.
        // DONE
    	
    	this.ioCostPerPage = ioCostPerPage;
    	this.numTuples = 0;
    	
    	try {
    		// Get the file
    		file = Database.getCatalog().getDatabaseFile(tableid);
        	
        	// Scan it and check each tuple
        	DbFileIterator itr = file.iterator(new TransactionId());
        	
        	//Tuple tuple;
        	td = file.getTupleDesc();
        	int numFields = td.numFields();
        	int[] mins = new int[numFields];
        	int[] maxes = new int[numFields];
        	boolean emptyMaxMin = true;
        	intHistograms = new IntHistogram[numFields];
        	stringHistograms = new StringHistogram[numFields];
        	Tuple tuple;
        	
        	itr.open();
        	
        	// Scan through the tuples to find min and max
        	while (itr.hasNext()) {
        		tuple = itr.next();
        		numTuples++; // Count tuples
        		
        		for (int i = 0; i < numFields; i++) {
        			
        			if (td.getFieldType(i) == Type.INT_TYPE) {
        				
        				int value = ((IntField) tuple.getField(i)).getValue();
        				
        				if (emptyMaxMin) {
        					// Initialize both max and min to the value 
        					// because there is nothing to compare it to
        					mins[i] = value;
                			maxes[i] = value;
        				}
        				else {
        					if (value < mins[i])
        						mins[i] = value;
        					
        					if (value > maxes[i])
        						maxes[i] = value;
        				}
        			}
        		}
        		
        		if (emptyMaxMin && (numFields != 0)) {
        			emptyMaxMin = false;
        		}
        	}
        	
        	//System.out.println("Number of tuples: " + numTuples);
        	//System.out.println("Number of fields: " + numFields);
        	
        	// Create the histograms
        	for (int i = 0; i < numFields; i++) {
        		if (td.getFieldType(i) == Type.INT_TYPE) {
        			intHistograms[i] = new IntHistogram(NUM_HIST_BINS, mins[i], maxes[i]);
        		}
        		else if (td.getFieldType(i) == Type.STRING_TYPE) {
        			stringHistograms[i] = new StringHistogram(NUM_HIST_BINS);
        		}
        	}
    		
        	// Fill the histograms with values of the tuples
        	itr.rewind();
   
        	while (itr.hasNext()) {
        		tuple = itr.next();
        		
        		for (int i = 0; i < numFields; i++) {
        			
        			if (td.getFieldType(i) == Type.INT_TYPE) {
        				intHistograms[i].addValue(((IntField) tuple.getField(i)).getValue());
        				
        			}
        			else if (td.getFieldType(i) == Type.STRING_TYPE) {
        				stringHistograms[i].addValue(((StringField) tuple.getField(i)).getValue());
        			}
        		}
        	}
        	
        	itr.close();
        	
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    }

    /**
     * Estimates the cost of sequentially scanning the file, given that the cost
     * to read a page is costPerPageIO. You can assume that there are no seeks
     * and that no pages are in the buffer pool.
     * 
     * Also, assume that your hard drive can only read entire pages at once, so
     * if the last page of the table only has one tuple on it, it's just as
     * expensive to read as a full page. (Most real hard drives can't
     * efficiently address regions smaller than a page at a time.)
     * 
     * @return The estimated cost of scanning the table.
     */
    public double estimateScanCost() {
        // DONE 
    	int pageSize = BufferPool.PAGE_SIZE;
    	int numTuplesPerPage = pageSize / td.getSize(); // pageSize / tupleSize
    	int pagesRead = ((numTuples - 1) / numTuplesPerPage) + 1; // At least one page is read
    	
        return pagesRead * ioCostPerPage;
    }

    /**
     * This method returns the number of tuples in the relation, given that a
     * predicate with selectivity selectivityFactor is applied.
     * 
     * @param selectivityFactor
     *            The selectivity of any predicates over the table
     * @return The estimated cardinality of the scan with the specified
     *         selectivityFactor
     */
    public int estimateTableCardinality(double selectivityFactor) {
        // DONE
    	return (int)(numTuples * selectivityFactor);
    }

    /**
     * The average selectivity of the field under op.
     * @param field
     *        the index of the field
     * @param op
     *        the operator in the predicate
     * The semantic of the method is that, given the table, and then given a
     * tuple, of which we do not know the value of the field, return the
     * expected selectivity. You may estimate this value from the histograms.
     * */
    public double avgSelectivity(int field, Predicate.Op op) {
        // DONE
    	double selectivity = 0.0;
    	
    	if (td.getFieldType(field) == Type.INT_TYPE) {
    		selectivity = intHistograms[field].avgSelectivity();
    	}
    	else if (td.getFieldType(field) == Type.STRING_TYPE) {
    		selectivity = stringHistograms[field].avgSelectivity();
    	}
    	
    	return selectivity;
    	
    	// NOTE: I tried this optimization, but it does not work
    	// because it reinvents the wheel by scanning the entire table
    	// to estimate selectivity for each of the tuple
    	/*try {
    		
    		itr.open();
        	
        	Tuple tuple;
        	while (itr.hasNext()) {
        		tuple = itr.next();
        		
        		selectivity += estimateSelectivity(field, op, tuple.getField(field));
        	}
        	
        	itr.close();
        	
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
        return selectivity / totalTuples();*/
    }

    /**
     * Estimate the selectivity of predicate <tt>field op constant</tt> on the
     * table.
     * 
     * @param field
     *            The field over which the predicate ranges
     * @param op
     *            The logical operation in the predicate
     * @param constant
     *            The value against which the field is compared
     * @return The estimated selectivity (fraction of tuples that satisfy) the
     *         predicate
     */
    public double estimateSelectivity(int field, Predicate.Op op, Field constant) {
        // DONE
    	double selectivity = 0.0;
    	
    	if (td.getFieldType(field) == Type.INT_TYPE)
    		selectivity = intHistograms[field].estimateSelectivity(op, ((IntField) constant).getValue());
    	else if (td.getFieldType(field) == Type.STRING_TYPE)
    		selectivity = stringHistograms[field].estimateSelectivity(op, ((StringField) constant).getValue());
        
    	return selectivity;
    }

    /**
     * return the total number of tuples in this table
     * */
    public int totalTuples() {
        // DONE
    	return numTuples;
    }

}
