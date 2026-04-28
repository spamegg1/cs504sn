import scalanative.unsafe.*
import scalanative.unsigned.{UnsignedRichInt, USize}
import scalanative.libc.stdio.printf
import scalanative.libc.string.{strlen, strcmp}
import scalanative.libc.ctype.{isalpha, isupper, islower, isspace, tolower, toupper}
import scalanative.libc.math.round
import scalanative.libc.stdlib.{EXIT_SUCCESS, EXIT_FAILURE}
import scala.util.boundary, boundary.break

object Plurality:
  // To do a fixed size of 20 chars instead of CString:
  // type Twenty    = Nat.Digit2[Nat._2, Nat._0]
  // type Cand = CStruct2[CArray[CChar, Twenty], CInt]

  val Max = 9 // Max number of cands
  type Cand = CStruct2[CString, CInt] // Cands have name and vote count
  val cands = Array.ofDim[Cand](Max)

  /** Finds the maximum number of votes after the election.
    *
    * @param candCt
    *   The number of cands.
    * @param cands
    *   A global mutable array of candidates.
    * @return
    *   The maximum number of votes obtained by any candidate.
    */
  def maxVotes(candCt: Int): Int =
    var max = 0
    var i   = 0
    while i < candCt do
      if cands(i)._2 > max then max = cands(i)._2
      i += 1
    max

  /** Prints the winner of the election.
    *
    * @param candCt
    *   The number of candidates.
    * @param cands
    *   A global mutable array of candidates.
    */
  def printWinner(candCt: Int): Unit =
    var max = maxVotes(candCt)
    var i   = 0
    while i < candCt do
      if cands(i)._2 == max then printf(c"%s\n", cands(i)._1)
      i += 1

  /** Updates vote counts for a given vote.
    *
    * @param candCt
    *   The number of candidates.
    * @param name
    *   The name of the candidate the vote is for.
    * @param cands
    *   A global mutable array of candidates.
    * @return
    *   true if vote is valid, false otherwise.
    */
  def vote(candCt: Int, name: CString): CBool = boundary:
    var i = 0
    while i < candCt do
      if strcmp(name, cands(i)._1) == 0 then
        cands(i)._2 = cands(i)._2 + 1
        break(true)
      i += 1
    false

  /** Populates the candidates array with initial values.
    *
    * @param args
    *   Names of candidates (from command line arguments).
    * @param candCt
    *   Number of candidates.
    */
  def populate(args: Array[String], candCt: Int)(using Zone): Unit =
    var i = 0
    while i < candCt do
      val newCand = alloc[Cand](1)
      newCand._1 = toCString(args(i)) // name
      newCand._2 = 0                  // votes
      cands(i) = newCand
      i += 1

  /** Gets votes from user.
    *
    * @param voterCt
    *   Number of voters.
    * @param candCt
    *   Number of candidates.
    */
  def getVotes(voterCt: Int, candCt: Int): Unit =
    var j = 0
    while j < voterCt do // Loop over all voters
      val name = getString(c"Vote: ")
      if !vote(candCt, name) then printf(c"Invalid vote.\n")
      j += 1

  /** Runs the plurality election.
    *
    * @param args
    *   Names of candidates as command-line arguments.
    */
  def main(args: Array[String]): Unit = boundary:
    val candCt = args.length

    if candCt < 1 then
      printf(c"Usage: plurality [candidate ...]\n")
      break(EXIT_FAILURE)

    if candCt > Max then
      printf(c"Maximum number of cands is %i\n", Max)
      break(EXIT_FAILURE)

    Zone:
      populate(args, candCt)
      val voterCt = getInt(c"Number of voters: ")
      getVotes(voterCt, candCt)
      printWinner(candCt)
