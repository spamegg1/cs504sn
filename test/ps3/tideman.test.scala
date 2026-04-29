import scalanative.unsafe.*
import munit.FunSuite

class TidemanTest extends FunSuite:
  test("tideman full integration testing"):
    import Tideman.*

    val candCt  = 3
    val voterCt = 9

    cands(0) = c"Alice"
    cands(1) = c"Bob"
    cands(2) = c"Charlie"

    val ranks = Array.ofDim[CInt](candCt)

    val v00 = vote(candCt, 0, c"Alice", ranks)
    val v01 = vote(candCt, 1, c"Bob", ranks)
    val v02 = vote(candCt, 2, c"Charlie", ranks)
    recordPrefs(candCt, ranks)

    val v10 = vote(candCt, 0, c"Alice", ranks)
    val v11 = vote(candCt, 1, c"Bob", ranks)
    val v12 = vote(candCt, 2, c"Charlie", ranks)
    recordPrefs(candCt, ranks)

    val v20 = vote(candCt, 0, c"Alice", ranks)
    val v21 = vote(candCt, 1, c"Bob", ranks)
    val v22 = vote(candCt, 2, c"Charlie", ranks)
    recordPrefs(candCt, ranks)

    val v30 = vote(candCt, 0, c"Bob", ranks)
    val v31 = vote(candCt, 1, c"Charlie", ranks)
    val v32 = vote(candCt, 2, c"Alice", ranks)
    recordPrefs(candCt, ranks)

    val v40 = vote(candCt, 0, c"Bob", ranks)
    val v41 = vote(candCt, 1, c"Charlie", ranks)
    val v42 = vote(candCt, 2, c"Alice", ranks)
    recordPrefs(candCt, ranks)

    val v50 = vote(candCt, 0, c"Charlie", ranks)
    val v51 = vote(candCt, 1, c"Alice", ranks)
    val v52 = vote(candCt, 2, c"Bob", ranks)
    recordPrefs(candCt, ranks)

    val v60 = vote(candCt, 0, c"Charlie", ranks)
    val v61 = vote(candCt, 1, c"Alice", ranks)
    val v62 = vote(candCt, 2, c"Bob", ranks)
    recordPrefs(candCt, ranks)

    val v70 = vote(candCt, 0, c"Charlie", ranks)
    val v71 = vote(candCt, 1, c"Alice", ranks)
    val v72 = vote(candCt, 2, c"Bob", ranks)
    recordPrefs(candCt, ranks)

    val v80 = vote(candCt, 0, c"Charlie", ranks)
    val v81 = vote(candCt, 1, c"Alice", ranks)
    val v82 = vote(candCt, 2, c"Bob", ranks)
    recordPrefs(candCt, ranks)

    // at this point, prefs should look like this:
    // 0 = Alice, 1 = Bob, 2 = Charlie
    // Alice/Bob = 7-2, Bob/Charlie = 5-4, Charlie/Alice = 6-3
    //   |0|1|2|
    // |0|0|7|3|
    // |1|2|0|5|
    // |2|6|4|0|

    assertEquals(prefs(0)(0), 0)
    assertEquals(prefs(0)(1), 7)
    assertEquals(prefs(0)(2), 3)

    assertEquals(prefs(1)(0), 2)
    assertEquals(prefs(1)(1), 0)
    assertEquals(prefs(1)(2), 5)

    assertEquals(prefs(2)(0), 6)
    assertEquals(prefs(2)(1), 4)
    assertEquals(prefs(2)(2), 0)

    Zone:
      addPairs(candCt)
      assertEquals(pairCt, 3)

      sortPairs
      // Now pairs should be:
      // (0,1) because Alice=0 beats Bob=1 by 7-2
      // (2,0) because Charlie=2 beats Alice=0 by 6-3
      // (1,2) because Bob=1 beats Charlie=2 by 5-4
      assertEquals(pairs(0)._1, 0)
      assertEquals(pairs(0)._2, 1)
      assertEquals(pairs(1)._1, 2)
      assertEquals(pairs(1)._2, 0)
      assertEquals(pairs(2)._1, 1)
      assertEquals(pairs(2)._2, 2)

      lockPairs(candCt)
      // Now locked array should be:
      //   |0|1|2|
      // |0|F|T|F|
      // |1|F|F|F|
      // |2|T|F|F|
      assert(!locked(0)(0))
      assert(locked(0)(1))
      assert(!locked(0)(2))
      assert(!locked(1)(0))
      assert(!locked(1)(1))
      assert(!locked(1)(2))
      assert(locked(2)(0))
      assert(!locked(2)(1))
      assert(!locked(2)(2))

      printWinner(candCt) // Should print Charlie
