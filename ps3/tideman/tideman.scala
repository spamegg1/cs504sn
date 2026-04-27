import scalanative.unsafe.*
import scalanative.unsigned.UnsignedRichInt
import scalanative.libc.stdio.printf
import scalanative.libc.stdlib.{EXIT_SUCCESS, EXIT_FAILURE}
import scala.util.boundary, boundary.break

object Tideman:
  val MaxCand = 9                           // max num of candidates
  val MaxPair = (MaxCand - 1) * MaxCand / 2 // max num of pairs of candidates
  var pairCt  = 0
  var candCt  = 0

  type Pair = CStruct2[CInt, CInt] // Each pair has a winner, loser

  val prefs  = Array.ofDim[CInt](MaxCand, MaxCand)  // prefs(i)(j) = # voters who prefer cand i to j
  val locked = Array.ofDim[CBool](MaxCand, MaxCand) // locked(i)(j): cand i is locked in over j
  val cands  = Array.ofDim[CString](MaxCand)
  val pairs  = Array.ofDim[Pair](MaxPair)

  def vote(rank: CInt, name: CString, ranks: Array[CInt]): CBool = false

  def recordPrefs(ranks: Array[CInt]): Unit = ()

  def addPairs: Unit    = ()
  def sortPairs: Unit   = ()
  def lockPairs: Unit   = ()
  def printWinner: Unit = ()

  def pathExists(winner: CInt, loser: CInt): CBool = false

  def main(args: Array[String]): Unit = boundary:
    candCt = args.length
    if candCt < 1 then
      printf(c"Usage: tideman [candidate ...]\n")
      break(EXIT_FAILURE)

    if candCt > MaxCand then
      printf(c"Maximum number of candidates is %i\n", MaxCand)
      break(EXIT_FAILURE)

    val voterCt = getInt(c"Number of voters: ")

    var i = 0 // Clear graph of locked in pairs
    while i < candCt do
      var j = 0
      while j < candCt do
        locked(i)(j) = false
        j += 1
      i += 1

    Zone:
      i = 0 // populate candidates
      while i < candCt do
        cands(i) = toCString(args(i))
        i += 1

    // Query for votes
    i = 0
    while i < voterCt do
      val ranks = Array.ofDim[CInt](candCt) // ranks[i] is voter's ith preference
      var j     = 0                         // Query for each rank
      while j < candCt do
        printf(c"Rank %i: ", j + 1)
        val name = getString()
        if !vote(j, name, ranks) then
          printf(c"Invalid vote.\n")
          break(EXIT_FAILURE)
        j += 1
      end while
      recordPrefs(ranks)
      printf(c"\n")
      i += 1
    end while

    addPairs
    sortPairs
    lockPairs
    printWinner
    break(EXIT_SUCCESS)
