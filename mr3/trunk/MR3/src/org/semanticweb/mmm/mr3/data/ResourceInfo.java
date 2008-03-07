/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mr3.sourceforge.net/
 * 
 * Copyright (C) 2003-2008 Yamaguchi Laboratory, Keio University. All rights reserved. 
 * 
 * This file is part of MR^3.
 * 
 * MR^3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MR^3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MR^3.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package org.semanticweb.mmm.mr3.data;

import java.util.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

/**
 * @author takeshi morita
 */
public abstract class ResourceInfo {

    protected transient List<MR3Literal> labelList;
    protected transient List<MR3Literal> commentList;

    public void setLabelList(List<MR3Literal> labelList) {
        this.labelList = labelList;
    }

    public void addLabel(MR3Literal literal) {
        labelList.add(literal);
    }

    public MR3Literal getFirstLabel() {
        if (0 < labelList.size()) { return labelList.get(0); }
        return null;
    }

    public List<MR3Literal> getLabelList() {
        return Collections.unmodifiableList(labelList);
    }

    public MR3Literal getDefaultLabel(String defaultLang) {
        for (Iterator i = labelList.iterator(); i.hasNext();) {
            MR3Literal literal = (MR3Literal) i.next();
            if (literal.getLanguage().equals(defaultLang)) { return literal; }
        }
        return null;
    }

    public void addComment(MR3Literal literal) {
        commentList.add(literal);
    }

    public List<MR3Literal> getCommentList() {
        return Collections.unmodifiableList(commentList);
    }

    public MR3Literal getFirstComment() {
        if (0 < commentList.size()) { return commentList.get(0); }
        return null;
    }

    public MR3Literal getDefaultComment(String defaultLang) {
        for (Iterator i = commentList.iterator(); i.hasNext();) {
            MR3Literal literal = (MR3Literal) i.next();
            if (literal.getLanguage().equals(defaultLang)) { return literal; }
        }
        return null;
    }

    public void setCommentList(List<MR3Literal> commentList) {
        this.commentList = commentList;
    }

    public Model getModel(Resource res) throws RDFException {
        Model tmpModel = ModelFactory.createDefaultModel();
        for (Iterator i = labelList.iterator(); i.hasNext();) {
            MR3Literal literal = (MR3Literal) i.next();
            tmpModel.add(tmpModel.createStatement(res, RDFS.label, literal.getLiteral()));
        }
        for (Iterator i = commentList.iterator(); i.hasNext();) {
            MR3Literal literal = (MR3Literal) i.next();
            tmpModel.add(tmpModel.createStatement(res, RDFS.comment, literal.getLiteral()));
        }
        return tmpModel;
    }

}
