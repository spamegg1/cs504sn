import scalanative.unsafe.*
import scalanative.unsigned.{UnsignedRichInt, USize}
import scalanative.libc.stdio.printf
import scalanative.libc.string.{strlen, strcmp}
import scalanative.libc.ctype.{isalpha, isupper, islower, isspace, tolower, toupper}
import scalanative.libc.math.round
import scalanative.libc.stdlib.{EXIT_SUCCESS, EXIT_FAILURE, malloc}
import scala.util.boundary, boundary.break

object Plurality:
  // To do a fixed size of 20 chars instead of CString:
  // type Twenty    = Nat.Digit2[Nat._2, Nat._0]
  // type Cand = CStruct2[CArray[CChar, Twenty], CInt]

  val Max = 9 // Max number of cands
  type Cand  = CStruct2[CString, CInt] // Cands have name and vote count
  type Cands = Array[Cand]
  val cands = Array.ofDim[Cand](Max)

  /** Finds the maximum number of votes after the election.
    *
    * @param candCount
    *   The number of cands.
    * @param cands
    *   A global mutable array of candidates.
    * @return
    *   The maximum number of votes obtained by any candidate.
    */
  def maxVotes(candCount: Int): Int =
    var max = 0
    var i   = 0
    while i < candCount do
      if cands(i)._2 > max then max = cands(i)._2
      i += 1
    max

  /** Prints the winner of the election.
    *
    * @param candCount
    *   The number of cands.
    * @param cands
    *   A global mutable array of cands.
    */
  def printWinner(candCount: Int): Unit =
    var max = maxVotes(candCount)
    var i   = 0
    while i < candCount do
      if cands(i)._2 == max then printf(c"%s\n", cands(i)._1)
      i += 1

  /** Updates vote counts for a given vote.
    *
    * @param candCount
    *   The number of cands.
    * @param name
    *   The name of the candidate the vote is for.
    * @param cands
    *   A global mutable array of cands.
    * @return
    *   true if vote is valid, false otherwise.
    */
  def vote(candCount: Int, name: CString): CBool = boundary:
    var i = 0
    while i < candCount do
      if strcmp(name, cands(i)._1) == 0 then
        cands(i)._2 = cands(i)._2 + 1
        break(true)
      i += 1
    false

  def main(args: Array[String]): Unit = boundary:
    val candCount = args.length

    if candCount < 1 then
      printf(c"Usage: plurality [candidate ...]\n")
      break(EXIT_FAILURE)

    if candCount > Max then
      printf(c"Maximum number of cands is %i\n", Max)
      break(EXIT_FAILURE)

    Zone:
      var i = 0 // Populate array of cands
      while i < candCount do
        val newCand = stackalloc[Cand](1)
        newCand._1 = toCString(args(i)) // name
        newCand._2 = 0                  // votes
        cands(i) = newCand
        i += 1

      val voterCount = getInt(c"Number of voters: ")

      var j = 0
      while j < voterCount do // Loop over all voters
        val name = getString(c"Vote: ")
        if !vote(candCount, name) then printf(c"Invalid vote.\n")
        j += 1

      printWinner(candCount)
