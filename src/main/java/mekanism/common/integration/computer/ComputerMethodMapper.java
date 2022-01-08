package mekanism.common.integration.computer;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.lang.annotation.ElementType;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.common.Mekanism;
import mekanism.common.integration.computer.BoundComputerMethod.ThreadAwareMethodHandle;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.SyntheticComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod.WrappingComputerMethodIndex;
import mekanism.common.lib.MekAnnotationScanner.BaseAnnotationScanner;
import mekanism.common.tile.interfaces.IComparatorSupport;
import mekanism.common.tile.interfaces.ITileDirectional;
import mekanism.common.tile.interfaces.ITileRedstone;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.ModFileScanData.AnnotationData;
import org.objectweb.asm.Type;

public class ComputerMethodMapper extends BaseAnnotationScanner {

    public static final ComputerMethodMapper INSTANCE = new ComputerMethodMapper();
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final MethodHandles.Lookup PUBLIC_LOOKUP = MethodHandles.publicLookup();
    private final Map<Class<?>, Map<String, List<MethodHandleInfo>>> namedMethodHandleCache = new Object2ObjectOpenHashMap<>();

    private ComputerMethodMapper() {
    }

    @Override
    protected boolean isEnabled() {
        return Mekanism.hooks.computerCompatEnabled();
    }

    @Override
    protected Map<ElementType, Type[]> getSupportedTypes() {
        Map<ElementType, Type[]> supportedTypes = new EnumMap<>(ElementType.class);
        Type wrappingType = Type.getType(WrappingComputerMethod.class);
        supportedTypes.put(ElementType.FIELD, new Type[]{Type.getType(SyntheticComputerMethod.class), wrappingType});
        supportedTypes.put(ElementType.METHOD, new Type[]{Type.getType(ComputerMethod.class), wrappingType});
        return supportedTypes;
    }

    @Override
    protected void collectScanData(Map<String, Class<?>> classNameCache, Map<Class<?>, List<AnnotationData>> knownClasses) {
        Type wrappingType = Type.getType(WrappingComputerMethod.class);
        Map<Class<?>, List<WrappingMethodHelper>> cachedWrappers = new Object2ObjectOpenHashMap<>();
        Map<Class<?>, List<MethodDetails>> rawMethodDetails = new Object2ObjectOpenHashMap<>();
        for (Entry<Class<?>, List<AnnotationData>> entry : knownClasses.entrySet()) {
            Class<?> annotatedClass = entry.getKey();
            List<MethodDetails> methodDetails = new ArrayList<>();
            rawMethodDetails.put(annotatedClass, methodDetails);
            for (AnnotationData data : entry.getValue()) {
                if (getAnnotationValue(data, "requiredMods", Collections.<String>emptyList()).stream().anyMatch(s -> !ModList.get().isLoaded(s))) {
                    //If the required mods are not loaded, skip this annotation as the restrictions are not met
                    continue;
                }
                if (data.getTargetType() == ElementType.FIELD) {
                    //Synthetic Computer Method(s) need to be generated for the field
                    String fieldName = data.getMemberName();
                    Field field = getField(annotatedClass, fieldName);
                    if (field == null) {
                        continue;
                    }
                    if (data.getAnnotationType().equals(wrappingType)) {
                        //Wrapping computer method
                        try {
                            MethodHandle methodHandle = LOOKUP.unreflectGetter(field);
                            wrapMethodHandle(classNameCache, methodHandle, data, methodDetails, cachedWrappers, annotatedClass, fieldName);
                        } catch (IllegalAccessException e) {
                            Mekanism.logger.error("Failed to create getter for field '{}' in class '{}'.", fieldName, annotatedClass.getSimpleName());
                        }
                    } else {
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
                    }
                } else {//data.getTargetType() == ElementType.METHOD
                    //Note: Signature is methodName followed by the method descriptor
                    // For example this method is: collectScanDataUnsafe(Ljava/util/Map;Ljava/util/Map;)V
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
                            if (data.getAnnotationType().equals(wrappingType)) {
                                //Wrapping computer method
                                wrapMethodHandle(classNameCache, methodHandle, data, methodDetails, cachedWrappers, annotatedClass, methodName);
                            } else {//ComputerMethod
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

    private static void wrapMethodHandle(Map<String, Class<?>> classNameCache, MethodHandle methodHandle, AnnotationData data, List<MethodDetails> methodDetails,
          Map<Class<?>, List<WrappingMethodHelper>> cachedWrappers, Class<?> annotatedClass, String identifier) {
        Class<?> wrapperClass = getAnnotationValue(classNameCache, data, "wrapper");
        if (wrapperClass != null) {
            List<String> methodNames = getAnnotationValue(data, "methodNames", Collections.emptyList());
            int methodNameCount = methodNames.size();
            if (methodNameCount == 0) {
                Mekanism.logger.warn("No method names on wrapper for {} in class '{}', so the WrappingComputerMethod annotation should probably be removed.",
                      identifier, annotatedClass.getSimpleName());
            } else {
                List<WrappingMethodHelper> wrapperHandles = cachedWrappers.computeIfAbsent(wrapperClass, clazz -> {
                    List<WrappingMethodHelper> helpers = new ArrayList<>();
                    try {
                        Method[] methods = clazz.getDeclaredMethods();
                        Arrays.sort(methods, (a, b) -> {
                            WrappingComputerMethodIndex aIndex = a.getAnnotation(WrappingComputerMethodIndex.class);
                            WrappingComputerMethodIndex bIndex = b.getAnnotation(WrappingComputerMethodIndex.class);
                            return Integer.compare(aIndex == null ? 0 : aIndex.value(), bIndex == null ? 0 : bIndex.value());
                        });
                        boolean hasFaultyOrder = false;
                        for (Method method : methods) {
                            WrappingComputerMethodIndex index = method.getAnnotation(WrappingComputerMethodIndex.class);
                            if (index == null) {
                                if (!helpers.isEmpty()) {
                                    hasFaultyOrder = true;
                                }
                            } else if (index.value() < helpers.size()) {
                                hasFaultyOrder = true;
                            }
                            helpers.add(new WrappingMethodHelper(PUBLIC_LOOKUP.unreflect(method)));
                        }
                        if (hasFaultyOrder) {
                            Mekanism.logger.error("Faulty method index annotations in class '{}'", clazz.getSimpleName());
                        }
                    } catch (IllegalAccessException e) {
                        Mekanism.logger.error("Failed to retrieve method handle for methods in class '{}'.", clazz.getSimpleName());
                    }
                    return helpers;
                });
                //Note: While technically recalculating the method handles above is slightly wasteful if they don't match up
                // below, this is something that should be run into at dev time as an error, and shouldn't make it into an
                // actual environment so shouldn't really matter too much
                if (wrapperHandles.size() != methodNameCount) {
                    Mekanism.logger.warn("Mismatch in count of method names ({}) for generated methods and methods to generate ({}).", methodNameCount,
                          wrapperHandles.size());
                } else {
                    MethodRestriction restriction = getAnnotationValue(data, "restriction", MethodRestriction.NONE);
                    boolean threadSafe = getAnnotationValue(data, "threadSafe", false);
                    for (int index = 0; index < methodNameCount; index++) {
                        //If there is an error at dev time it should crash with an IllegalArgumentException
                        MethodHandle newHandle = MethodHandles.filterReturnValue(methodHandle, wrapperHandles.get(index).asType(methodHandle.type().returnType()));
                        methodDetails.add(new MethodDetails(methodNames.get(index), newHandle, restriction, threadSafe));
                    }
                }
            }
        }
    }

    /**
     * @param handler      Handler to bind to.
     * @param boundMethods Map of method name to actual method to add our methods to.
     */
    public void getAndBindToHandler(@Nonnull Object handler, Map<String, BoundComputerMethod> boundMethods) {
        getAndBindToHandler(handler.getClass(), handler, boundMethods);
    }

    /**
     * @param handlerClass Class of the handler to bind to.
     * @param handler      Handler to bind to, {@code null} if the handler is a static class.
     * @param boundMethods Map of method name to actual method to add our methods to.
     */
    public void getAndBindToHandler(Class<?> handlerClass, @Nullable Object handler, Map<String, BoundComputerMethod> boundMethods) {
        Map<String, List<MethodHandleInfo>> namedMethods = namedMethodHandleCache.computeIfAbsent(handlerClass,
              clazz -> getData(namedMethodHandleCache, clazz, Collections.emptyMap()));
        boolean hasMethods = !boundMethods.isEmpty();
        for (Map.Entry<String, List<MethodHandleInfo>> entry : namedMethods.entrySet()) {
            String methodName = entry.getKey();
            List<MethodHandleInfo> methods = entry.getValue();
            //If we have no methods originally none should intersect, so we can skip the lookup checks
            BoundComputerMethod boundMethod = hasMethods ? boundMethods.get(methodName) : null;
            if (boundMethod == null) {
                //Use a list that is the min size we need (this is likely to be one)
                // Note: This technically may be less than the number of methods, if there is more than one
                // if some are restricted and some are not, but it is such a rare case that it shouldn't
                // matter having such a small amount extra in the backing list
                List<ThreadAwareMethodHandle> boundMethodHandles = new ArrayList<>(methods.size());
                for (MethodHandleInfo method : methods) {
                    if (method.restriction.test(handler)) {
                        boundMethodHandles.add(method.bindTo(handler));
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
                        boundMethod.addMethodImplementation(method.bindTo(handler));
                    }
                }
            }
        }
    }

    private static boolean validMethodName(String name) {
        return name.matches("^([a-zA-Z_$][a-zA-Z\\d_$]*)$");
    }

    private static class WrappingMethodHelper {

        private final Map<Class<?>, MethodHandle> mappedHandles = new Object2ObjectOpenHashMap<>();
        private final MethodHandle methodHandle;

        private WrappingMethodHelper(MethodHandle methodHandle) {
            this.methodHandle = methodHandle;
        }

        /**
         * Creates a method handle with the parameter being of the corresponding type instead. Useful to go from interface to concrete type.
         */
        public MethodHandle asType(Class<?> type) {
            if (type == methodHandle.type().parameterType(0)) {
                return methodHandle;
            }
            return mappedHandles.computeIfAbsent(type, clazz -> MethodHandles.explicitCastArguments(methodHandle, methodHandle.type().changeParameterType(0, clazz)));
        }
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

        public ThreadAwareMethodHandle bindTo(@Nullable Object handler) {
            return new ThreadAwareMethodHandle(handler == null ? methodHandle : methodHandle.bindTo(handler), threadSafe);
        }
    }

    public enum MethodRestriction implements Predicate<Object> {
        /**
         * No restrictions
         */
        NONE(handler -> true),
        /**
         * Handler is a directional tile that is actually directional.
         */
        DIRECTIONAL(handler -> handler instanceof ITileDirectional && ((ITileDirectional) handler).isDirectional()),
        /**
         * Handler is an energy handler that can handle energy.
         */
        ENERGY(handler -> handler instanceof IMekanismStrictEnergyHandler && ((IMekanismStrictEnergyHandler) handler).canHandleEnergy()),
        /**
         * Handler is a multiblock that can expose the multiblock.
         */
        MULTIBLOCK(handler -> handler instanceof TileEntityMultiblock && ((TileEntityMultiblock<?>) handler).exposesMultiblockToComputer()),
        /**
         * Handler is a tile that can support redstone.
         */
        REDSTONE_CONTROL(handler -> handler instanceof ITileRedstone && ((ITileRedstone) handler).supportsRedstone()),
        /**
         * Handler is a tile that has comparator support.
         */
        COMPARATOR(handler -> handler instanceof IComparatorSupport && ((IComparatorSupport) handler).supportsComparator());

        private final Predicate<Object> validator;

        MethodRestriction(Predicate<Object> validator) {
            this.validator = validator;
        }

        @Override
        public boolean test(@Nullable Object handler) {
            return validator.test(handler);
        }
    }
}