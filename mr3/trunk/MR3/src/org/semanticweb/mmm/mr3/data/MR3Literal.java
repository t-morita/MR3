/*
 * @(#) MR3Literal.java
 *
 *
 * Copyright (C) 2003-2005 The MMM Project
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package org.semanticweb.mmm.mr3.data;

import java.awt.*;
import java.io.*;

import org.semanticweb.mmm.mr3.util.*;

import com.hp.hpl.jena.datatypes.*;
import com.hp.hpl.jena.rdf.model.*;

/**
 * @author takeshi morita
 * 
 */
public class MR3Literal implements Serializable {

    private String str;
    private String lang;
    private RDFDatatype dataType;
    private Rectangle litRect;
    private Resource resource;
    private Property property;
    private static final long serialVersionUID = 75073546338792276L;

    public MR3Literal() {
        str = "";
        lang = "";
        dataType = null;
        litRect = new Rectangle();
        resource = null;
        property = null;
    }

    public MR3Literal(String s, String l, RDFDatatype dt) {
        str = s;
        lang = l;
        dataType = dt;
    }

    public MR3Literal(MR3Literal lit) {
        str = lit.getString();
        lang = lit.getLanguage();
        dataType = lit.getDatatype();
    }

    public MR3Literal(Literal lit) {
        try {
            str = lit.getString();
            lang = lit.getLanguage();
            dataType = lit.getDatatype();
        } catch (RDFException e) {
            e.printStackTrace();
        }
    }

    public boolean equals(MR3Literal lit) {
        if (dataType == null && lit.getDatatype() != null) { return false; }
        if (dataType != null && lit.getDatatype() == null) { return false; }
        if (dataType == null && lit.getDatatype() == null) { return lit.getLanguage().equals(lang)
                && lit.getString().equals(str); }
        return lit.getLanguage().equals(lang) && lit.getDatatype().equals(dataType) && lit.getString().equals(str);
    }

    public Literal getLiteral() {
        return Utilities.createLiteral(str, lang, dataType);
    }

    public void setString(String s) {
        str = s;
    }

    public String getString() {
        return str;
    }
    public void setLanguage(String s) {
        lang = s;
    }

    public String getLanguage() {
        return lang;
    }

    public void setDatatype(RDFDatatype dt) {
        dataType = dt;
    }

    public RDFDatatype getDatatype() {
        return dataType;
    }

    public void setRectangle(Rectangle rect) {
        litRect = rect;
    }

    public Rectangle getRectangle() {
        return litRect;
    }

    public Point getLocation() {
        return litRect.getLocation();
    }

    public void setX(int x) {
        litRect.x = x;
    }

    public void setY(int y) {
        litRect.y = y;
    }
    public void setWidth(int width) {
        litRect.width = width;
    }

    public void setHeight(int height) {
        litRect.height = height;
    }

    public void setResource(String res) {
        resource = ResourceFactory.createResource(res);
    }

    public Resource getResource() {
        return resource;
    }

    public void setProperty(String prop) {
        try {
            property = ResourceFactory.createProperty(prop);
        } catch (RDFException e) {
            e.printStackTrace();
        }
    }

    public Property getProperty() {
        return property;
    }

    public String toString() {
        return str;
    }
}
