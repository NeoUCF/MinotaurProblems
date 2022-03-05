# MinotaurProblems
## How to Run the Program:
On the command prompt, go to the directory which contains the .java files, then run:
- `java MinotaurParty.java` to run the solution to Problem 1.
- `java MinotaurVase.java` to run the solution to Problem 2.

Both programs will ask for an input of the number of guests.

## Problem 1:
### Correctness, Efficiency, and Evaluation:
In `MinotaurParty.java`, the guest(s) can confirm with certainty they all entered by assigning a counter guest to count empty plates and to request a new cupcake. The other guests will eat the cupcake if it's their first time and will leave a cupcake if they've already eaten. Once the counter guest counts n - 1 empty plates (n being the total number of guests known at the beginning), that means everyone had their turn at eating the cupcake, which means everyone entered the labyrinth.

For n guests, the expected runtime is O(n^2) since the exit condition is dependant on the counter guest counting n guest, the expected probability of the counter guest finding an empty plate is 1/n. This means after n passes through the labyrinth, the average/expected amount the counter will find empty plates is 1. So to find n guests, the counter must go through an average of n passes through maze for each guest. This means n * n, so the runtime is indeed O(n^2).

For Experimental evaluation, I tested between 3 different locking mechanism: MSCLock, AndersonLock, and ReentrantLock. The MSCLock and AndersonLock are my own implementation so it may not be the most optimal for this given problem. Running the MSCLock on 100 guests would take an average of 72000 milliseconds. Running the AndersonLock on 100 guests would take an average of 68000 milliseconds. Running the ReentrantLock would take an average of 3200 milliseconds. All the experimental tests take place on a Lenovo IdeaPad Flex 5 which has a series 4000 Ryzen 7 (8 core CPU).

## Problem 2:
### Strategy Discussion:
For the Crystal Vase problem, the 1st strategy of stop by and check is not very good since a guest may be 'starved' from actually seeing the vase. Although this strategy is simple to implement and it does ensure mutual exclusion, there may also be a lot of contention between the guests if there is a large crowd fight over entry to the vase room. There is also no ordering as to when the guests can enter, so it's possible a single guest can hog up the viewing of the vase.

The 2nd strategy is better than the first strategy as it significantly reduces contention by using a visible flag to all guests interesting in entering the vase room. However, this strategy still faces the issue of a guest being 'starved' from the vase.

The 3rd strategy is the best strategy since it uses a queue. Using a queue reduces contention, it brings ordering and fairness to the guests, and it ensure there is no 'starvation' by allowing each guest to have a chance to see the vase.

### Correctness, Efficiency, and Evaluation:
In `MinotaurVase.java`, I chose the 3rd strategy of using a queue. The main library that I used to implement this queue was by using the Semaphore Class's built in queue to keep track of the ordering an fairness of the guests. By allowing only 1 permit, the Semaphore locks the other attempted acquiring guests and places them in a queue. To help randomize the acquiring of a permit, I made the guest thread sleep a random amount of time prior to an acquire.

The Efficiency of the program is expected to be O(n) time since the program ends when all n guests have viewed the vase. When a guest is ready to acquire a permit, the guest will then be placed in a queue waiting for their turn. Emperically, it was also viewed from the number of iterations that the solution ran around 2n iterations before ending, so the runtime is O(n).

After running many tests, it was found that:
- The average time for 10 guests was 17 milliseconds.
- The average time for 100 guests was 130 milliseconds.
- The average time for 1000 guests was 1200 milliseconds.

## Problem 1: Minotaur’s Birthday Party (50 points)

The Minotaur invited N guests to his birthday party. When the guests arrived, he made the following announcement.

The guests may enter his labyrinth, one at a time and only when he invites them to do so. At the end of the labyrinth, the Minotaur placed a birthday cupcake on a plate. When a guest finds a way out of the labyrinth, he or she may decide to eat the birthday cupcake or leave it. If the cupcake is eaten by the previous guest, the next guest will find the cupcake plate empty and may request another cupcake by asking the Minotaur’s servants. When the servants bring a new cupcake the guest may decide to eat it or leave it on the plate.

The Minotaur’s only request for each guest is to not talk to the other guests about her or his visit to the labyrinth after the game has started. The guests are allowed to come up with a strategy prior to the beginning of the game. There are many birthday cupcakes, so the Minotaur may pick the same guests multiple times and ask them to enter the labyrinth. Before the party is over, the Minotaur wants to know if all of his guests have had the chance to enter his labyrinth. To do so, the guests must announce that they have all visited the labyrinth at least once.

Now the guests must come up with a strategy to let the Minotaur know that every guest entered the Minotaur’s labyrinth. It is known that there is already a birthday cupcake left at the labyrinth’s exit at the start of the game. How would the guests do this and not disappoint his generous and a bit temperamental host?

Create a program to simulate the winning strategy (protocol) where each guest is represented by one running thread. In your program you can choose a concrete number for N or ask the user to specify N at the start.

 

## Problem 2: Minotaur’s Crystal Vase (50 points)

The Minotaur decided to show his favorite crystal vase to his guests in a dedicated showroom with a single door. He did not want many guests to gather around the vase and accidentally break it. For this reason, he would allow only one guest at a time into the showroom. He asked his guests to choose from one of three possible strategies for viewing the Minotaur’s favorite crystal vase:

1) Any guest could stop by and check whether the showroom’s door is open at any time and try to enter the room. While this would allow the guests to roam around the castle and enjoy the party, this strategy may also cause large crowds of eager guests to gather around the door. A particular guest wanting to see the vase would also have no guarantee that she or he will be able to do so and when.

2) The Minotaur’s second strategy allowed the guests to place a sign on the door indicating when the showroom is available. The sign would read “AVAILABLE” or “BUSY.” Every guest is responsible to set the sign to “BUSY” when entering the showroom and back to “AVAILABLE” upon exit. That way guests would not bother trying to go to the showroom if it is not available.

3) The third strategy would allow the guests to line in a queue. Every guest exiting the room was responsible to notify the guest standing in front of the queue that the showroom is available. Guests were allowed to queue multiple times.

Which of these three strategies should the guests choose? Please discuss the advantages and disadvantages.

Implement the strategy/protocol of your choice where each guest is represented by 1 running thread. You can choose a concrete number for the number of guests or ask the user to specify it at the start.
