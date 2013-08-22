package ic2.api.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectionHelper {
	public static Object callMethod(Object aObject, String aMethod, boolean aPrivate, boolean aUseUpperCasedDataTypes, boolean aLogErrors, Object... aParameters) {
		try {
			Class<?>[] tParameterTypes = new Class<?>[aParameters.length];
			for (byte i = 0; i < aParameters.length; i++) {
				if (aParameters[i] instanceof Class) {
					tParameterTypes[i] = (Class)aParameters[i];
					aParameters[i] = null;
				} else {
					tParameterTypes[i] = aParameters[i].getClass();
				}
				if (!aUseUpperCasedDataTypes) {
					if (tParameterTypes[i] == Boolean.class	) tParameterTypes[i] = boolean.class;
					if (tParameterTypes[i] == Byte.class	) tParameterTypes[i] = byte.class;
					if (tParameterTypes[i] == Short.class	) tParameterTypes[i] = short.class;
					if (tParameterTypes[i] == Integer.class	) tParameterTypes[i] = int.class;
					if (tParameterTypes[i] == Long.class	) tParameterTypes[i] = long.class;
					if (tParameterTypes[i] == Float.class	) tParameterTypes[i] = float.class;
					if (tParameterTypes[i] == Double.class	) tParameterTypes[i] = double.class;
				}
			}
			Method tMethod = (aObject instanceof Class)?((Class)aObject).getMethod(aMethod, tParameterTypes):aObject.getClass().getMethod(aMethod, tParameterTypes);
			if (aPrivate) tMethod.setAccessible(true);
			return tMethod.invoke(aObject, aParameters);
		} catch (Throwable e) {
			if (aLogErrors) e.printStackTrace();
		}
		return null;
	}

	public static Object callConstructor(String aClass, int aConstructorIndex, Object aReplacementObject, boolean aLogErrors, Object... aParameters) {
		try {
			return Class.forName(aClass).getConstructors()[aConstructorIndex].newInstance(aParameters);
		} catch (Throwable e) {
			if (aLogErrors) e.printStackTrace();
		}
		return aReplacementObject;
	}

	public static Object getField(Object aObject, String aField, boolean aPrivate, boolean aLogErrors) {
		try {
			Field tField = (aObject instanceof Class)?((Class)aObject).getDeclaredField(aField):aObject.getClass().getDeclaredField(aField);
			if (aPrivate) tField.setAccessible(true);
			return tField.get(aObject);
		} catch (Throwable e) {
			if (aLogErrors) e.printStackTrace();
		}
		return null;
	}
}