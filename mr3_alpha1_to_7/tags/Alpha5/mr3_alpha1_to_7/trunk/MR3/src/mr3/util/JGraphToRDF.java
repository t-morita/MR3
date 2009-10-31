package mr3.util;
import java.util.*;

import mr3.data.*;
import mr3.jgraph.*;

import com.hp.hpl.mesa.rdf.jena.common.*;
import com.hp.hpl.mesa.rdf.jena.mem.*;
import com.hp.hpl.mesa.rdf.jena.model.*;
import com.hp.hpl.mesa.rdf.jena.vocabulary.*;
import com.jgraph.graph.*;

public class JGraphToRDF {

	private GraphManager gmanager;
	private RDFResourceInfoMap resInfoMap = RDFResourceInfoMap.getInstance();
	private RDFLiteralInfoMap litInfoMap = RDFLiteralInfoMap.getInstance();
	private RDFSInfoMap rdfsInfoMap = RDFSInfoMap.getInstance();

	public JGraphToRDF(GraphManager manager) {
		gmanager = manager;
	}

	public Model getSelectedPropertyModel() {
		RDFGraph graph = gmanager.getPropertyGraph();
		Object[] cells = graph.getAllSelectedCells();
		Model propertyModel = new ModelMem();
		createPropertyModel(graph, cells, propertyModel);

		return propertyModel;
	}

	public Model getPropertyModel() {
		RDFGraph graph = gmanager.getPropertyGraph();
		Object[] cells = graph.getAllCells();
		Model propertyModel = new ModelMem();
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
						propertyModel.add(info.getModel(gmanager.getBaseURI()));
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
		Model classModel = new ModelMem();
		createClassModel(graph, cells, classModel);

		return classModel;
	}

	public Model getClassModel() {
		RDFGraph graph = gmanager.getClassGraph();
		Object[] cells = graph.getAllCells();
		Model classModel = new ModelMem();
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
					classModel.add(info.getModel(gmanager.getBaseURI()));
				}
			}
		} catch (RDFException e) {
			e.printStackTrace();
		}
	}

	private void setResourceType(Model rdfModel, Object cell) {
		try {
			RDFResourceInfo info = resInfoMap.getCellInfo(cell);
			if (info.getType().getURI().length() != 0) {
				rdfModel.add(rdfModel.createStatement(info.getURI(), RDF.type, info.getType()));
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

	private Resource getResource(RDFResourceInfo info) {
		if (info.getURIType() == URIType.ANONYMOUS) {
			//			System.out.println(info.getURI().isAnon());
			return info.getURI();
		} else if (info.getURIType() == URIType.ID) {
			return new ResourceImpl(gmanager.getBaseURI() + info.getURI());
		} else {
			return info.getURI();
		}
	}

	public Model getSelectedRDFModel() {
		RDFGraph graph = gmanager.getRDFGraph();
		Model rdfModel = new ModelMem();
		Object[] edges = getEdges(rdfModel, graph, graph.getAllSelectedCells());
		createRDFModel(graph, rdfModel, edges);

		return rdfModel;
	}

	public Model getRDFModel() {
		RDFGraph graph = gmanager.getRDFGraph();
		Model rdfModel = new ModelMem();
		Object[] edges = getEdges(rdfModel, graph, graph.getAllCells());
		createRDFModel(graph, rdfModel, edges);

		return rdfModel;
	}

	private void createRDFModel(RDFGraph graph, Model rdfModel, Object[] edges) {
		for (int i = 0; i < edges.length; i++) {
			Edge edge = (Edge) edges[i];
			RDFResourceInfo info = resInfoMap.getCellInfo(graph.getSourceVertex(edge));
			Resource subject = getResource(info);
			try {
				Object propCell = rdfsInfoMap.getEdgeInfo(edge);
				RDFSInfo propInfo = rdfsInfoMap.getCellInfo(propCell);
				Property property = null;
				if (propInfo.getURIType() == URIType.URI) {
					property = new PropertyImpl(propInfo.getURI().getURI());
				} else {
					property = new PropertyImpl(gmanager.getBaseURI()+propInfo.getURI().getURI());
				}
				GraphCell targetCell = (GraphCell) graph.getTargetVertex(edge);

				if (graph.isRDFResourceCell(targetCell)) {
					info = resInfoMap.getCellInfo(targetCell);
					Resource object = getResource(info);
					rdfModel.add(rdfModel.createStatement(subject, property, object));
				} else if (graph.isRDFLiteralCell(targetCell)) {
					Literal object = litInfoMap.getCellInfo(targetCell);
					rdfModel.add(rdfModel.createStatement(subject, property, object));
				}
			} catch (RDFException e) {
				e.printStackTrace();
			}
		}
	}

}
