package mekanism.common.integration.crafttweaker.example.component;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import mekanism.common.integration.crafttweaker.example.BaseCrTExampleProvider;
import mekanism.common.integration.crafttweaker.example.CrTExampleBuilder;
import mekanism.common.integration.crafttweaker.recipe.manager.MekanismRecipeManager;
import mekanism.common.util.MekanismUtils;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openzen.zencode.java.ZenCodeType;

public class CrTExampleRecipeComponentBuilder<BUILDER_TYPE extends CrTExampleBuilder<BUILDER_TYPE>> extends CrTBaseExampleRecipeComponent {

    private final List<RecipeMethod> methods = new ArrayList<>();
    private final List<RecipeExample> examples = new ArrayList<>();
    private final BUILDER_TYPE parent;

    public CrTExampleRecipeComponentBuilder(BUILDER_TYPE parent, MekanismRecipeManager<?, ?> recipeManager, String... methodNames) {
        super(recipeManager);
        this.parent = parent;
        if (methodNames == null || methodNames.length == 0) {
            throw new IllegalArgumentException("No method names specified.");
        }
        Object2BooleanMap<String> usedMethodNames = new Object2BooleanArrayMap<>();
        for (String methodName : methodNames) {
            usedMethodNames.put(methodName, false);
        }
        Class<?> recipeManagerClass = recipeManager.getClass();
        //Lazy lookup of types so that if we don't have any methods with generics we don't need to traverse the class heirarchy
        Lazy<Map<Class<?>, Map<String, Type>>> lazyTypeLookup = Lazy.of(() -> GenericResolutionHelper.getTypes(recipeManagerClass));
        for (Method method : recipeManagerClass.getMethods()) {
            String methodName = method.getName();
            if (usedMethodNames.containsKey(methodName) && isZCMethod(method)) {
                usedMethodNames.put(methodName, true);
                Parameter[] methodParameters = method.getParameters();
                Type[] genericParameterTypes = method.getGenericParameterTypes();
                if (genericParameterTypes.length != methodParameters.length) {
                    throw new IllegalStateException("Mismatched number of parameters and generic versions");
                }
                //Convert the lazy type lookup into a lazy lookup for the specific class the method is declared in
                // that way if multiple classes define the same generic name we know which one to reference
                Lazy<Map<String, Type>> lazyMethodTypeLookup = Lazy.of(() -> lazyTypeLookup.get().getOrDefault(method.getDeclaringClass(), Collections.emptyMap()));
                Lazy<Map<String, Type>> lazyLocalTypeLookup = Lazy.of(() -> GenericResolutionHelper.calculateBaseTypes(method.getTypeParameters(),
                      lazyMethodTypeLookup.get()::get));
                MethodParameter[] transformedParameters = new MethodParameter[methodParameters.length];
                int optionalParameterCount = 0;
                List<String> parameterNames = lookupParameterNames(recipeManagerClass, method);
                LinkedHashMap<String, ParameterData> parameters = new LinkedHashMap<>();
                for (int i = 0; i < methodParameters.length; i++) {
                    Parameter parameter = methodParameters[i];
                    if (hasOptionalAnnotation(parameter)) {
                        optionalParameterCount++;
                    } else if (optionalParameterCount > 0) {
                        throw new RuntimeException("Optional parameters have to be consecutive and at the end. Found non optional parameter after an optional parameter.");
                    }
                    //Transform the parameters to proper type and cache the type in case we have optionals, so we can directly query the transformed type
                    MethodParameter transformedParameter = MethodParameter.get(parameter, genericParameterTypes[i], lazyLocalTypeLookup, lazyMethodTypeLookup);
                    transformedParameters[i] = transformedParameter;
                    addParameter(parameters, parameterNames, transformedParameter, i);
                }
                methods.add(new RecipeMethod(methodName, parameters));
                //First we build it up with no optionals excluded, and then we go through and create the other possible combinations
                for (int i = 1; i <= optionalParameterCount; i++) {
                    LinkedHashMap<String, ParameterData> reducedParameters = new LinkedHashMap<>();
                    for (int j = 0; j < transformedParameters.length - i; j++) {
                        addParameter(reducedParameters, parameterNames, transformedParameters[j], j);
                    }
                    methods.add(new RecipeMethod(methodName, reducedParameters));
                }
            }
        }
        List<String> missingMethods = usedMethodNames.object2BooleanEntrySet().stream()
              .filter(entry -> !entry.getBooleanValue()).map(Map.Entry::getKey).toList();
        if (!missingMethods.isEmpty()) {
            throw new RuntimeException("Recipe manager: '" + recipeManagerClass.getSimpleName() + "' does not contain any implementations for methods with names: ["
                                       + String.join(", ", missingMethods) + "].");
        }
        //Sort the methods into a predetermined order by method name, number of parameters, rough estimation of the signature
        methods.sort(Comparator.comparing((RecipeMethod method) -> method.methodName)
              .thenComparingInt((RecipeMethod method) -> method.parameterTypes.size())
              .thenComparing((RecipeMethod method) -> {
                  StringBuilder pseudoPath = new StringBuilder();
                  for (ParameterData parameterType : method.parameterTypes) {
                      pseudoPath.append(parameterType.type().getName());
                  }
                  return pseudoPath.toString();
              }));
    }

    private static List<String> lookupParameterNames(Class<?> clazz, Method method) {
        String signature = MethodType.methodType(method.getReturnType(), method.getParameterTypes()).descriptorString();
        String methodName = method.getName();
        List<String> parameterNames = getParameterNames(clazz, methodName, signature);
        while (parameterNames.isEmpty() && clazz.getSuperclass() != null) {
            clazz = clazz.getSuperclass();
            parameterNames = getParameterNames(clazz, methodName, signature);
        }
        return parameterNames;
    }

    private static List<String> getParameterNames(Class<?> clazz, String method, String signature) {
        if (BaseCrTExampleProvider.PARAMETER_NAMES == null) {
            return Collections.emptyList();
        }
        JsonObject classMethods = BaseCrTExampleProvider.PARAMETER_NAMES.getAsJsonObject(clazz.getName());
        return MekanismUtils.getParameterNames(classMethods, method, signature);
    }

    public CrTExampleRecipeComponentBuilder<BUILDER_TYPE> addExample(Object... params) {
        if (params == null || params.length == 0) {
            throw new IllegalArgumentException("No parameters specified.");
        }
        List<Class<?>> paramTypes = new ArrayList<>(params.length);
        for (Object param : params) {
            paramTypes.add(param.getClass());
        }
        for (RecipeMethod method : methods) {
            int typeCount = method.parameterTypes.size();
            if (typeCount == params.length) {
                boolean matches = true;
                for (int i = 0; i < typeCount; i++) {
                    ParameterData expectedData = method.parameterTypes.get(i);
                    Class<?> expected = expectedData.type();
                    Class<?> actual = paramTypes.get(i);
                    //TODO: Add in some way to validate the actual against the generic class
                    if (!expected.isAssignableFrom(actual) && !parent.getExampleProvider().supportsConversion(expected, expectedData.generic, actual)) {
                        matches = false;
                        break;
                    }
                }
                if (matches) {
                    examples.add(new RecipeExample(method, params));
                    method.hasExample = true;
                    return this;
                }
            }
        }
        throw new IllegalArgumentException("No matching recipe signature found for recipe type '" + recipeType + "'");
    }

    @NotNull
    @Override
    public String asString() {
        validate();
        StringBuilder stringBuilder = new StringBuilder();
        for (RecipeMethod method : methods) {
            stringBuilder.append("// ");
            appendRecipeMethodStart(stringBuilder, method.methodName);
            method.appendParameters(stringBuilder, (sb, name, type, generic) -> {
                sb.append(name)
                      .append(" as ")
                      .append(parent.getExampleProvider().getCrTClassName(type));
                if (generic != null) {
                    sb.append('<')
                          .append(parent.getExampleProvider().getCrTClassName(generic))
                          .append('>');
                }
            });
            stringBuilder.append(")\n");
        }
        //And an extra newline before implementations
        stringBuilder.append('\n');
        for (RecipeExample example : examples) {
            int paramCount = example.method.parameterTypes.size();
            if (paramCount == 0) {
                appendRecipeMethodStart(stringBuilder, example.method.methodName);
                stringBuilder.append(");\n");
            } else {
                //noinspection unchecked
                List<String>[] parameterRepresentations = new List[paramCount];
                for (int i = 0; i < paramCount; i++) {
                    Object exampleParam = example.params[i];
                    ParameterData parameterData = example.method.parameterTypes.get(i);
                    List<String> representations = parent.getExampleProvider().getConversionRepresentations(parameterData.type, parameterData.generic,
                          parent.getImports(), exampleParam);
                    if (representations.isEmpty()) {
                        throw new RuntimeException("No matching representations found for parameter " + i + " of type " + exampleParam.getClass().getSimpleName());
                    }
                    parameterRepresentations[i] = representations;
                }
                examplesAndVariation(stringBuilder, example.method.methodName, parameterRepresentations);
            }
        }
        return stringBuilder.toString();
    }

    private void examplesAndVariation(StringBuilder stringBuilder, String methodName, List<String>[] parameterRepresentations) {
        List<StringBuilder> combinedParameters = new ArrayList<>();
        combinedParameters.add(new StringBuilder());
        for (int i = 0; i < parameterRepresentations.length; i++) {
            boolean addComma = i != 0;
            List<String> representations = parameterRepresentations[i];
            int representationCount = representations.size();
            if (representationCount == 1) {
                appendToAll(combinedParameters, representations.getFirst(), addComma);
            } else {
                List<StringBuilder> currentCombinedParameters = copyCombined(combinedParameters);
                for (int j = 0; j < representationCount; j++) {
                    String representation = representations.get(j);
                    if (j == 0) {
                        appendToAll(combinedParameters, representation, addComma);
                    } else if (j == representationCount - 1) {
                        appendToAll(currentCombinedParameters, representation, addComma);
                        combinedParameters.addAll(currentCombinedParameters);
                    } else {
                        List<StringBuilder> newCombinedParameters = copyCombined(currentCombinedParameters);
                        appendToAll(newCombinedParameters, representation, addComma);
                        combinedParameters.addAll(newCombinedParameters);
                    }
                }
            }
        }
        appendRecipeMethodStart(stringBuilder, methodName);
        stringBuilder.append(combinedParameters.getFirst())
              .append(");\n");
        int possibilities = combinedParameters.size();
        if (possibilities > 1) {
            stringBuilder.append("//")
                  .append(possibilities > 2 ? "Alternate implementations" : "An alternate implementation")
                  .append(" of the above recipe are shown commented below. ")
                  .append(possibilities > 2 ? "These implementations make" : "This implementation makes")
                  .append(" use of implicit casting to allow easier calling:\n");
            for (int i = 1; i < possibilities; i++) {
                stringBuilder.append("// ");
                appendRecipeMethodStart(stringBuilder, methodName);
                stringBuilder.append(combinedParameters.get(i))
                      .append(");\n");
            }
            stringBuilder.append('\n');
        }
    }

    private void appendToAll(List<StringBuilder> combinedParameters, String toAppend, boolean addComma) {
        for (StringBuilder combinedParameter : combinedParameters) {
            if (addComma) {
                combinedParameter.append(", ");
            }
            combinedParameter.append(toAppend);
        }
    }

    private List<StringBuilder> copyCombined(List<StringBuilder> combinedParameters) {
        List<StringBuilder> copied = new ArrayList<>();
        for (StringBuilder combinedParameter : combinedParameters) {
            copied.add(new StringBuilder(combinedParameter));
        }
        return copied;
    }

    public BUILDER_TYPE end() {
        validate();
        return parent;
    }

    private void validate() {
        for (RecipeMethod method : methods) {
            if (!method.hasExample) {
                StringBuilder signature = new StringBuilder(method.methodName);
                signature.append('(');
                method.appendParameters(signature, (sb, name, type, generic) -> {
                    sb.append(type.getSimpleName())
                          .append(' ')
                          .append(name);
                    if (generic != null) {
                        sb.append('<')
                              .append(generic.getSimpleName())
                              .append('>');
                    }
                });
                signature.append(')');
                throw new RuntimeException("Recipe method: '" + signature + "' has no example usage declared.");
            }
        }
    }

    private void addParameter(LinkedHashMap<String, ParameterData> parameters, List<String> parameterNames, MethodParameter parameter, int index) {
        String name;
        if (index < parameterNames.size()) {
            name = parameterNames.get(index);
        } else {
            //Fallback to generated name
            name = parameter.name();
        }
        parameters.put(name, parameter.createData());
    }

    private boolean isZCMethod(Method method) {
        for (Annotation annotation : method.getAnnotations()) {
            if (annotation instanceof ZenCodeType.Method) {
                return true;
            }
        }
        return false;
    }

    private boolean hasOptionalAnnotation(Parameter parameter) {
        for (Annotation annotation : parameter.getAnnotations()) {
            if (annotation instanceof ZenCodeType.Optional || annotation instanceof ZenCodeType.OptionalInt ||
                annotation instanceof ZenCodeType.OptionalLong || annotation instanceof ZenCodeType.OptionalFloat ||
                annotation instanceof ZenCodeType.OptionalDouble || annotation instanceof ZenCodeType.OptionalString ||
                annotation instanceof ZenCodeType.OptionalBoolean || annotation instanceof ZenCodeType.OptionalChar) {
                return true;
            }
        }
        return false;
    }

    private static class RecipeMethod {

        private final List<String> parameterNames = new ArrayList<>();
        private final List<ParameterData> parameterTypes = new ArrayList<>();
        private final String methodName;
        private boolean hasExample;

        public RecipeMethod(String methodName, LinkedHashMap<String, ParameterData> parameters) {
            this.methodName = methodName;
            for (Map.Entry<String, ParameterData> entry : parameters.entrySet()) {
                parameterNames.add(entry.getKey());
                parameterTypes.add(entry.getValue());
            }
        }

        private void appendParameters(StringBuilder stringBuilder, ParameterWriter parameterWriter) {
            for (int i = 0, count = parameterNames.size(); i < count; i++) {
                if (i != 0) {
                    stringBuilder.append(", ");
                }
                ParameterData parameterData = parameterTypes.get(i);
                parameterWriter.write(stringBuilder, parameterNames.get(i), parameterData.type(), parameterData.generic());
            }
        }
    }

    @FunctionalInterface
    private interface ParameterWriter {

        void write(StringBuilder sb, String name, Class<?> type, @Nullable Class<?> genericType);
    }

    private record MethodParameter(String name, Type type) {

        /**
         * Helper to get a method parameter (name, type pair) either using the existing parameter, or if there is a subclass, and the parameter was a generic, using the
         * more specific type if possible.
         */
        private static MethodParameter get(Parameter parameter, Type genericParameterType, Lazy<Map<String, Type>> lazyLocalTypeLookup,
              Lazy<Map<String, Type>> lazyMethodTypeLookup) {
            Type type = null;
            if (genericParameterType instanceof TypeVariable<?> v) {
                String name = v.getName();
                //First try grabbing the type from any generics the method itself might declare
                type = lazyLocalTypeLookup.get().get(name);
                if (type == null) {
                    //If we couldn't find the corresponding type then we instead need to look at the class' generics
                    type = lazyMethodTypeLookup.get().get(name);
                }
            }
            if (type == null) {
                return new MethodParameter(parameter.getName(), parameter.getParameterizedType());
            }
            return new MethodParameter(parameter.getName(), type);
        }

        public ParameterData createData() {
            if (type instanceof Class<?> c) {
                return new ParameterData(c, null);
            } else if (type instanceof ParameterizedType parameterizedType && parameterizedType.getRawType() instanceof Class<?> c) {
                Type[] arguments = parameterizedType.getActualTypeArguments();
                //TODO: Improve the support of generics
                if (arguments.length == 1 && arguments[0] instanceof Class<?> genericClass) {
                    return new ParameterData(c, genericClass);
                }
                return new ParameterData(c, null);
            }
            throw new IllegalArgumentException("Unknown type: " + type.getTypeName());
        }
    }

    private record ParameterData(Class<?> type, @Nullable Class<?> generic) {
    }

    private record RecipeExample(RecipeMethod method, Object[] params) {

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            return o instanceof RecipeExample other && method.equals(other.method) && Arrays.deepEquals(params, other.params);
        }

        @Override
        public int hashCode() {
            return 31 * method.hashCode() + Arrays.deepHashCode(params);
        }
    }
}