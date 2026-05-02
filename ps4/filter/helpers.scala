import scalanative.unsafe.*
import scalanative.unsigned.*
import scalanative.libc.stdio.printf
import scalanative.libc.math.round
import scalanative.libc.stdlib.{EXIT_SUCCESS, EXIT_FAILURE}
import scala.util.boundary, boundary.break

object FilterHelpers:
  import Bmp.*

  /** Converts image to grayscale by averaging red, green and blue values of each triple.
    *
    * @param height
    *   Height of the image in pixels.
    * @param width
    *   Height of the image in pixels.
    * @param image
    *   The array holding the RGB values.
    */
  def grayscale(height: Int, width: Int, image: Bitmap): Unit =
    var i = 0
    while i < height do
      var j = 0
      while j < width do
        val sum    = image(i)(j)._1 + image(i)(j)._2 + image(i)(j)._3
        val double = sum.toDouble / 3.0
        val avg    = round(double).toByte.toUByte
        image(i)(j)._1 = avg
        image(i)(j)._2 = avg
        image(i)(j)._3 = avg
        j += 1
      i += 1

  /** Reflects the image horizontally.
    *
    * @param height
    *   Height of the image in pixels.
    * @param width
    *   Width of the image in pixels.
    * @param image
    *   The array holding the RGB values.
    */
  def reflect(height: Int, width: Int, image: Bitmap): Unit =
    var i = 0
    while i < height do
      var j = 0
      while j < width / 2 do
        val temp = image(i)(j)
        image(i)(j) = image(i)(width - 1 - j)
        image(i)(width - 1 - j) = temp
        j += 1
      i += 1

  def edges(height: Int, width: Int, image: Bitmap)(using Zone): Unit =
    val Black = alloc[RgbTriple](1)
    Black._1 = 0.toUByte
    Black._2 = 0.toUByte
    Black._3 = 0.toUByte
    ???

  def blur(height: Int, width: Int, image: Bitmap): Unit = ???
