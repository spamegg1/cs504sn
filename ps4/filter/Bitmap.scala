import scalanative.unsafe.*
import scalanative.unsigned.*

import Bmp.*

/** Convenient class to make it easier to work with two dimensional arrays.
  *
  * @param height
  *   Height of the image in pixels.
  * @param width
  *   Width of the image in pixels.
  * @param pixels
  *   The 2D array of pixels holding the RGB triples.
  */
case class Bitmap(height: Int, width: Int, pixels: Pixels):
  /** Convenient getter for a pixel in the image by row and column.
    *
    * @param row
    *   The row of the pixel in the image we want to access.
    * @param col
    *   The column of the pixel in the image we want to access.
    * @return
    *   The pixel at given row and column.
    */
  def at(row: Int)(col: Int): RgbTriple = pixels(row * width + col)

  /** Convenient setter for a pixel in the image by row and column.
    *
    * @param row
    *   The row of the pixel in the image we want to update.
    * @param col
    *   The column of the pixel in the image we want to update.
    * @param rgb
    *   The new pixel value we want to set.
    */
  def update(row: Int)(col: Int)(rgb: RgbTriple): Unit =
    val index = row * width + col
    pixels(index)._1 = rgb._1
    pixels(index)._2 = rgb._2
    pixels(index)._3 = rgb._3

  /** Convenient setter for a pixel in the image by row and column, when we want to set the RGB values of the pixel to
    * the same value obtained by applying a function only to this pixel's RGB values.
    *
    * @param row
    *   The row of the pixel in the image we want to update.
    * @param col
    *   The column of the pixel in the image we want to update.
    */
  def updatePixel(row: Int)(col: Int)(fn: Seq[Byte8] => Byte8): Unit =
    val index = row * width + col
    val pixel = pixels(index)
    val value = fn(Seq(pixel._1, pixel._2, pixel._3))
    pixels(index)._1 = value
    pixels(index)._2 = value
    pixels(index)._3 = value

  /** Convenient setter for a pixel in the image by row and column, when we want to set the red, green, blue values of
    * the pixel by some function applied to its surrounding neighbors in the image.
    *
    * @param row
    *   The row of the pixel in the image we want to update.
    * @param col
    *   The column of the pixel in the image we want to update.
    * @param neighbors
    *   A sequence of RGB triples surrounding the pixel in the image.
    * @param fn
    *   Some computation on the neighbors (such as averaging, or Sobel operator).
    */
  def updateTriples(row: Int)(col: Int)(neighbors: Seq[RgbTriple])(fn: Seq[Byte8] => Byte8): Unit =
    val index = row * width + col
    pixels(index)._1 = fn(neighbors.map(_._1))
    pixels(index)._2 = fn(neighbors.map(_._2))
    pixels(index)._3 = fn(neighbors.map(_._3))

  /** Copies the image by allocating a copy on the heap.
    *
    * @return
    *   New instance containing the copied image.
    */
  def copy(using Zone): Bitmap =
    val imgCopy = alloc[RgbTriple](height * width)
    val newCopy = Bitmap(height, width, imgCopy)
    var row     = 0
    while row < height do
      var col = 0
      while col < width do
        newCopy.update(row)(col)(at(row)(col))
        col += 1
      row += 1
    newCopy

  /** Creates a copy of the image with black pixels added around the 4 edges. The resulting image is 2 pixels larger in
    * both height and width.
    *
    * @return
    *   New instance containing the black-edged copy of the image.
    */
  def copyWithBlackEdges(using Zone): Bitmap =
    val Black = alloc[RgbTriple](1) // pure black pixel
    Black._1 = 0.toUByte
    Black._2 = 0.toUByte
    Black._3 = 0.toUByte

    val imgCopy = alloc[RgbTriple]((height + 2) * (width + 2))
    val newCopy = Bitmap(height + 2, width + 2, imgCopy)

    var col = 0
    while col < width + 2 do
      newCopy.update(0)(col)(Black)          // top edge all black
      newCopy.update(height + 1)(col)(Black) // bottom edge all black
      col += 1

    var row = 0
    while row < height + 2 do
      newCopy.update(row)(0)(Black)         // left edge all black
      newCopy.update(row)(width + 1)(Black) // right edge all black
      row += 1

    row = 1 // insides of the black edges are the same as image
    while row < height + 1 do
      col = 1
      while col < width + 1 do
        newCopy.update(row)(col)(at(row - 1)(col - 1))
        col += 1
      row += 1

    newCopy

  /** Reflects the image (in place) horizontally. */
  def reflect: Unit =
    var row = 0
    while row < height do
      var col = 0
      while col < width / 2 do
        val temp = at(row)(col)
        update(row)(col)(at(row)(width - 1 - col))
        update(row)(width - 1 - col)(temp)
        col += 1
      row += 1

  /** Converts image (in place) to grayscale by averaging red, green and blue values of each triple. */
  def grayscale: Unit =
    var row = 0
    while row < height do
      var col = 0
      while col < width do
        updatePixel(row)(col)(FilterHelpers.avg)
        col += 1
      row += 1

  /** Blurs image (in-place) by averaging color values of each pixel with its surrounding pixels. */
  def blur(using Zone): Unit =
    val imgCopy = copy
    var row     = 0
    while row < height do
      var col = 0
      while col < width do
        val neighbors = FilterHelpers
          .clamp(row, col, width, height)
          .map(imgCopy.pixels.apply(_))
        updateTriples(row)(col)(neighbors)(FilterHelpers.avg)
        col += 1
      row += 1

  /** Applies the Sobel operator for edge detection purposes. */
  def edges(using Zone): Unit =
    val imgCopy = copyWithBlackEdges // h+2 x w+2 copy with black edges
    var row     = 1
    while row < height + 1 do
      var col = 1
      while col < width + 1 do
        val neighbors = Seq(
          imgCopy.at(row - 1)(col - 1),
          imgCopy.at(row - 1)(col),
          imgCopy.at(row - 1)(col + 1),
          imgCopy.at(row)(col - 1),
          imgCopy.at(row)(col),
          imgCopy.at(row)(col + 1),
          imgCopy.at(row + 1)(col - 1),
          imgCopy.at(row + 1)(col),
          imgCopy.at(row + 1)(col + 1)
        )
        updateTriples(row)(col)(neighbors)(FilterHelpers.sobel)
        col += 1
      row += 1
