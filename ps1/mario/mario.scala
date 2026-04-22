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

  var i = 0
  while i < height do
    var j = 0
    while j < spaces do
      printf(c" ") // left spaces
      j += 1

    j = 0
    while j < hashes do
      printf(c"#") // left hashes
      j += 1

    printf(c"  ") // the gaps

    j = 0
    while j < hashes do
      printf(c"#") // right hashes
      j += 1

    printf(c"\n"); // there are no right spaces, go to next line

    hashes += 1
    spaces = height - hashes
    i += 1

@main
def mario: Unit = printPyramid(getHeight)
