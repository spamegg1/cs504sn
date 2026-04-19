import scalanative.unsafe.{CQuote, stackalloc, Ptr, CInt, CChar, CString, CDouble}
import scalanative.unsigned.{UnsignedRichInt, USize}
import scalanative.libc.stdio.printf
import scalanative.libc.string.strlen
import scalanative.libc.ctype.{isalpha, isupper, islower, isspace}
import scalanative.libc.math.round
import scalanative.libc.stdlib.{EXIT_SUCCESS, EXIT_FAILURE}
import scala.util.boundary, boundary.break

/** Scrabble letter scores */
val Points =
  Seq(1, 3, 3, 2, 1, 4, 2, 4, 1, 8, 5, 1, 3, 1, 1, 3, 10, 1, 1, 1, 1, 4, 4, 8, 4, 10, 0)

/** Looks up the score for each letter. Handles upper and lower case letters, and non-letters (which receive 0 points).
  *
  * @param letter
  *   A single character.
  * @return
  *   The Scrabble score for the character.
  */
def letterScore(letter: CChar): CInt =
  val index =
    if isupper(letter) != 0 then letter - 65
    else if islower(letter) != 0 then letter - 97
    else 26
  Points(index)

/** Sums up all the letter scores in a given word.
  *
  * @param word
  *   A string of characters.
  * @return
  *   The Scrabble score of the given word.
  */
def computeScore(word: CString): CInt =
  var score = 0
  for i <- 0 until strlen(word).toInt do score += letterScore(word(i))
  score

/** Gets input from two players, and decides winner / loser / tie by calculating their Scrabble scores. */
@main
def scrabble: Unit =
  val word1  = getString(c"Player 1: ")
  val word2  = getString(c"Player 2: ")
  val score1 = computeScore(word1)
  val score2 = computeScore(word2)
  if score1 < score2 then printf(c"Player 2 wins!\n")
  else if score2 < score1 then printf(c"Player 1 wins!\n")
  else printf(c"Tie!\n")
