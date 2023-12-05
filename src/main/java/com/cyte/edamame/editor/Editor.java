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
import com.cyte.edamame.render.RenderNode;
import com.cyte.edamame.render.RenderSystem;
import com.cyte.edamame.util.Utils;

import java.util.*;
import java.util.logging.Level;

import com.cyte.edamame.util.PairMutable;
import javafx.collections.*;
import javafx.geometry.Bounds;
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
    final public String Editor_ID = UUID.randomUUID().toString();

    public Integer Editor_Type = -1;
    public String Editor_Name = null;

    public PairMutable Editor_ZoomLimits;
    public Double Editor_ZoomFactor;
    public Double Editor_MouseDragFactor;
    public Double Editor_MouseDragCheckTimeout;
    public Color Editor_SelectionBoxColor;
    public Double Editor_SelectionBoxWidth;

    // DO NOT EDIT

    // holders for the UI elements. The UI elements are instantiated in a single FXML file/load
    // The individual elements are extracted to these for use by the EDAmame Application.

    /** The main Editor_Tab for EDAmame to include in its main Editor_Tab list. */
    protected Tab Editor_Tab = null;
    /** An optional ToolBar to provide EDAmame to include/append to its toolbars. */
    protected ToolBar Editor_ToolBar = null;
    /** a list of Editor_Tabs to include in the navigation Editor_Tab pane when this editor is active. (can be empty) */
    protected ObservableList<Tab> Editor_Tabs = FXCollections.observableArrayList();
    /**
     * A structure of menu items to include in EDAmame's Editor_Menus. Any menuitems associated with a string will
     * be inserted/visible under the menu with the same name in the EDAmame main menubar. The string must
     * match exactly including mneumonic underscores and such. Missing Editor_Menus at the EDAmame level are not created.
     */
    public HashMap<String, ObservableList<MenuItem>> Editor_Menus = new HashMap<String, ObservableList<MenuItem>>();    // NOTE: We have to use the hash map because a list would cause the menu items to disappear after the first editor dissection
    public RenderSystem Editor_RenderSystem;

    public boolean Editor_Visible = false;
    public boolean Editor_PressedLMB = false;
    public boolean Editor_PressedRMB = false;
    public Double Editor_Zoom = 1.0;
    public PairMutable Editor_MouseDragFirstPos = null;
    public PairMutable Editor_MouseDragDiffPos = null;
    public Long Editor_MouseDragLastTime = System.nanoTime();
    public PairMutable Editor_MouseDragFirstCenter = null;
    public boolean Editor_MouseDragReachedEdge = false;
    public PairMutable Editor_MouseDragPaneFirstPos = null;
    public Integer Editor_ShapesHighlighted = 0;
    public Integer Editor_ShapesSelected = 0;
    public boolean Editor_ShapesWereSelected = false;
    public boolean Editor_ShapesMoving = false;
    //public boolean Editor_PressedOnShape = false;
    public Rectangle Editor_SelectionBox = null;
    public boolean Editor_WasSelectionBox = false;
    public Line EditorSymbol_LinePreview = null;

    public MementoExperimental UndoRedoSystem = null;

    //// MAIN FUNCTIONS ////

    public void Editor_Init(Integer type, String name)
    {
        this.Editor_Type = type;
        this.Editor_Name = name;

        this.Editor_ZoomLimits = EDAmameController.Editor_ZoomLimits;
        this.Editor_ZoomFactor = EDAmameController.Editor_ZoomFactor;
        this.Editor_MouseDragFactor = EDAmameController.Editor_MouseDragFactor;
        this.Editor_MouseDragCheckTimeout = EDAmameController.Editor_MouseCheckTimeout;
        this.Editor_SelectionBoxColor = EDAmameController.Editor_SelectionBoxColors[this.Editor_Type];
        this.Editor_SelectionBoxWidth = EDAmameController.Editor_SelectionBoxWidth;

        this.UndoRedoSystem = new MementoExperimental(this);
    }

    /**
     * Request an editor to close. Handling any information/state saving as it needs.
     * @return true if the editor was able to close without unsaved information/state, false otherwise.
     */
    public void Editor_Close()
    {
        EDAmameApplication.App_Controller.Controller_EditorRemove(this);
    }

    public void Editor_Heartbeat()
    {
        if ((this.Editor_Type == -1) || (this.Editor_Name == null))
            throw new java.lang.Error("ERROR: Attempting to run editor without initializing it!");

        this.Editor_TestShapesClear();

        // Handling render node highlight, selected & snap shapes refreshing...
        for (int i = 0; i < this.Editor_RenderSystem.RenderSystem_Nodes.size(); i++)
        {
            RenderNode renderNode = this.Editor_RenderSystem.RenderSystem_Nodes.get(i);

            if (renderNode.RenderNode_Passive)
                continue;

            renderNode.RenderNode_BoundsRefresh();
            renderNode.RenderNode_SnapPointsRefresh();
        }

        // Handling centering of holder pane & crosshair...
        {
            PairMutable canvasSize = new PairMutable(this.Editor_RenderSystem.RenderSystem_Canvas.getWidth(),
                                                     this.Editor_RenderSystem.RenderSystem_Canvas.getHeight());
            PairMutable paneSize = new PairMutable(this.Editor_RenderSystem.RenderSystem_PaneListener.getWidth(),
                                                   this.Editor_RenderSystem.RenderSystem_PaneListener.getHeight());
            PairMutable centeredPos = new PairMutable(paneSize.GetLeftDouble() / 2 - canvasSize.GetLeftDouble() / 2,
                                                      paneSize.GetRightDouble() / 2 - canvasSize.GetRightDouble() / 2);

            this.Editor_RenderSystem.RenderSystem_PaneHolder.setLayoutX(centeredPos.GetLeftDouble());
            this.Editor_RenderSystem.RenderSystem_PaneHolder.setLayoutY(centeredPos.GetRightDouble());

            this.Editor_RenderSystem.RenderSystem_Crosshair.setTranslateX(this.Editor_RenderSystem.RenderSystem_PaneListener.getWidth() / 2);
            this.Editor_RenderSystem.RenderSystem_Crosshair.setTranslateY(this.Editor_RenderSystem.RenderSystem_PaneListener.getHeight() / 2);
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
    public Tab Editor_GetTab() { return Editor_Tab; }

    /**
     * Returns the optional ToolBar of this editor for inclusion by EDAmame.
     * @return The ToolBar if available, null otherwise.
     */
    public ToolBar Editor_GetToolBar() { return Editor_ToolBar; }

    /**
     * Returns a list of control Editor_Tabs for EDAmame to include in the main controls Editor_Tab pane.
     * @return A (possibly empty) list of control Editor_Tabs.
     */
    public ObservableList<Tab> Editor_GetControlTabs() { return Editor_Tabs; }

    //// RENDER FUNCTIONS ////

    public void Editor_NodesDeselectAll()
    {
        for (int i = 0; i < this.Editor_RenderSystem.RenderSystem_Nodes.size(); i++)
        {
            RenderNode renderNode = this.Editor_RenderSystem.RenderSystem_Nodes.get(i);

            if (!renderNode.RenderNode_Selected)
            {
                if (renderNode.RenderNode_HighlightedMouse || renderNode.RenderNode_HighlightedBox)
                {
                    renderNode.RenderNode_Selected = true;
                    //shape.RenderShape_ShapeSelectedRefresh();
                    //this.Editor_RenderSystem.RenderSystem_PaneSelections.getChildren().add(renderNode.RenderNode_ShapeSelected);
                    renderNode.RenderNode_ShapeSelected.setVisible(true);
                    this.Editor_ShapesSelected++;
                }
            }
            else
            {
                if ((!renderNode.RenderNode_HighlightedMouse && !renderNode.RenderNode_HighlightedBox) && !EDAmameController.Controller_IsKeyPressed(KeyCode.SHIFT))
                {
                    renderNode.RenderNode_Selected = false;
                    //this.Editor_RenderSystem.RenderSystem_NodeSelectionsRemove(renderNode);
                    renderNode.RenderNode_ShapeSelected.setVisible(false);
                    this.Editor_ShapesSelected--;
                }
            }

            if (renderNode.RenderNode_HighlightedBox && !renderNode.RenderNode_HighlightedMouse)
                //this.Editor_RenderSystem.RenderSystem_NodeHighlightsRemove(renderNode);
                renderNode.RenderNode_ShapeHighlighted.setVisible(false);

            renderNode.RenderNode_MousePressPos = null;
        }

        if (this.Editor_SelectionBox != null)
        {
            this.Editor_RenderSystem.RenderSystem_PaneListener.getChildren().remove(this.Editor_SelectionBox);
            this.Editor_SelectionBox = null;
            this.Editor_WasSelectionBox = true;
        }
        else
        {
            this.Editor_WasSelectionBox = false;
        }
    }

    public void Editor_NodeSnapPointsCheck(PairMutable posEvent)
    {
        PairMutable posMouse = this.Editor_RenderSystem.RenderSystem_PanePosListenerToHolder(new PairMutable(posEvent.GetLeftDouble(), posEvent.GetRightDouble()));

        for (int i = 0; i < this.Editor_RenderSystem.RenderSystem_Nodes.size(); i++)
        {
            RenderNode renderNode = this.Editor_RenderSystem.RenderSystem_Nodes.get(i);

            for (int j = 0; j < renderNode.RenderNode_SnapPoints.size(); j++)
            {
                Shape snapPoint = renderNode.RenderNode_SnapPoints.get(j);
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

    static public void Editor_LineDropPosCalculate(Line line, PairMutable posStart, PairMutable posEnd)
    {
        PairMutable posAvg = new PairMutable((posStart.GetLeftDouble() + posEnd.GetLeftDouble()) / 2, (posStart.GetRightDouble() + posEnd.GetRightDouble()) / 2);

        line.setStartX(posStart.GetLeftDouble() - posAvg.GetLeftDouble());
        line.setStartY(posStart.GetRightDouble() - posAvg.GetRightDouble());
        line.setEndX(posEnd.GetLeftDouble() - posAvg.GetLeftDouble());
        line.setEndY(posEnd.GetRightDouble() - posAvg.GetRightDouble());

        line.setTranslateX(posAvg.GetLeftDouble());
        line.setTranslateY(posAvg.GetRightDouble());
    }

    static public PairMutable Editor_LineEndPointsCalculate(Line line, boolean start)
    {
        if (start)
            return new PairMutable(line.getStartX() + line.getTranslateX(), line.getStartY() + line.getTranslateY());

        return new PairMutable(line.getEndX() + line.getTranslateX(), line.getEndY() + line.getTranslateY());
    }

    public void Editor_LinePreviewUpdate(PairMutable dropPos)
    {
        this.EditorSymbol_LinePreview.setEndX(dropPos.GetLeftDouble());
        this.EditorSymbol_LinePreview.setEndY(dropPos.GetRightDouble());
    }

    public void Editor_LinePreviewRemove()
    {
        this.Editor_RenderSystem.RenderSystem_NodeRemove("linePreview");
        this.EditorSymbol_LinePreview = null;
    }

    public void Editor_NodeHighlightsCheck(PairMutable posEvent)
    {
        PairMutable posMouse = this.Editor_RenderSystem.RenderSystem_PanePosListenerToHolder(new PairMutable(posEvent.GetLeftDouble(), posEvent.GetRightDouble()));

        for (int i = 0; i < this.Editor_RenderSystem.RenderSystem_Nodes.size(); i++)
        {
            RenderNode renderNode = this.Editor_RenderSystem.RenderSystem_Nodes.get(i);

            if (renderNode.RenderNode_Passive)
                continue;

            //renderNode.RenderNode_ShapeHighlightedRefresh();
            boolean onShape = renderNode.RenderNode_PosOnNode(posMouse);

            if (!EDAmameController.Controller_IsKeyPressed(KeyCode.CONTROL))
            {
                // Checking whether we are highlighting by cursor...
                if (onShape)
                {
                    if (EDAmameController.Controller_IsKeyPressed(KeyCode.Q))
                    {
                        if ((this.Editor_ShapesHighlighted > 1) && renderNode.RenderNode_HighlightedMouse)
                            renderNode.RenderNode_HighlightedMouse = false;
                        else if ((this.Editor_ShapesHighlighted == 0) && !renderNode.RenderNode_HighlightedMouse)
                            renderNode.RenderNode_HighlightedMouse = true;
                    }
                    else
                    {
                        if (!renderNode.RenderNode_HighlightedMouse)
                            renderNode.RenderNode_HighlightedMouse = true;
                    }
                }
                else
                {
                    if (renderNode.RenderNode_HighlightedMouse)
                        renderNode.RenderNode_HighlightedMouse = false;
                }

                // Checking whether we are highlighting by selection box...
                if (this.Editor_SelectionBox != null)
                {
                    Bounds shapeBounds = renderNode.RenderNode_Node.getBoundsInParent();
                    PairMutable selectionBoxL = this.Editor_RenderSystem.RenderSystem_PanePosListenerToHolder(new PairMutable(this.Editor_SelectionBox.getTranslateX(), this.Editor_SelectionBox.getTranslateY()));
                    PairMutable selectionBoxH = this.Editor_RenderSystem.RenderSystem_PanePosListenerToHolder(new PairMutable(this.Editor_SelectionBox.getTranslateX() + this.Editor_SelectionBox.getWidth(), this.Editor_SelectionBox.getTranslateY() + this.Editor_SelectionBox.getHeight()));

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
                        if (!renderNode.RenderNode_HighlightedBox)
                            renderNode.RenderNode_HighlightedBox = true;
                    }
                    else
                    {
                        if (renderNode.RenderNode_HighlightedBox)
                            renderNode.RenderNode_HighlightedBox = false;
                    }
                }
                else if (renderNode.RenderNode_HighlightedBox)
                {
                    renderNode.RenderNode_HighlightedBox = false;
                }
            }
            else
            {
                renderNode.RenderNode_HighlightedMouse = false;
                renderNode.RenderNode_HighlightedBox = false;
            }

            // Adjusting highlights accordingly...
            if ((renderNode.RenderNode_HighlightedMouse || renderNode.RenderNode_HighlightedBox) && !renderNode.RenderNode_Highlighted)
            {
                //shape.RenderShape_ShapeHighlightedRefresh();
                //this.Editor_RenderSystem.RenderSystem_PaneHighlights.getChildren().add(renderNode.RenderNode_ShapeHighlighted);
                renderNode.RenderNode_ShapeHighlighted.setVisible(true);
                renderNode.RenderNode_Highlighted = true;
                this.Editor_ShapesHighlighted++;
            }
            else if ((!renderNode.RenderNode_HighlightedMouse && !renderNode.RenderNode_HighlightedBox) && renderNode.RenderNode_Highlighted)
            {
                //this.Editor_RenderSystem.RenderSystem_NodeHighlightsRemove(renderNode);
                renderNode.RenderNode_ShapeHighlighted.setVisible(false);
                renderNode.RenderNode_Highlighted = false;
                this.Editor_ShapesHighlighted--;
            }
        }
    }

    public void Editor_MouseDragUpdate(PairMutable posMouse)
    {
        this.Editor_MouseDragDiffPos = new PairMutable((posMouse.GetLeftDouble() - this.Editor_MouseDragFirstPos.GetLeftDouble()) * this.Editor_MouseDragFactor / this.Editor_Zoom,
                                                       (posMouse.GetRightDouble() - this.Editor_MouseDragFirstPos.GetRightDouble()) * this.Editor_MouseDragFactor / this.Editor_Zoom);
    }

    public void Editor_MouseDragReset(PairMutable posMouse)
    {
        this.Editor_MouseDragFirstPos = new PairMutable(posMouse);
        this.Editor_MouseDragFirstCenter = new PairMutable(this.Editor_RenderSystem.RenderSystem_Center.GetLeftDouble(),
                                                           this.Editor_RenderSystem.RenderSystem_Center.GetRightDouble());
        this.Editor_MouseDragPaneFirstPos = new PairMutable(this.Editor_RenderSystem.RenderSystem_PaneHolderGetTranslate());

        if (this.Editor_ShapesSelected > 0)
        {
            for (int i = 0; i < this.Editor_RenderSystem.RenderSystem_Nodes.size(); i++)
            {
                RenderNode renderNode = this.Editor_RenderSystem.RenderSystem_Nodes.get(i);

                if (!renderNode.RenderNode_Selected)
                    continue;

                renderNode.RenderNode_MousePressPos = new PairMutable(renderNode.RenderNode_Node.getTranslateX(),
                                                                      renderNode.RenderNode_Node.getTranslateY());
            }
        }
    }

    //// CALLBACK FUNCTIONS ////

    public void Editor_OnDragOverGlobal(DragEvent event)
    {
        PairMutable eventPos = new PairMutable(event.getX(), event.getY());

        // Handling shape highlights...
        this.Editor_NodeHighlightsCheck(eventPos);

        // Handling node snap shapes...
        this.Editor_NodeSnapPointsCheck(eventPos);
    }

    public void Editor_OnDragDroppedGlobal(DragEvent event)
    {}

    public void Editor_OnMouseMovedGlobal(MouseEvent event)
    {
        PairMutable eventPos = new PairMutable(event.getX(), event.getY());
        PairMutable dropPos = this.Editor_RenderSystem.RenderSystem_PanePosListenerToHolder(eventPos);
        PairMutable realPos = this.Editor_RenderSystem.RenderSystem_PaneHolderGetRealPos(dropPos);

        // Handling shape highlights...
        this.Editor_NodeHighlightsCheck(eventPos);

        // Handling node snap shapes...
        this.Editor_NodeSnapPointsCheck(eventPos);

        // Handling line drawing preview...
        if (this.EditorSymbol_LinePreview != null)
            this.Editor_LinePreviewUpdate(dropPos);
    }

    public void Editor_OnMousePressedGlobal(MouseEvent event)
    {
        if (this.Editor_PressedLMB)
        {
            this.UndoRedoSystem.Memento_NodeHistoryUpdate();
        }
        else if (this.Editor_PressedRMB)
        {}

        this.Editor_MouseDragFirstPos = null;
        this.Editor_MouseDragPaneFirstPos = null;

        //if (this.Editor_ShapesHighlighted > 0)
        //    this.Editor_PressedOnShape = true;
    }

    public void Editor_OnMouseReleasedGlobal(MouseEvent event)
    {
        if (this.Editor_PressedLMB)
        {
            if (this.Editor_ShapesSelected > 0)
                this.Editor_ShapesWereSelected = true;
            else
                this.Editor_ShapesWereSelected = false;

            // Handling shape deselection (only if we're not moving any shapes or drawing any lines)
            if (!this.Editor_ShapesMoving &&
                (this.EditorSymbol_LinePreview == null))
                this.Editor_NodesDeselectAll();
        }
        else if (this.Editor_PressedRMB)
        {
            // Handling auto-zoom
            if (EDAmameController.Controller_IsKeyPressed(KeyCode.ALT))
            {
                this.Editor_RenderSystem.RenderSystem_PaneHolderSetTranslate(new PairMutable(0.0, 0.0));
                this.Editor_RenderSystem.RenderSystem_PaneHolderSetScale(new PairMutable(1.0, 1.0), false);

                this.Editor_RenderSystem.RenderSystem_Center = new PairMutable(0.0, 0.0);
                this.Editor_Zoom = 1.0;
            }
        }

        this.Editor_ShapesMoving = false;
        //this.Editor_PressedOnShape = false;
    }

    public void Editor_OnMouseDraggedGlobal(MouseEvent event)
    {
        PairMutable eventPos = new PairMutable(event.getX(), event.getY());
        PairMutable dropPos = this.Editor_RenderSystem.RenderSystem_PanePosListenerToHolder(eventPos);
        PairMutable realPos = this.Editor_RenderSystem.RenderSystem_PaneHolderGetRealPos(dropPos);

        // Handling shape highlights...
        this.Editor_NodeHighlightsCheck(eventPos);

        // Handling node snap shapes...
        this.Editor_NodeSnapPointsCheck(eventPos);

        // Handling line drawing preview...
        if (this.EditorSymbol_LinePreview != null)
            this.Editor_LinePreviewUpdate(dropPos);

        if (this.Editor_PressedLMB)
        {
            // Handling moving of the shapes (only if we have some shapes selected, we're not holding the box selection key and we're not drawing any lines)
            if ((this.Editor_ShapesSelected > 0) &&
                !EDAmameController.Controller_IsKeyPressed(KeyCode.SHIFT) &&
                (this.EditorSymbol_LinePreview == null))
            {
                for (int i = 0; i < this.Editor_RenderSystem.RenderSystem_Nodes.size(); i++)
                {
                    RenderNode renderNode = this.Editor_RenderSystem.RenderSystem_Nodes.get(i);

                    if (!renderNode.RenderNode_Selected)
                        continue;

                    PairMutable posPressReal = this.Editor_RenderSystem.RenderSystem_PaneHolderGetRealPos(new PairMutable(renderNode.RenderNode_MousePressPos.GetLeftDouble(),
                                                                                                                          renderNode.RenderNode_MousePressPos.GetRightDouble()));
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
                    if (EDAmameController.Controller_IsKeyPressed(KeyCode.CONTROL))
                    {
                        if (Math.abs(this.Editor_MouseDragDiffPos.GetLeftDouble()) < Math.abs(this.Editor_MouseDragDiffPos.GetRightDouble()))
                            posOffset.left = -this.Editor_MouseDragDiffPos.GetLeftDouble();
                        else
                            posOffset.right = -this.Editor_MouseDragDiffPos.GetRightDouble();
                    }

                    renderNode.RenderNode_Node.setTranslateX(renderNode.RenderNode_MousePressPos.GetLeftDouble() + this.Editor_MouseDragDiffPos.GetLeftDouble() + posOffset.GetLeftDouble());
                    renderNode.RenderNode_Node.setTranslateY(renderNode.RenderNode_MousePressPos.GetRightDouble() + this.Editor_MouseDragDiffPos.GetRightDouble() + posOffset.GetRightDouble());
                }

                this.Editor_ShapesMoving = true;
            }

            // Handling the box selection (only if we have no shapes selected, we are not moving the viewport and we're not drawing any lines)
            if (((this.Editor_ShapesSelected == 0) || EDAmameController.Controller_IsKeyPressed(KeyCode.SHIFT)) &&
                (this.EditorSymbol_LinePreview == null))
            {
                if (this.Editor_SelectionBox == null)
                {
                    this.Editor_SelectionBox = new Rectangle(0.0, 0.0);
                    this.Editor_SelectionBox.setFill(Color.TRANSPARENT);
                    this.Editor_SelectionBox.setStroke(this.Editor_SelectionBoxColor);
                    this.Editor_SelectionBox.setStrokeWidth(this.Editor_SelectionBoxWidth);

                    this.Editor_RenderSystem.RenderSystem_PaneListener.getChildren().add(1, this.Editor_SelectionBox);
                }

                // Adjusting if the width & height are negative...
                if (this.Editor_MouseDragDiffPos.GetLeftDouble() < 0)
                    this.Editor_SelectionBox.setTranslateX(this.Editor_MouseDragFirstPos.GetLeftDouble() + this.Editor_MouseDragDiffPos.GetLeftDouble() * this.Editor_Zoom / this.Editor_MouseDragFactor);
                else
                    this.Editor_SelectionBox.setTranslateX(this.Editor_MouseDragFirstPos.GetLeftDouble());

                if (this.Editor_MouseDragDiffPos.GetRightDouble() < 0)
                    this.Editor_SelectionBox.setTranslateY(this.Editor_MouseDragFirstPos.GetRightDouble() + this.Editor_MouseDragDiffPos.GetRightDouble() * this.Editor_Zoom / this.Editor_MouseDragFactor);
                else
                    this.Editor_SelectionBox.setTranslateY(this.Editor_MouseDragFirstPos.GetRightDouble());

                this.Editor_SelectionBox.setWidth(Math.abs(this.Editor_MouseDragDiffPos.GetLeftDouble() * this.Editor_Zoom / this.Editor_MouseDragFactor));
                this.Editor_SelectionBox.setHeight(Math.abs(this.Editor_MouseDragDiffPos.GetRightDouble() * this.Editor_Zoom / this.Editor_MouseDragFactor));
            }
        }
        else if (this.Editor_PressedRMB)
        {
            // Handling moving of the viewport
            {
                this.Editor_MouseDragReachedEdge = false;

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

                this.Editor_RenderSystem.RenderSystem_Center.left = this.Editor_MouseDragFirstCenter.GetLeftDouble() + this.Editor_MouseDragDiffPos.GetLeftDouble();
                this.Editor_RenderSystem.RenderSystem_Center.right = this.Editor_MouseDragFirstCenter.GetRightDouble() + this.Editor_MouseDragDiffPos.GetRightDouble();

                this.Editor_RenderSystem.RenderSystem_PaneHolderSetTranslate(new PairMutable(this.Editor_MouseDragPaneFirstPos.GetLeftDouble() + this.Editor_MouseDragDiffPos.GetLeftDouble() * this.Editor_Zoom,
                                                                                       this.Editor_MouseDragPaneFirstPos.GetRightDouble() + this.Editor_MouseDragDiffPos.GetRightDouble() * this.Editor_Zoom));
            }
        }
    }

    public void Editor_OnScrollGlobal(ScrollEvent event)
    {
        PairMutable eventPos = new PairMutable(event.getX(), event.getY());
        PairMutable dropPos = this.Editor_RenderSystem.RenderSystem_PanePosListenerToHolder(eventPos);
        PairMutable realPos = this.Editor_RenderSystem.RenderSystem_PaneHolderGetRealPos(dropPos);

        // Handling shape rotation (only if we have shapes selected and R is pressed)
        if ((this.Editor_ShapesSelected > 0) && EDAmameController.Controller_IsKeyPressed(KeyCode.R))
        {
            for (int i = 0; i < this.Editor_RenderSystem.RenderSystem_Nodes.size(); i++)
            {
                RenderNode renderNode = this.Editor_RenderSystem.RenderSystem_Nodes.get(i);
                String nodeId = renderNode.RenderNode_Node.getId();

                if (!renderNode.RenderNode_Selected ||
                    (renderNode.RenderNode_Node.getClass() == Line.class) ||
                    ((nodeId != null) && nodeId.contains("PIN_")))
                    continue;

                double angle = 10;

                if (event.getDeltaY() < 0)
                    angle = -10;

                renderNode.RenderNode_Node.setRotate(renderNode.RenderNode_Node.getRotate() + angle);

                //shape.RenderShape_ShapeSelectedRefresh();
                //shape.RenderShape_ShapeHighlightedRefresh();
            }
        }
        // Handling zoom scaling (only if we're not rotating anything)
        else
        {
            if (event.getDeltaY() < 0)
                if ((this.Editor_Zoom / this.Editor_ZoomFactor) <= this.Editor_ZoomLimits.GetLeftDouble())
                    this.Editor_Zoom = this.Editor_ZoomLimits.GetLeftDouble();
                else
                    this.Editor_Zoom /= this.Editor_ZoomFactor;
            else
            if ((this.Editor_Zoom * this.Editor_ZoomFactor) >= this.Editor_ZoomLimits.GetRightDouble())
                this.Editor_Zoom = this.Editor_ZoomLimits.GetRightDouble();
            else
                this.Editor_Zoom *= this.Editor_ZoomFactor;

            this.Editor_RenderSystem.RenderSystem_PaneHolderSetScale(new PairMutable(this.Editor_Zoom, this.Editor_Zoom), true);
        }

        // Handling shape highlights...
        this.Editor_NodeHighlightsCheck(eventPos);

        // Handling node snap shapes...
        this.Editor_NodeSnapPointsCheck(eventPos);

        // Handling line drawing preview...
        if (this.EditorSymbol_LinePreview != null)
            this.Editor_LinePreviewUpdate(dropPos);
    }

    public void Editor_OnKeyPressedGlobal(KeyEvent event)
    {
        // Handling element properties window...
        if (EDAmameController.Controller_IsKeyPressed(KeyCode.E) && (EDAmameController.Controller_EditorPropertiesWindow == null))
        {
            // Attempting to create the properties window...
            EditorProps propsWindow = EditorProps.EditorProps_Create();

            if ((propsWindow != null))
            {
                propsWindow.EditorProps_Stage.setOnHidden(e -> {
                    EDAmameController.Controller_EditorPropertiesWindow = null;
                });
                propsWindow.EditorProps_Editor = this;
                propsWindow.EditorProps_Stage.show();

                EDAmameController.Controller_EditorPropertiesWindow = propsWindow;
            }
        }

        // Handling shape deletion...
        if (EDAmameController.Controller_IsKeyPressed(KeyCode.BACK_SPACE) || EDAmameController.Controller_IsKeyPressed(KeyCode.DELETE))
        {
            if (this.Editor_ShapesSelected > 0)
            {
                for (int i = 0; i < this.Editor_RenderSystem.RenderSystem_Nodes.size(); i++)
                {
                    RenderNode renderNode = this.Editor_RenderSystem.RenderSystem_Nodes.get(i);

                    if (!renderNode.RenderNode_Selected)
                        continue;

                    if (renderNode.RenderNode_Highlighted)
                    {
                        //this.Editor_RenderSystem.RenderSystem_NodeHighlightsRemove(renderNode);
                        renderNode.RenderNode_ShapeHighlighted.setVisible(false);
                        this.Editor_ShapesHighlighted--;
                    }

                    //this.Editor_RenderSystem.RenderSystem_NodeSelectionsRemove(renderNode);
                    renderNode.RenderNode_ShapeSelected.setVisible(false);
                    this.Editor_ShapesSelected--;

                    this.Editor_RenderSystem.RenderSystem_PaneHolder.getChildren().remove(renderNode.RenderNode_Node);
                    this.Editor_RenderSystem.RenderSystem_Nodes.remove(renderNode);

                    i--;
                }
            }
        }

        // Handling line drawing interruption & element deselection...
        if (EDAmameController.Controller_IsKeyPressed(KeyCode.ESCAPE))
        {
            if (this.EditorSymbol_LinePreview != null)
                this.Editor_LinePreviewRemove();
            else
                this.Editor_NodesDeselectAll();
        }

        // Handling element undo...
        if (EDAmameController.Controller_IsKeyPressed(KeyCode.CONTROL) && EDAmameController.Controller_IsKeyPressed(KeyCode.Z))
            this.UndoRedoSystem.Memento_NodesUndo();

        // Handling element undo...
        if (EDAmameController.Controller_IsKeyPressed(KeyCode.CONTROL) && EDAmameController.Controller_IsKeyPressed(KeyCode.Y))
            this.UndoRedoSystem.Memento_NodesRedo();
    }

    public void Editor_OnKeyReleasedGlobal(KeyEvent event)
    {}

    abstract public void Editor_OnDragOverSpecific(DragEvent event);
    abstract public void Editor_OnDragDroppedSpecific(DragEvent event);
    abstract public void Editor_OnMouseMovedSpecific(MouseEvent event);
    abstract public void Editor_OnMousePressedSpecific(MouseEvent event);
    abstract public void Editor_OnMouseReleasedSpecific(MouseEvent event);
    abstract public void Editor_OnMouseDraggedSpecific(MouseEvent event);
    abstract public void Editor_OnScrollSpecific(ScrollEvent event);
    abstract public void Editor_OnKeyPressedSpecific(KeyEvent event);
    abstract public void Editor_OnKeyReleasedSpecific(KeyEvent event);

    public void Editor_ListenersInit()
    {
        // When we drag the mouse (from outside the viewport)...
        this.Editor_RenderSystem.RenderSystem_PaneListener.setOnDragOver(event -> {
            // Handling global callback actions
            this.Editor_OnDragOverGlobal(event);

            // Handling editor-specific callback actions
            this.Editor_OnDragOverSpecific(event);

            event.consume();
        });

        // When we drop something with the cursor (from outside the viewport)...
        this.Editor_RenderSystem.RenderSystem_PaneListener.setOnDragDropped(event -> {
            // Handling global callback actions
            this.Editor_OnDragDroppedGlobal(event);

            // Handling editor-specific callback actions
            this.Editor_OnDragDroppedSpecific(event);

            event.setDropCompleted(true);
            event.consume();
        });

        // When we move the mouse...
        this.Editor_RenderSystem.RenderSystem_PaneListener.setOnMouseMoved(event -> {
            // Handling global callback actions
            this.Editor_OnMouseMovedGlobal(event);

            // Handling editor-specific callback actions
            this.Editor_OnMouseMovedSpecific(event);

            event.consume();
        });

        // When we press down the mouse...
        this.Editor_RenderSystem.RenderSystem_PaneListener.setOnMousePressed(event -> {
            // Updating mouse pressed flags
            if (event.isPrimaryButtonDown())
                this.Editor_PressedLMB = true;
            if (event.isSecondaryButtonDown())
                this.Editor_PressedRMB = true;

            // Updating the current mouse drag positions
            PairMutable posMouse = new PairMutable(event.getX(), event.getY());

            if ((this.Editor_MouseDragFirstPos == null) || this.Editor_MouseDragReachedEdge)
                this.Editor_MouseDragReset(posMouse);

            // Handling global callback actions
            this.Editor_OnMousePressedGlobal(event);

            // Handling editor-specific callback actions
            this.Editor_OnMousePressedSpecific(event);

            event.consume();
        });

        // When we release the mouse...
        this.Editor_RenderSystem.RenderSystem_PaneListener.setOnMouseReleased(event -> {
            // Handling global callback actions
            this.Editor_OnMouseReleasedGlobal(event);

            // Handling editor-specific callback actions
            this.Editor_OnMouseReleasedSpecific(event);

            // Updating mouse pressed flags
            this.Editor_PressedLMB = false;
            this.Editor_PressedRMB = false;

            event.consume();
        });

        // When we drag the mouse (from inside the viewport)...
        this.Editor_RenderSystem.RenderSystem_PaneListener.setOnMouseDragged(event -> {
            // Only execute callback if we're past the check timeout
            if (((System.nanoTime() - this.Editor_MouseDragLastTime) / 1e9) < this.Editor_MouseDragCheckTimeout)
                return;

            // Updating the current mouse drag positions
            {
                PairMutable posMouse = new PairMutable(event.getX(), event.getY());

                if ((this.Editor_MouseDragFirstPos == null) || this.Editor_MouseDragReachedEdge)
                    this.Editor_MouseDragReset(posMouse);

                this.Editor_MouseDragUpdate(posMouse);
            }

            // Handling global callback actions
            this.Editor_OnMouseDraggedGlobal(event);

            // Handling editor-specific callback actions
            this.Editor_OnMouseDraggedSpecific(event);

            this.Editor_MouseDragLastTime = System.nanoTime();

            event.consume();
        });

        // When we scroll the mouse...
        this.Editor_RenderSystem.RenderSystem_PaneListener.setOnScroll(event -> {
            // Handling global callback actions
            this.Editor_OnScrollGlobal(event);

            // Handling editor-specific callback actions
            this.Editor_OnScrollSpecific(event);

            event.consume();
        });
    }

    //// PROPERTIES WINDOW FUNCTIONS ////

    public void Editor_PropsLoadGlobal()
    {
        Text globalHeader = new Text("Global Properties:");
        globalHeader.setStyle("-fx-font-weight: bold;");
        globalHeader.setStyle("-fx-font-size: 16px;");
        EDAmameController.Controller_EditorPropertiesWindow.EditorProps_PropsBox.getChildren().add(globalHeader);
        EDAmameController.Controller_EditorPropertiesWindow.EditorProps_PropsBox.getChildren().add(new Separator());

        if (this.Editor_ShapesSelected == 0)
            return;

        // Reading all global node properties...
        LinkedList<String> names = new LinkedList<String>();
        LinkedList<Double> posX = new LinkedList<Double>();
        LinkedList<Double> posY = new LinkedList<Double>();
        LinkedList<Double> rots = new LinkedList<Double>();
        LinkedList<Color> colors = new LinkedList<Color>();

        for (int i = 0; i < this.Editor_RenderSystem.RenderSystem_Nodes.size(); i++)
        {
            RenderNode renderNode = this.Editor_RenderSystem.RenderSystem_Nodes.get(i);

            if (!renderNode.RenderNode_Selected)
                continue;

            names.add(renderNode.RenderNode_Name);
            posX.add(renderNode.RenderNode_Node.getTranslateX() - this.Editor_RenderSystem.RenderSystem_PaneHolder.getWidth() / 2);
            posY.add(renderNode.RenderNode_Node.getTranslateY() - this.Editor_RenderSystem.RenderSystem_PaneHolder.getHeight() / 2);

            if ((renderNode.RenderNode_Node.getClass() != Line.class) &&
                !renderNode.RenderNode_IsPin)
                rots.add(renderNode.RenderNode_Node.getRotate());

            if (renderNode.RenderNode_Node.getClass() == Line.class)
            {
                colors.add((Color)((Line) renderNode.RenderNode_Node).getStroke());
            }
            else if (renderNode.RenderNode_IsPin)
            {
                Group group = (Group)renderNode.RenderNode_Node;

                if (group.getChildren().size() != 2)
                    throw new java.lang.Error("ERROR: Attempting to load pin into global properties editor without 2 children!");

                colors.add((Color)((Shape)group.getChildren().get(0)).getStroke());
            }
            else if (renderNode.RenderNode_Node.getClass() != Group.class)
            {
                colors.add((Color)((Shape)renderNode.RenderNode_Node).getFill());
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

            if (EDAmameController.Controller_IsListAllEqual(names))
                nameText.setText(names.get(0));
            else
                nameText.setText("<mixed>");

            EDAmameController.Controller_EditorPropertiesWindow.EditorProps_PropsBox.getChildren().add(nameBox);
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

            if (EDAmameController.Controller_IsListAllEqual(posX))
                posXText.setText(Double.toString(posX.get(0)));
            else
                posXText.setText("<mixed>");

            if (EDAmameController.Controller_IsListAllEqual(posY))
                posYText.setText(Double.toString(posY.get(0)));
            else
                posYText.setText("<mixed>");

            EDAmameController.Controller_EditorPropertiesWindow.EditorProps_PropsBox.getChildren().add(posHBox);
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

            if (EDAmameController.Controller_IsListAllEqual(rots))
                rotText.setText(Double.toString(rots.get(0)));
            else
                rotText.setText("<mixed>");

            EDAmameController.Controller_EditorPropertiesWindow.EditorProps_PropsBox.getChildren().add(rotHBox);
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

            if (EDAmameController.Controller_IsListAllEqual(colors))
                colorPicker.setValue(colors.get(0));
            else
                colorPicker.setValue(null);

            EDAmameController.Controller_EditorPropertiesWindow.EditorProps_PropsBox.getChildren().add(colorHBox);
        }

        EDAmameController.Controller_EditorPropertiesWindow.EditorProps_PropsBox.getChildren().add(new Separator());
    }

    public void Editor_PropsApplyGlobal()
    {
        if (this.Editor_ShapesSelected == 0)
            return;

        VBox propsBox = EDAmameController.Controller_EditorPropertiesWindow.EditorProps_PropsBox;

        // Iterating over all the nodes & attempting to apply global node properties if selected...
        for (int i = 0; i < this.Editor_RenderSystem.RenderSystem_Nodes.size(); i++)
        {
            RenderNode renderNode = this.Editor_RenderSystem.RenderSystem_Nodes.get(i);

            if (!renderNode.RenderNode_Selected)
                continue;

            // Applying name...
            {
                Integer nameBoxIdx = EDAmameController.Controller_FindNodeById(propsBox.getChildren(), "nameBox");

                if (nameBoxIdx != -1)
                {
                    HBox nameBox = (HBox)propsBox.getChildren().get(nameBoxIdx);
                    TextField nameText = (TextField)EDAmameController.Controller_GetNodeById(nameBox.getChildren(), "name");

                    if (nameText == null)
                        throw new java.lang.Error("ERROR: Unable to find \"name\" node in global properties window \"nameBox\" entry!");

                    String nameStr = nameText.getText();

                    if (!nameStr.isEmpty())
                    {
                        renderNode.RenderNode_Name = nameStr;
                    }
                    else if (!nameStr.equals("<mixed>"))
                    {
                        EDAmameController.Controller_SetStatusBar("Unable to apply element name because the entered field is empty!");
                    }
                }
            }

            // Applying position...
            {
                Integer posBoxIdx = EDAmameController.Controller_FindNodeById(propsBox.getChildren(), "posBox");

                if (posBoxIdx != -1)
                {
                    HBox posBox = (HBox)propsBox.getChildren().get(posBoxIdx);
                    TextField posXText = (TextField)EDAmameController.Controller_GetNodeById(posBox.getChildren(), "posX");
                    TextField posYText = (TextField)EDAmameController.Controller_GetNodeById(posBox.getChildren(), "posY");

                    if (posXText == null)
                        throw new java.lang.Error("ERROR: Unable to find \"posX\" node in global properties window \"posBox\" entry!");
                    if (posYText == null)
                        throw new java.lang.Error("ERROR: Unable to find \"posY\" node in global properties window \"posBox\" entry!");

                    String posXStr = posXText.getText();
                    String posYStr = posYText.getText();

                    if (EDAmameController.Controller_IsStringNum(posXStr))
                    {
                        Double newPosX = Double.parseDouble(posXStr);

                        /*if ((newPosX >= -EDAmameController.Editor_TheaterSize.GetLeftDouble() / 2) &&
                            (newPosX <= EDAmameController.Editor_TheaterSize.GetLeftDouble() / 2))
                        {*/
                            renderNode.RenderNode_Node.setTranslateX(newPosX + this.Editor_RenderSystem.RenderSystem_PaneHolder.getWidth() / 2);
                        /*}
                        else
                        {
                            EDAmameController.Controller_SetStatusBar("Unable to apply element X position because the entered field is outside the theater limits!");
                        }*/
                    }
                    else if (!posXStr.equals("<mixed>"))
                    {
                        EDAmameController.Controller_SetStatusBar("Unable to apply element X position because the entered field is non-numeric!");
                    }

                    if (EDAmameController.Controller_IsStringNum(posYStr))
                    {
                        Double newPosY = Double.parseDouble(posYStr);

                        /*if ((newPosY >= -EDAmameController.Editor_TheaterSize.GetRightDouble() / 2) &&
                            (newPosY <= EDAmameController.Editor_TheaterSize.GetRightDouble() / 2))
                        {*/
                            renderNode.RenderNode_Node.setTranslateY(newPosY + this.Editor_RenderSystem.RenderSystem_PaneHolder.getHeight() / 2);
                        /*}
                        else
                        {
                            EDAmameController.Controller_SetStatusBar("Unable to apply element Y position because the entered field is outside the theater limits!");
                        }*/
                    }
                    else if (!posYStr.equals("<mixed>"))
                    {
                        EDAmameController.Controller_SetStatusBar("Unable to apply element Y position because the entered field is non-numeric!");
                    }
                }

            }

            // Applying rotation...
            if ((renderNode.RenderNode_Node.getClass() != Line.class) &&
                !renderNode.RenderNode_IsPin)
            {
                Integer rotBoxIdx = EDAmameController.Controller_FindNodeById(propsBox.getChildren(), "rotBox");

                if (rotBoxIdx != -1)
                {
                    HBox rotBox = (HBox)propsBox.getChildren().get(rotBoxIdx);
                    TextField rotText = (TextField)EDAmameController.Controller_GetNodeById(rotBox.getChildren(), "rot");

                    if (rotText == null)
                        throw new java.lang.Error("ERROR: Unable to find \"rot\" node in global properties window \"rotBox\" entry!");

                    String rotStr = rotText.getText();

                    if (EDAmameController.Controller_IsStringNum(rotStr))
                    {
                        renderNode.RenderNode_Node.setRotate(Double.parseDouble(rotStr));
                    }
                    else if (!rotStr.equals("<mixed>"))
                    {
                        EDAmameController.Controller_SetStatusBar("Unable to apply element rotation because the entered field is non-numeric!");
                    }
                }
            }

            // Applying color...
            {
                Integer colorBoxIdx = EDAmameController.Controller_FindNodeById(propsBox.getChildren(), "colorBox");

                if (colorBoxIdx != -1)
                {
                    HBox colorBox = (HBox) propsBox.getChildren().get(colorBoxIdx);
                    ColorPicker colorPicker = (ColorPicker) EDAmameController.Controller_GetNodeById(colorBox.getChildren(), "color");

                    if (colorPicker == null)
                        throw new java.lang.Error("ERROR: Unable to find \"color\" node in Symbol Editor properties window \"colorBox\" entry!");

                    Color color = colorPicker.getValue();

                    if ((color != null) && (color != Color.TRANSPARENT) && (color.hashCode() != 0x00000000))
                    {
                        if (renderNode.RenderNode_Node.getClass() == Line.class)
                        {
                            ((Line)renderNode.RenderNode_Node).setStroke(color);
                        }
                        else if (renderNode.RenderNode_IsPin)
                        {
                            Group group = (Group)renderNode.RenderNode_Node;

                            if (group.getChildren().size() != 2)
                                throw new java.lang.Error("ERROR: Attempting to load pin into global properties window without 2 children!");

                            ((Shape)group.getChildren().get(0)).setFill(color);
                            ((Shape)group.getChildren().get(1)).setFill(color);
                        }
                        else if (renderNode.RenderNode_Node.getClass() != Group.class)
                        {
                            ((Shape)renderNode.RenderNode_Node).setFill(color);
                        }
                    }
                    else
                    {
                        if (color != null)
                            EDAmameController.Controller_SetStatusBar("Unable to apply shape colors because the entered color is transparent!");
                    }
                }
            }
        }

        this.UndoRedoSystem.Memento_NodeHistoryUpdate();
    }

    abstract public void Editor_PropsLoadSpecific();
    abstract public void Editor_PropsApplySpecific();

    //// SUPPORT FUNCTIONS ////

    public NetListExperimental<String> Editor_ToNetList()
    {
        NetListExperimental<String> netList = new NetListExperimental<String>();
        LinkedList<RenderNode> wires = new LinkedList<RenderNode>();
        LinkedList<String> pinLabels = new LinkedList<String>();
        LinkedList<PairMutable> pinPos = new LinkedList<PairMutable>();
        LinkedList<String> pinSymbolIDs = new LinkedList<String>();

        // Populating the tables above...
        for (int i = 0; i < this.Editor_RenderSystem.RenderSystem_Nodes.size(); i++)
        {
            RenderNode renderNode = this.Editor_RenderSystem.RenderSystem_Nodes.get(i);

            // If we're reading a wire...
            if (renderNode.RenderNode_Node.getClass() == Line.class)
            {
                wires.add(renderNode);
            }
            // If we're reading a symbol...
            else if (renderNode.RenderNode_Node.getClass() == Group.class)
            {
                Group symbol = (Group)renderNode.RenderNode_Node;

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
                        pinSymbolIDs.add(renderNode.RenderNode_ID);

                        this.Editor_RenderSystem.RenderSystem_TestShapeAdd(pos, 20.0, Color.BLUE, 0.5, false);
                    }
                }
            }
        }

        // Iterating through all the wires & checking their start & end connections...
        LinkedList<PairMutable> wireConnIDs = new LinkedList<PairMutable>();

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
                PairMutable checkPosEnd = Utils.GetPosInNodeParent(checkWire, new PairMutable(checkWire.getEndY(), checkWire.getEndY()));

                if (checkPosStart.EqualsDouble(wirePosStart))
                    nodeStartID = wires.get(j).RenderNode_ID;
                if (checkPosEnd.EqualsDouble(wirePosEnd))
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

        //netList.ReplaceNodeIDsWithNames(this.Editor_RenderSystem);

        return netList;
    }

    public static void Editor_TextFieldListenerInit(TextField textField)
    {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > EDAmameController.Editor_MaxChars)
                textField.setText(oldValue);
        });
    }

    public PairMutable Editor_MagneticSnapCheck(PairMutable pos)
    {
        PairMutable posSnapped = new PairMutable(pos);
        Double minDist = EDAmameController.Editor_SnapPointRadius;

        // Checking for magnetic snap...
        for (int i = 0; i < this.Editor_RenderSystem.RenderSystem_Nodes.size(); i++)
        {
            RenderNode renderNode = this.Editor_RenderSystem.RenderSystem_Nodes.get(i);

            if (renderNode.RenderNode_Passive)
                continue;

            for (int j = 0; j < renderNode.RenderNode_SnapPoints.size(); j++)
            {
                Shape snapPoint = renderNode.RenderNode_SnapPoints.get(j);
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
    public void Editor_Dissect(Integer editorType, Scene scene) throws InvalidClassException
    {
        Node root = scene.getRoot();

        if (root == null)
            throw new InvalidClassException("root of scene is null");
        if (root.getClass() != VBox.class)
            throw new InvalidClassException("Expected VBox but found " + root.getClass());

        // Searching the scene for all the required elements
        Iterator<Node> nodeIterator = ((VBox)root).getChildren().iterator();
        String prefix = this.Editor_ID;
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
                EDAmameController.Controller_Logger.log(Level.INFO, "Dissecting a ToolBar in Editor \"" + Editor_ID + "\"...\n");

                Editor_ToolBar = (ToolBar) node;
                Editor_ToolBar.setVisible(false); // toolbar starts invisible. Becomes visible on Tab selection.
                Editor_ToolBar.setId(prefix + "_TOOLBAR");
            }
            else if (node.getClass() == MenuBar.class)
            {
                EDAmameController.Controller_Logger.log(Level.INFO, "Dissecting a MenuBar in Editor \"" + Editor_ID + "\"...\n");

                ObservableList<Menu> menus = ((MenuBar)node).getMenus();

                for (int i = 0; i < menus.size(); i++)
                {
                    Menu currMenu = menus.get(i);
                    String currMenuName = currMenu.getText();

                    if (!this.Editor_Menus.containsKey(currMenuName))
                        this.Editor_Menus.put(currMenuName, FXCollections.observableArrayList());

                    this.Editor_Menus.get(currMenuName).addAll(currMenu.getItems());
                }
            }
            else if (node.getClass() == TabPane.class)
            {
                EDAmameController.Controller_Logger.log(Level.INFO, "Dissecting a TabPane in Editor \"" + Editor_ID + "\"...\n");

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

                                                            EDAmameController.Controller_Logger.log(Level.INFO, "Found selections pane of an editor with name \"" + this.Editor_Name + "\".\n");
                                                        }
                                                        else if (foundPaneHighlights == null)
                                                        {
                                                            foundPaneHighlights = (Pane)nextNodeD;

                                                            EDAmameController.Controller_Logger.log(Level.INFO, "Found highlights pane of an editor with name \"" + this.Editor_Name + "\".\n");
                                                        }
                                                        else if (foundPaneSnaps == null)
                                                        {
                                                            foundPaneSnaps = (Pane)nextNodeD;

                                                            EDAmameController.Controller_Logger.log(Level.INFO, "Found snaps pane of an editor with name \"" + this.Editor_Name + "\".\n");
                                                        }
                                                        else
                                                        {
                                                            throw new java.lang.Error("ERROR: Encountered extra pane under the holder pane!");
                                                        }
                                                    }
                                                    else if (nextNodeD.getClass() == Canvas.class)
                                                    {
                                                        foundCanvas = (Canvas)nextNodeD;

                                                        EDAmameController.Controller_Logger.log(Level.INFO, "Found canvas of an editor with name \"" + this.Editor_Name + "\".\n");
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
                        Editor_Tab = item;
                    }
                    else
                    {
                        Editor_Tabs.add(item);
                    }
                }
            }
        }

        if (foundPaneListener == null)
            throw new InvalidClassException("Unable to locate listener pane for an editor with name \"" + this.Editor_Name + "\"!");
        if (foundPaneHolder == null)
            throw new InvalidClassException("Unable to locate holder pane for an editor with name \"" + this.Editor_Name + "\"!");
        if (foundPaneHighlights == null)
            throw new InvalidClassException("Unable to locate highlights pane for an editor with name \"" + this.Editor_Name + "\"!");
        if (foundPaneSelections == null)
            throw new InvalidClassException("Unable to locate selections pane for an editor with name \"" + this.Editor_Name + "\"!");
        if (foundPaneSnaps == null)
            throw new InvalidClassException("Unable to locate snaps pane for an editor with name \"" + this.Editor_Name + "\"!");
        if (foundCanvas == null)
            throw new InvalidClassException("Unable to locate canvas for an editor with name \"" + this.Editor_Name + "\"!");
        if (foundCrosshair == null)
            throw new InvalidClassException("Unable to locate crosshair shape for an editor with name \"" + this.Editor_Name + "\"!");

        this.Editor_RenderSystem = new RenderSystem(foundPaneListener,
                                                    foundPaneHolder,
                                                    foundPaneHighlights,
                                                    foundPaneSelections,
                                                    foundPaneSnaps,
                                                    foundCanvas,
                                                    foundCrosshair,
                                                    EDAmameController.Editor_TheaterSize,
                                                    EDAmameController.Editor_BackgroundColors[editorType],
                                                    EDAmameController.Editor_GridPointColors[editorType],
                                                    EDAmameController.Editor_GridBoxColors[editorType],
                                                    EDAmameController.Editor_MaxShapes);
    }

    //// TESTING FUNCTIONS ////

    public void Editor_TestShapesClear()
    {
        for (int i = 0; i < this.Editor_RenderSystem.RenderSystem_PaneHolder.getChildren().size(); i++)
        {
            Node currChild = this.Editor_RenderSystem.RenderSystem_PaneHolder.getChildren().get(i);
            String currChildId = currChild.getId();

            if ((currChildId != null) && currChildId.equals("testShapePassive"))
            {
                this.Editor_RenderSystem.RenderSystem_PaneHolder.getChildren().remove(currChild);
                i--;
            }
        }
    }
}