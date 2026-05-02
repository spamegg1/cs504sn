import scalanative.unsafe.*
import scalanative.unsigned.*
import scalanative.libc.stdio.printf
import scalanative.libc.math.round
import scalanative.libc.stdlib.{EXIT_SUCCESS, EXIT_FAILURE}
import scala.util.boundary, boundary.break

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
    val double = values.foldLeft(0.toUInt)(_ + _).toDouble / values.size
    round(double).toByte.toUByte

  /** Copies the image of given height and width by allocating on the heap.
    *
    * @param height
    *   Height of the image in pixels.
    * @param width
    *   Width of the image in pixels.
    * @param image
    *   The array of pixels holding the RGB values.
    * @return
    *   Pointer to the new copy of the image.
    */
  def copy(height: Int, width: Int, image: Bitmap)(using Zone): Bitmap =
    val imgCopy = alloc[RgbTriple](height * width)
    var row     = 0
    while row < height do
      val rows = row * width
      var col  = 0
      while col < width do
        val index = rows + col
        imgCopy(index)._1 = image(index)._1
        imgCopy(index)._2 = image(index)._2
        imgCopy(index)._3 = image(index)._3
        col += 1
      row += 1
    imgCopy

  /** Converts image (in place) to grayscale by averaging red, green and blue values of each triple.
    *
    * @param height
    *   Height of the image in pixels.
    * @param width
    *   Height of the image in pixels.
    * @param image
    *   The array of pixels holding the RGB values.
    */
  def grayscale(height: Int, width: Int, image: Bitmap): Unit =
    var row = 0
    while row < height do
      val rows = row * width
      var col  = 0
      while col < width do
        val index   = rows + col
        val average = avg(Seq(image(index)._1, image(index)._2, image(index)._3))
        image(index)._1 = average
        image(index)._2 = average
        image(index)._3 = average
        col += 1
      row += 1

  /** Reflects the image (in place) horizontally.
    *
    * @param height
    *   Height of the image in pixels.
    * @param width
    *   Width of the image in pixels.
    * @param image
    *   The array holding the RGB values.
    */
  def reflect(height: Int, width: Int, image: Bitmap): Unit =
    var row = 0
    while row < height do
      val rows = row * width
      var col  = 0
      while col < width / 2 do
        val index = rows + col
        val refl  = rows + width - 1 - col
        val temp  = image(index)
        image(index) = image(refl)
        image(refl) = temp
        col += 1
      row += 1

  /** Applies the Sobel operator for edge detection purposes.
    *
    * @param height
    *   Height of the image in pixels.
    * @param width
    *   Width of the image in pixels.
    * @param image
    *   The array holding the RGB values.
    */
  def edges(height: Int, width: Int, image: Bitmap)(using Zone): Unit =
    val Black = alloc[RgbTriple](1)
    Black._1 = 0.toUByte
    Black._2 = 0.toUByte
    Black._3 = 0.toUByte
    ???

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
  def clamp(row: Int, col: Int, width: Int, height: Int) =
    val prevRow = row * (width - 1)
    val thisRow = row * width
    val nextRow = row * (width + 1)
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

  /** Blurs image by averaging color values of each pixel with its surrounding pixels.
    *
    * @param height
    *   Height of the image in pixels.
    * @param width
    *   Width of the image in pixels.
    * @param img
    *   The array holding the RGB values.
    */
  def blur(height: Int, width: Int, image: Bitmap)(using Zone): Unit =
    val imgCopy = copy(height, width, image)
    var row     = 0
    while row < height do
      val cur = row * width
      var col = 0
      while col < width do
        val index  = cur + col
        val pixels = clamp(row, col, width, height).map(imgCopy.apply(_))
        image(index)._1 = avg(pixels.map(_._1))
        image(index)._2 = avg(pixels.map(_._2))
        image(index)._3 = avg(pixels.map(_._3))
        col += 1
      row += 1
