import scalanative.unsafe.{CQuote, stackalloc, Ptr, CInt}
import scalanative.libc.stdio.{printf, sscanf, stdin, fgets}
import scala.util.boundary, boundary.break

/** Reads input from the user and parses it to an integer. If the input is not an integer, throws
  * `IllegalArgumentException`.
  *
  * @return
  *   The integer value inputted by the user.
  */
// def getInt: Int =
//   val line: Ptr[Byte]      = stackalloc[Byte](1024)
//   val intPointer: Ptr[Int] = stackalloc[Int](1)
//   boundary:
//     while fgets(line, 1024 - 1, stdin) != null do
//       val scanResult: CInt = sscanf(line, c"%d\n", intPointer)
//       if scanResult == 0 then throw java.lang.IllegalArgumentException("parse error in sscanf, expected integer")
//       else break()
//   !intPointer

@main
def credit: Unit =
  ()
