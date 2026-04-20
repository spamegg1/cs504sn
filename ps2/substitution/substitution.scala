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
def invalid(key: CString): CBool =
  boundary:
    if strlen(key) != 26 then
      printf(c"Key must contain 26 characters.\n")
      break(true)

    for i <- 0 until 26 do
      if isalpha(key(i)) == 0 then
        printf(c"Key must contain only alphabetical characters.\n")
        break(true)

    // check if key contains each lowercase letter exactly once
    for i <- 0 until 26 do
      var count = 0
      for j <- 0 until 26 do if Lower(i) == key(j) then count += 1
      if count != 1 then
        printf(c"Key must contain each letter exactly once.\n")
        break(true)

    break(false) // passed all checks, key is valid.

object Substitution:
  def main(args: Array[String]): Unit =
    boundary:
      // Give error if there is not exactly one command line argument
      if args.length != 1 then
        printf(c"Usage: ./substitution key\n")
        break(EXIT_FAILURE)

      Zone:
        // Store the key in a variable, convert all to lowercase
        val key = toCString(args.head)
        strToLower(key)
        if invalid(key) then break(EXIT_FAILURE) // Give error if key is invalid

        // Get from user the plaintext to be ciphered
        val plaintext = getString(c"plaintext: ")
        val length    = strlen(plaintext)

        // Declare the ciphertext, terminate with the null character
        val ciphertext = stackalloc[Byte](length + 1.toUSize)
        ciphertext(length) = 0.toByte

        // Go through the plaintext
        for i <- 0 until length.toInt do
          // First check if the ith character in plaintext is a letter
          if isalpha(plaintext(i)) != 0 then
            // Now check if it is upper case
            if isupper(plaintext(i)) != 0 then
              // Now find its position in the alphabet,
              // replace it by corresponding letter in key
              boundary:
                for k <- 0 until 26 do
                  if Upper(k) == plaintext(i) then
                    ciphertext(i) = toupper(key(k)).toByte
                    break()

            // Repeat same procedure for lower case
            if islower(plaintext(i)) != 0 then
              // Now find its position in the alphabet,
              // replace it by corresponding letter in key
              boundary:
                for k <- 0 until 26 do
                  if Lower(k) == plaintext(i) then
                    ciphertext(i) = key(k)
                    break()

          // Finally, if the ith character is not a letter, leave it unchanged
          // Also we do not move forward in the shift array this time,
          // as no letters from keyword were used. Variable j remains unchanged.
          else ciphertext(i) = plaintext(i)

        // Print the completed ciphertext
        printf(c"ciphertext: %s\n", ciphertext)
        break(EXIT_SUCCESS)
