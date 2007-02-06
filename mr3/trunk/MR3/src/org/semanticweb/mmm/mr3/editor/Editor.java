/*
 * @(#) Editor.java
 * 
 * 
 * Copyright (C) 2003-2005 The MMM Project
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *  
 */

package org.semanticweb.mmm.mr3.editor;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.undo.*;

import org.jgraph.*;
import org.jgraph.event.*;
import org.jgraph.graph.*;
import org.semanticweb.mmm.mr3.actions.*;
import org.semanticweb.mmm.mr3.data.*;
import org.semanticweb.mmm.mr3.data.MR3Constants.*;
import org.semanticweb.mmm.mr3.io.*;
import org.semanticweb.mmm.mr3.jgraph.*;
import org.semanticweb.mmm.mr3.ui.*;
import org.semanticweb.mmm.mr3.util.*;

/*
 * 
 * @author takeshi morita
 * 
 */
public abstract class Editor extends JInternalFrame implements GraphSelectionListener {

    protected RDFGraph graph;
    protected GraphManager gmanager;
    protected JScrollPane graphScrollPane;
    protected GraphUndoManager undoManager;

    protected Object[] lastSelectionCells;

    protected MR3CellMaker cellMaker;
    protected MR3Generator mr3Generator;
    protected MR3Parser mr3Parser;

    protected Action undo, redo, remove;
    private RDFSInfoMap rdfsInfoMap = RDFSInfoMap.getInstance();

    protected Editor(String title) {
        super(title, true, false, true);
        setIconifiable(true);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        try {
            setSelected(true);
        } catch (java.beans.PropertyVetoException e) {
            e.printStackTrace();
        }
        setVisible(true);
    }

    protected void initEditor(RDFGraph g, GraphManager gm) {
        graph = g;
        lastSelectionCells = new Object[0];
        initField(gm);
        initListener();
        initLayout();
    }

    protected void initField(GraphManager gm) {
        gmanager = gm;
        undoManager = new GraphUndoManager();
        cellMaker = new MR3CellMaker(gmanager);
        mr3Parser = new MR3Parser(gmanager);
        mr3Generator = new MR3Generator(gmanager);
    }

    protected void initListener() {
        graph.getModel().addUndoableEditListener(undoManager);
        graph.getSelectionModel().addGraphSelectionListener(this);
        graph.addKeyListener(new EditorKeyEvent());
    }

    protected void initLayout() {
        Container container = getContentPane();
        container.setLayout(new BorderLayout());
        container.add(createToolBar(), BorderLayout.NORTH);
        graphScrollPane = new JScrollPane(graph);
        container.add(graphScrollPane, BorderLayout.CENTER);
    }

    class EditorKeyEvent extends KeyAdapter {
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                remove.actionPerformed(null);
            }
        }
    }

    // Brings the Specified Cells to Front
    public void toFront(Object[] c) {
        if (c != null && c.length > 0) graph.getGraphLayoutCache().toFront(graph.getGraphLayoutCache().getMapping(c));
    }

    // Sends the Specified Cells to Back
    public void toBack(Object[] c) {
        if (c != null && c.length > 0) graph.getGraphLayoutCache().toBack(graph.getGraphLayoutCache().getMapping(c));
    }

    protected void setToolStatus() {
        boolean enabled = !graph.isSelectionEmpty();
        remove.setEnabled(enabled);
    }

    public JViewport getJViewport() {
        return graphScrollPane.getViewport();
    }

    public JGraph getGraph() {
        return graph;
    }

    private static final Point INSERT_POINT = new Point(10, 10);
    protected static JToggleButton editModeButton = new JToggleButton();

    class InsertEllipseResourceAction extends AbstractAction {

        InsertEllipseResourceAction() {
            super("", Utilities.getImageIcon("ellipse.gif"));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_MASK));
        }

        public void actionPerformed(ActionEvent e) {
            RDFGraphMarqueeHandler mh = (RDFGraphMarqueeHandler) graph.getMarqueeHandler();
            GraphCell insertCell = mh.insertResourceCell(INSERT_POINT);
            if (insertCell != null) {
                if (graph.getType() == GraphType.RDF) {
                    HistoryManager.saveHistory(HistoryType.INSERT_RESOURCE, insertCell);
                } else {
                    HistoryManager.saveHistory(HistoryType.INSERT_ONT_PROPERTY, insertCell);
                }
            }
        }
    }

    class InsertRectangleResourceAction extends AbstractAction {

        InsertRectangleResourceAction() {
            super("", Utilities.getImageIcon("rectangle.gif"));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_MASK));
        }

        public void actionPerformed(ActionEvent e) {
            RDFGraphMarqueeHandler mh = (RDFGraphMarqueeHandler) graph.getMarqueeHandler();
            if (gmanager.isRDFGraph(graph)) {
                GraphCell cell = mh.insertLiteralCell(INSERT_POINT);
                if (cell != null) {
                    HistoryManager.saveHistory(HistoryType.INSERT_LITERAL, cell);
                }
            } else if (gmanager.isClassGraph(graph)) {
                GraphCell cell = mh.insertResourceCell(INSERT_POINT);
                if (cell != null) {
                    HistoryManager.saveHistory(HistoryType.INSERT_CLASS, cell);
                }
            }
        }
    }

    /** _Create ToolBar */
    public JToolBar createToolBar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);

        if (graph.getMarqueeHandler() instanceof RDFGraphMarqueeHandler) {
            RDFGraphMarqueeHandler mh = (RDFGraphMarqueeHandler) graph.getMarqueeHandler();

            ButtonGroup group = new ButtonGroup();
            mh.moveButton.setSelected(true);
            group.add(mh.moveButton);
            group.add(mh.connectButton);

            mh.moveButton.setIcon(Utilities.getImageIcon("move.gif")); // move
            toolbar.add(mh.moveButton);

            ImageIcon connectIcon = Utilities.getImageIcon(Translator.getString("Action.Connect.Icon"));
            mh.connectButton.setIcon(connectIcon);
            toolbar.add(mh.connectButton);

            if (gmanager.isRDFGraph(graph)) {
                toolbar.addSeparator();
                editModeButton.setIcon(Utilities.getImageIcon("editMode.gif"));
                toolbar.add(editModeButton);
                // toolbar.add(new AbstractAction("", connectIcon) {
                // public void actionPerformed(ActionEvent e) {
                // GraphCell cell = (GraphCell) graph.getSelectionCell();
                // if (graph.isOneCellSelected(cell) &&
                // graph.isRDFResourceCell(cell)) {
                // Port port = (Port) ((DefaultGraphCell) cell).getChildAt(0);
                // cellMaker.selfConnect(port, "", graph);
                // }
                // }
                // });
            }
        }

        toolbar.addSeparator();

        if (gmanager.isRDFGraph(graph) || gmanager.isPropertyGraph(graph)) {
            toolbar.add(new InsertEllipseResourceAction());
        }

        if (!gmanager.isPropertyGraph(graph)) {
            toolbar.add(new InsertRectangleResourceAction());
        }

        toolbar.addSeparator();
        undo = new UndoAction();
        undo.setEnabled(true);
        toolbar.add(undo);
        redo = new RedoAction();
        redo.setEnabled(true);
        toolbar.add(redo);

        toolbar.addSeparator();
        toolbar.add(new CopyAction(graph));
        toolbar.add(new CutAction(graph));
        toolbar.add(new PasteAction(graph, gmanager));

        toolbar.addSeparator();
        remove = new RemoveAction(graph, gmanager);
        remove.setEnabled(false);
        toolbar.add(remove);

        toolbar.addSeparator();
        toolbar.add(new FindResAction(graph, gmanager));

        toolbar.addSeparator();
        toolbar.add(new ZoomAction(graph, this, ZoomAction.ZOOM_STD, ZoomAction.ZOOM_STD_ICON));
        toolbar.add(new ZoomAction(graph, this, ZoomAction.ZOOM_IN, ZoomAction.ZOOM_IN_ICON));
        toolbar.add(new ZoomAction(graph, this, ZoomAction.ZOOM_OUT, ZoomAction.ZOOM_OUT_ICON));
        toolbar.add(new ZoomAction(graph, this, ZoomAction.ZOOM_SUITABLE, ZoomAction.ZOOM_SUITABLE_ICON));

        toolbar.addSeparator();
        toolbar.add(new GroupAction(graph));
        toolbar.add(new UnGroupAction(graph));
        toolbar.addSeparator();
        toolbar.add(new GraphLayoutAction(gmanager, graph.getType()));

        return toolbar;
    }

    // Update Undo/Redo Button State based on Undo Manager
    protected void updateHistoryButtons() {
        // The View Argument Defines the Context
        undo.setEnabled(undoManager.canUndo(graph.getGraphLayoutCache()));
        redo.setEnabled(undoManager.canRedo(graph.getGraphLayoutCache()));
    }

    private void uriConsistencyCheck(Object[] orgAllCells) {
        if (graph.getType() == GraphType.RDF) { return; }
        Object[] newAllCells = graph.getAllCells();
        Set<GraphCell> newRDFSCellSet = new HashSet<GraphCell>();
        for (int i = 0; i < newAllCells.length; i++) { // undo/redo前よりもセル数が増えた場合
            GraphCell cell = (GraphCell) newAllCells[i];
            if (RDFGraph.isRDFSCell(cell)) {
                newRDFSCellSet.add(cell);
                RDFSInfo info = (RDFSInfo) GraphConstants.getValue(cell.getAttributes());
                if (graph.getType() == GraphType.CLASS && !rdfsInfoMap.isClassCell(info.getURI())) {
                    rdfsInfoMap.putURICellMap(info, cell);
                } else if (graph.getType() == GraphType.PROPERTY && !rdfsInfoMap.isPropertyCell(info.getURI())) {
                    rdfsInfoMap.putURICellMap(info, cell);
                }
            }
        }
        for (int i = 0; i < orgAllCells.length; i++) { // undo/redo前よりもセル数が減った場合
            GraphCell cell = (GraphCell) orgAllCells[i];
            if (RDFGraph.isRDFSCell(cell) && !newRDFSCellSet.contains(cell)) {
                rdfsInfoMap.removeCellInfo(cell);
            }
        }
    }

    class UndoAction extends AbstractAction {
        UndoAction() {
            super("undo", Utilities.getImageIcon("undo.gif"));
        }
        // Undo the last Change to the Model or the View
        public void actionPerformed(ActionEvent e) {
            try {
                Object[] orgAllCells = graph.getAllCells();
                int orgCellCnt = orgAllCells.length;
                while (true) {
                    undoManager.undo(graph.getGraphLayoutCache());
                    Object[] newAllCells = graph.getAllCells();
                    if (orgCellCnt != newAllCells.length) {
                        uriConsistencyCheck(orgAllCells);
                        break;
                    }
                }
            } catch (CannotUndoException ex) {
                // ex.printStackTrace();
            } finally {
                updateHistoryButtons();
            }
        }
    }

    class RedoAction extends AbstractAction {
        RedoAction() {
            super("redo", Utilities.getImageIcon("redo.gif"));
        }
        // Redo the last Change to the Model or the View
        public void actionPerformed(ActionEvent e) {
            try {
                Object[] orgAllCells = graph.getAllCells();
                int orgCellCnt = orgAllCells.length;
                while (true) {
                    undoManager.redo(graph.getGraphLayoutCache());
                    Object[] newAllCells = graph.getAllCells();
                    if (orgCellCnt != newAllCells.length) {
                        uriConsistencyCheck(orgAllCells);
                        break;
                    }
                }
            } catch (CannotRedoException ex) {
                // ex.printStackTrace();
            } finally {
                updateHistoryButtons();
            }
        }
    }

    public JScrollPane getJScrollPane() {
        return graphScrollPane;
    }

    // This will change the source of the actionevent to graph.
    protected class EventRedirector extends AbstractAction {

        protected Action action;

        // Construct the "Wrapper" Action
        public EventRedirector(Action a) {
            super("", (ImageIcon) a.getValue(Action.SMALL_ICON));
            this.action = a;
        }

        // Redirect the Actionevent
        public void actionPerformed(ActionEvent e) {
            e = new ActionEvent(graph, e.getID(), e.getActionCommand(), e.getModifiers());
            action.actionPerformed(e);
        }
    }
}
