/*
 * $Id: PinTest.java,v 1.1 2007/04/19 10:41:39 norman Exp $
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
package org.esa.beam.visat.toolviews.pin;

import java.awt.Color;
import java.awt.geom.Line2D;
import java.io.StringWriter;
import java.util.Vector;

import junit.framework.TestCase;

import org.esa.beam.dataio.dimap.DimapProductConstants;
import org.esa.beam.framework.datamodel.*;
import org.esa.beam.util.SystemUtils;
import org.esa.beam.util.XmlWriter;
import org.jdom.Element;

public class PinTest extends TestCase {

    private Product _product;
    private Vector _eventTypes;
    private Vector _events;
    private final String _NODE_ADDED = "nodeAdded";
    private final String _NODE_CHANGED = "nodeChanged";
    private final String _NODE_REMOVED = "nodeRemoved";
    private final String _ls = SystemUtils.LS;


    public void setUp() {
        _product = new Product("product", "t", 10, 10);
        _eventTypes = new Vector();
        _events = new Vector();
        _product.addProductNodeListener(new ProductNodeListener() {
            public void nodeChanged(ProductNodeEvent event) {
                _eventTypes.add(_NODE_CHANGED);
                _events.add(event);
            }

            public void nodeDataChanged(ProductNodeEvent event) {
                _eventTypes.add("ndc");
                _events.add(event);
            }

            public void nodeAdded(ProductNodeEvent event) {
                _eventTypes.add(_NODE_ADDED);
                _events.add(event);
            }

            public void nodeRemoved(ProductNodeEvent event) {
                _eventTypes.add(_NODE_REMOVED);
                _events.add(event);
            }
        });
    }

    public void tearDown() {
    }

    public void testPinEvents() {
        final Pin pin1 = new Pin("pinName", "pinLabel", new GeoPos());

        assertEquals(0, _product.getNumPins());
        assertEquals(0, _events.size());
        assertEquals(0, _eventTypes.size());

        _product.addPin(pin1);
        assertEquals(1, _product.getNumPins());
        assertEquals(2, _events.size());
        assertEquals(2, _eventTypes.size());

        pin1.setDescription("descPin1");
        assertEquals(1, _product.getNumPins());
        assertEquals(3, _events.size());
        assertEquals(3, _eventTypes.size());

        pin1.setGeoPos(new GeoPos(4, 4));
        assertEquals(1, _product.getNumPins());
        assertEquals(4, _events.size());
        assertEquals(4, _eventTypes.size());

        pin1.setSymbol(new PinSymbol("symb1", new Line2D.Float(4, 5, 6, 7)));
        assertEquals(1, _product.getNumPins());
        assertEquals(5, _events.size());
        assertEquals(5, _eventTypes.size());

        _product.removePin(pin1);
        assertEquals(0, _product.getNumPins());
        assertEquals(6, _events.size());
        assertEquals(6, _eventTypes.size());


        final String[] expectedEventTypes = new String[]{
            _NODE_CHANGED,
            _NODE_ADDED,
            _NODE_CHANGED,
            _NODE_CHANGED,
            _NODE_CHANGED,
            _NODE_REMOVED
        };
        final String[] currentEventTypes = (String[]) _eventTypes.toArray(new String[_eventTypes.size()]);
        for (int i = 0; i < currentEventTypes.length; i++) {
            assertEquals("event number: " + i, expectedEventTypes[i], currentEventTypes[i]);
        }

        final String[] expectedPropertyNames = new String[]{
            ProductNode.PROPERTY_NAME_OWNER,
            null,
            ProductNode.PROPERTY_NAME_DESCRIPTION,
            Pin.PROPERTY_NAME_GEOPOS,
            Pin.PROPERTY_NAME_PINSYMBOL,
            null
        };
        final ProductNodeEvent[] currentProductNodeEvents = (ProductNodeEvent[]) _events.toArray(
                new ProductNodeEvent[_events.size()]);
        for (int i = 0; i < currentProductNodeEvents.length; i++) {
            final ProductNodeEvent currentProductNodeEvent = currentProductNodeEvents[i];
            assertEquals("event number: " + i, pin1, currentProductNodeEvent.getSourceNode());
            assertEquals("event number: " + i, expectedPropertyNames[i], currentProductNodeEvent.getPropertyName());
        }
    }

    public void testWriteXML_XmlWriterIsNull() {
        Pin pin = new Pin("pinName", "pinLabel", new GeoPos());

        try {
            pin.writeXML(null, 1);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            // expected IllegalArgumentException
        } catch (Exception e) {
            fail("IllegalArgumentException expected");
        }
    }

    public void testWriteXML_IndentIsSmallerThanZero() {
        Pin pin = new Pin("pinName", "pinLabel", new GeoPos());

        try {
            pin.writeXML(new XmlWriter(new StringWriter(), false), -1);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            // expected IllegalArgumentException
        } catch (Exception e) {
            fail("IllegalArgumentException expected");
        }
    }

    public void testWriteXML_DifferentValidIndent() {
        Pin pin = new Pin("pinName", "pinLabel", new GeoPos(4f, 87f));
        pin.setDescription("pinDescription");
        pin.setSymbol(PinSymbol.createDefaultPinSymbol());

        StringWriter stringWriter = new StringWriter();
        pin.writeXML(new XmlWriter(stringWriter, false), 0);
        String expected = "" +
                          "<Pin name=\"pinName\">" + _ls +
                          "    <LABEL>pinLabel</LABEL>" + _ls +
                          "    <DESCRIPTION>pinDescription</DESCRIPTION>" + _ls +
                          "    <LATITUDE>4.0</LATITUDE>" + _ls +
                          "    <LONGITUDE>87.0</LONGITUDE>" + _ls +
                          "    <FillColor>" + _ls +
                          "        <COLOR red=\"128\" green=\"128\" blue=\"255\" alpha=\"255\" />" + _ls +
                          "    </FillColor>" + _ls +
                          "    <OutlineColor>" + _ls +
                          "        <COLOR red=\"0\" green=\"0\" blue=\"64\" alpha=\"255\" />" + _ls +
                          "    </OutlineColor>" + _ls +
                          "</Pin>" + _ls;
        assertEquals(expected, stringWriter.toString());

        stringWriter = new StringWriter();
        pin.writeXML(new XmlWriter(stringWriter, false), 3);
        expected = "" +
                   "            <Pin name=\"pinName\">" + _ls +
                   "                <LABEL>pinLabel</LABEL>" + _ls +
                   "                <DESCRIPTION>pinDescription</DESCRIPTION>" + _ls +
                   "                <LATITUDE>4.0</LATITUDE>" + _ls +
                   "                <LONGITUDE>87.0</LONGITUDE>" + _ls +
                   "                <FillColor>" + _ls +
                   "                    <COLOR red=\"128\" green=\"128\" blue=\"255\" alpha=\"255\" />" + _ls +
                   "                </FillColor>" + _ls +
                   "                <OutlineColor>" + _ls +
                   "                    <COLOR red=\"0\" green=\"0\" blue=\"64\" alpha=\"255\" />" + _ls +
                   "                </OutlineColor>" + _ls +
                   "            </Pin>" + _ls;
        assertEquals(expected, stringWriter.toString());
    }

    public void testCreatePin_FromJDOMElement() {
        final String pinName = "pin14";
        final String pinDesc = "descr";
        final float pinLat = 5.7f;
        final float pinLon = 23.4f;

        try {
            Pin.createPin(null);
            fail("NullPointerException expexcted");
        } catch (NullPointerException e) {
            // OK
        }

        Element pinElem = new Element(DimapProductConstants.TAG_PIN);

        try {
            Pin.createPin(pinElem);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            // OK
        }

        pinElem.setAttribute(DimapProductConstants.ATTRIB_NAME, pinName);

        try {
            Pin.createPin(pinElem);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            // OK
        }

        final Element latElem = new Element(DimapProductConstants.TAG_PIN_LATITUDE);
        latElem.setText(String.valueOf(pinLat));
        pinElem.addContent(latElem);

        try {
            Pin.createPin(pinElem);
            fail("IllegalArgumentException expected");
        } catch (Exception e) {
            // OK
        }

        final Element lonElem = new Element(DimapProductConstants.TAG_PIN_LONGITUDE);
        lonElem.setText(String.valueOf(pinLon));
        pinElem.addContent(lonElem);

        Pin pin = Pin.createPin(pinElem);
        assertNotNull("pin must be not null", pin);
        assertEquals(pinName, pin.getName());
        assertNull(pin.getDescription());
        assertEquals(pinLat, pin.getGeoPos().lat, 1e-15f);
        assertEquals(pinLon, pin.getGeoPos().lon, 1e-15f);

        final Element descElem = new Element(DimapProductConstants.TAG_PIN_DESCRIPTION);
        descElem.setText(pinDesc);
        pinElem.addContent(descElem);

        pin = Pin.createPin(pinElem);
        assertNotNull("pin must be not null", pin);
        assertEquals(pinName, pin.getName());
        assertEquals(pinDesc, pin.getDescription());
        assertEquals(pinLat, pin.getGeoPos().lat, 1e-15f);
        assertEquals(pinLon, pin.getGeoPos().lon, 1e-15f);

        final Element fillElem = new Element(DimapProductConstants.TAG_PIN_FILL_COLOR);
        Element colorElem = new Element(DimapProductConstants.TAG_COLOR);
        colorElem.setAttribute(DimapProductConstants.ATTRIB_RED, "255");
        colorElem.setAttribute(DimapProductConstants.ATTRIB_GREEN, "0");
        colorElem.setAttribute(DimapProductConstants.ATTRIB_BLUE, "0");
        colorElem.setAttribute(DimapProductConstants.ATTRIB_ALPHA, "255");
        fillElem.addContent(colorElem);
        pinElem.addContent(fillElem);

        final Element outlineElem = new Element(DimapProductConstants.TAG_PIN_OUTLINE_COLOR);
        colorElem = new Element(DimapProductConstants.TAG_COLOR);
        colorElem.setAttribute(DimapProductConstants.ATTRIB_RED, "0");
        colorElem.setAttribute(DimapProductConstants.ATTRIB_GREEN, "0");
        colorElem.setAttribute(DimapProductConstants.ATTRIB_BLUE, "255");
        colorElem.setAttribute(DimapProductConstants.ATTRIB_ALPHA, "255");
        outlineElem.addContent(colorElem);
        pinElem.addContent(outlineElem);

        pin = Pin.createPin(pinElem);
        assertNotNull("pin must be not null", pin);
        assertEquals(pinName, pin.getName());
        assertEquals(pinDesc, pin.getDescription());
        assertEquals(pinLat, pin.getGeoPos().lat, 1e-15f);
        assertEquals(pinLon, pin.getGeoPos().lon, 1e-15f);
        PinSymbol symbol = pin.getSymbol();
        assertNotNull(symbol);
        assertEquals(Color.red, symbol.getFillPaint());
        assertEquals(Color.blue, symbol.getOutlineColor());
    }

    public void testLabelSettings() {
        Pin p = new Pin("rallamann", "rallamann", new GeoPos());
        assertEquals("rallamann", p.getName());
        assertEquals("rallamann", p.getLabel());

        p.setLabel("schanteri");
        assertEquals("rallamann", p.getName());
        assertEquals("schanteri", p.getLabel());

        p.setLabel(null);
        assertEquals("", p.getLabel());

        p.setLabel("");
        assertEquals("", p.getLabel());

    }
}
