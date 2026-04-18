import scalanative.unsafe.{CQuote, stackalloc, Ptr, CInt, CString}
import scalanative.unsigned.{UnsignedRichInt, USize}
import scalanative.libc.stdio.printf
import scalanative.libc.string.strlen
import scalanative.libc.ctype.{isalpha, isupper, islower}
import scalanative.libc.stdlib.{EXIT_SUCCESS, EXIT_FAILURE}
import scala.util.boundary, boundary.break

object Caesar:
  val lower = "abcdefghijklmnopqrstuvwxyz"
  val upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"

  /** Writes ciphertext based on given plaintext and key. The ciphertext should be pre-allocated by the caller, with
    * size `length + 1`.
    *
    * @param plaintext
    *   The plaintext to be ciphered.
    * @param ciphertext
    *   A pre-allocated CString to write into.
    * @param length
    *   The string length of the plaintext (and the ciphertext)
    * @param key
    *   An integer from 0 to 25, the alphabet shift value for Caesar cipher.
    */
  def writeCiphertext(plaintext: CString, ciphertext: CString, length: USize, key: Int): Unit =
    for i <- 0 until length.toInt do
      val cchar = plaintext(i)
      if isalpha(cchar) != 0 then
        if isupper(cchar) != 0 then
          val j = upper.indexWhere(_ == cchar)
          ciphertext(i) = upper((j + key) % 26).toByte
        if islower(cchar) != 0 then
          val j = lower.indexWhere(_ == cchar)
          ciphertext(i) = lower((j + key) % 26).toByte
      else ciphertext(i) = cchar
    ciphertext(length) = 0.toByte

  /** Interactive Caesar cipher. Accepts the shift value (key) as command-line argument, then prompts user to input the
    * plaintext, and prints the ciphertext.
    *
    * @param args
    *   Command line arguments. Should only be 1 argument (the key value, nonnegative integer).
    */
  def main(args: Array[String]): Unit = boundary:
    if args.length != 1 then
      printf(c"Error, need exactly 1 argument\n")
      break(EXIT_FAILURE)

    val keyOpt = args.head.toIntOption

    keyOpt match
      case None =>
        printf(c"Usage: ./caesar key\n")
        break(EXIT_FAILURE)

      case Some(k) =>
        val key        = k % 26
        val plaintext  = getString(c"plaintext: ")
        val length     = strlen(plaintext)
        val ciphertext = stackalloc[Byte](length + 1.toUSize)
        writeCiphertext(plaintext, ciphertext, length, key)
        printf(c"ciphertext: %s\n", ciphertext)
        break(EXIT_SUCCESS)
