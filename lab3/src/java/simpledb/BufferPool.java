package simpledb;

import java.io.*;

import java.util.*; // for ArrayList<>
import java.util.concurrent.ConcurrentHashMap;

/**
 * BufferPool manages the reading and writing of pages into memory from
 * disk. Access methods call into it to retrieve pages, and it fetches
 * pages from the appropriate location.
 * <p>
 * The BufferPool is also responsible for locking;  when a transaction fetches
 * a page, BufferPool checks that the transaction has the appropriate
 * locks to read/write the page.
 * 
 * @Threadsafe, all fields are final
 */
public class BufferPool {
    /** Bytes per page, including header. */
    public static final int PAGE_SIZE = 4096;

    private static int pageSize = PAGE_SIZE;
    
    /** Default number of pages passed to the constructor. This is used by
    other classes. BufferPool should use the numPages argument to the
    constructor instead. */
    public static final int DEFAULT_PAGES = 50;

    // ME:
    // As indicated above, ConcurrentHashMap is clearly recommended to store the pages
    // because it supports concurrent retrievals and some degree of concurrent updates 
    private ConcurrentHashMap<PageId, PageNode> pageNodes;
    private int maxNumPages;
    private PageId newestPageId = null;
    private PageId oldestPageId = null;
    
    // Used to enforce LRU policy
    private class PageNode {
    	public PageId next = null;
    	public PageId prev = null;
    	public Page page;
    	
    	public PageNode(Page page) {
    		this.page = page;
    	}
    }
    
    private void insertToMap(Page page) {
    	
    	PageId pid = page.getId();
    	
    	if (pageNodes.get(pid) == null) {
    		
    		PageNode pn = new PageNode(page);
    		pageNodes.put(pid, pn);
    		
    		if (newestPageId != null) {
    			PageNode second = pageNodes.get(newestPageId);
    			second.prev = pid;
    		}
    		
    		pn.prev = null;
    		pn.next = newestPageId;
    		newestPageId = pid;
    		
    		if (oldestPageId == null)
    			oldestPageId = pid;
    		
    	} else {
    		
    		// Page has been cached, update MAP
    		if (pid != newestPageId) {
        		PageNode pn = pageNodes.get(pid);
        		
        		if (pn.prev != null) {
        			PageNode prevNode = pageNodes.get(pn.prev);
        			prevNode.next = pn.next;
        		}
        		
        		if (pn.next != null) {
        			PageNode nextNode = pageNodes.get(pn.next);
        			nextNode.prev = pn.prev;
        		}
        		
        		if (newestPageId != null) {
        			PageNode second = pageNodes.get(newestPageId);
        			second.prev = pid;
        		}
        		
        		pn.prev = null;
        		pn.next = newestPageId;
        		newestPageId = pid;
        		
        		if (oldestPageId == null)
        			oldestPageId = pid;
    		}
    		
    	}
    }
    
    /**
     * Creates a BufferPool that caches up to numPages pages.
     *
     * @param numPages maximum number of pages in this buffer pool.
     */
    public BufferPool(int numPages) {
    	// DONE
        pageNodes = new ConcurrentHashMap<PageId, PageNode>();
    	maxNumPages = numPages;
    }
    
    public static int getPageSize() {
      return pageSize;
    }
    
    // THIS FUNCTION SHOULD ONLY BE USED FOR TESTING!!
    public static void setPageSize(int pageSize) {
    	BufferPool.pageSize = pageSize;
    }

    /**
     * Retrieve the specified page with the associated permissions.
     * Will acquire a lock and may block if that lock is held by another
     * transaction.
     * <p>
     * The retrieved page should be looked up in the buffer pool.  If it
     * is present, it should be returned.  If it is not present, it should
     * be added to the buffer pool and returned.  If there is insufficient
     * space in the buffer pool, an page should be evicted and the new page
     * should be added in its place.
     *
     * @param tid the ID of the transaction requesting the page
     * @param pid the ID of the requested page
     * @param perm the requested permissions on the page
     */
    public  Page getPage(TransactionId tid, PageId pid, Permissions perm)
        throws TransactionAbortedException, DbException {
        // DONE
    	// Get the page by the pid
    	PageNode pn = pageNodes.get(pid);
    	Page page;
    	
    	if (pn == null) {
    		// Page has not been cached
    		if (pageNodes.size() == maxNumPages)
    			evictPage();
    		
    		page = Database.getCatalog().getDatabaseFile(pid.getTableId()).readPage(pid);
    		
    		// IMPORTANT: store page in node
    		insertToMap(page);
    	} else {
    		//System.out.println("BufferPool.java: Page has been cached.");
    		page = pn.page;
    	}
    	
    	return page;
    }

    /**
     * Releases the lock on a page.
     * Calling this is very risky, and may result in wrong behavior. Think hard
     * about who needs to call this and why, and why they can run the risk of
     * calling it.
     *
     * @param tid the ID of the transaction requesting the unlock
     * @param pid the ID of the page to unlock
     */
    public  void releasePage(TransactionId tid, PageId pid) {
        // some code goes here
        // not necessary for lab1|lab2
    }

    /**
     * Release all locks associated with a given transaction.
     *
     * @param tid the ID of the transaction requesting the unlock
     */
    public void transactionComplete(TransactionId tid) throws IOException {
        // some code goes here
        // not necessary for lab1|lab2
    }

    /** Return true if the specified transaction has a lock on the specified page */
    public boolean holdsLock(TransactionId tid, PageId p) {
        // some code goes here
        // not necessary for lab1|lab2
        return false;
    }

    /**
     * Commit or abort a given transaction; release all locks associated to
     * the transaction.
     *
     * @param tid the ID of the transaction requesting the unlock
     * @param commit a flag indicating whether we should commit or abort
     */
    public void transactionComplete(TransactionId tid, boolean commit)
        throws IOException {
        // some code goes here
        // not necessary for lab1|lab2
    }

    /**
     * Add a tuple to the specified table on behalf of transaction tid.  Will
     * acquire a write lock on the page the tuple is added to and any other 
     * pages that are updated (Lock acquisition is not needed for lab2). 
     * May block if the lock(s) cannot be acquired.
     * 
     * Marks any pages that were dirtied by the operation as dirty by calling
     * their markDirty bit, and updates cached versions of any pages that have 
     * been dirtied so that future requests see up-to-date pages. 
     *
     * @param tid the transaction adding the tuple
     * @param tableId the table to add the tuple to
     * @param t the tuple to add
     */
    public void insertTuple(TransactionId tid, int tableId, Tuple t)
        throws DbException, TransactionAbortedException {
    	// IOException - removed
        // DONE
    	try {
    		HeapFile file = (HeapFile)Database.getCatalog().getDatabaseFile(tableId);
        	ArrayList<Page> dirtiedPages = file.insertTuple(tid, t);
        	
        	for (Page dirtiedPage: dirtiedPages) {
        		// Mark page as dirty
        		dirtiedPage.markDirty(true, tid);
        		
        		// Update page's cached version
        		pageNodes.get(dirtiedPage.getId()).page = dirtiedPage;
        	}
    	} catch(IOException e) {
    		e.printStackTrace();
    	}
    }

    /**
     * Remove the specified tuple from the buffer pool.
     * Will acquire a write lock on the page the tuple is removed from and any
     * other pages that are updated. May block if the lock(s) cannot be acquired.
     *
     * Marks any pages that were dirtied by the operation as dirty by calling
     * their markDirty bit, and updates cached versions of any pages that have 
     * been dirtied so that future requests see up-to-date pages. 
     *
     * @param tid the transaction deleting the tuple.
     * @param t the tuple to delete
     */
    public  void deleteTuple(TransactionId tid, Tuple t)
        throws DbException, TransactionAbortedException {
    	// IOException - removed
    	// DONE
    	HeapFile file = (HeapFile)Database.getCatalog().getDatabaseFile(t.getRecordId().getPageId().getTableId());
		ArrayList<Page> dirtiedPages = file.deleteTuple(tid, t);
		
		for (Page dirtiedPage: dirtiedPages) {
			// Mark page as dirty
			dirtiedPage.markDirty(true, tid);
			
			// Update page's cached version
			pageNodes.get(dirtiedPage.getId()).page = dirtiedPage;
		}
    }

    /**
     * Flush all dirty pages to disk.
     * NB: Be careful using this routine -- it writes dirty data to disk so will
     *     break simpledb if running in NO STEAL mode.
     */
    public synchronized void flushAllPages() throws IOException {
        // DONE
    	for (PageNode pn: pageNodes.values()) {
    		if (pn.page.isDirty() != null)
    			flushPage(pn.page.getId());
    	}
    }

    /** Remove the specific page id from the buffer pool.
        Needed by the recovery manager to ensure that the
        buffer pool doesn't keep a rolled back page in its
        cache.
    */
    public synchronized void discardPage(PageId pid) {
        // some code goes here
        // only necessary for lab5
    }

    /**
     * Flushes a certain page to disk
     * @param pid an ID indicating the page to flush
     */
    private synchronized  void flushPage(PageId pid) throws IOException {
    	// DONE
    	try {
    		Page page = pageNodes.get(pid).page;
        	
        	// Write the page to disk
        	Database.getCatalog().getDatabaseFile(pid.getTableId()).writePage(page);
        	
        	// Mark the page as not dirty
        	page.markDirty(false, page.isDirty());
    	} catch(IOException e) {
    		e.printStackTrace();
    	} 	
    }

    /** Write all pages of the specified transaction to disk.
     */
    public synchronized  void flushPages(TransactionId tid) throws IOException {
        // some code goes here
        // not necessary for lab1|lab2
    }

    /**
     * Discards a page from the buffer pool.
     * Flushes the page to disk to ensure dirty pages are updated on disk.
     */
    private synchronized  void evictPage() throws DbException {
        // DONE
    	try {
    		
    		if (oldestPageId != null) {
    			
    			// Evict the least-recently-used page
        		PageNode pn = pageNodes.get(oldestPageId);
        		PageId tempId = oldestPageId;
        		
        		if (pn.prev != null) {
        			PageNode prevNode = pageNodes.get(pn.prev);
            		prevNode.next = null;	
        		}
        		
        		// Update oldest pageId
        		oldestPageId = pn.prev;
        		
        		if (oldestPageId == null)
        			newestPageId = null;
        		
        		flushPage(tempId);
        		pageNodes.remove(tempId);
    		}
    		
    	} catch(IOException e) {
    		e.printStackTrace();
    	}	
    }

}
