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
import org.apache.lucene.analysis.TokenStream;

/** Analyzer for the arabic language. This analyzer uses Tim Buckwalter's algorithm
 * (available at <a href="http://www.ldc.upenn.edu/Catalog/CatalogEntry.jsp?catalogId=LDC2002L49">LDC
 * Catalog</a>) to identify the morphological category of arabic tokens.
 * The meaningful morphological categories are still to be determined but the current list gives
 * good results.
 * Final tokens are a romanized version of the canonical word.
 * @author Pierrick Brihaye, 2003
 */
public final class ArabicStemAnalyzer extends Analyzer {	
	
	/** Returns a token stream of romanized arabic words whose morphological categories are found to be semantically meaningful.
	 * @return The token stream
	 * @param reader The reader
	 */
    public TokenStream tokenStream(String FieldName, Reader reader)    {
        TokenStream result = new ArabicTokenizer(reader);
		result = new ArabicStemmer(result);
        result = new ArabicGrammaticalFilter(result);
        return result;
    }
	

}

