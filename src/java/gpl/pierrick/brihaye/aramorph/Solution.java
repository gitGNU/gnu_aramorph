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

/** A solution for a word.
 * @author Pierrick Brihaye, 2003*/
public class Solution {
	
	private int cnt;
	private DictionaryEntry prefix;
	private DictionaryEntry stem;
	private DictionaryEntry suffix;
	
	/** Constructs a solution for a word.
	 * @param cnt Order in sequence ; not very useful
	 * @param prefix The prefix
	 * @param stem The stem
	 * @param suffix The suffix
	 */
	protected Solution(int cnt, DictionaryEntry prefix, DictionaryEntry stem, DictionaryEntry suffix) {
		this.cnt = cnt;
		this.prefix = prefix;
		this.stem = stem;
		this.suffix = suffix;
	}
	
	/** Returns the order in solutions' sequence.
	 * @return The order in sequence
	 */
	public int getCnt() { return this.cnt; }
	
	/** Returns the lemma id in the dictionary.
	 * @return The lemma ID
	 */
	public String getLemma() {
		return stem.getLemmaID().replaceFirst("(_|-).*$",""); //inconsistent formatting of lemma IDs
	}
	
	/** Returns the vocalization of the prefix.
	 * @return The vocalization
	 */
	public String getPrefixVocalization() {
		return prefix.getVocalization();
	}

	/** Returns the vocalization of the stem.
	 * @return The vocalization
	 */
	public String getStemVocalization() {
		return stem.getVocalization();
	}
	
	/** Returns the vocalization of the suffix.
	 * @return The vocalization
	 */
	public String getSuffixVocalization() {
		return suffix.getVocalization();
	}	
	
	/** Returns the vocalization of the word in the Buckwalter transliteration system.
	 * @return The vocalization
	 */
	public String getWordVocalization() {
		return prefix.getVocalization() + stem.getVocalization() + suffix.getVocalization();
	}

	/** Returns the vocalization of the word.
	 * @return The vocalization
	 */
	public String getWordArabicVocalization() {
		return AraMorph.arabizeWord(prefix.getVocalization() + stem.getVocalization() + suffix.getVocalization());
	}	
	
	/** Returns the morphology of the prefix.
	 * @return The morphology
	 */
	public String getPrefixMorphology() {
		return prefix.getMorphology();
	}

	/** Returns the morphology of the stem.
	 * @return The morphology
	 */
	public String getStemMorphology() {
		return stem.getMorphology();
	}

	/** Returns the morphology of the suffix.
	 * @return The morphology
	 */
	public String getSuffixMorphology() {
		return suffix.getMorphology();
	}
	
	/** Returns the morphology of the word.
	 * @return The morphology
	 */
	public String getWordMorphology() {
		StringBuffer sb = new StringBuffer();
		if (!"".equals(prefix.getMorphology()))
			sb.append("\t" + "Prefix : " + prefix.getMorphology() + "\n");		
		if (!"".equals(stem.getMorphology()))
			sb.append("\t" + "Stem : " + stem.getMorphology() + "\n");
		if (!"".equals(suffix.getMorphology()))
			sb.append("\t" + "Suffix : " + suffix.getMorphology() + "\n");
		return sb.toString();
	}
	
	/** Returns the grammatical category of the prefix.
	 * @return The grammatical category
	 */
	public String getPrefixPOS() {
		return prefix.getPOS().replaceFirst("^.*/","");
	}

	/** Returns the grammatical category of the stem.
	 * @return The grammatical category
	 */
	public String getStemPOS() {
		return stem.getPOS().replaceFirst("^.*/","");
	}

	/** Returns the grammatical category of the suffix.
	 * @return The grammatical category
	 */
	public String getSuffixPOS() {
		return suffix.getPOS().replaceFirst("^.*/","");
	}
	
	/** Returns the word in the Buckwalter transliteration system an its grammatical category.
	 * @return The grammatical category
	 */
	public String getWordPOS() {
		StringBuffer sb = new StringBuffer();
		if (!"".equals(prefix.getPOS()))
			sb.append("\t" + "Prefix : " + prefix.getPOS() + "\n");		
		if (!"".equals(stem.getPOS()))
			sb.append("\t" + "Stem : " + stem.getPOS() + "\n");
		if (!"".equals(suffix.getPOS()))
			sb.append("\t" + "Suffix : " + suffix.getPOS() + "\n");
		return sb.toString();
	}
	
	/** Returns the word in arabic and its grammatical category.
	 * @return The grammatical category
	 */
	public String getWordArabicPOS() {
		StringBuffer sb = new StringBuffer();
		if (!"".equals(prefix.getArabicPOS()))
			sb.append("\t" + "Prefix : " + prefix.getArabicPOS() + "\n");		
		if (!"".equals(stem.getArabicPOS()))
			sb.append("\t" + "Stem : " + stem.getArabicPOS() + "\n");
		if (!"".equals(suffix.getArabicPOS()))
			sb.append("\t" + "Suffix : " + suffix.getArabicPOS() + "\n");
		return sb.toString();
	}		
	
	/** Returns the english glosses of the prefix.
	 * @return The glosses.
	 */
	public String getPrefixGlosses() { return prefix.getGloss(); }
	
	/** Returns a list of the english glosses of the prefix.
	 * @return The glosses
	 */
	public String[] getPrefixGlossesList() {
		String[] glosses = prefix.getGloss().split("[/()]");
		if (glosses.length > 0) return glosses;
		//return at least one gloss
		String[] gloss = new String[1];
		glosses[0] = prefix.getGloss();
		return glosses;
	}

	/** Returns the english glosses of the stem.
	 * @return The glosses.
	 */
	public String getStemGlosses() { return stem.getGloss(); }
	
	/** Returns a list of the english glosses of the stem.
	 * @return The glosses
	 */
	public String[] getStemGlossesList() {
		String[] glosses = stem.getGloss().split("[/()]");		
		if (glosses.length > 0) return glosses;
		//return at least one gloss
		String[] gloss = new String[1];
		glosses[0] = stem.getGloss();
		return glosses;
	}
	
	/** Returns the english glosses of the suffix.
	 * @return The glosses.
	 */
	public String getSuffixGlosses() { return suffix.getGloss(); }
	
	/** Returns a list of the english glosses of the suffix.
	 * @return The glosses
	 */
	public String[] getSuffixGlossesList() {
		String[] glosses = suffix.getGloss().split("[/()]");
		if (glosses.length > 0) return glosses;
		//return at least one gloss
		String[] gloss = new String[1];
		glosses[0] = suffix.getGloss();
		return glosses;
	}	
	
	/** Returns the english glosses of the word.
	 * @return The glosses.
	 */
	public String getWordGlosses() {
		StringBuffer sb = new StringBuffer();
		if (!"".equals(prefix.getGloss()))
			sb.append("\t" + "Prefix : " + prefix.getGloss() + "\n");		
		if (!"".equals(stem.getGloss()))
			sb.append("\t" + "Stem : " + stem.getGloss() + "\n");
		if (!"".equals(suffix.getGloss()))
			sb.append("\t" + "Suffix : " + suffix.getGloss() + "\n");
		return sb.toString();		
	}
	
	/** Returns the prefix of the word.
	 * @return The prefix
	 */
	public DictionaryEntry getPrefix() { return this.prefix; }
	
	/** Returns the stem of the word.
	 * @return The stem
	 */
	public DictionaryEntry getStem() { return this.stem; }
	
	/** Returns the suffix of the word.
	 * @return The suffix
	 */
	public DictionaryEntry getSuffix() { return this.suffix; }
	
	/** Returns a string representation of how the word can be analyzed using the Buckwalter transliteration system.
	 * @return The representation
	 */
	public String toString() {
		return new String(
		"\n" + "SOLUTION #" + cnt + "\n"
		+ "Lemma  : " + "\t" + getLemma() + "\n"
		+ "Vocalized as : " + "\t" + this.getWordVocalization() + "\n"
		+ "Morphology : " + "\n"
		+ this.getWordMorphology()
		+ "Grammatical category : " + "\n"
		+ this.getWordPOS()
		+ "Glossed as : " + "\n"
		+ this.getWordGlosses()
		);
	}
	
	/** Returns a string representation of how the word can be analyzed using arabic wherever possible.
	 * @return The representation,
	 */
	public String toArabizedString() {
		return new String(
		"\n" + "SOLUTION #" + cnt + "\n"
		+ "Lemma  : " + "\t" + getLemma() + "\n"
		+ "Vocalized as : " + "\t" + this.getWordArabicVocalization() + "\n"
		+ "Morphology : " + "\n"
		+ this.getWordMorphology()
		+ "Grammatical category : " + "\n"
		+ this.getWordArabicPOS()
		+ "Glossed as : " + "\n"
		+ this.getWordGlosses()
		);
	}	
}



