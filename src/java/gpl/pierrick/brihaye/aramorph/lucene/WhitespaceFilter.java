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

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

/**
 * Normalizes token text to whitespaces
 */
public final class WhitespaceFilter extends TokenFilter {
	
	private Token receivedToken = null;
	private StringBuffer receivedText = new StringBuffer();	
	
	/** Constructs a filter which tokenizes words from the input stream.
	 * @param input The token stream from a tokenizer
	 */
	public WhitespaceFilter(TokenStream input){
		super(input);
	}
	
	private String getNextPart() {		
		StringBuffer emittedText = new StringBuffer();
		//left trim the token
		while(true) {
			if (receivedText.length() == 0) break;
			char c = receivedText.charAt(0);			
			if (Character.isLetterOrDigit(c)) break;
			receivedText.deleteCharAt(0);		
		}				
		//keep the good stuff
		while(true) {
			if (receivedText.length() == 0) break;
			char c = receivedText.charAt(0);
			if (!Character.isLetterOrDigit(c)) break;
			emittedText.append(receivedText.charAt(0));
			receivedText.deleteCharAt(0);			
		}		
		//right trim the token
		while(true) {
			if (receivedText.length() == 0) break;
			char c = receivedText.charAt(0);
			if (Character.isLetterOrDigit(c)) break;
			receivedText.deleteCharAt(0);		
		}				
		return emittedText.toString();
	}
	
	/** Returns the next word in the stream.
	 * @throws IOException If a problem occurs
	 * @return The word
	 */
	public final Token next() throws IOException {
		
		boolean newToken = false;
		
		while (true) {				
			//New token ?
			if (receivedText.length() == 0) {
				receivedToken = input.next();			
				newToken = true;
				if (receivedToken == null)	return null;
				receivedText.append(receivedToken.termText());				
			}
			String emittedText = getNextPart();						
			if (emittedText.length() > 0) {				
				//TODO : should we untype the token and let the next Filters operate ?
				Token emittedToken = new Token(emittedText, receivedToken.startOffset(), receivedToken.endOffset(), receivedToken.type());								
				if (newToken) emittedToken.setPositionIncrement(receivedToken.getPositionIncrement());				
				else emittedToken.setPositionIncrement(0);				
				return emittedToken;
			}				
		}
	}
}
