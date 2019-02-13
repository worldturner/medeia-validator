Comments on JSON Schema specification
===

Comments on draft-07
---

### Section 8.4 "contentMediaType"
> [...] and the character set SHOULD be the character set into which the JSON 
> string value was decoded (for which the default is **Unicode**).

Emphasis added to the word "Unicode". "Unicode" is a standard, it's not a character set.
There are a number of character sets related to unicode:

[UTF-7, UTF-8, UTF-16, UTF-32][1]

[1]: https://en.wikipedia.org/wiki/Unicode#Unicode_Transformation_Format_and_Universal_Coded_Character_Set