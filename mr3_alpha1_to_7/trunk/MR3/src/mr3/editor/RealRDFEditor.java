package mr3.editor;
import java.io.*;

import mr3.jgraph.*;
import mr3.ui.*;

import com.jgraph.event.*;

/**
 * @author takeshi morita
 *
 */
public class RealRDFEditor extends Editor {

	public RealRDFEditor(GraphManager gm, NameSpaceTableDialog nsD, FindResourceDialog findResD) {
		initEditor(gm.getRealRDFGraph(), gm, nsD, findResD);
	}

	public void importFile(File file) {
		try {
			Reader reader = new FileReader(file);

			RDFGraph newGraph = rdfToGraph.convertRealRDFToJGraph(reader);
			replaceGraph(newGraph);
			gmanager.applyTreeLayout(graph, 'r');
			fitWindow();
			// gmanager.changeRealRDFCellView(); となる予定
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public void valueChanged(GraphSelectionEvent e) {
		setToolStatus();
		//changeAttrPanel();
		//propWindow.validate(); // validateメソッドを呼ばないと再描画がうまくいかない
	}
}
