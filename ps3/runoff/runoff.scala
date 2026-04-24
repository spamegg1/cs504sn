import scalanative.unsafe.*
import scalanative.unsigned.{UnsignedRichInt, USize}
import scalanative.libc.stdio.printf
import scalanative.libc.string.{strlen, strcmp}
import scalanative.libc.stdlib.{EXIT_SUCCESS, EXIT_FAILURE}
import scala.util.boundary, boundary.break

// Max voters and candidates
val MaxVoters     = 100
val MaxCandidates = 9

// preferences(i)(j) is jth preference for voter i
val preferences = Array.ofDim[Int](MaxVoters, MaxCandidates)

type Candidate = CStruct3[CString, CInt, CBool] // name, votes, eliminated?
val candidates = Array.ofDim[Candidate](MaxCandidates) // Array of candidates

// Numbers of voters and candidates
var voterCount     = 0
var candidateCount = 0

/** Records preference if vote is valid.
  *
  * @param voter
  *   The integer identifying the voter.
  * @param rank
  *   The ranked preference of the voter for this candidate.
  * @param name
  *   The name of the candidate.
  * @return
  *   true if vote is valid, false if invalid.
  */
def vote(voter: CInt, rank: CInt, name: CString): CBool = boundary:
  var i = 0
  while i < candidateCount do
    if strcmp(candidates(i)._1, name) == 0 then
      preferences(voter)(rank) = i
      break(true)
    i += 1
  false

// Tabulate votes for non-eliminated candidates
def tabulate: Unit =
  var i = 0
  while i < voterCount do
    boundary:
      var j = 0
      while j < candidateCount do
        if !candidates(preferences(i)(j))._3 then
          candidates(preferences(i)(j))._2 = candidates(preferences(i)(j))._2 + 1
          break()
        j += 1
    i += 1

// Print the winner of the election, if there is one
def printWinner: CBool = boundary:
  var i = 0
  while i < candidateCount do
    if candidates(i)._2 > voterCount / 2 then
      printf(c"%s\n", candidates(i)._1)
      break(true)
    i += 1
  return false;

/** Finds the minimum number of votes among candidates.
  *
  * @return
  *   The minimum number of votes any remaining candidate has.
  */
def findMin: CInt =
  var min = voterCount
  var i   = 0
  while i < candidateCount do
    if !candidates(i)._3 && candidates(i)._2 < min then min = candidates(i)._2
    i += 1
  min

/** Checks if election is tied between all candidates.
  *
  * @param min
  *   The minimum vote count received in this round of voting.
  * @return
  *   true if the election is tied between all candidates, false otherwise.
  */
def isTie(min: CInt): CBool = boundary:
  var i = 0
  while i < candidateCount do
    if !candidates(i)._3 && candidates(i)._2 != min then break(false)
    i += 1
  true

/** Eliminates the candidate(s) in last place.
  *
  * @param min
  *   The minimum vote count received in this round of voting.
  */
def eliminate(min: CInt): Unit =
  var i = 0
  while i < candidateCount do
    if candidates(i)._2 == min then candidates(i)._3 = true
    i += 1

@main
def runoff: Unit = ()
