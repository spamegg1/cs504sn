import scalanative.unsafe.*
import scalanative.unsigned.{UnsignedRichInt, USize}
import scalanative.libc.stdio.printf
import scalanative.libc.string.strlen
import scalanative.libc.ctype.{isalpha, isupper, islower, isspace, tolower, toupper}
import scalanative.libc.math.round
import scalanative.libc.stdlib.{EXIT_SUCCESS, EXIT_FAILURE}
import scala.util.boundary, boundary.break

val Lower = c"abcdefghijklmnopqrstuvwxyz"
val Upper = c"ABCDEFGHIJKLMNOPQRSTUVWXYZ"

/** Converts string to lowercase, in place.
  *
  * @param key
  *   A string of characters.
  */
def strToLower(key: CString): Unit =
  for i <- 0 until strlen(key).toInt do key(i) = tolower(key(i)).toByte

/** Checks given key for validity. Assumes key is all lowercase.
  *
  * @param key
  *   A string of characters.
  * @return
  *   true if key is invalid, false if key is good.
  */
def invalid(key: CString): CBool = boundary:
  if strlen(key) != 26 then
    printf(c"Key must contain 26 characters.\n")
    break(true)

  for i <- 0 until 26 do
    if isalpha(key(i)) == 0 then
      printf(c"Key must contain only alphabetical characters.\n")
      break(true)

  for i <- 0 until 26 do // check if key contains each lowercase letter exactly once
    var count = 0
    for j <- 0 until 26 do if Lower(i) == key(j) then count += 1
    if count != 1 then
      printf(c"Key must contain each letter exactly once.\n")
      break(true)

  break(false) // passed all checks, key is valid.

/** Writes ciphertext to given buffer, in place, based on given key.
  *
  * @param length
  *   Length of the plaintext (and of the ciphertext).
  * @param plaintext
  *   A string of characters.
  * @param ciphertext
  *   Pre-allocated buffer to write into.
  * @param key
  *   A permutation of the 26-letter alphabet to be used as cipher.
  */
def substitute(length: CSize, plaintext: CString, ciphertext: CString, key: CString): Unit =
  for i <- 0 until length.toInt do // Go through the plaintext
    if isalpha(plaintext(i)) != 0 then   // check if the ith char is a letter
      if isupper(plaintext(i)) != 0 then // Now check if it is upper case
        boundary:
          for k <- 0 until 26 do
            if Upper(k) == plaintext(i) then         // Now find its position in the alphabet
              ciphertext(i) = toupper(key(k)).toByte // replace it by letter in key
              break()
      if islower(plaintext(i)) != 0 then // Repeat same procedure for lower case
        boundary:
          for k <- 0 until 26 do
            if Lower(k) == plaintext(i) then         // Now find its position in the alphabet
              ciphertext(i) = tolower(key(k)).toByte // replace it by letter in key
              break()
    else ciphertext(i) = plaintext(i) // Finally, if it's not a letter, leave it unchanged

object Substitution:
  def main(args: Array[String]): Unit =
    boundary:
      if args.length != 1 then // error if there is not exactly 1 argument
        printf(c"Usage: ./substitution key\n")
        break(EXIT_FAILURE)

      Zone:
        val key = toCString(args.head)
        strToLower(key)
        if invalid(key) then break(EXIT_FAILURE)
        val plaintext  = getString(c"plaintext: ")
        val length     = strlen(plaintext)
        val ciphertext = stackalloc[Byte](length + 1.toUSize) // Allocate ciphertext
        substitute(length, plaintext, ciphertext, key) // write substitution
        ciphertext(length) = 0.toByte // null terminate
        printf(c"ciphertext: %s\n", ciphertext)
        break(EXIT_SUCCESS)
