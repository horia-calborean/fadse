package jmetal.core.util.measure.impl;

import jmetal.core.util.measure.Measure;
import jmetal.core.util.naming.impl.SimpleDescribedEntity;

/**
 * {@link SimpleMeasure} is a basic implementation of {@link Measure}. It
 * provides a basic support for the most generic properties required by this
 * interface.
 * 
 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
 * 
 * @param <Value>
 */
@SuppressWarnings("serial")
public class SimpleMeasure<Value> extends SimpleDescribedEntity implements
		Measure<Value> {

	/**
	 * Create a {@link SimpleMeasure} with a given name and a given description.
	 * 
	 * @param name
	 *            the name of the {@link Measure}
	 * @param description
	 *            the description of the {@link Measure}
	 */
	public SimpleMeasure(String name, String description) {
		super(name, description);
	}

	/**
	 * Create a {@link SimpleMeasure} with a given name and a <code>null</code>
	 * description.
	 * 
	 * @param name
	 *            the name of the {@link Measure}
	 */
	public SimpleMeasure(String name) {
		super(name);
	}

	/**
	 * Create a {@link SimpleMeasure} with the class name as its name and a
	 * <code>null</code> description.
	 * 
	 */
	public SimpleMeasure() {
		super();
	}

}
