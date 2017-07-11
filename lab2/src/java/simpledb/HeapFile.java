package simpledb;

import java.io.*;
import java.util.*;

/**
 * HeapFile is an implementation of a DbFile that stores a collection of tuples
 * in no particular order. Tuples are stored on pages, each of which is a fixed
 * size, and the file is simply a collection of those pages. HeapFile works
 * closely with HeapPage. The format of HeapPages is described in the HeapPage
 * constructor.
 * 
 * @see simpledb.HeapPage#HeapPage
 * @author Sam Madden
 */
public class HeapFile implements DbFile {
	
	// ME
	private File file;
	private TupleDesc tupleDesc;

    /**
     * Constructs a heap file backed by the specified file.
     * 
     * @param f
     *            the file that stores the on-disk backing store for this heap
     *            file.
     */
    public HeapFile(File f, TupleDesc td) {
        // DONE
    	file = f;
    	tupleDesc = td;
    }

    /**
     * Returns the File backing this HeapFile on disk.
     * 
     * @return the File backing this HeapFile on disk.
     */
    public File getFile() {
        // DONE
    	return file;
    }

    /**
     * Returns an ID uniquely identifying this HeapFile. Implementation note:
     * you will need to generate this tableid somewhere ensure that each
     * HeapFile has a "unique id," and that you always return the same value for
     * a particular HeapFile. We suggest hashing the absolute file name of the
     * file underlying the heapfile, i.e. f.getAbsoluteFile().hashCode().
     * 
     * @return an ID uniquely identifying this HeapFile.
     */
    public int getId() {
    	// DONE
        return file.getAbsoluteFile().hashCode();
    }

    /**
     * Returns the TupleDesc of the table stored in this DbFile.
     * 
     * @return TupleDesc of this DbFile.
     */
    public TupleDesc getTupleDesc() {
        // DONE
    	return tupleDesc;
    }

    // see DbFile.java for javadocs
    public Page readPage(PageId pid) {
        // DONE
    	RandomAccessFile fOut = null;
    	
    	try {
    		// Open the file
    		fOut = new RandomAccessFile(file, "r");
    	} catch(FileNotFoundException e) {
    		throw new IllegalArgumentException("File cannot be opened.");
    	}
    	
    	try {
    		// Read the page
    		fOut.seek(pid.pageNumber() * BufferPool.PAGE_SIZE);
    		byte[] bytes = new byte[BufferPool.PAGE_SIZE];
    		fOut.read(bytes, 0, BufferPool.PAGE_SIZE);
    		fOut.close();
    		return new HeapPage(new HeapPageId(pid.getTableId(), pid.pageNumber()), bytes);
    	} catch(IOException e) {
    		throw new IllegalArgumentException("Page does not exist in this file.");
    	}
    }

    // see DbFile.java for javadocs
    public void writePage(Page page) throws IOException {
        // DONE
    	RandomAccessFile fOut = null;
    	
    	try {
    		// Open the file
    		fOut = new RandomAccessFile(file, "rw");
    	} catch(FileNotFoundException e) {
    		throw new IllegalArgumentException("File cannot be opened.");
    	}
    	
    	// Write page
    	fOut.seek(page.getId().pageNumber());
    	fOut.write(page.getPageData());
    	fOut.close();
    }

    /**
     * Returns the number of pages in this HeapFile.
     */
    public int numPages() {
        // DONE
    	return (int)file.length() / BufferPool.getPageSize();
    }

    // see DbFile.java for javadocs
    public ArrayList<Page> insertTuple(TransactionId tid, Tuple t)
            throws DbException, IOException, TransactionAbortedException {
        // DONE
    	ArrayList<Page> pages = new ArrayList<Page>();
    	
    	// Check if we have an empty slot in any of our pages
    	for (int i = 0; i < numPages(); i++) {
    		
    		HeapPage page = (HeapPage)Database.getBufferPool().getPage(tid, new HeapPageId(getId(), i), Permissions.READ_WRITE);
        	
    		// Check for an empty slot
    		if (page.getNumEmptySlots() > 0) {
    			// We have an empty slot
    			page.insertTuple(t);
    			pages.add(page);
    			return pages;
    		} 
    		else {
    			// We don't have an empty slot
    			// Release the page (aka lock on the page)
    			// Maybe not necessary for lab2, however it is here for completion sake
    			Database.getBufferPool().releasePage(tid, page.getId());
    		}
    	}
    	
    	// All pages are full, create a new page
    	HeapPageId pid = new HeapPageId(getId(), numPages());
    	HeapPage newPage = new HeapPage(pid, HeapPage.createEmptyPageData());
    	
    	// Write new page data to file
    	FileOutputStream of = new FileOutputStream(file, true); // append to file
    	of.write(newPage.getPageData());
    	of.close();
    	
    	// IMPORTANT: Don't forget to update the page with the required permissions
    	newPage = (HeapPage)Database.getBufferPool().getPage(tid, pid, Permissions.READ_WRITE);
    	
    	// Insert tuple to new page
    	newPage.insertTuple(t);
    	pages.add(newPage);
    	return pages;
    }

    // see DbFile.java for javadocs
    public ArrayList<Page> deleteTuple(TransactionId tid, Tuple t) throws DbException,
            TransactionAbortedException {
        // DONE
    	ArrayList<Page> pages = new ArrayList<Page>();
    	
    	HeapPage page = (HeapPage)Database.getBufferPool().getPage(tid, t.getRecordId().getPageId(), Permissions.READ_WRITE);
    	
    	// Delete tuple from the page
    	page.deleteTuple(t);
    	pages.add(page);
    	return pages;
    }

    // see DbFile.java for javadocs
    public DbFileIterator iterator(TransactionId tid) {
        // DONE
    	return new HeapFileIterator(tid, this.getId(), this.numPages());
    }

}

