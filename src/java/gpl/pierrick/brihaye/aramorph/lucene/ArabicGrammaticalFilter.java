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

import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

/** A filter that will filter arabic tokens regarding to their grammatical category.
 * @author Pierrick Brihaye, 2003
 */
public class ArabicGrammaticalFilter extends TokenFilter {
	
	/** Whether or not the analyzer should output debug messages */
	protected boolean debug = false;
	
	private String[] WORTHY_CATEGORIES = {
		"ABBREV",
		"ADJ",
		"ADV",		
		"NOUN",
		"NOUN_PROP",				
		"VERB_IMPERATIVE",
		"VERB_IMPERFECT",
		"VERB_PERFECT",
		"NO_RESULT" //Mainly concerns words that are not in the dictionary (e.g. foreign words)
	};
	
	private String[] UNWORTHY_CATEGORIES = {		
		"CONJ",		
		"DEM_PRON_F",
		"DEM_PRON_FS",
		"DEM_PRON_FD",
		"DEM_PRON_MD",
		"DEM_PRON_MP",
		"DEM_PRON_MS",
		"DET",
		"FUNC_WORD",
		"INTERJ",
		"INTERROG",		
		"NO_STEM",	
		"NUMERIC_COMMA",	
		"PART", 
		"PREP",
		"PRON_1P",
		"PRON_1S",
		"PRON_2D",
		"PRON_2FP",
		"PRON_2FS",
		"PRON_2MP",
		"PRON_2MS",
		"PRON_3D",
		"PRON_3FP",
		"PRON_3FS",
		"PRON_3MP",
		"PRON_3MS",
		"REL_PRON",
	};
	
	private ArrayList worthyCategories = null;
	private ArrayList unworthyCategories = null;
	private ArrayList tokenStems = null;
	
	/** Constructs a filter that will filter arabic tokens regarding to their grammatical category.
	 * @param input The token stream from a tokenizer
	 */
	public ArabicGrammaticalFilter(TokenStream input) {
		this(input, false);
	}
	
	/** Constructs a filter that will filter arabic tokens regarding to their grammatical category.
	 * @param input The token stream from a tokenizer
	 * @param debug Whether or not the tokenizer should display convenience messages on <CODE>System.out</CODE>
	 */
	public ArabicGrammaticalFilter(TokenStream input, boolean debug) {
		super(input);
		this.debug = debug;
		worthyCategories = new ArrayList();
		for (int i = 0; i < WORTHY_CATEGORIES.length; i++)
			worthyCategories.add(WORTHY_CATEGORIES[i]);
		unworthyCategories = new ArrayList();
		for (int i = 0; i < UNWORTHY_CATEGORIES.length; i++)
			unworthyCategories.add(UNWORTHY_CATEGORIES[i]);
	}
	
	/** Returns the next token in the stream, or <CODE>null</CODE> at EOS.
	 * @throws IOException If a problem occurs
	 * @return The token
	 */
	public final Token next() throws IOException {
		for (Token token = input.next(); token != null; token = input.next()) {
			//Is it a new token ?
			if (token.getPositionIncrement() != 0) {
				//Creates a new list
				tokenStems = new ArrayList();
			}
			//Is its category of interest ?
			if (worthyCategories.contains((String)token.type())) {
				if (debug) System.out.println(token.termText() + "\t" + token.type() + "\t" + "[" + token.startOffset() + "-" + token.endOffset() + "]" + "\t" + token.getPositionIncrement());
				//Has this termText already been delivered ?
				if (!tokenStems.contains(token.termText())) {
					//Mark it as delivered
					tokenStems.add(token.termText());
					return token; //TODO : how to handle multiple types for the same termText ?
				}
			}
			//Additionnal ckeck for categories that are not yet handled. To be removed when all is tested.
			else if (!unworthyCategories.contains((String)token.type())) {				
				System.err.println("What to do with category : \"" + token.type() + "\" ?");
				//Has this termText already been delivered ?
				if (!tokenStems.contains(token.termText())) {
					//Mark it as delivered
					tokenStems.add(token.termText());
					return token; //TODO : how to handle multiple types for the same termText ?
				}
			}
		}
		//EOS reached
		return null;
	}
}
