/*
* MapComposer is an OrbisGIS plugin dedicated to the creation of cartographic
* documents.
*
* This plugin was firstly developed  at French IRSTV institute as part of the MApUCE project,
* funded by the French Agence Nationale de la Recherche (ANR) under contract ANR-13-VBDU-0004.
* 
* Since 2015, MapComposer is developed and maintened by the GIS group of the DECIDE team of the 
* Lab-STICC CNRS laboratory, see <http://www.lab-sticc.fr/>.
*
* The GIS group of the DECIDE team is located at :
*
* Laboratoire Lab-STICC – CNRS UMR 6285
* Equipe DECIDE
* UNIVERSITÉ DE BRETAGNE-SUD
* Institut Universitaire de Technologie de Vannes
* 8, Rue Montaigne - BP 561 56017 Vannes Cedex
*
* Copyright (C) 2007-2014 CNRS (IRSTV FR CNRS 2488)
* Copyright (C) 2015-2017 CNRS (Lab-STICC UMR CNRS 6285)
*
* The MapComposer plugin is distributed under GPL 3 license. 
* This file is part of the MapComposer plugin.
*
* The MapComposer plugin is free software: you can redistribute it and/or modify it under the
* terms of the GNU General Public License as published by the Free Software
* Foundation, either version 3 of the License, or (at your option) any later
* version.
*
* The MapComposer plugin is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU General Public License for more details <http://www.gnu.org/licenses/>.
*/

package org.orbisgis.mapcomposer.view.utils;

import org.orbisgis.mapcomposer.controller.MainController;

import javax.swing.*;
import javax.swing.plaf.LayerUI;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.beans.EventHandler;
import java.beans.PropertyChangeEvent;

/**
 * This class is used as an overlay to the CompositionArea to permit to draw the bounding box of a new GraphicalElement and print messages to the user.
 * It will be enabled only when the user wants to create a new GraphicalElement. So the user can draw the bounding box of the new GraphicalElement.
 * The size will be transfer to the UIController to create the GE.
 *
 * @author Sylvain PALOMINOS
 */
public class CompositionAreaOverlay extends LayerUI<JComponent>{
    /** Point where the mouse is pressed.*/
    private Point start;
    /** Point where the mouse is released.*/
    private Point end;
    /** MainController */
    private MainController mainController;

    /** Enumeration containing all the mode of drawing
     * NEW_GE : The user is drawing a new GraphicalElement.
     * RESIZE_GE : The user is resizing a GraphicalElement.
     * NONE : the user isn't interacting with the overlay.
     */
    public enum Mode{NEW_GE, RESIZE_GE, NONE}
    /** Drawing mode of the Overlay */
    private Mode mode;

    /** Stop the overlay if the message has not change during this time */
    private static final int MESSAGE_TIMEOUT = 5000;
    /** Size of the arc value for drawing the round rectangle **/
    private static final int MESSAGE_ARC = 15;
    /** Space between the window border and the message box border **/
    private static final int MESSAGE_MARGIN = 10;
    /** Alpha value applied to the message box.**/
    private static final float MESSAGE_ALPHA = 0.7f;
    /** Border in pixels, on top and bottom of text message */
    private static final int OVERLAY_INNER_BORDER = 2;

    /** Message to diasplay */
    private String message;
    /** Font of the message */
    private Font messageFont;
    /** Timer of the mesage */
    private Timer messageTimer;

    /** This value is the ratio between width and height of the new GraphicalElement (width/height) */
    private float ratio;

    /** Tells if the ratio should be respected */
    private boolean respectRatio;

    /** Tells if the message needs to be drawn */
    private boolean isMessageDrawn;

    /**
     * Main constructor.
     * @param mainController
     */
    public CompositionAreaOverlay(MainController mainController){
        this.mainController = mainController;
        ratio = -1;
        mode = null;
        messageFont = new JLabel().getFont().deriveFont(Font.BOLD);
        this.respectRatio = false;
    }

    /**
     * Set the ratio value to respect on drawing the new GraphicalElement bounding box.
     * @param ratio
     */
    public void setRatio(float ratio) {
        this.ratio = ratio;
    }

    /**
     * Set the start point of the rectangle drawn. The point coordinate origin should be the CompositionAreaOverlay origin.
     * @param start
     */
    public void setStart(Point start) {
        this.start = start;
    }

    /**
     * Set the end point of the rectangle drawn. The point coordinate origin should be the CompositionAreaOverlay origin.
     * @param end
     */
    public void setEnd(Point end) {
        this.end = end;
    }

    /**
     * Set the mode of the overlay : NONE if nothing should be drawn, NEW_GE if a new GraphicalElement is drawn by the user, RESIZE_GE if the user is resizing an element.
     * @param mode
     */
    public void setMode(Mode mode) {
        this.mode = mode;
    }

    @Override
    public void applyPropertyChange(PropertyChangeEvent pce, JLayer l) {
        if ("isMessageDrawn".equals(pce.getPropertyName())) {
            l.repaint();
        }
    }

    @Override
    public void paint (Graphics g, JComponent c) {
        super.paint(g, c);
        //Test if the user is doing a modification on a GraphicalElement
        if (mode == Mode.NEW_GE || mode == Mode.RESIZE_GE){
            //Verify that the extreme points of the draw are defined
            if (start != null && end != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                float[] dash = {10.0f};
                g2.setStroke(new BasicStroke(1.0f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER,10.0f,dash,0.0f));
                int x, y;
                float width, height;
                x = (end.x < start.x) ? end.x : start.x;
                y = (end.y < start.y) ? end.y : start.y;
                //If the ratio in negative, there is no need to respect it
                if(!respectRatio) {
                    width = Math.abs(end.x - start.x);
                    height = Math.abs(end.y - start.y);
                }
                //if the ratio is positive, the new GE bounding box have to respect it.
                else{
                    width = (Math.abs(end.x - start.x)>(Math.abs(end.y - start.y)*ratio))?Math.abs(end.x - start.x):Math.abs(end.y - start.y)*ratio;
                    height = width/ratio;

                }
                g2.drawRect(x, y, (int)width, (int)height);
                g2.dispose();
            }
        }
        if(isMessageDrawn) {
            int x = MESSAGE_MARGIN / 2;
            int w = c.getWidth() - MESSAGE_MARGIN;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setFont(messageFont);
            FontMetrics fm = g2.getFontMetrics();
            Rectangle2D textSize = fm.getStringBounds(message, g2);
            Composite urComposite = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, MESSAGE_ALPHA));

            int overlayHeight = (int) (Math.ceil(textSize.getHeight() + OVERLAY_INNER_BORDER * 2));
            int y = c.getHeight() - overlayHeight;
            int h = (overlayHeight) + MESSAGE_ARC;

            g2.setColor(Color.BLACK);
            g2.fillRoundRect(x, y, w, h, MESSAGE_ARC, MESSAGE_ARC);
            g2.setColor(Color.WHITE);
            g2.drawString(message, OVERLAY_INNER_BORDER * 2 + (int) textSize.getHeight(), (int) (c.getHeight() - (overlayHeight / 2.f) + (textSize.getHeight() / 2.f)));
            g2.setStroke(new BasicStroke(2));
            g2.setColor(Color.LIGHT_GRAY);
            g2.drawRoundRect((x) - 1, y - 1, w + 1, h + 1, MESSAGE_ARC+1, MESSAGE_ARC+1);
            g2.setComposite(urComposite);
            g2.dispose();
        }
    }

    @Override
    protected void processMouseMotionEvent(MouseEvent e, JLayer<? extends JComponent> l) {
        //If the LayerUI is disable, it doesn't consume the mouse event.
        if(mode == Mode.NONE)
            super.processMouseMotionEvent(e, l);
        else if(mode == Mode.NEW_GE) {

            respectRatio = e.isShiftDown();

            if (e.getID() == MouseEvent.MOUSE_DRAGGED) {
                end = new Point(e.getLocationOnScreen().x - mainController.getMainWindow().getCompositionArea().getLocationOnScreen().x,
                                e.getLocationOnScreen().y - mainController.getMainWindow().getCompositionArea().getLocationOnScreen().y);
                l.repaint();
            }
            e.consume();
        }
        else if(mode == Mode.RESIZE_GE){
            super.processMouseMotionEvent(e, l);
            l.repaint();
        }
    }

    @Override
    protected void processMouseEvent(MouseEvent e, JLayer<? extends JComponent> l) {
        //If the LayerUI is disable, it doesn't consume the mouse event.
        if(mode == Mode.NONE)
            super.processMouseEvent(e, l);
        else if(mode == Mode.NEW_GE) {

            respectRatio = e.isShiftDown();

            if (e.getID() == MouseEvent.MOUSE_PRESSED) {
                //Get the click location on the screen an save the location in the compositionArea
                start = new Point(  e.getLocationOnScreen().x - mainController.getMainWindow().getCompositionArea().getLocationOnScreen().x,
                                    e.getLocationOnScreen().y - mainController.getMainWindow().getCompositionArea().getLocationOnScreen().y);
            }
            if (e.getID() == MouseEvent.MOUSE_RELEASED) {
                int x, y;
                float width, height;
                //If the ratio in negative, there is no need to respect it
                if(ratio<=0 || !respectRatio) {
                    x = (end.x < start.x) ? end.x : start.x;
                    y = (end.y < start.y) ? end.y : start.y;
                    width = Math.abs(end.x - start.x);
                    height = Math.abs(end.y - start.y);
                }
                //if the ratio is positive, the new GE bounding box have to respect it.
                else{
                    x = (end.x < start.x) ? end.x : start.x;
                    y = (end.y < start.y) ? end.y : start.y;
                    width = (Math.abs(end.x - start.x)>(Math.abs(end.y - start.y)*ratio))?Math.abs(end.x - start.x):Math.abs(end.y - start.y)*ratio;
                    height = width/ratio;

                }
                Point point = mainController.getMainWindow().getCompositionArea().screenPointToDocumentPoint(new Point(x, y));
                mainController.getGEController().setNewGE(point.x, point.y, (int)width, (int)height);
                start=null;
                end=null;
                ratio=-1;
            }
            e.consume();
        }
        else if(mode == Mode.RESIZE_GE){
            super.processMouseEvent(e, l);
            if (e.getID() == MouseEvent.MOUSE_RELEASED) {
                start = null;
                end = null;
                ratio = -1;
                l.repaint();
            }
        }
    }

    /**
     * Write a message in the UILayer of the CompositionArea
     * @param message String to write.
     */
    public void writeMessage(String message){
        this.message = message;
        isMessageDrawn =true;
        messageTimer = new Timer(MESSAGE_TIMEOUT, EventHandler.create(ActionListener.class, this, "clearMessage"));
        messageTimer.setRepeats(true);
        messageTimer.start();
        this.firePropertyChange("isMessageDrawn", null, null);
    }

    /**
     * Makes the message box appear from the bottom of the window.
     */
    public void clearMessage(){
        isMessageDrawn =false;
        messageTimer.stop();
        this.firePropertyChange("isMessageDrawn", null, null);
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        JLayer jlayer = (JLayer)c;
        jlayer.setLayerEventMask( AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK );
    }

    @Override
    public void uninstallUI(JComponent c) {
        JLayer jlayer = (JLayer)c;
        jlayer.setLayerEventMask(0);
        super.uninstallUI(c);
    }
}
