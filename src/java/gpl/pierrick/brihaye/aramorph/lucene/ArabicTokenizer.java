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
import java.io.Reader;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.Tokenizer;

/** A tokenizer that will return tokens in the arabic alphabet. This tokenizer
 * is a bit rude since it also filters digits and punctuation, even in an arabic
 * part of stream. Well... I've planned to write a
 * "universal", highly configurable, character tokenizer.
 * @author Pierrick Brihaye, 2003
 */
public class ArabicTokenizer extends Tokenizer {
	
	private int offset = 0, bufferIndex=0, dataLen=0;
	private static final int MAX_WORD_LEN = 255;
	private static final int IO_BUFFER_SIZE = 1024;
	private final char[] buffer = new char[MAX_WORD_LEN];
	private final char[] ioBuffer = new char[IO_BUFFER_SIZE];
	
	private boolean debug = false;
	
	/** Constructs a tokenizer that will return tokens in the arabic alphabet.
	 * @param input The reader
	 */	
	public ArabicTokenizer(Reader input) {
		this(input, false);
	}
	
	/** Constructs a tokenizer that will return tokens in the arabic alphabet.
	 * @param input The reader
	 * @param debug Whether or not the tokenizer should display convenience messages on <CODE>System.out</CODE>
	 */	
	public ArabicTokenizer(Reader input, boolean debug) {
		super(input);
		this.debug = debug;
	}
	
	/** Whether or not a character is in the arabic alphabet.
	 * @param c The char
	 * @return The result
	 */
	protected boolean isArabicChar(char c) {
		/*
		TODO : deal with digits
		Arabic digits (yes !) are not in use in Middle-West (from Egypt to Iraq) ;
		Only hindic digits should de considered
		Arabic digits as well as hindic digits are in use in the Maghreb (from Morocco to Lybia)
		We should have an option to set the digit processing
		 */
		if (c == '\u067E') return true; //U+067E : ARABIC LETTER PEH
		if (c == '\u0679') return true; //U+0679 : ARABIC LETTER TTEH
		if (c == '\u0686') return true; //U+0686 : ARABIC LETTER TCHEH
		if (c == '\u0698') return true; //U+0698 : ARABIC LETTER JEH
		if (c == '\u0688') return true; //U+0688 : ARABIC LETTER DDAL
		if (c == '\u06AF') return true; //U+06AF : ARABIC LETTER GAF
		if (c == '\u06A9') return true; //U+06A9 : ARABIC LETTER KEHEH
		if (c == '\u0691') return true; //U+0691 : ARABIC LETTER RREH
		if (c == '\u06BA') return true; //U+06BA : ARABIC LETTER NOON GHUNNA
		//if (c == '\u060C') return true; //U+060C : ARABIC COMMA
		if (c == '\u06BE') return true; //U+06BE : ARABIC LETTER HEH DOACHASHMEE
		//if (c == '\u061B') return true; //U+061B : ARABIC SEMICOLON
		//if (c == '\u061F') return true; //U+061F : ARABIC QUESTION MARK
		if (c == '\u06C1') return true; //U+06C1 : ARABIC LETTER HEH GOAL
		if (c == '\u0621') return true; //U+0621 : ARABIC LETTER HAMZA
		if (c == '\u0622') return true; //U+0622 : ARABIC LETTER ALEF WITH MADDA ABOVE
		if (c == '\u0623') return true; //U+0623 : ARABIC LETTER ALEF WITH HAMZA ABOVE
		if (c == '\u0624') return true; //U+0624 : ARABIC LETTER WAW WITH HAMZA ABOVE
		if (c == '\u0625') return true; //U+0625 : ARABIC LETTER ALEF WITH HAMZA BELOW
		if (c == '\u0626') return true; //U+0626 : ARABIC LETTER YEH WITH HAMZA ABOVE
		if (c == '\u0627') return true; //U+0627 : ARABIC LETTER ALEF
		if (c == '\u0628') return true; //U+0628 : ARABIC LETTER BEH
		if (c == '\u0629') return true; //U+0629 : ARABIC LETTER TEH MARBUTA
		if (c == '\u062A') return true; //U+062A : ARABIC LETTER TEH
		if (c == '\u062B') return true; //U+062B : ARABIC LETTER THEH
		if (c == '\u062C') return true; //U+062C : ARABIC LETTER JEEM
		if (c == '\u062D') return true; //U+062D : ARABIC LETTER HAH
		if (c == '\u062E') return true; //U+062E : ARABIC LETTER KHAH
		if (c == '\u062F') return true; //U+062F : ARABIC LETTER DAL
		if (c == '\u0630') return true; //U+0630 : ARABIC LETTER THAL
		if (c == '\u0631') return true; //U+0631 : ARABIC LETTER REH
		if (c == '\u0632') return true; //U+0632 : ARABIC LETTER ZAIN
		if (c == '\u0633') return true; //U+0633 : ARABIC LETTER SEEN
		if (c == '\u0634') return true; //U+0634 : ARABIC LETTER SHEEN
		if (c == '\u0635') return true; //U+0635 : ARABIC LETTER SAD
		if (c == '\u0636') return true; //U+0636 : ARABIC LETTER DAD
		if (c == '\u0637') return true; //U+0637 : ARABIC LETTER TAH
		if (c == '\u0638') return true; //U+0638 : ARABIC LETTER ZAH
		if (c == '\u0639') return true; //U+0639 : ARABIC LETTER AIN
		if (c == '\u063A') return true; //U+063A : ARABIC LETTER GHAIN
		if (c == '\u0640') return true; //U+0640 : ARABIC TATWEEL
		if (c == '\u0641') return true; //U+0641 : ARABIC LETTER FEH
		if (c == '\u0642') return true; //U+0642 : ARABIC LETTER QAF
		if (c == '\u0643') return true; //U+0643 : ARABIC LETTER KAF
		if (c == '\u0644') return true; //U+0644 : ARABIC LETTER LAM
		if (c == '\u0645') return true; //U+0645 : ARABIC LETTER MEEM
		if (c == '\u0646') return true; //U+0646 : ARABIC LETTER NOON
		if (c == '\u0647') return true; //U+0647 : ARABIC LETTER HEH
		if (c == '\u0648') return true; //U+0648 : ARABIC LETTER WAW
		if (c == '\u0649') return true; //U+0649 : ARABIC LETTER ALEF MAKSURA
		if (c == '\u064A') return true; //U+064A : ARABIC LETTER YEH
		if (c == '\u064B') return true; //U+064B : ARABIC FATHATAN
		if (c == '\u064C') return true; //U+064C : ARABIC DAMMATAN
		if (c == '\u064D') return true; //U+064D : ARABIC KASRATAN
		if (c == '\u064E') return true; //U+064E : ARABIC FATHA
		if (c == '\u064F') return true; //U+064F : ARABIC DAMMA
		if (c == '\u0650') return true; //U+0650 : ARABIC KASRA
		if (c == '\u0651') return true; //U+0651 : ARABIC SHADDA
		if (c == '\u0652') return true; //U+0652 : ARABIC SUKUN
		if (c == '\u06D2') return true; //U+06D2 : ARABIC LETTER YEH BARREE
		if (c == '\u0640') return true; //U+0640 : ARABIC TATWEEL
		if (c == '\u064B') return true; //U+064B : ARABIC FATHATAN
		if (c == '\u064C') return true; //U+064C : ARABIC DAMMATAN
		if (c == '\u064D') return true; //U+064D : ARABIC KASRATAN
		if (c == '\u064E') return true; //U+064E : ARABIC FATHA
		if (c == '\u064F') return true; //U+064F : ARABIC DAMMA
		if (c == '\u0650') return true; //U+0650 : ARABIC KASRA
		if (c == '\u0651') return true; //U+0651 : ARABIC SHADDA
		if (c == '\u0652') return true; //U+0652 : ARABIC SUKUN
		return false;
	}
	
	/** Returns the next token in the stream, or <CODE>null</CODE> at EOS.
	 * @throws IOException If a problem occurs
	 * @return The token with its type set to <CODE>ARABIC</CODE>
	 */
	public Token next() throws IOException {
		int length = 0;
		int start = offset;
		boolean isArabicToken = false;
		while (true) {
			final char c;
			offset++;
			//Is buffer empty ?
			if (bufferIndex >= dataLen) {
				//Get a chunk
				dataLen = input.read(ioBuffer);
				bufferIndex = 0;
			};
			//Test for EOS
			if (dataLen == -1) {
				//Output the current token if any
				if (length > 0)	break;
				//Or return without any token
				else return null;
			}
			else c = (char) ioBuffer[bufferIndex++];
			//If it's an arabic char...
			if (isArabicChar(c)) {
				//Mark it as arabic
				isArabicToken = true;
				//Set start of token
				if (length == 0) start = offset - 1;
				//Buffer it
				buffer[length++] = c;
				//Buffer overflow : output the current token
				if (length == MAX_WORD_LEN) break;
			}
			//Not an arabic char : output the current token if any
			else if (length > 0) break;
		}
		String txt = new String(buffer, 0, length);
		if (debug) System.out.println("Token : " + txt);
		return new Token(txt, start, start+length, "ARABIC");
	}
	
}


