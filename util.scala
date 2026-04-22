/** Some syntax sugar for a while loop. Avoid for-loops since they introduce things to the stack and do not play nicely
  * with `stackalloc`.
  *
  * @param start
  *   Start value of the loop variable.
  * @param until
  *   End value of the loop variable.
  * @param body
  *   Body of the loop to be executed.
  */
def loop(start: Int, until: Int)(body: Int => Any): Unit =
  var i = start
  while i < until do
    body(i)
    i += 1
