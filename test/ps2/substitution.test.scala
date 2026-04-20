import scalanative.unsafe.{CQuote, stackalloc, fromCString, Zone}
import scalanative.unsigned.UnsignedRichInt
import munit.FunSuite

class SubstitutionTest extends FunSuite:
  test("substitution works correctly on valid key and short plaintext"):
    val key        = c"YTNSHKVEFXRBAUQZCLWDMIPGJO"
    val plaintext  = c"Hello!"
    val ciphertext = stackalloc[Byte](7)
    substitute(7.toUSize, plaintext, ciphertext, key)
    ciphertext(6) = 0.toByte
    assertEquals(fromCString(ciphertext), "Ehbbq!")
