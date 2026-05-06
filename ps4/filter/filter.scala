import scalanative.unsafe.*
import scalanative.unsigned.*
import scalanative.libc.stdio.*
import scalanative.libc.stdlib.{EXIT_SUCCESS, EXIT_FAILURE}
import scalanative.libc.math.abs
import scalanative.posix.unistd.{getopt, optind}
import scala.util.boundary, boundary.break

object Filter:
  import Bmp.*

  /** Converts Scala-style command line arguments to C-style.
    *
    * @param args
    *   The `Array[String]` that we normally get from a Scala main method.
    * @return
    *   The `argc` and `argv` that C main methods normally use.
    */
  def convertCliArgs(args: Array[String])(using Zone): (CInt, Ptr[CString]) =
    val argc = args.length + 1
    val argv = alloc[CString](argc)
    argv(0) = c"./main" // fake! :)
    var i = 1
    while i < argc do
      argv(i) = toCString(args(i - 1))
      i += 1
    (argc, argv)

  def main(args: Array[String]): Unit = boundary:
    Zone:
      val (argc, argv) = convertCliArgs(args) // CLI args in C format
      val filters      = c"begr"              // Define allowable filters

      val filter = getopt(argc, argv, filters)
      if filter == '?' then // Get filter flag and check validity
        printf(c"Invalid filter.\n")
        break(EXIT_FAILURE)

      val filter2 = getopt(argc, argv, filters)
      if filter2 != -1 then // Ensure only one filter
        printf(c"Only one filter allowed.\n")
        break(EXIT_FAILURE)

      if argc != optind + 2 then // should be only 2 more args left to process
        printf(c"Usage: ./filter [flag] infile outfile\n")
        break(EXIT_FAILURE)

      val infile  = argv(optind) // Remember filenames
      val outfile = argv(optind + 1)

      val inptr = fopen(infile, c"r")
      if inptr == null then // Open input file
        printf(c"Could not open %s.\n", infile)
        break(EXIT_FAILURE)

      val outptr = fopen(outfile, c"w")
      if outptr == null then // Open output file
        fclose(inptr)
        printf(c"Could not create %s.\n", outfile)
        break(EXIT_FAILURE)

      val fileHeader = alloc[BitmapFileHeader](1) // Read infile's BITMAPFILEHEADER
      fread(fileHeader, sizeof[BitmapFileHeader], 1.toCSize, inptr)

      val infoHeader = alloc[BitmapInfoHeader](1) // Read infile's BITMAPINFOHEADER
      fread(infoHeader, sizeof[BitmapInfoHeader], 1.toCSize, inptr)

      // Ensure infile is (likely) a 24-bit uncompressed BMP 4.0
      if (
          fileHeader._1 != 0x4d42 || // bfType
          fileHeader._5 != 54 ||     // bfOffBits
          infoHeader._1 != 40 ||     // biSize
          infoHeader._5 != 24 ||     // biBitCount
          infoHeader._6 != 0         // biCompression
        )
      then
        fclose(outptr)
        fclose(inptr)
        printf(c"Unsupported file format.\n")
        break(EXIT_FAILURE)

      // Get image's dimensions
      val height = abs(infoHeader._3) // biHeight
      val width  = abs(infoHeader._2) // biWidth
      printf(c"height: %d\n", height)
      printf(c"width: %d\n", width)

      // Allocate memory for image
      val pixels = alloc[RgbTriple](height * width)
      if pixels == null then
        printf(c"Not enough memory to store image.\n")
        fclose(outptr)
        fclose(inptr)
        break(EXIT_FAILURE)
      val bmp = Bitmap(height, width, pixels)

      // Determine padding for scanlines
      val padding = (4 - (width * sizeof[RgbTriple].toInt) % 4) % 4

      var row = 0 // Iterate over infile's scanlines, read rows into pixels
      while row < height do
        fread(pixels, sizeof[RgbTriple], width.toCSize, inptr)
        fseek(inptr, padding, SEEK_CUR) // Skip over padding
        row += 1

      filter match // Filter image
        case 'b' => bmp.blur
        case 'e' => bmp.edges
        case 'g' => bmp.grayscale
        case 'r' => bmp.reflect

      // Write outfile's BITMAPFILEHEADER
      fwrite(fileHeader, sizeof[BitmapFileHeader], 1.toCSize, outptr)

      // Write outfile's BITMAPINFOHEADER
      fwrite(infoHeader, sizeof[BitmapInfoHeader], 1.toCSize, outptr)

      row = 0               // Write new pixels to outfile
      while row < height do // Write row to outfile
        fwrite(pixels, sizeof[RgbTriple], width.toCSize, outptr)
        var pad = 0 // Write padding at end of row
        while pad < padding do
          fputc(0x00, outptr)
          pad += 1
        row += 1

      fclose(inptr) // Close files and return
      fclose(outptr)
      EXIT_SUCCESS
