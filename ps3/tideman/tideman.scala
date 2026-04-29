import scalanative.unsafe.*
import scalanative.unsigned.UnsignedRichInt
import scalanative.libc.stdio.printf
import scalanative.libc.string.strcmp
import scalanative.libc.stdlib.{EXIT_SUCCESS, EXIT_FAILURE}
import scala.util.boundary, boundary.break

object Tideman:
  val MaxCand = 9                           // max num of candidates
  val MaxPair = (MaxCand - 1) * MaxCand / 2 // max num of pairs of candidates
  var pairCt  = 0

  type Pair = CStruct2[CInt, CInt] // Each pair has a winner, loser

  val prefs  = Array.ofDim[CInt](MaxCand, MaxCand)  // prefs(i)(j) = # voters who prefer cand i to j
  val locked = Array.ofDim[CBool](MaxCand, MaxCand) // locked(i)(j): cand i is locked in over j
  val cands  = Array.ofDim[CString](MaxCand)
  val pairs  = Array.ofDim[Pair](MaxPair)

  /** Updates ranks, given a new vote.
    *
    * @param candCt
    *   Number of candidates.
    * @param rank
    *   The rank to be updated.
    * @param name
    *   Name of candidate.
    * @param ranks
    *   List of preferred candidates of current voter.
    * @return
    *   true if vote is valid (candidate exists), false if invalid (candidate does not exist).
    */
  def vote(candCt: Int, rank: CInt, name: CString, ranks: Array[CInt]): CBool = boundary:
    var i = 0
    while i < candCt do
      if strcmp(name, cands(i)) == 0 then
        ranks(rank) = i
        break(true)
      i += 1
    false

  /** Records the ranked preferences of a voter.
    *
    * @param candCt
    *   Number of candidates.
    * @param ranks
    *   The ranks of one voter.
    */
  def recordPrefs(candCt: Int, ranks: Array[CInt]): Unit =
    var i = 0
    while i < candCt - 1 do
      prefs(i)(i) = 0
      var j = i + 1
      while j < candCt do
        prefs(ranks(i))(ranks(j)) += 1
        j += 1
      i += 1

  /** Records pairs of candidates to the pairs array based on the preferences matrix.
    *
    * @param candCt
    *   Number of candidates.
    */
  def addPairs(candCt: Int)(using Zone): Unit =
    var i = 0
    while i < candCt - 1 do
      var j = i + 1
      while j < candCt do
        val prefIJ  = prefs(i)(j)
        val prefJI  = prefs(j)(i)
        val newPair = alloc[Pair](1)

        if prefIJ > prefJI then
          newPair._1 = i // winner
          newPair._2 = j // loser
          pairs(pairCt) = newPair
          pairCt += 1
        if prefIJ < prefJI then
          newPair._1 = j // winner
          newPair._2 = i // loser
          pairs(pairCt) = newPair
          pairCt += 1
        j += 1
      i += 1

  /** Sorts pairs in decreasing order by strength of victory. */
  def sortPairs(using Zone): Unit =
    var i = 0
    while i < pairCt - 1 do
      var j = i + 1
      while j < pairCt do
        val (ith, jth) = (pairs(i), pairs(j))
        val strI       = prefs(ith._1)(ith._2)
        val strJ       = prefs(jth._1)(jth._2)
        if strI < strJ then
          val temp = ith
          pairs(i) = jth
          pairs(j) = temp
        j += 1
      i += 1

  /** Locks pairs into the candidate graph in order, without creating cycles.
    *
    * @param candCt
    *   Number of candidates.
    */
  def lockPairs(candCt: Int): Unit =
    var i = 0
    while i < pairCt do
      // assume pairs is sorted by decreasing strength of victory
      val pr = pairs(i)
      if !pathExists(candCt, pr._1, pr._2) then locked(pr._1)(pr._2) = true
      i += 1

  /** Prints the winner of the election.
    *
    * @param candCt
    *   Number of candidates.
    */
  def printWinner(candCt: Int): Unit =
    var winner = 0
    var flag   = true
    boundary:
      while winner < candCt do
        var i = 0
        boundary:
          while i < candCt do
            if locked(i)(winner) then
              flag = false
              break()
            i += 1
        if flag then break()
        else
          winner += 1
          flag = true
    printf(c"%s\n", cands(winner))

  /** Checks if a directed path exists from loser to winner.
    *
    * @param winner
    *   Number of candidates.
    * @param winner
    *   Number of a candidate.
    * @param loser
    *   Number of a candidate.
    * @return
    *   true if there is a path from loser to winner, false otherwise.
    */
  def pathExists(candCt: Int, winner: CInt, loser: CInt): CBool = boundary:
    var i = 0
    while i < candCt do
      if locked(loser)(i) && (i == winner || pathExists(candCt, winner, i)) then break(true)
      i += 1
    false

  /** Clears graph of locked in pairs.
    *
    * @param candCt
    *   Number of candidates.
    */
  def clearLocked(candCt: Int): Unit =
    var i = 0
    while i < candCt do
      var j = 0
      while j < candCt do
        locked(i)(j) = false
        j += 1
      i += 1

  /** Populates the candidates.
    *
    * @param candCt
    *   Number of candidates.
    * @param args
    *   Names of the candidates (as command line arguments).
    */
  def populate(candCt: Int, args: Array[String])(using Zone): Unit =
    var i = 0
    while i < candCt do
      cands(i) = toCString(args(i))
      i += 1

  /** Queries the user for votes.
    *
    * @param voterCt
    *   Number of voters.
    * @param candCt
    *   Number of candidates.
    * @return
    *   true if all votes are valid, false if there is at least one invalid vote.
    */
  def queryVotes(voterCt: Int, candCt: Int)(using Zone): CBool = boundary:
    val ranks = Array.ofDim[CInt](candCt) // ranks(i) is voter's ith preference
    var i     = 0
    while i < voterCt do // Query for votes
      var j = 0
      while j < candCt do // Query for each rank
        printf(c"Rank %i: ", j + 1)
        val name = getString()
        if !vote(candCt, j, name, ranks) then
          printf(c"Invalid vote.\n")
          break(false)
        j += 1
      recordPrefs(candCt, ranks)
      printf(c"\n")
      i += 1
    end while
    true

  /** Runs the tideman election.
    *
    * @param args
    *   Names of candidates as command line arguments.
    */
  def main(args: Array[String]): Unit = boundary:
    val candCt = args.length
    if candCt < 1 then
      printf(c"Usage: tideman [candidate ...]\n")
      break(EXIT_FAILURE)

    if candCt > MaxCand then
      printf(c"Maximum number of candidates is %i\n", MaxCand)
      break(EXIT_FAILURE)

    val voterCt = getInt(c"Number of voters: ")
    clearLocked(candCt)

    Zone:
      populate(candCt, args)
      if !queryVotes(voterCt, candCt) then break(EXIT_FAILURE)
      addPairs(candCt)
      sortPairs
      lockPairs(candCt)
      printWinner(candCt)
      break(EXIT_SUCCESS)
