package org.eclipse.fx.ecp.handlers;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.emf.edit.domain.EditingDomain;

public class RedoHandler {

	private EditingDomain editingDomain;

	@CanExecute
	public boolean canRedo(@Optional EditingDomain editingDomain) {
		this.editingDomain = editingDomain;

		if (editingDomain != null)
			return editingDomain.getCommandStack().canRedo();

		return false;
	}

	@Execute
	public void redo() {
		editingDomain.getCommandStack().redo();
	}

}
