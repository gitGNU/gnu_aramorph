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

/** An abstraction of a dictionary entry for a word. 
 @author Pierrick Brihaye, 2003
 */
class DictionaryEntry {
	
	private String entry;
	private String lemmaID;
	private String vocalization;
	private String cat;
	private String gloss;
	private String POS;
	
	protected DictionaryEntry(String entry, String lemmaID, String vocalization, String cat, String gloss, String POS) {
		this.entry = entry;
		this.lemmaID = lemmaID;
		this.vocalization = vocalization;
		this.cat = cat;
		this.gloss = gloss;
		this.POS = POS;
	}
	
	protected String getEntry() { return this.entry; }
	
	protected String getLemmaID() { return this.lemmaID; }
	
	protected String getVocalization() { return this.vocalization; }
	
	protected String getCat() {	return this.cat; }
	
	protected String  getPOS() { return this.POS; }
	
	protected String getGloss() { return this.gloss; }
	
}



