# LemmaLearner-Java

A version of my LemmaLearner project, written in Java instead of python.

The idea is to generate a list of words with associated sentences based on a given set of texts (for example books), in a directly learnable order (one new word per sentence). The sentences can be chosen in a way to optimize the learning experience. The learning list can then be converted into things like anki cards, such that one can easily learn a language. 

It works for different languages, but not languages without spaces (chinese, japanese, etc.).

## Overview of LemmaLearner

The learning list is based on the sentences harvested from a given set of books. From these sentences, it will produce a "learning list", consisting of a list of pairs of words and sentences. Any given sentence contains the associated word, and furthermore, it does not contain any words that haven't been learned yet/isn't found before the current pair in the learning list. In this way the words are learned in a directly learnable order.

LemmaLearner does not use words as the actual basis for generating the learning list, but instead lemmas. Words like "run" and "ran" are both two forms/conjugations of the lemma "run". These words would be grouped together as one lemma, such that when "run" has been learned, "ran" is also included. Note that this lemmatization uses a Wikitionary based database, located in the "wiktionary files" folder, and thus requires files corresponding to the language that is being learned.

In general, the lemmas are learned (mostly) in order of frequency, such that the most frequent lemmas are learned first. The sentences used to learn the different lemmas are chosen in a somewhat intelligent manner, to optimize the learning experience. This includes:

 - Trying to include lemmas in a given sentence that has been learned, but which haven't been seen often. 
 
 - Trying to include conjugations of already-learned lemmas, which have not been seen before.
 
 - How long the sentence is. The sentence must not be to long or to short, otherwise it will be excluded.
  

## How to use

I will add a GUI before I give a description of this.

## Structure of the program

Add description.

For the programmers: The learning is done in a greedy manner, mostly because I'm fairly confident that the problem is NP-hard.

## TODO

 - Make a GUI.

 - Make Anki output.

 - Make it able to use subtitle files as an input.





