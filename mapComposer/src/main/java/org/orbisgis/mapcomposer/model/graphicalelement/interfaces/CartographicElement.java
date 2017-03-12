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

package org.orbisgis.mapcomposer.model.graphicalelement.interfaces;

import org.orbisgis.coremap.layerModel.OwsMapContext;

/**
 * This interface extends the GraphicalElement interface and represent a cartographic element based on a OwsMapContext file.
 * It contain the basics filed of a GraphicalElement plus an OwsMapContext (OwsContextCA) which contain all the data from the map created in OrbisGIS.
 * To simply the use of this interface, the OwsMapContext is set by giving its path.
 *
 * @author Sylvain PALOMINOS
 */
public interface CartographicElement extends GraphicalElement{

    /**
     * Returns the OwsMapContext.
     * @return The OwsMapContext.
     */
    public OwsMapContext getOwsMapContext();

    /**
     * Returns the OwsContext path.
     * @return The OwsContext path.
     */
    public String getOwsPath();
    
    /**
     * Sets the OwsContext path.
     * @param owsContext New OwsContext path.
     */
    public void setOwsContext(String owsContext);
}
