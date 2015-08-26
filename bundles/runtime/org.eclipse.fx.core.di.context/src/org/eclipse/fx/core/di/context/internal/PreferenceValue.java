/*******************************************************************************
 * Copyright (c) 2013 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *******************************************************************************/
package org.eclipse.fx.core.di.context.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.fx.core.Callback;
import org.eclipse.fx.core.Subscription;
import org.eclipse.fx.core.di.ScopedObjectFactory;
import org.eclipse.fx.core.log.Log;
import org.eclipse.fx.core.log.Logger;
import org.eclipse.fx.core.preferences.Value;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Implementation of a ContextBoundValue
 *
 * @param <T>
 *            the type
 */
public class PreferenceValue<T> implements Value<T> {
	@Nullable
	private IEclipsePreferences preference;
	@Nullable
	private String contextKey;
	@Nullable
	List<Callback<T>> callbacks;
	@Nullable
	List<Callback<Void>> disposalCallbacks;
	@Nullable
	private T value;

	@Inject
	@Optional
	@Nullable
	IEventBroker eventBroker;

	@Inject
	@Log
	private Logger logger;

	@Nullable
	private IPreferenceChangeListener listener;

	/**
	 * Initialize the value
	 *
	 * @param contextKey
	 *            the context key
	 * @param preference
	 *            the preference node
	 * @param cl
	 *            the type
	 */
	@SuppressWarnings("unchecked")
	public void init(@NonNull String contextKey, @NonNull IEclipsePreferences preference, Class<?> cl) {
		this.contextKey = contextKey;
		this.preference = preference;
		this.contextKey = contextKey;

		IPreferenceChangeListener listener = event -> {
			if( contextKey.equals(event.getKey()) ) {
				setCurrentValue((@Nullable T) PreferenceValueSupplier.getValue(preference, contextKey, cl));
			}
		};
		preference.addPreferenceChangeListener(listener);
		this.listener = listener;
		setCurrentValue((@Nullable T) PreferenceValueSupplier.getValue(preference, contextKey, cl));
	}

	@SuppressWarnings("unchecked")
	void setCurrentValue(@Nullable T o) {
		this.value = o;
		if (this.callbacks != null) {
			for (Callback<?> c : this.callbacks.toArray(new Callback<?>[0])) {
				try {
					((Callback<T>) c).call(o);
				} catch (Throwable t) {
					this.logger.error("Failed while executing callback", t); //$NON-NLS-1$
				}
			}
		}
	}

	@Override
	@Nullable
	public T getValue() {
		return this.value;
	}

	@Override
	public void publish(@Nullable T value) {
		IEclipsePreferences preference = this.preference;
		if (preference == null) {
			throw new IllegalStateException("The backing IEclipsePreference is null"); //$NON-NLS-1$
		}

		if (value == null) {
			preference.remove(this.contextKey);
		} else {
			preference.put(this.contextKey, value.toString());
			try {
				preference.flush();
			} catch (BackingStoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (this.eventBroker != null) {
			this.eventBroker.send(ScopedObjectFactory.KEYMODIFED_TOPIC,
					Collections.singletonMap(this.contextKey, value));
		}
	}

	@Override
	public Subscription subscribeOnValueChange(final Callback<T> callback) {
		if (this.callbacks == null) {
			this.callbacks = new ArrayList<Callback<T>>();
		}

		if (this.callbacks != null) {
			this.callbacks.add(callback);
		}

		return new Subscription() {

			@Override
			public void dispose() {
				List<Callback<T>> callbacks = PreferenceValue.this.callbacks;
				if (callbacks != null) {
					callbacks.remove(callback);
				}
			}
		};
	}

	@Override
	public Subscription subscribeOnDispose(final Callback<Void> callback) {
		if (this.disposalCallbacks == null) {
			this.disposalCallbacks = new ArrayList<Callback<Void>>();
		}
		if (this.disposalCallbacks != null) {
			this.disposalCallbacks.add(callback);
		}

		return new Subscription() {

			@Override
			public void dispose() {
				List<Callback<Void>> disposalCallbacks = PreferenceValue.this.disposalCallbacks;
				if (disposalCallbacks != null) {
					disposalCallbacks.remove(callback);
				}
			}
		};
	}

	// @SuppressWarnings("null")
	// @Override
	// public <A> A adaptTo(@NonNull Class<A> adapt) {
	// return this.adapterService.adapt(this, adapt, new
	// ValueAccessImpl(this.preference));
	// }
	//
	// @Override
	// public boolean canAdaptTo(Class<?> adapt) {
	// return this.adapterService.canAdapt(this, adapt);
	// }

	@PreDestroy
	void dispose() {
		List<Callback<Void>> disposalCallbacks = this.disposalCallbacks;
		if (disposalCallbacks != null) {
			for (Callback<?> callback : disposalCallbacks.toArray(new Callback<?>[0])) {
				try {
					callback.call(null);
				} catch (Throwable t) {
					this.logger.error("Failure while executing clean up callback", t); //$NON-NLS-1$
				}

			}
			disposalCallbacks.clear();
		}
		if (this.callbacks != null) {
			this.callbacks.clear();
		}
		this.value = null;
		IEclipsePreferences preference = this.preference;
		IPreferenceChangeListener listener = this.listener;
		if( preference != null && listener != null ) {
			preference.removePreferenceChangeListener(listener);
		}
		this.preference = null;
		this.listener = null;
	}

	// static class ValueAccessImpl implements ValueAccess {
	// private final IEclipsePreferences context;
	//
	// public ValueAccessImpl(IEclipsePreferences context) {
	// this.context = context;
	// }
	//
	// @SuppressWarnings("unchecked")
	// @Override
	// public <O> O getValue(String key) {
	// return (O) this.context.get(key);
	// }
	//
	// @SuppressWarnings("null")
	// @Override
	// public <O> O getValue(@NonNull Class<O> key) {
	// return this.context.get(key);
	// }
	//
	// }
}