import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class Main {

    public static void main( String[] args ) {

        System.out.println( "scheduling ur day!!" );
        taskHelper();
    }

    public static final int TOTAL_TASK_NUMBER = 12;

    public static void taskHelper() {

        int taskRemovedNumber = 0;

        // creating task objects
        Task task1  = createTask1();
        Task task2  = createTask2();
        Task task3  = createTask3();
        Task task4  = createTask4();
        Task task5  = createTask5();
        Task task6  = createTask6();
        Task task7  = createTask7();
        Task task8  = createTask8();
        Task task9  = createTask9();
        Task task10 = createTask10();
        Task task11 = createTask11();
        Task task12 = createTask12();

        List<Task> taskList = new ArrayList<>();
        taskList.add( task1 );
        taskList.add( task2 );
        taskList.add( task3 );
        taskList.add( task4 );
        taskList.add( task5 );
        taskList.add( task6 );
        taskList.add( task7 );
        taskList.add( task8 );
        taskList.add( task9 );
        taskList.add( task10 );
        taskList.add( task11 );
        taskList.add( task12 );

        taskList = sortByStartTime( taskList );

        List<Task> taskListWithStartTime = clearStartTime( taskList );

        List<Task> taskListWithoutTime = getNoStartTime( taskList );

        List<Task> taskListWithoutTimeSortByDuration = sortDuration( taskListWithoutTime );

        // now we need to divide these tasks into two list
        // which means we divide them into 2 task threads.

        // Get first two start tasks with start time
        Task firstTask = taskListWithStartTime.get( 0 );
        Task secondTask = taskListWithStartTime.get( 1 );

        Task lastTask = taskListWithStartTime.get( taskListWithStartTime.size() - 1 );

        // calculate the 1st and the 2nd task time gap,
        // and check if there is another task without start time but its duration is less than the gap;
        // if so, then we pick the first one which will be the 2nd thread task starter

        int timeGap = secondTask.getStartTime() - firstTask.getStartTime();

        // get the latest time
        int latestStartTime = lastTask.getStartTime();
        int latestEndTime = lastTask.getStartTime() + lastTask.getDuration();

        List<Task> fitTimeGapTask = filterTimeGapHelper( taskListWithoutTimeSortByDuration, timeGap );

        if ( fitTimeGapTask.size() != 0 ) {
            secondTask = fitTimeGapTask.get( 0 );
        }

        // now, we get two threads' starters

        // create two thread lists
        List<Task> thread1 = new ArrayList<>();
        List<Task> thread2 = new ArrayList<>();

        // check the compatibility
        boolean compChecker = checkCompHelper( firstTask, secondTask );

        if ( compChecker ) {
            thread1.add( firstTask );
            thread2.add( secondTask );
        } else {
            return;
        }

        removeElement( taskList, firstTask );
        removeCompInCompList( firstTask.getId(), taskList );
        taskRemovedNumber ++;
        removeElement( taskList, secondTask );
        removeCompInCompList( secondTask.getId(), taskList );
        taskRemovedNumber ++;
        removeElement( taskList, lastTask );
        removeCompInCompList( lastTask.getId(), taskList );
        taskRemovedNumber ++;

        taskList = sortDurationFromMost( taskList );

        List<Integer> endTimeListThread1 = new ArrayList<>();
        int firstEndTimeThread1 = thread1.get( 0 ).startTime + thread1.get( 0 ).getDuration();

        endTimeListThread1.add( firstEndTimeThread1 );

        // thread 1 builder
        for ( int i = 0; i < TOTAL_TASK_NUMBER - taskRemovedNumber; i ++ ) {
            int startTime = thread1.get( thread1.size() - 1 ).getStartTime() + thread1.get( thread1.size() - 1 ).getDuration();
            int endTime = startTime + taskList.get( i ).getDuration();
            if ( endTime < latestStartTime ) {
                taskList.get( i ).setStartTime( startTime );
                thread1.add( taskList.get( i ) );
                endTimeListThread1.add( endTime );
                removeCompInCompList( taskList.get( i ).getId(), taskList );
            } else {
                i ++;
            }
        }

        // remove the task which has been put in thread1 from taskList
        for ( Task task : thread1 ) {
            removeElement( taskList, task );
            taskRemovedNumber ++;
        }

        // thread 2 builder
        for ( int i = 0, j = 0; i < taskList.size(); i ++) {
            int lastEndTime = thread2.get( thread2.size() - 1 ).getStartTime() + thread2.get( thread2.size() - 1 ).getDuration();

            if ( lastEndTime < latestStartTime  ) {
                int thread1TaskEndTime = endTimeListThread1.get( j ).intValue();
                if ( lastEndTime < thread1TaskEndTime ) {
                    List<Integer> commonComp = findCompNumber( thread1.get( j ).getComp(), taskList.get( i ).getComp() );
                    List<Integer> thread1Comp = thread1.get( j ).getComp();
                    thread2Builder( taskList, thread1, thread2, i, lastEndTime, thread1TaskEndTime, commonComp, thread1Comp );
                } else if ( lastEndTime == thread1TaskEndTime ) {
                    j ++;
                    for ( Task task : taskList ) {
                        if ( thread1.get( j ).getComp().contains( task.getId() ) ) {
                            task.setStartTime( lastEndTime );
                            thread2.add( task );
                            removeWinner ( task.getId(), taskList );
                            removeCompInCompList( task.getId(), taskList );
                            removeCompInCompList( task.getId(), thread1 );
                            break;
                        }
                    }
                } else {
                    j ++;
                    List<Integer> commonComp = findCompNumber( thread1.get( j ).getComp(), taskList.get( i ).getComp() );
                    List<Integer> thread1Comp = thread1.get( j ).getComp();
                    thread2Builder( taskList, thread1, thread2, i, lastEndTime, thread1TaskEndTime, commonComp, thread1Comp );
                }
            } else {
                break;
            }
        }

        // end the thread2
        int thread2LastEndTime = thread2.get( thread2.size() - 1 ).getStartTime() + thread2.get( thread2.size() - 1 ).getDuration();

        int thread2TimeGap = latestStartTime - thread2LastEndTime;

        for ( Task task : taskList ) {
            if ( task.getDuration() <= thread2TimeGap && thread1.get( thread1.size() - 1 ).getComp().contains( task.getId() ) ) {
                task.setStartTime( thread2LastEndTime );
                thread2.add( task );
                removeWinner ( task.getId(), taskList );
                removeCompInCompList( task.getId(), taskList );
                removeCompInCompList( task.getId(), thread1 );
                break;
            }
        }

        // append at the end of thread1
        thread1.add( lastTask );

        for ( Task task : taskList ) {
            task.setStartTime( latestEndTime );
            thread1.add( task );
            latestEndTime = thread1.get( thread1.size() - 1 ).getStartTime() + thread1.get( thread1.size() - 1 ).getDuration();

        }

        // merge two thread lists into one list in order to print out
        List<List<Task>> lists = new ArrayList<>();
        lists.add( thread1 );
        lists.add( thread2 );

        List<Task> res = new ArrayList<>();
        int totalSize = 12;
        boolean first;
        List<Task> lowest = lists.iterator().next();

        while ( res.size() < totalSize ) {
            first = true;
            for (List<Task> l : lists) {
                if (! l.isEmpty()) {
                    if (first) {
                        lowest = l;
                        first = false;
                    }
                    else if ( l.get( 0 ).getStartTime().compareTo( lowest.get( 0 ).getStartTime() ) <= 0 ) {
                        lowest = l;
                    }
                }
            }
            res.add(lowest.get(0));
            lowest.remove(0);
        }

        System.out.println();

        for ( Task task : res ) {
//for some reason, cant not express double 0 after performing %
            System.out.println( task.getDescription()
                                        + ", "
                                        + task.getStartTime() / 60
                                        + ":"
                                        + task.getStartTime() % 60
                                        + "-"
                                        + ( task.getStartTime() + task.getDuration() ) / 60
                                        + ":"
                                        + ( task.getStartTime() + task.getDuration() ) % 60 );

        }
    }

    private static void thread2Builder( List<Task> taskList, List<Task> thread1 ,List<Task> thread2, int i, int lastEndTime, int thread1TaskEndTime, List<Integer> commonComp, List<Integer> thread1Comp ) {
        if ( commonComp.isEmpty() ) {
            int timeGap = thread1TaskEndTime - lastEndTime;
            for ( Task task : taskList ) {
                if ( task.getDuration() < timeGap ) {
                    task.setStartTime( lastEndTime );
                    if ( taskList.contains( task ) ) {
                        thread2.add( task );
                        removeCompInCompList( task.getId(), taskList );
                        removeCompInCompList( task.getId(), thread1 );
                        removeWinner ( task.getId(), taskList );
                        break;
                    } else {
                        continue;
                    }

                } else {
                    for ( Task winner : taskList ) {
                        if ( thread1Comp.contains( winner.getId() ) ) {
                            winner.setStartTime( lastEndTime );
                            if ( taskList.contains( winner ) ) {
                                thread2.add( winner );
                                removeCompInCompList( winner.getId(), taskList );
                                removeCompInCompList( winner.getId(), thread1 );
                                removeWinner ( winner.getId(), taskList );
                                break;
                            } else {
                                continue;
                            }

                        }
                        lastEndTime = thread1TaskEndTime;
                        taskList.get( i ).setStartTime( lastEndTime );
                        thread2.add( taskList.get( i ) );
                        removeCompInCompList( taskList.get( i ) .getId(), taskList );
                        removeCompInCompList( taskList.get( i ) .getId(), thread1 );
                        removeWinner ( taskList.get( i ) .getId(), taskList );
                        break;
                    }
                    break;
                }
            }
        } else {
            List<Task> tempList = getTheTaskFromList( commonComp, taskList );
            tempList = sortDurationFromMost( tempList );
            Task winner = findElementInTaskList( tempList.get( 0 ).getId(), taskList );
            removeWinner ( winner.getId(), taskList );
            removeCompInCompList( winner.getId(), taskList );
            removeCompInCompList( winner.getId(), thread1 );
            winner.setStartTime( lastEndTime );
            thread2.add( winner );
        }
    }

    private static void removeCompInCompList ( int id, List<Task> tasks ) {
        for ( Task task : tasks ) {
            List<Integer> temp = new ArrayList<>();
            for ( Integer comp : task.getComp() ) {
                if ( comp != id ) {
                    temp.add( comp );
                }
            }
            task.setComp( temp );
        }
    }

    private static void removeWinner ( int id, List<Task> tasks ) {
        tasks.removeIf( e -> e.getId() == id );
    }

    private static Task findElementInTaskList( int id, List<Task> tasks ) {
        return tasks.stream().filter( e -> id == e.getId() ).findAny().orElse( null );
    }

    private static List<Task> getTheTaskFromList( List<Integer> comps, List<Task> tasks ) {
        List<Task> res = new ArrayList<>();

        for ( Integer comp : comps ) {
            for ( Task task : tasks ) {
                if ( task.getId() == comp.intValue()  ) {
                    res.add( task );
                }
            }
        }

        return res;
    }

    private static List<Integer> findCompNumber( List<Integer> comp1, List<Integer> comp2 ) {
        return comp1.stream().filter( comp2::contains ).collect( toList() );
    }

    private static void addTaskIntoThread(List<Task> taskList, List<Task> thread, List<Task> theOtherThread, Task winner, Task loser) {
        if ( theOtherThread.get( theOtherThread.size() - 1 ).getComp().contains( winner.getId() ) ) {
            winner.setStartTime( thread.get( thread.size() - 1 ).getStartTime() + thread.get( thread.size() - 1 ).getDuration() );
            thread.add( winner );
            taskList.removeIf( s -> s.getId() == winner.getId() );
            for ( Task task : taskList ) {
                task.getComp().removeIf( s -> s.equals( winner.getId() ) );
            }
        } else if ( theOtherThread.get( theOtherThread.size() - 1 ).getComp().contains( loser.getId() ) ) {
            loser.setStartTime( thread.get( thread.size() - 1 ).getStartTime() + thread.get( thread.size() - 1 ).getDuration() );
            thread.add( loser );
            taskList.removeIf( s -> s.getId() == loser.getId() );
            for ( Task task : taskList ) {
                task.getComp().removeIf( s -> s.equals( loser.getId() ) );
            }
        } else if ( thread.get( thread.size() - 1 ).getComp().contains( winner.getId() ) ) {
            winner.setStartTime( theOtherThread.get( theOtherThread.size() - 1 ).getStartTime() + theOtherThread.get( theOtherThread.size() - 1 ).getDuration() );
            theOtherThread.add( winner );
            taskList.removeIf( s -> s.getId() == winner.getId() );
            for ( Task task : taskList ) {
                task.getComp().removeIf( s -> s.equals( winner.getId() ) );
            }
        } else if ( thread.get( thread.size() - 1 ).getComp().contains( loser.getId() ) ) {
            loser.setStartTime( theOtherThread.get( theOtherThread.size() - 1 ).getStartTime() + theOtherThread.get( theOtherThread.size() - 1 ).getDuration() );
            theOtherThread.add( loser );
            taskList.removeIf( s -> s.getId() == loser.getId() );
            for ( Task task : taskList ) {
                task.getComp().removeIf( s -> s.equals( loser.getId() ) );
            }
        } else {
            winner.setStartTime( thread.get( thread.size() - 1 ).getStartTime() + thread.get( thread.size() - 1 ).getDuration() );
            thread.add( winner );
            taskList.removeIf( s -> s.getId() == winner.getId() );
        }
    }

    private static List<Task> sortCamp ( List<Task> tasks ) {
        Collections.sort( tasks, (o1, o2) -> {
            if ( o1.getComp().size() < o2.getComp().size() ) return 1;
            if ( o1.getComp().size() > o2.getComp().size() ) return -1;
            return 0;
        });
        return tasks;
    }

    private static List<Task> sortDurationFromMost ( List<Task> tasks ) {
        Collections.sort( tasks, (o1, o2) -> {
            if ( o1.getDuration() < o2.getDuration() ) return 1;
            if ( o1.getDuration() > o2.getDuration() ) return -1;
            return 0;
        });
        return tasks;
    }

    private static List<Task> sortDuration ( List<Task> tasks ) {
        Collections.sort( tasks, (o1, o2) -> {
            if ( o1.getDuration() > o2.getDuration() ) return 1;
            if ( o1.getDuration() < o2.getDuration() ) return -1;
            return 0;
        });
        return tasks;
    }

    private static void removeElement ( List<Task> tasks, Task task ) {
        tasks.removeIf( e -> e.getId() == task.getId() );
    }

    private static boolean checkCompHelper ( Task task1, Task task2 ) {
        int endTimeTask2 = task2.getStartTime() + task2.getDuration();

        // does these two tasks in the same time range?
        if ( endTimeTask2 > task1.getStartTime() ) {
            return task1.getComp().contains( task2.getId() ) && task2.getComp().contains( task1.getId() );
        }

        return true;
    }

    private static List<Task> sortByStartTime( List<Task> tasks ) {
        Collections.sort( tasks, (o1, o2) -> {
            if ( o1.getStartTime() > o2.getStartTime() ) return 1;
            if ( o1.getStartTime() < o2.getStartTime() ) return -1;
            return 0;
        });
        return tasks;
    }

    private static List<Task> getNoStartTime( List<Task> tasks ) {
        return tasks.stream().filter( e -> e.startTime == 0 ).collect( toList() );
    }

    private static List<Task> clearStartTime( List<Task> tasks ) {
        return tasks.stream().filter( e -> e.startTime != 0 ).collect( toList() );
    }

    private static List<Task> filterTimeGapHelper( List<Task> tasks, int gap ) {
        return tasks.stream().filter( e -> e.duration < gap ).collect( toList() );
    }

    // helper methods for creating the tasks
    private static Task createTask1() {
        List<Integer> compList = new ArrayList<>();
        compList.add( 2 );
        compList.add( 5 );
        compList.add( 7 );
        compList.add( 3 );

        Task task = new Task ();
        task.setDescription( "Eat Breakfast" );
        task.setId( 1 );
        // 7:00 -> 7 * 60 = 420;
        task.setStartTime( 420 );
        task.setDuration( 30 );
        task.setComp( compList );

        return task;
    }

    private static Task createTask2 () {
        List<Integer> compList = new ArrayList<>();
        compList.add( 1 );
        compList.add( 7 );
        compList.add( 9 );

        Task task = new Task();
        task.setDescription( "Do the dishes" );
        task.setId( 2 );
        task.setDuration( 20 );
        task.setComp( compList );

        return task;
    }

    private static Task createTask3 () {
        List<Integer> compList = new ArrayList<>();
        compList.add( 11 );
        compList.add( 1 );

        Task task = new Task();
        task.setDescription( "Watch the sunrise" );
        task.setId( 3 );
        // 7:30 -> 7 * 60 + 30 = 430;
        task.setStartTime( 430 );
        task.setDuration( 15 );
        task.setComp( compList );

        return task;
    }

    private static Task createTask4 () {
        List<Integer> compList = new ArrayList<>();
        compList.add( 5 );
        compList.add( 7 );
        compList.add( 11 );

        Task task = new Task();
        task.setDescription( "Hit the Treadmill" );
        task.setId( 4 );
        task.setDuration( 60 );
        task.setComp( compList );

        return task;
    }

    private static Task createTask5() {
        List<Integer> compList = new ArrayList<>();
        compList.add( 1 );
        compList.add( 4 );
        compList.add( 9 );

        Task task = new Task();
        task.setDescription( "Catch up on all the things" );
        task.setId( 5 );
        task.setDuration( 25 );
        task.setComp( compList );

        return task;
    }

    private static Task createTask6() {
        List<Integer> compList = new ArrayList<>();
        compList.add( 7 );
        compList.add( 9 );

        Task task = new Task();
        task.setDescription( "Buy groceries for the week" );
        task.setId( 6 );
        task.setDuration( 75 );
        task.setComp( compList );

        return task;
    }

    private static Task createTask7() {
        List<Integer> compList = new ArrayList<>();
        compList.add( 1 );
        compList.add( 2 );
        compList.add( 4 );
        compList.add( 6 );
        compList.add( 9 );

        Task task = new Task();
        task.setDescription( "Catch up with your family at home" );
        task.setId( 7 );
        task.setDuration( 45 );
        task.setComp( compList );

        return task;
    }

    private static Task createTask8() {
        List<Integer> compList = new ArrayList<>();
        compList.add( 9 );
        compList.add( 11 );

        Task task = new Task();
        task.setDescription( "Vacuum the house" );
        task.setId( 8 );
        task.setDuration( 35 );
        task.setComp( compList );

        return task;
    }

    private static Task createTask9() {
        List<Integer> compList = new ArrayList<>();
        compList.add( 2 );
        compList.add( 5 );
        compList.add( 6 );
        compList.add( 7 );
        compList.add( 8 );
        compList.add( 10 );
        compList.add( 11 );

        Task task = new Task();
        task.setDescription( "Enjoy a snack" );
        task.setId( 9 );
        task.setDuration( 15 );
        task.setComp( compList );

        return task;
    }

    private static Task createTask10() {
        List<Integer> compList = new ArrayList<>();
        compList.add( 9 );
        compList.add( 11 );

        Task task = new Task();
        task.setDescription( "Mow the Lawn" );
        task.setId( 10 );
        task.setDuration( 75 );
        task.setComp( compList );

        return task;
    }

    private static Task createTask11() {
        List<Integer> compList = new ArrayList<>();
        compList.add( 3 );
        compList.add( 4 );
        compList.add( 8 );
        compList.add( 9 );
        compList.add( 10 );

        Task task = new Task();
        task.setDescription( "Listen to Skeptics Guide to the Universe" );
        task.setId( 11 );
        task.setDuration( 90 );
        task.setComp( compList );

        return task;
    }

    private static Task createTask12() {
        List<Integer> compList = new ArrayList<>();

        Task task = new Task();
        task.setDescription( "Midmorning break" );
        task.setId( 12 );
        // 10:45 -> 10 * 60 + 45 = 645;
        task.setStartTime( 645 );
        task.setDuration( 20 );
        task.setComp( compList );

        return task;
    }
}
//better to import a file 