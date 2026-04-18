import scalanative.unsafe.{CQuote, stackalloc, Ptr, CInt, CString, fromCString}
import scalanative.unsigned.{UnsignedRichInt, USize}
import scalanative.libc.string.strlen
import munit.FunSuite

class CaesarTest extends FunSuite:
  test("writeCiphertext is correct for key=14 on a string of length 9"):
    val plaintext  = c"plaintext"
    val ciphertext = stackalloc[Byte](10)
    Caesar.writeCiphertext(plaintext, ciphertext, 9.toUSize, 14)
    assertEquals(fromCString(ciphertext), "dzowbhslh")
