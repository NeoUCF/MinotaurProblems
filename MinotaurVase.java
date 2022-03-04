import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

public class MinotaurVase
{
    private static int NUM_GUEST = 10; // Default to 10 guests
	public static List<ViewingGuest> guests = new ArrayList<>();

    public static void main(String[] args)
    {
        getUserInput();

        createAndStartGuests();

        final long startTime = System.currentTimeMillis();
        
        vase();

        final long endTime = System.currentTimeMillis();

        try
        {
            Thread.sleep(100);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        long executionTime = endTime - startTime;
        System.out.println("It took " + ViewingGuest.iter + " iterations and " + executionTime + " milliseconds for all "
            + NUM_GUEST + " guest(s) to have viewed the crystal vase.");
    }

    // If incorrect input (non-integers), then defaults to 10 guests.
    public static void getUserInput()
    {
        Scanner input = new Scanner(System.in);
        System.out.println("Enter Number of Guests");

        try
        {
            NUM_GUEST = input.nextInt();
            ViewingGuest.NUM_GUEST = NUM_GUEST;
            System.out.println("Number of Guests: " + NUM_GUEST);
        }
        catch (Exception e)
        {
            System.err.println("Please enter in an integer.");
            System.exit(1);
        }
        finally
        {
            input.close();
        }
    }

    // Prepare ViewingGuest Threads for use.
    public static void createAndStartGuests()
    {
        if (NUM_GUEST < 1) return;

        Thread[] guestThreads = new Thread[NUM_GUEST];

        // Restrict 1 guest to view the vase at a time.
		// The true parameter ensures guests come in fair order when attemping to take a permit.
        ViewingGuest.sem = new Semaphore(1, true);
        // "When fairness is set true, the semaphore guarantees that threads invoking
        // any of the acquire methods are selected to obtain permits in the order in which
        // their invocation of those methods was processed (FIFO)." [Java Semaphore Docs]

        // Add other guests
        for (int i = 0; i < NUM_GUEST; i++)
        {
            ViewingGuest tempHold = new ViewingGuest();
            guests.add(tempHold);

            guestThreads[i] = new Thread(tempHold, "ViewingGuest - " + i);
        }

		// Start threads
		for (int i = 0; i < NUM_GUEST; i++)
        {
            guestThreads[i].start();
		}
    }

    public static void vase()
    {
        ViewingGuest.unleashFlag = true;
        while (ViewingGuest.unleashFlag) {}
    }
}

class ViewingGuest implements Runnable
{
    public static Semaphore sem;
    public static volatile boolean unleashFlag;
    public static int NUM_GUEST;
    public static int count;
    public static int iter;

    private Random rand = new Random();
    private boolean hasEntered;

    public void run()
    {
        while (count < NUM_GUEST)
        {
            if (unleashFlag)
            {
                // There is 1 available permit
                // Once there are 0, the semaphore waits for
                // the taken permit to be released. The guests waiting
                // for an acquire will be queued in order.
                try
                {
                    Thread.sleep((long)rand.nextInt(NUM_GUEST));
                    sem.acquire();
                    visitVase();
                    sem.release();
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }

                iter++;
            }
        }
        unleashFlag = false;
    }

    public void visitVase()
    {
        // System.out.println(Thread.currentThread().getName() + " is viewing the vase");
        if (!hasEntered)
        {
            hasEntered = true;
            count++;
        }
    }
}
