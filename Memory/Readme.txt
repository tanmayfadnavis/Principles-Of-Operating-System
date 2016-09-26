*****************READ ME ******************

DESIGN OF THE MEMORY PROJECT.

FINISHED TASKS.

=========FrameTableEntry.java===============
public FrameTableEntry(int frameID). In this method, we just call the super with the frameId.

========MMU.java============================
public static void init(). In this method, we are just running the for loop for the frameTablesize and and setting the new frame table entry,

static public PageTableEntry do_refer(int memoryAddress,int referenceType, ThreadCB thread). This method handles memory references. It calculates which memory page contains the memoryAddress and then determines whether the page is valid
and does page fault if the page is invalid and if page is valid sets its page as referenced and set the dirty bit.

=========PageFaultHandler.java====================
public static int do_handlePageFault(ThreadCB thread, int referenceType,PageTableEntry page) This method handles the page faults. It checks and returns if the page is valid.
It also checks if the page is being brought in by some other thread i.e if the page is already page faulted. In this case, the thread is suspended on that page. Else, a new frame
is choosen and reserved until the swap in of requested page into frame is complete.

=========PageTable.java====================
public PageTable(TaskCB ownerTask). This method calls the super(ownerTask) constructor. Then it creates the page table entry for each and every page.
 
public void do_deallocateMemory(). This method frees up the main memory occupied by the task. It then unreserves the free pages.
========PageTableEntry.java================
boolean pageFaulted= false;
public long reftimer;
We have created 2 variables, to check if there is a page fault and the second one as reftimer.

public PageTableEntry(PageTable ownerPageTable, int pageNumber). This method calls the super constructor and then sets the reference timer.

public int do_lock(IORB iorb). This method increments the lock count on th page by one. It first increments the lock count and then checks if the page is valid. If the page is not valid, it calls the page fault handle.

=======UNFINISHED TASKS==============

NONE.

========================================

FIFO -  FIRST IN FIRST OUT. In the algorithm, the OS maintains the list of all pages in memory. When new page occurs and if there is a page fault, the oldest page, i.e. the page which came first is removed and the new page is put at the tail end of the list. Page is removed from the head.

LRU - LEAST RECENTLY USED. In this algorithm, when a page fault occurs, the page which has not been used for the longest time is thrown. This is based on the reference time for each and every page.

COMPARISON.
Comparioson is made in the presentation along with the graph. In general, page faults in LRU are less than that in FIFO.