/*
 * OrbisWPS contains a set of libraries to build a Web Processing Service (WPS)
 * compliant with the 2.0 specification.
 *
 * OrbisWPS is part of the OrbisGIS platform
 *
 * OrbisGIS is a java GIS application dedicated to research in GIScience.
 * OrbisGIS is developed by the GIS group of the DECIDE team of the
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
 * OrbisWPS is distributed under GPL 3 license.
 *
 * Copyright (C) 2015-2017 CNRS (Lab-STICC UMR CNRS 6285)
 *
 *
 * OrbisWPS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * OrbisWPS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * OrbisWPS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.orbisgis.groovy;

import groovy.lang.Closure;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Groovy closure used to ba able to cancel a running process.
 *
 * @author Sylvain PALOMINOS
 */
public class CancelClosure extends Closure {

    private boolean wasRunningStatement = false;

    private List<Statement> statementList;

    public CancelClosure(Object owner) {
        super(owner);
        statementList = new ArrayList<>();
    }

    /**
     * Called method by groovy.
     * @param stmt
     */
    public void doCall(Statement stmt){
        statementList.add(stmt);
    }

    /**
     * Cancel all the running sql queries.
     */
    public void cancel(){
        for(Statement stmt : statementList){
            try {
                stmt.cancel();
            } catch (SQLException ignored) {
                wasRunningStatement = true;
            }
        }
    }

    public boolean wasRunningStatement(){
        return wasRunningStatement;
    }
}
