/*
Copyright (C) 2003  Pierrick Brihaye
pierrick.brihaye@wanadoo.fr

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

package gpl.pierrick.brihaye.aramorph.lucene;

import gpl.pierrick.brihaye.aramorph.AraMorph;
import gpl.pierrick.brihaye.aramorph.Solution;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

/** A stemmer that will return the possible stems for arabic tokens.*/
public class ArabicStemmer extends TokenFilter {
	
	private boolean debug = false;
	private AraMorph araMorph = null;
	private String romanizedToken = null;
	private Token receivedToken = null;
	private boolean processingToken = false;	
	private ArrayList tokenSolutions = null;
	
	/** Constructs a stemmer that will return the possible stems for arabic tokens.
	 * @param input The token stream from a tokenizer
	 */
	public ArabicStemmer(TokenStream input) {
		this(input, false);
	}
	
	/** Constructs a stemmer that will return the possible stems for arabic tokens.
	 * @param input The reader
	 * @param debug Whether or not the stemmer should display convenience messages on <CODE>System.out</CODE>
	 */
	public ArabicStemmer(TokenStream input, boolean debug) {
		super(input);
		this.debug = debug;
		araMorph = new AraMorph();
	}
	
	/** Returns the arabic stemmer in use.
	 * @return The arabic stemmer
	 * @see gpl.pierrick.brihaye.aramorph.AraMorph
	 */
	public AraMorph getAramorph() { return araMorph; }
	
	/** Returns the next stem for the given token.
	 * @param firstOne Whether or not this stem is the first one
	 * @return The token. Its <CODE>termText</CODE> is the romanized arabic <STRONG>stem</STRONG>. Its <CODE>type</CODE> is the morphological category of the <STRONG>stem</STRONG>.
	 * When several stems are available, every emitted token's
	 * <CODE>PositionIncrement</CODE> but the first one is set to <CODE>0</CODE>
	 * @see org.apache.lucene.analysis.Token#setPositionIncrement(int)
	 */
	private Token nextSolution(boolean firstOne) {
		Token emittedToken = null;
		String tokenText = null;
		String tokenType = null;
		try {
			//Get the first solution
			Solution currentSolution = (Solution)tokenSolutions.get(0);
			//This is the trick ! Only the canonical form of the stem is to be considered
			tokenText = currentSolution.getStemVocalization(); //TODO : should we use the "entry" ?
			//Token is typed in order to filter it later			
			tokenType = currentSolution.getStemPOS();
			//OK : we're done with this solution
			tokenSolutions.remove(0);
			//Will there be further treatment ?
			processingToken = !tokenSolutions.isEmpty();
		}
		//It should not be normally possible !
		catch (IndexOutOfBoundsException e) {
			System.err.println("Something went wrong in nextSolution");
			processingToken = false;
			//Re-emit the same token text (romanized) : not the best solution :-(
			tokenText = romanizedToken;
			tokenType = "PLACE_HOLDER";
		}
		emittedToken = new Token(tokenText,receivedToken.startOffset(),receivedToken.endOffset(),tokenType);
		if (!firstOne) emittedToken.setPositionIncrement(0);
		if (debug) System.out.println(emittedToken.termText() + "\t" + emittedToken.type() + "\t" + "[" + emittedToken.startOffset() + "-" + emittedToken.endOffset() + "]" + "\t" + emittedToken.getPositionIncrement());
		return emittedToken;
	}
	
	/** Returns the next token in the stream, or <CODE>null</CODE> at EOS.
	 * @throws IOException If a problem occurs
	 * @return The token with its <CODE>type</CODE> set to the morphological identification of the
	 * <STRONG>stem</STRONG>. Tokens with no morphological identification have their <CODE>type</CODE> set to
	 * <CODE>NO_RESULT</CODE>. Token's termText is the romanized form of the
	 * <STRONG>stem</STRONG>
	 * @see org.apache.lucene.analysis.Token#type()
	 */
	public final Token next() throws IOException {
		//If no token is currently processed, fetch another one
		if (!processingToken) {
			receivedToken = input.next();
			if (receivedToken == null) return null;
			romanizedToken = araMorph.romanizeWord(receivedToken.termText());
			//Analyse it (in arabic)
			if (araMorph.analyzeToken(receivedToken.termText())) {
				tokenSolutions = new ArrayList(araMorph.getWordSolutions(romanizedToken));
				
				//DEBUG : this does actually nothing, good place for a breakpoint
				if (tokenSolutions.isEmpty()) { //oh, no !
					tokenSolutions.clear();
				}
				
				return nextSolution(true);
			}
			else {
				processingToken = false;
				if (debug) System.out.println(romanizedToken + "\t" + "NO_RESULT");
				return new Token(romanizedToken,receivedToken.startOffset(),receivedToken.endOffset(),"NO_RESULT");
			}
		}
		//Continue to process the current token
		else return nextSolution(false);
	}
}


