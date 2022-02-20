import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class MinotaurParty
{
    private static final int NUM_GUEST = 8;
	public static List<Guest> guests = new ArrayList<>();;


    public static void main(String[] args)
    {
        System.out.println(NUM_GUEST);

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

        // try {
        //     while (true)
        //     {

        //         System.out.println("1 sec...");
        //         Thread.sleep(1000);
        //         System.out.println("released");
        //         guestSemaphore.release();
        //         // System.out.println(guestThreads[0].isAlive());
        //     }
        // } catch (InterruptedException e) {
        //     e.printStackTrace();
        // }
        // guest threads are still running,
        while (!Guest.everyoneEntered)
        {
            try {
            int randIndex = (int)(NUM_GUEST * Math.random());
            System.out.println("=====================");
            System.out.println(randIndex);
            guests.get(randIndex).takePermit();
            Thread.sleep(10); // Is this bad?
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("IT'S DONE");
    }
}

class Guest implements Runnable
{
    Semaphore guestSemaphore;
    protected static volatile boolean everyoneEntered = false;
    protected static volatile boolean cakeExists = true;
    protected boolean inMaze = false;

    Guest(Semaphore guestSemaphore)
    {
        this.guestSemaphore = guestSemaphore;
    }

    public void takePermit()
    {
        try
        {
            this.guestSemaphore.acquire();
            this.inMaze = true;
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    public void setEntered()
    {
        this.inMaze = true;
    }

    public void setExit()
    {
        this.inMaze = false;
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

            // try
            // {
            //     guestSemaphore.acquire();
            // }
            // catch (InterruptedException e)
            // {
            //     e.printStackTrace();
            // }
            
            // System.out.println("Working in Counter");
            if (inMaze)
            {
                inMaze = false;

                if (!cakeExists)
                {
                    count++;
                    requestCake();

                    if (count == NUM_GUEST - 1)
                    {
                        count++;
                        everyoneEntered = true;
                    }
                }
                System.out.println(count + "badadadada" + Thread.currentThread());
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
        System.out.println("eaten" + cakeExists);
        cakeExists = false;
        this.hasEaten = true;
    } 

    @Override
    public void run()
    {
        while (!everyoneEntered)
        {
            // try
            // {
            //     guestSemaphore.acquire();
            // }
            // catch (InterruptedException e)
            // {
            //     e.printStackTrace();
            // }

            // System.out.println("Working in Guest" + Thread.currentThread());
            if (inMaze)
            {
                inMaze = false;

                if (!hasEaten && cakeExists)
                {
                    eatCake();
                }
                System.out.println("Loving it" + Thread.currentThread());
                this.guestSemaphore.release();
            }
        }
    }
}