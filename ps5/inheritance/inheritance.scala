import scalanative.unsafe.*
import scalanative.libc.stdio.*
import scalanative.posix.time.time
import scalanative.libc.stdlib.*
import scala.util.boundary, boundary.break

object Person:
  // Each person has two parents and two alleles
  // The void pointers have to be cast to Person at call site
  type Parents = CArray[CVoidPtr, Nat._2]
  type Alleles = CArray[CChar, Nat._2]
  type Person  = CStruct2[Parents, Alleles]

  val Generations  = 3
  val IndentLength = 4

  /** Create a new individual with generations.
    *
    * @param generations
    *   Number of generations.
    * @return
    *   Pointer to the new person created.
    */
  def createFamily(generations: CInt)(using Zone): Ptr[Person] =
    val person = alloc[Person](1) // Allocate memory for new person
    if generations > 1 then // If there are still generations left to create
      // Create two new parents for current person
      val parent0 = createFamily(generations - 1)
      val parent1 = createFamily(generations - 1)
      person._1(0) = parent0 // Set parent pointers
      person._1(1) = parent1 // for current person
      person._2(0) = parent0._2.apply(rand() % 2) // Randomly assign person's alleles
      person._2(1) = parent1._2.apply(rand() % 2) // based on the alleles of their parents
    else
      person._1(0) = null         // If there are no generations left to create
      person._1(1) = null         // Set parent pointers to null
      person._2(0) = randomAllele // Randomly assign alleles
      person._2(1) = randomAllele
    person

  /** Print each family member and their alleles.
    *
    * @param person
    *   Pointer to the person we want to print the family of.
    * @param generation
    *   The number of generations of the person's family tree.
    */
  def printFamily(person: Ptr[Person], generation: CInt): Unit = boundary:
    if person == null then break(EXIT_SUCCESS) // Handle base case

    var i      = 0 // Print indentation
    val indent = generation * IndentLength
    while i < indent do
      printf(c" ")
      i += 1

    if generation == 0 then // Print person
      printf(
        c"Child (Generation %i): blood type %c%c\n",
        generation,
        person._2.apply(0),
        person._2.apply(1)
      )
    else if generation == 1 then
      printf(
        c"Parent (Generation %i): blood type %c%c\n",
        generation,
        person._2.apply(0),
        person._2.apply(1)
      )
    else
      i = 0
      while i < generation - 2 do
        printf(c"Great-")
        i += 1
      printf(
        c"Grandparent (Generation %i): blood type %c%c\n",
        generation,
        person._2.apply(0),
        person._2.apply(1)
      )

    // Print parents of current generation
    printFamily(person._1.apply(0).asInstanceOf[Ptr[Person]], generation + 1)
    printFamily(person._1.apply(1).asInstanceOf[Ptr[Person]], generation + 1)

  /** Randomly chooses a blood type allele.
    *
    * @return
    *   `'A'`, `'B'` or `'O'`.
    */
  def randomAllele: CChar = rand() % 3 match
    case 0 => 'A'
    case 1 => 'B'
    case 2 => 'O'

@main
def inheritance: Unit =
  import Person.*
  Zone:
    srand(time(null).toInt)
    val person = createFamily(Generations)
    printFamily(person, 0)
