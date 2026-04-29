import scalanative.unsafe.*
import munit.FunSuite

class RunoffTest extends FunSuite:
  test("full integration test for a complex runoff election"):
    import Runoff.*

    val voterCt = 9
    val candCt  = 3

    val alice: Cand = stackalloc[Cand](1)
    alice._1 = c"Alice" // name
    alice._2 = 0        // votes
    alice._3 = false    // eliminated?

    val bob: Cand = stackalloc[Cand](1)
    bob._1 = c"Bob" // name
    bob._2 = 0      // votes
    bob._3 = false  // eliminated?

    val charlie: Cand = stackalloc[Cand](1)
    charlie._1 = c"Charlie" // name
    charlie._2 = 0          // votes
    charlie._3 = false      // eliminated?

    cands(0) = alice
    cands(1) = bob
    cands(2) = charlie

    val v00 = vote(0, 0, c"Alice", candCt)
    val v01 = vote(0, 1, c"Bob", candCt)
    val v02 = vote(0, 2, c"Charlie", candCt)

    val v10 = vote(1, 0, c"Alice", candCt)
    val v11 = vote(1, 1, c"Bob", candCt)
    val v12 = vote(1, 2, c"Charlie", candCt)

    val v20 = vote(2, 0, c"Bob", candCt)
    val v21 = vote(2, 1, c"Alice", candCt)
    val v22 = vote(2, 2, c"Charlie", candCt)

    val v30 = vote(3, 0, c"Bob", candCt)
    val v31 = vote(3, 1, c"Alice", candCt)
    val v32 = vote(3, 2, c"Charlie", candCt)

    val v40 = vote(4, 0, c"Bob", candCt)
    val v41 = vote(4, 1, c"Alice", candCt)
    val v42 = vote(4, 2, c"Charlie", candCt)

    val v50 = vote(5, 0, c"Charlie", candCt)
    val v51 = vote(5, 1, c"Alice", candCt)
    val v52 = vote(5, 2, c"Bob", candCt)

    val v60 = vote(6, 0, c"Charlie", candCt)
    val v61 = vote(6, 1, c"Alice", candCt)
    val v62 = vote(6, 2, c"Bob", candCt)

    val v70 = vote(7, 0, c"Charlie", candCt)
    val v71 = vote(7, 1, c"Bob", candCt)
    val v72 = vote(7, 2, c"Alice", candCt)

    val v80 = vote(8, 0, c"Charlie", candCt)
    val v81 = vote(8, 1, c"Bob", candCt)
    val v82 = vote(8, 2, c"Alice", candCt)

    // at this point, prefs looks like this:
    // 0: Alice, 1: Bob, 2: Charlie
    //       | |0|1|2|3|4|5|6|7|8| voters
    // ranks |0|0|0|1|1|1|2|2|2|2|
    //       |1|1|1|0|0|0|0|0|1|1|
    //       |2|2|2|2|2|2|1|1|0|0|

    tabulate(voterCt, candCt)    // Bob and Charlie have 4 votes, Alice has 2.
    assertEquals(cands(0)._2, 2) // Alice has 2 votes
    assertEquals(cands(1)._2, 3) // Bob has 3 votes
    assertEquals(cands(2)._2, 4) // Charlie has 4 votes

    val won = printWinner(voterCt, candCt)
    assert(!won) // 50% of votes is 9/2 = 4, nobody has more than 4. Nobody wins.

    val min = findMin(voterCt, candCt)
    assertEquals(min, 2)

    val tie = isTie(min, candCt)
    assert(!tie) // result is 4,4,2, so not a tie.

    eliminate(min, candCt) // Alice is eliminated with 2 votes
    assert(cands(0)._3)    // Alice should be eliminated
    assert(!cands(1)._3)   // Bob should not be eliminated
    assert(!cands(2)._3)   // Charlie should not be eliminated

    resetVotes(candCt)

    tabulate(voterCt, candCt)    // In the second round Bob gets 5 votes, Charlie gets 4.
    assertEquals(cands(0)._2, 0) // Alice has 0 votes (eliminated last round)
    assertEquals(cands(1)._2, 5) // Bob has 5 votes
    assertEquals(cands(2)._2, 4) // Charlie has 4 votes

    val won2 = printWinner(voterCt, candCt) // should print Bob
    assert(won2) // Bob is the winner
