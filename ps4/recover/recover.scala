import scalanative.unsafe.*
import scalanative.unsigned.*
import scalanative.libc.stdio.*
import scalanative.libc.stdlib.{EXIT_SUCCESS, EXIT_FAILURE}
import scala.util.boundary, boundary.break

object Recover:
  def main(args: Array[String]): Unit = boundary:
    val argc = args.length
    if argc != 1 then // ensure proper usage
      fprintf(stderr, c"Usage: recover rawfile\n")
      break(EXIT_FAILURE)

    Zone:
      val infile = toCString(args.head) // remember filename
      val inptr  = fopen(infile, c"r")  // open input file
      if inptr == null then
        fprintf(stderr, c"Could not open %s.\n", infile)
        break(EXIT_FAILURE)

      var jpegCount   = -1 // Count the number of JPGs found
      var headerCheck = 0  // Detects header block
      var jpegCheck   = 0  // Detects non-header jpeg block

      val buffer         = alloc[UByte](512) // Declare buffer to read raw file into
      val filename       = alloc[CChar](8)   // Create output file
      var img: Ptr[FILE] = null

      // This is the main loop for finding and writing JPEGs
      while fread(buffer, 1.toCSize, 512.toCSize, inptr) == 512 do
        // Check if we are at the start of a new JPEG
        if (
            buffer(0) == 0xff &&
            buffer(1) == 0xd8 &&
            buffer(2) == 0xff &&
            (buffer(3) & 0xf0.toUByte) == 0xe0 // bitwise addition
          )
        then
          headerCheck = 1 // JPEG Header block found
          jpegCount += 1  // Increment JPEG count
          // Check if we have already found a JPEG before
          // This means we are at the end of a JPEG and the start of another JPEG
          if jpegCheck == 1 then
            fclose(img)                               // We need to close previous JPEG file,
            jpegCheck = 0                             // Reset jpegCheck to 0
            sprintf(filename, c"%03i.jpg", jpegCount) // name the new JPEG
            img = fopen(filename, c"w")               // Open a new file with that name
          else                                        // We found our very FIRST JPEG
            sprintf(filename, c"%03i.jpg", jpegCount) // Name the first JPEG as "000.jpg"
            img = fopen(filename, c"w")               // Open a new file with that name

        if headerCheck == 1 then // If we are NOT at the start of a new JPEG
          // The current 512 bytes belong to the currently opened JPEG
          // So we are in the middle of a JPEG
          jpegCheck = 1
          fwrite(buffer, 1.toCSize, 512.toCSize, img) // so keep writing into it
      end while

      // Now close all remaining files
      fclose(img)
      fclose(inptr)
      break(EXIT_SUCCESS) // success
