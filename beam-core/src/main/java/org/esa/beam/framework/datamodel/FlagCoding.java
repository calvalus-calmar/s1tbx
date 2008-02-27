/*
 * $Id: FlagCoding.java,v 1.1.1.1 2006/09/11 08:16:45 norman Exp $
 *
 * Copyright (C) 2002 by Brockmann Consult (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation. This program is distributed in the hope it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.esa.beam.framework.datamodel;

import org.esa.beam.util.Debug;
import org.esa.beam.util.Guardian;


/**
 * Provides the information required to decode integer sample values that
 * are combined of single flags (bit indexes).
 */
public class FlagCoding extends SampleCoding {

    /**
     * Constructs a new flag coding object with the given name.
     *
     * @param name the name
     */
    public FlagCoding(String name) {
        super(name);
    }

    /**
     * Returns a metadata attribute wich is the representation of the flag with the given name. This method delegates to
     * getPropertyValue(String).
     *
     * @param name the flag name
     * @return a metadata attribute wich is the representation of the flag with the given name
     */
    public MetadataAttribute getFlag(String name) {
        return getAttribute(name);
    }

    /**
     * Returns a string array which contains the names of all flags contained in this <code>FlagCoding</code> object.
     *
     * @return a string array which contains all names of this <code>FlagCoding</code>.<br> If this
     *         <code>FlagCoding</code> does not contain any flag, <code>null</code> is returned
     */
    public String[] getFlagNames() {
        return getAttributeNames();
    }

    /**
     * Adds a new flag definition to this flags coding.
     *
     * @param name        the flag name
     * @param flagMask    the flag's bit mask
     * @param description the description text
     * @throws IllegalArgumentException if <code>name</code> is null
     */
    public void addFlag(String name, int flagMask, String description) {
        addValue(name, flagMask, description);
    }

    /**
     * Returns the flag mask value for the specified flag name.
     *
     * @param name the flag name
     * @return flagMask the flag's bit mask as a 32 bit integer
     * @throws IllegalArgumentException if <code>name</code> is null, or a flag with the name does not exist
     */
    public int getFlagMask(String name) {
        Guardian.assertNotNull("name", name);
        MetadataAttribute attribute = getAttribute(name);
        if (attribute == null) {
            throw new IllegalArgumentException("flag '" + name + "' not found");
        }
        Debug.assertTrue(attribute.getData().isInt());
        Debug.assertTrue(attribute.getData().isScalar());
        return attribute.getData().getElemInt();
    }

    //////////////////////////////////////////////////////////////////////////
    // Visitor-Pattern support

    /**
     * Accepts the given visitor. This method implements the well known 'Visitor' design pattern of the gang-of-four.
     * The visitor pattern allows to define new operations on the product data model without the need to add more code
     * to it. The new operation is implemented by the visitor.
     * <p/>
     * <p>The method simply calls <code>visitor.visit(this)</code>.
     *
     * @param visitor the visitor, must not be <code>null</code>
     */
    @Override
    public void acceptVisitor(ProductVisitor visitor) {
        Guardian.assertNotNull("visitor", visitor);
        visitor.visit(this);
    }
}
