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

package org.orbisgis.mapcomposer.model.configurationattribute.attribute;

import org.orbisgis.mapcomposer.controller.MainController;
import org.orbisgis.mapcomposer.model.configurationattribute.interfaces.ConfigurationAttribute;
import org.orbisgis.mapcomposer.model.configurationattribute.interfaces.ListCA;
import org.orbisgis.mapcomposer.model.configurationattribute.interfaces.RefreshCA;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * This class represent a list of source and extends the BaseListCA abstract class.
 * A source can be a list of path to files or a list of values (like up, down, left, right).
 * @see org.orbisgis.mapcomposer.model.configurationattribute.attribute.BaseListCA
 *
 * @author Sylvain PALOMINOS
 */
public class SourceListCA extends BaseListCA<String> implements RefreshCA{
    /** Index of the value selected.*/
    private int index;

    /** Property itself */
    private List<String> list;

    /**
     * Empty constructor used in the SaveHandler, on loading a project save.
     */
    public SourceListCA(){
        super("void", false);
        this.list  = new ArrayList<>();
    }

    /**
     * Default constructor for the SourceListCA.
     * @param name Name of the ConfigurationAttribute
     * @param readOnly Boolean to enable or not the modification of the CA
     */
    public SourceListCA(String name, boolean readOnly){
        super(name, readOnly);
        index = 0;
        list = new ArrayList<>();
    }

    @Override public void setValue(List<String> value) {this.list=value;}
    @Override public List<String> getValue() {return list;}

    @Override public boolean isSameValue(ConfigurationAttribute configurationAttribute) {
        if(configurationAttribute instanceof ListCA){
            if(getSelected()==null)
                return false;
            return getSelected().equals(((ListCA) configurationAttribute).getSelected());
        }
        return false;
    }

    @Override public void       add(String value)       {list.add(value);}
    @Override public boolean    remove(String value)    {return this.list.remove(value);}
    
    @Override public String     getSelected()           {return list.get(index);}
    @Override public void       select(String choice)   {index=list.indexOf(choice);}

    @Override
    public void refresh(MainController uic) {
        List<String> temp = new ArrayList<>();
        for(String s : list)
            if(!temp.contains(s)) temp.add(s);
        list=temp;
    }

    @Override
    public void setField(String name, String value) {
        super.setField(name, value);
        if(name.equals("list"))
            list= Arrays.asList(value.split(","));
        if(name.equals("index"))
            index=Integer.parseInt(value);
    }

    @Override
    public Map<String, Object> getAllFields() {
        Map ret = super.getAllFields();
        ret.put("index", index);
        String s="";
        for(String str : list)
            s+=str+",";
        if(list.size()>0)s=s.substring(0, s.length()-1);
        ret.put("list", s);
        return ret;
    }

    @Override
    public ConfigurationAttribute deepCopy() {
        SourceListCA copy = new SourceListCA();
        List<String> list = new ArrayList<>();
        for(String s : getValue())
        list.add(s);
        copy.setValue(list);
        copy.select(this.getSelected());
        copy.setReadOnly(this.getReadOnly());
        copy.setName(this.getName());

        return copy;
    }
}
