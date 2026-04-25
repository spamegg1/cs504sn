import scalanative.unsafe.*
import scalanative.unsigned.{UnsignedRichInt, USize}
import scalanative.libc.string.strlen
import scalanative.libc.math.round
import munit.FunSuite

class RunoffTest extends FunSuite:
  test("vote correctly sets preferences and tabulate correctly counts votes"):
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
    val v22 = vote(2, 1, c"Charlie", candCt)

    val v30 = vote(3, 0, c"Bob", candCt)
    val v31 = vote(3, 1, c"Alice", candCt)
    val v32 = vote(3, 1, c"Charlie", candCt)

    val v40 = vote(4, 0, c"Bob", candCt)
    val v41 = vote(4, 1, c"Alice", candCt)
    val v42 = vote(4, 1, c"Charlie", candCt)

    val v50 = vote(5, 0, c"Charlie", candCt)
    val v51 = vote(5, 1, c"Alice", candCt)
    val v52 = vote(5, 1, c"Bob", candCt)

    val v60 = vote(6, 0, c"Charlie", candCt)
    val v61 = vote(6, 1, c"Alice", candCt)
    val v62 = vote(6, 1, c"Bob", candCt)

    val v70 = vote(7, 0, c"Charlie", candCt)
    val v71 = vote(7, 1, c"Bob", candCt)
    val v72 = vote(7, 1, c"Alice", candCt)

    val v80 = vote(8, 0, c"Charlie", candCt)
    val v81 = vote(8, 1, c"Bob", candCt)
    val v82 = vote(8, 1, c"Alice", candCt)

    assert(v00) // all votes are valid
    assert(v01)
    assert(v02)
    assert(v10)
    assert(v11)
    assert(v12)
    assert(v20)
    assert(v21)
    assert(v22)
    assert(v30)
    assert(v31)
    assert(v32)
    assert(v40)
    assert(v41)
    assert(v42)
    assert(v50)
    assert(v51)
    assert(v52)
    assert(v60)
    assert(v61)
    assert(v62)
    assert(v70)
    assert(v71)
    assert(v72)
    assert(v80)
    assert(v81)
    assert(v82)

    // at this point, prefs looks like this:
    // 0: Alice, 1: Bob, 2: Charlie
    //       | |0|1|2|3|4|5|6|7|8| voters
    // ranks |0|0|1|1|1|1|2|2|2|2|
    //       |1|1|0|0|0|0|0|0|1|1|
    //       |2|2|2|2|2|2|1|1|0|0|
    // 50% of votes is 9/2 = 4, nobody has more than 4.
    // Alice is eliminated with min = 2 votes.
    // In the second round Bob gets 5 votes, Charlie gets 4. Bob wins!
