import scalanative.unsafe.*
import scalanative.unsigned.{UnsignedRichInt, USize}
import scalanative.libc.string.strlen
import scalanative.libc.math.round
import munit.FunSuite

class ReadabilityTest extends FunSuite:
  /** Helper to read text from files into strings.
    *
    * @param file
    *   The path to the file.
    * @return
    *   Contents of the file as a string.
    */
  def fileToText(file: os.Path)(using Zone): CString =
    val lines = os.read.lines(file).mkString(" ")
    toCString(lines)

  test("grade and getScore calculate the grade level of various texts correctly"):
    val path  = os.pwd / "test" / "ps2"
    val files = Seq(
      "grade0.txt",
      "grade2.txt",
      "grade3.txt",
      "grade5.txt",
      "grade7.txt",
      "grade8-1.txt",
      "grade8-2.txt",
      "grade9.txt",
      "grade10.txt",
      "grade16plus.txt"
    )
    val results = Seq(0, 2, 3, 5, 7, 8, 8, 9, 10, 16)
    Zone:
      val texts  = files.map(file => fileToText(path / file))
      val grades = texts.map(text => grade(getScore(text)))
      grades
        .zip(results)
        .map: (obtained, expected) =>
          assertEquals(obtained, expected)
