package mekanism.common.integration.computer;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.lang.annotation.ElementType;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import mekanism.common.Mekanism;
import mekanism.common.lib.MekAnnotationScanner.BaseAnnotationScanner;
import net.minecraftforge.forgespi.language.ModFileScanData.AnnotationData;
import org.objectweb.asm.Type;

public class ComputerMethodMapper extends BaseAnnotationScanner {

    public static final ComputerMethodMapper INSTANCE = new ComputerMethodMapper();
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private final Map<Class<?>, Map<String, MethodHandle>> namedMethodHandleCache = new Object2ObjectOpenHashMap<>();

    private ComputerMethodMapper() {
    }

    @Override
    protected boolean isEnabled() {
        return Mekanism.hooks.computerCompatEnabled();
    }

    @Override
    protected Map<ElementType, Type> getSupportedTypes() {
        Map<ElementType, Type> supportedTypes = new EnumMap<>(ElementType.class);
        supportedTypes.put(ElementType.FIELD, Type.getType(SyntheticComputerMethod.class));
        supportedTypes.put(ElementType.METHOD, Type.getType(ComputerMethod.class));
        return supportedTypes;
    }

    @Override
    protected void collectScanData(Map<Class<?>, List<AnnotationData>> knownClasses) {
        Map<Class<?>, List<MethodDetails>> rawMethodDetails = new Object2ObjectOpenHashMap<>();
        for (Entry<Class<?>, List<AnnotationData>> entry : knownClasses.entrySet()) {
            Class<?> annotatedClass = entry.getKey();
            List<MethodDetails> methodDetails = new ArrayList<>();
            rawMethodDetails.put(annotatedClass, methodDetails);
            for (AnnotationData data : entry.getValue()) {
                if (data.getTargetType() == ElementType.FIELD) {
                    //Synthetic Computer Method(s) need to be generated for the field
                    String fieldName = data.getMemberName();
                    Field field = getField(annotatedClass, fieldName);
                    if (field == null) {
                        continue;
                    }
                    String getterName = getAnnotationValue(data, "getter", "");
                    String setterName = getAnnotationValue(data, "setter", "");
                    if (getterName.isEmpty() && setterName.isEmpty()) {
                        Mekanism.logger.error("Field: '{}' in class '{}' is annotated to generate a computer method but does not specify a getter or setter.",
                              fieldName, annotatedClass.getSimpleName());
                    } else {
                        createSyntheticMethod(methodDetails, annotatedClass, field, fieldName, getterName, true);
                        createSyntheticMethod(methodDetails, annotatedClass, field, fieldName, setterName, false);
                    }
                } else {//data.getTargetType() == ElementType.METHOD
                    //Note: Signature is methodName followed by the method descriptor
                    // For example this method is: collectScanDataUnsafe(Ljava/util/Map;)V
                    String methodSignature = data.getMemberName();
                    int descriptorStart = methodSignature.indexOf('(');
                    if (descriptorStart == -1) {
                        Mekanism.logger.error("Method '{}' in class '{}' does not have a method descriptor.", methodSignature, annotatedClass.getSimpleName());
                    } else {
                        String methodDescriptor = methodSignature.substring(descriptorStart);
                        String methodName = methodSignature.substring(0, descriptorStart);
                        Method method = getMethod(annotatedClass, methodName, methodDescriptor);
                        if (method != null) {
                            //Note: We need to grab the method handle via the method so that we can access private and protected methods properly
                            MethodHandle methodHandle;
                            try {
                                methodHandle = LOOKUP.unreflect(method);
                            } catch (IllegalAccessException e) {
                                Mekanism.logger.error("Failed to retrieve method handle for method '{}' in class '{}'.", methodName,
                                      annotatedClass.getSimpleName());
                                continue;
                            }
                            //See if there is a name override defined for the method, or fallback
                            String methodNameOverride = getAnnotationValue(data, "nameOverride", methodName, name -> {
                                if (name.isEmpty()) {
                                    Mekanism.logger.warn("Specified name override for method '{}' in class '{}' is explicitly set to empty and "
                                                         + "will not be used.", methodName, annotatedClass.getSimpleName());
                                } else if (validMethodName(name)) {
                                    return true;
                                } else {
                                    Mekanism.logger.error("Specified name override '{}' for method '{}' in class '{}' is not a valid method name and "
                                                          + "will not be used.", name, methodName, annotatedClass.getSimpleName());
                                }
                                return false;
                            });
                            methodDetails.add(new MethodDetails(methodHandle, methodNameOverride));
                        }
                    }
                }
            }
        }
        List<ClassBasedInfo<MethodDetails>> methodDetails = combineWithParents(rawMethodDetails);
        for (ClassBasedInfo<MethodDetails> details : methodDetails) {
            //Linked map to preserve order
            Map<String, MethodHandle> cache = new LinkedHashMap<>();
            details.infoList.sort(Comparator.comparing(info -> info.methodName));
            for (MethodDetails handle : details.infoList) {
                if (cache.containsKey(handle.methodName)) {
                    //TODO - 10.1: Add more details if needed to MethodDetails so that we can avoid clashes by creating automatic fallbacks here
                    // and in getAndBindToHandler depending on the answer to https://github.com/SquidDev-CC/CC-Tweaked/discussions/727
                    // A decent way to do this might be to have a map of method name to an int count, and increment it each time we have
                    // a conflict and then massage it together?
                    Mekanism.logger.error("Duplicate computer method name '{}'. Please report this.", handle.methodName);
                } else {
                    cache.put(handle.methodName, handle.method);
                }
            }
            namedMethodHandleCache.put(details.clazz, cache);
        }
    }

    private static void createSyntheticMethod(List<MethodDetails> methodDetails, Class<?> annotatedClass, Field field, String fieldName, String methodName,
          boolean isGetter) {
        if (!methodName.isEmpty()) {
            if (validMethodName(methodName)) {
                try {
                    MethodHandle methodHandle = isGetter ? LOOKUP.unreflectGetter(field) : LOOKUP.unreflectSetter(field);
                    methodDetails.add(new MethodDetails(methodHandle, methodName));
                } catch (IllegalAccessException e) {
                    Mekanism.logger.error("Failed to create {} for field '{}' in class '{}'.", isGetter ? "getter" : "setter", fieldName,
                          annotatedClass.getSimpleName());
                }
            } else {
                Mekanism.logger.error("Specified {} name '{}' for field '{}' in class '{}' is not a valid method name.", isGetter ? "getter" : "setter",
                      methodName, fieldName, annotatedClass.getSimpleName());
            }
        }
    }

    /**
     * @param handler      Handler to bind to
     * @param boundMethods Map of method name to actual method to add our methods to.
     */
    public void getAndBindToHandler(Object handler, Map<String, BoundComputerMethod> boundMethods) {
        Map<String, MethodHandle> methods = namedMethodHandleCache.computeIfAbsent(handler.getClass(),
              clazz -> getData(namedMethodHandleCache, clazz, Collections.emptyMap()));
        for (Map.Entry<String, MethodHandle> method : methods.entrySet()) {
            String methodName = method.getKey();
            if (boundMethods.put(methodName, new BoundComputerMethod(method.getValue().bindTo(handler), methodName)) != null) {
                //TODO - 10.1: Re-evalaute handling
                Mekanism.logger.error("Duplicate computer method name '{}'. Please report this.", methodName);
            }
        }
    }

    private static boolean validMethodName(String name) {
        return name.matches("^([a-zA-Z_$][a-zA-Z\\d_$]*)$");
    }

    private static class MethodDetails {

        private final MethodHandle method;
        private final String methodName;

        private MethodDetails(MethodHandle method, String methodName) {
            this.method = method;
            this.methodName = methodName;
        }
    }
}