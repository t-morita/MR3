package jp.ac.shizuoka.cs.panda.mmm.mr3.ui;
import java.awt.*;

import javax.swing.*;

import jp.ac.shizuoka.cs.panda.mmm.mr3.data.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.jgraph.*;
import jp.ac.shizuoka.cs.panda.mmm.mr3.util.*;

import org.jgraph.event.*;
import org.jgraph.graph.*;

import com.hp.hpl.jena.rdf.model.*;

/**
 *
 * @author takeshi morita 
 */
public class SelectResourceTypeDialog extends SelectClassDialog {

	private JLabel dspURI;
	private Resource uri;
	private URIType uriType;
	private GraphCell cell;
	private GraphCell prevCell;

	public SelectResourceTypeDialog(GraphManager manager) {
		super("Select Resource Type", manager);
	}

	protected void initEachDialogAttr() {
		dspURI = new JLabel();
		initComponent(dspURI, "URI", listWidth, listHeight);
	}

	protected void setEachDialogAttrLayout() {
		gridbag.setConstraints(dspURI, c);
		inlinePanel.add(dspURI);
	}

	private void changeTypeCellColor(Object typeCell) {
		Object[] cells = graph.getAllCells();
		for (int i = 0; i < cells.length; i++) {
			GraphCell cell = (GraphCell) cells[i];
			if (graph.isRDFSClassCell(cell)) {
				if (cell == typeCell) {
					ChangeCellAttributes.changeCellColor(graph, cell, Color.yellow);
					prevCell = cell;
					graph.setSelectionCell(cell);
					break;
				}
			}
		}
	}

	public void setInitCell(Object typeCell) {
		changeAllCellColor(Color.green);
		if (typeCell == null) {
			prevCell = null;
			dspURI.setText("");
			return;
		} else {
			changeTypeCellColor(typeCell);
		}
	}

	public void valueChanged(GraphSelectionEvent e) {
		cell = (GraphCell) graph.getSelectionCell();
		if (graph.getSelectionCount() == 1 && graph.getModel().getChildCount(cell) <= 1) {
			if (graph.isRDFSClassCell(cell)) {
				ChangeCellAttributes.changeCellColor(graph, prevCell, Color.green);
				ChangeCellAttributes.changeCellColor(graph, cell, Color.yellow);
				RDFSInfo info = rdfsMap.getCellInfo(cell);
				dspURI.setText(info.getURIStr());
				dspURI.setToolTipText(info.getURIStr());
				uri = info.getURI();
				prevCell = cell;
			}
		}
	}

	public URIType getURIType() {
		return uriType;
	}

	public Object getValue() {
		if (prevCell != null) {
			if (isOk) {
				isOk = false;
				ChangeCellAttributes.changeCellColor(graph, prevCell, Color.green);
				return uri;
			} else {
				ChangeCellAttributes.changeCellColor(graph, prevCell, Color.green);
				return null;
			}
		} else {
			return null;
		}
	}
}
