# Mario

## Problem to Solve

Toward the beginning of World 1-1 in Nintendo’s Super Mario Brothers, Mario must hop over
adjacent pyramids of blocks, per the below.
Implement a program that recreates that pyramid,
using hashes ( `#` ) for bricks, as in the below:

```bash
   # #
  ## ##
 ### ###
#### ####
```

And let’s allow the user to decide just how tall the pyramids should be by �rst prompting
them for a positive int between, say, 1 and 8, inclusive.

### Examples

Here’s how the program might work if the user inputs 8 when prompted:

```bash
$ ./mario
Height: 8
       # #
      ## ##
     ### ###
    #### ####
   ##### #####
  ###### ######
 ####### #######
######## ########
```

Here’s how the program might work if the user inputs 4 when prompted:

```bash
$ ./mario
Height: 4
   # #
  ## ##
 ### ###
#### ####
```

Here’s how the program might work if the user inputs 2 when prompted:

```bash
$ ./mario
Height: 2
 # #
## ##
```

And here’s how the program might work if the user inputs 1 when prompted:

```bash
$ ./mario
Height: 1
# #
```

If the user doesn’t, in fact, input a positive integer between 1 and 8, inclusive, when
prompted, the program should re-prompt the user until they cooperate:

```bash
$ ./mario
Height: -1
Height: 0
Height: 42
Height: 50
Height: 4

   # #
  ## ##
 ### ###
#### ####
```

Notice that width of the “gap” between adjacent pyramids is equal to
the width of two hashes, irrespective of the pyramids’ heights.

## How to test your code

Does your code work as prescribed when you input

- -1 (or other negative numbers)?
- 0 ?
- 1 through 8 ?
- 9 or other positive numbers?
- letters or words?
- no input at all, when you only hit Enter?
