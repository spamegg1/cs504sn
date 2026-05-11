import scalanative.unsafe.*
import scalanative.unsigned.*
import scalanative.libc.string.strlen
import scalanative.libc.ctype.*
import scalanative.libc.stdio.*
import scalanative.libc.stdlib.{EXIT_SUCCESS, EXIT_FAILURE}
import scala.util.boundary, boundary.break

object Dictionary:
  import Node.*

  val MaxLen   = 45 // Maximum length for a word
  val AlphSize = 27 // Size of our alphabet: letters a-z plus the apostrophe
  val Apostr   = 26 // Apostrophe is designated position 26 by our choice
  val ASCII_a  = 97 // Subtract magic number 97, so that a is 0, z is 25

  /** Variable to keep track of dictionary's word count in loadDict */
  var wordCount = 0

  /** This is the pointer (root) to the top of the dictionary trie structure. Will be initialized and used in dictionary
    * (in load and unload functions). It was necessary to declare it here in order to make unload recursive because
    * unload takes no parameters and we are not allowed to change it.
    */
  var root: Ptr[Node] = null

  /** Checks if word is in dictionary. Assumes that dictionary is already loaded.
    *
    * @param word
    *   The word we want to look up in the dictionary.
    * @return
    *   true if word is in dictionary, false otherwise.
    */
  def check(word: Ptr[CChar]): CBool = boundary:
    val len = strlen(word) // Find length of word
    var pos = -1           // denotes position of character in alphabet (0-26)

    // Keep track of current node traversing the trie structure, starting at top
    var current: Ptr[Node] = root

    // For each character in word, find corresponding element in children[]
    var i = 0
    while i < len.toInt do
      // Find location of character tolower(word[i]) in children[]
      // This is the same as in load(), except case insensitive
      pos = if tolower(word(i)) == '\'' then Apostr else tolower(word(i)) - ASCII_a

      // If children[pos] is NULL, word is not in dictionary, so MISSPELLED
      // Otherwise, move on to the next character in word
      // Update current node, traversing down, for next letter in word
      if current.children(pos) == null then break(false)
      else current = current.children(pos).asInstanceOf[Ptr[Node]]
      i += 1

    current.is_word // If we are at the end of the input word, and is_word is true

  /** Loads selected dictionary into memory.
    *
    * @param dict
    *   The path to the dictionary to be used.
    * @return
    *   true if dictionary loaded correctly, false otherwise.
    */
  def loadDict(dict: CString)(using Zone): CBool = boundary:
    wordCount = 0 // Initialize word count

    // Initialize root, check if pointer is NULL (i.e. memory allocation failed)
    root = Node()
    if root == null then
      printf(c"Failed to initialize trie structure\n")
      break(false)

    // Open dictionary for reading, check if pointer is NULL (i.e. file opening failed)
    val file = fopen(dict, c"r")
    if file == null then
      printf(c"Failed to open dictionary\n")
      break(false)

    // Keep track of current node traversing the trie structure, starting at top
    var current = root
    var pos     = -1 // denotes position of character in alphabet (0-26)

    // Load each word in dictionary into trie structure, letter by letter
    var c = fgetc(file)
    while c != EOF do
      // Find location of character c in alphabet (i.e. in children[])
      // Constants APOSTR and ASCII_a are defined in dictionary.h
      pos = if c == '\'' then Apostr else c - ASCII_a

      if c == '\n' then        // Check for newline character
        current.is_word = true // We must have reached end of a word in dictionary
        current = root         // For next word, go back to the root
        wordCount += 1         // Increment word count

      // if c has never been seen in this position
      else if current.children(pos) == null then
        val newnode = Node() // Make pointer to a new node, have children[pos] point to it
        if newnode == null then // Check if newnode pointer is NULL (allocation failed)
          fclose(file)
          break(false)
        // Have current node's children[pos] point to newnode
        // so that the letters of the word can continue
        current.children(pos) = newnode.asInstanceOf[Ptr[Node]]
        current = newnode // Move forward in trie, change current node to newnode

      // c was seen before, move to new node and continue
      // follow where children[pos] is pointing, that's the new current node
      else current = current.children(pos).asInstanceOf[Ptr[Node]]
      end if
      c = fgetc(file)
    end while

    fclose(file)
    true // Dictionary successfully loaded, close file and return true

  /** Returns number of words in dictionary if loaded, else 0 if not yet loaded.
    *
    * @return
    *   Number of words in loaded dictionary.
    */
  def getSize: UInt = wordCount.toUInt
