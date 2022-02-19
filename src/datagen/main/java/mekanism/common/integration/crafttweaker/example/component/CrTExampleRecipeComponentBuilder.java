package mekanism.common.integration.crafttweaker.example.component;

import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import mekanism.api.functions.TriConsumer;
import mekanism.common.integration.crafttweaker.example.BaseCrTExampleProvider;
import mekanism.common.integration.crafttweaker.example.CrTExampleBuilder;
import mekanism.common.integration.crafttweaker.recipe.MekanismRecipeManager;
import org.openzen.zencode.java.ZenCodeType;

public class CrTExampleRecipeComponentBuilder<BUILDER_TYPE extends CrTExampleBuilder<BUILDER_TYPE>> extends CrTBaseExampleRecipeComponent {

    private final List<RecipeMethod> methods = new ArrayList<>();
    private final List<RecipeExample> examples = new ArrayList<>();
    private final BUILDER_TYPE parent;

    public CrTExampleRecipeComponentBuilder(BUILDER_TYPE parent, MekanismRecipeManager<?> recipeManager, String... methodNames) {
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
        for (Method method : recipeManagerClass.getMethods()) {
            String methodName = method.getName();
            if (usedMethodNames.containsKey(methodName) && isZCMethod(method)) {
                usedMethodNames.put(methodName, true);
                int optionalParameterCount = 0;
                Parameter[] methodParameters = method.getParameters();
                List<String> parameterNames = lookupParameterNames(recipeManagerClass, methodName, methodParameters);
                LinkedHashMap<String, Class<?>> parameters = new LinkedHashMap<>();
                for (int i = 0; i < methodParameters.length; i++) {
                    Parameter parameter = methodParameters[i];
                    if (hasOptionalAnnotation(parameter)) {
                        optionalParameterCount++;
                    } else if (optionalParameterCount > 0) {
                        throw new RuntimeException("Optional parameters have to be consecutive and at the end. Found non optional parameter after an optional parameter.");
                    }
                    addParameter(parameters, parameterNames, parameter, i);
                }
                methods.add(new RecipeMethod(methodName, parameters));
                //First we build it up with no optionals excluded, and then we go through and create the other possible combinations
                for (int i = 1; i <= optionalParameterCount; i++) {
                    LinkedHashMap<String, Class<?>> reducedParameters = new LinkedHashMap<>();
                    for (int j = 0; j < methodParameters.length - i; j++) {
                        addParameter(reducedParameters, parameterNames, methodParameters[j], j);
                    }
                    methods.add(new RecipeMethod(methodName, reducedParameters));
                }
            }
        }
        List<String> missingMethods = usedMethodNames.object2BooleanEntrySet().stream()
              .filter(entry -> !entry.getBooleanValue()).map(Map.Entry::getKey).collect(Collectors.toList());
        if (!missingMethods.isEmpty()) {
            throw new RuntimeException("Recipe manager: '" + recipeManagerClass.getSimpleName() + "' does not contain any implementations for methods with names: ["
                                       + String.join(", ", missingMethods) + "].");
        }
        //Sort the methods into a predetermined order by method name, number of parameters, rough estimation of the signature
        methods.sort(Comparator.comparing((RecipeMethod method) -> method.methodName)
              .thenComparingInt((RecipeMethod method) -> method.parameterTypes.size())
              .thenComparing((RecipeMethod method) -> {
                  StringBuilder pseudoPath = new StringBuilder();
                  for (Class<?> parameterType : method.parameterTypes) {
                      pseudoPath.append(parameterType.getName());
                  }
                  return pseudoPath.toString();
              }));
    }

    private static List<String> lookupParameterNames(Class<?> clazz, String methodName, Parameter[] methodParameters) {
        StringBuilder stringBuilder = new StringBuilder(methodName);
        stringBuilder.append('(');
        for (Parameter methodParameter : methodParameters) {
            stringBuilder.append(methodParameter.getType().getName()).append(';');
        }
        stringBuilder.append(')');
        String key = stringBuilder.toString().replaceAll("\\$", ".");
        List<String> parameterNames = getParameterNames(clazz, key);
        while (parameterNames.isEmpty() && clazz.getSuperclass() != null) {
            clazz = clazz.getSuperclass();
            parameterNames = getParameterNames(clazz, key);
        }
        return parameterNames;
    }

    private static List<String> getParameterNames(Class<?> clazz, String method) {
        return BaseCrTExampleProvider.PARAMETER_NAMES.getOrDefault(clazz.getName(), Collections.emptyMap()).getOrDefault(method, Collections.emptyList());
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
                    Class<?> expected = method.parameterTypes.get(i);
                    Class<?> actual = paramTypes.get(i);
                    if (!expected.isAssignableFrom(actual) && !parent.getExampleProvider().supportsConversion(expected, actual)) {
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

    @Nonnull
    @Override
    public String asString() {
        validate();
        StringBuilder stringBuilder = new StringBuilder();
        for (RecipeMethod method : methods) {
            stringBuilder.append("// ");
            appendRecipeMethodStart(stringBuilder, method.methodName);
            method.appendParameters(stringBuilder, (sb, name, type) -> sb
                  .append(name)
                  .append(" as ")
                  .append(parent.getExampleProvider().getCrTClassName(type)));
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
                List<String>[] parameterRepresentations = new List[paramCount];
                for (int i = 0; i < paramCount; i++) {
                    Object exampleParam = example.params[i];
                    List<String> representations = parent.getExampleProvider().getConversionRepresentations(example.method.parameterTypes.get(i), parent.getImports(),
                          exampleParam);
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
                appendToAll(combinedParameters, representations.get(0), addComma);
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
        stringBuilder.append(combinedParameters.get(0))
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
                signature.append("(");
                method.appendParameters(signature, (sb, name, type) -> sb
                      .append(type.getSimpleName())
                      .append(' ')
                      .append(name));
                signature.append(")");
                throw new RuntimeException("Recipe method: '" + signature + "' has no example usage declared.");
            }
        }
    }

    private void addParameter(LinkedHashMap<String, Class<?>> parameters, List<String> parameterNames, Parameter parameter, int index) {
        String name;
        if (index < parameterNames.size()) {
            name = parameterNames.get(index);
        } else {
            //Fallback to generated name
            name = parameter.getName();
        }
        parameters.put(name, parameter.getType());
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
        private final List<Class<?>> parameterTypes = new ArrayList<>();
        private final String methodName;
        private boolean hasExample;

        public RecipeMethod(String methodName, LinkedHashMap<String, Class<?>> parameters) {
            this.methodName = methodName;
            for (Map.Entry<String, Class<?>> entry : parameters.entrySet()) {
                parameterNames.add(entry.getKey());
                parameterTypes.add(entry.getValue());
            }
        }

        private void appendParameters(StringBuilder stringBuilder, TriConsumer<StringBuilder, String, Class<?>> parameterWriter) {
            for (int i = 0, count = parameterNames.size(); i < count; i++) {
                if (i != 0) {
                    stringBuilder.append(", ");
                }
                parameterWriter.accept(stringBuilder, parameterNames.get(i), parameterTypes.get(i));
            }
        }
    }

    private static class RecipeExample {

        private final RecipeMethod method;
        private final Object[] params;

        public RecipeExample(RecipeMethod method, Object[] params) {
            this.method = method;
            this.params = params;
        }
    }
}