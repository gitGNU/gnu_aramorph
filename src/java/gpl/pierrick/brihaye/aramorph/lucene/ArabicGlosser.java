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
import java.util.Iterator;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

/** A filter that will return english glosses from arabic stems.*/
public class ArabicGlosser extends TokenFilter {
	
	/** Whether or not the analyzer should output debug messages */
	protected boolean debug = false;
	private AraMorph araMorph = null;
	private String romanizedToken = null;
	private Token receivedToken = null;
	private boolean processingToken = false;
	private ArrayList tokenGlosses = null;
	private ArrayList tokenPOS = null;
	
	/** Constructs a filter that will return english glosses from arabic stems.
	 * @param input The token stream from a tokenizer
	 */
	public ArabicGlosser(TokenStream input) {
		this(input, false);
	}
	
	/** Constructs a filter that will return english glosses from arabic stems.
	 * @param input The token stream from a tokenizer
	 * @param debug Whether or not the tokenizer should display convenience messages on <CODE>System.out</CODE>
	 */
	public ArabicGlosser(TokenStream input, boolean debug) {
		super(input);
		this.debug = debug;
		araMorph = new AraMorph();
	}
	
	/** Returns the arabic morphological analyzer in use.
	 * @return The arabic morphological analyzer
	 * @see gpl.pierrick.brihaye.aramorph.AraMorph
	 */
	public AraMorph getAramorph() { return araMorph; }
	
	/** Returns the next gloss for the given stem.
	 * @param firstOne Whether or not this gloss is the first one
	 * @return The gloss. Its <CODE>termText</CODE> is the gloss of the <STRONG>stem</STRONG>. Its <CODE>type</CODE> is the morphological category of the <STRONG>stem</STRONG>.
	 * When several glosses are available, every emitted token's
	 * <CODE>PositionIncrement</CODE> but the first one is set to <CODE>0</CODE>
	 * @see org.apache.lucene.analysis.Token#setPositionIncrement(int)
	 */
	private Token nextGloss(boolean firstOne) {
		Token emittedToken = null;
		String tokenText = null;
		String tokenType = null;
		try {
			tokenText = (String)tokenGlosses.get(0);			
			//Token is typed in order to filter it later			
			tokenType = (String)tokenPOS.get(0);
			//OK : we're done with this gloss
			tokenGlosses.remove(0);
			tokenPOS.remove(0);
			//Will there be further treatment ?
			processingToken = !tokenGlosses.isEmpty();
		}
		//It should not be normally possible !
		catch (IndexOutOfBoundsException e) {
			System.err.println("Something went wrong in nextGloss");
			processingToken = false;
			//Re-emit the same token text (romanized) : not the best solution :-(
			tokenText = romanizedToken;
			tokenType = "PLACE_HOLDER";
		}
		//TODO : further normalization : remove punctuation and the like
		emittedToken = new Token(tokenText,receivedToken.startOffset(),receivedToken.endOffset(),tokenType);
		if (!firstOne) emittedToken.setPositionIncrement(0);
		if (debug) System.out.println(emittedToken.termText() + "\t" + emittedToken.type() + "\t" + "[" + emittedToken.startOffset() + "-" + emittedToken.endOffset() + "]" + "\t" + emittedToken.getPositionIncrement());
		return emittedToken;
	}
	
	/** Returns the next token in the stream, or <CODE>null</CODE> at EOS.
	 * @throws IOException If a problem occurs
	 * @return The gloss with its <CODE>type</CODE> set to the morphological identification of the
	 * <STRONG>stem</STRONG>. Glosses with no morphological identification have their <CODE>type</CODE> set to
	 * <CODE>NO_RESULT</CODE>. termText() is the english gloss of the
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
			if (araMorph.analyzeToken(receivedToken.termText(), false)) {				
				tokenGlosses = new ArrayList();
				tokenPOS = new ArrayList();
				Iterator it_solutions = araMorph.getWordSolutions(romanizedToken).iterator();
				while (it_solutions != null && it_solutions.hasNext()) {
					Solution currentSolution = (Solution)it_solutions.next();
					for (int i = 0; i < currentSolution.getStemGlossesList().length ; i++) {
						tokenGlosses.add(currentSolution.getStemGlossesList()[i]);
						//same POS for all glosses
						tokenPOS.add(currentSolution.getStemPOS());						
					}					
				}
				
				//DEBUG : this does actually nothing, good place for a breakpoint
				if (tokenGlosses.isEmpty() || tokenPOS.isEmpty()) { //oh, no !
					tokenGlosses.clear();
				}
				
				return nextGloss(true);
			}
			else {
				processingToken = false;
				if (debug) System.out.println(romanizedToken + "\t" + "NO_RESULT");
				return new Token(romanizedToken,receivedToken.startOffset(),receivedToken.endOffset(),"NO_RESULT");
			}
		}
		//Continue to process the current token
		else return nextGloss(false);
	}
}


