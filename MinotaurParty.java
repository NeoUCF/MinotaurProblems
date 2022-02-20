import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class MinotaurParty
{
    private static int NUM_GUEST = 10; // Default to 10 guests
    private static int iter = 1; // Number of iterations
	public static List<Guest> guests = new ArrayList<>();

    public static void main(String[] args)
    {
        // Read user input.
        // If incorrect input (non-integers), then defaults to 10 guests.
        getUserInput();

        createAndStartGuests();

        final long startTime = System.currentTimeMillis();

        labyrinth();

        final long endTime = System.currentTimeMillis();

        long executionTime = endTime - startTime;
        System.out.println("It took " + iter + " iterations and " + executionTime + " milliseconds for all "
            + NUM_GUEST + " guest(s) to confirm with certainty they all entered.");
    }

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
        }
    }

    public static void createAndStartGuests()
    {
        if (NUM_GUEST < 1) return;

        Thread[] guestThreads = new Thread[NUM_GUEST];

        // Restricts 1 guest to be in labyrinth at a time.
		// The true parameter ensures guests come in fair order.
    	Semaphore guestSemaphore = new Semaphore(1, true);

        Guest tempHold = new CounterGuestThread(guestSemaphore, NUM_GUEST);
        guests.add(tempHold);

        // Create the guest that will count.
		guestThreads[0] = new Thread(tempHold);

        // Add other guests
        for (int i = 1; i < NUM_GUEST; i++)
        {
            tempHold = new GuestThread(guestSemaphore);
            guests.add(tempHold);

            guestThreads[i] = new Thread(tempHold);
        }

		// Begin Labyrinth (start threads)
		for (int i = 0; i < NUM_GUEST; i++)
        {
            guestThreads[i].start();
		}
    }

    public static void labyrinth()
    {
        if (NUM_GUEST <= 1) Guest.everyoneEntered = true;
        
        Random rnd = new Random();

        while (!Guest.everyoneEntered)
        {
            int randIndex = rnd.nextInt(NUM_GUEST);
            guests.get(randIndex).setEntered();
            // System.out.println(randIndex);

            // try
            // {
            //     // System.out.println("=====================");
            //     // System.out.println(randIndex);
            //     Thread.sleep(5); // Is this bad?
            // }
            // catch (InterruptedException e)
            // {
            //     e.printStackTrace();
            // }
            iter++;
        }
    }
}

class Qnode
{
    boolean locked = false;
    Qnode next = null;
}

class MSCLock implements Lock
{
    AtomicReference<Qnode> tail = new AtomicReference<Qnode>();
    Qnode qnode = new Qnode();

    public void lock()
    {
        Qnode pred = tail.getAndSet(qnode);

        if (pred != null)
        {
            qnode.locked = true;
            pred.next = qnode;

            while (qnode.locked) { ; }
        }
    }

    public void unlock()
    {
        if (qnode.next == null)
        {
            if (tail.compareAndSet(qnode, null))
            {
                return;
            }

            while (qnode.next == null) { ; }
        }

        qnode.next.locked = false;
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean tryLock() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Condition newCondition() {
        // TODO Auto-generated method stub
        return null;
    }
}

class Guest implements Runnable
{
    Semaphore guestSemaphore;
    public static volatile boolean everyoneEntered = false;
    protected static volatile boolean cakeExists = true;
    protected static AtomicBoolean inMaze = new AtomicBoolean();

    Guest(Semaphore guestSemaphore)
    {
        this.guestSemaphore = guestSemaphore;
    }

    public void takePermit()
    {
        try
        {
            this.guestSemaphore.acquire();
            inMaze.set(true);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    public void setEntered()
    {
        inMaze.compareAndSet(false, true);
    }

    public void run()
    {
        
    }
}

class CounterGuestThread extends Guest
{
    private int count = 0;
    private int NUM_GUEST;

    CounterGuestThread(Semaphore guestSemaphore, int NUM_GUEST)
    {
        super(guestSemaphore);
        this.NUM_GUEST = NUM_GUEST;
    }

    void requestCake()
    {
        System.out.println("Cake Replenished");
        cakeExists = true;
    }

    @Override
    public void run()
    {
        while (!everyoneEntered)
        {
            if (!inMaze.compareAndSet(true, false))
            {
                if (!cakeExists)
                {
                    count++;
                    requestCake();

                    if (count == NUM_GUEST - 1)
                    {
                        System.out.println("Counter: " + count);
                        count++;
                        everyoneEntered = true;
                    }

                    System.out.println("Counter: " + count);
                }

                this.guestSemaphore.release();
            }
        }
    }
}

class GuestThread extends Guest
{
    private boolean hasEaten = false;
    GuestThread(Semaphore guestSemaphore)
    {
        super(guestSemaphore);
    }

    void eatCake()
    {
        // System.out.println("eaten" + cakeExists);
        cakeExists = false;
        this.hasEaten = true;
    } 

    @Override
    public void run()
    {
        while (!everyoneEntered)
        {
            if (!inMaze.compareAndSet(true, false))
            {
                if (!hasEaten && cakeExists)
                {
                    eatCake();
                }
                System.out.println("Normal Guest#" + Thread.currentThread().getId());
                this.guestSemaphore.release();
            }
        }
    }
}