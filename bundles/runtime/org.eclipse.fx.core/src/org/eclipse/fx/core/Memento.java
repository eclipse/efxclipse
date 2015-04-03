/*******************************************************************************
 * Copyright (c) 2015 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl<tom.schindl@bestsolution.at> - initial API and implementation
 *******************************************************************************/
package org.eclipse.fx.core;

/**
 * A simple storage API for states that need to be stored
 * 
 * @since 2.0
 */
public interface Memento {
	/**
	 * Store a string value
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	public void put(String key, String value);

	/**
	 * Store a boolean value
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	public void put(String key, boolean value);

	/**
	 * Store an integer value
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	public void put(String key, int value);

	/**
	 * Store a double value
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	public void put(String key, double value);

	/**
	 * Remove the given key
	 * 
	 * @param key
	 *            the key
	 */
	public void remove(String key);

	/**
	 * Check if the given key exists
	 * 
	 * @param key
	 *            the key
	 * @return <code>true</code> if exists else <code>false</code>
	 */
	public boolean exists(String key);

	/**
	 * Retrieve the value for the given key
	 * 
	 * @param key
	 *            the key
	 * @param defaultValue
	 *            the default value if the key does not exists or is
	 *            <code>null</code>
	 * @return the value or the default value provided
	 */
	public String get(String key, String defaultValue);

	/**
	 * Retrieve the value for the given key
	 * 
	 * @param key
	 *            the key
	 * @param defaultValue
	 *            the default value of the key does not exits, is
	 *            <code>null</code> or not a boolean
	 * @return the value or the default value provided
	 */
	public boolean get(String key, boolean defaultValue);

	/**
	 * Retrieve the value for the given key
	 * 
	 * @param key
	 *            the value
	 * @param defaultValue
	 *            the default value of the key does not exits, is
	 *            <code>null</code> or not an int
	 * @return the value or the default value provided
	 */
	public int get(String key, int defaultValue);
	
	/**
	 * Retrieve the value for the given key
	 * @param key the key
	 * @param defaultValue the default value of the key does not exits, is
	 *            <code>null</code> or not a double
	 * @return the value or the default value provided
	 */
	public double get(String key, double defaultValue);
}
