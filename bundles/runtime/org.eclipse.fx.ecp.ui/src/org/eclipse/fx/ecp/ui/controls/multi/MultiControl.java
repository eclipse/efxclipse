package org.eclipse.fx.ecp.ui.controls.multi;

import java.net.URL;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecp.edit.ECPControlContext;
import org.eclipse.emf.edit.command.MoveCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
import org.eclipse.fx.ecp.ui.Control;
import org.eclipse.fx.ecp.ui.ECPUIPlugin;
import org.eclipse.fx.ecp.ui.controls.ValidationMessage;
import org.osgi.framework.Bundle;

@SuppressWarnings("all")
public class MultiControl extends VBox implements Control {

	private ValidationMessage validationMessage = null;
	private EStructuralFeature feature;
	private EObject modelElement;
	private EditingDomain editingDomain;
	private EList<Object> values;
	private VBox controlsBox;
	protected final AdapterImpl modelElementAdapter;

	public MultiControl(final IItemPropertyDescriptor propertyDescriptor, final ECPControlContext context) {
		modelElement = context.getModelElement();
		editingDomain = context.getEditingDomain();
		feature = (EStructuralFeature) propertyDescriptor.getFeature(modelElement);
		values = (EList<Object>) modelElement.eGet(feature);

		// TODO move to css
		setSpacing(4);

		controlsBox = new VBox();
		getChildren().add(controlsBox);

		// TODO move to css
		controlsBox.setSpacing(4);

		for (int i = 0; i < values.size(); i++) {
			controlsBox.getChildren().add(createEmbeddedControl(propertyDescriptor, context, i));
		}

		// if (!(feature.getEType() instanceof EDataType))
		// return;

		if (feature.getEType() instanceof EEnum) {
			getChildren().add(new EnumAddControl(editingDomain, feature, modelElement));
		} else if (feature.getEType() instanceof EDataType) {
			getChildren().add(new TextFieldAddControl(editingDomain, feature, modelElement));
		} else if (feature.getEType() instanceof EObject) {
			EReference reference = (EReference) feature;
			if (reference.isContainment())
				getChildren().add(new ReferenceAddControl(editingDomain, reference, modelElement));
			else
				getChildren().add(new ReferenceDropControl(editingDomain, reference, modelElement));
		}

		modelElementAdapter = new AdapterImpl() {

			@Override
			public void notifyChanged(Notification msg) {

				if (msg.getFeature() == feature) {

					final int position = msg.getPosition();

					ObservableList<Node> children = controlsBox.getChildren();
					switch (msg.getEventType()) {
					case Notification.ADD:
						controlsBox.getChildren().add(createEmbeddedControl(propertyDescriptor, context, position));
						break;
					case Notification.REMOVE:
						children.remove(position);
						break;
					case Notification.MOVE:
					case Notification.SET:
					case Notification.UNSET:
						break;
					default:
						throw new UnsupportedOperationException();
					}

					for (int i = 0; i < children.size(); i++) {
						Node node = children.get(i);
						if (node instanceof AbstractEmbeddedControl)
							((AbstractEmbeddedControl) node).setIndex(i);
					}

				}

			}

		};

		modelElement.eAdapters().add(modelElementAdapter);

		validationMessage = new ValidationMessage();
		getChildren().add(validationMessage);
	}

	private AbstractEmbeddedControl createEmbeddedControl(final IItemPropertyDescriptor propertyDescriptor,
			final ECPControlContext context, int i) {

		if (feature instanceof EReference) {
			return new EmbeddedReferenceControl(propertyDescriptor, context, i);
		} else if (feature.getEType() instanceof EEnum) {
			return new EmbeddedEnumControl(propertyDescriptor, context, i);
		} else {
			return new EmbeddedTextFieldControl(propertyDescriptor, context, i);
		}

	}

	private void updateIndices(int first, int last, int offset) {
		for (int i = first; i <= last; i++) {
			ControlWrapper control = (ControlWrapper) controlsBox.getChildren().get(i);
			control.setIndex(control.getIndex() + offset);
		}
	}

	public static ImageView getImage(String resourcePath) {
		Bundle bundle = Platform.getBundle(ECPUIPlugin.PLUGIN_ID);
		Path path = new Path(resourcePath);
		URL url = FileLocator.find(bundle, path, null);
		return new ImageView(url.toExternalForm());
	}

	@Override
	public void handleValidation(Diagnostic diagnostic) {
		if (diagnostic.getSeverity() != Diagnostic.OK) {
			validationMessage.setMessage(diagnostic.getMessage());
		} else {
			resetValidation();
		}
	}

	@Override
	public void resetValidation() {
		validationMessage.setMessage(null);
	}

	public static class Factory implements Control.Factory {

		@Override
		public Control createControl(IItemPropertyDescriptor itemPropertyDescriptor, ECPControlContext context) {
			return new MultiControl(itemPropertyDescriptor, context);
		}

	}

	class ControlWrapper extends HBox {

		private final EmbeddedControl embeddedControl;
		private Button upButton;
		private Button downButton;
		private int index;

		public ControlWrapper(IItemPropertyDescriptor propertyDescriptor, ECPControlContext context, int initialIndex) {
			index = initialIndex;

			setFillHeight(true);

			if (feature instanceof EReference) {
				embeddedControl = new EmbeddedReferenceControl(propertyDescriptor, context, initialIndex);
			} else if (feature.getEType() instanceof EEnum) {
				embeddedControl = new EmbeddedEnumControl(propertyDescriptor, context, initialIndex);
			} else {
				embeddedControl = new EmbeddedTextFieldControl(propertyDescriptor, context, initialIndex);
			}

			((Node) embeddedControl).getStyleClass().add("left-pill");

			HBox.setHgrow((Node) embeddedControl, Priority.ALWAYS);
			getChildren().add((Node) embeddedControl);

			if (feature.isOrdered()) {

				upButton = new Button();
				getChildren().add(upButton);
				upButton.getStyleClass().addAll("moveUpButton", "center-pill");
				upButton.setOnAction(new EventHandler<ActionEvent>() {

					@Override
					public void handle(ActionEvent arg0) {
						Command command = MoveCommand.create(editingDomain, modelElement, feature, values.get(index), index - 1);
						if (command.canExecute())
							editingDomain.getCommandStack().execute(command);
					}

				});

				downButton = new Button();
				downButton.getStyleClass().addAll("moveDownButton", "center-pill");
				getChildren().add(downButton);
				downButton.setOnAction(new EventHandler<ActionEvent>() {

					@Override
					public void handle(ActionEvent arg0) {
						Command command = MoveCommand.create(editingDomain, modelElement, feature, values.get(index), index + 1);
						if (command.canExecute())
							editingDomain.getCommandStack().execute(command);
					}

				});

			}

			final Button deleteButton = new Button();
			getChildren().add(deleteButton);
			deleteButton.getStyleClass().addAll("deleteButton", "right-pill");
			deleteButton.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent arg0) {
					Command command = RemoveCommand.create(editingDomain, modelElement, feature, values.get(index));
					if (command.canExecute())
						editingDomain.getCommandStack().execute(command);
				}

			});

			updateButtons();
		}

		public int getIndex() {
			return index;
		}

		public void setIndex(int index) {
			this.index = index;
			embeddedControl.setIndex(index);
		}

		public void updateButtons() {
			if (upButton != null)
				upButton.setDisable(index < 1);

			if (downButton != null)
				downButton.setDisable(index > values.size() - 2);
		}

	}

	@Override
	public void dispose() {
		modelElement.eAdapters().add(modelElementAdapter);
	}

}
