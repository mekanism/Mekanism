package mekanism.common.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class ReflectionUtils
{
	/**
	 * Retrieves a private value from a defined class and field.
	 * @param obj - the Object to retrieve the value from, null if static
	 * @param c - Class to retrieve field value from
	 * @param fields - possible names of field to iterate through
	 * @return value as an Object, cast as necessary
	 */
	public static Object getPrivateValue(Object obj, Class c, String[] fields)
	{
		for(String field : fields)
		{
			try {
				Field f = c.getDeclaredField(field);
				f.setAccessible(true);
				return f.get(obj);
			} catch(Exception e) {
				continue;
			}
		}

		return null;
	}

	/**
	 * Sets a private value from a defined class and field to a new value.
	 * @param obj - the Object to perform the operation on, null if static
	 * @param value - value to set the field to
	 * @param c - Class the operation will be performed on
	 * @param fields - possible names of field to iterate through
	 */
	public static void setPrivateValue(Object obj, Object value, Class c, String[] fields)
	{
		for(String field : fields)
		{
			try {
				Field f = c.getDeclaredField(field);
				f.setAccessible(true);
				f.set(obj, value);
			} catch(Exception e) {
				continue;
			}
		}
	}

	/**
	 * Retrieves a private method from a class, sets it as accessible, and returns it.
	 * @param c - Class the method is located in
	 * @param methods - possible names of the method to iterate through
	 * @param params - the Types inserted as parameters into the method
	 * @return private method
	 */
	public static Method getPrivateMethod(Class c, String[] methods, Class... params)
	{
		for(String method : methods)
		{
			try {
				Method m = c.getDeclaredMethod(method, params);
				m.setAccessible(true);
				return m;
			} catch(Exception e) {
				continue;
			}
		}

		return null;
	}
}
