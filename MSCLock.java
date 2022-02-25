import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

class QNode
{
    boolean locked = false;
    QNode next = null;
}

// MSCLock implementation directly from textbook
// [The Art of Multiprocessor Programming, 155]
public class MSCLock implements Lock
{
    AtomicReference<QNode> tail;
    ThreadLocal<QNode> myNode;

    public MSCLock()
    {
        tail = new AtomicReference<QNode>(null);
        myNode = new ThreadLocal<QNode>() {
            protected QNode initialValue()
            {
                return new QNode();
            }
        };
    }

    public void lock()
    {
        QNode qnode = myNode.get();
        // System.out.println("tail: " + tail.get());
        QNode pred = tail.getAndSet(qnode);

        // System.out.println("locked");

        // System.out.println("Qnode: " + qnode);
        // System.out.println("pred: " + pred);
        if (pred != null)
        {
            qnode.locked = true;
            pred.next = qnode;

            // System.out.println("while");

            // wait until predecessor gives up the lock
            while (qnode.locked) {}
            // System.out.println("out");

        }
    }

    public void unlock()
    {
        QNode qnode = myNode.get();

        // System.out.println("Attempt unlock");

        if (qnode.next == null)
        {
            if (tail.compareAndSet(qnode, null)) return;
            
            // wait until successor fills in the next field
            while (qnode.next == null) {}
            // System.out.println("in");
        }

        qnode.next.locked = false;
        qnode.next = null;
        // System.out.println("unlocked");
    }

    public void lockInterruptibly() throws InterruptedException
    {
    }

    public boolean tryLock()
    {
        return false;
    }

    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException
    {
        return false;
    }

    public Condition newCondition()
    {
        return null;
    }
}