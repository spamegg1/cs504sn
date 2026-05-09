import scalanative.unsafe.*
import scalanative.unsigned.*
import scalanative.libc.stdio.*
import scalanative.libc.stdint.*
import scalanative.libc.stdlib.{EXIT_SUCCESS, EXIT_FAILURE, atof}
import scala.util.boundary, boundary.break

object Volume:
  val HeaderSize = 44.toCSize // Number of bytes in .wav header

  def main(args: Array[String]): Unit = boundary:
    val argc = args.length
    if argc != 3 then // Check command-line arguments
      printf(c"Usage: ./volume input.wav output.wav factor\n")
      break(EXIT_FAILURE)

    Zone:
      // Open files and determine scaling factor
      val input: Ptr[FILE] = fopen(toCString(args.head), c"r")
      if input == null then
        printf(c"Could not open file.\n")
        break(EXIT_FAILURE)

      val output: Ptr[FILE] = fopen(toCString(args(1)), c"w")
      if output == null then
        printf(c"Could not open file.\n")
        break(EXIT_FAILURE)

      val factor = atof(toCString(args(2)))

      // Copy header from input file to output file
      val header = alloc[UByte](HeaderSize)
      fread(header, HeaderSize, 1.toCSize, input)
      fwrite(header, HeaderSize, 1.toCSize, output)

      // Read samples from input file and write updated data to output file
      val buffer = alloc[int16_t](1) // Create a buffer for a single sample

      // Read single sample from input into buffer while there are samples left to read
      while fread(buffer, sizeof[int16_t], 1.toCSize, input) != 0 do
        val double = (!buffer).toDouble * factor
        !buffer = double.toShort                           // Update volume of sample
        fwrite(buffer, sizeof[int16_t], 1.toCSize, output) // Write updated sample to new file

      fclose(input) // Close files
      fclose(output)
      EXIT_SUCCESS
