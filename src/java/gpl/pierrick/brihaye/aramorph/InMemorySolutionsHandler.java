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

import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

/** An in-memory handler for managing solutions found by the morphological analyzer.
 * @see org.apache.lucene.analysis.ar.aramorph.AraMorph
 *@author Pierrick Brihaye, 2003
 */
class InMemorySolutionsHandler {
	
	/** The unique instance of this handler. */
	private static InMemorySolutionsHandler handler = null;
	
	//Cache (TODO : consider LRU maps)
	
	/** Solutions for analyzed words.
	 * <PRE>key</PRE> = word
	 * <PRE>value</PRE> = set of solutions (can be empty)
	 */
	private static Hashtable solutions = new Hashtable();
	/** Alternative spellings for analyzed words.
	 * <PRE>key</PRE> = word
	 * <PRE>value</PRE> = set of alternative spellings (can be empty)
	 */
	private static Hashtable alternativeSpellings = new Hashtable();
	
	/** Private constructor to avoid multiple instanciations. */
	private InMemorySolutionsHandler() {
		System.out.println("Initializing in-memory solutions handler...");
		handler = this;
		System.out.println("... done.");
	};
	
	/** Returns a unique instance of the handler.
	 * @return The instance
	 */
	protected static synchronized InMemorySolutionsHandler getHandler() {
		if (handler == null) return new InMemorySolutionsHandler();
		else return handler;
	}
	
	/** Add solutions for the given word.
	 * @param translitered The word
	 * @param sol The solutions
	 */
	protected static synchronized void addSolutions(String translitered, HashSet sol) {
		solutions.put(translitered, sol);
	}
	
	/** Whether or not the word already gave solutions.
	 * @param translitered The word
	 * @return The result
	 */
	protected static boolean hasSolutions(String translitered) {
		return solutions.containsKey(translitered);
	}
	
	/** Returns an iterator on the solutions of the given word.
	 * @param translitered The word
	 * @return The iterator
	 */
	protected static Iterator getSolutionsIterator(String translitered) {
		if (!solutions.containsKey(translitered)) return null;
		else return ((Collection)solutions.get(translitered)).iterator();
	}
	
	/** Add alternative spellings for the given word.
	 * @param translitered The word
	 * @param alt The alternative spellings
	 */
	protected static synchronized void addAlternativeSpellings(String translitered, HashSet alt) {
		alternativeSpellings.put(translitered, alt);
	}
	
	/** Whether or not the word already gave alternative spellings.
	 * @param translitered The word
	 * @return The result
	 */
	protected static boolean hasAlternativeSpellings(String translitered) {
		return alternativeSpellings.containsKey(translitered);
	}
	
	/** Returns an iterator on the alternative spellings of the given word.
	 * @param translitered The word
	 * @return The iterator
	 */
	protected static Iterator getAlternativeSpellingsIterator(String translitered) {
		if (!alternativeSpellings.containsKey(translitered)) return null;
		return ((Collection)alternativeSpellings.get(translitered)).iterator();
	}
	
}





