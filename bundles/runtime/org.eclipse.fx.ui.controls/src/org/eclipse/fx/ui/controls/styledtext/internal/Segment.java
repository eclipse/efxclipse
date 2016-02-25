package org.eclipse.fx.ui.controls.styledtext.internal;

import java.util.List;

public class Segment {
	String text;
	List<String> styleClasses;

	public String getText() {
		return text;
	}

	public List<String> getStyleClasses() {
		return styleClasses;
	}

	public Segment(String text, List<String> styleClasses) {
		this.text = text;
		this.styleClasses = styleClasses;
	}

	@Override
	public String toString() {
		return "Segment(" + this.text + ", " + this.styleClasses + ")";   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((styleClasses == null) ? 0 : styleClasses.hashCode());
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Segment other = (Segment) obj;
		if (styleClasses == null) {
			if (other.styleClasses != null)
				return false;
		} else if (!styleClasses.equals(other.styleClasses))
			return false;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		return true;
	}



}