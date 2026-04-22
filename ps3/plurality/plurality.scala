import scalanative.unsafe.*
import scalanative.unsigned.{UnsignedRichInt, USize}
import scalanative.libc.stdio.printf
import scalanative.libc.string.{strlen, strcmp}
import scalanative.libc.ctype.{isalpha, isupper, islower, isspace, tolower, toupper}
import scalanative.libc.math.round
import scalanative.libc.stdlib.{EXIT_SUCCESS, EXIT_FAILURE, malloc}
import scala.util.boundary, boundary.break

// To do a fixed size of 20 chars instead of CString:
// type Twenty    = Nat.Digit2[Nat._2, Nat._0]
// type Candidate = CStruct2[CArray[CChar, Twenty], CInt]

val Max = 9 // Max number of candidates
type Candidate  = CStruct2[CString, CInt] // Candidates have name and vote count
type Candidates = Array[Candidate]

/** Finds the maximum number of votes after the election.
  *
  * @param candidateCount
  *   The number of candidates.
  * @param candidates
  *   A global mutable array of candidates.
  * @return
  *   The maximum number of votes obtained by any candidate.
  */
def maxVotes(candidateCount: Int)(using candidates: Candidates): Int =
  var max = 0
  var i   = 0
  while i < candidateCount do
    if candidates(i)._2 > max then max = candidates(i)._2
    i += 1
  max

/** Prints the winner of the election.
  *
  * @param candidateCount
  *   The number of candidates.
  * @param candidates
  *   A global mutable array of candidates.
  */
def printWinner(candidateCount: Int)(using candidates: Candidates): Unit =
  var max = maxVotes(candidateCount)
  var i   = 0
  while i < candidateCount do
    if candidates(i)._2 == max then printf(c"%s\n", candidates(i)._1)
    i += 1

/** Updates vote counts for a given vote.
  *
  * @param candidateCount
  *   The number of candidates.
  * @param name
  *   The name of the candidate the vote is for.
  * @param candidates
  *   A global mutable array of candidates.
  * @return
  *   true if vote is valid, false otherwise.
  */
def vote(candidateCount: Int, name: CString)(using candidates: Candidates): CBool = boundary:
  var i = 0
  while i < candidateCount do
    if strcmp(name, candidates(i)._1) == 0 then
      candidates(i)._2 = candidates(i)._2 + 1
      break(true)
    i += 1
  false

object Plurality:
  def main(args: Array[String]): Unit = boundary:
    val candidateCount = args.length

    if candidateCount < 1 then
      printf(c"Usage: plurality [candidate ...]\n")
      break(EXIT_FAILURE)

    if candidateCount > Max then
      printf(c"Maximum number of candidates is %i\n", Max)
      break(EXIT_FAILURE)

    given candidates: Candidates = Array.ofDim[Candidate](Max)

    Zone:
      var i = 0 // Populate array of candidates
      while i < candidateCount do
        val newCandidate = stackalloc[Candidate](1)
        newCandidate._1 = toCString(args(i)) // name
        newCandidate._2 = 0                  // votes
        candidates(i) = newCandidate
        i += 1

      val voterCount = getInt(c"Number of voters: ")

      i = 0
      while i < voterCount do // Loop over all voters
        val name = getString(c"Vote: ")
        if !vote(candidateCount, name) then printf(c"Invalid vote.\n")
        i += 1

      printWinner(candidateCount)
