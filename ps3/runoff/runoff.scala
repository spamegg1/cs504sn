import scalanative.unsafe.*
import scalanative.unsigned.UnsignedRichInt
import scalanative.libc.stdio.printf
import scalanative.libc.string.strcmp
import scalanative.libc.stdlib.{EXIT_SUCCESS, EXIT_FAILURE}
import scala.util.boundary, boundary.break

object Runoff:
  val MaxVoters = 100
  val MaxCands  = 9
  // prefs(i)(j): ith voter's jth preferred candidate (Int = candidate number)
  val prefs = Array.ofDim[Int](MaxVoters, MaxCands)
  type Cand = CStruct3[CString, CInt, CBool] // name, votes, eliminated?
  val cands = Array.ofDim[Cand](MaxCands) // array index = candidate number

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
  def vote(voter: CInt, rank: CInt, name: CString, candCt: Int): CBool = boundary:
    var i = 0 // i = candidate number
    while i < candCt do
      if strcmp(cands(i)._1, name) == 0 then
        prefs(voter)(rank) = i
        break(true)
      i += 1
    false

  /** Tabulates votes for non-eliminated candidates.
    *
    * This means that for each voter, we find that voter's highest non-eliminated candidate and increase that
    * candidate's vote by 1.
    */
  def tabulate(voterCt: Int, candCt: Int): Unit =
    var i = 0 // i = voter number
    while i < voterCt do
      boundary:
        var j = 0 // j = ranked pref of voter i
        while j < candCt do
          val cand = prefs(i)(j)
          if !cands(cand)._3 then
            cands(cand)._2 = cands(cand)._2 + 1
            break()
          j += 1
      i += 1

  /** Prints the winner of the election, if there is one.
    *
    * @return
    *   true if there is a winner, false otherwise.
    */
  def printWinner(voterCt: Int, candCt: Int): CBool = boundary:
    var i = 0
    while i < candCt do
      if cands(i)._2 > voterCt / 2 then
        printf(c"%s\n", cands(i)._1)
        break(true)
      i += 1
    false

  /** Finds the minimum number of votes among cands.
    *
    * @return
    *   The minimum number of votes any remaining candidate has.
    */
  def findMin(voterCt: Int, candCt: Int): CInt =
    var min = voterCt
    var i   = 0
    while i < candCt do
      if !cands(i)._3 && cands(i)._2 < min then min = cands(i)._2
      i += 1
    min

  /** Checks if election is tied between all cands.
    *
    * @param min
    *   The minimum vote count received in this round of voting.
    * @return
    *   true if the election is tied between all cands, false otherwise.
    */
  def isTie(min: CInt, candCt: Int): CBool = boundary:
    var i = 0
    while i < candCt do
      if !cands(i)._3 && cands(i)._2 != min then break(false)
      i += 1
    true

  /** Eliminates the candidate(s) in last place.
    *
    * @param min
    *   The minimum vote count received in this round of voting.
    */
  def eliminate(min: CInt, candCt: Int): Unit =
    var i = 0
    while i < candCt do
      if cands(i)._2 == min then cands(i)._3 = true
      i += 1

  /** Resets vote counts back to zero.
    *
    * @param candCt
    *   Number of candidates.
    */
  def resetVotes(candCt: Int): Unit =
    var i = 0
    while i < candCt do
      cands(i)._2 = 0
      i += 1

  /** Populates array of candidates with names (from command line arguments).
    *
    * @param args
    *   Names of candidates (from command-line arguments.)
    * @param candCt
    *   Number of candidates.
    */
  def populate(args: Array[String], candCt: Int)(using Zone): Unit =
    var i = 0
    while i < candCt do
      val newCand = alloc[Cand](1)
      newCand._1 = toCString(args(i))
      newCand._2 = 0
      newCand._3 = false
      cands(i) = newCand
      i += 1

  /** Repeatedly holds runoff elections until there is a winner.
    *
    * @param voterCt
    *   Number of voters.
    * @param candCt
    *   Number of candidates.
    */
  def repeatRunoff(voterCt: Int, candCt: Int): Unit = boundary:
    while true do               // Keep holding runoffs until winner exists
      tabulate(voterCt, candCt) // Calculate votes given remaining candidates
      val won = printWinner(voterCt, candCt)
      if won then break()
      val min = findMin(voterCt, candCt)
      val tie = isTie(min, candCt)
      if tie then // If tie, everyone wins. print all non-eliminated winners
        var i = 0
        while i < candCt do
          if !cands(i)._3 then printf(c"%s\n", cands(i)._1)
          i += 1
        break()
      eliminate(min, candCt)
      resetVotes(candCt)

  /** Queries the user for votes.
    *
    * @param voterCt
    *   Number of voters.
    * @param candCt
    *   Number of candidates.
    * @return
    *   false if there is an invalid vote, true otherwise.
    */
  def queryVotes(voterCt: Int, candCt: Int)(using Zone): CBool = boundary:
    var i = 0
    while i < voterCt do // Query each voter
      var j = 0
      while j < candCt do // Query for each rank
        printf(c"Rank %i: ", j + 1)
        val name = getString()
        if !vote(i, j, name, candCt) then // Record vote, unless it's invalid
          printf(c"Invalid vote.\n")
          break(false) // breaks out of both while loops
        j += 1
      end while
      printf(c"\n")
      i += 1
    end while
    true // all votes are valid

  /** Runs the runoff election.
    *
    * @param args
    *   Names of the candidates as command line arguments.
    */
  def main(args: Array[String]): Unit = boundary:
    val candCt = args.length

    if candCt < 1 then
      printf(c"Usage: runoff [candidate ...]\n")
      break(EXIT_FAILURE)

    if candCt > MaxCands then
      printf(c"Maximum number of candidates is %i\n", MaxCands)
      break(EXIT_FAILURE)

    val voterCt = getInt(c"Number of voters: ")
    if voterCt > MaxVoters then
      printf(c"Maximum number of voters is %i\n", MaxVoters)
      break(EXIT_FAILURE)

    Zone:
      populate(args, candCt)
      if !queryVotes(voterCt, candCt) then break(EXIT_FAILURE)
      repeatRunoff(voterCt, candCt)

    EXIT_SUCCESS
