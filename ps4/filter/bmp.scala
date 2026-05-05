import scalanative.unsafe.*
import scalanative.unsigned.*
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
  type BitmapFileHeader = CStruct5[
    Word,  // bfType
    DWord, // bfSize
    Word,  // bfReserved1
    Word,  // bfReserved2
    DWord  // bfOffBits
  ]

  /** The BITMAPINFOHEADER structure contains information about the dimensions and color format of a DIB
    * [device-independent bitmap].
    *
    * Adapted from http://msdn.microsoft.com/en-us/library/dd183376(VS.85).aspx.
    */
  type BitmapInfoHeader = CStruct11[
    DWord,  // biSize
    Long32, // biWidth
    Long32, // biHeight
    Word,   // biPlanes
    Word,   // biBitCount
    DWord,  // biCompression
    DWord,  // biSizeImage
    Long32, // biXPelsPerMeter
    Long32, // biYPelsPerMeter
    DWord,  // biClrUsed
    DWord   // biClrImportant
  ]

  /** This structure describes a color consisting of relative intensities of red, green, and blue.
    *
    * Adapted from http://msdn.microsoft.com/en-us/library/aa922590.aspx.
    */
  type RgbTriple = CStruct3[Byte8, Byte8, Byte8] // blue, green, red

  /** Convenient shorthand for a rectangle of RGB values. */
  type Pixels = Ptr[RgbTriple]
