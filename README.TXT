Arabic Analyzer for Java.

This work is a Java port of the amazing Tim Buckwalter's Arabic Morphological Analyzer Version 1.0 (http://www.ldc.upenn.edu/Catalog/CatalogEntry.jsp?catalogId=LDC2002L49) originaly written in Perl.
The morphological analyzer is available in the class gpl.pierrick.brihaye.aramorph.AraMorph.

It also provides utility classes for analyzing arabic content with Lucene (http://jakarta.apache.org/lucene/docs/index.html).
The Lucene classes are available in the package gpl.pierrick.brihaye.aramorph.lucene. 
2 command-line programs for testing the analyzers are available in the package gpl.pierrick.brihaye.aramorph.test.

How does the analyzer work ?

Like every Lucene analyzer, the input stream is read from a Reader, then :
a) tokenized : see gpl.pierrick.brihaye.aramorph.lucene.ArabicTokenizer
b) stemmed and grammatically analyzed : see gpl.pierrick.brihaye.aramorph.lucene.ArabicStemmer
c) grammatically filtered : see gpl.pierrick.brihaye.aramorph.lucene.ArabicGrammaticalFilter

The analyzer finally outputs tokens which are grammatically "valuable" stems (nouns, adjectives, verbs...).

The tokenizer tokenizes the incoming stream at arabic letters and filters everything that is not an arabic "word".
- TODO : consider output of arabic digits (with the problem of hindic/arabic digits whose accuracy as an arabic content is locale-dependant)
- TODO : consider output of arabic punctuation.
- TODO : consider output of non-arabic characters by making use of org.apache.lucene.analysis.Token.type()

The morphological stemmer makes use of the AraMorph class to output one token per grammatical analysis of a word (see gpl.pierrick.brihaye.aramorph.Solution). If there are several ways to analyze a word, and this is quite usual, every token but the first one has its positionIncrement set to 0. See : http://jakarta.apache.org/lucene/docs/api/org/apache/lucene/analysis/Token.html#setPositionIncrement(int).

IMPORTANT NOTE : the actual implementation of Lucene's QueryParser is unable to handle such tokens. I'm working on a patch which is able to handle multiple tokens at the same position.

An arabic word can basically be divided in a combination of prefix + stem + suffix. See : http://www.ldc.upenn.edu/doc/LDC2003T06/POS-info.txt. Only the grammatical category (aka POS) of the *stem* is retained as prefixes and suffixes are not to be considered as relevant in a "word analysis".

The morphological filter filters the analyzed tokens whose grammatical category is not found to be semantically relevant (prepositions, particles, pronouns...). It is a grammatical approach for filtering "empty words".
- TODO : not all possible grammatical categories have been encountered in test documents and the list of managed grammatical categories is incomplete. Feedback is welcome.
- TODO : the relevant/not-relevant grammatical categories list is still subject to discussion. Feedback is also welcome here.

The provided AraMorph class is still very close to he original Perl implementation and many TODOes remain :
- TODO : use an external library for regular expressions. For now, the code relies on JDK 1.4+ native support of regular expressions.
- TODO : use native Java Unicode Strings : for now, every arabic word is previously transformed in "BuckWalter transliteration" : http://www.qamus.org/transliteration.htm.
- TODO : all diacritics are removed before the analysis, resulting in a loss of information that could help in giving more accurate analysis results. Not so easy to implement, I'm afraid...
- TODO : use Unicode dictionaries, possibly in XML format. Maybe the arabized dictionaries of the Duali project (http://www.arabeyes.org/project.php?proj=Duali) could be a first step in achieving this goal.
- TODO : improve the major performance issue, AraMorph initialization (~1 minute on my machine). All dictionaries are loaded in memory. Well... maybe a Lucene index could help ;-) ? For this purpose, it would be interesting to have a more generic interface for the dictionary handlers.
- TODO : make better consideration of synchronization in a multi-thread environment.
- TODO : improve/refactor the caching mechanism, which is very basic.
- TODO : improve Buckwalter's dictionaries. There are no analysis results when the "w(a)" cunjunction is aggregated with a negative particle ("lA"/"mA") and a following verb (e.g. "wlAyktb" returns nothing whereas "wlA yktb" returns a correct analysis).
- TODO : output analysis results to an XML Consumer ?
- TODO : get rid of Perlicisms ;-)

Bonus :
Also enclosed is an english/arabic glosser. It works quite the same than the ArabicStemmer, but it ouputs the english glosses of the stems rather than the stem itself. This kind of analyzer is a rare case where the search analyzer shall be different from the one used during indexing.

I am very indebted in Tim Buckwalter's work ; nothing would have been possible without it.

Pierrick Brihaye, 2003/10/11
pierrick.brihaye@wanadoo.fr
