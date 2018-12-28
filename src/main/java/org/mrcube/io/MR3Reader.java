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

package org.mrcube.io;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.*;
import org.jgraph.graph.GraphCell;
import org.jgraph.graph.GraphConstants;
import org.mrcube.MR3;
import org.mrcube.jgraph.GraphManager;
import org.mrcube.jgraph.RDFGraph;
import org.mrcube.layout.GraphLayoutUtilities;
import org.mrcube.layout.JGraphTreeLayout;
import org.mrcube.layout.VGJTreeLayout;
import org.mrcube.models.MR3Constants.HistoryType;
import org.mrcube.models.MR3Resource;
import org.mrcube.models.RDFSModel;
import org.mrcube.models.RDFSModelMap;
import org.mrcube.utils.ProjectManager;
import org.mrcube.views.HistoryManager;
import org.mrcube.views.NameSpaceTableDialog;
import org.mrcube.views.ReplaceRDFSDialog;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * @author Takeshi Morita
 */
public class MR3Reader {

	private MR3Parser mr3Parser;
	private GraphManager gmanager;
	private MR3Generator mr3Generator;
	private RDFSModelExtraction extractRDFS;
	private NameSpaceTableDialog nsTableDialog;
	private JGraphTreeLayout jgraphTreeLayout;

	private WeakReference<ReplaceRDFSDialog> replaceRDFSDialogRef;

	public MR3Reader(GraphManager gm) {
		gmanager = gm;
		nsTableDialog = gmanager.getNSTableDialog();
		mr3Parser = new MR3Parser(gmanager);
		mr3Generator = new MR3Generator(gmanager);
		jgraphTreeLayout = new JGraphTreeLayout(gmanager);
		extractRDFS = new RDFSModelExtraction(gmanager);
		replaceRDFSDialogRef = new WeakReference<>(null);
	}

	public void replaceRDFModel(Model newModel) {
		model = newModel;
		if (model == null) {
			return;
		}
		new Thread(() -> {
			gmanager.importing(true);
			replaceRDF(model);
			performTreeLayout();
			gmanager.importing(false);
		}).start();
	}

	public void mergeRDFModelThread(Model newModel) {
		model = newModel;
		new Thread(() -> {
			gmanager.importing(true);
			mergeRDFModel(model);
			gmanager.importing(false);
		}).start();
	}

	public void mergeRDFModel(Model newModel) {
		Model rdfModel = mr3Generator.getRDFModel(false);
		rdfModel.add(newModel);
		nsTableDialog.setCurrentNSPrefix(rdfModel);
		gmanager.getCurrentRDFGraph().removeAllCells();
		if (ProjectManager.getLayoutMap() != null) {
			mr3Parser.replaceProjectRDFGraph(rdfModel);
		} else {
			mr3Parser.replaceDefaultRDFGraph(rdfModel);
		}

		// RDFからRDFSへ反映されたクラス，プロパティの処理
		mergeRDFSModel(ModelFactory.createDefaultModel());
		resetPropertyInfo();
	}

	private void resetPropertyInfo() {
		RDFGraph rdfGraph = gmanager.getCurrentRDFGraph();
		Object[] cells = rdfGraph.getAllCells();
		for (Object cell1 : cells) {
			GraphCell cell = (GraphCell) cell1;
			if (RDFGraph.isRDFPropertyCell(cell)) {
				RDFSModel info = (RDFSModel) GraphConstants.getValue(cell.getAttributes());
				if (!info.getURI().equals(MR3Resource.Nil)) {
					GraphCell propCell = gmanager.getPropertyCell(info.getURI(), false);
					Object newInfo = GraphConstants.getValue(propCell.getAttributes());
					GraphConstants.setValue(cell.getAttributes(), newInfo);
				}
			}
		}
	}

	public void mergeRDFSModel(Model model) {
		MR3.STATUS_BAR.initIndeterminate();
		// addInnerModel(model);
		// model.add(mr3Generator.getPropertyModel(false));
		// model.add(mr3Generator.getClassModel(false));
		// Model classModel = extractRDFS.extractClassModel(model);
		// Model propertyModel = extractRDFS.extractPropertyModel(model);
		mergePropertyModel(model);
		mergeClassModel(model);
		// nsTableDialog.setCurrentNSPrefix(model);
		setPropertyLabels();
		MR3.STATUS_BAR.hideProgressBar();
	}

	private OntModel ontModel;

	public void mergeOntologyModel(OntModel model) {
		ontModel = model;
		if (ontModel == null) {
			return;
		}
		new Thread(() -> {
			gmanager.importing(true);
			// OntModelからDefaultModelへの変換処理
			Model rdfsModel = OntModelToRDFSModel.convertOntModelToRDFSModel(ontModel);
			mergeRDFs(rdfsModel);
			performTreeLayout();
			gmanager.importing(false);
		}).start();
	}

	// private void addRDFTypeModel(Model rdfModel) {
	// Model innerModel = ModelFactory.createDefaultModel();
	// for (NodeIterator i = rdfModel.listObjectsOfProperty(RDF.type);
	// i.hasNext();) {
	// innerModel.add((Resource) i.next(), RDF.type, RDFS.Class);
	// }
	// rdfModel.add(innerModel);
	// }

	// private void addRDFPropertyModel(Model rdfModel) {
	// Model innerModel = ModelFactory.createDefaultModel();
	// for (StmtIterator i = rdfModel.listStatements(); i.hasNext();) {
	// Statement stmt = i.nextStatement();
	// innerModel.add(stmt.getPredicate(), RDF.type, RDF.Property);
	// }
	// rdfModel.add(innerModel);
	// }

	private void setPropertyLabel(RDFGraph graph, Object edge) {
		GraphCell sourceCell = (GraphCell) graph.getSourceVertex(edge);
		RDFSModel sourceInfo = (RDFSModel) GraphConstants.getValue(sourceCell.getAttributes());
		Resource sourceRes = sourceInfo.getURI();
		GraphCell targetCell = (GraphCell) graph.getTargetVertex(edge);
		RDFSModel targetInfo = (RDFSModel) GraphConstants.getValue(targetCell.getAttributes());
		Resource targetRes = targetInfo.getURI();
		RDFSModelMap rdfsModelMap = gmanager.getCurrentRDFSInfoMap();
		Model model = rdfsModelMap.getPropertyLabelModel();
		for (StmtIterator i = model.listStatements(); i.hasNext();) {
			Statement stmt = i.nextStatement();
			Resource subject = stmt.getSubject();
			Property predicate = stmt.getPredicate();
			RDFNode object = stmt.getObject();
			if (sourceRes.equals(subject) && targetRes.equals(object)) {
				String uri = predicate.toString();
				gmanager.setCellValue((GraphCell) edge, uri.substring(uri.indexOf('_') + 1));
			}
		}
	}

	private void setPropertyLabels() {
		RDFGraph classGraph = gmanager.getCurrentClassGraph();
		for (Object cell : classGraph.getAllCells()) {
			if (RDFGraph.isEdge(cell)) {
				setPropertyLabel(classGraph, cell);
			}
		}
	}

	private void mergeClassModel(Model model) {
		model.add(mr3Generator.getClassModel(false));
		nsTableDialog.setCurrentNSPrefix(model);
		// gmanager.getRDFSTreeEditor().setClassTreeRoot(model);
		extractRDFS.extractClassModel(model);
		if (ProjectManager.getLayoutMap() != null) {
			mr3Parser.createClassGraph(ProjectManager.getLayoutMap());
		} else {
			mr3Parser.createClassGraph(VGJTreeLayout.getVGJClassCellLayoutMap());
		}
		RDFSModelMap rdfsModelMap = gmanager.getCurrentRDFSInfoMap();
		rdfsModelMap.clearTemporaryObject();
		gmanager.getCurrentClassGraph().clearSelection();
	}

	private void mergePropertyModel(Model model) {
		model.add(mr3Generator.getPropertyModel(false));
		nsTableDialog.setCurrentNSPrefix(model);
		// gmanager.getRDFSTreeEditor().setPropertyTreeRoot(model);
		extractRDFS.extractPropertyModel(model);
		if (ProjectManager.getLayoutMap() != null) {
			mr3Parser.createPropertyGraph(ProjectManager.getLayoutMap());
		} else {
			mr3Parser.createPropertyGraph(VGJTreeLayout.getVGJPropertyCellLayoutMap());
		}
		RDFSModelMap rdfsModelMap = gmanager.getCurrentRDFSInfoMap();
		rdfsModelMap.clearTemporaryObject();
		gmanager.getCurrentPropertyGraph().clearSelection();
	}

	public void replaceRDF(Model model) {
		if (model != null) {
			nsTableDialog.setCurrentNSPrefix(model);
			gmanager.getCurrentRDFGraph().removeAllCells();
			// replaceGraph(mr3Parser.createRDFGraph(model,
			// VGJTreeLayout.getVGJRDFCellLayoutMap(model)));
			mr3Parser.replaceDefaultRDFGraph(model);
			// RDFからRDFSへ反映されたクラス，プロパティの処理
			mergeRDFSModel(ModelFactory.createDefaultModel());
		}
	}

	public void replaceRDFS(Model model) {
		if (model != null) {
			mergeRDFSModel(model);
		}
	}

	public void replaceRDFSModel(Model mdl) {
		model = mdl;
		if (model == null) {
			return;
		}
		new Thread(() -> {
			if (gmanager.getCurrentRDFGraph().getAllCells().length == 0) {
				gmanager.getCurrentClassGraph().removeAllCells();
				gmanager.getCurrentPropertyGraph().removeAllCells();
				gmanager.importing(true);
				replaceRDFS(model);
				performRDFSTreeLayout();
			} else {
				ReplaceRDFSDialog replaceRDFSDialog = getReplaceRDFSDialog(model);
				if (replaceRDFSDialog.isApply()) {
					gmanager.importing(true);
					replaceRDFS(model);
					performRDFSTreeLayout();
				}
			}
			gmanager.importing(false);
		}).start();
	}

	public ReplaceRDFSDialog getReplaceRDFSDialog(Model model) {
		ReplaceRDFSDialog result = replaceRDFSDialogRef.get();
		if (result == null) {
			result = new ReplaceRDFSDialog(gmanager);
			replaceRDFSDialogRef = new WeakReference<>(result);
		}
		result.initListData(model.union(ModelFactory.createDefaultModel()));
		result.setVisible(true);
		return result;
	}

	public void mergeRDFs(Model model) {
		if (model != null) {
			mergeRDFSModel(model);
			mergeRDFModel(model);
		}
	}

	public void mergeRDFPlusRDFSModel(Model mdl) {
		model = mdl;
		if (model == null) {
			return;
		}
		new Thread(() -> {
			gmanager.importing(true);
			mergeRDFs(model);
			performTreeLayout();
			gmanager.importing(false);
		}).start();
	}

	public void performTreeLayout() {
		if (GraphLayoutUtilities.LAYOUT_TYPE.equals(GraphLayoutUtilities.JGRAPH_TREE_LAYOUT)) {
			jgraphTreeLayout.performJGraphTreeLayout();
		}
	}

	public void performRDFSTreeLayout() {
		if (GraphLayoutUtilities.LAYOUT_TYPE.equals(GraphLayoutUtilities.JGRAPH_TREE_LAYOUT)) {
			jgraphTreeLayout.performJGraphRDFSTreeLayout();
		}
	}

	// private void addInnerModel(Model model) {
	// Model innerModel = ModelFactory.createDefaultModel();
	// Property[] classProperties = { RDFS.subClassOf, RDFS.domain, RDFS.range
	// };
	//
	// for (int i = 0; i < classProperties.length; i++) {
	// for (NodeIterator iter =
	// model.listObjectsOfProperty(classProperties[i]); iter.hasNext();) {
	// innerModel.add((Resource) iter.next(), RDF.type, RDFS.Class);
	// }
	// }
	// for (NodeIterator iter =
	// model.listObjectsOfProperty(RDFS.subPropertyOf); iter.hasNext();) {
	// innerModel.add((Resource) iter.next(), RDF.type, RDF.Property);
	// }
	// model.add(innerModel);
	// }

	private Model model;

	public void replaceProjectModel(Model projectModel) {
		model = projectModel;
		if (model == null) {
			return;
		}
		new Thread(() -> {
			File currentProjectFile = MR3.getCurrentProject().getCurrentProjectFile(); // NewProjectよりも前のを保存
			gmanager.importing(true);
			ProjectManager projectManager = new ProjectManager(gmanager);
			Model projectModel1 = projectManager.extractProjectModel(model);
			mergeRDFs(model);
			projectManager.loadProject(projectModel1);
			projectManager.removeEmptyClass();
			nsTableDialog.setPrefixNSInfoSet();
			gmanager.clearSelection();
			gmanager.refreshGraphs();
			gmanager.importing(false);
			if (currentProjectFile != null) {
				MR3.getCurrentProject().setCurrentProjectFile(currentProjectFile);
				HistoryManager.saveHistory(HistoryType.OPEN_PROJECT,
						currentProjectFile.getAbsolutePath());
			}
		}).start();
	}
}