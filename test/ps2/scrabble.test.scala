import scalanative.unsafe.CQuote
import munit.FunSuite

class ScrabbleTest extends FunSuite:
  test("computeScore correctly detects various ties"):
    assertEquals(computeScore(c"Question?"), computeScore(c"Question!"))
    assertEquals(computeScore(c"drawing"), computeScore(c"illustration"))

  test("computeScore correctly computes winners and losers for various scrabbles"):
    assert(computeScore(c"Oh,") < computeScore(c"hai!"))
    assert(computeScore(c"science") < computeScore(c"COMPUTER"))
    assert(computeScore(c"wiNNeR") < computeScore(c"Scrabble"))
    assert(computeScore(c"dog") < computeScore(c"pig"))
    assert(computeScore(c"figure?") < computeScore(c"Skating!"))
