package simpledb;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Tuple maintains information about the contents of a tuple. Tuples have a
 * specified schema specified by a TupleDesc object and contain Field objects
 * with the data for each field.
 */
public class Tuple implements Serializable {

    private static final long serialVersionUID = 1L;
    
    // ME:
    private TupleDesc myTD;
    private RecordId myRID;
    private Field fields[];

    /**
     * Create a new tuple with the specified schema (type).
     * 
     * @param td
     *            the schema of this tuple. It must be a valid TupleDesc
     *            instance with at least one field.
     */
    public Tuple(TupleDesc td) {
        // DONE
    	if (td.numFields() < 1)
    		// error: TupleDesc needs to have at least one field
    		throw new IllegalArgumentException("Invalid TupleDesc with less than one field.");
    	
    	myTD = td;
    	
    	fields = new Field[myTD.numFields()];
    }

    /**
     * @return The TupleDesc representing the schema of this tuple.
     */
    public TupleDesc getTupleDesc() {
        // DONE
    	return myTD;
    }

    /**
     * @return The RecordId representing the location of this tuple on disk. May
     *         be null.
     */
    public RecordId getRecordId() {
        // DONE
    	return myRID;
    }

    /**
     * Set the RecordId information for this tuple.
     * 
     * @param rid
     *            the new RecordId for this tuple.
     */
    public void setRecordId(RecordId rid) {
        // DONE
    	myRID = rid;
    }

    /**
     * Change the value of the ith field of this tuple.
     * 
     * @param i
     *            index of the field to change. It must be a valid index.
     * @param f
     *            new value for the field.
     */
    public void setField(int i, Field f) {
        // DONE
    	if (i < 0 && i >= myTD.numFields())
    		// error: invalid index
    		throw new NoSuchElementException("Invalid field index.");
    	
    	fields[i] = f;
    }

    /**
     * @return the value of the ith field, or null if it has not been set.
     * 
     * @param i
     *            field index to return. Must be a valid index.
     */
    public Field getField(int i) {
        // DONE
    	if (i < 0 && i >= myTD.numFields())
    		// error: invalid index
    		throw new NoSuchElementException("Invalid field index.");
    	
    	return fields[i];
    }

    /**
     * Returns the contents of this Tuple as a string. Note that to pass the
     * system tests, the format needs to be as follows:
     * 
     * column1\tcolumn2\tcolumn3\t...\tcolumnN\n
     * 
     * where \t is any whitespace, except newline, and \n is a newline
     */
    public String toString() {
        // DONE
    	// A single space delimits the columns
    	String str = "";
    	int i;
    	for (i = 0; i < myTD.numFields() - 1; i++)
    		str += fields[i].toString() + " ";
    	
    	str += fields[i].toString() + "\n";
    	return str;
    }
    
    /**
     * @return
     *        An iterator which iterates over all the fields of this tuple
     * */
    public Iterator<Field> fields()
    {
        // DONE
    	return Arrays.asList(fields).iterator();
    }
    
    /**
     * reset the TupleDesc of this tuple
     * */
    public void resetTupleDesc(TupleDesc td)
    {
        // DONE
    	myTD = td;
    }
}
