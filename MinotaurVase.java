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
            // NUM_GUEST = NUM_GUEST;
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

        ViewingGuest.unleashFlag = true;
    }
}

class ViewingGuest implements Runnable
{
    public static Semaphore sem;
    public static volatile boolean unleashFlag;

    private Random rand = new Random();

    public void run()
    {
        while (true)
        {
            // There is a 1% chance (per loop) any guest may want to view the vase.
            if (unleashFlag && rand.nextDouble() < 0.01)
            {
                // There is 1 available permit
                // Once there are 0, the semaphore waits for
                // the taken permit to be released. The guests waiting
                // for an acquire will be queued in order.
                try
                {
                    sem.acquire();
                    visitVase();
                    sem.release();
                    Thread.sleep((long)rand.nextInt(10000));
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    public void visitVase()
    {
        System.out.println(Thread.currentThread().getName() + " is viewing the vase");
    }
}
