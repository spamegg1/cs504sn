# Plurality

## Problem to Solve

Elections come in all shapes and sizes.
In the UK, the Prime Minister is officially appointed by the monarch,
who generally chooses the leader of the political party that wins
the most seats in the House of Commons. The United States uses a
multi-step Electoral College process where citizens vote on how
each state should allocate Electors who then elect the President.

Perhaps the simplest way to hold an election, though, is via a method
commonly known as the “plurality vote” (also known as “first-past-the-post”
or “winner take all”). In the plurality vote, every voter gets to vote
for one candidate. At the end of the election, whichever candidate has
the greatest number of votes is declared the winner of the election.

For this problem, you’ll implement a program that runs a plurality election.

## Demo

```bash
$ ./plurality Alice Bob Charlie
Number of voters: 3
Vote: Alice
Vote: Alice
Vote: Bob
Alice
$ ./plurality Alice Bob Charlie
Number of voters: 3
Vote: David
Invalid vote.
Vote: Alice
Vote: Alice
Alice
$ ./plurality Alice Bob Charlie
Number of voters: 3
Vote: Alice
Vote: Bob
Vote: Charlie
Alice
Bob
Charlie
$ ./plurality
Usage: plurality [candidate ...]
```

## Distribution Code

### Understand the code

Whenever you’ll extend the functionality of existing code,
it’s best to be sure you first understand it in its present state.

Look first at the top of the file. The line #define MAX 9 is some
syntax used here to mean that MAX is a constant (equal to 9) that
can be used throughout the program. Here, it represents the maximum
number of candidates an election can have.

```c
// Max number of candidates
# define MAX 9
```

Notice that plurality then uses this constant to define a global
array—that is, an array that any function can access.

```c
// Array of candidates
candidate candidates[MAX];
```

But what, in this case, is a candidate? In plurality, a candidate is a struct.
Each candidate has two fields: a string called name representing the
candidate’s name, and an int called votes representing the number of
votes the candidate has.

```c
// Candidates have name and vote count
typedef struct
{
    string name;
    int votes;
}
candidate;
```

Now, take a look at the main function itself.
See if you can find where the program sets a global variable
candidate_count representing the number of candidates in the election.

```c
// Number of candidates
int candidate_count;
```

What about where it copies command-line arguments into the array candidates?

```c
// Populate array of candidates
candidate_count = argc - 1;
if (candidate_count > MAX)
{
    printf("Maximum number of candidates is %i\n", MAX);
    return 2;
}
for (int i = 0; i < candidate_count; i++)
{
    candidates[i].name = argv[i + 1];
    candidates[i].votes = 0;
}
```

And where it asks the user to type in the number of voters?

```c
int voter_count = get_int("Number of voters: ");
```

Then, the program lets every voter type in a vote,
calling the vote function on each candidate voted for.
Finally, main makes a call to the print_winner function
to print out the winner (or winners) of the election.
We’ll leave it to you to identify the code that implements this functionality.

If you look further down in the file, though,
you’ll notice that the vote and print_winner functions have been left blank.

```c
// Update vote totals given a new vote
bool vote(string name)
{
    // TODO
    return false;
}

// Print the winner (or winners) of the election
void print_winner(void)
{
    // TODO
    return;
}
```

This part is up to you to complete! You should not modify
anything else in plurality.c other than the implementations
of the vote and print_winner functions (and the inclusion of
additional header files, if you’d like).

## Hints

### Complete the `vote` function

Next, complete the vote function.

- Consider that vote’s signature, `bool vote(string name)`,
  shows it takes a single argument, a string called `name`,
  representing the name of the candidate who was voted for.
- `vote` should return a bool, where true indicates a vote
  was successfully cast and false indicates it was not.

One way to approach this problem is to do the following:

- Iterate over each candidate
  - Check if candidate’s name matches the input, name
    - If yes, increment that candidate’s votes and return true
    - If no, continue checking
- If no matches after checking each candidate, return false

Let’s write some pseudocode to remind you to do just that:

```c
// Update vote totals given a new vote
bool vote(string name)
{
    // Iterate over each candidate
        // Check if candidate's name matches given name
            // If yes, increment candidate's votes and return true

    // If no match, return false
}
```

We’ll leave the implementation in code, though, up to you!

### Complete the `printWinner` function

Finally, complete the `print_winner` function.

- The function should print out the name of the candidate
  who received the most votes in the election, and then print a newline.
- The election could end in a tie if multiple candidates each
  have the maximum number of votes. In that case, you should output
  the names of each of the winning candidates, each on a separate line.

You might think a sorting algorithm would best solve this problem:
imagine sorting candidates by their vote totals and printing the top candidate
(or candidates). Recall, though, that sorting can be expensive:
even Merge Sort, one of the fastest sorting algorithms, runs in `𝑂⁡(𝑁 𝑙⁢𝑜⁢𝑔⁡(𝑁))`.

Consider that you need only two pieces of information to solve this problem:

- The maximum number of votes
- The candidate (or candidates) with that number of votes

As such, a good solution might require only two searches.
Write some pseudocode to remind yourself to do just that:

```c
// Print the winner (or winners) of the election
void print_winner(void)
{
    // Find the maximum number of votes

    // Print the candidate (or candidates) with maximum votes

}
```

We’ll leave the code, though, up to you!
