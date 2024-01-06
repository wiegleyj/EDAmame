/*
 * Copyright (c) 2022. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.editor;
import com.cyte.edamame.EDAmameApplication;
import com.cyte.edamame.EDAmameController;
import com.cyte.edamame.memento.MementoExperimental;
import com.cyte.edamame.netlist.NetListExperimental;
import com.cyte.edamame.netlist.NetListExperimentalNode;
import com.cyte.edamame.node.EDANode;
import com.cyte.edamame.util.Utils;

import java.util.*;
import java.util.logging.Level;

import com.cyte.edamame.util.PairMutable;
import javafx.collections.*;
import javafx.geometry.Bounds;
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
public abstract class Editor
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
    public Shape crosshair;
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

    public MementoExperimental undoRedoSystem = null;

    //// MAIN FUNCTIONS ////

    public void Init(Integer type, String name)
    {
        this.type = type;
        this.name = name;

        this.theaterSize = EDAmameController.Editor_TheaterSize;
        this.backgroundColor = EDAmameController.Editor_BackgroundColors[type];
        this.gridPointColor = EDAmameController.Editor_GridPointColors[type];
        this.gridBoxColor = EDAmameController.Editor_GridBoxColors[type];
        this.maxShapes = EDAmameController.Editor_MaxShapes;
        this.nodes = new LinkedList<EDANode>();

        this.center = new PairMutable(0.0, 0.0);
        this.zoomLimits = EDAmameController.Editor_ZoomLimits;
        this.zoomFactor = EDAmameController.Editor_ZoomFactor;
        this.mouseDragFactor = EDAmameController.Editor_MouseDragFactor;
        this.mouseDragCheckTimeout = EDAmameController.Editor_MouseCheckTimeout;
        this.selectionBoxColor = EDAmameController.Editor_SelectionBoxColors[this.type];
        this.selectionBoxWidth = EDAmameController.Editor_SelectionBoxWidth;

        this.undoRedoSystem = new MementoExperimental(this);
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
        {
            EDANode renderNode = this.nodes.get(i);

            if (renderNode.passive)
                continue;

            renderNode.BoundsRefresh();
            renderNode.SnapPointsRefresh();
        }

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

            this.crosshair.setTranslateX(this.paneListener.getWidth() / 2);
            this.crosshair.setTranslateY(this.paneListener.getHeight() / 2);
        }

        //for (int i = 0; i < this.Editor_RenderSystem.RenderSystem_Nodes.size(); i++)
        //    System.out.println(this.Editor_RenderSystem.RenderSystem_Nodes.get(i).RenderNode_Node.getBoundsInParent().toString());

        //System.out.println(this.Editor_RenderSystem.RenderSystem_Nodes.size());

        //System.out.println(this.Editor_RenderSystem.RenderSystem_Nodes.size());
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

    //// RENDERING FUNCTIONS ////

    public void NodesDeselectAll()
    {
        for (int i = 0; i < this.nodes.size(); i++)
        {
            EDANode renderNode = this.nodes.get(i);

            if (!renderNode.selected)
            {
                if (renderNode.highlightedMouse || renderNode.highlightedBox)
                {
                    renderNode.selected = true;
                    //shape.RenderShape_ShapeSelectedRefresh();
                    //this.Editor_RenderSystem.RenderSystem_PaneSelections.getChildren().add(renderNode.RenderNode_ShapeSelected);
                    renderNode.shapeSelected.setVisible(true);
                    this.shapesSelected++;
                }
            }
            else
            {
                if ((!renderNode.highlightedMouse && !renderNode.highlightedBox) && !EDAmameController.IsKeyPressed(KeyCode.SHIFT))
                {
                    renderNode.selected = false;
                    //this.Editor_RenderSystem.RenderSystem_NodeSelectionsRemove(renderNode);
                    renderNode.shapeSelected.setVisible(false);
                    this.shapesSelected--;
                }
            }

            if (renderNode.highlightedBox && !renderNode.highlightedMouse)
                //this.Editor_RenderSystem.RenderSystem_NodeHighlightsRemove(renderNode);
                renderNode.shapeHighlighted.setVisible(false);

            renderNode.mousePressPos = null;
        }

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
        {
            EDANode renderNode = this.nodes.get(i);

            for (int j = 0; j < renderNode.manualSnapPoints.size(); j++)
            {
                Shape snapPoint = renderNode.manualSnapPoints.get(j);
                PairMutable snapPos = new PairMutable(snapPoint.getTranslateX(), snapPoint.getTranslateY());

                Double dist = Utils.GetDist(posMouse, snapPos);

                if (dist <= EDAmameController.Editor_SnapPointRadius)
                {
                    snapPoint.setVisible(true);
                }
                else
                {
                    snapPoint.setVisible(false);
                }
            }
        }
    }

    static public void LineDropPosCalculate(Line line, PairMutable posStart, PairMutable posEnd)
    {
        PairMutable posAvg = new PairMutable((posStart.GetLeftDouble() + posEnd.GetLeftDouble()) / 2, (posStart.GetRightDouble() + posEnd.GetRightDouble()) / 2);

        line.setStartX(posStart.GetLeftDouble() - posAvg.GetLeftDouble());
        line.setStartY(posStart.GetRightDouble() - posAvg.GetRightDouble());
        line.setEndX(posEnd.GetLeftDouble() - posAvg.GetLeftDouble());
        line.setEndY(posEnd.GetRightDouble() - posAvg.GetRightDouble());

        line.setTranslateX(posAvg.GetLeftDouble());
        line.setTranslateY(posAvg.GetRightDouble());
    }

    static public PairMutable LineEndPointsCalculate(Line line, boolean start)
    {
        if (start)
            return new PairMutable(line.getStartX() + line.getTranslateX(), line.getStartY() + line.getTranslateY());

        return new PairMutable(line.getEndX() + line.getTranslateX(), line.getEndY() + line.getTranslateY());
    }

    public void LinePreviewUpdate(PairMutable dropPos)
    {
        this.linePreview.setEndX(dropPos.GetLeftDouble());
        this.linePreview.setEndY(dropPos.GetRightDouble());
    }

    public void LinePreviewRemove()
    {
        this.NodeRemove("linePreview");
        this.linePreview = null;
    }

    public void NodeHighlightsCheck(PairMutable posEvent)
    {
        PairMutable posMouse = this.PanePosListenerToHolder(new PairMutable(posEvent.GetLeftDouble(), posEvent.GetRightDouble()));

        for (int i = 0; i < this.nodes.size(); i++)
        {
            EDANode renderNode = this.nodes.get(i);

            if (renderNode.passive)
                continue;

            //renderNode.RenderNode_ShapeHighlightedRefresh();
            boolean onShape = renderNode.PosOnNode(posMouse);

            if (!EDAmameController.IsKeyPressed(KeyCode.CONTROL))
            {
                // Checking whether we are highlighting by cursor...
                if (onShape)
                {
                    if (EDAmameController.IsKeyPressed(KeyCode.Q))
                    {
                        if ((this.shapesHighlighted > 1) && renderNode.highlightedMouse)
                            renderNode.highlightedMouse = false;
                        else if ((this.shapesHighlighted == 0) && !renderNode.highlightedMouse)
                            renderNode.highlightedMouse = true;
                    }
                    else
                    {
                        if (!renderNode.highlightedMouse)
                            renderNode.highlightedMouse = true;
                    }
                }
                else
                {
                    if (renderNode.highlightedMouse)
                        renderNode.highlightedMouse = false;
                }

                // Checking whether we are highlighting by selection box...
                if (this.selectionBox != null)
                {
                    Bounds shapeBounds = renderNode.node.getBoundsInParent();
                    PairMutable selectionBoxL = this.PanePosListenerToHolder(new PairMutable(this.selectionBox.getTranslateX(), this.selectionBox.getTranslateY()));
                    PairMutable selectionBoxH = this.PanePosListenerToHolder(new PairMutable(this.selectionBox.getTranslateX() + this.selectionBox.getWidth(), this.selectionBox.getTranslateY() + this.selectionBox.getHeight()));

                /*Circle testShapeL = new Circle(5, Color.RED);
                testShapeL.setId("TESTMARKER");
                testShapeL.setTranslateX(shapeBounds.getMinX());
                testShapeL.setTranslateY(shapeBounds.getMinY());
                this.paneHolder.getChildren().add(testShapeL);

                Circle testShapeH = new Circle(5, Color.RED);
                testShapeH.setId("TESTMARKER");
                testShapeH.setTranslateX(shapeBounds.getMaxX());
                testShapeH.setTranslateY(shapeBounds.getMaxY());
                this.paneHolder.getChildren().add(testShapeH);

                Circle testShapeBL = new Circle(5, Color.BLUE);
                testShapeBL.setId("TESTMARKER");
                PairMutable boxL = this.RenderSystem_PanePosListenerToHolder(new PairMutable(this.selectionBox.getTranslateX(), this.selectionBox.getTranslateY()));
                testShapeBL.setTranslateX(boxL.GetLeftDouble());
                testShapeBL.setTranslateY(boxL.GetRightDouble());
                this.paneHolder.getChildren().add(testShapeBL);

                Circle testShapeBH = new Circle(5, Color.BLUE);
                testShapeBH.setId("TESTMARKER");
                PairMutable boxH = this.RenderSystem_PanePosListenerToHolder(new PairMutable(this.selectionBox.getTranslateX() + this.selectionBox.getWidth(), this.selectionBox.getTranslateY() + this.selectionBox.getHeight()));
                testShapeBH.setTranslateX(boxH.GetLeftDouble());
                testShapeBH.setTranslateY(boxH.GetRightDouble());
                this.paneHolder.getChildren().add(testShapeBH);*/

                    if ((selectionBoxL.GetLeftDouble() < shapeBounds.getMaxX()) &&
                            (selectionBoxH.GetLeftDouble() > shapeBounds.getMinX()) &&
                            (selectionBoxL.GetRightDouble() < shapeBounds.getMaxY()) &&
                            (selectionBoxH.GetRightDouble() > shapeBounds.getMinY()))
                    {
                        if (!renderNode.highlightedBox)
                            renderNode.highlightedBox = true;
                    }
                    else
                    {
                        if (renderNode.highlightedBox)
                            renderNode.highlightedBox = false;
                    }
                }
                else if (renderNode.highlightedBox)
                {
                    renderNode.highlightedBox = false;
                }
            }
            else
            {
                renderNode.highlightedMouse = false;
                renderNode.highlightedBox = false;
            }

            // Adjusting highlights accordingly...
            if ((renderNode.highlightedMouse || renderNode.highlightedBox) && !renderNode.highlighted)
            {
                //shape.RenderShape_ShapeHighlightedRefresh();
                //this.Editor_RenderSystem.RenderSystem_PaneHighlights.getChildren().add(renderNode.RenderNode_ShapeHighlighted);
                renderNode.shapeHighlighted.setVisible(true);
                renderNode.highlighted = true;
                this.shapesHighlighted++;
            }
            else if ((!renderNode.highlightedMouse && !renderNode.highlightedBox) && renderNode.highlighted)
            {
                //this.Editor_RenderSystem.RenderSystem_NodeHighlightsRemove(renderNode);
                renderNode.shapeHighlighted.setVisible(false);
                renderNode.highlighted = false;
                this.shapesHighlighted--;
            }
        }
    }

    public void MouseDragUpdate(PairMutable posMouse)
    {
        this.mouseDragDiffPos = new PairMutable((posMouse.GetLeftDouble() - this.mouseDragFirstPos.GetLeftDouble()) * this.mouseDragFactor / this.zoom,
                                                       (posMouse.GetRightDouble() - this.mouseDragFirstPos.GetRightDouble()) * this.mouseDragFactor / this.zoom);
    }

    public void MouseDragReset(PairMutable posMouse)
    {
        this.mouseDragFirstPos = new PairMutable(posMouse);
        this.mouseDragFirstCenter = new PairMutable(this.center.GetLeftDouble(),
                                                           this.center.GetRightDouble());
        this.mouseDragPaneFirstPos = new PairMutable(this.PaneHolderGetTranslate());

        if (this.shapesSelected > 0)
        {
            for (int i = 0; i < this.nodes.size(); i++)
            {
                EDANode renderNode = this.nodes.get(i);

                if (!renderNode.selected)
                    continue;

                renderNode.mousePressPos = new PairMutable(renderNode.node.getTranslateX(),
                                                                      renderNode.node.getTranslateY());
            }
        }
    }

    public void CanvasRenderGrid()
    {
        // Clearing the canvas
        this.CanvasClear();

        // Drawing the points
        gc.setFill(this.gridPointColor);
        gc.setGlobalAlpha(1.0);
        Double width = 3.0;

        Double posX = -2500.0;
        Double posY = -2500.0;

        for (int i = 0; i < 70; i++)
        {
            for (int j = 0; j < 70; j++)
            {
                gc.fillOval(posX - (width / 2), posY - (width / 2), width, width);

                posX += 100.0;
            }

            posX = -2500.0;
            posY += 100.0;
        }

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

    public PairMutable PaneHolderGetRealCenter()
    {
        return new PairMutable(-this.center.GetLeftDouble() + this.paneHolder.getWidth() / 2,
                -this.center.GetRightDouble() + this.paneHolder.getHeight() / 2);
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

    public int NodeFind(String id)
    {
        for (int i = 0; i < this.nodes.size(); i++)
            if (this.nodes.get(i).id.equals(id))
                return i;

        return -1;
    }

    public void NodesAdd(LinkedList<EDANode> renderNodes)
    {
        for (int i = 0; i < renderNodes.size(); i++)
            this.NodeAdd(renderNodes.get(i));
    }

    public void NodeAdd(EDANode renderNode)
    {
        if (this.nodes.size() >= this.maxShapes)
            throw new java.lang.Error("ERROR: Exceeded render system maximum render nodes limit!");

        this.nodes.add(renderNode);
        this.paneHolder.getChildren().add(1, renderNode.node);

        if (!renderNode.passive)
        {
            this.paneHighlights.getChildren().add(renderNode.shapeHighlighted);
            this.paneSelections.getChildren().add(renderNode.shapeSelected);
        }

        for (int i = 0; i < renderNode.manualSnapPoints.size(); i++)
            this.paneSnaps.getChildren().add(renderNode.manualSnapPoints.get(i));
    }

    public void NodesClear()
    {
        while (!this.nodes.isEmpty())
            NodeRemove(this.nodes.get(0).name);
    }

    public EDANode NodeRemove(String name)
    {
        for (int i = 0; i < this.nodes.size(); i++)
        {
            EDANode renderNode = this.nodes.get(i);

            if (renderNode.name.equals(name))
            {
                this.nodes.remove(renderNode);
                this.paneHolder.getChildren().remove(renderNode.node);

                if (!renderNode.passive)
                {
                    this.paneHighlights.getChildren().remove(renderNode.shapeHighlighted);
                    this.paneSelections.getChildren().remove(renderNode.shapeSelected);
                }

                renderNode.highlighted = false;
                renderNode.selected = false;

                for (int j = 0; j < renderNode.manualSnapPoints.size(); j++)
                    this.paneSnaps.getChildren().remove(renderNode.manualSnapPoints.get(j));

                return renderNode;
            }
        }

        return null;
    }

    public LinkedList<EDANode> NodesClone()
    {
        LinkedList<EDANode> clonedNodes = new LinkedList<EDANode>();

        for (int i = 0; i < this.nodes.size(); i++)
            clonedNodes.add(this.nodes.get(i).Clone());

        return clonedNodes;
    }

    //// CALLBACK FUNCTIONS ////

    public void OnDragOverGlobal(DragEvent event)
    {
        PairMutable eventPos = new PairMutable(event.getX(), event.getY());

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

        // Handling shape highlights...
        this.NodeHighlightsCheck(eventPos);

        // Handling node snap shapes...
        this.NodeSnapPointsCheck(eventPos);

        // Handling line drawing preview...
        if (this.linePreview != null)
            this.LinePreviewUpdate(dropPos);
    }

    public void OnMousePressedGlobal(MouseEvent event)
    {
        if (this.pressedLMB)
        {
            this.undoRedoSystem.NodeHistoryUpdate();
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

        // Handling shape highlights...
        this.NodeHighlightsCheck(eventPos);

        // Handling node snap shapes...
        this.NodeSnapPointsCheck(eventPos);

        // Handling line drawing preview...
        if (this.linePreview != null)
            this.LinePreviewUpdate(dropPos);

        if (this.pressedLMB)
        {
            // Handling moving of the shapes (only if we have some shapes selected, we're not holding the box selection key and we're not drawing any lines)
            if ((this.shapesSelected > 0) &&
                !EDAmameController.IsKeyPressed(KeyCode.SHIFT) &&
                (this.linePreview == null))
            {
                for (int i = 0; i < this.nodes.size(); i++)
                {
                    EDANode renderNode = this.nodes.get(i);

                    if (!renderNode.selected)
                        continue;

                    PairMutable posPressReal = this.PaneHolderGetRealPos(new PairMutable(renderNode.mousePressPos.GetLeftDouble(),
                                                                                                                          renderNode.mousePressPos.GetRightDouble()));
                    PairMutable posOffset = new PairMutable(0.0, 0.0);

                    /*if ((posPressReal.GetLeftDouble() + this.Editor_MouseDragDiffPos.GetLeftDouble()) < -EDAmameController.Editor_TheaterSize.GetLeftDouble() / 2)
                        edgeOffset.left = -(posPressReal.GetLeftDouble() + this.Editor_MouseDragDiffPos.GetLeftDouble() + EDAmameController.Editor_TheaterSize.GetLeftDouble() / 2);
                    if ((posPressReal.GetLeftDouble() + this.Editor_MouseDragDiffPos.GetLeftDouble()) > EDAmameController.Editor_TheaterSize.GetLeftDouble() / 2)
                        edgeOffset.left = -(posPressReal.GetLeftDouble() + this.Editor_MouseDragDiffPos.GetLeftDouble() - EDAmameController.Editor_TheaterSize.GetLeftDouble() / 2);
                    if ((posPressReal.GetRightDouble() + this.Editor_MouseDragDiffPos.GetRightDouble()) < -EDAmameController.Editor_TheaterSize.GetRightDouble() / 2)
                        edgeOffset.right = -(posPressReal.GetRightDouble() + this.Editor_MouseDragDiffPos.GetRightDouble() + EDAmameController.Editor_TheaterSize.GetRightDouble() / 2);
                    if ((posPressReal.GetRightDouble() + this.Editor_MouseDragDiffPos.GetRightDouble()) > EDAmameController.Editor_TheaterSize.GetRightDouble() / 2)
                        edgeOffset.right = -(posPressReal.GetRightDouble() + this.Editor_MouseDragDiffPos.GetRightDouble() - EDAmameController.Editor_TheaterSize.GetRightDouble() / 2);*/

                    // Handling straight-only dragging...
                    if (EDAmameController.IsKeyPressed(KeyCode.CONTROL))
                    {
                        if (Math.abs(this.mouseDragDiffPos.GetLeftDouble()) < Math.abs(this.mouseDragDiffPos.GetRightDouble()))
                            posOffset.left = -this.mouseDragDiffPos.GetLeftDouble();
                        else
                            posOffset.right = -this.mouseDragDiffPos.GetRightDouble();
                    }

                    renderNode.node.setTranslateX(renderNode.mousePressPos.GetLeftDouble() + this.mouseDragDiffPos.GetLeftDouble() + posOffset.GetLeftDouble());
                    renderNode.node.setTranslateY(renderNode.mousePressPos.GetRightDouble() + this.mouseDragDiffPos.GetRightDouble() + posOffset.GetRightDouble());
                }

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

                /*if ((this.Editor_RenderSystem.RenderSystem_Center.GetLeftDouble() <= -this.Editor_RenderSystem.RenderSystem_TheaterSize.GetLeftDouble() / 2) && (this.Editor_MouseDragDiffPos.GetLeftDouble() < 0))
                {
                    this.Editor_MouseDragDiffPos.left = this.Editor_MouseDragDiffPos.GetLeftDouble() + (-this.Editor_RenderSystem.RenderSystem_TheaterSize.GetLeftDouble() / 2 - (this.Editor_MouseDragFirstCenter.GetLeftDouble() + this.Editor_MouseDragDiffPos.GetLeftDouble()));
                    this.Editor_MouseDragReachedEdge = true;
                }
                if ((this.Editor_RenderSystem.RenderSystem_Center.GetLeftDouble() >= this.Editor_RenderSystem.RenderSystem_TheaterSize.GetLeftDouble() / 2) && (this.Editor_MouseDragDiffPos.GetLeftDouble() > 0))
                {
                    this.Editor_MouseDragDiffPos.left = this.Editor_MouseDragDiffPos.GetLeftDouble() + (this.Editor_RenderSystem.RenderSystem_TheaterSize.GetLeftDouble() / 2 - (this.Editor_MouseDragFirstCenter.GetLeftDouble() + this.Editor_MouseDragDiffPos.GetLeftDouble()));
                    this.Editor_MouseDragReachedEdge = true;
                }
                if ((this.Editor_RenderSystem.RenderSystem_Center.GetRightDouble() <= -this.Editor_RenderSystem.RenderSystem_TheaterSize.GetRightDouble() / 2) && (this.Editor_MouseDragDiffPos.GetRightDouble() < 0))
                {
                    this.Editor_MouseDragDiffPos.right = this.Editor_MouseDragDiffPos.GetRightDouble() + (-this.Editor_RenderSystem.RenderSystem_TheaterSize.GetRightDouble() / 2 - (this.Editor_MouseDragFirstCenter.GetRightDouble() + this.Editor_MouseDragDiffPos.GetRightDouble()));
                    this.Editor_MouseDragReachedEdge = true;
                }
                if ((this.Editor_RenderSystem.RenderSystem_Center.GetRightDouble() >= this.Editor_RenderSystem.RenderSystem_TheaterSize.GetRightDouble() / 2) && (this.Editor_MouseDragDiffPos.GetRightDouble() > 0))
                {
                    this.Editor_MouseDragDiffPos.right = this.Editor_MouseDragDiffPos.GetRightDouble() + (this.Editor_RenderSystem.RenderSystem_TheaterSize.GetRightDouble() / 2 - (this.Editor_MouseDragFirstCenter.GetRightDouble() + this.Editor_MouseDragDiffPos.GetRightDouble()));
                    this.Editor_MouseDragReachedEdge = true;
                }*/

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
            {
                EDANode renderNode = this.nodes.get(i);
                String nodeId = renderNode.node.getId();

                if (!renderNode.selected ||
                    (renderNode.node.getClass() == Line.class) ||
                    ((nodeId != null) && nodeId.contains("PIN_")))
                    continue;

                double angle = 10;

                if (event.getDeltaY() < 0)
                    angle = -10;

                renderNode.node.setRotate(renderNode.node.getRotate() + angle);

                //shape.RenderShape_ShapeSelectedRefresh();
                //shape.RenderShape_ShapeHighlightedRefresh();
            }
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

        // Handling shape highlights...
        this.NodeHighlightsCheck(eventPos);

        // Handling node snap shapes...
        this.NodeSnapPointsCheck(eventPos);

        // Handling line drawing preview...
        if (this.linePreview != null)
            this.LinePreviewUpdate(dropPos);
    }

    public void OnKeyPressedGlobal(KeyEvent event)
    {
        // Handling element properties window...
        if (EDAmameController.IsKeyPressed(KeyCode.E) && (EDAmameController.editorPropertiesWindow == null))
        {
            // Attempting to create the properties window...
            EditorProps propsWindow = EditorProps.Create();

            if ((propsWindow != null))
            {
                propsWindow.stage.setOnHidden(e -> {
                    EDAmameController.editorPropertiesWindow = null;
                });
                propsWindow.editor = this;
                propsWindow.stage.show();

                EDAmameController.editorPropertiesWindow = propsWindow;
            }
        }

        // Handling shape deletion...
        if (EDAmameController.IsKeyPressed(KeyCode.BACK_SPACE) || EDAmameController.IsKeyPressed(KeyCode.DELETE))
        {
            if (this.shapesSelected > 0)
            {
                for (int i = 0; i < this.nodes.size(); i++)
                {
                    EDANode renderNode = this.nodes.get(i);

                    if (!renderNode.selected)
                        continue;

                    if (renderNode.highlighted)
                    {
                        //this.Editor_RenderSystem.RenderSystem_NodeHighlightsRemove(renderNode);
                        renderNode.shapeHighlighted.setVisible(false);
                        this.shapesHighlighted--;
                    }

                    //this.Editor_RenderSystem.RenderSystem_NodeSelectionsRemove(renderNode);
                    renderNode.shapeSelected.setVisible(false);
                    this.shapesSelected--;

                    this.paneHolder.getChildren().remove(renderNode.node);
                    this.nodes.remove(renderNode);

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
            this.undoRedoSystem.NodesUndo();

        // Handling element undo...
        if (EDAmameController.IsKeyPressed(KeyCode.CONTROL) && EDAmameController.IsKeyPressed(KeyCode.Y))
            this.undoRedoSystem.NodesRedo();
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
        LinkedList<Color> colors = new LinkedList<Color>();

        for (int i = 0; i < this.nodes.size(); i++)
        {
            EDANode renderNode = this.nodes.get(i);

            if (!renderNode.selected)
                continue;

            names.add(renderNode.name);
            posX.add(renderNode.node.getTranslateX() - this.paneHolder.getWidth() / 2);
            posY.add(renderNode.node.getTranslateY() - this.paneHolder.getHeight() / 2);

            if ((renderNode.node.getClass() != Line.class) &&
                !renderNode.isPin)
                rots.add(renderNode.node.getRotate());

            if (renderNode.node.getClass() == Line.class)
            {
                colors.add((Color)((Line) renderNode.node).getStroke());
            }
            else if (renderNode.isPin)
            {
                Group group = (Group)renderNode.node;

                if (group.getChildren().size() != 2)
                    throw new java.lang.Error("ERROR: Attempting to load pin into global properties editor without 2 children!");

                colors.add((Color)((Shape)group.getChildren().get(0)).getStroke());
            }
            else if (renderNode.node.getClass() != Group.class)
            {
                colors.add((Color)((Shape)renderNode.node).getFill());
            }
        }

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

        // Creating color box...
        if (!colors.isEmpty())
        {
            HBox colorHBox = new HBox(10);
            colorHBox.setId("colorBox");
            colorHBox.getChildren().add(new Label("Colors: "));
            ColorPicker colorPicker = new ColorPicker();
            colorPicker.setId("color");
            colorHBox.getChildren().add(colorPicker);

            if (EDAmameController.IsListAllEqual(colors))
                colorPicker.setValue(colors.get(0));
            else
                colorPicker.setValue(null);

            EDAmameController.editorPropertiesWindow.propsBox.getChildren().add(colorHBox);
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
        {
            EDANode renderNode = this.nodes.get(i);

            if (!renderNode.selected)
                continue;

            // Applying name...
            {
                Integer nameBoxIdx = EDAmameController.FindNodeById(propsBox.getChildren(), "nameBox");

                if (nameBoxIdx != -1)
                {
                    HBox nameBox = (HBox)propsBox.getChildren().get(nameBoxIdx);
                    TextField nameText = (TextField)EDAmameController.GetNodeById(nameBox.getChildren(), "name");

                    if (nameText == null)
                        throw new java.lang.Error("ERROR: Unable to find \"name\" node in global properties window \"nameBox\" entry!");

                    String nameStr = nameText.getText();

                    if (!nameStr.isEmpty())
                    {
                        renderNode.name = nameStr;
                    }
                    else if (!nameStr.equals("<mixed>"))
                    {
                        EDAmameController.SetStatusBar("Unable to apply element name because the entered field is empty!");
                    }
                }
            }

            // Applying position...
            {
                Integer posBoxIdx = EDAmameController.FindNodeById(propsBox.getChildren(), "posBox");

                if (posBoxIdx != -1)
                {
                    HBox posBox = (HBox)propsBox.getChildren().get(posBoxIdx);
                    TextField posXText = (TextField)EDAmameController.GetNodeById(posBox.getChildren(), "posX");
                    TextField posYText = (TextField)EDAmameController.GetNodeById(posBox.getChildren(), "posY");

                    if (posXText == null)
                        throw new java.lang.Error("ERROR: Unable to find \"posX\" node in global properties window \"posBox\" entry!");
                    if (posYText == null)
                        throw new java.lang.Error("ERROR: Unable to find \"posY\" node in global properties window \"posBox\" entry!");

                    String posXStr = posXText.getText();
                    String posYStr = posYText.getText();

                    if (EDAmameController.IsStringNum(posXStr))
                    {
                        Double newPosX = Double.parseDouble(posXStr);

                        /*if ((newPosX >= -EDAmameController.Editor_TheaterSize.GetLeftDouble() / 2) &&
                            (newPosX <= EDAmameController.Editor_TheaterSize.GetLeftDouble() / 2))
                        {*/
                            renderNode.node.setTranslateX(newPosX + this.paneHolder.getWidth() / 2);
                        /*}
                        else
                        {
                            EDAmameController.Controller_SetStatusBar("Unable to apply element X position because the entered field is outside the theater limits!");
                        }*/
                    }
                    else if (!posXStr.equals("<mixed>"))
                    {
                        EDAmameController.SetStatusBar("Unable to apply element X position because the entered field is non-numeric!");
                    }

                    if (EDAmameController.IsStringNum(posYStr))
                    {
                        Double newPosY = Double.parseDouble(posYStr);

                        /*if ((newPosY >= -EDAmameController.Editor_TheaterSize.GetRightDouble() / 2) &&
                            (newPosY <= EDAmameController.Editor_TheaterSize.GetRightDouble() / 2))
                        {*/
                            renderNode.node.setTranslateY(newPosY + this.paneHolder.getHeight() / 2);
                        /*}
                        else
                        {
                            EDAmameController.Controller_SetStatusBar("Unable to apply element Y position because the entered field is outside the theater limits!");
                        }*/
                    }
                    else if (!posYStr.equals("<mixed>"))
                    {
                        EDAmameController.SetStatusBar("Unable to apply element Y position because the entered field is non-numeric!");
                    }
                }

            }

            // Applying rotation...
            if ((renderNode.node.getClass() != Line.class) &&
                !renderNode.isPin)
            {
                Integer rotBoxIdx = EDAmameController.FindNodeById(propsBox.getChildren(), "rotBox");

                if (rotBoxIdx != -1)
                {
                    HBox rotBox = (HBox)propsBox.getChildren().get(rotBoxIdx);
                    TextField rotText = (TextField)EDAmameController.GetNodeById(rotBox.getChildren(), "rot");

                    if (rotText == null)
                        throw new java.lang.Error("ERROR: Unable to find \"rot\" node in global properties window \"rotBox\" entry!");

                    String rotStr = rotText.getText();

                    if (EDAmameController.IsStringNum(rotStr))
                    {
                        renderNode.node.setRotate(Double.parseDouble(rotStr));
                    }
                    else if (!rotStr.equals("<mixed>"))
                    {
                        EDAmameController.SetStatusBar("Unable to apply element rotation because the entered field is non-numeric!");
                    }
                }
            }

            // Applying color...
            {
                Integer colorBoxIdx = EDAmameController.FindNodeById(propsBox.getChildren(), "colorBox");

                if (colorBoxIdx != -1)
                {
                    HBox colorBox = (HBox) propsBox.getChildren().get(colorBoxIdx);
                    ColorPicker colorPicker = (ColorPicker) EDAmameController.GetNodeById(colorBox.getChildren(), "color");

                    if (colorPicker == null)
                        throw new java.lang.Error("ERROR: Unable to find \"color\" node in Symbol Editor properties window \"colorBox\" entry!");

                    Color color = colorPicker.getValue();

                    if ((color != null) && (color != Color.TRANSPARENT) && (color.hashCode() != 0x00000000))
                    {
                        if (renderNode.node.getClass() == Line.class)
                        {
                            ((Line)renderNode.node).setStroke(color);
                        }
                        else if (renderNode.isPin)
                        {
                            Group group = (Group)renderNode.node;

                            if (group.getChildren().size() != 2)
                                throw new java.lang.Error("ERROR: Attempting to load pin into global properties window without 2 children!");

                            ((Shape)group.getChildren().get(0)).setFill(color);
                            ((Shape)group.getChildren().get(1)).setFill(color);
                        }
                        else if (renderNode.node.getClass() != Group.class)
                        {
                            ((Shape)renderNode.node).setFill(color);
                        }
                    }
                    else
                    {
                        if (color != null)
                            EDAmameController.SetStatusBar("Unable to apply shape colors because the entered color is transparent!");
                    }
                }
            }
        }

        this.undoRedoSystem.NodeHistoryUpdate();
    }

    abstract public void PropsLoadSpecific();
    abstract public void PropsApplySpecific();

    //// SUPPORT FUNCTIONS ////

    public NetListExperimental<String> ToNetList()
    {
        LinkedList<EDANode> wires = new LinkedList<EDANode>();
        LinkedList<String> pinLabels = new LinkedList<String>();
        LinkedList<PairMutable> pinPos = new LinkedList<PairMutable>();
        LinkedList<String> pinSymbolIDs = new LinkedList<String>();

        // Populating the tables above...
        for (int i = 0; i < this.nodes.size(); i++)
        {
            EDANode renderNode = this.nodes.get(i);

            // If we're reading a wire...
            if (renderNode.node.getClass() == Line.class)
            {
                wires.add(renderNode);
            }
            // If we're reading a symbol...
            else if (renderNode.node.getClass() == Group.class)
            {
                Group symbol = (Group)renderNode.node;

                for (int j = 0; j < symbol.getChildren().size(); j++)
                {
                    Node currSymbolChild = symbol.getChildren().get(j);

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
                                pos = Utils.GetPosInNodeParent(symbol, Utils.GetPosInNodeParent(pin, new PairMutable(currPinChild.getTranslateX(), currPinChild.getTranslateY())));
                        }

                        if ((label == null) || (pos == null))
                            throw new java.lang.Error("ERROR: Attempting to load a pin without label or position during a net list conversion!");

                        pinLabels.add(label);
                        pinPos.add(pos);
                        pinSymbolIDs.add(renderNode.id);

                        //this.Editor_RenderSystem.RenderSystem_TestShapeAdd(pos, 20.0, Color.BLUE, 0.5, false);
                    }
                }
            }
        }

        // Creating the wire connections list... (symbol id, pin label)
        LinkedList<PairMutable> wireConns = new LinkedList<PairMutable>();

        for (int i = 0; i < wires.size(); i++)
        {
            Line currWire = (Line)wires.get(i).node;

            PairMutable nodeStart = null;
            PairMutable nodeEnd = null;
            PairMutable wirePosStart = Utils.GetPosInNodeParent(currWire, new PairMutable(currWire.getStartX(), currWire.getStartY()));
            PairMutable wirePosEnd = Utils.GetPosInNodeParent(currWire, new PairMutable(currWire.getEndX(), currWire.getEndY()));

            //this.Editor_RenderSystem.RenderSystem_TestShapeAdd(wirePosStart, 20.0, Color.RED, 0.5, false);
            //this.Editor_RenderSystem.RenderSystem_TestShapeAdd(wirePosEnd, 20.0, Color.RED, 0.5, false);

            // Checking whether the wire is connected to another wire...
            for (int j = 0; j < wires.size(); j++)
            {
                if (j == i)
                    continue;

                Line checkWire = (Line)wires.get(j).node;

                PairMutable checkPosStart = Utils.GetPosInNodeParent(checkWire, new PairMutable(checkWire.getStartX(), checkWire.getStartY()));
                PairMutable checkPosEnd = Utils.GetPosInNodeParent(checkWire, new PairMutable(checkWire.getEndX(), checkWire.getEndY()));

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
                {
                    this.NetListWireConnAdd(netList,
                                                   pinSymbolIDs.get(connectedPinIdx),
                                                   pinLabels.get(connectedPinIdx),
                                                   currWireConn.GetRightPair().GetLeftString(),
                                                   currWireConn.GetRightPair().GetRightString());
                }
            }
        }

        // Iterating through all the wires & checking their start & end connections...
        /*LinkedList<PairMutable> wireConnIDs = new LinkedList<PairMutable>();

        for (int i = 0; i < wires.size(); i++)
        {
            Line currWire = (Line)wires.get(i).RenderNode_Node;

            String nodeStartID = null;
            String nodeEndID = null;
            PairMutable wirePosStart = Utils.GetPosInNodeParent(currWire, new PairMutable(currWire.getStartX(), currWire.getStartY()));
            PairMutable wirePosEnd = Utils.GetPosInNodeParent(currWire, new PairMutable(currWire.getEndX(), currWire.getEndY()));

            this.Editor_RenderSystem.RenderSystem_TestShapeAdd(wirePosStart, 20.0, Color.RED, 0.5, false);
            this.Editor_RenderSystem.RenderSystem_TestShapeAdd(wirePosEnd, 20.0, Color.RED, 0.5, false);

            // Checking whether the wire is connected to another wire...
            for (int j = 0; j < wires.size(); j++)
            {
                if (j == i)
                    continue;

                Line checkWire = (Line)wires.get(j).RenderNode_Node;

                PairMutable checkPosStart = Utils.GetPosInNodeParent(checkWire, new PairMutable(checkWire.getStartX(), checkWire.getStartY()));
                PairMutable checkPosEnd = Utils.GetPosInNodeParent(checkWire, new PairMutable(checkWire.getEndX(), checkWire.getEndY()));

                if (checkPosStart.EqualsDouble(wirePosStart) || checkPosEnd.EqualsDouble(wirePosStart))
                    nodeStartID = wires.get(j).RenderNode_ID;
                if (checkPosStart.EqualsDouble(wirePosEnd) || checkPosEnd.EqualsDouble(wirePosEnd))
                    nodeEndID = wires.get(j).RenderNode_ID;
            }

            // Checking whether the wire is connected to a symbol... (overrides the line connections)
            for (int j = 0; j < pinPos.size(); j++)
            {
                PairMutable currPinPos = pinPos.get(j);

                if (currPinPos.EqualsDouble(wirePosStart))
                    nodeStartID = pinSymbolIDs.get(j);
                if (currPinPos.EqualsDouble(wirePosEnd))
                    nodeEndID = pinSymbolIDs.get(j);
            }

            wireConnIDs.add(new PairMutable(nodeStartID, nodeEndID));
        }

        // Creating the symbol connection net list...
        for (int i = 0; i < wires.size(); i++)
        {
            //System.out.println("Wire " + i);
            //System.out.println("\t" + wireConnIDs.get(i).GetLeftString() + ", " + wireConnIDs.get(i).GetRightString());

            PairMutable currWireConnIDs = wireConnIDs.get(i);
            int nodeStartIdx = this.Editor_RenderSystem.RenderSystem_NodeFind(currWireConnIDs.GetLeftString());
            int nodeEndIdx = this.Editor_RenderSystem.RenderSystem_NodeFind(currWireConnIDs.GetRightString());

            if ((nodeStartIdx == -1) || (nodeEndIdx == -1))
                continue;
            if (nodeStartIdx == nodeEndIdx)
                continue;

            RenderNode nodeStart = this.Editor_RenderSystem.RenderSystem_Nodes.get(nodeStartIdx);
            RenderNode nodeEnd = this.Editor_RenderSystem.RenderSystem_Nodes.get(nodeEndIdx);

            NetListExperimentalNode<String> netListNode = new NetListExperimentalNode<String>(wires.get(i).RenderNode_ID);
            netListNode.ConnAppend(nodeStart.RenderNode_ID);
            netListNode.ConnAppend(nodeEnd.RenderNode_ID);

            netList.Append(netListNode);
        }

        // Filtering out all the wires in the created net list...
        int netListLen = netList.GetNodeNum();

        System.out.println("---- iter 0 ----");
        System.out.println(netList.ToString());
        System.out.println(this.Editor_NetListReplaceIDsWithNames(netList).ToString() + "\n");

        for (int i = 0; i < netListLen; i++)
        {
            PairMutable currWireConnIDs = wireConnIDs.get(i);
            int nodeStartIdx = this.Editor_RenderSystem.RenderSystem_NodeFind(currWireConnIDs.GetLeftString());
            int nodeEndIdx = this.Editor_RenderSystem.RenderSystem_NodeFind(currWireConnIDs.GetRightString());

            if ((nodeStartIdx == -1) || (nodeEndIdx == -1))
                throw new java.lang.Error("ERROR: Encountered a wire with at least one endpoint null while filtering wires out of an Editor net list!");
            if (nodeStartIdx == nodeEndIdx)
                throw new java.lang.Error("ERROR: Encountered a wire that connects to itself while filtering wires out of an Editor net list!");

            NetListExperimentalNode<String> nodeID = netList.Get(i);
            RenderNode renderNode = this.Editor_RenderSystem.RenderSystem_Nodes.get(this.Editor_RenderSystem.RenderSystem_NodeFind(nodeID.GetValue()));
            RenderNode renderNodeStart = this.Editor_RenderSystem.RenderSystem_Nodes.get(nodeStartIdx);
            RenderNode renderNodeEnd = this.Editor_RenderSystem.RenderSystem_Nodes.get(nodeEndIdx);

            if (renderNode.RenderNode_Node.getClass() == Line.class)
            {
                netList.Remove(netList.Find(nodeID.GetValue()));

                int netListNodeStartIdx = netList.Find(renderNodeStart.RenderNode_ID);
                int netListNodeEndIdx = netList.Find(renderNodeEnd.RenderNode_ID);
                NetListExperimentalNode<String> netListNodeStart = null;
                NetListExperimentalNode<String> netListNodeEnd = null;

                if (netListNodeStartIdx == -1)
                {
                    netList.Append(new NetListExperimentalNode<String>(renderNodeStart.RenderNode_ID));
                    netListNodeStart = netList.Get(netList.GetNodeNum() - 1);

                    netListNodeStart.ConnAppend(null);
                    netListNodeStart.ConnAppend(renderNodeEnd.RenderNode_ID);
                }
                else
                {
                    //i--;
                    netListNodeStart = netList.Get(netListNodeStartIdx);
                    //netListNodeStart.ConnClear();

                    netListNodeStart.ConnSet(1, renderNodeEnd.RenderNode_ID);
                }

                if (netListNodeEndIdx == -1)
                {
                    netList.Append(new NetListExperimentalNode<String>(renderNodeStart.RenderNode_ID));
                    netListNodeEnd = netList.Get(netList.GetNodeNum() - 1);

                    netListNodeEnd.ConnAppend(renderNodeStart.RenderNode_ID);
                    netListNodeEnd.ConnAppend(null);
                }
                else
                {
                    //i--;
                    netListNodeEnd = netList.Get(netListNodeEndIdx);
                    //netListNodeEnd.ConnClear();

                    netListNodeEnd.ConnSet(0, renderNodeStart.RenderNode_ID);
                }
            }

            System.out.println("---- iter " + (i + 1) + " ----");
            System.out.println(netList.ToString());
            System.out.println(this.Editor_NetListReplaceIDsWithNames(netList).ToString() + "\n");
        }

        // Replacing all the wire & symbol IDs with their names...
        netList = this.Editor_NetListReplaceIDsWithNames(netList);*/

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
        String leftSymbolName = this.nodes.get(this.NodeFind(leftSymbolId)).name;
        String rightSymbolName = this.nodes.get(this.NodeFind(rightSymbolId)).name;

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
    static public int NetListWireFind(LinkedList<EDANode> wires, String id)
    {
        for (int i = 0; i < wires.size(); i++)
            if (wires.get(i).id.equals(id))
                return i;

        return -1;
    }

    public static void TextFieldListenerInit(TextField textField)
    {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > EDAmameController.Editor_MaxChars)
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
            EDANode renderNode = this.nodes.get(i);

            if (renderNode.passive)
                continue;

            for (int j = 0; j < renderNode.manualSnapPoints.size(); j++)
            {
                Shape snapPoint = renderNode.manualSnapPoints.get(j);
                PairMutable snapPos = new PairMutable(snapPoint.getTranslateX(), snapPoint.getTranslateY());

                Double currDist = Utils.GetDist(pos, snapPos);

                if (currDist <= minDist)
                {
                    posSnapped = snapPos;
                    minDist = currDist;
                }
            }
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
        Shape foundCrosshair = null;

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
                                            else if (nextNodeC.getClass() == Circle.class)
                                            {
                                                foundCrosshair = (Circle)nextNodeC;
                                            }
                                            else
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
        if (foundCrosshair == null)
            throw new InvalidClassException("Unable to locate crosshair shape for an editor with name \"" + this.name + "\"!");

        this.paneListener = foundPaneListener;
        this.paneHolder = foundPaneHolder;
        this.paneHighlights = foundPaneHighlights;
        this.paneSelections = foundPaneSelections;
        this.paneSnaps = foundPaneSnaps;
        this.canvas = foundCanvas;
        this.gc = this.canvas.getGraphicsContext2D();
        this.crosshair = foundCrosshair;

        this.PaneHolderSetTranslate(new PairMutable(0.0, 0.0));
        this.paneHolder.prefWidthProperty().bind(this.canvas.widthProperty());
        this.paneHolder.prefHeightProperty().bind(this.canvas.heightProperty());
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
}