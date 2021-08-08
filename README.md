# LemmaLearner-Java

A version of my LemmaLearner project, written in Java instead of python.

The idea is to generate a list of words with associated sentences, in a directly learnable order (one new word per sentence). This can then be converted into things like anki cards.

The sentences and words are found by parsing a given number of texts. A custom ANTLR based parser have been created to do this. It splits texts into paragraphs, sentences and words. An ANTLR based parser is used, as texts follows a context free language when parenthesis and quotes are used, making it impossible to parse with a regular language/expression. In the python version of the program, nlptk was used to do this, but the output was low quality because it didn't take quotes into account (or at least handeled it poorly).
