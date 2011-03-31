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
 * Copyright (c) 2011 Pentaho Corporation..  All rights reserved.
 * 
 * @author Ezequiel Cuellar
 */

package org.pentaho.platform.dataaccess.datasource.wizard;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.wizard.models.*;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.JoinSelectionServiceGwtImpl;
import org.pentaho.platform.dataaccess.datasource.wizard.sources.multitable.MultiTableDatasource;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulMenuList;
import org.pentaho.ui.xul.containers.XulListbox;
import org.pentaho.ui.xul.containers.XulVbox;
import org.pentaho.ui.xul.gwt.binding.GwtBindingFactory;
import org.pentaho.ui.xul.stereotype.Bindable;
import org.pentaho.ui.xul.util.AbstractModelList;

@SuppressWarnings("unchecked")
public class JoinDefinitionStepController extends AbstractWizardStep implements PropertyChangeListener {

	protected static final String JOIN_DEFINITION_PANEL_ID = "joinDefinitionWindow";

	private XulVbox joinDefinitionDialog;
	private JoinGuiModel joinGuiModel;
	private XulListbox leftTables;
	private XulListbox rightTables;
	private XulListbox joins;
	private XulMenuList<JoinFieldModel> leftKeyFieldList;
	private XulMenuList<JoinFieldModel> rightKeyFieldList;
	private JoinSelectionServiceGwtImpl joinSelectionServiceGwtImpl;
	private IConnection selectedConnection;

	public JoinDefinitionStepController(JoinGuiModel joinGuiModel, JoinSelectionServiceGwtImpl joinSelectionServiceGwtImpl, IConnection selectedConnection, MultiTableDatasource parentDatasource) {
		super(parentDatasource);
		this.joinGuiModel = joinGuiModel;
		this.joinSelectionServiceGwtImpl = joinSelectionServiceGwtImpl;
		this.selectedConnection = selectedConnection;
	}

	public void propertyChange(PropertyChangeEvent evt) {
		final XulListbox xulListbox = (XulListbox) evt.getSource();
		final JoinTableModel table = (JoinTableModel) xulListbox.getSelectedItem();
		if (table != null) {
			joinSelectionServiceGwtImpl.getTableFields(table.getName(), selectedConnection, new XulServiceCallback<List>() {
				public void error(String message, Throwable error) {
					error.printStackTrace();
				}

				public void success(List fields) {
					List<JoinFieldModel> fieldModels = table.processTableFields(fields);
					table.setFields(new AbstractModelList<JoinFieldModel>(fieldModels));
					if (xulListbox.equals(leftTables)) {
						leftKeyFieldList.setElements(fields);
					}
					if (xulListbox.equals(rightTables)) {
						rightKeyFieldList.setElements(fields);
					}
				}
			});
		}
	}

	public String getName() {
		return "joinDefinitionStepController";
	}

	@Bindable
	public void createJoin() {
		JoinModel join = new JoinModel();
		join.setLeftKeyFieldModel(this.joinGuiModel.getLeftKeyField());
		join.setRightKeyFieldModel(this.joinGuiModel.getRightKeyField());

		// validate against duplicate joins.
		// and
		// validate against joining to the same table.
		if (notDuplicate(join) && notSelfJoin(join)) {
			this.joinGuiModel.addJoin(join);
		}

		// validate all tables have been used.
		if (allTablesJoined()) {
			parentDatasource.setFinishable(true);
		}
	}

	private boolean notDuplicate(JoinModel newJoin) {
		boolean joinExists = true;
		for (JoinModel join : this.joinGuiModel.getJoins()) {
			if (newJoin.equals(join)) {
				joinExists = false;
				break;
			}
		}
		return joinExists;
	}

	private boolean notSelfJoin(JoinModel newJoin) {
		return !newJoin.getLeftKeyFieldModel().getParentTable().equals(newJoin.getRightKeyFieldModel().getParentTable());
	}

	private boolean allTablesJoined() {

		boolean allTablesJoined = true;
		next: for (JoinTableModel table : this.joinGuiModel.getSelectedTables()) {
			for (JoinModel join : this.joinGuiModel.getJoins()) {
				JoinTableModel table1 = join.getLeftKeyFieldModel().getParentTable();
				JoinTableModel table2 = join.getRightKeyFieldModel().getParentTable();
				if (table.equals(table1) || table.equals(table2)) {
					continue next;
				}
			}
			allTablesJoined = false;
			break;
		}
		return allTablesJoined;
	}

	@Bindable
	public void deleteJoin() {
		this.joinGuiModel.removeSelectedJoin();
		parentDatasource.setFinishable(this.allTablesJoined());
	}

	@Override
	public void init(IWizardModel wizardModel) throws XulException {

		this.joinDefinitionDialog = (XulVbox) document.getElementById(JOIN_DEFINITION_PANEL_ID);
		this.joins = (XulListbox) document.getElementById("joins");
		this.leftKeyFieldList = (XulMenuList<JoinFieldModel>) document.getElementById("leftKeyField");
		this.rightKeyFieldList = (XulMenuList<JoinFieldModel>) document.getElementById("rightKeyField");

		this.leftTables = (XulListbox) document.getElementById("leftTables");
		this.leftTables.addPropertyChangeListener(this);

		this.rightTables = (XulListbox) document.getElementById("rightTables");
		this.rightTables.addPropertyChangeListener(this);

		super.init(wizardModel);
	}

	public void setBindings() {

		BindingFactory bf = new GwtBindingFactory(document);
		bf.createBinding(this.joinGuiModel.getSelectedTables(), "children", this.leftTables, "elements");
		bf.createBinding(this.joinGuiModel.getSelectedTables(), "children", this.rightTables, "elements");
		bf.createBinding(this.leftTables, "selectedItem", this.joinGuiModel, "leftJoinTable");
		bf.createBinding(this.rightTables, "selectedItem", this.joinGuiModel, "rightJoinTable");
		bf.createBinding(this.joinGuiModel.getJoins(), "children", this.joins, "elements");
		bf.createBinding(this.joins, "selectedItem", this.joinGuiModel, "selectedJoin");
		bf.createBinding(this.leftKeyFieldList, "selectedIndex", this.joinGuiModel, "leftKeyField", new BindingConvertor<Integer, JoinFieldModel>() {

			@Override
			public JoinFieldModel sourceToTarget(final Integer index) {
				if (index == -1) {
					return null;
				}
				return joinGuiModel.getLeftJoinTable().getFields().get(index);
			}

			@Override
			public Integer targetToSource(final JoinFieldModel value) {
				return null;
			}
		});

		bf.createBinding(this.rightKeyFieldList, "selectedIndex", this.joinGuiModel, "rightKeyField", new BindingConvertor<Integer, JoinFieldModel>() {

			@Override
			public JoinFieldModel sourceToTarget(final Integer index) {
				if (index == -1) {
					return null;
				}
				return joinGuiModel.getRightJoinTable().getFields().get(index);
			}

			@Override
			public Integer targetToSource(final JoinFieldModel value) {
				return null;
			}
		});
	}

	@Override
	public void stepActivatingForward() {
		if (this.joins.getElements() != null) {
			parentDatasource.setFinishable(this.allTablesJoined());
		}
	}

	public String getStepName() {
		return "Define Joins";
	}

	public XulComponent getUIComponent() {
		return this.joinDefinitionDialog;
	}
}