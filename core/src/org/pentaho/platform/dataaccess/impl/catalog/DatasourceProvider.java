/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.dataaccess.impl.catalog;

import java.util.List;

import org.pentaho.platform.dataaccess.api.catalog.IDatasource;
import org.pentaho.platform.dataaccess.api.catalog.IDatasourceProvider;
import org.pentaho.platform.dataaccess.api.catalog.IDatasourceType;

/**
 * An implementation of IDatasourceProvider where one instance represents one type of datasource that has been
 * registered by the system.
 * 
 * @author wseyler
 */
public class DatasourceProvider implements IDatasourceProvider {

  private List<IDatasource> datasources;
  private IDatasourceType type;

  public DatasourceProvider() {
    super();
  }

  public DatasourceProvider( IDatasourceType type ) {
    this();
    this.type = type;
  }

  public DatasourceProvider( IDatasourceType type, List<IDatasource> datasources ) {
    this( type );
    this.datasources = datasources;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.platform.dataaccess.catalog.api.IDatasourceProvider#getDatasources()
   */
  @Override
  public List<IDatasource> getDatasources() {
    return datasources;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.platform.dataaccess.catalog.api.IDatasourceProvider#getType()
   */
  @Override
  public IDatasourceType getType() {
    return type;
  }

  public void setDatasources( List<IDatasource> datasources ) {
    this.datasources = datasources;
  }

  public void addDatasource( IDatasource datasource ) {
    datasources.add( datasource );
  }

  public void setType( IDatasourceType type ) {
    this.type = type;
  }

}
