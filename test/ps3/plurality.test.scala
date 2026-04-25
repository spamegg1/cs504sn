import scalanative.unsafe.*
import scalanative.unsigned.{UnsignedRichInt, USize}
import scalanative.libc.string.strlen
import scalanative.libc.math.round
import munit.FunSuite

class PluralityTest extends FunSuite:
  test("vote returns true/false for a valid/invalid candidate and produces correct vote counts"):
    import Plurality.*

    val alice: Cand   = stackalloc[Cand](1)
    val charlie: Cand = stackalloc[Cand](1)

    alice._1 = c"Alice"     // name
    alice._2 = 0            // votes
    charlie._1 = c"Charlie" // name
    charlie._2 = 0          // votes

    cands(0) = alice
    cands(1) = charlie

    assert(vote(2, c"Alice"))
    assert(!vote(2, c"Bob")) // invalid vote
    assert(vote(2, c"Alice"))
    assert(vote(2, c"Charlie"))
    assertEquals(cands(0)._2, 2) // Alice has 2 votes
    assertEquals(cands(1)._2, 1) // Charlie has 1 vote
    printWinner(2)               // should print "Alice"
