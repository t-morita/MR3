/*
 * Created on 2003/07/24
 *
 */
package jp.ac.shizuoka.cs.panda.mmm.mr3.actions;

import java.awt.event.*;

import javax.swing.*;

import jp.ac.shizuoka.cs.panda.mmm.mr3.jgraph.*;

/**
 * @author takeshi morita
 */

public class PasteAction extends AbstractAction {
	RDFGraph graph;

	public PasteAction(RDFGraph g, String title) {
		super(title);
		graph = g;
	}

	public void actionPerformed(ActionEvent e) {
		graph.paste();
	}
}
