import scalanative.unsafe.*
import scalanative.unsigned.*
import scalanative.libc.math.{round, sqrt}

object FilterHelpers:
  import Bmp.*

  /** Averages given color values 0-255.
    *
    * @param values
    *   A sequence of values to be averaged.
    * @return
    *   The average of the values rounded to nearest integer.
    */
  def avg(values: Seq[Byte8]): Byte8 =
    val double = values.map(_.toInt).foldLeft(0)(_ + _).toDouble / values.size
    round(double).toByte.toUByte // .toByte should be safe since avg of 0-255 is 0-255

  /** Applies the Sobel operator to a 3x3 input matrix.
    *
    * @param colors
    *   A sequence of color values 0-255, coming from the 8 neighbors surrounding a pixel.
    * @return
    *   The Sobel filter applied to the color values.
    */
  def sobel(colors: Seq[Byte8]): Byte8 =
    val Seq(a, b, c, d, _, f, g, h, i) = colors.map(_.toInt)
    val gx                             = c + f + f + i - a - d - d - g
    val gy                             = g + h + h + i - a - b - b - c
    val res                            = round(sqrt((gx * gx + gy * gy).toDouble))
    if res > 255 then 255.toUByte else res.toByte.toUByte

  /** Calculates the list of indices of all pixels around a given row,col position, including the position itself. There
    * can be as few as 4 (the corners) and as many as 9 (in the middle, away from all edges).
    *
    * @param row
    *   The row of the current pixel.
    * @param col
    *   The column of the current pixel.
    * @param width
    *   Width of the image.
    * @param height
    *   Height of the image.
    * @return
    *   List of indices of the pixel's surrounding neighbors.
    */
  def clamp(row: Int, col: Int, width: Int, height: Int): Seq[Int] =
    val prevRow = (row - 1) * width
    val thisRow = row * width
    val nextRow = (row + 1) * width
    val i       = thisRow + col
    val p       = prevRow + col
    val n       = nextRow + col
    if row == 0 then                                                // top row
      if col == 0 then Seq(i, i + 1, n, n + 1)                      // top left
      else if col == width - 1 then Seq(i, i - 1, n, n - 1)         // top right
      else Seq(i, i - 1, i + 1, n, n - 1, n + 1)                    // top mid
    else if row == height - 1 then                                  // bottom row
      if col == 0 then Seq(i, i + 1, p, p + 1)                      // bottom left
      else if col == width - 1 then Seq(i, i - 1, p, p - 1)         // bottom right
      else Seq(i, i - 1, i + 1, p, p - 1, p + 1)                    // bottom mid
    else                                                            // mid row
    if col == 0 then Seq(i, i + 1, p, p + 1, n, n + 1)              // mid left
    else if col == width - 1 then Seq(i, i - 1, p, p - 1, n, n - 1) // mid right
    else Seq(i, i - 1, i + 1, p - 1, p, p + 1, n - 1, n, n + 1)     // mid mid

  /** Produces the 9 coordinate pairs surrounding a given pixel's coordinates.
    *
    * @param row
    *   Row of the pixel.
    * @param col
    *   Column of the pixel.
    * @return
    *   `(row, col)` pairs of the 9 cells around the pixel, including the pixel itself.
    */
  def neighbors(row: Int)(col: Int) = Seq(
    (row - 1, col - 1),
    (row - 1, col),
    (row - 1, col + 1),
    (row, col - 1),
    (row, col),
    (row, col + 1),
    (row + 1, col - 1),
    (row + 1, col),
    (row + 1, col + 1)
  )
