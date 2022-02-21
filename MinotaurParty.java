import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class MinotaurParty
{
    private static int NUM_GUEST = 10; // Default to 10 guests
    private static int iter = 1; // Number of iterations
	public static List<Guest> guests = new ArrayList<>();

    public static void main(String[] args)
    {
        getUserInput();

        createAndStartGuests();

        final long startTime = System.currentTimeMillis();

        labyrinth();

        final long endTime = System.currentTimeMillis();

        long executionTime = endTime - startTime;
        System.out.println("It took " + iter + " iterations and " + executionTime + " milliseconds for all "
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

        Guest tempHold = new Guest();
        guests.add(tempHold);

        // Create the guest that will count.
		guestThreads[0] = new Thread(tempHold, "Counter");

        // Add other guests
        for (int i = 1; i < NUM_GUEST; i++)
        {
            tempHold = new Guest();
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
            if (Guest.mazeOccupied.compareAndSet(false, true))
            {
                // System.out.println("Entering: Guest - " + randIndex);
                guests.get(randIndex).setEntered();
                
                iter++;
            }
        }
    }
}

// Has methods for guests to enter/exit maze, eat/replace cake, and count up guests.
class Guest implements Runnable
{
    public static AtomicBoolean everyoneEntered = new AtomicBoolean();
    public static AtomicBoolean mazeOccupied = new AtomicBoolean();
    public static AtomicBoolean cakeExists = new AtomicBoolean(true);
    public static int count = 0;
    public static int NUM_GUEST;

    volatile boolean hasEaten = false;
    volatile boolean inMaze = false;

    public void run()
    {
        while (!everyoneEntered.get())
        {
            if (inMaze)
            {
                exitMaze();
            }
        }
    }

    public void exitMaze()
    {
        if (Thread.currentThread().getName().equals("Counter"))
        {
            if (cakeExists.compareAndSet(false, true))
            {
                count++;
                System.out.println("Counter: " + count);
                System.out.println("Cake Replenished");
            }

            if (count == NUM_GUEST - 1)
            {
                System.out.println("Counter: " + (count + 1));

                everyoneEntered.set(true);
            }
        }
        else
        {
            if (!hasEaten && cakeExists.compareAndSet(true, false))
            {
                hasEaten = true;
                System.out.println("Eating: " + Thread.currentThread().getName());
            }
        }

        inMaze = false;
        mazeOccupied.set(false);
    }

    public void setEntered()
    {
        inMaze = true;
    }
}