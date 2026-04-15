import munit.FunSuite

class CashTest extends FunSuite:
  test("getCoins returns correct number of coins for various change"):
    val in  = List(41, 1, 15, 160, 2300)
    val out = List(4, 1, 2, 7, 92)
    assertEquals(out, in.map(getCoins), "getCoins returns wrong number of coins")
