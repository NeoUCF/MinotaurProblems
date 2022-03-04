import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

public class MinotaurParty
{
    private static int NUM_GUEST = 10; // Default to 10 guests
	public static List<Guest> guests = new ArrayList<>();

    public static void main(String[] args)
    {
        getUserInput();

        createAndStartGuests();

        final long startTime = System.currentTimeMillis();

        labyrinth();

        final long endTime = System.currentTimeMillis();

        long executionTime = endTime - startTime;
        System.out.println("It took " + Guest.iter + " iterations and " + executionTime + " milliseconds for all "
            + NUM_GUEST + " guest(s) to confirm with certainty they all entered.");
    }

    // If incorrect input (non-integers), then defaults to 10 guests.
    public static void getUserInput()
    {
        Scanner input = new Scanner(System.in);
        System.out.println("Enter Number of Guests");

        try
        {
            NUM_GUEST = input.nextInt();
            System.out.println("Number of Guests: " + NUM_GUEST);
        }
        catch (Exception e)
        {
            System.err.println("Please enter in an integer.");
        }
        finally
        {
            input.close();
            Guest.NUM_GUEST = NUM_GUEST;
        }
    }

    // Prepare Guest Threads for use.
    public static void createAndStartGuests()
    {
        if (NUM_GUEST < 1) return;

        Thread[] guestThreads = new Thread[NUM_GUEST];
        ReentrantLock rel = new ReentrantLock(true); // true makes it fair

        Guest tempHold = new Guest(rel);
        tempHold.isCounter = true;
        guests.add(tempHold);

        // Create the guest that will count.
		guestThreads[0] = new Thread(tempHold, "Counter");

        // Add other guests
        for (int i = 1; i < NUM_GUEST; i++)
        {
            tempHold = new Guest(rel);
            guests.add(tempHold);

            guestThreads[i] = new Thread(tempHold, "Guest - " + i);
        }

		// Begin Labyrinth (start threads)
		for (int i = 0; i < NUM_GUEST; i++)
        {
            guestThreads[i].start();
		}
    }

    // Simulates choosing of guest at random to go into labyrinth.
    public static void labyrinth()
    {
        if (NUM_GUEST <= 1) Guest.everyoneEntered.set(true);

        Random rnd = new Random();

        while (!Guest.everyoneEntered.get())
        {
            int randIndex = rnd.nextInt(NUM_GUEST);
            guests.get(randIndex).setEntered();
        }
    }
}

// Has methods for guests to enter/exit maze, eat/replace cake, and count up guests.
class Guest implements Runnable
{
    public static AtomicBoolean everyoneEntered = new AtomicBoolean();
    public static AtomicBoolean cakeExists = new AtomicBoolean(true);
    public static ReentrantLock reentrantLock;
    public static int count = 0;
    public static int NUM_GUEST;
    public static int iter;

    volatile boolean hasEaten = false;
    volatile boolean inMaze = false;
    boolean isCounter;

    public Guest(ReentrantLock rel)
    {
        reentrantLock = rel;
    }

    public void run()
    {
        while (!everyoneEntered.get())
        {
            if (inMaze)
            {
                reentrantLock.lock();
                try
                {
                    exitMaze();
                }
                finally
                {
                    iter++;
                    reentrantLock.unlock();
                }
            }
        }
    }

    public void exitMaze()
    {
        // System.out.println(Thread.currentThread().getName());
        if (isCounter)
        {
            if (cakeExists.compareAndSet(false, true))
            {
                count++;
                // System.out.println("Counter: " + count);
                // System.out.println("Cake Replenished");
            }

            if (count == NUM_GUEST - 1)
            {
                // System.out.println("Counter: " + (count + 1));

                everyoneEntered.set(true);
            }
        }
        else
        {
            if (!hasEaten && cakeExists.compareAndSet(true, false))
            {
                hasEaten = true;
                // System.out.println("Eating: " + Thread.currentThread().getName());
            }
        }

        inMaze = false;
        // System.out.println(this.myNode.get().locked);
    }

    public void setEntered()
    {
        inMaze = true;
    }
}