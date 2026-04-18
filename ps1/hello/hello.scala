import scalanative.unsafe.{CQuote, CString, stackalloc}
import scalanative.libc.stdio.{printf, stdin, fgets, sscanf}
import scalanative.libc.stdlib.malloc
import scala.util.boundary, boundary.break
import java.lang.IllegalArgumentException as IAE

/** Gets input from user via `stdin` and returns the string the user inputted (pointer to the beginning of the string,
  * allocated on the heap). Does not check if string length is safe, if `malloc` failed, if `fgets` failed or if
  * `sscanf` failed. Assumes user input to be less than 256 bytes.
  *
  * @return
  *   The `CString` user typed into `stdin`.
  */
def getString(message: CString = c""): CString =
  printf(message)
  val in  = stackalloc[Byte](256)
  val out = malloc(256)
  val _   = fgets(in, 256, stdin)
  val _   = sscanf(in, c"%255s\n", out)
  out

/** Asks the user for their name and prints a greeting message. Example:
  *
  * ❯ ./hello
  *
  * What is your name?
  *
  * Spam
  *
  * hello, Spam
  */
@main
def hello: Unit =
  val name = getString(c"What is your name?\n")
  printf(c"hello, %s\n", name);
