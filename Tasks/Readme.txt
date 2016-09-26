****************README***********************

#####Design of the TaskCB. java class######

# There are three lists in TaskCB class to keep track of list of threads in the task, list ports and list of files opened in the task.

#GenereicList data structure in OSP project was used to implement these lists. This data structure was used mainly because it is a synchronized list. Data structures like ArrayLists cannot be used in this scenario, since there can be multiple threads modifying these lists at the same time.

#Initialization of all the parameters of TaskCB was done inside do_create() method.

#In do_kill method, we iterate through all the elements in each list(thread, ports and file) and killed/removed them from the list. 



###Group members and responsibilities#####
Isira Samarasekera: Coding, Writing 
Pivithuru Wijegunawradana: Designing, Coding
Tanmay Fadnavis: Designing, Coding