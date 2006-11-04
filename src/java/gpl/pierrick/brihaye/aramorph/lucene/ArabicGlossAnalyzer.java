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

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardFilter;

/** An english glosser for the arabic language. This glosser uses Tim Buckwalter's algorithm
 * (available at <a href="http://www.ldc.upenn.edu/Catalog/CatalogEntry.jsp?catalogId=LDC2002L49">LDC
 * Catalog</a>) to identify the grammatical category of arabic tokens and then return their glosses.
 * The significant grammatical categories are still to be determined but the current list gives
 * good results. 
 * @author Pierrick Brihaye, 2003
 */
public final class ArabicGlossAnalyzer extends Analyzer {

    /** An array containing some common english words that are usually not
	useful for searching. */
    public static final String[] STOP_WORDS = {
	"a", "and", "are", "as", "at", "be", "but", "by",
	"for", "if", "in", "into", "is", "it",
	"no", "not", "of", "on", "or", "s", "such",
	"t", "that", "the", "their", "then", "there", "these",
	"they", "this", "to", "was", "will", "with"
    };	
	
	/** Returns a token stream of glosses of arabic words whose grammatical categories are found to be significant.
	 * @return The token stream
	 * @param reader The reader
	 */
	public TokenStream tokenStream(String fieldName, Reader reader)    {
		TokenStream result = new ArabicTokenizer(reader);
		result = new ArabicGlosser(result);
		result = new ArabicGrammaticalFilter(result);	
		result = new WhitespaceFilter(result);
		result = new StandardFilter(result);
		result = new LowerCaseFilter(result);
		result = new StopFilter(result, StopFilter.makeStopSet(STOP_WORDS));			
		return result;
	}	
	
}

