package mekanism.common.integration.crafttweaker.example.component;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.jetbrains.annotations.Nullable;

public class GenericResolutionHelper {

    static Map<String, Type> calculateBaseTypes(TypeVariable<?>[] baseTypeParameters, Function<String, @Nullable Type> parentLookup) {
        if (baseTypeParameters.length == 0) {
            return Collections.emptyMap();
        }
        Map<String, Type> types = new HashMap<>(baseTypeParameters.length);
        for (TypeVariable<?> baseTypeParameter : baseTypeParameters) {
            Type[] bounds = baseTypeParameter.getBounds();
            if (bounds.length >= 1) {
                //TODO: Improve how we handle there being multiple bounds. For now we just use the base bound
                trackType(types, baseTypeParameter.getName(), bounds[0], parentLookup);
            }
        }
        return types;
    }

    static Map<Class<?>, Map<String, Type>> getTypes(Class<?> clazz) {
        Map<Class<?>, Map<String, Type>> classBasedTypes = new HashMap<>();
        //Add the types for the top level class as best as we can
        Map<String, Type> types = calculateBaseTypes(clazz.getTypeParameters(), name -> null);
        if (!types.isEmpty()) {
            classBasedTypes.put(clazz, types);
        }
        //TODO: At some point if the need arises we may want to also handle implemented interfaces rather than just parent classes
        Type genericSuperclass;
        while ((genericSuperclass = clazz.getGenericSuperclass()) != null) {
            if (genericSuperclass instanceof Class<?> generic) {
                //No generics, update class to be at super level and continue
                clazz = generic;
            } else if (genericSuperclass instanceof ParameterizedType parameterizedType && parameterizedType.getRawType() instanceof Class<?> superClass) {
                Type[] arguments = parameterizedType.getActualTypeArguments();
                TypeVariable<? extends Class<?>>[] typeParameters = superClass.getTypeParameters();
                if (arguments.length == typeParameters.length) {
                    //Validate we have the same number of type arguments as type parameters
                    Map<String, Type> parentTypes = classBasedTypes.getOrDefault(clazz, Collections.emptyMap());
                    Function<String, Type> parentTypeLookup = parentTypes::get;
                    Map<String, Type> localTypes = new HashMap<>(typeParameters.length);
                    for (int i = 0; i < typeParameters.length; i++) {
                        //For type parameter, lookup the argument and track its type
                        Type argument = arguments[i];
                        if (!trackType(localTypes, typeParameters[i].getName(), argument, parentTypeLookup)) {
                            //If we were unable to track the type, error
                            throw new IllegalStateException("Unhandled argument type: " + argument.getTypeName());
                        }
                    }
                    //Update class to be super class and add any found generic types to our tracker
                    clazz = superClass;
                    if (!localTypes.isEmpty()) {
                        classBasedTypes.put(clazz, localTypes);
                    }
                } else {
                    throw new IllegalStateException("Mismatched number of actual type arguments and type parameters");
                }
            } else {
                //Unknown type just exit/bail
                break;
            }
        }
        return classBasedTypes;
    }

    private static boolean trackType(Map<String, Type> types, String name, Type argument, Function<String, @Nullable Type> parentLookup) {
        if (argument instanceof Class || argument instanceof ParameterizedType) {
            //If it is a type we know how to handle when parsing as a parameter add it
            types.put(name, argument);
            return true;
        } else if (argument instanceof TypeVariable<?> typeVariable) {
            // otherwise, if it is a declared generic reference, look to see if the parent has one
            // and inherit the type data from what the parent passed to that generic
            Type inherited = parentLookup.apply(typeVariable.getName());
            if (inherited != null) {
                types.put(name, inherited);
                return true;
            }
        }
        return false;
    }
}