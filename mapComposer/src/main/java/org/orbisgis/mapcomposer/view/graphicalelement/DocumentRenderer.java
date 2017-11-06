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

package org.orbisgis.mapcomposer.view.graphicalelement;

import org.orbisgis.commons.progress.ProgressMonitor;
import org.orbisgis.mapcomposer.controller.MainController;
import org.orbisgis.mapcomposer.model.configurationattribute.attribute.IntegerCA;
import org.orbisgis.mapcomposer.model.configurationattribute.attribute.SourceListCA;
import org.orbisgis.mapcomposer.model.configurationattribute.attribute.StringCA;
import org.orbisgis.mapcomposer.model.configurationattribute.interfaces.ConfigurationAttribute;
import org.orbisgis.mapcomposer.model.graphicalelement.element.Document;
import org.orbisgis.mapcomposer.model.graphicalelement.element.SimpleGE;
import org.orbisgis.mapcomposer.model.graphicalelement.interfaces.GraphicalElement;
import org.orbisgis.mapcomposer.view.utils.UIDialogProperties;
import org.orbisgis.sif.UIPanel;
import org.orbisgis.sif.common.ContainerItem;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeListener;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.EventHandler;
import java.util.List;

/**
 * Renderer associated to the Document GraphicalElement.
 *
 * @author Sylvain PALOMINOS
 */
public class DocumentRenderer implements RendererRaster, RendererVector, CustomConfigurationPanel {

    /** Object for the translation*/
    private static final I18n i18n = I18nFactory.getI18n(DocumentRenderer.class);

    @Override
    public void drawGE(Graphics2D graphics2D, GraphicalElement ge) {
        //Returns a white rectangle without applying any rotation
        graphics2D.setPaint(new Color(255, 255, 255));
        graphics2D.fillRect(0, 0, ge.getWidth(), ge.getHeight());
    }

    @Override
    public BufferedImage createGEImage(GraphicalElement ge, ProgressMonitor pm) {
        BufferedImage bi = new BufferedImage(ge.getWidth(), ge.getHeight(), BufferedImage.TYPE_INT_ARGB);
        drawGE(bi.createGraphics(), ge);
        return bi;
    }

    @Override
    public UIPanel createConfigurationPanel(List<ConfigurationAttribute> caList, MainController mainController,
                                            boolean enableLock){

        //Create the UIDialogProperties that will be returned
        UIDialogProperties uid = new UIDialogProperties(mainController, enableLock);

        //Get the ConfigurationAttribute of the unit and its JComponent
        SourceListCA unitCA = null;
        for(ConfigurationAttribute ca : caList)
            if(ca.getName().equals(Document.sUnit))
                unitCA = (SourceListCA)ca;

        JComboBox<ContainerItem<Document.Unit>> unitBox = new JComboBox<>();
        for(Document.Unit alignment : Document.Unit.values()) {
            unitBox.addItem(new ContainerItem<>(alignment, i18n.tr(alignment.name())));
            if(Document.Unit.valueOf(unitCA.getSelected()).equals(alignment)){
                unitBox.setSelectedItem(unitBox.getItemAt(unitBox.getItemCount()-1));
            }
        }

        unitBox.putClientProperty("ca", unitCA);
        unitBox.putClientProperty("last", ((ContainerItem<Document.Unit>)unitBox.getSelectedItem()).getKey());
        unitBox.addActionListener(EventHandler.create(ActionListener.class, this, "onUnitChange", "source"));

        //Get the ConfigurationAttribute of the width and its JComponent
        IntegerCA widthCA = null;
        for(ConfigurationAttribute ca : caList)
            if(ca.getName().equals(SimpleGE.sWidth))
                widthCA = (IntegerCA)ca;

        JLabel widthLabel = new JLabel(i18n.tr("width"));
        JSpinner widthSpinner = new JSpinner(new SpinnerNumberModel((int)widthCA.getValue(), 0, Integer.MAX_VALUE,
                1));
        //Adds the listener and the client properties needed.
        widthSpinner.putClientProperty("unit", unitBox);
        widthSpinner.putClientProperty("ca", widthCA);
        widthSpinner.addChangeListener(EventHandler.create(ChangeListener.class, this, "onValueChange", "source"));

        widthLabel.setEnabled(false);
        widthSpinner.setEnabled(false);

        //Get the ConfigurationAttribute of the height and its JComponent
        IntegerCA heightCA = null;
        for(ConfigurationAttribute ca : caList)
            if(ca.getName().equals(SimpleGE.sHeight))
                heightCA = (IntegerCA)ca;

        JLabel heightLabel = new JLabel(i18n.tr("height"));
        JSpinner heightSpinner = new JSpinner(new SpinnerNumberModel((int)heightCA.getValue(), 0, Integer.MAX_VALUE,
                1));
        //Adds the listener and the client properties needed.
        heightSpinner.putClientProperty("unit", unitBox);
        heightSpinner.putClientProperty("ca", heightCA);
        heightSpinner.addChangeListener(EventHandler.create(ChangeListener.class, this, "onValueChange", "source"));

        heightLabel.setEnabled(false);
        heightSpinner.setEnabled(false);

        unitBox.putClientProperty("widthSpinner", widthSpinner);
        unitBox.putClientProperty("heightSpinner", heightSpinner);

        //Get the ConfigurationAttribute of the format and its JComponent
        SourceListCA formatCA = null;
        for(ConfigurationAttribute ca : caList)
            if(ca.getName().equals(Document.sFormat))
                formatCA = (SourceListCA)ca;

        JLabel formatName = new JLabel(i18n.tr(formatCA.getName()));
        uid.addComponent(formatName, formatCA, 0, 0, 1, 1);

        JComboBox<ContainerItem<Document.Format>> formatBox = new JComboBox<>();
        for(Document.Format format : Document.Format.values()) {
            formatBox.addItem(new ContainerItem<>(format, i18n.tr(format.name())));
            if(Document.Format.valueOf(formatCA.getSelected()).equals(format)){
                formatBox.setSelectedItem(formatBox.getItemAt(formatBox.getItemCount()-1));
            }
        }

        //Adds the listener and the client properties needed.
        formatBox.putClientProperty("widthLabel", widthLabel);
        formatBox.putClientProperty("widthSpinner", widthSpinner);
        formatBox.putClientProperty("heightLabel", heightLabel);
        formatBox.putClientProperty("heightSpinner", heightSpinner);
        formatBox.putClientProperty("unitBox", unitBox);
        formatBox.putClientProperty("ca", formatCA);
        formatBox.addActionListener(EventHandler.create(ActionListener.class, this, "onFormatChange", "source"));

        //Adds all the previous elements to the UIDialogProperties
        uid.addComponent(formatBox, formatCA, 1, 0, 2, 1);
        uid.addComponent(unitBox, unitCA, 0, 1, 2, 1);

        uid.addComponent(widthLabel, widthCA, 1, 1, 1, 1);
        uid.addComponent(widthSpinner, widthCA, 2, 1, 1, 1);

        uid.addComponent(heightLabel, heightCA, 1, 2, 1, 1);
        uid.addComponent(heightSpinner, heightCA, 2, 2, 1, 1);

        //Find the orientation ConfigurationAttribute
        SourceListCA orientationCA = null;
        for(ConfigurationAttribute ca : caList)
            if(ca.getName().equals(Document.sOrientation))
                orientationCA = (SourceListCA)ca;

        JLabel orientationName = new JLabel(i18n.tr(orientationCA.getName()));
        uid.addComponent(orientationName, orientationCA, 0, 4, 1, 1);

        //Adds the listener and the client properties needed.
        JComboBox<ContainerItem<Document.Orientation>> orientationBox = new JComboBox<>();
        for(Document.Orientation orientation : Document.Orientation.values()){
            orientationBox.addItem(new ContainerItem<>(orientation, i18n.tr(orientation.name())));
            if(Document.Orientation.valueOf(orientationCA.getSelected()).equals(orientation)){
                orientationBox.setSelectedItem(orientationBox.getItemAt(orientationBox.getItemCount()-1));
            }
        }
        uid.addComponent(orientationBox, orientationCA, 1, 4, 2, 1);
        orientationBox.putClientProperty("ca", orientationCA);
        orientationBox.putClientProperty("last", Document.Orientation.LANDSCAPE);
        orientationBox.putClientProperty("widthSpinner", widthSpinner);
        orientationBox.putClientProperty("heightSpinner", heightSpinner);
        orientationBox.addActionListener(EventHandler.create(
                ActionListener.class, this,"onOrientationChange", "source"));

        formatBox.putClientProperty("orientationBox", orientationBox);

        //Find the name ConfigurationAttribute
        StringCA nameCA = null;
        for(ConfigurationAttribute ca : caList)
            if(ca.getName().equals(Document.sName))
                nameCA = (StringCA)ca;

        JLabel nameName = new JLabel(i18n.tr(nameCA.getName()));
        uid.addComponent(nameName, nameCA, 0, 6, 1, 1);

        JTextArea nameArea = (JTextArea)mainController.getCAManager().getRenderer(nameCA)
                .createJComponentFromCA(nameCA);
        uid.addComponent(nameArea, nameCA, 0, 7, 4, 4);

        //Sets the different comboBoxes
        formatBox.setSelectedItem(formatBox.getSelectedItem());
        unitBox.setSelectedItem(unitBox.getSelectedItem());
        orientationBox.setSelectedItem(orientationBox.getSelectedItem());

        return uid;
    }

    /**
     * When the value of the document format is changed, set the width and height value with the one from the format.
     * @param formatSpinner Spinner representing the format.
     */
    public void onFormatChange(Object formatSpinner){
        JComboBox formatBox = (JComboBox) formatSpinner;

        JComboBox unitBox = (JComboBox) ((JComboBox) formatSpinner).getClientProperty("unitBox");
        Document.Unit unit = (Document.Unit) ((ContainerItem)unitBox.getSelectedItem()).getKey();
        JComboBox orientationBox = (JComboBox) ((JComboBox) formatSpinner).getClientProperty("orientationBox");
        JLabel widthLabel = (JLabel) formatBox.getClientProperty("widthLabel");
        JSpinner widthSpinner = (JSpinner) formatBox.getClientProperty("widthSpinner");
        JLabel heightLabel = (JLabel) formatBox.getClientProperty("heightLabel");
        JSpinner heightSpinner = (JSpinner) formatBox.getClientProperty("heightSpinner");

        Document.Format format = ((ContainerItem<Document.Format>)formatBox.getSelectedItem()).getKey();

        //If the format selected is CUSTOM, enable the spinners
        boolean isCustomSelected  = format.equals(Document.Format.CUSTOM);
        widthLabel.setEnabled(isCustomSelected);
        widthSpinner.setEnabled(isCustomSelected);
        heightLabel.setEnabled(isCustomSelected);
        heightSpinner.setEnabled(isCustomSelected);

        double width;
        double height;

        //Sets the width and height according to the format
        if(format.equals(Document.Format.CUSTOM.name())){
            //If the format is custom, keep the previous width and height value
            width = Double.parseDouble(widthSpinner.getValue().toString());
            height = Double.parseDouble(heightSpinner.getValue().toString());
        }
        else {
            if (((ContainerItem<Document.Orientation>)orientationBox.getSelectedItem()).getKey()
                    .equals(Document.Orientation.PORTRAIT.name())){
                width = format.getPixelWidth() / unit.getRatioToPixel();
                height = format.getPixelHeight() / unit.getRatioToPixel();
            }
            else{
                height = format.getPixelWidth() / unit.getRatioToPixel();
                width = format.getPixelHeight() / unit.getRatioToPixel();
            }
        }

        //Correct the values (round for millimeter, and two digits for inch)
        if(unit.equals(Document.Unit.IN)){
            widthSpinner.setValue( ((double) ((int)(width*100)) )/100 );
            heightSpinner.setValue( ((double) ((int)(height * 100))) / 100);
        }
        if(unit.equals(Document.Unit.MM)){
            widthSpinner.setValue((int)Math.round(width));
            heightSpinner.setValue((int)Math.round(height));
        }
        ((SourceListCA)formatBox.getClientProperty("ca")).select(((ContainerItem<Document.Format>) formatBox
                .getSelectedItem()).getKey().name());
        unitBox.setSelectedItem(unitBox.getSelectedItem());
    }

    /**
     * When the value of the document orientation is changed, invert width and height values.
     * @param orientationSpinner Spinner representing the format.
     */
    public void onOrientationChange(Object orientationSpinner){
        JComboBox<ContainerItem<Document.Orientation>> orientationBox = (JComboBox) orientationSpinner;
        Document.Orientation orientation = ((ContainerItem<Document.Orientation>)orientationBox.getSelectedItem())
                .getKey();
        //Verify if the selected orientation is different from the last selected one
        if(!orientation.equals(orientationBox.getClientProperty("last"))) {
            JSpinner widthSpinner = (JSpinner) orientationBox.getClientProperty("widthSpinner");
            JSpinner heightSpinner = (JSpinner) orientationBox.getClientProperty("heightSpinner");
            //Invert the width and the height
            Object temp = widthSpinner.getValue();
            widthSpinner.setValue(heightSpinner.getValue());
            heightSpinner.setValue(temp);
            //Store the new orientation
            orientationBox.putClientProperty("last", orientation);
            ((SourceListCA)orientationBox.getClientProperty("ca")).select(orientation.name());
        }
    }

    /**
     * When the value of the document unit is changed, convert the width and height values.
     * @param unitSpinner Spinner representing the format.
     */
    public void onUnitChange(Object unitSpinner){
        JComboBox unitBox = (JComboBox) unitSpinner;
        Document.Unit unitBefore = (Document.Unit) unitBox.getClientProperty("last");
        Document.Unit unitNow = ((ContainerItem<Document.Unit>)unitBox.getSelectedItem()).getKey();

        JSpinner widthSpinner = (JSpinner) unitBox.getClientProperty("widthSpinner");
        JSpinner heightSpinner = (JSpinner) unitBox.getClientProperty("heightSpinner");

        //Get the actual width and height value
        double width = 1;
        if(widthSpinner.getValue() instanceof Double){
            width = (double)widthSpinner.getValue();
        }
        if(widthSpinner.getValue() instanceof Integer){
            width = (int)widthSpinner.getValue();
        }
        double height = 1;
        if(heightSpinner.getValue() instanceof Double){
            height = (double)heightSpinner.getValue();
        }
        if(heightSpinner.getValue() instanceof Integer){
            height = (int)heightSpinner.getValue();
        }

        //Convert the value to the pixel unit
        width *= unitBefore.getRatioToPixel();
        height *= unitBefore.getRatioToPixel();

        //Convert the pixel value to the new unit
        width /= unitNow.getRatioToPixel();
        height /= unitNow.getRatioToPixel();

        //According to the unit set the width and height spinner model and store the new unit value
        switch(unitNow){
            case PIXEL:
                width = (int)Math.round(width);
                height = (int)Math.round(height);
                widthSpinner.setModel(new SpinnerNumberModel(width, 0, Integer.MAX_VALUE, 1));
                heightSpinner.setModel(new SpinnerNumberModel(height, 0, Integer.MAX_VALUE, 1));
                unitBox.putClientProperty("last", Document.Unit.PIXEL);
                break;
            case MM:
                width = (int)Math.round(width);
                height = (int)Math.round(height);
                widthSpinner.setModel(new SpinnerNumberModel(width, 0, Integer.MAX_VALUE, 1));
                heightSpinner.setModel(new SpinnerNumberModel(height, 0, Integer.MAX_VALUE, 1));
                unitBox.putClientProperty("last", Document.Unit.MM);
                break;
            case IN:
                width = ((double) ((int)(width*100)) )/100;
                height = ((double) ((int)(height * 100))) / 100;
                widthSpinner.setModel(new SpinnerNumberModel(width, 0, Integer.MAX_VALUE, 0.1));
                heightSpinner.setModel(new SpinnerNumberModel(height, 0, Integer.MAX_VALUE, 0.1));
                unitBox.putClientProperty("last", Document.Unit.IN);
                break;
        }

        ((SourceListCA)unitBox.getClientProperty("ca")).select(unitNow.name());
    }

    /**
     * When the value of the width or height spinner is changed, set the corresponding IntegerCA value.
     * @param spinner Width or height spinner.
     */
    public void onValueChange(Object spinner){
        JSpinner unitSpinner = (JSpinner) spinner;
        Document.Unit unit = (Document.Unit) ((ContainerItem)((JComboBox)unitSpinner.getClientProperty("unit"))
                .getSelectedItem()).getKey();
        double value = 1;
        //Get the value which can be Double or Integer
        if(unitSpinner.getValue() instanceof Double){
            value = (double)unitSpinner.getValue();
        }
        if(unitSpinner.getValue() instanceof Integer){
            value = (int)unitSpinner.getValue();
        }
        IntegerCA ca = (IntegerCA) unitSpinner.getClientProperty("ca");
        ca.setValue((int)(value * unit.getRatioToPixel()));
    }
}
