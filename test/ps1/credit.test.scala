import munit.FunSuite

class CreditTest extends FunSuite:
  val in = List(
    "378282246310005",
    "371449635398431",
    "5555555555554444",
    "5105105105105100",
    "4111111111111111",
    "4012888888881881",
    "4222222222222",
    "369421438430814",
    "5673598276138003",
    "4111111111111113",
    "4222222222223",
    "3400000000000620",
    "430000000000000"
  ).map: ccStr =>
    (ccStr.map(_.asDigit).reverse, ccStr.length)

  test("checkSum returns correct sums for various credit card numbers"):
    val out = List(60, 80, 60, 20, 30, 90, 40, 70, 60, 32, 41, 20, 10)
    assertEquals(out, in.map(checkSum.tupled), "checkSum returned wrong sum")
