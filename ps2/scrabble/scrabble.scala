import scalanative.unsafe.{CQuote, stackalloc, Ptr, CInt, CChar, CString, CDouble}
import scalanative.unsigned.{UnsignedRichInt, USize}
import scalanative.libc.stdio.printf
import scalanative.libc.string.strlen
import scalanative.libc.ctype.{isalpha, isupper, islower, isspace}
import scalanative.libc.math.round
import scalanative.libc.stdlib.{EXIT_SUCCESS, EXIT_FAILURE}
import scala.util.boundary, boundary.break

/** Gets string input from user. */
@main
def scrabble: Unit =
  val text = getString(c"Text: ")
  ()
