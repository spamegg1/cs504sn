import scalanative.unsafe.{CQuote, stackalloc, Ptr, CInt, CChar, CString, CDouble}
import scalanative.unsigned.{UnsignedRichInt, USize}
import scalanative.libc.stdio.printf
import scalanative.libc.string.strlen
import scalanative.libc.ctype.{isalpha, isupper, islower, isspace}
import scalanative.libc.math.round
import scalanative.libc.stdlib.{EXIT_SUCCESS, EXIT_FAILURE}
import scala.util.boundary, boundary.break

/** Checks if a character is a "period" (`.`, `?`, `!`).
  *
  * @param letter
  *   A single character.
  * @return
  *   `true` if it's a dot, question mark or exclamation, `false` otherwise.
  */
def isperiod(letter: CChar): Boolean = letter == '.' || letter == '?' || letter == '!'

/** Counts the number of alphabetical characters in given text.
  *
  * @param text
  *   A string of characters.
  * @return
  *   The number of alphabetical characters in text.
  */
def countLetters(text: CString): CInt =
  var count = 0
  for i <- 0 until strlen(text).toInt do if isalpha(text(i)) != 0 then count += 1
  count

/** Counts the number of words in a given sentence.
  *
  * @param text
  *   A string of characters.
  * @return
  *   The number of words in the sentence (separated by spaces).
  */
def countWords(text: CString): CInt =
  var count = 1
  for i <- 0 until strlen(text).toInt do if isspace(text(i)) != 0 then count += 1
  count

/** Counts the number of sentences in a given text. Assumes that a sentence will not start or end with a space, and
  * assumes that a sentence will not have multiple spaces in a row.
  *
  * @param text
  *   A string of characters, consisting of many sentences.
  * @return
  *   The number of sentences in the text (separated by a dot or a question mark or an exclamation mark).
  */
def countSentences(text: CString): CInt =
  var count = 0
  for i <- 0 until strlen(text).toInt do if isperiod(text(i)) then count += 1
  count

/** Calculates the Coleman-Liau score of the given letter, word, sentence counts.
  *
  * The score is calculated by the formula `0.0588 * L - 0.296 * S - 15.8` where `L` and `S` are the average numbers of
  * letters and sentences.
  *
  * @param letters
  *   The number of letters in a given text.
  * @param words
  *   The number of words in a given text.
  * @param sentences
  *   The number of sentences in a given text.
  * @return
  *   The Coleman-Liau score of the text.
  */
def colemanLiau(letters: CInt, words: CInt, sentences: CInt): CDouble =
  val L = letters * 100.0 / words
  val S = sentences * 100.0 / words
  0.0588 * L - 0.296 * S - 15.8

/** Wrapper for `colemanLiau` that works directly on text.
  *
  * @param text
  *   A string of characters.
  * @return
  *   The Coleman-Liau score of the text.
  */
def getScore(text: CString): CDouble =
  val letters   = countLetters(text)
  val words     = countWords(text)
  val sentences = countSentences(text)
  colemanLiau(letters, words, sentences)

/** Prints the grade level associated with the given score.
  *
  * @param score
  *   The Coleman-Liau score of a text.
  */
def printScore(score: CDouble): Unit =
  if 16.0 <= score then printf(c"Grade 16+\n")
  else if score < 1.0 then printf(c"Before Grade 1\n")
  else printf(c"Grade %i\n", round(score))

/** Useful for testing.
  *
  * @param score
  *   The Coleman-Liau score of a text.
  * @return
  *   The grade level of the score.
  */
def grade(score: CDouble): CInt =
  if 16.0 <= score then 16 else if score < 1.0 then 0 else round(score).toInt

/** Gets string input from user and returns its grade level of readability. */
@main
def readability: Unit =
  val text = getString(c"Text: ")
  printScore(getScore(text))
