package org.semanticweb.mmm.mr3.io;
import java.util.*;

import org.jgraph.graph.*;
import org.semanticweb.mmm.mr3.data.*;
import org.semanticweb.mmm.mr3.jgraph.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

public class MR3Generator {

	private GraphManager gmanager;
	private RDFResourceInfoMap resInfoMap = RDFResourceInfoMap.getInstance();
	private RDFLiteralInfoMap litInfoMap = RDFLiteralInfoMap.getInstance();
	private RDFSInfoMap rdfsInfoMap = RDFSInfoMap.getInstance();

	public MR3Generator(GraphManager manager) {
		gmanager = manager;
	}

	public Model getSelectedPropertyModel() {
		RDFGraph graph = gmanager.getPropertyGraph();
		Object[] cells = graph.getAllSelectedCells();
		Model propertyModel = ModelFactory.createDefaultModel();
		createPropertyModel(graph, cells, propertyModel);

		return propertyModel;
	}

	public Model getPropertyModel() {
		RDFGraph graph = gmanager.getPropertyGraph();
		Object[] cells = graph.getAllCells();
		Model propertyModel = ModelFactory.createDefaultModel();
		createPropertyModel(graph, cells, propertyModel);

		return propertyModel;
	}

	private void createPropertyModel(RDFGraph graph, Object[] cells, Model propertyModel) {
		try {
			for (int i = 0; i < cells.length; i++) {
				Object cell = cells[i];
				if (graph.isRDFSPropertyCell(cell)) {
					PropertyInfo info = (PropertyInfo) rdfsInfoMap.getCellInfo(cell);
					Set supProperties = graph.getTargetCells((DefaultGraphCell) cell);
					info.setSupRDFS(supProperties);
					if (!info.getURI().equals(MR3Resource.Property)) {
						propertyModel.add(info.getModel());
					}
				}
			}
		} catch (RDFException e) {
			e.printStackTrace();
		}
	}

	public Model getSelectedClassModel() {
		RDFGraph graph = gmanager.getClassGraph();
		Object[] cells = graph.getAllSelectedCells();
		Model classModel = ModelFactory.createDefaultModel();
		createClassModel(graph, cells, classModel);

		return classModel;
	}

	public Model getClassModel() {
		RDFGraph graph = gmanager.getClassGraph();
		Object[] cells = graph.getAllCells();
		Model classModel = ModelFactory.createDefaultModel();
		createClassModel(graph, cells, classModel);

		return classModel;
	}

	private void createClassModel(RDFGraph graph, Object[] cells, Model classModel) {
		try {
			for (int i = 0; i < cells.length; i++) {
				Object cell = cells[i];
				if (graph.isRDFSClassCell(cell)) {
					ClassInfo info = (ClassInfo) rdfsInfoMap.getCellInfo(cell);
					Set supClasses = graph.getTargetCells((DefaultGraphCell) cell);
					info.setSupRDFS(supClasses);
					classModel.add(info.getModel());
				}
			}
		} catch (RDFException e) {
			e.printStackTrace();
		}
	}

	private void setResourceType(Model rdfModel, Object cell) {
		try {
			RDFResourceInfo resInfo = resInfoMap.getCellInfo(cell);
			if (resInfo.getType().getURI().length() != 0) {
				rdfModel.add(rdfModel.createStatement(resInfo.getURI(), RDF.type, resInfo.getType()));
			}
		} catch (RDFException rex) {
			rex.printStackTrace();
		}
	}

	/** Edgeのリストを得るついでに，TypeのStatementsも作っている．分けた方がわかりやすいが．*/
	private Object[] getEdges(Model rdfModel, RDFGraph graph, Object[] cells) {
		if (cells != null) {
			ArrayList result = new ArrayList();
			for (int i = 0; i < cells.length; i++) {
				Object cell = cells[i];
				if (graph.isEdge(cell)) {
					result.add(cell);
				} else if (!graph.isTypeCell(cell) && graph.isRDFResourceCell(cell)) {
					setResourceType(rdfModel, cell);
				}
			}
			return result.toArray();
		}
		return null;
	}

	public Model getSelectedRDFModel() {
		RDFGraph graph = gmanager.getRDFGraph();
		Model rdfModel = ModelFactory.createDefaultModel();
		Object[] edges = getEdges(rdfModel, graph, graph.getAllSelectedCells());
		createRDFModel(graph, rdfModel, edges);

		return rdfModel;
	}

	public Model getRDFModel() {
		RDFGraph graph = gmanager.getRDFGraph();
		Model rdfModel = ModelFactory.createDefaultModel();
		Object[] edges = getEdges(rdfModel, graph, graph.getAllCells());
		createRDFModel(graph, rdfModel, edges);

		return rdfModel;
	}

	private Property getRDFProperty(Edge edge) throws RDFException {
		Object propCell = rdfsInfoMap.getEdgeInfo(edge);
		RDFSInfo propInfo = rdfsInfoMap.getCellInfo(propCell);
		Property property = null;
		if (propInfo == null) {
			property = MR3Resource.Nil;
		} else {
			property = ResourceFactory.createProperty(propInfo.getURI().getURI());
		}
		return property;
	}

	private void createRDFModel(RDFGraph graph, Model rdfModel, Object[] edges) {
		for (int i = 0; i < edges.length; i++) {
			Edge edge = (Edge) edges[i];
			RDFResourceInfo info = resInfoMap.getCellInfo(graph.getSourceVertex(edge));
			Resource subject = info.getURI();
			try {
				Property property = getRDFProperty(edge);
				GraphCell targetCell = (GraphCell) graph.getTargetVertex(edge);

				if (graph.isRDFResourceCell(targetCell)) {
					info = resInfoMap.getCellInfo(targetCell);
					rdfModel.add(rdfModel.createStatement(subject, property, info.getURI()));
				} else if (graph.isRDFLiteralCell(targetCell)) {
					Literal literal = litInfoMap.getCellInfo(targetCell);
					rdfModel.add(rdfModel.createStatement(subject, property, literal));
				}
			} catch (RDFException e) {
				e.printStackTrace();
			}
		}
	}

}
