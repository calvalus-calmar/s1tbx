/*
 * Copyright (C) 2015 by Array Systems Computing Inc. http://www.array.ca
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */
package org.csa.rstb.io.radarsat2;

import org.esa.snap.core.dataio.DecodeQualification;
import org.esa.snap.core.dataio.ProductReader;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.util.io.SnapFileFilter;
import org.esa.snap.engine_utilities.gpf.ReaderUtils;
import org.esa.snap.engine_utilities.util.ZipUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.Locale;

/**
 * The ReaderPlugIn for Radarsat2 products.
 */
public class Radarsat2ProductReaderPlugIn implements ProductReaderPlugIn {

    /**
     * Checks whether the given object is an acceptable input for this product reader and if so, the method checks if it
     * is capable of decoding the input's content.
     *
     * @param input any input object
     * @return true if this product reader can decode the given input, otherwise false.
     */
    public DecodeQualification getDecodeQualification(final Object input) {
        final Path path = ReaderUtils.getPathFromInput(input);
        if (path != null) {
            final File metadataFile = findMetadataFile(path);
            if (metadataFile != null) {

                final File[] files = metadataFile.getParentFile().listFiles();
                if(files != null) {
                    for (File f : files) {
                        final String name = f.toPath().getFileName().toString().toLowerCase();
                        if (name.startsWith("lut") || name.startsWith("imagery")) {
                            return DecodeQualification.INTENDED;
                        }
                        if (name.endsWith("ntf")) {
                            return DecodeQualification.SUITABLE;
                        }
                    }
                }
                if(metadataFile.getParent().toUpperCase().startsWith("RS2")) {
                    return DecodeQualification.INTENDED;
                }
                return DecodeQualification.SUITABLE;
            }
            final String filename = path.getFileName().toString().toLowerCase();
            if (filename.endsWith(".zip") && filename.startsWith("rs2") &&
                    ZipUtils.findInZip(path.toFile(), "", Radarsat2Constants.PRODUCT_HEADER_NAME)) {
                return DecodeQualification.INTENDED;
            }
        }
        //todo zip stream

        return DecodeQualification.UNABLE;
    }

    public static File findMetadataFile(final Path folderPath) {
        final File folder = folderPath.toFile();
        if (folder.isDirectory()) {
            final File[] fileList = folder.listFiles();
            if (fileList != null) {
                for (File f : fileList) {
                    final String fileName = f.toPath().getFileName().toString().toLowerCase();
                    if (fileName.equals(Radarsat2Constants.PRODUCT_HEADER_NAME) ||
                            fileName.equalsIgnoreCase(Radarsat2Constants.RSM_SIM_PRODUCT_HEADER_NAME)) {
                        return f;
                    }
                    // don't search deeper
//                    if (f.isDirectory()) {
//                        final File foundFile = findMetadataFile(f.toPath());
//                        if (foundFile != null) {
//                            return foundFile;
//                        }
//                    }
                }
            }
        } else {
            final String fileName = folderPath.getFileName().toString().toLowerCase();
            if (fileName.equals(Radarsat2Constants.PRODUCT_HEADER_NAME) ||
                    fileName.equalsIgnoreCase(Radarsat2Constants.RSM_SIM_PRODUCT_HEADER_NAME)){
                return folder;
            }
        }
        return null;
    }

    /**
     * Returns an array containing the classes that represent valid input types for this reader.
     * <p>
     * <p> Intances of the classes returned in this array are valid objects for the <code>setInput</code> method of the
     * <code>ProductReader</code> interface (the method will not throw an <code>InvalidArgumentException</code> in this
     * case).
     *
     * @return an array containing valid input types, never <code>null</code>
     */
    public Class[] getInputTypes() {
        return Radarsat2Constants.VALID_INPUT_TYPES;
    }

    /**
     * Creates an instance of the actual product reader class. This method should never return <code>null</code>.
     *
     * @return a new reader instance, never <code>null</code>
     */
    public ProductReader createReaderInstance() {
        return new Radarsat2ProductReader(this);
    }

    public SnapFileFilter getProductFileFilter() {
        return new FileFilter();
    }

    /**
     * Gets the names of the product formats handled by this product I/O plug-in.
     *
     * @return the names of the product formats handled by this product I/O plug-in, never <code>null</code>
     */
    public String[] getFormatNames() {
        return Radarsat2Constants.getFormatNames();
    }

    /**
     * Gets the default file extensions associated with each of the format names returned by the <code>{@link
     * #getFormatNames}</code> method. <p>The string array returned shall always have the same length as the array
     * returned by the <code>{@link #getFormatNames}</code> method. <p>The extensions returned in the string array shall
     * always include a leading colon ('.') character, e.g. <code>".hdf"</code>
     *
     * @return the default file extensions for this product I/O plug-in, never <code>null</code>
     */
    public String[] getDefaultFileExtensions() {
        return Radarsat2Constants.getFormatFileExtensions();
    }

    /**
     * Gets a short description of this plug-in. If the given locale is set to <code>null</code> the default locale is
     * used.
     * <p>
     * <p> In a GUI, the description returned could be used as tool-tip text.
     *
     * @param locale the local for the given decription string, if <code>null</code> the default locale is used
     * @return a textual description of this product reader/writer
     */
    public String getDescription(final Locale locale) {
        return Radarsat2Constants.getPluginDescription();
    }

    public static class FileFilter extends SnapFileFilter {

        public FileFilter() {
            super();
            setFormatName(Radarsat2Constants.getFormatNames()[0]);
            setExtensions(Radarsat2Constants.getFormatFileExtensions());
            setDescription(Radarsat2Constants.getPluginDescription());
        }

        /**
         * Tests whether or not the given file is accepted by this filter. The default implementation returns
         * <code>true</code> if the given file is a directory or the path string ends with one of the registered extensions.
         * if no extension are defined, the method always returns <code>true</code>
         *
         * @param file the file to be or not be accepted.
         * @return <code>true</code> if given file is accepted by this filter
         */
        public boolean accept(final File file) {
            if (super.accept(file)) {
                final String name = file.getName().toUpperCase();
                if (file.isDirectory() ||
                        (name.startsWith(Radarsat2Constants.PRODUCT_HEADER_PREFIX) && name.endsWith(Radarsat2Constants.getIndicationKey())) ||
                        (name.startsWith("RS2") && name.endsWith(".ZIP"))) {
                    return true;
                }
            }
            return false;
        }
    }
}
