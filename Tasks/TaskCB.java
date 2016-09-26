package osp.Tasks;



import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

import osp.IFLModules.*;
import osp.Threads.*;
import osp.Ports.*;
import osp.Memory.*;
import osp.FileSys.*;
import osp.Utilities.*;
import osp.Hardware.*;

/**
    The student module dealing with the creation and killing of
    tasks.  A task acts primarily as a container for threads and as
    a holder of resources.  Execution is associated entirely with
    threads.  The primary methods that the student will implement
    are do_create(TaskCB) and do_kill(TaskCB).  The student can choose
    how to keep track of which threads are part of a task.  In this
    implementation, an array is used.

    @OSPProject Tasks
*/
public class TaskCB extends IflTaskCB
{
	
	private GenericList threads; 	// Synchronized list to maintain threads
	private GenericList ports;		// Synchronized list to maintain ports
	private GenericList files;		// Synchronized list to maintain files
	
    /**
       The task constructor. Must have super
       as its first statement.

       @OSPProject Tasks
    */	
    public TaskCB()
    {
    	super(); 
    }

    /**
       This method is called once at the beginning of the
       simulation. Can be used to initialize static variables.

       @OSPProject Tasks
    */
    public static void init()
    {
    }

    /** 
        Sets the properties of a new task, passed as an argument. 
        
        Creates a new thread list, sets TaskLive status and creation time,
        creates and opens the task's swap file of the size equal to the size
	(in bytes) of the addressable virtual memory.

	@return task or null

        @OSPProject Tasks
    */
    static public TaskCB do_create()
    {
       
    	TaskCB newTask = new TaskCB();					// Create a task
    	
    	PageTable pageTable = new PageTable(newTask);  	// Create the pagetable for task
    	
    	newTask.threads = new GenericList(); 			// Create the thread list of the task			
    	newTask.ports = new GenericList();				// Create the ports list of the task
    	newTask.files = new GenericList();				// Create the file list of the task
    	
    	newTask.setPageTable(pageTable);				// Assign the page table to the task
    	newTask.setCreationTime(HClock.get());			// Set creation time of the task as current system time
    	newTask.setStatus(TaskLive);					// Set the status of the task to TaskLive
    	newTask.setPriority(2);							// Set the priority of the task to some arbitrary value
    	OpenFile swapFile = createSwapFile(newTask);	// Create swap file
    	if(swapFile == null)							// If swap file creation failed, dispatch a thread and return null
    	{
    		ThreadCB.dispatch();
    		return null;
    	}
    	else
        {
    	newTask.setSwapFile(swapFile);					// If swap file creation is successful, set the created file as the swap file.
    	ThreadCB.create(newTask);						// Create a active thread for the task. In order to execute, the task
        }												// should atleast have one active thread.

    	return newTask;									// return the created, and initialized task
    }
    
    
    /* Utillity function to create swap file */
    private static OpenFile createSwapFile(TaskCB newTask) {
    	String swapFilePath = SwapDeviceMountPoint + "/" + newTask.getID(); // Construct the path of swap file
    	
    	/* Find the size of the swap file. As the swap file contains  the image of the task’s virtual memory space,
    	 *  it is equal to the maximum number of bytes in the virtual address space of the task	. 
    	 *  The maximum number of bytes, which can be addressed with n  bits is,
    	 *   2^n. So the swap file size is equal to 2^n bytes. */	
    	
    	int swapFileSize = (int)Math.pow(2,MMU.getVirtualAddressBits());	
    	OpenFile file = null;
    
    	/* If file was created successfully, open the file for the task and return the OpenFile */
		if(FileSys.create(swapFilePath, swapFileSize)!=0) {
			 file = OpenFile.open(swapFilePath, newTask);
		}
		return file;	
	}

	/**
       Kills the specified task and all of it threads. 

       Sets the status TaskTerm, frees all memory frames 
       (reserved frames may not be unreserved, but must be marked 
       free), deletes the task's swap file.
	
       @OSPProject Tasks
    */
    public void do_kill()
    {
    	/* Get Enumberations for thread, ports and files */
    	Enumeration<ThreadCB> itThread=threads.forwardIterator();
    	Enumeration<PortCB> itports=ports.forwardIterator();
    	Enumeration<OpenFile> itFile=files.forwardIterator();
      
    	// Kill all the active threads in the task
    	while (itThread.hasMoreElements()){
    	   itThread.nextElement().kill();
       }
       
    	// Destroy all the ports in the task
       while (itports.hasMoreElements()){
    	   itports.nextElement().destroy();
       }
       
       // Close all the files open by the task.
       while (itFile.hasMoreElements()){
    	   itFile.nextElement().close();
       }
       
      
       this.setStatus(TaskTerm);				// Set the status of the task as terminated.
       this.getPageTable().deallocateMemory();	// Deallocate the memory of the page table

       FileSys.delete(SwapDeviceMountPoint + "/" + this.getID() );        // delete the swap file.
    }

    /** 
	Returns a count of the number of threads in this task. 
	
	@OSPProject Tasks
    */
    public int do_getThreadCount()
    {
    	return threads.length();
    }

    /**
       Adds the specified thread to this task. 
       @return FAILURE, if the number of threads exceeds MaxThreadsPerTask;
       SUCCESS otherwise.
       
       @OSPProject Tasks
    */
    public int do_addThread(ThreadCB thread)
    {
    	/* If the current thread count is below the maximum threads allowed per task, append the thread to the list and 
    	  return success, else return failure */
    	if(this.do_getThreadCount()<ThreadCB.MaxThreadsPerTask){
    		threads.append(thread);
    		return SUCCESS;
    	}
    	
    	 return FAILURE;
    
    }

    /**
       Removes the specified thread from this task. 		

       @OSPProject Tasks
    */
    public int do_removeThread(ThreadCB thread)
    {
        // Remove a thread from the list. If success return SUCCESS,else return FAILURE.
    	if (threads.remove(thread)!=null){
    		return SUCCESS;
    	}	
    	return FAILURE;
    }

    /**
       Return number of ports currently owned by this task. 

       @OSPProject Tasks
    */
    public int do_getPortCount()
    {
    	return ports.length();
    }

    /**
       Add the port to the list of ports owned by this task.
	
       @OSPProject Tasks 
    */ 
    public int do_addPort(PortCB newPort)
    {
    	/* If the current port count is below the maximum number of ports allowed per task, append 
    	 the port to the list and return success, else return failure */
    	if(this.do_getPortCount()<PortCB.MaxPortsPerTask){
    		ports.append(newPort);
    		return SUCCESS;
    	}
    	
    	 return FAILURE;
    
    }

    /**
       Remove the port from the list of ports owned by this task.

       @OSPProject Tasks 
    */ 
    public int do_removePort(PortCB oldPort)
    {
        // remove the specified port from the list. If successful, return SUCCESS else return FAILURE.
    	if (ports.remove(oldPort)!=null){
    		return SUCCESS;
    	}
    	
    	
    	return FAILURE;
    }

    /**
       Insert file into the open files table of the task.

       @OSPProject Tasks
    */
    public void do_addFile(OpenFile file)
    {
    		// Append the passed OpenFile instance to the file list
        	files.append(file);

    }

    /** 
	Remove file from the task's open files table.

	@OSPProject Tasks
    */
    public int do_removeFile(OpenFile file)
    {
    	// remove the passed file from the file list. If successful return SUCCESS else return FAILURE
    	if (files.remove(file)!=null){
    		return SUCCESS;
    	}	
    	return FAILURE;
    }

    /**
       Called by OSP after printing an error message. The student can
       insert code here to print various tables and data structures
       in their state just after the error happened.  The body can be
       left empty, if this feature is not used.
       
       @OSPProject Tasks
    */
    public static void atError()
    {
        // your code goes here

    }

    /**
       Called by OSP after printing a warning message. The student
       can insert code here to print various tables and data
       structures in their state just after the warning happened.
       The body can be left empty, if this feature is not used.
       
       @OSPProject Tasks
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
