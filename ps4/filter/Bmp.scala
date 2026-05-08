import scalanative.unsafe.*
import scalanative.unsigned.*
import scalanative.libc.stdlib.malloc
import scalanative.libc.stdint.{uint16_t, uint32_t, int32_t}

object Bmp:
  /** Common Data Types
    *
    * The data types in this section are essentially aliases for C/C++ primitive data types.
    *
    * Adapted from http://msdn.microsoft.com/en-us/library/cc230309.aspx. See http://en.wikipedia.org/wiki/Stdint.h for
    * more on stdint.h.
    */
  type Byte8  = UByte // uint8_t not available in libc.stdint
  type Word   = uint16_t
  type DWord  = uint32_t
  type Long32 = int32_t

  /** The BITMAPFILEHEADER structure contains information about the type, size, and layout of a file that contains a DIB
    * [device-independent bitmap].
    *
    * Adapted from http://msdn.microsoft.com/en-us/library/dd183374(VS.85).aspx.
    */
  type BitmapFileHeader = Ptr[Byte]

  /** Contains field getters and some convenience methods for the custom struct `BitmapFileHeader`. */
  object BitmapFileHeader:
    private val offset1 = sizeof[Word]
    private val offset2 = offset1 + sizeof[DWord]
    private val offset3 = offset2 + sizeof[Word]
    private val offset4 = offset3 + sizeof[Word]

    /** Total size of a `BitmapFileHeader`. */
    val size = offset4 + sizeof[DWord]

    /** Allocates memory on the heap for 1 file header struct. Has to be freed manually.
      *
      * @return
      *   Pointer to the file header struct allocated (or `null` if failed).
      */
    def alloc = malloc(size).asInstanceOf[BitmapFileHeader]

    /** Field getters (using pointer arithmetic and casts). */
    extension (bmpFh: BitmapFileHeader)
      def bfType      = bmpFh.asInstanceOf[Ptr[Word]]
      def bfSize      = (bmpFh + offset1).asInstanceOf[Ptr[DWord]]
      def bfReserved1 = (bmpFh + offset2).asInstanceOf[Ptr[Word]]
      def bfReserved2 = (bmpFh + offset3).asInstanceOf[Ptr[Word]]
      def bfOffBits   = (bmpFh + offset4).asInstanceOf[Ptr[DWord]]

  /** The BITMAPINFOHEADER structure contains information about the dimensions and color format of a DIB
    * [device-independent bitmap].
    *
    * Adapted from http://msdn.microsoft.com/en-us/library/dd183376(VS.85).aspx.
    */
  type BitmapInfoHeader = Ptr[Byte]

  /** Contains field getters and some convenience methods for the custom struct `BitmapInfoHeader`. */
  object BitmapInfoHeader:
    private val offset1  = sizeof[DWord]
    private val offset2  = offset1 + sizeof[Long32]
    private val offset3  = offset2 + sizeof[Long32]
    private val offset4  = offset3 + sizeof[Word]
    private val offset5  = offset4 + sizeof[Word]
    private val offset6  = offset5 + sizeof[DWord]
    private val offset7  = offset6 + sizeof[DWord]
    private val offset8  = offset7 + sizeof[Long32]
    private val offset9  = offset8 + sizeof[Long32]
    private val offset10 = offset9 + sizeof[DWord]

    /** Total size of a `BitmapInfoHeader`. */
    val size = offset10 + sizeof[DWord]

    /** Allocates memory on the heap for 1 info header struct. Has to be freed manually.
      *
      * @return
      *   Pointer to the info header struct allocated (or `null` if failed).
      */
    def alloc = malloc(size).asInstanceOf[BitmapInfoHeader]

    /** Field getters for `BitmapInfoHeader` (using pointer arithmetic and casts). */
    extension (bmpIh: BitmapInfoHeader)
      def biSize          = bmpIh.asInstanceOf[Ptr[DWord]]
      def biWidth         = (bmpIh + offset1).asInstanceOf[Ptr[Long32]]
      def biHeight        = (bmpIh + offset2).asInstanceOf[Ptr[Long32]]
      def biPlanes        = (bmpIh + offset3).asInstanceOf[Ptr[Word]]
      def biBitCount      = (bmpIh + offset4).asInstanceOf[Ptr[Word]]
      def biCompression   = (bmpIh + offset5).asInstanceOf[Ptr[DWord]]
      def biSizeImage     = (bmpIh + offset6).asInstanceOf[Ptr[DWord]]
      def biXPelsPerMeter = (bmpIh + offset7).asInstanceOf[Ptr[Long32]]
      def biYPelsPerMeter = (bmpIh + offset8).asInstanceOf[Ptr[Long32]]
      def biClrUsed       = (bmpIh + offset9).asInstanceOf[Ptr[DWord]]
      def biClrImportant  = (bmpIh + offset10).asInstanceOf[Ptr[DWord]]

  /** This structure describes a color consisting of relative intensities of red, green, and blue.
    *
    * Adapted from http://msdn.microsoft.com/en-us/library/aa922590.aspx.
    */
  type RgbTriple = CStruct3[Byte8, Byte8, Byte8] // blue, green, red

  extension (rgb: RgbTriple)
    /** Convenient setter method to update an RGB triple.
      *
      * @param that
      *   Another RGB triple whose fields we want to copy into this.
      */
    def set(that: RgbTriple): Unit =
      rgb._1 = that._1
      rgb._2 = that._2
      rgb._3 = that._3

  /** Convenient shorthand for a rectangle of RGB values. */
  type Pixels = Ptr[RgbTriple]
