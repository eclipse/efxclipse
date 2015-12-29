/*******************************************************************************
 * Copyright (c) 2015 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	Tom Schindl<tom.schindl@bestsolution.at> - initial API and implementation
 *******************************************************************************/
package org.eclipse.fx.ui.controls.list;

import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;

/**
 * Utility to set up a {@link ComboBox} and {@link ListView}
 *
 * @since 2.3.0
 */
public class ListComboUtil {
	/**
	 * Setup a combo box
	 *
	 * @param comboBox
	 *            the combo box
	 * @param items
	 *            the items
	 * @param labelExtractor
	 *            function to extract the label
	 * @return the combo box passed in
	 */
	public static <T> ComboBox<T> setupCombo(ComboBox<T> comboBox, ObservableList<T> items, @NonNull Function<@Nullable T, @Nullable CharSequence> labelExtractor) {
		comboBox.setCellFactory(v -> new SimpleListCell<T>(labelExtractor));
		comboBox.setButtonCell(new SimpleListCell<>(labelExtractor));
		comboBox.setItems(items);
		return comboBox;
	}
}
