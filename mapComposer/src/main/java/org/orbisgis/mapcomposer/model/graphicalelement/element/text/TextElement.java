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

package org.orbisgis.mapcomposer.model.graphicalelement.element.text;

import org.orbisgis.mapcomposer.model.configurationattribute.interfaces.ConfigurationAttribute;
import org.orbisgis.mapcomposer.model.configurationattribute.attribute.ColorCA;
import org.orbisgis.mapcomposer.model.configurationattribute.attribute.IntegerCA;
import org.orbisgis.mapcomposer.model.configurationattribute.attribute.SourceListCA;
import org.orbisgis.mapcomposer.model.configurationattribute.attribute.StringCA;
import org.orbisgis.mapcomposer.model.graphicalelement.element.SimpleGE;
import org.orbisgis.mapcomposer.model.graphicalelement.interfaces.GEProperties;
import org.orbisgis.mapcomposer.model.graphicalelement.interfaces.GraphicalElement;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import java.util.ArrayList;
import java.util.List;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;

/**
 * GraphicalElement displaying a text. Several aspects can be defined such as the text color, the font,the font size ...
 *
 * @author Sylvain PALOMINOS
 */
public class TextElement extends SimpleGE implements GEProperties{
    /** Fonts allowed */
    private SourceListCA font;
    /** Color of the Text */
    private ColorCA colorText;
    /** Color of the background */
    private ColorCA colorBack;
    /** Alignment of the text */
    private SourceListCA alignment;
    /** Style (plain, italic, bold) of the text */
    private SourceListCA style;
    /** Size of the font */
    private IntegerCA fontSize;
    /** Text itself */
    private StringCA text;
    /** Alpha (transparency value) */
    private IntegerCA alpha;

    /** Object for the translation*/
    private static final I18n i18n = I18nFactory.getI18n(TextElement.class);

    /** Displayed name of the text*/
    public static final String sText = I18n.marktr("Text");
    /** Displayed name of the text color*/
    public static final String sTextColor = I18n.marktr("Text color");
    /** Displayed name of the background color*/
    public static final String sBackColor = I18n.marktr("Background color");
    /** Displayed name of the alignment*/
    public static final String sAlignment = I18n.marktr("Alignment");
    /** Displayed name of the style*/
    public static final String sStyle = I18n.marktr("Style");
    /** Displayed name of the alpha*/
    public static final String sAlpha = I18n.marktr("Alpha");
    /** Displayed name of the font*/
    public static final String sFont = I18n.marktr("Font");
    /** Displayed name of the font size*/
    public static final String sFontSize = I18n.marktr("Font size");


    /**
     * Public main constructor.
     */
    public TextElement(){
        //ConfigurationAttribute instantiation
        this.font = new SourceListCA(sFont, false);
        this.colorText = new ColorCA(sTextColor, false, Color.BLACK);
        this.colorBack = new ColorCA(sBackColor, false, Color.WHITE);
        this.alignment = new SourceListCA(sAlignment, false);
        this.style = new SourceListCA(sStyle, false);
        this.fontSize = new IntegerCA(sFontSize, false, 12, true, 1, 99999);
        this.text = new StringCA(sText, false, i18n.tr("Some text"));
        this.alpha = new IntegerCA(sAlpha, false, 0, true, 0, 100);
        //ConfigurationAttribute initialisation
        for(String s : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()){
            this.font.add(s);
        }
        this.font.select(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()[0]);
        this.alignment.add(Alignment.LEFT.name());
        this.alignment.add(Alignment.CENTER.name());
        this.alignment.add(Alignment.RIGHT.name());
        this.alignment.select(Alignment.CENTER.name());
        this.style.add(Style.PLAIN.name());
        this.style.add(Style.ITALIC.name());
        this.style.add(Style.BOLD.name());
        this.style.select(Style.PLAIN.name());


        /** Mark for translation Font style strings **/
        I18n.marktr("PLAIN");
        I18n.marktr("ITALIC");
        I18n.marktr("BOLD");
        /** Mark for translation Text alignment strings **/
        I18n.marktr("LEFT");
        I18n.marktr("CENTER");
        I18n.marktr("RIGHT");
    }

    @Override
    public String getGEName(){
        return i18n.tr("Text");
    }

    @Override
    public List<ConfigurationAttribute> getAllAttributes() {
        List<ConfigurationAttribute> list = new ArrayList<>();
        list.add(font);
        list.add(colorBack);
        list.add(colorText);
        list.add(alignment);
        list.add(style);
        list.add(fontSize);
        list.add(text);
        list.add(alpha);
        return list;
    }

    @Override
    public List<ConfigurationAttribute> getSavableAttributes() {
        List<ConfigurationAttribute> list = super.getSavableAttributes();
        list.add(font);
        list.add(colorBack);
        list.add(colorText);
        list.add(alignment);
        list.add(style);
        list.add(fontSize);
        list.add(text);
        list.add(alpha);
        return list;
    }

    @Override
    public GraphicalElement deepCopy() {
        TextElement copy = (TextElement)super.deepCopy();
        copy.alpha = (IntegerCA) this.alpha.deepCopy();
        copy.text = (StringCA) this.text.deepCopy();
        copy.colorText = (ColorCA) this.colorText.deepCopy();
        copy.colorBack = (ColorCA) this.colorBack.deepCopy();
        copy.fontSize = (IntegerCA) this.fontSize.deepCopy();
        copy.alignment = (SourceListCA) this.alignment.deepCopy();
        copy.style = (SourceListCA) this.style.deepCopy();
        copy.font = (SourceListCA) this.font.deepCopy();

        return copy;
    }

    @Override
    public void setAttribute(ConfigurationAttribute ca) {
        super.setAttribute(ca);
        if(ca.getName().equals(sFont))
            font=(SourceListCA)ca;
        if(ca.getName().equals(sTextColor))
            colorText=(ColorCA)ca;
        if(ca.getName().equals(sBackColor))
            colorBack=(ColorCA)ca;
        if(ca.getName().equals(sAlignment))
            alignment=(SourceListCA)ca;
        if(ca.getName().equals(sStyle))
            style=(SourceListCA)ca;
        if(ca.getName().equals(sFontSize))
            fontSize=(IntegerCA)ca;
        if(ca.getName().equals(sText))
            text=(StringCA)ca;
        if(ca.getName().equals(sAlpha))
            alpha=(IntegerCA)ca;
    }

    /**
     * Add a font available.
     * @param font A new font.
     */
    public void addFont(String font) {this.font.add(font);}

    /**
     * Sets the color of the text.
     * @param color The new color.
     */
    public void setColorText(Color color) {this.colorText.setValue(color);}

    /**
     * Sets the color of the background.
     * @param color The new color.
     */
    public void setColorBack(Color color) {this.colorBack.setValue(color);}

    /**
     * Sets the font size of the text.
     * @param fontSize The new font size.
     */
    public void setFontSize(int fontSize) {this.fontSize.setValue(fontSize);}

    /**
     * Sets the text of the element
     * @param text The new text.
     */
    public void setText(String text) {this.text.setValue(text);}

    /**
     * Sets the alpha value of the element
     * @param alpha The new value.
     */
    public void setAlpha(int alpha) {this.alpha.setValue(alpha);}

    /**
     * Sets the style of the element
     * @param style The new style.
     */
    public void setStyle(Style style) {this.style.select(style.name());}

    /**
     * Sets the alignment of the text
     * @param alignment The new alignment.
     */
    public void setAlignment(Alignment alignment) {this.alignment.select(alignment.name());}
    
    
    /**
     * Returns the selected font.
     * @return font The font selected.
     */
    public String getFont() {return this.font.getSelected();}

    /**
     * Returns the text color.
     * @return The color of the text.
     */
    public Color getColorBack() {return this.colorBack.getValue();}

    /**
     * Returns the background color.
     * @return The color of the background.
     */
    public Color getColorText() {return this.colorText.getValue();}

    /**
     * Returns the selected alignment.
     * @return The alignment selected.
     */
    public Alignment getAlignment() {return Alignment.valueOf(this.alignment.getSelected());}

    /**
     * Returns the selected stle.
     * @return The style selected.
     */
    public int getStyle() {return Style.valueOf(this.style.getSelected()).getFontStyle();}

    /**
     * Returns the font size.
     * @return The font size.
     */
    public Integer getFontSize() {return this.fontSize.getValue();}

    /**
     * Returns text of the element.
     * @return The text of the element.
     */
    public String getText() {return this.text.getValue();}

    /**
     * Returns the alpha value of the element.
     * @return The alpha value of the element.
     */
    public int getAlpha() {return this.alpha.getValue();}

    @Override
    public boolean isDocumentNeeded() {
        return true;
    }

    @Override
    public boolean isAlwaysOnTop() {
        return false;
    }

    @Override
    public boolean isAlwaysOnBack() {
        return false;
    }

    @Override
    public boolean isDrawnByUser() {
        return true;
    }

    @Override
    public boolean isAlwaysRefreshed() {
        return true;
    }

    @Override
    public boolean isAlwaysCentered() {
        return false;
    }

    @Override
    public boolean isEditedByMouse() {
        return true;
    }

    /**
     * Enumeration for the text alignment.
     */
    public static enum Alignment{ LEFT, CENTER, RIGHT }
    
    /**
     * Enumeration for the text font style.
     */
    public static enum Style{

        PLAIN(Font.PLAIN),
        ITALIC(Font.ITALIC),
        BOLD(Font.BOLD);

        /**Font style value.*/
        private final int value;

        /**Enum constructor*/
        private Style(final int value) {this.value = value;}

        /**
         * Returns the font style.
         * @return The font style.
         */
        public int getFontStyle() {return value;}
    }
}
