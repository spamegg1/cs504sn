import scalanative.unsafe.*
import scalanative.unsigned.*

/** Create a trie structure (with nodes) for loading the dictionary and finding words. It's recursive, so we are using a
  * `CVoidPtr` to refer to children `Node`s because cyclic references in types are illegal in Scala.
  */
object Node:
  type TwentySeven = Nat.Digit2[Nat._2, Nat._7]    // 26 letters + apostrophe '\''
  type Children    = CArray[CVoidPtr, TwentySeven] // CVoidPtr = Ptr[Node]
  type Node        = CStruct2[CBool, Children]

  extension (node: Node)
    inline def is_word: CBool          = node._1
    inline def is_word_=(bool: CBool)  = (!node.at1 = bool)
    inline def children                = node._2.asInstanceOf[Children]
    inline def children_=(c: Children) = !node.at2 = c.asInstanceOf[Children]
  end extension

  def apply()(using Zone): Ptr[Node] = alloc[Node](1)

  def apply(is_word: CBool, children: Children)(using Zone): Ptr[Node] =
    val ptr = apply()
    (!ptr).is_word = is_word
    (!ptr).children = children
    ptr
end Node
