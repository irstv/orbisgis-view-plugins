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

package org.orbisgis.mapcomposer.model.graphicalelement.element;

import org.orbisgis.mapcomposer.controller.MainController;
import org.orbisgis.mapcomposer.model.graphicalelement.interfaces.GraphicalElement;
import org.orbisgis.mapcomposer.model.configurationattribute.interfaces.ConfigurationAttribute;
import org.orbisgis.mapcomposer.model.configurationattribute.attribute.IntegerCA;
import org.slf4j.LoggerFactory;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple implementation of the GraphicalElement interface.
 * It contains all the basic ConfigurationAttributes (CA) and the implementation of the interface functions.
 *
 * @author Sylvain PALOMINOS
 */
public abstract class SimpleGE implements GraphicalElement{
    /** x position of the GE.*/
    protected IntegerCA x;
    /** y position of the GE.*/
    protected IntegerCA y;
    /** Inclination of the GE.*/
    protected IntegerCA rotation;
    /** Height of the GE.*/
    protected IntegerCA height;
    /** Width of the GE.*/
    protected IntegerCA width;
    /** Z index of the GE.*/
    protected IntegerCA z;

    /** Translation*/
    private static final I18n i18n = I18nFactory.getI18n(SimpleGE.class);

    /**Displayed name of the x position*/
    public static final String sX = I18n.marktr("x");

    /**Displayed name of the y position*/
    public static final String sY = I18n.marktr("y");

    /**Displayed name of the rotation*/
    public static final String sRotation = I18n.marktr("Rotation");

    /**Displayed name of the height*/
    public static final String sHeight = I18n.marktr("Height");

    /**Displayed name of the width*/
    public static final String sWidth = I18n.marktr("Width");
    
    /**
     * Main constructor.
     */
    public SimpleGE(){
        //ConfigurationAttribute instantiation
        x= new IntegerCA(sX, false, 0);
        y= new IntegerCA(sY, false, 0);
        z= new IntegerCA(I18n.marktr("z"), false, 0);
        rotation= new IntegerCA(sRotation, false, 0, true, -360, 360);
        height= new IntegerCA(sHeight, false, 50, true, 1, Integer.MAX_VALUE);
        width= new IntegerCA(sWidth, false, 50, true, 1, Integer.MAX_VALUE);
    }
    
//Setters
    @Override public void setX(int x)   {this.x.setValue(x);}
    @Override public void setY(int y)   {this.y.setValue(y);}
    @Override public void setZ(int z)   {this.z.setValue(z); }
    @Override public void setRotation(int rotation) {this.rotation.setValue(rotation);}
    @Override public void setHeight(int height)     {this.height.setValue(height);}
    @Override public void setWidth(int width)       {this.width.setValue(width);}
//Getters
    @Override public int getX() {return this.x.getValue();}
    @Override public int getY() {return this.y.getValue();}
    @Override public int getZ() {return this.z.getValue();}
    @Override public int getRotation()  {return this.rotation.getValue();}
    @Override public int getHeight()    {return this.height.getValue();}
    @Override public int getWidth()     {return this.width.getValue();}

    @Override
    public void setAttribute(ConfigurationAttribute ca) {
        if(ca.getName().equals(x.getName()))
            x=(IntegerCA)ca;
        if(ca.getName().equals(y.getName()))
            y=(IntegerCA)ca;
        if(ca.getName().equals(z.getName()))
            z=(IntegerCA)ca;
        if(ca.getName().equals(rotation.getName()))
            rotation=(IntegerCA)ca;
        if(ca.getName().equals(height.getName()))
            height=(IntegerCA)ca;
        if(ca.getName().equals(width.getName()))
            width=(IntegerCA)ca;
    }

    @Override
    public List<ConfigurationAttribute> getSavableAttributes() {
        List<ConfigurationAttribute> list = new ArrayList<>();
        list.add(x);
        list.add(y);
        list.add(z);
        list.add(width);
        list.add(height);
        list.add(rotation);
        return list;
    }

    @Override
    public GraphicalElement deepCopy(){
        SimpleGE copy = null;
        try {
            copy = getClass().newInstance();
        } catch (InstantiationException|IllegalAccessException e) {
            LoggerFactory.getLogger(MainController.class).error(e.getMessage());
        }
        copy.x = (IntegerCA) this.x.deepCopy();
        copy.y = (IntegerCA) this.y.deepCopy();
        copy.z = (IntegerCA) this.z.deepCopy();
        copy.rotation = (IntegerCA) this.rotation.deepCopy();
        copy.width = (IntegerCA) this.width.deepCopy();
        copy.height = (IntegerCA) this.height.deepCopy();

        return copy;
    }
}