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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.MultiHashMap;

/** An in-memory dictionary of prefixes, stems, suffixes and combinations fed with
 * resources avalaible in the classpath.
 * TODO : use a Lucene index ;-) or any other fast-access resources.
 *@author Pierrick Brihaye, 2003
 */
class InMemoryDictionaryHandler {
	
	/** The unique instance of this handler. */
	private static InMemoryDictionaryHandler handler = null;
	/** Dictionary of prefixes */
	private static MultiHashMap prefixes = new MultiHashMap(78);
	/** Dictionary of stems */
	private static MultiHashMap stems = new MultiHashMap(47261);
	/** Dictionary of suffixes */
	private static MultiHashMap suffixes = new MultiHashMap(206);
	/** Compatibility table for prefixes-stems combinations.
	 * TODO : definitely not the best container
	 */
	private static HashSet hash_AB = new HashSet(1648);
	/** Compatibility table for prefixes-suffixes combinations.
	 * TODO : definitely not the best container
	 */
	private static HashSet hash_AC = new HashSet(598);
	/** Compatibility table for stems-suffixes combinations.
	 * TODO : definitely not the best container
	 */
	private static HashSet hash_BC = new HashSet(1285);
	
	/** Private constructor to avoid multiple instanciations. */
	private InMemoryDictionaryHandler() {
		System.out.println("Initializing in-memory dictionary handler...");
		// load 3 lexicons
		loadDictionary(prefixes, "dictPrefixes", this.getClass().getResourceAsStream("dictionaries/dictPrefixes"));
		loadDictionary(stems, "dictStems", this.getClass().getResourceAsStream("dictionaries/dictStems"));
		loadDictionary(suffixes, "dictSuffixes", this.getClass().getResourceAsStream("dictionaries/dictSuffixes"));
		//load 3 compatibility tables
		loadCompatibilityTable(hash_AB, "tableAB", this.getClass().getResourceAsStream("dictionaries/tableAB"));
		loadCompatibilityTable(hash_AC, "tableAC", this.getClass().getResourceAsStream("dictionaries/tableAC"));
		loadCompatibilityTable(hash_BC, "tableBC", this.getClass().getResourceAsStream("dictionaries/tableBC"));
		handler = this;
		System.out.println("... done.");
	};
	
	/** Returns a unique instance of the handler.
	 * @return The instance
	 */
	protected static synchronized InMemoryDictionaryHandler getHandler() {
		if (handler == null) return new InMemoryDictionaryHandler();
		else return handler;
	}
	
	/** Whether or not the prefix is in the dictionary.
	 * @param translitered The prefix
	 * @return The result
	 */
	protected static boolean hasPrefix(String translitered) {
		return prefixes.containsKey(translitered);
	}
	
	/** Returns an iterator on the solutions for the given prefix.
	 * @param translitered The prefix
	 * @return The iterator
	 */
	protected Iterator getPrefixIterator(String translitered) {
		if (!prefixes.containsKey(translitered)) return null;
		else return ((Collection)prefixes.get(translitered)).iterator();
	}
	
	/** Whether or not the stem is in the dictionary.
	 * @param translitered The stem
	 * @return The result
	 */
	protected static boolean hasStem(String translitered) {
		return stems.containsKey(translitered);
	}
	
	/** Returns an iterator on the solutions for the given stem.
	 * @param translitered The stem
	 * @return The iterator
	 */
	protected Iterator getStemIterator(String translitered) {
		if (!stems.containsKey(translitered)) return null;
		else return ((Collection)stems.get(translitered)).iterator();
	}
	
	/** Whether or not the suffix is in the dictionary.
	 * @param translitered The suffix
	 * @return The result
	 */
	protected static boolean hasSuffix(String translitered) {
		return suffixes.containsKey(translitered);
	}
	
	/** Returns an iterator on the solutions for the given suffix.
	 * @param translitered The suffix
	 * @return The iterator
	 */
	protected Iterator getSuffixIterator(String translitered) {
		if (!suffixes.containsKey(translitered)) return null;
		else return ((Collection)suffixes.get(translitered)).iterator();
	}
	
	/** Whether or not the prefix/stem combination is possible.
	 * @param AB The prefix and stem combination.
	 * @return The result
	 */
	protected static boolean hasAB(String A, String B) {
		return hash_AB.contains(A + " " + B);
	}
	
	/** Whether or not the prefix/suffix combination is possible.
	 * @param AC The prefix and suffix combination.
	 * @return The result
	 */
	protected static boolean hasAC(String A, String C) {
		return hash_AC.contains(A + " " + C);
	}
	
	/** Whether or not the stem/suffix combination is possible.
	 * @param BC The stem and suffix combination.
	 * @return The result
	 */
	protected static boolean hasBC(String B, String C) {
		return hash_BC.contains(B + " " + C);
	}
	
	/** Loads a dictionary into a <CODE>Set</CODE> where the <PRE>key</PRE> is entry and its <PRE>value</PRE> is a
	 * <CODE>List</CODE> (each entry can have multiple values)
	 * @param set The set
	 * @param name A human-readable name
	 * @param is The stream
	 * @throws RuntimeException If a problem occurs when reading the dictionary
	 */
	private void loadDictionary(Map set, String name, InputStream is) throws RuntimeException { //TODO : should be static
		HashSet lemmas = new HashSet();
		int forms = 0;
		String lemmaID = "";
		System.out.print("Loading dictionary : " + name + " ");
		try {
			LineNumberReader IN = new LineNumberReader(new InputStreamReader(is,"ISO8859_1"));
			String line = null;
			while ((line = IN.readLine()) != null) {
				if ((IN.getLineNumber() % 1000) == 1) System.out.print(".");
				// new lemma
				if (line.startsWith(";; ")) {
					lemmaID = line.substring(3);
					// lemmaID's must be unique
					if (lemmas.contains(lemmaID))
						throw new RuntimeException("Lemma " + lemmaID + "in " + name + " (line " + IN.getLineNumber() + ") isn't unique");
					lemmas.add(lemmaID);
				}
				// comment
				else if (line.startsWith(";")) {}
				else {
					String split[] = line.split("\t",-1); //-1 to avoid triming of trail values
					
					//a little error-checking won't hurt :
					if (split.length != 4) {
						throw new RuntimeException("Entry in " + name + " (line " + IN.getLineNumber() + ") doesn't have 4 fields (3 tabs)");
					}
					String entry = split[0]; // get the entry for use as key
					String voc = split[1];
					String cat = split[2];
					String glossPOS = split[3];
					
					String gloss;
					String POS;
					
					Pattern p;
					Matcher m;
					
					// two ways to get the POS info:
					// (1) explicitly, by extracting it from the gloss field:
					p = Pattern.compile(".*" + "<pos>(.+?)</pos>" + ".*");
					m = p.matcher(glossPOS);
					if (m.matches()) {
						POS = m.group(1); //extract POS from glossPOS
						gloss = glossPOS; //we clean up the gloss later (see below)
					}
					// (2) by deduction: use the cat (and sometimes the voc and gloss) to deduce the appropriate POS
					else {
						// we need the gloss to guess proper names
						gloss = glossPOS; 
						// null prefix or suffix
						if (cat.matches("^(Pref-0|Suff-0)$")) { 
							POS = "";
						}
						else if (cat.matches("^F" + ".*")) {
							POS = voc + "/FUNC_WORD";
						}
						else if (cat.matches("^IV" + ".*")) {
							POS = voc + "/VERB_IMPERFECT";
						}
						else if (cat.matches("^PV" + ".*")) {
							POS = voc + "/VERB_PERFECT";
						}
						else if (cat.matches("^CV" + ".*")) {
							POS = voc + "/VERB_IMPERATIVE";
						}						
						else if (cat.matches("^N" + ".*")) {
							// educated guess (99% correct)
							if (gloss.matches("^[A-Z]" + ".*")) {
								POS = voc + "/NOUN_PROP";
							}
							// (was NOUN_ADJ: some of these are really ADJ's and need to be tagged manually)
							else if (voc.matches(".*" + "iy~$")) { 
								POS = voc + "/NOUN";
							}
							else 
								POS = voc + "/NOUN";
						}
						else {
							throw new RuntimeException("No POS can be deduced in " + name + " (line " + IN.getLineNumber() + ")");
						}
					}
					
					// clean up the gloss: remove POS info and extra space, and convert upper-ASCII  to lower (it doesn't convert well to UTF-8)
					gloss = gloss.replaceFirst("<pos>.+?</pos>","");
					gloss = gloss.trim();
					//TODO : we definitely need a translate() method in the java packages !
					gloss = gloss.replaceAll(";","/"); //TODO : is it necessary ?
					gloss = gloss.replaceAll("À","A");
					gloss = gloss.replaceAll("Á","A");
					gloss = gloss.replaceAll("Â","A");
					gloss = gloss.replaceAll("Ã","A");
					gloss = gloss.replaceAll("Ä","A");
					gloss = gloss.replaceAll("Å","A");
					gloss = gloss.replaceAll("Ç","C");
					gloss = gloss.replaceAll("È","E");
					gloss = gloss.replaceAll("É","E");
					gloss = gloss.replaceAll("Ê","E");
					gloss = gloss.replaceAll("Ë","E");
					gloss = gloss.replaceAll("Ì","I");
					gloss = gloss.replaceAll("Í","I");
					gloss = gloss.replaceAll("Î","I");
					gloss = gloss.replaceAll("Ï","I");
					gloss = gloss.replaceAll("Ñ","N");
					gloss = gloss.replaceAll("Ò","O");
					gloss = gloss.replaceAll("Ó","O");
					gloss = gloss.replaceAll("Ô","O");
					gloss = gloss.replaceAll("Õ","O");
					gloss = gloss.replaceAll("Ö","O");
					gloss = gloss.replaceAll("Ù","U");
					gloss = gloss.replaceAll("Ú","U");
					gloss = gloss.replaceAll("Û","U");
					gloss = gloss.replaceAll("Ü","U");
					gloss = gloss.replaceAll("à","a");
					gloss = gloss.replaceAll("á","a");
					gloss = gloss.replaceAll("â","a");
					gloss = gloss.replaceAll("ã","a");
					gloss = gloss.replaceAll("ä","a");
					gloss = gloss.replaceAll("å","a");
					gloss = gloss.replaceAll("ç","c");
					gloss = gloss.replaceAll("è","e");
					gloss = gloss.replaceAll("é","e");
					gloss = gloss.replaceAll("ê","e");
					gloss = gloss.replaceAll("ë","e");
					gloss = gloss.replaceAll("ì","i");
					gloss = gloss.replaceAll("í","i");
					gloss = gloss.replaceAll("î","i");
					gloss = gloss.replaceAll("ï","i");
					gloss = gloss.replaceAll("ñ","n");
					gloss = gloss.replaceAll("ò","o");
					gloss = gloss.replaceAll("ó","o");
					gloss = gloss.replaceAll("ô","o");
					gloss = gloss.replaceAll("õ","o");
					gloss = gloss.replaceAll("ö","o");
					gloss = gloss.replaceAll("ù","u");
					gloss = gloss.replaceAll("ú","u");
					gloss = gloss.replaceAll("û","u");
					gloss = gloss.replaceAll("ü","u");
					gloss = gloss.replaceAll("Æ","AE");
					gloss = gloss.replaceAll("Š","Sh");
					gloss = gloss.replaceAll("Ž","Zh");
					gloss = gloss.replaceAll("ß","ss");
					gloss = gloss.replaceAll("æ","ae");
					gloss = gloss.replaceAll("š","sh");
					gloss = gloss.replaceAll("ž","zh");
					// note that although we read 4 fields from the dict we now save 5 fields in the hash table
					// because the info in last field, glossPOS, was split into two: gloss and POS
					DictionaryEntry de = new DictionaryEntry(entry, lemmaID, voc, cat, gloss, POS);
					if (set.containsKey(entry)) {
						((Collection)set.get(entry)).add(de);
					}
					else set.put(entry, de);
					forms++;
				}
			}
			IN.close();
			System.out.println();
			if (!"".equals(lemmaID)) System.out.print(lemmas.size() + " lemmas and ");
			System.out.println(set.size() + " entries totalizing " + forms + " forms");
		}
		catch (IOException e) {
			throw new RuntimeException("Can not open : " + name);
		}
	}
	
	/** Loads a compatibility table into a <CODE>Set</CODE>.
	 * @param set The set
	 * @param name A human-readable name
	 * @param is The stream
	 * @throws RuntimeException If a problem occurs when reading the compatibility table
	 */
	private static void loadCompatibilityTable(Set set, String name, InputStream is) throws RuntimeException {
		System.out.print("Loading compatibility table : " + name + " ");
		try {
			LineNumberReader IN = new LineNumberReader(new InputStreamReader(is,"ISO8859_1"));
			String line = null;
			while ((line = IN.readLine()) != null) {
				if ((IN.getLineNumber() % 1000) == 1) System.out.print(".");
				if (!line.startsWith(";")) { //Ignore comments
					line = line.trim();
					line = line.replaceAll("\\s+", " ");
					set.add(line);
				}
			}
			IN.close();
			System.out.println();
			System.out.println(set.size() + " entries");
		}
		catch (IOException e) {
			throw new RuntimeException("Can not open : " + name);
		}
	}
	
	
}


