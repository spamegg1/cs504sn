import scalanative.unsafe.{CQuote, stackalloc, Ptr, CLong}
import scalanative.libc.stdio.{printf, sscanf, stdin, fgets}
import scala.util.boundary, boundary.break

/** Reads input from the user and parses it to a long. If the input is not a long, throws `IllegalArgumentException`.
  *
  * @return
  *   The long value inputted by the user.
  */
def getLong: Long =
  val line: Ptr[Byte]    = stackalloc[Byte](1024)
  val longPtr: Ptr[Long] = stackalloc[Long](1)
  boundary:
    while fgets(line, 1024 - 1, stdin) != null do
      val scanResult = sscanf(line, c"%lld\n", longPtr)
      if scanResult == 0 then throw java.lang.IllegalArgumentException("parse error in sscanf, expected long")
      else break()
  !longPtr

/** Reads input from the user for the credit card number and parses it to a nonnegative long.
  *
  * @return
  *   The nonnegative long value inputted by the user.
  */
def getCC: Long =
  var cc = -1L
  while cc < 0L do
    printf(c"Number: ")
    cc = getLong
  cc

/** Calculates the checksum of a credit card number via Luhn's algorithm.
  *
  * @param digits
  *   The digits of the card number (ordered right-to-left).
  * @param len
  *   The length of the card number (13, 15 or 16).
  * @return
  *   The checksum of the card number via Luhn's algorithm.
  */
def checkSum(digits: Seq[Int], len: Int): Int =
  require(13 <= len)
  var sum = 0
  (1 to 11 by 2).foreach: i =>
    sum += (digits(i) * 2) % 10
    sum += (digits(i) * 2) / 10
  if len == 15 || len == 16 then
    sum += (digits(13) * 2) % 10
    sum += (digits(13) * 2) / 10
  if len == 16 then
    sum += (digits(15) * 2) % 10
    sum += (digits(15) * 2) / 10
  (0 to 12 by 2).foreach: i =>
    sum += digits(i)
  if len == 15 || len == 16 then sum += digits(14)
  sum

/** Prints the type of card (AMEX, VISA or MASTERCARD).
  *
  * @param len
  *   Length of the card number (13, 15 or 16).
  * @param firstDigit
  *   The first (leftmost) digit of the card number.
  */
def printCardType(len: Int, firstDigit: Int): Unit = len match
  case 13 => printf(c"VISA\n")
  case 15 => printf(c"AMEX\n")
  case 16 => if firstDigit == 4 then printf(c"VISA\n") else printf(c"MASTERCARD\n")

/** Gets credit card number as input from user, and prints the type of credit card. */
@main
def credit: Unit =
  val ValidLengths = List(13, 15, 16)
  val Amex         = List(34, 37)
  val MasterVisa   = List(51, 52, 53, 54, 55)

  val cc: Long       = getCC
  val ccStr          = cc.toString
  val digits         = ccStr.map(_.asDigit).reverse
  val firstDigit     = ccStr.head.asDigit
  val firstTwoDigits = ccStr.take(2).toInt
  val len            = digits.length

  boundary:
    if !ValidLengths.contains(len) then
      printf(c"INVALID\n")
      break()

    len match
      case 15 =>
        if !Amex.contains(firstTwoDigits) then
          printf(c"INVALID\n")
          break()
      case 16 =>
        if !MasterVisa.contains(firstTwoDigits) && firstDigit != 4 then
          printf(c"INVALID\n")
          break()
      case 13 => // VISA
        if firstDigit != 4 then
          printf(c"INVALID\n")
          break()

    if checkSum(digits, len) % 10 != 0 then
      printf(c"INVALID\n")
      break()
    printCardType(len, firstDigit)
