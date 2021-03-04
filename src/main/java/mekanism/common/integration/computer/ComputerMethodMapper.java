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
import java.util.function.Predicate;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.common.Mekanism;
import mekanism.common.integration.computer.BoundComputerMethod.ThreadAwareMethodHandle;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.SyntheticComputerMethod;
import mekanism.common.lib.MekAnnotationScanner.BaseAnnotationScanner;
import mekanism.common.tile.interfaces.ITileDirectional;
import mekanism.common.tile.interfaces.ITileRedstone;
import net.minecraftforge.forgespi.language.ModFileScanData.AnnotationData;
import org.objectweb.asm.Type;

//TODO - 10.1: Creating a wrapper based system similar to sync mapper's that lets us say:
// Use this method's return result or field's value as the input to create a set of synthetic methods based on a given naming scheme.
// This would for example allow us to be more strictly annotation based for things like getting the item in a given slot, or for the
// cases where we have getters for the substance in a tank, the capacity, the needed amount, the filled percentage. Potentially the
// way we could even do the naming scheme is just relying on a string list as an input and the names to match up with what methods
// a passed in class represents will be created. Another good target case for this would probably be for frequencies
public class ComputerMethodMapper extends BaseAnnotationScanner {

    public static final ComputerMethodMapper INSTANCE = new ComputerMethodMapper();
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private final Map<Class<?>, Map<String, List<MethodHandleInfo>>> namedMethodHandleCache = new Object2ObjectOpenHashMap<>();

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
                        MethodRestriction restriction = getAnnotationValue(data, "restriction", MethodRestriction.NONE);
                        createSyntheticMethod(methodDetails, annotatedClass, field, fieldName, getterName, true, restriction,
                              getAnnotationValue(data, "threadSafeGetter", false));
                        createSyntheticMethod(methodDetails, annotatedClass, field, fieldName, setterName, false, restriction,
                              getAnnotationValue(data, "threadSafeSetter", false));
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
                            methodDetails.add(new MethodDetails(methodNameOverride, methodHandle, getAnnotationValue(data, "restriction", MethodRestriction.NONE),
                                  getAnnotationValue(data, "threadSafe", false)));
                        }
                    }
                }
            }
        }
        List<ClassBasedInfo<MethodDetails>> methodDetails = combineWithParents(rawMethodDetails);
        for (ClassBasedInfo<MethodDetails> details : methodDetails) {
            //Linked map to preserve order
            Map<String, List<MethodHandleInfo>> cache = new LinkedHashMap<>();
            details.infoList.sort(Comparator.comparing(info -> info.methodName));
            for (MethodDetails handle : details.infoList) {
                //Add the method handle to the list of methods with that method name for our computer handler
                // Note: we construct the list with an initial capacity of one, as that is likely how many we
                // actually have per methodName, we just support using a list
                cache.computeIfAbsent(handle.methodName, methodName -> new ArrayList<>(1))
                      .add(new MethodHandleInfo(handle.method, handle.restriction, handle.threadSafe));
            }
            namedMethodHandleCache.put(details.clazz, cache);
        }
    }

    private static void createSyntheticMethod(List<MethodDetails> methodDetails, Class<?> annotatedClass, Field field, String fieldName, String methodName,
          boolean isGetter, MethodRestriction restriction, boolean threadSafe) {
        if (!methodName.isEmpty()) {
            if (validMethodName(methodName)) {
                try {
                    MethodHandle methodHandle = isGetter ? LOOKUP.unreflectGetter(field) : LOOKUP.unreflectSetter(field);
                    methodDetails.add(new MethodDetails(methodName, methodHandle, restriction, threadSafe));
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
        Map<String, List<MethodHandleInfo>> namedMethods = namedMethodHandleCache.computeIfAbsent(handler.getClass(),
              clazz -> getData(namedMethodHandleCache, clazz, Collections.emptyMap()));
        boolean hasMethods = !boundMethods.isEmpty();
        for (Map.Entry<String, List<MethodHandleInfo>> entry : namedMethods.entrySet()) {
            String methodName = entry.getKey();
            List<MethodHandleInfo> methods = entry.getValue();
            //If we have no methods originally none should intersect so we can skip the lookup checks
            BoundComputerMethod boundMethod = hasMethods ? boundMethods.get(methodName) : null;
            if (boundMethod == null) {
                //Use a list that is the min size we need (this is likely to be one)
                // Note: This technically may be less than the number of methods, if there is more than one
                // if some are restricted and some are not, but it is such a rare case that it shouldn't
                // matter having such a small amount extra in the backing list
                List<ThreadAwareMethodHandle> boundMethodHandles = new ArrayList<>(methods.size());
                for (MethodHandleInfo method : methods) {
                    if (method.restriction.test(handler)) {
                        boundMethodHandles.add(new ThreadAwareMethodHandle(method.methodHandle.bindTo(handler), method.threadSafe));
                    }
                }
                if (!boundMethodHandles.isEmpty()) {
                    //Assuming we actually have some method handles and aren't invalid for all of them
                    // create a bound method and add it to our list of bound methods.
                    boundMethods.put(methodName, new BoundComputerMethod(methodName, boundMethodHandles));
                }
            } else {
                //This is unlikely to ever actually happen, but if it does, then we want to add
                // all our methods after binding them to the existing bound computer method
                // but before adding them validate the restrictions on the method are met
                for (MethodHandleInfo method : methods) {
                    if (method.restriction.test(handler)) {
                        boundMethod.addMethodImplementation(new ThreadAwareMethodHandle(method.methodHandle.bindTo(handler), method.threadSafe));
                    }
                }
            }
        }
    }

    private static boolean validMethodName(String name) {
        return name.matches("^([a-zA-Z_$][a-zA-Z\\d_$]*)$");
    }

    private static class MethodDetails {

        private final MethodRestriction restriction;
        private final MethodHandle method;
        private final String methodName;
        private final boolean threadSafe;

        private MethodDetails(String methodName, MethodHandle method, MethodRestriction restriction, boolean threadSafe) {
            this.method = method;
            this.methodName = methodName;
            this.restriction = restriction;
            this.threadSafe = threadSafe;
        }
    }

    private static class MethodHandleInfo {

        private final MethodRestriction restriction;
        private final MethodHandle methodHandle;
        private final boolean threadSafe;

        public MethodHandleInfo(MethodHandle methodHandle, MethodRestriction restriction, boolean threadSafe) {
            this.methodHandle = methodHandle;
            this.restriction = restriction;
            this.threadSafe = threadSafe;
        }
    }

    public enum MethodRestriction implements Predicate<Object> {
        /**
         * No restrictions
         */
        NONE(handler -> true),
        /**
         * Handler is an directional tile that is actually directional.
         */
        DIRECTIONAL(handler -> handler instanceof ITileDirectional && ((ITileDirectional) handler).isDirectional()),
        /**
         * Handler is an energy handler that can handle energy.
         */
        ENERGY(handler -> handler instanceof IMekanismStrictEnergyHandler && ((IMekanismStrictEnergyHandler) handler).canHandleEnergy()),
        /**
         * Handler is an directional tile that is actually directional.
         */
        REDSTONE_CONTROL(handler -> handler instanceof ITileRedstone && ((ITileRedstone) handler).supportsRedstone());

        private final Predicate<Object> validator;

        MethodRestriction(Predicate<Object> validator) {
            this.validator = validator;
        }

        @Override
        public boolean test(Object handler) {
            return validator.test(handler);
        }
    }
}