****************README***********************

#####Design of the ThreadCB. java class######

The ThreadCB class firstlt extends from IflThreadCB. Then we are creating a GenericList "readyqueue" . This will act as a queue for the threads. Also, there is a variable schedulingAlgorithm which decides which algorithm to implement i.e. FCFS or Priority or Round Robin, etc.

do_create method.
The do_create method takes a TaskCB object. It creates a null ThreadCB object.
If we are able to successfully add the thread to the task, we set the thread's priority, status and append it to the ready queue.
Then we dispatch the thread.

do_kill method.
In the do_kill method, we get the status of the current thread. If the thread is in ready state, we remove it from the ready queue. If the thread is in running state, we set the CPU to idel.

The rest of the completed methods are:

cancelAllPendingIOForTheThread().
do_suspend()
do_resume()
do_dispatch()
setCPUIdle()
preempt()

FinishedTasks.

1. static public ThreadCB do_create(TaskCB task)
2. public void do_kill()
3. private void cancelAllPendingIOForTheThread()
4. public void do_suspend(Event event)
5. public void do_resume()
6. public static int do_dispatch()
7. private static void preempt()
8. private static void setCPUIdle()
9. public static void atError()
10. public static void atWarning()

UnfinishedTasks.

NONE.

Design for the scheduling algorithms.

1.FCFS - FIRST COME FIRST SERVE.
We first check if the value of the page table base register is null. If it is, we first remove the thread from the ready queue. There is an error, if there are no threads in the ready queue. Then, we set the thread status to running. We set the PTBR to this thread and then we set the PBTR point to current thread.

2.ROUND ROBIN.
Firstly, the running thread is pre-empted. Then, the next thread is removed from the ready queue. And then, we make its status to running and change the PBTR respectively.

3.PRIORITY DRIVEN.
We first create empty thread and task. We get the current task and get the current thread of the current task and assign it to the task and thread created. We then remove a thread from the priority queue. If the new thread's priority is greater than the running thread's priority, we pre-empt the running thread. Then set the new thread's status as running and set the PBTR accordingly.

4.FEEDBACK.
We created 3 feedback queues and 1 ready queue. We schedule from the ready queue only. If a thread times out, then we put the thread in the feedback Queue1.This queue has less priority.
In a similar way, threads move from feedback queue1 to 2 and 3, if it does not complete and the priorities are reduced similarly. Thus, once all the threads from the ready queue
are processed, then threads from the feedback queue1 are processed next and hence forth. Also, the time slices for all the queues are different.
Ready queue has the time slice of 100 ticks. The time slice for feedback queue1 is 200 ticks, feedback queue2 is 400 ticks. Thus, every time
the thread goes to a level below, its time slice increases.Thus we implement the feedback algorithm.

PERFORMANCE EVALUATION.

The performance we are measuring the throughput and response time vs the thread size.

1. FCFS. 
 FCFS have an excellent performance in small threads, i.e. it gives a very high throughput and quick response time. But as the thread size increases,
 the performance of FCFS decreases. It gives large processor time to a single process hence the de-gradation in the performance. When the thread size is large,
 the response time increases and the throughput decreases.

2. Round Robin.
 Round Robin provides good response time for smaller threads. The throughput is also good, it depends on the time quantum. After we increase the thread size, the output
 is oscilatting. The throughput and response time keeps on increasing and decreasing even when we keep on increasing the thread size.

