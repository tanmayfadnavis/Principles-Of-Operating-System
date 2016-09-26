package osp.Threads;
import java.util.Vector;
import java.util.Enumeration;
import osp.Utilities.*;
import sun.util.resources.CurrencyNames_th_TH;
import osp.IFLModules.*;
import osp.Tasks.*;
import osp.EventEngine.*;
import osp.Hardware.*;
import osp.Devices.*;
import osp.Memory.*;
import osp.Resources.*;

/**
   This class is responsible for actions related to threads, including
   creating, killing, dispatching, resuming, and suspending threads.

   @OSPProject Threads
*/
public class ThreadCB extends IflThreadCB 
{
	
	public static GenericList readyQueue = new GenericList();
	
	
	/**
	 * 0 : FCFS,
	 * 1 : Round Robin,
	 * 2 : FeedBack 
	**/
	public static int schedulingAlgorithm = 1;
    /**
       The thread constructor. Must call 

       	   super();

       as its first statement.

       @OSPProject Threads
    */
    public ThreadCB()
    {
    }

    /**
       This method will be called once at the beginning of the
       simulation. The student can set up static variables here.
       
       @OSPProject Threads
    */
    public static void init()
    {
    }

    /** 
        Sets up a new thread and adds it to the given task. 
        The method must set the ready status 
        and attempt to add thread to task. If the latter fails 
        because there are already too many threads in this task, 
        so does this method, otherwise, the thread is appended 
        to the ready queue and dispatch() is called.

	The priority of the thread can be set using the getPriority/setPriority
	methods. However, OSP itself doesn't care what the actual value of
	the priority is. These methods are just provided in case priority
	scheduling is required.

	@return thread or null

        @OSPProject Threads
    */
    static public ThreadCB do_create(TaskCB task)
    {
    	ThreadCB thread = null;
    	if(task.getThreadCount() < ThreadCB.MaxThreadsPerTask)
    	{
    		thread = new ThreadCB();
        	if(task.addThread(thread) == FAILURE)
        		thread = null;   	
        	else 
        	{
        		thread.setTask(task);
        		thread.setPriority(task.getPriority());
        		thread.setStatus(ThreadCB.ThreadReady);
        		readyQueue.append(thread);      		
        	}       		
    	}   	
       dispatch();      
       return thread;    	

    }

    /** 
	Kills the specified thread. 

	The status must be set to ThreadKill, the thread must be
	removed from the task's list of threads and its pending IORBs
	must be purged from all device queues.
        
	If some thread was on the ready queue, it must removed, if the 
	thread was running, the processor becomes idle, and dispatch() 
	must be called to resume a waiting thread.
	
	@OSPProject Threads
    */
    public void do_kill()
    {
        int currentStatus = getStatus();
        if(currentStatus == ThreadReady) // Killing a ready Thread
        	readyQueue.remove(this);
       
        else if(currentStatus == ThreadRunning) // killing a running thread  	
        	setCPUIdle();
        
        getTask().removeThread(this);
        setStatus(ThreadKill);
        
        cancelAllPendingIOForTheThread();
        ResourceCB.giveupResources(this);
        
        TaskCB task = getTask();
        if(task.getThreadCount()==0)
        	task.kill();
        
        dispatch();

    }

	private void cancelAllPendingIOForTheThread() {
		for(int i=0; i < Device.getTableSize(); i++)
        {
        	Device device = Device.get(i);
        	device.cancelPendingIO(this);
        }
	}

    /** Suspends the thread that is currenly on the processor on the 
        specified event. 

        Note that the thread being suspended doesn't need to be
        running. It can also be waiting for completion of a pagefault
        and be suspended on the IORB that is bringing the page in.
	
	Thread's status must be changed to ThreadWaiting or higher,
        the processor set to idle, the thread must be in the right
        waiting queue, and dispatch() must be called to give CPU
        control to some other thread.

	@param event - event on which to suspend this thread.

        @OSPProject Threads
    */
    public void do_suspend(Event event)
    {
    	 int currentStatus = getStatus();

         if(currentStatus == ThreadRunning) // suspending ready thread
         {
        	 setStatus(ThreadWaiting);
        	 setCPUIdle();            
         }        	
         else if(currentStatus >= ThreadWaiting) // suspending running thread
         {
        	 setStatus(currentStatus+1);        	 
         }
    	 event.addThread(this);
         dispatch();

    }

    /** Resumes the thread.
        
	Only a thread with the status ThreadWaiting or higher
	can be resumed.  The status must be set to ThreadReady or
	decremented, respectively.
	A ready thread should be placed on the ready queue.
	
	@OSPProject Threads
    */
    public void do_resume()
    {
    	int currentStatus = getStatus();
    	if(currentStatus > ThreadWaiting)
    		setStatus(currentStatus-1);
    	else if(currentStatus == ThreadWaiting)
    	{
    		setStatus(ThreadReady);
    		readyQueue.append(this);
    	}
    	dispatch();
    }

    /** 
        Selects a thread from the run queue and dispatches it. 

        If there is just one theread ready to run, reschedule the thread 
        currently on the processor.

        In addition to setting the correct thread status it must
        update the PTBR.
	
	@return SUCCESS or FAILURE

        @OSPProject Threads
    */
    public static int do_dispatch()
    {
    	
    	// Get the current running thread and do the context Switch 
    	if(schedulingAlgorithm == 0)
    	{
    		if(MMU.getPTBR() == null)
    		{
    			ThreadCB nextThread= (ThreadCB)readyQueue.removeHead();
            	if( nextThread == null )
            		return FAILURE;
         
            	nextThread.setStatus(ThreadRunning);
            	MMU.setPTBR(nextThread.getTask().getPageTable());
            	MMU.getPTBR().getTask().setCurrentThread(nextThread);
            	     	
    		}
    	}
    	else if(schedulingAlgorithm == 1)
    	{
        	preempt();
        	HTimer.set(100);
    		ThreadCB nextThread= (ThreadCB)readyQueue.removeHead();
        	if( nextThread == null )
        		return FAILURE;
     
        	nextThread.setStatus(ThreadRunning);
        	MMU.setPTBR(nextThread.getTask().getPageTable());
        	MMU.getPTBR().getTask().setCurrentThread(nextThread);
        	
    	}

    	return SUCCESS;

    }



	private static void preempt() {
		PageTable pageTable = MMU.getPTBR();
    	if(pageTable != null)
    	{
    		TaskCB currentTask = pageTable.getTask();
        	if(currentTask != null)
        	{
            	ThreadCB currentRunningThread = currentTask.getCurrentThread();
            	if(currentRunningThread != null)
            	{
                	currentRunningThread.setStatus(ThreadReady);
                	readyQueue.append(currentRunningThread);
                	setCPUIdle();            	
            	}
        	}
    	}
	}

	private static void setCPUIdle() {
		MMU.getPTBR().getTask().setCurrentThread(null);
		MMU.setPTBR(null);
	}

    /**
       Called by OSP after printing an error message. The student can
       insert code here to print various tables and data structures in
       their state just after the error happened.  The body can be
       left empty, if this feature is not used.

       @OSPProject Threads
    */
    public static void atError()
    {
        // your code goes here

    }

    /** Called by OSP after printing a warning message. The student
        can insert code here to print various tables and data
        structures in their state just after the warning happened.
        The body can be left empty, if this feature is not used.
       
        @OSPProject Threads
     */
    public static void atWarning()
    {
        // your code goes here

    }


    /*
       Feel free to add methods/fields to improve the readability of your code
    */

}

/*
      Feel free to add local classes to improve the readability of your code
*/
