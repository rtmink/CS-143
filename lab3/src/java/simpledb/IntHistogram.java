package simpledb;

/** A class to represent a fixed-width histogram over a single integer-based field.
 */
public class IntHistogram {

	// ME
	private int numBuckets;
	private int min;
	private int max;
	private double bucketSize; // uniform range of the values in the bucket
	private int numTuples;
	private int[] buckets; // each entry contains the number of tuples in the bucket
	
    /**
     * Create a new IntHistogram.
     * 
     * This IntHistogram should maintain a histogram of integer values that it receives.
     * It should split the histogram into "buckets" buckets.
     * 
     * The values that are being histogrammed will be provided one-at-a-time through the "addValue()" function.
     * 
     * Your implementation should use space and have execution time that are both
     * constant with respect to the number of values being histogrammed.  For example, you shouldn't 
     * simply store every value that you see in a sorted list.
     * 
     * @param buckets The number of buckets to split the input value into.
     * @param min The minimum integer value that will ever be passed to this class for histogramming
     * @param max The maximum integer value that will ever be passed to this class for histogramming
     */
    public IntHistogram(int buckets, int min, int max) {
    	// DONE
    	this.numBuckets = buckets;
    	this.min = min;
    	this.max = max;
    	// IMPORTANT: cast the denominator to double to correctly compute bucketSize
    	this.bucketSize = (max - min) / (double) this.numBuckets;  
    	this.numTuples = 0;
    	this.buckets = new int[this.numBuckets];
    }
    
    // ME
    private int index(int v) {
    	int index = (int)((v - min) / bucketSize);
    	if (index == numBuckets && index > 0)
    		index--;
    	return index;
    }

    /**
     * Add a value to the set of values that you are keeping a histogram of.
     * @param v Value to add to the histogram
     */
    public void addValue(int v) {
    	// DONE
    	if (v < min || v > max)
    		return;
    	
    	buckets[index(v)]++;
    	numTuples++;
    }

    // ME
    private double equalSelectivity(int v) {
    	if (v < min || v > max)
    		return 0.0;
    	
    	// IMPORTANT: Ensure bucket size is at least 1 if it happens to be a fraction of 1
    	return (buckets[index(v)] / Math.max(1, bucketSize)) / numTuples;
    }
    
    // ME
    private double greaterThanSelectivity(int v) {
    	if (v < min)
    		return 1.0;
    	
    	if (v > max)
    		return 0.0;
    	
    	int index = index(v);
    	double bFrac = buckets[index] / numTuples;
    	double bRight = (index + 1) * bucketSize;
    	double bPart = (bRight - v) / bucketSize;
    	double otherBFracs = 0.0;
    	
    	for (int i = index + 1; i < numBuckets; i++) {
    		otherBFracs += buckets[i];
    	}
    	otherBFracs /= numTuples;
    	
    	// contribution of bucket containing v whose value is greater than v
    	// plus the rest of the buckets
    	return (bFrac * bPart) + otherBFracs; 
    }
    
    /**
     * Estimate the selectivity of a particular predicate and operand on this table.
     * 
     * For example, if "op" is "GREATER_THAN" and "v" is 5, 
     * return your estimate of the fraction of elements that are greater than 5.
     * 
     * @param op Operator
     * @param v Value
     * @return Predicted selectivity of this particular operator and value
     */
    public double estimateSelectivity(Predicate.Op op, int v) {
    	// DONE
    	double selectivity = 0.0;
    	
    	if (op == Predicate.Op.EQUALS)
    		selectivity = equalSelectivity(v);
    	else if (op == Predicate.Op.GREATER_THAN)
    		selectivity = greaterThanSelectivity(v);
    	else if (op == Predicate.Op.GREATER_THAN_OR_EQ)
    		selectivity = greaterThanSelectivity(v) + equalSelectivity(v);
    	else if (op == Predicate.Op.LESS_THAN)
    		selectivity = 1 - greaterThanSelectivity(v) - equalSelectivity(v);
    	else if (op == Predicate.Op.LESS_THAN_OR_EQ)
    		selectivity = 1 - greaterThanSelectivity(v);
    	else if (op == Predicate.Op.NOT_EQUALS)
    		selectivity = 1 - equalSelectivity(v);
    	
    	return selectivity;
    }
    
    /**
     * @return
     *     the average selectivity of this histogram.
     *     
     *     This is not an indispensable method to implement the basic
     *     join optimization. It may be needed if you want to
     *     implement a more efficient optimization
     * */
    public double avgSelectivity()
    {
        // DONE
    	/*double selectivity = 0.0;
    	for (int i = 0; i < numBuckets; i++) {
    		selectivity += buckets[i];
    	}
        return selectivity / numBuckets;*/
    	
    	// NOTE: more efficient than above
    	return numTuples / numBuckets;
    }
    
    /**
     * @return A string describing this histogram, for debugging purposes
     */
    public String toString() {
    	// DONE
    	String heights = "| " + buckets[0] + " | ";
    	String ranges = "| " + min + " | ";
    	
    	for (int i = 1; i < numBuckets; i++) {
    		heights += buckets[i] + " | ";
    		int range = min + (i * (int)bucketSize) + 1;
    		ranges += range + " | ";
    	}
    	
        return heights + "\n" + ranges;
    }
}
