import scalanative.unsafe.*
import scalanative.unsigned.*
import scalanative.libc.stdio.*
import scalanative.libc.ctype.*
import scalanative.libc.stdlib.{EXIT_SUCCESS, EXIT_FAILURE}
import scalanative.posix.sys.resource.*
import scala.util.boundary, boundary.break

object Speller:
  val DefaultDict = c"dictionaries/large" // Default dictionary

  extension (r: rusage)
    def ru_utime = r._1
    def ru_stime = r._2

  extension (t: timeval)
    def tv_sec  = t._1.toLong.toDouble
    def tv_usec = t._2.toLong.toDouble

  /** Calculates the time difference between two resource usages.
    *
    * @param b
    *   Pointer to an `rusage`.
    * @param a
    *   Pointer to an `rusage`.
    * @return
    *   Time difference, in seconds, between `b` and `a`.
    */
  def calcTimeDiff(b: Ptr[rusage], a: Ptr[rusage]): CDouble =
    if b == null || a == null then 0.0
    else
      (((a.ru_utime.tv_sec * 1000000 + a.ru_utime.tv_usec) -
        (b.ru_utime.tv_sec * 1000000 + b.ru_utime.tv_usec)) +
        ((a.ru_stime.tv_sec * 1000000 + a.ru_stime.tv_usec) -
          (b.ru_stime.tv_sec * 1000000 + b.ru_stime.tv_usec)))
        / 1000000.0

  def main(args: Array[String]): Unit = boundary:
    import Dictionary.MaxLen

    val argc = args.length // Check for correct number of args
    if argc != 1 && argc != 2 then
      printf(c"Usage: ./speller [DICTIONARY] text\n")
      break(EXIT_FAILURE)

    Zone:
      val before     = alloc[rusage](1) // Structures for timing data
      val after      = alloc[rusage](1)
      var time_check = 0.0              // Benchmarks

      // Load selected dictionary (or the default), time how long it takes to load it
      val dictionary = if argc == 2 then toCString(args.head) else DefaultDict
      getrusage(RUSAGE_SELF, before)
      val loaded = Dictionary.loadDict(dictionary)
      getrusage(RUSAGE_SELF, after)
      val time_load = calcTimeDiff(before, after)

      if !loaded then // Exit if dictionary not loaded
        printf(c"Could not load %s.\n", dictionary)
        break(EXIT_FAILURE)

      val text = toCString(if argc == 2 then args(1) else args.head)
      val file = fopen(text, c"r") // Try to open text
      if file == null then
        printf(c"Could not open %s.\n", text)
        break(EXIT_FAILURE)

      // printf(c"\nMISSPELLED WORDS\n\n") // Prepare to report misspell

      var index    = 0 // Prepare to spell-check
      var misspell = 0
      var words    = 0
      val word     = alloc[CChar](MaxLen + 1)

      val c = alloc[CChar](1) // Spell-check each word in text
      while fread(c, sizeof[CChar], 1.toCSize, file) != 0 do
        // Allow only alphabetical characters and apostrophes
        if isalpha(!c) != 0 || (!c == '\'' && index > 0) then
          word(index) = !c // Append character to word
          index += 1
          if index > MaxLen then // Ignore alphabetical strings too long to be words
            // Consume remainder of alphabetical string
            while fread(c, sizeof[CChar], 1.toCSize, file) != 0 && isalpha(!c) != 0 do ()
            index = 0                 // Prepare for new word
        else if isdigit(!c) != 0 then // Ignore words with numbers (like MS Word can)
          // Consume remainder of alphanumeric string
          while fread(c, sizeof[CChar], 1.toCSize, file) != 0 && isalnum(!c) != 0 do ()
          index = 0              // Prepare for new word
        else if index > 0 then   // We must have found a whole word
          word(index) = 0.toByte // Terminate current word
          words += 1             // Update counter

          getrusage(RUSAGE_SELF, before) // Check word's spelling
          var misspelled = !Dictionary.check(word)
          getrusage(RUSAGE_SELF, after)
          time_check += calcTimeDiff(before, after) // Update benchmark

          if misspelled then // Print word if misspelled
            // printf(c"%s\n", word)
            misspell += 1
          index = 0 // Prepare for next word

      if ferror(file) != 0 then // Check whether there was an error
        fclose(file)
        printf(c"Error reading %s.\n", text)
        break(EXIT_FAILURE)

      fclose(file)

      // Calculate dictionary's size and measure how long it takes to do that
      getrusage(RUSAGE_SELF, before)
      val wordCount = Dictionary.getSize
      getrusage(RUSAGE_SELF, after)
      val time_size = calcTimeDiff(before, after)

      val total = time_load + time_check + time_size
      printf(c"\n")
      printf(c"WORDS MISSPELLED:     %d\n", misspell)
      printf(c"WORDS IN DICTIONARY:  %d\n", wordCount)
      printf(c"WORDS IN TEXT:        %d\n", words)
      printf(c"TIME IN load:         %.2f\n", time_load)
      printf(c"TIME IN check:        %.2f\n", time_check)
      printf(c"TIME IN size:         %.2f\n", time_size)
      printf(c"TIME IN TOTAL:        %.2f\n\n", total)
      break(EXIT_SUCCESS)
