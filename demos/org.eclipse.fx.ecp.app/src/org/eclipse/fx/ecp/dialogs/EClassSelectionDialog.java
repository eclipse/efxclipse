package org.eclipse.fx.ecp.dialogs;

import java.io.IOException;
import java.net.URL;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.edit.domain.EditingDomain;

public class EClassSelectionDialog extends Stage {

	public EClassSelectionDialog(Resource resource, EditingDomain editingDomain) {

		URL location = getClass().getResource("EClassSelectionDialog.fxml");
		
		FXMLLoader fxmlLoader = new FXMLLoader(location);

		fxmlLoader.setController(new NewModelElementDialogController(this, resource, editingDomain));

		Pane root = null;
		
		try {
			root = (Pane) fxmlLoader.load();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		setTitle("New Model Element");

		initModality(Modality.APPLICATION_MODAL);

		Scene scene = new Scene(root);

		setScene(scene);
		
	}

}
