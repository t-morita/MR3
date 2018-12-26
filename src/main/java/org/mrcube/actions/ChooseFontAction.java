/*
 * Project Name: MR^3 (Meta-Model Management based on RDFs Revision Reflection)
 * Project Website: http://mrcube.org/
 * 
 * Copyright (C) 2003-2018 Yamaguchi Laboratory, Keio University. All rights reserved.
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

package org.mrcube.actions;

import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.GraphCell;
import org.jgraph.graph.GraphConstants;
import org.mrcube.MR3;
import org.mrcube.jgraph.RDFGraph;
import org.mrcube.utils.GraphUtilities;
import say.swing.JFontChooser;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.lang.ref.WeakReference;

/**
 * @author Takeshi Morita
 */
public class ChooseFontAction extends MR3AbstractAction {

	private WeakReference<JFontChooser> jfontChooserRef;

	public ChooseFontAction(MR3 mr3, String name) {
		super(mr3, name);
		jfontChooserRef = new WeakReference<>(null);
	}

	private JFontChooser getJFontChooser() {
		JFontChooser result = jfontChooserRef.get();
		if (result == null) {
			result = new JFontChooser();
			jfontChooserRef = new WeakReference<>(result);
		}
		return result;
	}

	public void actionPerformed(ActionEvent arg0) {
		JFontChooser jfontChooser = getJFontChooser();
		jfontChooser.setSelectedFont(mr3.getFont());
		int result = jfontChooser.showDialog(mr3.getGraphManager().getRootFrame());
		if (result == JFontChooser.OK_OPTION) {
			// System.out.println(jfontChooser.getSelectedFont());
			Font font = jfontChooser.getSelectedFont();
			GraphUtilities.defaultFont = font;
			mr3.setFont(font);
			setGraphFont(mr3.getRDFGraph(), font);
			setGraphFont(mr3.getClassGraph(), font);
			setGraphFont(mr3.getPropertyGraph(), font);
			if (mr3.getExportDialog() != null) {
				mr3.getExportDialog().setFont(font);
			}
		}
	}

	private void setGraphFont(RDFGraph graph, Font font) {
		Object[] cells = graph.getAllCells();
		for (Object cell1 : cells) {
			if (cell1 instanceof GraphCell) {
				GraphCell cell = (GraphCell) cell1;
				AttributeMap map = cell.getAttributes();
				GraphConstants.setFont(map, font);
				GraphUtilities.editCell(cell, map, graph);
			}
		}
	}
}
