/*
Copyright (C) 2003  Pierrick Brihaye
pierrick.brihaye@wanadoo.fr
 
Original Perl code :
Portions (c) 2002 QAMUS LLC (www.qamus.org), 
(c) 2002 Trustees of the University of Pennsylvania 
 
This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the
Free Software Foundation, Inc.
59 Temple Place - Suite 330, Boston, MA  02111-1307, USA
or connect to:
http://www.fsf.org/copyleft/gpl.html
*/

package gpl.pierrick.brihaye.aramorph;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.text.DecimalFormat;
import java.util.*;

/** A java port of Buckwalter Arabic Morphological Analyzer Version 1.0.
 * Original Perl distribution avalaible from :
 * <a
 * href="http://www.ldc.upenn.edu/Catalog/CatalogEntry.jsp?catalogId=LDC2002L49">LDC
 * Catalog</a>
 * @author Pierrick Brihaye, 2003
 */
public class AraMorph {
	
	/** The dictionary handler.
	 * TODO : use more generic interface.
	 */
	private static InMemoryDictionaryHandler dict = null;
	/** The solutions handler.
	 * TODO : use more generic interface.
	 */	
	protected static InMemorySolutionsHandler sol = null;
	/** Whether or not the analyzer should output some convenience messages */
	protected boolean verbose = false;
	/** The stream where to output the results */
	PrintStream outputStream = null;
	
	//Stats
	
	/** Lines processed */
	private int linesCounter = 0;
	/** Arabic tokens processed */
	private int arabicTokensCounter = 0;
	/** Not arabic tokens processed */
	private int notArabicTokensCounter = 0;
	
	/** Arabic words which have been succesfully analyzed.
	 * <PRE>key</PRE> = word
	 * <PRE>value</PRE> = occurences
	 */
	private Hashtable found = new Hashtable();
	
	/** Arabic words which have not been succesfully analyzed.
	 * <PRE>key</PRE> = word
	 * <PRE>value</PRE> = occurences
	 */
	private Hashtable notFound = new Hashtable(); //key = word, value = occurences
	
	/** Constructs an arabic morphological analyzer that will output nothing. */
	public AraMorph() {
		this(null, false);
	}
	
	/** Constructs an arabic morphological analyzer
	 * @param outputStream The stream where to output the results. Can be <CODE>null</CODE> if no output is
	 * desired
	 * @param verbose Whether or not the analyzer should output some convenience messages
	 */
	public AraMorph(PrintStream outputStream, boolean verbose) {
		this.outputStream = outputStream;
		this.verbose = verbose;
		dict = InMemoryDictionaryHandler.getHandler();
		sol = InMemorySolutionsHandler.getHandler();
	}
	
	/** Whether or not the analyzer should output some convenience messages
	 * @param verbose Output status
	 */
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}
	
	/** Returns a word in the Buckwalter transliteration system from a word in arabic.
	 * Vowels and diacritics are <strong>discarded</strong>.
	 * @param word The word in arabic
	 * @return The romanized word
	 */
	public static synchronized String romanizeWord(String word) {
		String tmp_word = word;
		tmp_word = tmp_word.replaceAll("\u0621", "'"); //\u0621 : ARABIC LETTER HAMZA
		tmp_word = tmp_word.replaceAll("\u0622", "|"); //\u0622 : ARABIC LETTER ALEF WITH MADDA ABOVE
		tmp_word = tmp_word.replaceAll("\u0623", ">"); //\u0623 : ARABIC LETTER ALEF WITH HAMZA ABOVE
		tmp_word = tmp_word.replaceAll("\u0624", "&"); //\u0624 : ARABIC LETTER WAW WITH HAMZA ABOVE
		tmp_word = tmp_word.replaceAll("\u0625", "<"); //\u0625 : ARABIC LETTER ALEF WITH HAMZA BELOW
		tmp_word = tmp_word.replaceAll("\u0626", "}"); //\u0626 : ARABIC LETTER YEH WITH HAMZA ABOVE
		tmp_word = tmp_word.replaceAll("\u0627", "A"); //\u0627 : ARABIC LETTER ALEF
		tmp_word = tmp_word.replaceAll("\u0628", "b"); //\u0628 : ARABIC LETTER BEH
		tmp_word = tmp_word.replaceAll("\u0629", "p"); //\u0629 : ARABIC LETTER TEH MARBUTA
		tmp_word = tmp_word.replaceAll("\u062A", "t"); //\u062A : ARABIC LETTER TEH
		tmp_word = tmp_word.replaceAll("\u062B", "v"); //\u062B : ARABIC LETTER THEH
		tmp_word = tmp_word.replaceAll("\u062C", "j"); //\u062C : ARABIC LETTER JEEM
		tmp_word = tmp_word.replaceAll("\u062D", "H"); //\u062D : ARABIC LETTER HAH
		tmp_word = tmp_word.replaceAll("\u062E", "x"); //\u062E : ARABIC LETTER KHAH
		tmp_word = tmp_word.replaceAll("\u062F", "d"); //\u062F : ARABIC LETTER DAL
		tmp_word = tmp_word.replaceAll("\u0630", "*"); //\u0630 : ARABIC LETTER THAL
		tmp_word = tmp_word.replaceAll("\u0631", "r"); //\u0631 : ARABIC LETTER REH
		tmp_word = tmp_word.replaceAll("\u0632", "z"); //\u0632 : ARABIC LETTER ZAIN
		tmp_word = tmp_word.replaceAll("\u0633", "s"); //\u0633 : ARABIC LETTER SEEN
		tmp_word = tmp_word.replaceAll("\u0634", "\\$"); //\u0634 : ARABIC LETTER SHEEN
		tmp_word = tmp_word.replaceAll("\u0635", "S"); //\u0635 : ARABIC LETTER SAD
		tmp_word = tmp_word.replaceAll("\u0636", "D"); //\u0636 : ARABIC LETTER DAD
		tmp_word = tmp_word.replaceAll("\u0637", "T"); //\u0637 : ARABIC LETTER TAH
		tmp_word = tmp_word.replaceAll("\u0638", "Z"); //\u0638 : ARABIC LETTER ZAH
		tmp_word = tmp_word.replaceAll("\u0639", "E"); //\u0639 : ARABIC LETTER AIN
		tmp_word = tmp_word.replaceAll("\u063A", "g"); //\u063A : ARABIC LETTER GHAIN		
		tmp_word = tmp_word.replaceAll("\u0640", "_"); //\u0640 : ARABIC TATWEEL
		tmp_word = tmp_word.replaceAll("\u0641", "f"); //\u0641 : ARABIC LETTER FEH
		tmp_word = tmp_word.replaceAll("\u0642", "q"); //\u0642 : ARABIC LETTER QAF
		tmp_word = tmp_word.replaceAll("\u0643", "k"); //\u0643 : ARABIC LETTER KAF
		tmp_word = tmp_word.replaceAll("\u0644", "l"); //\u0644 : ARABIC LETTER LAM
		tmp_word = tmp_word.replaceAll("\u0645", "m"); //\u0645 : ARABIC LETTER MEEM
		tmp_word = tmp_word.replaceAll("\u0646", "n"); //\u0646 : ARABIC LETTER NOON
		tmp_word = tmp_word.replaceAll("\u0647", "h"); //\u0647 : ARABIC LETTER HEH
		tmp_word = tmp_word.replaceAll("\u0648", "w"); //\u0648 : ARABIC LETTER WAW
		tmp_word = tmp_word.replaceAll("\u0649", "Y"); //\u0649 : ARABIC LETTER ALEF MAKSURA
		tmp_word = tmp_word.replaceAll("\u064A", "y"); //\u064A : ARABIC LETTER YEH
		tmp_word = tmp_word.replaceAll("\u064B", "F"); //\u064B : ARABIC FATHATAN
		tmp_word = tmp_word.replaceAll("\u064C", "N"); //\u064C : ARABIC DAMMATAN
		tmp_word = tmp_word.replaceAll("\u064D", "K"); //\u064D : ARABIC KASRATAN
		tmp_word = tmp_word.replaceAll("\u064E", "a"); //\u064E : ARABIC FATHA
		tmp_word = tmp_word.replaceAll("\u064F", "u"); //\u064F : ARABIC DAMMA
		tmp_word = tmp_word.replaceAll("\u0650", "i"); //\u0650 : ARABIC KASRA
		tmp_word = tmp_word.replaceAll("\u0651", "~"); //\u0651 : ARABIC SHADDA
		tmp_word = tmp_word.replaceAll("\u0652", "o"); //\u0652 : ARABIC SUKUN		
		tmp_word = tmp_word.replaceAll("\u0670", "`"); //\u0670 : ARABIC LETTER SUPERSCRIPT ALEF
		tmp_word = tmp_word.replaceAll("\u0671", "{"); //\u0671 : ARABIC LETTER ALEF WASLA
		tmp_word = tmp_word.replaceAll("\u067E", "P"); //\u067E : ARABIC LETTER PEH
		tmp_word = tmp_word.replaceAll("\u0686", "J"); //\u0686 : ARABIC LETTER TCHEH
		tmp_word = tmp_word.replaceAll("\u06A4", "V"); //\u06A4 : ARABIC LETTER VEH
		tmp_word = tmp_word.replaceAll("\u06AF", "G"); //\u06AF : ARABIC LETTER GAF
		tmp_word = tmp_word.replaceAll("\u0698", "R"); //\u0698 : ARABIC LETTER JEH (no more in Buckwalter system)
		//Not in Buckwalter system \u0679 : ARABIC LETTER TTEH
		//Not in Buckwalter system \u0688 : ARABIC LETTER DDAL
		//Not in Buckwalter system \u06A9 : ARABIC LETTER KEHEH
		//Not in Buckwalter system \u0691 : ARABIC LETTER RREH
		//Not in Buckwalter system \u06BA : ARABIC LETTER NOON GHUNNA
		//Not in Buckwalter system \u06BE : ARABIC LETTER HEH DOACHASHMEE
		//Not in Buckwalter system \u06C1 : ARABIC LETTER HEH GOAL
		//Not in Buckwalter system \u06D2 : ARABIC LETTER YEH BARREE
		tmp_word = tmp_word.replaceAll("\u060C", ","); //\u060C : ARABIC COMMA
		tmp_word = tmp_word.replaceAll("\u061B", ";"); //\u061B : ARABIC SEMICOLON
		tmp_word = tmp_word.replaceAll("\u061F", "?"); //\u061F : ARABIC QUESTION MARK
		//Not significant for morphological analysis
		tmp_word = tmp_word.replaceAll("\u0640", ""); //\u0640 : ARABIC TATWEEL
		//Not suitable for morphological analysis : remove all vowels/diacritics, i.e. undo the job !
		tmp_word = tmp_word.replaceAll("[FNKaui~o]", "");
		//TODO : how to handle ARABIC LETTER SUPERSCRIPT ALEF and ARABIC LETTER ALEF WASLA ?		
		tmp_word = tmp_word.replaceAll("[`\\{]", ""); //strip them for now
		return tmp_word;
	}
	
	/** Return an word in arabic from a word in the Buckwalter transliteration system.
	 * @param translitered The romanized word
	 * @return The word in arabic
	 */
	public static synchronized String arabizeWord(String translitered) {
		String tmp_word = translitered;
		// convert to transliteration
		tmp_word = tmp_word.replaceAll("'", "\u0621"); //\u0621 : ARABIC LETTER HAMZA
		tmp_word = tmp_word.replaceAll("\\|", "\u0622"); //\u0622 : ARABIC LETTER ALEF WITH MADDA ABOVE
		tmp_word = tmp_word.replaceAll(">", "\u0623"); //\u0623 : ARABIC LETTER ALEF WITH HAMZA ABOVE
		tmp_word = tmp_word.replaceAll("&", "\u0624"); //\u0624 : ARABIC LETTER WAW WITH HAMZA ABOVE
		tmp_word = tmp_word.replaceAll("<", "\u0625"); //\u0625 : ARABIC LETTER ALEF WITH HAMZA BELOW
		tmp_word = tmp_word.replaceAll("}", "\u0626"); //\u0626 : ARABIC LETTER YEH WITH HAMZA ABOVE
		tmp_word = tmp_word.replaceAll("A", "\u0627"); //\u0627 : ARABIC LETTER ALEF
		tmp_word = tmp_word.replaceAll("b", "\u0628"); //\u0628 : ARABIC LETTER BEH
		tmp_word = tmp_word.replaceAll("p", "\u0629"); //\u0629 : ARABIC LETTER TEH MARBUTA
		tmp_word = tmp_word.replaceAll("t", "\u062A"); //\u062A : ARABIC LETTER TEH
		tmp_word = tmp_word.replaceAll("v", "\u062B"); //\u062B : ARABIC LETTER THEH
		tmp_word = tmp_word.replaceAll("j", "\u062C"); //\u062C : ARABIC LETTER JEEM
		tmp_word = tmp_word.replaceAll("H", "\u062D"); //\u062D : ARABIC LETTER HAH
		tmp_word = tmp_word.replaceAll("x", "\u062E"); //\u062E : ARABIC LETTER KHAH
		tmp_word = tmp_word.replaceAll("d", "\u062F"); //\u062F : ARABIC LETTER DAL
		tmp_word = tmp_word.replaceAll("\\*", "\u0630"); //\u0630 : ARABIC LETTER THAL
		tmp_word = tmp_word.replaceAll("r", "\u0631"); //\u0631 : ARABIC LETTER REH
		tmp_word = tmp_word.replaceAll("z", "\u0632"); //\u0632 : ARABIC LETTER ZAIN
		tmp_word = tmp_word.replaceAll("s", "\u0633" ); //\u0633 : ARABIC LETTER SEEN
		tmp_word = tmp_word.replaceAll("\\$", "\u0634"); //\u0634 : ARABIC LETTER SHEEN
		tmp_word = tmp_word.replaceAll("S", "\u0635"); //\u0635 : ARABIC LETTER SAD
		tmp_word = tmp_word.replaceAll("D", "\u0636"); //\u0636 : ARABIC LETTER DAD
		tmp_word = tmp_word.replaceAll("T", "\u0637"); //\u0637 : ARABIC LETTER TAH
		tmp_word = tmp_word.replaceAll("Z", "\u0638"); //\u0638 : ARABIC LETTER ZAH
		tmp_word = tmp_word.replaceAll("E", "\u0639"); //\u0639 : ARABIC LETTER AIN
		tmp_word = tmp_word.replaceAll("g", "\u063A"); //\u063A : ARABIC LETTER GHAIN
		tmp_word = tmp_word.replaceAll("_", "\u0640"); //\u0640 : ARABIC TATWEEL
		tmp_word = tmp_word.replaceAll("f", "\u0641"); //\u0641 : ARABIC LETTER FEH
		tmp_word = tmp_word.replaceAll("q", "\u0642"); //\u0642 : ARABIC LETTER QAF
		tmp_word = tmp_word.replaceAll("k", "\u0643"); //\u0643 : ARABIC LETTER KAF
		tmp_word = tmp_word.replaceAll("l", "\u0644"); //\u0644 : ARABIC LETTER LAM
		tmp_word = tmp_word.replaceAll("m", "\u0645"); //\u0645 : ARABIC LETTER MEEM
		tmp_word = tmp_word.replaceAll("n", "\u0646"); //\u0646 : ARABIC LETTER NOON
		tmp_word = tmp_word.replaceAll("h", "\u0647"); //\u0647 : ARABIC LETTER HEH
		tmp_word = tmp_word.replaceAll("w", "\u0648"); //\u0648 : ARABIC LETTER WAW
		tmp_word = tmp_word.replaceAll("Y", "\u0649"); //\u0649 : ARABIC LETTER ALEF MAKSURA
		tmp_word = tmp_word.replaceAll("y", "\u064A"); //\u064A : ARABIC LETTER YEH
		tmp_word = tmp_word.replaceAll("F", "\u064B"); //\u064B : ARABIC FATHATAN
		tmp_word = tmp_word.replaceAll("N", "\u064C"); //\u064C : ARABIC DAMMATAN
		tmp_word = tmp_word.replaceAll("K", "\u064D"); //\u064D : ARABIC KASRATAN
		tmp_word = tmp_word.replaceAll("a", "\u064E"); //\u064E : ARABIC FATHA
		tmp_word = tmp_word.replaceAll("u", "\u064F"); //\u064F : ARABIC DAMMA
		tmp_word = tmp_word.replaceAll("i", "\u0650"); //\u0650 : ARABIC KASRA
		tmp_word = tmp_word.replaceAll("~", "\u0651"); //\u0651 : ARABIC SHADDA
		tmp_word = tmp_word.replaceAll("o", "\u0652"); //\u0652 : ARABIC SUKUN
		tmp_word = tmp_word.replaceAll("`", "\u0670"); //\u0670 : ARABIC LETTER SUPERSCRIPT ALEF
		tmp_word = tmp_word.replaceAll("\\{", "\u0671"); //\u0671 : ARABIC LETTER ALEF WASLA
		tmp_word = tmp_word.replaceAll("P", "\u067E"); //\u067E : ARABIC LETTER PEH
		tmp_word = tmp_word.replaceAll("J", "\u0686"); //\u0686 : ARABIC LETTER TCHEH
		tmp_word = tmp_word.replaceAll("V", "\u06A4"); //\u06A4 : ARABIC LETTER VEH
		tmp_word = tmp_word.replaceAll("G", "\u06AF"); //\u06AF : ARABIC LETTER GAF
		tmp_word = tmp_word.replaceAll("R", "\u0698"); //\u0698 : ARABIC LETTER JEH (no more in Buckwalter system)
		//Not in Buckwalter system \u0679 : ARABIC LETTER TTEH
		//Not in Buckwalter system \u0688 : ARABIC LETTER DDAL
		//Not in Buckwalter system \u06A9 : ARABIC LETTER KEHEH
		//Not in Buckwalter system \u0691 : ARABIC LETTER RREH
		//Not in Buckwalter system \u06BA : ARABIC LETTER NOON GHUNNA
		//Not in Buckwalter system \u06BE : ARABIC LETTER HEH DOACHASHMEE
		//Not in Buckwalter system \u06C1 : ARABIC LETTER HEH GOAL
		//Not in Buckwalter system \u06D2 : ARABIC LETTER YEH BARREE
		tmp_word = tmp_word.replaceAll(",", "\u060C" ); //\u060C : ARABIC COMMA
		tmp_word = tmp_word.replaceAll(";", "\u061B"); //\u061B : ARABIC SEMICOLON
		tmp_word = tmp_word.replaceAll("\\?", "\u061F"); //\u061F : ARABIC QUESTION MARK
		return tmp_word;
	}
	
	/** Analyze the content of a stream
	 * @param IN The stream to be analyzed
	 */
	private void analyze(LineNumberReader IN, boolean outputBuckwalter) {
		try {
			String line = null;
			while ((line = IN.readLine()) != null) {
				linesCounter++;
				if (verbose) System.out.println("Processing line : " + IN.getLineNumber());
				List tokens = tokenize(line);
				Iterator it_tokens = tokens.iterator();
				while (it_tokens != null && it_tokens.hasNext()) {
					String token = (String)it_tokens.next();
					analyzeToken(token, outputBuckwalter);
				}
			}
		}
		catch (IOException e) {
			throw new RuntimeException("Can not read line " + IN.getLineNumber());
		}
	}
	
	/** Tokenizes a <CODE>String</CODE> using arabic boundaries.
	 * @return A list of tokens
	 * @param str The <CODE>String</CODE>
	 */
	private List tokenize(String str) {
		str = str.trim();
		str = str.replaceAll("\\s+", " ");
		//ignored \u0688 : ARABIC LETTER DDAL
		//ignored \u06A9 : ARABIC LETTER KEHEH
		//ignored \u0691 : ARABIC LETTER RREH
		//ignored \u06BA : ARABIC LETTER NOON GHUNNA
		//ignored \u06BE : ARABIC LETTER HEH DOACHASHMEE
		//ignored \u06C1 : ARABIC LETTER HEH GOAL
		//ignored \u06D2 : ARABIC LETTER YEH BARREE
		String split[] = str.split("([^\u067E\u0686\u0698\u06AF\u0621-\u0636\u0637-\u0643\u0644\u0645-\u0648\u0649-\u064A\u064B-\u064E\u064F\u0650\u0651\u0652]+)");
		ArrayList tokens = new ArrayList();
		//return at least one token, the string if necessary
		if (split.length == 0) tokens.add(str);
		else tokens.addAll(Arrays.asList(split));
		return tokens;
	}
	
	/** Analyzes a token and return the results in the Buckwalter transliteration system.
	 * For performance issues, the analyzer keeps track of the results.
	 * @param token The token to be analyzed
	 * @return Whether or not the word has a solution in arabic
	 */
	public boolean analyzeToken(String token) {
		return this.analyzeToken(token, true);
	}
		
	/** Analyzes a token.
	 * For performance issues, the analyzer keeps track of the results.
	 * @return Whether or not the word has a solution in arabic
	 * @param outputBuckwalter Whether or not the Buckwalter transliteration system should be used. If not, outputs will be in arabic wherever possible
	 * @param token The token to be analyzed
	 */
	public boolean analyzeToken(String token, boolean outputBuckwalter) {
		if (outputStream != null) outputStream.println("Processing token : " + "\t" + token);
		//TODO : check accuracy
		//ignored \u0688 : ARABIC LETTER DDAL
		//ignored \u06A9 : ARABIC LETTER KEHEH
		//ignored \u0691 : ARABIC LETTER RREH
		//ignored \u06BA : ARABIC LETTER NOON GHUNNA
		//ignored \u06BE : ARABIC LETTER HEH DOACHASHMEE
		//ignored \u06C1 : ARABIC LETTER HEH GOAL
		//ignored \u0640 : ARABIC TATWEEL
		//ignored \u06D2 : ARABIC LETTER YEH BARREE
		if (!token.matches("([\u067E\u0686\u0698\u06AF\u0621-\u063A\u0641-\u0652])+")) {
			token = token.trim();
			// tokenize it on white space
			String subTokens[] = token.split("\\s+");
			for (int i = 0 ; i < subTokens.length ; i++) {
				if (!"".equals(subTokens[i].trim())) {
					notArabicTokensCounter++;
					if (outputStream != null) outputStream.println("Non-Arabic : " + subTokens[i]);
				}
			}
			return false;
		}
		else {
			boolean hasSolutions = false;
			arabicTokensCounter++;
			String translitered = romanizeWord(token);
			if (outputStream != null && verbose) outputStream.println("Transliteration : " + "\t" + translitered);
			//Already processed : previously found
			if (found.containsKey(translitered)) {
				if (outputStream != null && verbose) outputStream.println("Token already processed.");
				if (!found.containsKey(translitered))
					throw new RuntimeException("There is no key for " + translitered + " in found");
				//increase reference counter
				Integer foundCounter = (Integer)found.get(translitered);
				found.put(translitered, new Integer(foundCounter.intValue() + 1));
				hasSolutions = true;
			}
			//Already processed : previously not found
			else if (notFound.containsKey(translitered)) {
				if (outputStream != null && verbose) outputStream.println("Token already processed without solution.");
				if (!notFound.containsKey(translitered))
					throw new RuntimeException("There is no key for " + translitered + " in notFound");
				//increase reference counter
				Integer notFoundCounter = (Integer)notFound.get(translitered);
				notFound.put(translitered, new Integer(notFoundCounter.intValue() + 1));
				hasSolutions = false;
			}
			//Not yet processed
			else {
				
				if (outputStream != null && verbose) outputStream.println("Token not yet processed.");
				
				//word has solutions...
				if (feedWordSolutions(translitered)) {
					//mark word as found
					if (found.containsKey(translitered))
						throw new RuntimeException("There is already a key for " + translitered + " in found");
					if (outputStream != null && verbose) outputStream.println("Token has direct solutions.");
					//set reference counter to 1
					found.put(translitered,  new Integer(1));
					hasSolutions = true;
				}
				//word has no direct solution
				else {
					//if there are some alternative spellings
					if (feedAlternativeSpellings(translitered)) {
						
						boolean alternativesGiveSolutions = false;
						Iterator it_alternatives = sol.getAlternativeSpellingsIterator(translitered);
						while (it_alternatives != null && it_alternatives.hasNext()) {
							String alternative = (String)it_alternatives.next();
							//feed solutions with alternative spellings' ones
							alternativesGiveSolutions = (alternativesGiveSolutions || feedWordSolutions(alternative));
						}
						
						if (alternativesGiveSolutions) {
							//consistency check
							if (found.containsKey(translitered))
								throw new RuntimeException("There is already a key for " + translitered + " in found");
							if (outputStream != null && verbose) outputStream.println("Token's alternative spellings have solutions.");
							//mark word as found set reference counter to 1
							found.put(translitered,  new Integer(1));
							hasSolutions = true;
						}
						else {
							//consistency check
							if (notFound.containsKey(translitered))
								throw new RuntimeException("There is already a key for " + translitered + " in notFound");
							if (outputStream != null && verbose) outputStream.println("Token's alternative spellings have no solution.");
							//mark word as not found and set reference counter to 1
							notFound.put(translitered, new Integer(1));
							hasSolutions = false;
						}
					}
					//there are no alternative
					else {
						//consistency check
						if (notFound.containsKey(translitered))
							throw new RuntimeException("There is already a key for " + translitered + " in notFound");
						if (outputStream != null && verbose) outputStream.println("Token has no solution and no alternative spellings.");
						//mark word as not found and set reference counter to 1
						notFound.put(translitered, new Integer(1));
						hasSolutions = false;
					}
				}
			}
			
			//output solutions : TODO consider XML output
			if (outputStream != null) {
				if (found.containsKey(translitered)) {
					if (sol.hasSolutions(translitered)) {
						Iterator it_solutions = sol.getSolutionsIterator(translitered);
						while (it_solutions != null && it_solutions.hasNext()) {
							Solution solution = (Solution)it_solutions.next();
							if (outputBuckwalter) 
								outputStream.println(solution.toString());
							else
								outputStream.println(solution.toArabizedString());
						}
					}
					if (sol.hasAlternativeSpellings(translitered)) {
						if (outputStream != null && verbose) outputStream.println("No direct solution");
						Iterator it_alternatives = sol.getAlternativeSpellingsIterator(translitered);
						while (it_alternatives != null && it_alternatives.hasNext()) {
							String alternative = (String)it_alternatives.next();
							if (outputStream != null && verbose) outputStream.println("Considering alternative spelling :" + "\t" + alternative);
							//if (sol.hasSolutions(alternative) && !(sol.getSolutions(alternative).isEmpty()) {
							if (sol.hasSolutions(alternative)) {
								Iterator it_solutions = sol.getSolutionsIterator(alternative);
								while (it_solutions != null && it_solutions.hasNext()) {
									Solution solution = (Solution)it_solutions.next();
									if (outputBuckwalter) 
										outputStream.println(solution.toString());
									else
										outputStream.println(solution.toArabizedString());
								}
							}
						}
					}
				}
				else if (notFound.containsKey(translitered)) {
					outputStream.println("No solution");
				}
				else throw new RuntimeException(translitered + " is neither in found or notFound !");
			}
			
			return hasSolutions;
		}
	}
	
	/** Splits a word in prefix + stem + suffix combinations.
	 * @return The list of combinations
	 * @param translitered The word. It is assumed that {@link #romanizeWord(String word) romanizeWord} has been called before
	 */
	private HashSet segmentWord(String translitered) {
		HashSet segmented = new HashSet();
		int prefix_len = 0;
		int suffix_len = 0;
		//TODO : why 4 ? The info could certainly be grabbed from dictionnaries...
		while ((prefix_len) <= 4 && (prefix_len <= translitered.length())) {
			String prefix = translitered.substring(0, prefix_len);
			int stem_len = (translitered.length() - prefix_len);
			suffix_len = 0;
			//TODO : why 6 ?  The info could certainly be grabbed from dictionnaries...
			while ((stem_len >= 1) && (suffix_len <= 6)) {
				String stem = translitered.substring(prefix_len, prefix_len + stem_len);
				String suffix = translitered.substring(prefix_len + stem_len, prefix_len + stem_len + suffix_len);
				segmented.add(new SegmentedWord(prefix, stem, suffix));
				stem_len--;
				suffix_len++;
			}
			prefix_len++;
		}
		return segmented;
	}
	
	/** Feed an internal list of solutions for the given word
	 * @param translitered The word. It is assumed that {@link #romanizeWord(String word) romanizeWord} has been called before
	 * @return Whether or not there are solutions for this word
	 */
	private boolean feedWordSolutions(String translitered) {
		//No need to reprocess
		if (sol.hasSolutions(translitered)) return true;
		HashSet wordSolutions = new HashSet();
		int cnt = 0;
		//get a list of valid segmentations
		HashSet segments = segmentWord(translitered);
		//Brute force algorithm
		Iterator it_seg = segments.iterator();
		while (it_seg != null && it_seg.hasNext()) {
			SegmentedWord segmentedWord = (SegmentedWord)it_seg.next();
			//Is prefix known ?
			if (dict.hasPrefix(segmentedWord.getPrefix())) {
				//Is stem known ?
				if (dict.hasStem(segmentedWord.getStem())) {
					//Is suffix known ?
					if (dict.hasSuffix(segmentedWord.getSuffix())) {
						//Compatibility check
						Iterator it_prefix = dict.getPrefixIterator(segmentedWord.getPrefix());
						while (it_prefix != null && it_prefix.hasNext()) {
							DictionaryEntry prefix = (DictionaryEntry)it_prefix.next();
							Iterator it_stem = dict.getStemIterator(segmentedWord.getStem());
							while (it_stem != null && it_stem.hasNext()) {
								DictionaryEntry stem = (DictionaryEntry)it_stem.next();
								//Prefix/Stem compatibility
								if (dict.hasAB(prefix.getMorphology(), stem.getMorphology())) {
									Iterator it_suffix = dict.getSuffixIterator(segmentedWord.getSuffix());
									while (it_suffix != null && it_suffix.hasNext()) {
										DictionaryEntry suffix = (DictionaryEntry)it_suffix.next();
										//Prefix/Suffix compatiblity
										if (dict.hasAC(prefix.getMorphology(), suffix.getMorphology())) {
											//Stem/Suffix compatibility
											if (dict.hasBC(stem.getMorphology(), suffix.getMorphology())) {
												//All tests passed : it is a solution
												wordSolutions.add(new Solution(++cnt, prefix, stem, suffix));
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		//Add all solutions, if any
		if (!wordSolutions.isEmpty()) sol.addSolutions(translitered, wordSolutions);
		return (!wordSolutions.isEmpty());
	}
	
	/** Feed an internal list of alternative spellings for the given word
	 * @param translitered The word. It is assumed that {@link #romanizeWord(String word) romanizeWord} has been called before
	 * @return Whether or not there are alternative spellings for this word
	 */
	private boolean feedAlternativeSpellings(String translitered) {
		//No need to reprocess
		if (sol.hasAlternativeSpellings(translitered)) return true;
		HashSet wordAlternativeSpellings = new HashSet();
		String temp = translitered;
		String temp2;
		//final 'alif maqSuura + hamza-on-the-line
		if (temp.matches(".*" + "Y'$")) { // Y_w'_Y'
			//-> yaa' + hamza-on-the-line
			temp = temp.replaceAll("Y", "y"); // y_w'_y'
			if (outputStream != null && verbose) outputStream.println("Found alternative spelling "+ temp + " for word " + translitered);
			wordAlternativeSpellings.add(temp); // y_w'_y'  -- pushed
			//medial waaw + hamza-on-the-line -> hamza-on-waaw
			temp2 = temp.replaceFirst("w'", "&"); //y_&__y'
			if (!temp.equals(temp2)) {
				temp = temp2; //y_&__y'
				if (outputStream != null && verbose) outputStream.println("Found alternative spelling "+ temp + " for word " + translitered);
				wordAlternativeSpellings.add(temp); //y_&__y'  -- pushed
			}
			temp = translitered; // Y_w'_Y'
			//-> yaa' + hamza-on-the-line
			temp = temp.replaceAll("Y","y"); // y_w'_y'
			//final yaa' + hamza-on-the-line -> hamza-on-yaa'
			temp = temp.replaceFirst("y'$","}"); // y_w'_}
			if (outputStream != null && verbose) outputStream.println("Found alternative spelling "+ temp + " for word " + translitered);
			wordAlternativeSpellings.add(temp); // y_w'_}   -- pushed
			//medial waaw + hamza-on-the-line -> hamza-on-waaw
			temp2 = temp.replaceFirst("w'", "&"); //  y_&__}
			if (!temp.equals(temp2)) {
				temp = temp2; //  y_&__}
				if (outputStream != null && verbose) outputStream.println("Found alternative spelling "+ temp + " for word " + translitered);
				wordAlternativeSpellings.add(temp); //y_&__}   -- pushed
			}
		}
		//final yaa' + hamza-on-the-line
		else if (temp.matches(".*" + "y'$")) { //Y_w'_y'
			//'alif maqSuura -> yaa'
			temp2 = temp.replaceAll("Y", "y"); //y_w'_y'
			if (!temp.equals(temp2)) {
				temp = temp2; //y_w'_y'
				if (outputStream != null && verbose) outputStream.println("Found alternative spelling "+ temp + " for word " + translitered);
				wordAlternativeSpellings.add(temp); //y_w'_y'  -- pushed
			}
			//medial waaw + hamza-on-the-line -> hamza-on-waaw
			temp2 = temp.replaceFirst("w'", "&"); // y_&__y'
			if (!temp.equals(temp2)) {
				temp = temp2; // y_&__y'
				if (outputStream != null && verbose) outputStream.println("Found alternative spelling "+ temp + " for word " + translitered);
				wordAlternativeSpellings.add(temp); // y_&__y'  -- pushed
			}
			temp = translitered; // Y_w'_y'
			//'alif maqSuura -> yaa'
			temp = temp.replaceAll("Y", "y"); //y_w'_y'
			//final yaa' + hamza-on-the-line -> 'alif maqSuura
			temp = temp.replaceFirst("y'$", "}"); // y_w'_}
			if (outputStream != null && verbose) outputStream.println("Found alternative spelling "+ temp + " for word " + translitered);
			wordAlternativeSpellings.add(temp); // y_w'_}   -- pushed
			//medial waaw + hamza-on-the-line -> hamza-on-waaw
			temp2 = temp.replaceFirst("w'", "&"); // y_&__}
			if (!temp.equals(temp2)) {
				temp = temp2; // y_&__}
				if (outputStream != null && verbose) outputStream.println("Found alternative spelling "+ temp + " for word " + translitered);
				wordAlternativeSpellings.add(temp); // y_&__}   -- pushed
			}
		}
		//final yaa'
		else if (temp.matches(".*" + "y$")) { // Y_w'_y
			//'alif maqSuura -> yaa'
			temp = temp.replaceAll("Y", "y"); // y_w'_y
			//medial waaw + hamza-on-the-line -> hamza-on-waaw
			temp2 = temp.replaceFirst("w'", "&"); // y_&__y
			if (!temp.equals(temp2)) {
				temp = temp2; // y_&__y
				if (outputStream != null && verbose) outputStream.println("Found alternative spelling "+ temp + " for word " + translitered);
				wordAlternativeSpellings.add(temp); // y_&__y   -- pushed
			}
			temp = translitered; //Y_w'_y
			//'alif maqSuura -> yaa'
			temp = temp.replaceAll("Y", "y"); // y_w'_y
			//final yaa' -> 'alif maqSuura
			temp = temp.replaceAll("y$", "Y"); // y_w'_Y
			if (outputStream != null && verbose) outputStream.println("Found alternative spelling "+ temp + " for word " + translitered);
			wordAlternativeSpellings.add(temp); // y_w'_Y   -- pushed
			//medial waaw + hamza-on-the-line -> hamza-on-waaw
			temp2= temp.replaceFirst("w'", "&"); // y_&__Y
			if (!temp.equals(temp2)) {
				temp = temp2; // y_&__Y
				if (outputStream != null && verbose) outputStream.println("Found alternative spelling "+ temp + " for word " + translitered);
				wordAlternativeSpellings.add(temp); // y_&__Y   -- pushed
			}
		}
		//final haa'
		else if (temp.matches(".*" + "h$")) { // Y_w'_h
			//'alif maqSuura -> yaa'
			temp2 = temp.replaceAll("Y", "y"); // y_w'_h
			if (!temp.equals(temp2)) {
				temp = temp2; // y_w'_h
				if (outputStream != null && verbose) outputStream.println("Found alternative spelling "+ temp + " for word " + translitered);
				wordAlternativeSpellings.add(temp); // y_w'_h   -- pushed
			}
			//medial waaw + hamza-on-the-line -> hamza-on-waaw
			temp2 = temp.replaceFirst("w'", "&"); // y_&__h
			if (!temp.equals(temp2)) {
				temp = temp2; // y_&__h
				if (outputStream != null && verbose) outputStream.println("Found alternative spelling "+ temp + " for word " + translitered);
				wordAlternativeSpellings.add(temp); // y_&__h   -- pushed
			}
			//final haa' -> taa' marbuuTa
			temp = temp.replaceFirst("h$", "p"); // y_w'_p
			if (outputStream != null && verbose) outputStream.println("Found alternative spelling "+ temp + " for word " + translitered);
			wordAlternativeSpellings.add(temp); // y_w'_p   -- pushed
		}
		//final taa' marbuuTa
		else if (temp.matches(".*" + "p$")) { // Y_w'_p
			//'alif maqSuura -> yaa'
			temp2 = temp.replaceAll("Y", "y"); // y_w'_p
			if (!temp.equals(temp2)) {
				temp = temp2; // y_w'_p
				if (outputStream != null && verbose) outputStream.println("Found alternative spelling "+ temp + " for word " + translitered);
				wordAlternativeSpellings.add(temp); // y_w'_p   -- pushed
			}
			//medial waaw + hamza-on-the-line -> hamza-on-waaw
			temp2 = temp.replaceFirst("w'", "&"); // y_&__p
			if (!temp.equals(temp2)) {
				temp = temp2; // y_&__p
				if (outputStream != null && verbose) outputStream.println("Found alternative spelling "+ temp + " for word " + translitered);
				wordAlternativeSpellings.add(temp); // y_&__p   -- pushed
			}
			//final taa' marbuuTa -> haa'
			temp = temp.replaceFirst("p$", "h"); //y_w'_h
			if (outputStream != null && verbose) outputStream.println("Found alternative spelling "+ temp + " for word " + translitered);
			wordAlternativeSpellings.add(temp); // //y_w'_h   -- pushed
		}
		//Substitutions before matching
		else {
			//final 'alif maqSuura -> yaa'
			temp2 = temp.replaceFirst("Y$", "y"); // Y_w'_y
			if (!temp.equals(temp2)) {
				temp = temp2; // Y_w'_y
				//'alif maqSuura -> yaa'
				temp = temp.replaceAll("Y", "y"); // y_w'_y
				if (outputStream != null && verbose) outputStream.println("Found alternative spelling "+ temp + " for word " + translitered);
				wordAlternativeSpellings.add(temp); // y_w'_y   -- pushed
				//medial waaw + hamza-on-the-line -> hamza-on-waaw
				temp2 = temp.replaceFirst("w'", "&"); // y_&__y
				if (!temp.equals(temp2)) {
					temp = temp2; // y_&__y
					if (outputStream != null && verbose) outputStream.println("Found alternative spelling "+ temp + " for word " + translitered);
					wordAlternativeSpellings.add(temp); // y_&__y   -- pushed
				}
			}
			else {
				//'alif maqSuura -> yaa'
				temp2 = temp.replaceAll("Y", "y"); // y_w'__
				if (!temp.equals(temp2)) {
					temp = temp2; // y_w'__
					if (outputStream != null && verbose) outputStream.println("Found alternative spelling "+ temp + " for word " + translitered);
					wordAlternativeSpellings.add(temp); // y_w'__   -- pushed
					//medial waaw + hamza-on-the-line -> hamza-on-waaw
					temp2 = temp.replaceFirst("w'", "&"); // y_&___
					if (!temp.equals(temp2)) {
						temp = temp2; // y_&___
						if (outputStream != null && verbose) outputStream.println("Found alternative spelling "+ temp + " for word " + translitered);
						wordAlternativeSpellings.add(temp); // y_&___   -- pushed
					}
				}
				else {
					//medial waaw + hamza-on-the-line -> hamza-on-waaw
					temp2 = temp.replaceFirst("w'", "&"); // y_&___
					if (!temp.equals(temp2)) {
						temp = temp2; // y_&___
						if (outputStream != null && verbose) outputStream.println("Found alternative spelling "+ temp + " for word " + translitered);
						wordAlternativeSpellings.add(temp); // y_&___   -- pushed
					}
					else {} //nothing
				}
			}
		}
		//Add all alternative spellings, if any
		if (!wordAlternativeSpellings.isEmpty()) sol.addAlternativeSpellings(translitered, wordAlternativeSpellings);
		return (!wordAlternativeSpellings.isEmpty());
	}
	
	/** Returns the solutions for a previously analyzed word.
	 * @param translitered The word.
	 * @return A list of solutions
	 * @see Solution
	 */
	public HashSet getWordSolutions(String word) {
		HashSet wordSolutions = new HashSet();
		String translitered = romanizeWord(word);
		if (found.containsKey(translitered)) {
			if (sol.hasSolutions(translitered)) {
				Iterator it_sol = sol.getSolutionsIterator(translitered);
				while (it_sol != null && it_sol.hasNext()) {
					Solution solution = (Solution)it_sol.next();
					wordSolutions.add(solution);
				}
			}
			if (sol.hasAlternativeSpellings(translitered)) {
				Iterator it_alt = sol.getAlternativeSpellingsIterator(translitered);
				while (it_alt != null && it_alt.hasNext()) {
					String alt = (String)it_alt.next();
					//Notice that the alternative spelling may not be (yet) marked as found
					if (sol.hasSolutions(alt)) {
						Iterator it_sol = sol.getSolutionsIterator(alt);
						while (it_sol != null && it_sol.hasNext()) {
							Solution solution = (Solution)it_sol.next();
							wordSolutions.add(solution);
						}
					}
				}
			}
		}
		else if (notFound.containsKey(translitered)) {}
		else throw new RuntimeException(translitered + " is neither in found or notFound !");
		return wordSolutions;
	}
	
	/** Display the statistics on what has been analyzed so far. */
	public void printStats() {
		DecimalFormat df = new DecimalFormat("##.##%");
		double total = found.size() + notFound.size(); //double to force casting
		System.out.println();
		System.out.println("=================== Statistics ===================");
		System.out.println("Lines : " + linesCounter);
		System.out.println("Arabic tokens : " + arabicTokensCounter);
		System.out.println("Non-arabic tokens : " + notArabicTokensCounter);
		System.out.println("Words found : " + found.size() + " (" + df.format(found.size() / total) + ")");
		System.out.println("Words not found : " + notFound.size() + " (" + df.format(notFound.size() / total) + ")");
		System.out.println("==================================================");
		System.out.println();
	}
	
	/** Display help for command line interface. */
	private static void PrintUsage() {
		System.err.println("Arabic Morphological Analyzer for Java(tm)");
		System.err.println("Ported to Java(tm) by Pierrick Brihaye, 2003.");
		System.err.println("Based on :");
		System.err.println("BUCKWALTER ARABIC MORPHOLOGICAL ANALYZER");
		System.err.println("Portions (c) 2002 QAMUS LLC (www.qamus.org),");
		System.err.println("(c) 2002 Trustees of the University of Pennsylvania.");
		System.err.println("This program is governed by :");
		System.err.println("The Apache Software License, Version 1.1");
		System.err.println("");
		System.err.println("Usage :");
		System.err.println("");
		System.err.println("araMorph inFile [inEncoding] [outFile] [outEncoding] [-v]");
		System.err.println("");
		System.err.println("inFile : file to be analyzed");
		System.err.println("inEncoding : encoding for inFile, default CP1256");
		System.err.println("outFile : result file, default console");
		System.err.println("outEncoding : encoding for outFile, if not specified use Buckwalter transliteration with system's file.encoding");
		System.err.println("-v : verbose mode");
	}
	
	/** Entry point for command line interface.
	 * @param args Arguments
	 */
	public static void main(String[] args) {
		
		boolean argsOK = true;
		String inputFile = null;
		String outputFile = null;
		String inputEncoding = null;
		String outputEncoding = null;
		Boolean verbose = null;
				
		if (args.length == 0) argsOK = false;
		//TODO : should we be more severe ?
		else {
			for (int i = 0 ; i < args.length ; i++) {
				//verbose flag ?
				if ("-v".equals(args[i])) {
					if (verbose == null) {
						verbose = new Boolean(true);
						continue;
					}
					else {
						argsOK = false;
						break;
					}
				}
				//is it a charset ?
				try {
					Charset.forName(args[i]);
					if (inputEncoding == null) {
						inputEncoding = args[i];
						continue;
					}
					else if (outputEncoding == null) {
						outputEncoding = args[i];
						continue;
					}
					//too many charsets
					else {
						argsOK = false;
						break;
					}
				}
				catch (IllegalCharsetNameException e) {}
				catch (UnsupportedCharsetException e) {}
				//is it a file name ?
				if (inputFile == null) {
					inputFile = args[i];
					continue;
				}
				else if (outputFile == null) {
					outputFile = args[i];
					continue;
				}
				//too many files
				else {
					argsOK = false;
					break;
				}
			}
		}
		
		if (!argsOK || inputFile == null) PrintUsage();
		else {
			
			if (inputEncoding == null) inputEncoding = "Cp1256"; //TODO : change default ?						
			if (verbose == null) verbose = new Boolean(false);
			LineNumberReader IN = null;
			PrintStream ps = null;
			
			if (outputFile != null) {
				try {
					if (outputEncoding == null)
						ps = new PrintStream(new FileOutputStream(outputFile), true);
					else
						ps = new PrintStream(new FileOutputStream(outputFile), true, outputEncoding);
				}
				catch (FileNotFoundException e) {
					System.err.println("Can't write to output file : " + outputFile);
				}
				catch (UnsupportedEncodingException e) {
					System.err.println("Unsupported output encoding : " + outputEncoding);
				}
			}
			else ps = System.out;
			
			try {
				AraMorph araMorph = new AraMorph(ps, verbose.booleanValue());				
				IN = new LineNumberReader(new BufferedReader(new InputStreamReader(new FileInputStream(inputFile),inputEncoding)));
				if (outputEncoding == null) 
					araMorph.analyze(IN, true);
				else
					araMorph.analyze(IN, false);
				araMorph.printStats();
			}
			catch (IOException e) {
				throw new RuntimeException("Problem : " + e.getMessage());
			}
			finally {
				try {
					if (IN != null) IN.close();
				}
				catch (IOException e) {}
				if (ps != null) ps.close();
			}
		}
	}
	
	//Inner class
	
	private class SegmentedWord {
		
		private String prefix;
		private String stem;
		private String suffix;
		
		protected SegmentedWord(String prefix, String stem, String suffix) {
			this.prefix = prefix;
			this.stem = stem;
			this.suffix = suffix;
		}
		
		protected String getPrefix() { return this.prefix; }
		protected String getStem() { return this.stem; }
		protected String getSuffix() { return this.suffix; }
	}
	
}
