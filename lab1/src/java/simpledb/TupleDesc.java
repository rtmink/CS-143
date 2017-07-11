package simpledb;

import java.io.Serializable;
import java.util.*;

/**
 * TupleDesc describes the schema of a tuple.
 */
public class TupleDesc implements Serializable {

    /**
     * A help class to facilitate organizing the information of each field
     * */
    public static class TDItem implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * The type of the field
         * */
        public final Type fieldType;
        
        /**
         * The name of the field
         * */
        public final String fieldName;

        public TDItem(Type t, String n) {
            this.fieldName = n;
            this.fieldType = t;
        }

        public String toString() {
            return fieldName + "(" + fieldType + ")";
        }
    }
    
    // ME: List of fields
    // ArrayList makes sense because it is an ordered list of the fields
    private ArrayList<TDItem> fields = new ArrayList<TDItem>();

    /**
     * @return
     *        An iterator which iterates over all the field TDItems
     *        that are included in this TupleDesc
     * */
    public Iterator<TDItem> iterator() {
        // DONE
    	return fields.iterator();
    }

    private static final long serialVersionUID = 1L;

    /**
     * Create a new TupleDesc with typeAr.length fields with fields of the
     * specified types, with associated named fields.
     * 
     * @param typeAr
     *            array specifying the number of and types of fields in this
     *            TupleDesc. It must contain at least one entry.
     * @param fieldAr
     *            array specifying the names of the fields. Note that names may
     *            be null.
     */
    public TupleDesc(Type[] typeAr, String[] fieldAr) {
        // DONE
    	if (typeAr.length < 1)
    		// error: must contain at least one entry
    		throw new IllegalArgumentException("TupleDesc must contain at least one entry.");
    	
    	for (int i = 0; i < typeAr.length; i++)
    		fields.add(new TDItem(typeAr[i], fieldAr[i]));
    }

    /**
     * Constructor. Create a new tuple desc with typeAr.length fields with
     * fields of the specified types, with anonymous (unnamed) fields.
     * 
     * @param typeAr
     *            array specifying the number of and types of fields in this
     *            TupleDesc. It must contain at least one entry.
     */
    public TupleDesc(Type[] typeAr) {
        // DONE
    	if (typeAr.length < 1)
    		// error: must contain at least one entry
    		throw new IllegalArgumentException("TupleDesc must contain at least one entry.");
    	
    	// Unnamed field is denoted as an empty string
    	for (int i = 0; i < typeAr.length; i++)
    		fields.add(new TDItem(typeAr[i], ""));
    }

    /**
     * @return the number of fields in this TupleDesc
     */
    public int numFields() {
        // DONE
    	return fields.size();
    }

    /**
     * Gets the (possibly null) field name of the ith field of this TupleDesc.
     * 
     * @param i
     *            index of the field name to return. It must be a valid index.
     * @return the name of the ith field
     * @throws NoSuchElementException
     *             if i is not a valid field reference.
     */
    public String getFieldName(int i) throws NoSuchElementException {
        // DONE
    	if (i < 0 || i >= this.numFields())
    		throw new NoSuchElementException("Invalid field index.");
    	
    	return fields.get(i).fieldName;
    }

    /**
     * Gets the type of the ith field of this TupleDesc.
     * 
     * @param i
     *            The index of the field to get the type of. It must be a valid
     *            index.
     * @return the type of the ith field
     * @throws NoSuchElementException
     *             if i is not a valid field reference.
     */
    public Type getFieldType(int i) throws NoSuchElementException {
        // DONE
    	if (i < 0 || i >= this.numFields())
    		// error: invalid index
    		throw new NoSuchElementException("Invalid field index.");
    	
    	return fields.get(i).fieldType;
    }

    /**
     * Find the index of the field with a given name.
     * 
     * @param name
     *            name of the field.
     * @return the index of the field that is first to have the given name.
     * @throws NoSuchElementException
     *             if no field with a matching name is found.
     */
    public int fieldNameToIndex(String name) throws NoSuchElementException {
        // DONE
    	int i = 0;
    	Iterator<TDItem> itr = this.iterator();
    	while (itr.hasNext())
    	{
    		if (itr.next().fieldName.equals(name))	// TODO: check for null name?
    			return i;
    		
    		i++;
    	}
    	
    	// no field with a matching name, throw an error
    	throw new NoSuchElementException("No field with a matching name.");
    }

    /**
     * @return The size (in bytes) of tuples corresponding to this TupleDesc.
     *         Note that tuples from a given TupleDesc are of a fixed size.
     */
    public int getSize() {
        // DONE
    	int size = 0;
    	for (TDItem field: fields)
    		size += field.fieldType.getLen();
    	
    	return size;
    }

    /**
     * Merge two TupleDescs into one, with td1.numFields + td2.numFields fields,
     * with the first td1.numFields coming from td1 and the remaining from td2.
     * 
     * @param td1
     *            The TupleDesc with the first fields of the new TupleDesc
     * @param td2
     *            The TupleDesc with the last fields of the TupleDesc
     * @return the new TupleDesc
     */
    public static TupleDesc merge(TupleDesc td1, TupleDesc td2) {
        // DONE
    	int arraySize = td1.numFields() + td2.numFields();
    	Type types[] = new Type[arraySize];
    	String names[] = new String[arraySize];
    	
    	// i is used in both for loops so it is declared outside the first loop
    	int i;
    	for (i = 0; i < td1.numFields(); i++)
    	{
    		types[i] = td1.getFieldType(i);
    		names[i] = td1.getFieldName(i);
    	}
    	
    	for (int j = 0; j < td2.numFields(); j++)
    	{
    		types[i] = td2.getFieldType(j);
    		names[i] = td2.getFieldName(j);
    		i++;
    	}
    	
    	return new TupleDesc(types, names);
    }

    /**
     * Compares the specified object with this TupleDesc for equality. Two
     * TupleDescs are considered equal if they are the same size and if the n-th
     * type in this TupleDesc is equal to the n-th type in td.
     * 
     * @param o
     *            the Object to be compared for equality with this TupleDesc.
     * @return true if the object is equal to this TupleDesc.
     */
    public boolean equals(Object o) {
        // DONE
    	if (!(o instanceof TupleDesc))
    		// object is not an instance of TupleDesc
    		return false;
    	
    	TupleDesc td = (TupleDesc)o;
    	
    	if (this.getSize() != td.getSize())
    		// different size 
    		return false;
    	
    	for (int i = 0; i < this.numFields(); i++)
    	{
    		if (!(this.getFieldType(i).equals(td.getFieldType(i))))
    			// n-th types are not equal
    			return false;
    	}
    	
    	return true;
    }

    public int hashCode() {
        // If you want to use TupleDesc as keys for HashMap, implement this so
        // that equal objects have equals hashCode() results
        throw new UnsupportedOperationException("unimplemented");
    }

    /**
     * Returns a String describing this descriptor. It should be of the form
     * "fieldType[0](fieldName[0]), ..., fieldType[M](fieldName[M])", although
     * the exact format does not matter.
     * 
     * @return String describing this descriptor.
     */
    public String toString() {
        // DONE
    	int i;
    	String desc = "";
    	
    	// Make sure the last string does not end with a comma
    	for (i = 0; i < this.numFields() - 1; i++)
    		desc += this.getFieldType(i) + "(" + this.getFieldName(i) + "), ";
    	
    	desc += this.getFieldType(i) + "(" + this.getFieldName(i) + ")";
    	
    	return desc;
    }
}
