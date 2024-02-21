/*
 * Copyright (c) 2022. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.editor;
import com.cyte.edamame.EDAmameApplication;
import com.cyte.edamame.EDAmameController;
import com.cyte.edamame.memento.*;
import com.cyte.edamame.netlist.NetListExperimental;
import com.cyte.edamame.netlist.NetListExperimentalNode;
import com.cyte.edamame.node.*;

import java.util.*;
import java.util.logging.Level;

import com.cyte.edamame.misc.PairMutable;
import javafx.collections.*;
import javafx.geometry.Point2D;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.canvas.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;

import java.io.InvalidClassException;

/**
 * Librarys and projects are all modified through the use of special purpose editor modules.<p>
 *
 * All Controller_Editors are based on the abstract Editor class so that they conform with what the EDAmame main
 * application expects and will attempt to obtain and display editor specific controls for.
 *
 * @author Jeff Wiegley, Ph.D.
 * @author jeffrey.wiegley@gmail.com
 */
abstract public class Editor implements Originator, Recorded, StateHashable
{
    //// GLOBAL VARIABLES ////

    /** Every editor can be uniquely identified by a random UUID (which do not persist across application execution. */
    final public String id = UUID.randomUUID().toString();

    public Integer type = -1;
    public String name = null;

    // DO NOT EDIT

    // holders for the UI elements. The UI elements are instantiated in a single FXML file/load
    // The individual elements are extracted to these for use by the EDAmame Application.

    /** The main Editor_Tab for EDAmame to include in its main Editor_Tab list. */
    protected Tab tab = null;
    /** An optional ToolBar to provide EDAmame to include/append to its toolbars. */
    protected ToolBar toolBar = null;
    /** a list of Editor_Tabs to include in the navigation Editor_Tab pane when this editor is active. (can be empty) */
    protected ObservableList<Tab> tabs = FXCollections.observableArrayList();
    /**
     * A structure of menu items to include in EDAmame's Editor_Menus. Any menuitems associated with a string will
     * be inserted/visible under the menu with the same name in the EDAmame main menubar. The string must
     * match exactly including mneumonic underscores and such. Missing Editor_Menus at the EDAmame level are not created.
     */
    public HashMap<String, ObservableList<MenuItem>> menus = new HashMap<String, ObservableList<MenuItem>>();    // NOTE: We have to use the hash map because a list would cause the menu items to disappear after the first editor dissection

    public PairMutable zoomLimits;
    public Double zoomFactor;
    public Double mouseDragFactor;
    public Double mouseDragCheckTimeout;
    public Color selectionBoxColor;
    public Double selectionBoxWidth;

    public Pane paneListener;
    public Pane paneHolder;
    public Pane paneHighlights;
    public Pane paneSelections;
    public Pane paneSnaps;
    public Canvas canvas;
    public GraphicsContext gc;
    public PairMutable theaterSize;
    public Color backgroundColor;
    public Color gridPointColor;
    public Color gridBoxColor;
    public Integer maxShapes;
    public LinkedList<EDANode> nodes;
    public PairMutable center;
    public boolean visible = false;
    public boolean pressedLMB = false;
    public boolean pressedRMB = false;
    public Double zoom = 1.0;
    public PairMutable mouseDragFirstPos = null;
    public PairMutable mouseDragDiffPos = null;
    public Long mouseDragLastTime = System.nanoTime();
    public PairMutable mouseDragFirstCenter = null;
    public boolean mouseDragReachedEdge = false;
    public PairMutable mouseDragPaneFirstPos = null;
    public Integer shapesHighlighted = 0;
    public Integer shapesSelected = 0;
    public boolean shapesWereSelected = false;
    public boolean shapesMoving = false;
    public Rectangle selectionBox = null;
    public boolean wasSelectionBox = false;
    public Line linePreview = null;
    public double snapGridSpacing = -1;
    static public int GerberApertureCounter = 10;

    //public MementoExperimental undoRedoSystem = null;
    private Recorder recorder;
    private int lastNodesHash = 0;

    //// MAIN FUNCTIONS ////

    public void Init(Integer type, String name)
    {
        this.type = type;
        this.name = name;

        this.center = new PairMutable(0.0, 0.0);
        this.zoomLimits = EDAmameController.Editor_ZoomLimits;
        this.zoomFactor = EDAmameController.Editor_ZoomFactor;
        this.mouseDragFactor = EDAmameController.Editor_MouseDragFactor;
        this.mouseDragCheckTimeout = EDAmameController.Editor_MouseCheckTimeout;
        this.selectionBoxColor = EDAmameController.Editor_SelectionBoxColors[this.type];
        this.selectionBoxWidth = EDAmameController.Editor_SelectionBoxWidth;

        this.theaterSize = EDAmameController.Editor_TheaterSize;
        this.backgroundColor = EDAmameController.Editor_BackgroundColors[type];
        this.gridPointColor = EDAmameController.Editor_GridPointColors[type];
        this.gridBoxColor = EDAmameController.Editor_GridPointColors[type];
        this.maxShapes = EDAmameController.Editor_MaxShapes;
        this.nodes = new LinkedList<EDANode>();

        this.recorder = new Recorder(this);
        registerRecorder(recorder);
        //this.undoRedoSystem = new MementoExperimental(this);
    }

    /**
     * Request an editor to close. Handling any information/state saving as it needs.
     * @return true if the editor was able to close without unsaved information/state, false otherwise.
     */
    public void Close()
    {
        EDAmameApplication.controller.EditorRemove(this);
    }

    public void Heartbeat()
    {
        if ((this.type == -1) || (this.name == null))
            throw new java.lang.Error("ERROR: Attempting to run editor without initializing it!");

        this.TestShapesClear();

        // Handling render node highlight, selected & snap shapes refreshing...
        for (int i = 0; i < this.nodes.size(); i++)
            this.nodes.get(i).Heartbeat();

        // Handling centering of holder pane & crosshair...
        {
            PairMutable canvasSize = new PairMutable(this.canvas.getWidth(),
                    this.canvas.getHeight());
            PairMutable paneSize = new PairMutable(this.paneListener.getWidth(),
                    this.paneListener.getHeight());
            PairMutable centeredPos = new PairMutable(paneSize.GetLeftDouble() / 2 - canvasSize.GetLeftDouble() / 2,
                    paneSize.GetRightDouble() / 2 - canvasSize.GetRightDouble() / 2);

            this.paneHolder.setLayoutX(centeredPos.GetLeftDouble());
            this.paneHolder.setLayoutY(centeredPos.GetRightDouble());
        }

        //for (int i = 0; i < this.Editor_RenderSystem.RenderSystem_Nodes.size(); i++)
        //    System.out.println(this.Editor_RenderSystem.RenderSystem_Nodes.get(i).RenderNode_Node.getBoundsInParent().toString());

        //System.out.println(this.Editor_RenderSystem.RenderSystem_Nodes.size());

        //System.out.println(this.Editor_RenderSystem.RenderSystem_Nodes.size());

        System.out.println(this.shapesHighlighted + ", " + this.shapesSelected);
        //System.out.println(this.linePreview);
    }

    //// GETTER FUNCTIONS ////

    /**
     * Returns the main editor Tab node for inclusion by EDAmame.
     * @return the main Editor_Tab for this editor.
     */
    public Tab GetTab() { return tab; }

    /**
     * Returns the optional ToolBar of this editor for inclusion by EDAmame.
     * @return The ToolBar if available, null otherwise.
     */
    public ToolBar GetToolBar() { return toolBar; }

    /**
     * Returns a list of control Editor_Tabs for EDAmame to include in the main controls Editor_Tab pane.
     * @return A (possibly empty) list of control Editor_Tabs.
     */
    public ObservableList<Tab> GetControlTabs() { return tabs; }

    //// PANE FUNCTIONS ////

    public PairMutable PaneHolderGetDrawPos(PairMutable pos)
    {
        return new PairMutable(pos.GetLeftDouble() + this.paneHolder.getWidth() / 2,
                pos.GetRightDouble() + this.paneHolder.getHeight() / 2);
    }

    public PairMutable PaneHolderGetRealPos(PairMutable pos)
    {
        return new PairMutable(pos.GetLeftDouble() - this.paneHolder.getWidth() / 2,
                pos.GetRightDouble() - this.paneHolder.getHeight() / 2);
    }

    public PairMutable PanePosListenerToHolder(PairMutable pos)
    {
        Point2D newPos = this.paneHolder.parentToLocal(pos.GetLeftDouble(), pos.GetRightDouble());

        return new PairMutable(newPos.getX(), newPos.getY());
    }

    public PairMutable PanePosHolderToListener(PairMutable pos)
    {
        Point2D newPos = this.paneHolder.localToParent(pos.GetLeftDouble(), pos.GetRightDouble());

        return new PairMutable(newPos.getX(), newPos.getY());
    }

    public void PaneHolderSetTranslate(PairMutable pos)
    {
        this.paneHolder.setTranslateX(pos.GetLeftDouble());
        this.paneHolder.setTranslateY(pos.GetRightDouble());
    }

    public void PaneHolderSetScale(PairMutable scale, boolean compensate)
    {
        PairMutable prevScale = this.PaneHolderGetScale();

        this.paneHolder.setScaleX(scale.GetLeftDouble());
        this.paneHolder.setScaleY(scale.GetRightDouble());

        if (compensate)
        {
            PairMutable scaleDelta = new PairMutable(scale.GetLeftDouble() - prevScale.GetLeftDouble(),
                    scale.GetRightDouble() - prevScale.GetRightDouble());
            PairMutable newPos = this.PaneHolderGetTranslate();

            newPos.left = newPos.GetLeftDouble() + this.center.GetLeftDouble() * scaleDelta.GetLeftDouble();
            newPos.right = newPos.GetRightDouble() + this.center.GetRightDouble() * scaleDelta.GetRightDouble();

            this.PaneHolderSetTranslate(newPos);
        }
    }

    public PairMutable PaneHolderGetScale()
    {
        return new PairMutable(this.paneHolder.getScaleX(), this.paneHolder.getScaleY());
    }

    public PairMutable PaneHolderGetTranslate()
    {
        return new PairMutable(this.paneHolder.getTranslateX(), this.paneHolder.getTranslateY());
    }

    //// CANVAS FUNCTIONS ////

    public void CanvasDraw()
    {
        // Clearing the canvas
        this.CanvasClear();

        // Drawing the points
        gc.setFill(this.gridPointColor);
        gc.setGlobalAlpha(0.5);

        double width = 2;
        double spacing = 100;

        if (this.snapGridSpacing != -1)
            spacing = this.snapGridSpacing * 10;

        for (int i = 0; i < 100; i++)
            for (int j = 0; j < 100; j++)
                gc.fillOval(i * spacing + this.canvas.getWidth() / 2 - width / 2, j * spacing + this.canvas.getHeight() / 2 - width / 2, width, width);
        for (int i = 1; i < 100; i++)
            for (int j = 1; j < 100; j++)
                gc.fillOval(-i * spacing + this.canvas.getWidth() / 2 - width / 2, j * spacing + this.canvas.getHeight() / 2 - width / 2, width, width);
        for (int i = 1; i < 100; i++)
            for (int j = 1; j < 100; j++)
                gc.fillOval(i * spacing + this.canvas.getWidth() / 2 - width / 2, -j * spacing + this.canvas.getHeight() / 2 - width / 2, width, width);
        for (int i = 0; i < 100; i++)
            for (int j = 0; j < 100; j++)
                if ((i != 0) || (j != 0))
                    gc.fillOval(-i * spacing + this.canvas.getWidth() / 2 - width / 2, -j * spacing + this.canvas.getHeight() / 2 - width / 2, width, width);

        // Drawing the grid box
        gc.setStroke(this.gridBoxColor);
        gc.setGlobalAlpha(1.0);
        gc.setLineWidth(2.0);
        gc.strokeLine(this.canvas.getWidth() / 2 + -EDAmameController.Editor_TheaterSize.GetLeftDouble() / 2,
                this.canvas.getHeight() / 2 + -EDAmameController.Editor_TheaterSize.GetRightDouble() / 2,
                this.canvas.getWidth() / 2 + EDAmameController.Editor_TheaterSize.GetLeftDouble() / 2,
                this.canvas.getHeight() / 2 + -EDAmameController.Editor_TheaterSize.GetRightDouble() / 2);
        gc.strokeLine(this.canvas.getWidth() / 2 + EDAmameController.Editor_TheaterSize.GetLeftDouble() / 2,
                this.canvas.getHeight() / 2 + -EDAmameController.Editor_TheaterSize.GetRightDouble() / 2,
                this.canvas.getWidth() / 2 + EDAmameController.Editor_TheaterSize.GetLeftDouble() / 2,
                this.canvas.getHeight() / 2 + EDAmameController.Editor_TheaterSize.GetRightDouble() / 2);
        gc.strokeLine(this.canvas.getWidth() / 2 + EDAmameController.Editor_TheaterSize.GetLeftDouble() / 2,
                this.canvas.getHeight() / 2 + EDAmameController.Editor_TheaterSize.GetRightDouble() / 2,
                this.canvas.getWidth() / 2 + -EDAmameController.Editor_TheaterSize.GetLeftDouble() / 2,
                this.canvas.getHeight() / 2 + EDAmameController.Editor_TheaterSize.GetRightDouble() / 2);
        gc.strokeLine(this.canvas.getWidth() / 2 + -EDAmameController.Editor_TheaterSize.GetLeftDouble() / 2,
                this.canvas.getHeight() / 2 + EDAmameController.Editor_TheaterSize.GetRightDouble() / 2,
                this.canvas.getWidth() / 2 + -EDAmameController.Editor_TheaterSize.GetLeftDouble() / 2,
                this.canvas.getHeight() / 2 + -EDAmameController.Editor_TheaterSize.GetRightDouble() / 2);
    }

    public void CanvasClear()
    {
        this.gc.clearRect(0, 0, this.canvas.getWidth(), this.canvas.getHeight());
        this.gc.setFill(this.backgroundColor);
        this.gc.fillRect(0, 0, this.canvas.getWidth(), this.canvas.getHeight());
    }

    public void CanvasRedraw()
    {
        this.CanvasClear();
        this.CanvasDraw();
    }

    //// NODE FUNCTIONS ////

    public void NodesDeselectAll()
    {
        // Deselecting all the nodes...
        for (int i = 0; i < this.nodes.size(); i++)
            this.nodes.get(i).SelectCheck();

        // Removing the selection box...
        if (this.selectionBox != null)
        {
            this.paneListener.getChildren().remove(this.selectionBox);
            this.selectionBox = null;
            this.wasSelectionBox = true;
        }
        else
        {
            this.wasSelectionBox = false;
        }
    }

    public void NodeSnapPointsCheck(PairMutable posEvent)
    {
        PairMutable posMouse = this.PanePosListenerToHolder(new PairMutable(posEvent.GetLeftDouble(), posEvent.GetRightDouble()));

        for (int i = 0; i < this.nodes.size(); i++)
            this.nodes.get(i).SnapPointsCheck(posMouse);
    }

    public void NodeHighlightsCheck(PairMutable posEvent)
    {
        for (int i = 0; i < this.nodes.size(); i++)
            this.nodes.get(i).HighlightCheck(this.PanePosListenerToHolder(new PairMutable(posEvent.GetLeftDouble(), posEvent.GetRightDouble())));
    }

    public int NodeFindByID(String id)
    {
        for (int i = 0; i < this.nodes.size(); i++)
            if (this.nodes.get(i).id.equals(id))
                return i;

        return -1;
    }

    public int NodeFindByName(String name)
    {
        for (int i = 0; i < this.nodes.size(); i++)
            if (this.nodes.get(i).name.equals(name))
                return i;

        return -1;
    }

    public void NodesAdd(LinkedList<EDANode> nodes)
    {
        for (int i = 0; i < nodes.size(); i++)
            nodes.get(i).Add();
    }

    public void NodesClear()
    {
        while (!this.nodes.isEmpty())
            this.nodes.get(0).Remove();
    }

    public LinkedList<EDANode> NodesClone()
    {
        LinkedList<EDANode> clonedNodes = new LinkedList<EDANode>();

        for (int i = 0; i < this.nodes.size(); i++)
            clonedNodes.add(this.nodes.get(i).Clone());

        return clonedNodes;
    }

    //// CURSOR FUNCTIONS ////

    abstract public PairMutable CursorPreviewGetPos();
    abstract public void CursorPreviewUpdate(PairMutable pos);

    public void MouseDragUpdate(PairMutable posMouse)
    {
        this.mouseDragDiffPos = new PairMutable((posMouse.GetLeftDouble() - this.mouseDragFirstPos.GetLeftDouble()) * this.mouseDragFactor / this.zoom,
                (posMouse.GetRightDouble() - this.mouseDragFirstPos.GetRightDouble()) * this.mouseDragFactor / this.zoom);
    }

    public void MouseDragReset(PairMutable posMouse)
    {
        this.mouseDragFirstPos = new PairMutable(posMouse);
        this.mouseDragFirstCenter = new PairMutable(this.center.GetLeftDouble(), this.center.GetRightDouble());
        this.mouseDragPaneFirstPos = new PairMutable(this.PaneHolderGetTranslate());

        if (this.shapesSelected > 0)
            for (int i = 0; i < this.nodes.size(); i++)
                this.nodes.get(i).MoveReset();
    }

    //// LINE PREVIEW FUNCTIONS ////

    public void LinePreviewUpdate()
    {
        PairMutable dropPos = this.PanePosListenerToHolder(this.CursorPreviewGetPos());

        this.linePreview.setEndX(dropPos.GetLeftDouble());
        this.linePreview.setEndY(dropPos.GetRightDouble());
    }

    public void LinePreviewRemove()
    {
        this.nodes.get(this.NodeFindByName("linePreview")).Remove();
        this.linePreview = null;
    }

    //// CALLBACK FUNCTIONS ////

    public void OnDragOverGlobal(DragEvent event)
    {
        PairMutable eventPos = new PairMutable(event.getX(), event.getY());

        // Handling cursor preview...
        this.CursorPreviewUpdate(eventPos);

        // Handling shape highlights...
        this.NodeHighlightsCheck(eventPos);

        // Handling node snap shapes...
        this.NodeSnapPointsCheck(eventPos);
    }

    public void OnDragDroppedGlobal(DragEvent event)
    {}

    public void OnMouseMovedGlobal(MouseEvent event)
    {
        PairMutable eventPos = new PairMutable(event.getX(), event.getY());
        PairMutable dropPos = this.PanePosListenerToHolder(eventPos);
        PairMutable realPos = this.PaneHolderGetRealPos(dropPos);

        // Handling cursor preview...
        this.CursorPreviewUpdate(eventPos);

        // Handling shape highlights...
        this.NodeHighlightsCheck(eventPos);

        // Handling node snap shapes...
        this.NodeSnapPointsCheck(eventPos);

        // Handling line drawing preview...
        if (this.linePreview != null)
            this.LinePreviewUpdate();
    }

    public void OnMousePressedGlobal(MouseEvent event)
    {
        if (this.pressedLMB)
        {
            recorder.update();
        }
        else if (this.pressedRMB)
        {}

        this.mouseDragFirstPos = null;
        this.mouseDragPaneFirstPos = null;

        //if (this.Editor_ShapesHighlighted > 0)
        //    this.Editor_PressedOnShape = true;
    }

    public void OnMouseReleasedGlobal(MouseEvent event)
    {
        if (this.pressedLMB)
        {
            if (this.shapesSelected > 0)
                this.shapesWereSelected = true;
            else
                this.shapesWereSelected = false;

            // Handling shape deselection (only if we're not moving any shapes or drawing any lines)
            if (!this.shapesMoving &&
                    (this.linePreview == null))
                this.NodesDeselectAll();
        }
        else if (this.pressedRMB)
        {
            // Handling auto-zoom
            if (EDAmameController.IsKeyPressed(KeyCode.ALT))
            {
                this.PaneHolderSetTranslate(new PairMutable(0.0, 0.0));
                this.PaneHolderSetScale(new PairMutable(1.0, 1.0), false);

                this.center = new PairMutable(0.0, 0.0);
                this.zoom = 1.0;
            }
        }

        this.shapesMoving = false;
        //this.Editor_PressedOnShape = false;
    }

    public void OnMouseDraggedGlobal(MouseEvent event)
    {
        PairMutable eventPos = new PairMutable(event.getX(), event.getY());
        PairMutable dropPos = this.PanePosListenerToHolder(eventPos);
        PairMutable realPos = this.PaneHolderGetRealPos(dropPos);

        // Handling cursor preview...
        this.CursorPreviewUpdate(eventPos);

        // Handling shape highlights...
        this.NodeHighlightsCheck(eventPos);

        // Handling node snap shapes...
        this.NodeSnapPointsCheck(eventPos);

        // Handling line drawing preview...
        if (this.linePreview != null)
            this.LinePreviewUpdate();

        if (this.pressedLMB)
        {
            // Handling moving of the shapes (only if we have some shapes selected, we're not holding the box selection key and we're not drawing any lines)
            if ((this.shapesSelected > 0) &&
                    !EDAmameController.IsKeyPressed(KeyCode.SHIFT) &&
                    (this.linePreview == null))
            {
                for (int i = 0; i < this.nodes.size(); i++)
                    this.nodes.get(i).Move();

                this.shapesMoving = true;
            }

            // Handling the box selection (only if we have no shapes selected, we are not moving the viewport and we're not drawing any lines)
            if (((this.shapesSelected == 0) || EDAmameController.IsKeyPressed(KeyCode.SHIFT)) &&
                    (this.linePreview == null))
            {
                if (this.selectionBox == null)
                {
                    this.selectionBox = new Rectangle(0.0, 0.0);
                    this.selectionBox.setFill(Color.TRANSPARENT);
                    this.selectionBox.setStroke(this.selectionBoxColor);
                    this.selectionBox.setStrokeWidth(this.selectionBoxWidth);

                    this.paneListener.getChildren().add(1, this.selectionBox);
                }

                // Adjusting if the width & height are negative...
                if (this.mouseDragDiffPos.GetLeftDouble() < 0)
                    this.selectionBox.setTranslateX(this.mouseDragFirstPos.GetLeftDouble() + this.mouseDragDiffPos.GetLeftDouble() * this.zoom / this.mouseDragFactor);
                else
                    this.selectionBox.setTranslateX(this.mouseDragFirstPos.GetLeftDouble());

                if (this.mouseDragDiffPos.GetRightDouble() < 0)
                    this.selectionBox.setTranslateY(this.mouseDragFirstPos.GetRightDouble() + this.mouseDragDiffPos.GetRightDouble() * this.zoom / this.mouseDragFactor);
                else
                    this.selectionBox.setTranslateY(this.mouseDragFirstPos.GetRightDouble());

                this.selectionBox.setWidth(Math.abs(this.mouseDragDiffPos.GetLeftDouble() * this.zoom / this.mouseDragFactor));
                this.selectionBox.setHeight(Math.abs(this.mouseDragDiffPos.GetRightDouble() * this.zoom / this.mouseDragFactor));
            }
        }
        else if (this.pressedRMB)
        {
            // Handling moving of the viewport
            {
                this.mouseDragReachedEdge = false;

                this.center.left = this.mouseDragFirstCenter.GetLeftDouble() + this.mouseDragDiffPos.GetLeftDouble();
                this.center.right = this.mouseDragFirstCenter.GetRightDouble() + this.mouseDragDiffPos.GetRightDouble();

                this.PaneHolderSetTranslate(new PairMutable(this.mouseDragPaneFirstPos.GetLeftDouble() + this.mouseDragDiffPos.GetLeftDouble() * this.zoom,
                        this.mouseDragPaneFirstPos.GetRightDouble() + this.mouseDragDiffPos.GetRightDouble() * this.zoom));
            }
        }
    }

    public void OnScrollGlobal(ScrollEvent event)
    {
        PairMutable eventPos = new PairMutable(event.getX(), event.getY());
        PairMutable dropPos = this.PanePosListenerToHolder(eventPos);
        PairMutable realPos = this.PaneHolderGetRealPos(dropPos);

        // Handling shape rotation (only if we have shapes selected and R is pressed)
        if ((this.shapesSelected > 0) && EDAmameController.IsKeyPressed(KeyCode.R))
        {
            for (int i = 0; i < this.nodes.size(); i++)
                this.nodes.get(i).Rotate(event.getDeltaY());
        }
        // Handling zoom scaling (only if we're not rotating anything)
        else
        {
            if (event.getDeltaY() < 0)
                if ((this.zoom / this.zoomFactor) <= this.zoomLimits.GetLeftDouble())
                    this.zoom = this.zoomLimits.GetLeftDouble();
                else
                    this.zoom /= this.zoomFactor;
            else
            if ((this.zoom * this.zoomFactor) >= this.zoomLimits.GetRightDouble())
                this.zoom = this.zoomLimits.GetRightDouble();
            else
                this.zoom *= this.zoomFactor;

            this.PaneHolderSetScale(new PairMutable(this.zoom, this.zoom), true);
        }

        // Handling cursor preview...
        this.CursorPreviewUpdate(eventPos);

        // Handling shape highlights...
        this.NodeHighlightsCheck(eventPos);

        // Handling node snap shapes...
        this.NodeSnapPointsCheck(eventPos);

        // Handling line drawing preview...
        if (this.linePreview != null)
            this.LinePreviewUpdate();
    }

    public void OnKeyPressedGlobal(KeyEvent event)
    {
        // Handling element properties window...
        if (EDAmameController.IsKeyPressed(KeyCode.E) && (EDAmameController.editorPropertiesWindow == null))
        {
            // Attempting to create the properties window...
            EditorProps propsWindow = EditorProps.Create(this);

            if ((propsWindow != null))
            {
                propsWindow.stage.setOnHidden(e -> {
                    EDAmameController.editorPropertiesWindow = null;
                });
                propsWindow.stage.show();

                EDAmameController.editorPropertiesWindow = propsWindow;
            }
        }

        // Handling all-shape selection...
        if (EDAmameController.IsKeyPressed(KeyCode.CONTROL) && EDAmameController.IsKeyPressed(KeyCode.A))
            for (int i = 0; i < this.nodes.size(); i++)
                this.nodes.get(i).Select();

        // Handling shape deletion...
        if (EDAmameController.IsKeyPressed(KeyCode.BACK_SPACE) || EDAmameController.IsKeyPressed(KeyCode.DELETE))
        {
            if (this.shapesSelected > 0)
            {
                for (int i = 0; i < this.nodes.size(); i++)
                {
                    EDANode node = this.nodes.get(i);

                    if (!node.selected)
                        continue;

                    node.Remove();
                    i--;
                }
            }
        }

        // Handling line drawing interruption & element deselection...
        if (EDAmameController.IsKeyPressed(KeyCode.ESCAPE))
        {
            if (this.linePreview != null)
                this.LinePreviewRemove();
            else
                this.NodesDeselectAll();
        }

        // Handling element undo...
        if (EDAmameController.IsKeyPressed(KeyCode.CONTROL) && EDAmameController.IsKeyPressed(KeyCode.Z))
            recorder.undo();

        // Handling element undo...
        if (EDAmameController.IsKeyPressed(KeyCode.CONTROL) && EDAmameController.IsKeyPressed(KeyCode.Y))
            recorder.redo();
    }

    public void OnKeyReleasedGlobal(KeyEvent event)
    {}

    abstract public void OnDragOverSpecific(DragEvent event);
    abstract public void OnDragDroppedSpecific(DragEvent event);
    abstract public void OnMouseMovedSpecific(MouseEvent event);
    abstract public void OnMousePressedSpecific(MouseEvent event);
    abstract public void OnMouseReleasedSpecific(MouseEvent event);
    abstract public void OnMouseDraggedSpecific(MouseEvent event);
    abstract public void OnScrollSpecific(ScrollEvent event);
    abstract public void OnKeyPressedSpecific(KeyEvent event);
    abstract public void OnKeyReleasedSpecific(KeyEvent event);

    public void ListenersInit()
    {
        // When we drag the mouse (from outside the viewport)...
        this.paneListener.setOnDragOver(event -> {
            // Handling global callback actions
            this.OnDragOverGlobal(event);

            // Handling editor-specific callback actions
            this.OnDragOverSpecific(event);

            event.consume();
        });

        // When we drop something with the cursor (from outside the viewport)...
        this.paneListener.setOnDragDropped(event -> {
            // Handling global callback actions
            this.OnDragDroppedGlobal(event);

            // Handling editor-specific callback actions
            this.OnDragDroppedSpecific(event);

            event.setDropCompleted(true);
            event.consume();
        });

        // When we move the mouse...
        this.paneListener.setOnMouseMoved(event -> {
            // Handling global callback actions
            this.OnMouseMovedGlobal(event);

            // Handling editor-specific callback actions
            this.OnMouseMovedSpecific(event);

            event.consume();
        });

        // When we press down the mouse...
        this.paneListener.setOnMousePressed(event -> {
            // Updating mouse pressed flags
            if (event.isPrimaryButtonDown())
                this.pressedLMB = true;
            if (event.isSecondaryButtonDown())
                this.pressedRMB = true;

            // Updating the current mouse drag positions
            PairMutable posMouse = new PairMutable(event.getX(), event.getY());

            if ((this.mouseDragFirstPos == null) || this.mouseDragReachedEdge)
                this.MouseDragReset(posMouse);

            // Handling global callback actions
            this.OnMousePressedGlobal(event);

            // Handling editor-specific callback actions
            this.OnMousePressedSpecific(event);

            event.consume();
        });

        // When we release the mouse...
        this.paneListener.setOnMouseReleased(event -> {
            // Handling global callback actions
            this.OnMouseReleasedGlobal(event);

            // Handling editor-specific callback actions
            this.OnMouseReleasedSpecific(event);

            // Updating mouse pressed flags
            this.pressedLMB = false;
            this.pressedRMB = false;

            event.consume();
        });

        // When we drag the mouse (from inside the viewport)...
        this.paneListener.setOnMouseDragged(event -> {
            // Only execute callback if we're past the check timeout
            if (((System.nanoTime() - this.mouseDragLastTime) / 1e9) < this.mouseDragCheckTimeout)
                return;

            // Updating the current mouse drag positions
            {
                PairMutable posMouse = new PairMutable(event.getX(), event.getY());

                if ((this.mouseDragFirstPos == null) || this.mouseDragReachedEdge)
                    this.MouseDragReset(posMouse);

                this.MouseDragUpdate(posMouse);
            }

            // Handling global callback actions
            this.OnMouseDraggedGlobal(event);

            // Handling editor-specific callback actions
            this.OnMouseDraggedSpecific(event);

            this.mouseDragLastTime = System.nanoTime();

            event.consume();
        });

        // When we scroll the mouse...
        this.paneListener.setOnScroll(event -> {
            // Handling global callback actions
            this.OnScrollGlobal(event);

            // Handling editor-specific callback actions
            this.OnScrollSpecific(event);

            event.consume();
        });
    }

    //// PROPERTIES WINDOW FUNCTIONS ////

    public void PropsLoadGlobal()
    {
        Text globalHeader = new Text("Global Properties:");
        globalHeader.setStyle("-fx-font-weight: bold;");
        globalHeader.setStyle("-fx-font-size: 16px;");
        EDAmameController.editorPropertiesWindow.propsBox.getChildren().add(globalHeader);
        EDAmameController.editorPropertiesWindow.propsBox.getChildren().add(new Separator());

        if (this.shapesSelected == 0)
            return;

        // Reading all global node properties...
        LinkedList<String> names = new LinkedList<String>();
        LinkedList<Double> posX = new LinkedList<Double>();
        LinkedList<Double> posY = new LinkedList<Double>();
        LinkedList<Double> rots = new LinkedList<Double>();

        for (int i = 0; i < this.nodes.size(); i++)
            this.nodes.get(i).PropsLoadGlobal(names, posX, posY, rots);

        // Creating name box...
        if (!names.isEmpty())
        {
            HBox nameBox = new HBox(10);
            nameBox.setId("nameBox");
            nameBox.getChildren().add(new Label("Element Names: "));
            TextField nameText = new TextField();
            nameText.setId("name");
            nameBox.getChildren().add(nameText);

            if (EDAmameController.IsListAllEqual(names))
                nameText.setText(names.get(0));
            else
                nameText.setText("<mixed>");

            EDAmameController.editorPropertiesWindow.propsBox.getChildren().add(nameBox);
        }

        // Creating position box...
        if (!posX.isEmpty() && !posY.isEmpty())
        {
            HBox posHBox = new HBox(10);
            posHBox.setId("posBox");
            posHBox.getChildren().add(new Label("Element Positions X: "));
            TextField posXText = new TextField();
            posXText.setMinWidth(100);
            posXText.setPrefWidth(100);
            posXText.setMaxWidth(100);
            posXText.setId("posX");
            posHBox.getChildren().add(posXText);
            posHBox.getChildren().add(new Label("Y: "));
            TextField posYText = new TextField();
            posYText.setId("posY");
            posYText.setMinWidth(100);
            posYText.setPrefWidth(100);
            posYText.setMaxWidth(100);
            posHBox.getChildren().add(posYText);

            if (EDAmameController.IsListAllEqual(posX))
                posXText.setText(Double.toString(posX.get(0)));
            else
                posXText.setText("<mixed>");

            if (EDAmameController.IsListAllEqual(posY))
                posYText.setText(Double.toString(posY.get(0)));
            else
                posYText.setText("<mixed>");

            EDAmameController.editorPropertiesWindow.propsBox.getChildren().add(posHBox);
        }

        // Creating rotation box...
        if (!rots.isEmpty())
        {
            HBox rotHBox = new HBox(10);
            rotHBox.setId("rotBox");
            rotHBox.getChildren().add(new Label("Element Rotations: "));
            TextField rotText = new TextField();
            rotText.setId("rot");
            rotHBox.getChildren().add(rotText);

            if (EDAmameController.IsListAllEqual(rots))
                rotText.setText(Double.toString(rots.get(0)));
            else
                rotText.setText("<mixed>");

            EDAmameController.editorPropertiesWindow.propsBox.getChildren().add(rotHBox);
        }

        EDAmameController.editorPropertiesWindow.propsBox.getChildren().add(new Separator());
    }

    public void PropsApplyGlobal()
    {
        if (this.shapesSelected == 0)
            return;

        VBox propsBox = EDAmameController.editorPropertiesWindow.propsBox;

        // Iterating over all the nodes & attempting to apply global node properties if selected...
        for (int i = 0; i < this.nodes.size(); i++)
            this.nodes.get(i).PropsApplyGlobal(propsBox);

        recorder.update();
    }

    abstract public void PropsLoadSpecific();
    abstract public void PropsApplySpecific();

    //// SETTINGS WINDOW FUNCTIONS ////

    public void SettingsLoadGlobal()
    {
        Text globalHeader = new Text("Global Settings:");
        globalHeader.setStyle("-fx-font-weight: bold;");
        globalHeader.setStyle("-fx-font-size: 16px;");
        EDAmameController.editorSettingsWindow.settingsBox.getChildren().add(globalHeader);
        EDAmameController.editorSettingsWindow.settingsBox.getChildren().add(new Separator());
    }

    public void SettingsApplyGlobal()
    {}

    abstract public void SettingsLoadSpecific();
    abstract public void SettingsApplySpecific();

    //// NETLIST FUNCTIONS ////

    public NetListExperimental<String> ToNetList()  // ASK!!!
    {
        LinkedList<EDALine> wires = new LinkedList<EDALine>();
        LinkedList<String> pinLabels = new LinkedList<String>();
        LinkedList<PairMutable> pinPos = new LinkedList<PairMutable>();
        LinkedList<String> pinSymbolIDs = new LinkedList<String>();

        // Populating the tables above...
        for (int i = 0; i < this.nodes.size(); i++)
        {
            EDANode node = this.nodes.get(i);

            // If we're reading a wire...
            if (node.getClass() == EDALine.class)
            {
                wires.add((EDALine)node);
            }
            // If we're reading a symbol...
            else if (node.getClass() == EDAGroup.class)
            {
                Group symbolNode = (Group)node.GetNode();

                for (int j = 0; j < symbolNode.getChildren().size(); j++)
                {
                    Node currSymbolChild = symbolNode.getChildren().get(j);

                    if (currSymbolChild.getClass() == Group.class)
                    {
                        Group pin = (Group)currSymbolChild;
                        String label = null;
                        PairMutable pos = null;

                        for (int k = 0; k < pin.getChildren().size(); k++)
                        {
                            Node currPinChild = pin.getChildren().get(k);

                            if (currPinChild.getClass() == Text.class)
                                label = ((Text)currPinChild).getText();
                            else if (currPinChild.getClass() == Circle.class)
                                pos = EDANode.GetPosInNodeParent(symbolNode, EDANode.GetPosInNodeParent(pin, new PairMutable(currPinChild.getTranslateX(), currPinChild.getTranslateY())));
                        }

                        if ((label == null) || (pos == null))
                            throw new java.lang.Error("ERROR: Attempting to load a pin without label or position during a net list conversion!");

                        pinLabels.add(label);
                        pinPos.add(pos);
                        pinSymbolIDs.add(node.id);

                        //this.Editor_RenderSystem.RenderSystem_TestShapeAdd(pos, 20.0, Color.BLUE, 0.5, false);
                    }
                }
            }
        }

        // Creating the wire connections list... (symbol id, pin label)
        LinkedList<PairMutable> wireConns = new LinkedList<PairMutable>();

        for (int i = 0; i < wires.size(); i++)
        {
            Line currWireNode = (Line)wires.get(i).GetNode();

            PairMutable nodeStart = null;
            PairMutable nodeEnd = null;
            PairMutable wirePosStart = EDANode.GetPosInNodeParent(currWireNode, new PairMutable(currWireNode.getStartX(), currWireNode.getStartY()));
            PairMutable wirePosEnd = EDANode.GetPosInNodeParent(currWireNode, new PairMutable(currWireNode.getEndX(), currWireNode.getEndY()));

            //this.Editor_RenderSystem.RenderSystem_TestShapeAdd(wirePosStart, 20.0, Color.RED, 0.5, false);
            //this.Editor_RenderSystem.RenderSystem_TestShapeAdd(wirePosEnd, 20.0, Color.RED, 0.5, false);

            // Checking whether the wire is connected to another wire...
            for (int j = 0; j < wires.size(); j++)
            {
                if (j == i)
                    continue;

                Line checkWireNode = (Line)wires.get(j).GetNode();

                PairMutable checkPosStart = EDANode.GetPosInNodeParent(checkWireNode, new PairMutable(checkWireNode.getStartX(), checkWireNode.getStartY()));
                PairMutable checkPosEnd = EDANode.GetPosInNodeParent(checkWireNode, new PairMutable(checkWireNode.getEndX(), checkWireNode.getEndY()));

                if (checkPosStart.EqualsDouble(wirePosStart) || checkPosEnd.EqualsDouble(wirePosStart))
                    nodeStart = new PairMutable(wires.get(j).id, null);
                if (checkPosStart.EqualsDouble(wirePosEnd) || checkPosEnd.EqualsDouble(wirePosEnd))
                    nodeEnd = new PairMutable(wires.get(j).id, null);
            }

            // Checking whether the wire is connected to a symbol... (overrides the line connections)
            for (int j = 0; j < pinPos.size(); j++)
            {
                PairMutable currPinPos = pinPos.get(j);

                if (currPinPos.EqualsDouble(wirePosStart))
                    nodeStart = new PairMutable(pinSymbolIDs.get(j), pinLabels.get(j));
                if (currPinPos.EqualsDouble(wirePosEnd))
                    nodeEnd = new PairMutable(pinSymbolIDs.get(j), pinLabels.get(j));
            }

            if ((nodeStart != null) || (nodeEnd != null))
                wireConns.add(new PairMutable(nodeStart, nodeEnd));
        }

        // Creating the initial wire connection net list...
        NetListExperimental<String> netList = new NetListExperimental<String>();

        for (int i = 0; i < wires.size(); i++)
        {
            PairMutable currWireConn = wireConns.get(i);

            if ((currWireConn.GetLeftPair().right != null) && (currWireConn.GetRightPair().right != null))
            {
                this.NetListWireConnAdd(netList,
                        currWireConn.GetLeftPair().GetLeftString(),
                        currWireConn.GetLeftPair().GetRightString(),
                        currWireConn.GetRightPair().GetLeftString(),
                        currWireConn.GetRightPair().GetRightString());
            }
            else if (currWireConn.GetLeftPair().right != null)
            {
                int connectedPinIdx = -1;
                int nextCheckWireIdx = NetListWireFind(wires, currWireConn.GetRightPair().GetLeftString());

                while (nextCheckWireIdx != -1)
                {
                    PairMutable nextCheckWireConn = wireConns.get(nextCheckWireIdx);

                    if (nextCheckWireConn.GetRightPair().right != null)
                    {
                        connectedPinIdx = NetListPinFind(pinSymbolIDs, nextCheckWireConn.GetRightPair().GetLeftString());

                        break;
                    }

                    nextCheckWireIdx = NetListWireFind(wires, nextCheckWireConn.GetRightPair().GetLeftString());
                }

                if (connectedPinIdx != -1)
                {
                    this.NetListWireConnAdd(netList,
                            currWireConn.GetLeftPair().GetLeftString(),
                            currWireConn.GetLeftPair().GetRightString(),
                            pinSymbolIDs.get(connectedPinIdx),
                            pinLabels.get(connectedPinIdx));
                }
            }
            else if (currWireConn.GetRightPair().right != null)
            {
                int connectedPinIdx = -1;
                int nextCheckWireIdx = NetListWireFind(wires, currWireConn.GetLeftPair().GetLeftString());

                while (nextCheckWireIdx != -1)
                {
                    PairMutable nextCheckWireConn = wireConns.get(nextCheckWireIdx);

                    if (nextCheckWireConn.GetLeftPair().right != null)
                    {
                        connectedPinIdx = NetListPinFind(pinSymbolIDs, nextCheckWireConn.GetLeftPair().GetLeftString());

                        break;
                    }

                    nextCheckWireIdx = NetListWireFind(wires, nextCheckWireConn.GetLeftPair().GetLeftString());
                }

                if (connectedPinIdx != -1)
                    this.NetListWireConnAdd(netList,
                            pinSymbolIDs.get(connectedPinIdx),
                            pinLabels.get(connectedPinIdx),
                            currWireConn.GetRightPair().GetLeftString(),
                            currWireConn.GetRightPair().GetRightString());
            }
        }

        //netList.ReplaceNodeIDsWithNames(this.Editor_RenderSystem);

        //System.out.println("wires: " + wires.toString());
        //System.out.println("pinLabels: " + pinLabels.toString());
        //System.out.println("pinPos: " + pinPos.toString());
        //System.out.println("pinSymbolIDs: " + pinSymbolIDs.toString());
        //System.out.println("wireConnIDs: " + wireConnIDs.toString());

        //for (int i = 0; i < netList.GetNodeNum(); i++)
        //    System.out.print(netList.Get(i).GetValue() + ", ");
        //System.out.println("::\n\n");

        return netList;
    }

    // REFACTOR THIS!!!
    public void NetListWireConnAdd(NetListExperimental<String> netList, String leftSymbolId, String leftPinName, String rightSymbolId, String rightPinName)
    {
        String leftSymbolName = this.nodes.get(this.NodeFindByID(leftSymbolId)).name;
        String rightSymbolName = this.nodes.get(this.NodeFindByID(rightSymbolId)).name;

        String currWireConnStartStr = leftSymbolName + " (" + leftPinName + ")";
        String currWireConnEndStr = rightSymbolName + " (" + rightPinName + ")";
        int currWireConnStartNodeIdx = netList.Find(currWireConnStartStr);
        int currWireConnEndNodeIdx = netList.Find(currWireConnEndStr);
        NetListExperimentalNode<String> currWireConnStartNode = null;
        NetListExperimentalNode<String> currWireConnEndNode = null;

        if (currWireConnStartNodeIdx == -1)
        {
            currWireConnStartNode = new NetListExperimentalNode<String>(currWireConnStartStr);
            netList.Append(currWireConnStartNode);
        }
        else
        {
            currWireConnStartNode = netList.Get(currWireConnStartNodeIdx);
        }
        if (currWireConnEndNodeIdx == -1)
        {
            currWireConnEndNode = new NetListExperimentalNode<String>(currWireConnEndStr);
            netList.Append(currWireConnEndNode);
        }
        else
        {
            currWireConnEndNode = netList.Get(currWireConnEndNodeIdx);
        }

        currWireConnStartNode.ConnAppend(currWireConnEndStr);
        currWireConnEndNode.ConnAppend(currWireConnStartStr);
    }

    // REFACTOR THIS!!!
    static public int NetListPinFind(LinkedList<String> pins, String id)
    {
        for (int i = 0; i < pins.size(); i++)
            if (pins.get(i).equals(id))
                return i;

        return -1;
    }

    // REFACTOR THIS!!!
    static public int NetListWireFind(LinkedList<EDALine> wires, String id)
    {
        for (int i = 0; i < wires.size(); i++)
            if (wires.get(i).id.equals(id))
                return i;

        return -1;
    }

    //// SUPPORT FUNCTIONS ////

    public static int FindPCBLayer(String layer)
    {
        for (int i = 0; i < EDAmameController.Editor_PCBLayers.length; i++)
            if (EDAmameController.Editor_PCBLayers[i].equals(layer))
                return i;

        return -1;
    }

    public static Color GetPCBLayerColor(String layer)
    {
        for (int i = 0; i < EDAmameController.Editor_PCBLayers.length; i++)
            if (EDAmameController.Editor_PCBLayers[i].equals(layer))
                return EDAmameController.Editor_PCBLayerColors[i];

        return null;
    }

    public boolean CanDropSomething()
    {
        return (this.shapesSelected == 0) && !this.shapesMoving && (this.selectionBox == null) && !this.shapesWereSelected && !this.wasSelectionBox;
    }

    public PairMutable PosSnapToGridPoint(PairMutable pos)
    {
        if (this.snapGridSpacing < 0)
            return pos;

        double scaledSpacing = this.snapGridSpacing * 10;

        return new PairMutable(Math.round(pos.GetLeftDouble() / scaledSpacing) * scaledSpacing,
                Math.round(pos.GetRightDouble() / scaledSpacing) * scaledSpacing);
    }

    public double PosSnapToGridPoint(double pos)
    {
        if (this.snapGridSpacing < 0)
            return pos;

        double scaledSpacing = this.snapGridSpacing * 10;

        return Math.round(pos / scaledSpacing) * scaledSpacing;
    }

    static public void LineDropPosCalculate(Line line, PairMutable posStart, PairMutable posEnd)
    {
        line.setStartX(0);
        line.setStartY(0);
        line.setEndX(posEnd.GetLeftDouble() - posStart.GetLeftDouble());
        line.setEndY(posEnd.GetRightDouble() - posStart.GetRightDouble());

        line.setTranslateX(posStart.GetLeftDouble());
        line.setTranslateY(posStart.GetRightDouble());
    }

    static public PairMutable LineEndPointsCalculate(Line line, boolean start)
    {
        if (start)
            return new PairMutable(line.getStartX() + line.getTranslateX(), line.getStartY() + line.getTranslateY());

        return new PairMutable(line.getEndX() + line.getTranslateX(), line.getEndY() + line.getTranslateY());
    }

    public static void TextFieldListenerInit(TextField textField)
    {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > EDAmameController.EditorSymbol_MaxChars)
                textField.setText(oldValue);
        });
    }

    public PairMutable MagneticSnapCheck(PairMutable pos)
    {
        PairMutable posSnapped = new PairMutable(pos);
        Double minDist = EDAmameController.Editor_SnapPointRadius;

        // Checking for magnetic snap...
        for (int i = 0; i < this.nodes.size(); i++)
        {
            PairMutable nodePosSnapped = this.nodes.get(i).MagneticSnapCheck(posSnapped, minDist);
            posSnapped = nodePosSnapped.GetLeftPair();
            minDist = nodePosSnapped.GetRightDouble();
        }

        return posSnapped;
    }

    /** Editor_Dissect a controller into its component for delivery to EDAmame<p>
     *
     * The design of Controller_Editors is intended to be done through FXML and SceneBuilder. SceneBuilder doesn't
     * support multiple scenes in a single FXML. So, all required components are boxed into a single VBOX.
     * Once an editor factory has loaded an FXML file which
     * @param scene The scene to Editor_Dissect for expected UI elements.
     * @throws InvalidClassException if the expected UI scene organization is not found as expected.
     */
    public void Dissect(Integer editorType, Scene scene) throws InvalidClassException
    {
        Node root = scene.getRoot();

        if (root == null)
            throw new InvalidClassException("root of scene is null");
        if (root.getClass() != VBox.class)
            throw new InvalidClassException("Expected VBox but found " + root.getClass());

        // Searching the scene for all the required elements
        Iterator<Node> nodeIterator = ((VBox)root).getChildren().iterator();
        String prefix = this.id;
        Pane foundPaneListener = null;
        Pane foundPaneHolder = null;
        Pane foundPaneHighlights = null;
        Pane foundPaneSelections = null;
        Pane foundPaneSnaps = null;
        Canvas foundCanvas = null;

        while (nodeIterator.hasNext())
        {
            Node node = nodeIterator.next();

            if (node.getClass() == ToolBar.class)
            {
                EDAmameController.logger.log(Level.INFO, "Dissecting a ToolBar in Editor \"" + id + "\"...\n");

                toolBar = (ToolBar) node;
                toolBar.setVisible(false); // toolbar starts invisible. Becomes visible on Tab selection.
                toolBar.setId(prefix + "_TOOLBAR");
            }
            else if (node.getClass() == MenuBar.class)
            {
                EDAmameController.logger.log(Level.INFO, "Dissecting a MenuBar in Editor \"" + id + "\"...\n");

                ObservableList<Menu> menus = ((MenuBar)node).getMenus();

                for (int i = 0; i < menus.size(); i++)
                {
                    Menu currMenu = menus.get(i);
                    String currMenuName = currMenu.getText();

                    if (!this.menus.containsKey(currMenuName))
                        this.menus.put(currMenuName, FXCollections.observableArrayList());

                    this.menus.get(currMenuName).addAll(currMenu.getItems());
                }
            }
            else if (node.getClass() == TabPane.class)
            {
                EDAmameController.logger.log(Level.INFO, "Dissecting a TabPane in Editor \"" + id + "\"...\n");

                TabPane paneNode = (TabPane)node;
                Iterator<Tab> tabIterator = paneNode.getTabs().iterator();

                while (tabIterator.hasNext())
                {
                    Tab item = tabIterator.next();

                    // Processing the editor Editor_Tab
                    if (item.getText().equals("EditorTab"))
                    {
                        // Searching the editor Editor_Tab for the stack pane and the canvas
                        HBox editorBox = (HBox)item.getContent();

                        // Searching for the stack pane...
                        for (int i = 0; i < editorBox.getChildren().size(); i++)
                        {
                            Node nextNodeA = editorBox.getChildren().get(i);

                            if (nextNodeA.getClass() == StackPane.class)
                            {
                                StackPane foundStackPane = (StackPane)nextNodeA;

                                // Searching for the listener pane...
                                for (int j = 0; j < foundStackPane.getChildren().size(); j++)
                                {
                                    Node nextNodeB = foundStackPane.getChildren().get(j);

                                    if (nextNodeB.getClass() == Pane.class)
                                    {
                                        foundPaneListener = (Pane)nextNodeB;

                                        // Searching for the holder pane and the crosshair...
                                        for (int k = 0; k < foundPaneListener.getChildren().size(); k++)
                                        {
                                            Node nextNodeC = foundPaneListener.getChildren().get(k);

                                            if (nextNodeC.getClass() == Pane.class)
                                            {
                                                foundPaneHolder = (Pane)nextNodeC;

                                                // Searching for the highlight pane, selections pane, snaps pane and the canvas...
                                                for (int l = 0; l < foundPaneHolder.getChildren().size(); l++)
                                                {
                                                    Node nextNodeD = foundPaneHolder.getChildren().get(l);

                                                    if (nextNodeD.getClass() == Pane.class)
                                                    {
                                                        if (foundPaneSelections == null)
                                                        {
                                                            foundPaneSelections = (Pane)nextNodeD;

                                                            EDAmameController.logger.log(Level.INFO, "Found selections pane of an editor with name \"" + this.name + "\".\n");
                                                        }
                                                        else if (foundPaneHighlights == null)
                                                        {
                                                            foundPaneHighlights = (Pane)nextNodeD;

                                                            EDAmameController.logger.log(Level.INFO, "Found highlights pane of an editor with name \"" + this.name + "\".\n");
                                                        }
                                                        else if (foundPaneSnaps == null)
                                                        {
                                                            foundPaneSnaps = (Pane)nextNodeD;

                                                            EDAmameController.logger.log(Level.INFO, "Found snaps pane of an editor with name \"" + this.name + "\".\n");
                                                        }
                                                        else
                                                        {
                                                            throw new java.lang.Error("ERROR: Encountered extra pane under the holder pane!");
                                                        }
                                                    }
                                                    else if (nextNodeD.getClass() == Canvas.class)
                                                    {
                                                        foundCanvas = (Canvas)nextNodeD;

                                                        EDAmameController.logger.log(Level.INFO, "Found canvas of an editor with name \"" + this.name + "\".\n");
                                                    }
                                                    else
                                                    {
                                                        throw new java.lang.Error("ERROR: Encountered unknown child under the holder pane!");
                                                    }
                                                }
                                            }
                                            else if (nextNodeC.getClass() != Circle.class)
                                            {
                                                throw new java.lang.Error("ERROR: Encountered unknown child under the listener pane!");
                                            }
                                        }

                                        break;
                                    }
                                }

                                break;
                            }
                        }

                        // Setting the pointers to the editor Editor_Tab
                        item.setText(EDAmameController.Editor_Names[editorType]);
                        tab = item;
                    }
                    else
                    {
                        tabs.add(item);
                    }
                }
            }
        }

        if (foundPaneListener == null)
            throw new InvalidClassException("Unable to locate listener pane for an editor with name \"" + this.name + "\"!");
        if (foundPaneHolder == null)
            throw new InvalidClassException("Unable to locate holder pane for an editor with name \"" + this.name + "\"!");
        if (foundPaneHighlights == null)
            throw new InvalidClassException("Unable to locate highlights pane for an editor with name \"" + this.name + "\"!");
        if (foundPaneSelections == null)
            throw new InvalidClassException("Unable to locate selections pane for an editor with name \"" + this.name + "\"!");
        if (foundPaneSnaps == null)
            throw new InvalidClassException("Unable to locate snaps pane for an editor with name \"" + this.name + "\"!");
        if (foundCanvas == null)
            throw new InvalidClassException("Unable to locate canvas for an editor with name \"" + this.name + "\"!");

        this.paneListener = foundPaneListener;
        this.paneHolder = foundPaneHolder;
        this.paneHighlights = foundPaneHighlights;
        this.paneSelections = foundPaneSelections;
        this.paneSnaps = foundPaneSnaps;
        this.canvas = foundCanvas;
        this.gc = this.canvas.getGraphicsContext2D();

        this.PaneHolderSetTranslate(new PairMutable(0.0, 0.0));
        this.paneHolder.prefWidthProperty().bind(this.canvas.widthProperty());
        this.paneHolder.prefHeightProperty().bind(this.canvas.heightProperty());
    }

    public static boolean CheckShapeTransparency(String strokeSize, Paint strokeColor, Color fillColor)
    {
        if (EDAmameController.IsStringNum(strokeSize))
        {
            double strokeDouble = Double.parseDouble(strokeSize);
            if ((strokeDouble == 0) || (!strokeColor.isOpaque()))
            {
                if (!((fillColor != null) && (fillColor != Color.TRANSPARENT) && (fillColor.hashCode() != 0x00000000)) || (fillColor.getOpacity() != 1.0))
                {
                    EDAmameController.SetStatusBar("Unable to drop shape because the entered fill and border is transparent!");
                    return false;
                }
            }
        }
        else
        {
            return false;
        }
        return true;
    }

    public static boolean CheckStringBorderSize(String checkString)
    {
        if (EDAmameController.IsStringNum(checkString))
        {
            double checkDouble = Double.parseDouble(checkString);
            if (((checkDouble >= EDAmameController.EditorSymbol_BorderMin) && (checkDouble <= EDAmameController.EditorSymbol_BorderMax)))
            {
                return true;
            }
            else
            {
                EDAmameController.SetStatusBar("Unable to drop shape because the entered border size field is outside the limits! (Length limits: " + EDAmameController.EditorSymbol_BorderMin + ", " + EDAmameController.EditorSymbol_BorderMax + ")");
                return false;
            }
        }
        else
        {
            EDAmameController.SetStatusBar("Unable to drop shape because the entered border size is non-numeric!");
            return false;
        }
    }

    public static boolean CheckStringCircleBounds(String checkString)
    {
        if (EDAmameController.IsStringNum(checkString))
        {
            double checkDouble = Double.parseDouble(checkString);
            if (((checkDouble >= EDAmameController.EditorSymbol_CircleRadiusMin) && (checkDouble <= EDAmameController.EditorSymbol_CircleRadiusMax)))
            {
                return true;
            }
            else
            {
                EDAmameController.SetStatusBar("Unable to drop circle because the entered radius field is outside the limits! (Radius limits: " + EDAmameController.EditorSymbol_CircleRadiusMin + ", " + EDAmameController.EditorSymbol_CircleRadiusMax + ")");
                return false;
            }
        }
        else
        {
            EDAmameController.SetStatusBar("Unable to drop circle because the entered radius field is non-numeric!");
            return false;
        }
    }

    public static boolean CheckStringRectBounds(String checkWidth, String checkHeight)
    {
        if ((EDAmameController.IsStringNum(checkWidth)) && (EDAmameController.IsStringNum(checkHeight)))
        {
            double doubleWidth = Double.parseDouble(checkWidth);
            double doubleHeight = Double.parseDouble(checkHeight);
            if (!((doubleWidth >= EDAmameController.EditorSymbol_RectWidthMin) && (doubleWidth <= EDAmameController.EditorSymbol_RectWidthMax)))
            {
                EDAmameController.SetStatusBar("Unable to drop rectangle because the entered width or height field is outside the limits! (Width limits: " + EDAmameController.EditorSymbol_RectWidthMin + ", " + EDAmameController.EditorSymbol_RectWidthMax + " | Height limits: " + EDAmameController.EditorSymbol_RectHeightMin + ", " + EDAmameController.EditorSymbol_RectHeightMax + ")");
                return false;
            }
            else if (!((doubleHeight >= EDAmameController.EditorSymbol_RectHeightMin) && (doubleHeight <= EDAmameController.EditorSymbol_RectHeightMax)))
            {
                EDAmameController.SetStatusBar("Unable to drop rectangle because the entered width or height field is outside the limits! (Width limits: " + EDAmameController.EditorSymbol_RectWidthMin + ", " + EDAmameController.EditorSymbol_RectWidthMax + " | Height limits: " + EDAmameController.EditorSymbol_RectHeightMin + ", " + EDAmameController.EditorSymbol_RectHeightMax + ")");
                return false;
            }
            else
            {
                return true;
            }
        }
        else
        {
            EDAmameController.SetStatusBar("Unable to drop rectangle because the entered width or height field is non-numeric!");
            return false;
        }
    }

    public static boolean CheckStringTriBounds(String checkString)
    {
        if (EDAmameController.IsStringNum(checkString))
        {
            double checkDouble = Double.parseDouble(checkString);
            if (((checkDouble >= EDAmameController.EditorSymbol_TriLenMin) && (checkDouble <= EDAmameController.EditorSymbol_TriLenMax)))
            {
                return true;
            }
            else
            {
                EDAmameController.SetStatusBar("Unable to drop triangle because the entered length field is outside the limits! (Length limits: " + EDAmameController.EditorSymbol_TriLenMin + ", " + EDAmameController.EditorSymbol_TriLenMax + ")");
                return false;
            }
        }
        else
        {
            EDAmameController.SetStatusBar("Unable to drop triangle because the entered length field is non-numeric!");
            return false;
        }
    }

    //// TESTING FUNCTIONS ////

    public void TestShapeAdd(PairMutable pos, Double radius, Color color, double opacity, boolean passive)
    {
        Circle testShape = new Circle(radius, color);

        if (passive)
            testShape.setId("testShapePassive");
        else
            testShape.setId("testShapeActive");

        testShape.setTranslateX(pos.GetLeftDouble());
        testShape.setTranslateY(pos.GetRightDouble());
        testShape.setOpacity(opacity);

        this.paneHolder.getChildren().add(testShape);
    }

    public void TestShapesClear()
    {
        for (int i = 0; i < this.paneHolder.getChildren().size(); i++)
        {
            Node currChild = this.paneHolder.getChildren().get(i);
            String currChildId = currChild.getId();

            if ((currChildId != null) && currChildId.equals("testShapePassive"))
            {
                this.paneHolder.getChildren().remove(currChild);
                i--;
            }
        }
    }

    //// MEMENTO IMPLEMENTATION ////

    /// ORIGINATOR IMPLEMENTATION ///
    @Override
    public Memento saveToMemento() {
        return new Memento() {
            private final LinkedList<EDANode> nodes = new LinkedList<>(Editor.this.NodesClone());

            @Override
            public Memento restore() {
                Editor.this.NodesClear();
                Editor.this.NodesAdd(nodes);
                return this;
            }
        };
    }

    @Override
    public boolean hasStateChanged() {
        int currentHash = StateHashUtil.generateStateHash(this);
        return currentHash != lastNodesHash;
    }

    @Override
    public void stateRecorded() {
        this.lastNodesHash = StateHashUtil.generateStateHash(this);
    }

    /// RECORDER IMPLEMENTATION ///
    @Override
    public void registerRecorder(Recorder recorder) {
        this.recorder = recorder;
    }

    /// HASH IMPLEMENTATION ///
    @Override
    public Collection<?> getState() {
        return this.nodes;
    }
}