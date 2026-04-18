import scalanative.unsafe.CQuote
import scalanative.libc.stdio.printf

/** Gets input from the user for the height of the pyramid Mario has to jump over.
  *
  * @return
  *   The integer height (between 1 and 8 inclusive) the user entered.
  */
def getHeight: Int =
  var height = 0
  while height < 1 || height > 8 do height = getInt(c"Height: ")
  height

/** Prints a pyramid of given height.
  *
  * @param height
  *   An integer between 1 and 8, inclusive.
  */
def printPyramid(height: Int): Unit =
  var hashes = 1
  var spaces = height - hashes

  for i <- 0 until height do
    for j <- 0 until spaces do printf(c" ") // left spaces
    for j <- 0 until hashes do printf(c"#") // left hashes
    printf(c"  ") // the gaps
    for j <- 0 until hashes do printf(c"#") // right hashes
    printf(c"\n"); // there are no right spaces, go to next line
    hashes += 1
    spaces = height - hashes

@main
def mario: Unit = printPyramid(getHeight)
