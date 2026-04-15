import scalanative.unsafe.{CQuote, stackalloc, Ptr, CInt}
import scalanative.libc.stdio.{printf, sscanf, stdin, fgets}
import scala.util.boundary, boundary.break

/** Reads input from the user and parses it to an integer. If the input is not an integer, throws
  * `IllegalArgumentException`.
  *
  * @return
  *   The integer value inputted by the user.
  */
def getInt: Int =
  val line: Ptr[Byte]      = stackalloc[Byte](1024)
  val intPointer: Ptr[Int] = stackalloc[Int](1)
  boundary:
    while fgets(line, 1024 - 1, stdin) != null do
      val scanResult: CInt = sscanf(line, c"%d\n", intPointer)
      if scanResult == 0 then throw java.lang.IllegalArgumentException("parse error in sscanf, expected integer")
      else break()
  !intPointer

/** Reads input from the user for the change owed and parses it to a nonnegative integer.
  *
  * @return
  *   The nonnegative integer value inputted by the user.
  */
def getCents: Int =
  var cents = -1
  while cents < 0 do
    printf(c"Change owed: ")
    cents = getInt
  cents

/** Calculates the number of coins for change owed.
  *
  * @param cents
  *   The number of cents the customer is owed.
  * @return
  *   The minimum number of coins (quarters, dimes, nickels, pennies) to be returned to customer.
  */
def getCoins(cents: Int): Int =
  val quarters = cents / 25            // Calculate how many quarters to give the customer
  val change1  = cents - quarters * 25 // Remove quarters
  val dimes    = change1 / 10          // Calculate how many dimes to give the customer
  val change2  = change1 - dimes * 10  // Remove dimes
  val nickels  = change2 / 5           // Calculate how many nickels to give the customer
  val pennies  = change2 - nickels * 5 // Remove nickels, what's left are pennies
  quarters + dimes + nickels + pennies

@main
def cash: Unit =
  val cents = getCents        // Ask how many cents the customer is owed
  val coins = getCoins(cents) // total number of coins to give the customer
  printf(c"%i\n", coins)
