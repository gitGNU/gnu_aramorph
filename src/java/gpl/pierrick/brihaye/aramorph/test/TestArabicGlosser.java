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

package gpl.pierrick.brihaye.aramorph.test;

import gpl.pierrick.brihaye.aramorph.lucene.ArabicGlossAnalyzer;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;

/** Convenient class for testing the arabic/english glosser.
 * @author Pierrick Brihaye, 2003
 */
public class TestArabicGlosser {
	
	/** Display help for command line interface. */
	private static void PrintUsage() {
		System.err.println("Arabic/english glosser Analyzer for Java(tm)");
		System.err.println("Ported to Java(tm) by Pierrick Brihaye, 2003.");		
		System.err.println("Based on :");
		System.err.println("BUCKWALTER ARABIC MORPHOLOGICAL ANALYZER");
		System.err.println("Portions (c) 2002 QAMUS LLC (www.qamus.org),");
		System.err.println("(c) 2002 Trustees of the University of Pennsylvania.");
		System.err.println("This program is governed by :");
		System.err.println("The Apache Software License, Version 1.1");		
		System.err.println("");
		System.err.println("Usage :");
		System.err.println("");
		System.err.println("araMorph inFile [inEncoding] [outFile] [outEncoding] [-v]");
		System.err.println("");
		System.err.println("inFile : file to be analyzed");
		System.err.println("inEncoding : encoding for inFile, default CP1256");
		System.err.println("outFile : result file, default console");
		System.err.println("outEncoding : encoding for outFile, default CP1256");
	}
	
	/** Entry point for command line interface.
	 * @param args Arguments
	 */
	public static void main(String[] args) {
		
		boolean argsOK = true;
		String inputFile = null;
		String outputFile = null;
		String inputEncoding = null;
		String outputEncoding = null;
		
		if (args.length == 0) argsOK = false;
		//TODO : should we be more severe ?
		else {
			for (int i = 0 ; i < args.length ; i++) {
				//is it a charset ?
				try {
					Charset.forName(args[i]);
					if (inputEncoding == null) {
						inputEncoding = args[i];
						continue;
					}
					else if (outputEncoding == null) {
						outputEncoding = args[i];
						continue;
					}
					//too many charsets
					else {
						argsOK = false;
						break;
					}
				}
				catch (IllegalCharsetNameException e) {}
				catch (UnsupportedCharsetException e) {}
				//is it a file name ?
				if (inputFile == null) {
					inputFile = args[i];
					continue;
				}
				else if (outputFile == null) {
					outputFile = args[i];
					continue;
				}
				//too many files
				else {
					argsOK = false;
					break;
				}
			}
		}
		
		if (!argsOK || inputFile == null) PrintUsage();
		else {
			
			if (inputEncoding == null) inputEncoding = "Cp1256"; //TODO : change default ?
			//if (outputEncoding == null) outputEncoding = System.getProperty("file.encoding");
			if (outputEncoding == null) outputEncoding = "Cp1256"; //TODO : change default ?
			BufferedReader IN = null;
			PrintStream ps = null;
			
			if (outputFile != null) {
				try {
					ps = new PrintStream(new FileOutputStream(outputFile), true, outputEncoding);
				}
				catch (FileNotFoundException e) {
					System.err.println("Can't write to output file : " + outputFile);
				}
				catch (UnsupportedEncodingException e) {
					System.err.println("Unsupported output encoding : " + outputEncoding);
				}
			}
			else ps = System.out;
			
			try {
				IN = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile),inputEncoding));
				ArabicGlossAnalyzer arabicGlossAnalyzer = new ArabicGlossAnalyzer();
				TokenStream tokenStream = arabicGlossAnalyzer.tokenStream(null, IN);
				Token token = tokenStream.next();
				while (token != null) {
					ps.println(token.termText() + "\t" + "[" + token.startOffset() + "-" + token.endOffset() + "]" + "\t" + token.type());
					token = tokenStream.next();
				}
			}
			catch (IOException e) {
				throw new RuntimeException("Problem : " + e.getMessage());
			}
			finally {
				try {
					if (IN != null) IN.close();
				}
				catch (IOException e) {}
				if (ps != null) ps.close();
			}
		}
	}
}


