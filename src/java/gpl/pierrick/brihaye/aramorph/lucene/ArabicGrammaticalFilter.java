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

/** A filter that will filter arabic tokens regarding to their morphological category.
 * @author Pierrick Brihaye, 2003
 */
public class ArabicGrammaticalFilter extends TokenFilter {
	
	private boolean debug = false;
	
	private String[] WORTHY_CATEGORIES = {
		"ADJ",
		"ADV",
		"FUT_PART",
		"NOUN",
		"NOUN_PROP",
		"NSUFF_FEM_DU_ACCGEN",
		"NSUFF_FEM_DU_ACCGEN_POSS",
		"NSUFF_FEM_DU_NOM", //not attested in my tests
		"NSUFF_FEM_DU_NOM_POSS",
		"NSUFF_MASC_DU_ACCGEN",
		"NSUFF_MASC_DU_ACCGEN_POSS",
		"NSUFF_MASC_DU_NOM",
		"NSUFF_MASC_DU_NOM_POSS",
		"PART",
		"PVSUFF_SUBJ:1S",
		"PVSUFF_SUBJ:2FP",
		"PVSUFF_SUBJ:2FS",
		"PVSUFF_SUBJ:2MP",
		"PVSUFF_SUBJ:2MS",
		"PVSUFF_SUBJ:3FP",
		"PVSUFF_SUBJ:3FS",
		"PVSUFF_SUBJ:3MP",
		"PVSUFF_SUBJ:3MS",
		"VERB_IMPERATIVE",
		"VERB_IMPERFECT",
		"VERB_PERFECT",
		"NO_RESULT" //Mainly concerns words that are not in the dictionary (e.g. foreign words)
	};
	
	private String[] UNWORTHY_CATEGORIES = {
		"ABBREV",
		"CONJ",
		"DET",
		"DEM_PRON_F",
		"DEM_PRON_FS",
		"DEM_PRON_FD",
		"DEM_PRON_MD",
		"DEM_PRON_MP",
		"DEM_PRON_MS",
		"FUNC_WORD",
		"INTERJ",
		"INTERROG",
		"INTERROG_PART",
		"IVSUFF_SUBJ:MP_MOOD:I", //TODO : check occurences and decide
		"NEG_PART",
		"NUMERIC_COMMA",
		"POSS_PRON_1S",
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
		"REL_PRON+bayona",
		"REL_PRON+bayoni"
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
				System.err.println("What to do with category : " + token.type() + " ?");
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
