/*
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 */
package nexus;

import java.io.IOException;
import java.io.Writer;

/**
 * Represents general info about nexus objects.
 * 
 * @author Richard Holland
 * @author Tobias Thierer
 * @author Jim Balhoff
 * @since 1.6
 */
public interface NexusObject {
	/**
	 * Writes this object to the given writer.
	 * 
	 * @param writer
	 *            the writer to write to.
	 * @throws IOException
	 *             if it could not be written.
	 */
	public void writeObject(Writer writer) throws IOException;
}
