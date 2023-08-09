package mekanism.common.integration.computer;

import mekanism.generated.mekanism.ComputerMethodRegistry_mekanism;
import net.minecraftforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Central place for Factories to be registered and bound.
 * Each child mod must call the generated code somewhere in its initialisation, e.g. {@link ComputerMethodRegistry_mekanism#init()}
 * {@link #bindTo} is used to gather methods for a subject or class (static methods only, or it would explode at runtime)
 * Factories are constructed lazily to reduce initialisation time.
 */
public class FactoryRegistry {
    /** subject to Factory registration map */
    private static final Map<Class<?>, Lazy<ComputerMethodFactory<?>>> factories = new HashMap<>();
    /** interface to factory registration map, must be iterated */
    public static final Map<Class<?>, Lazy<ComputerMethodFactory<?>>> interfaceFactories = new HashMap<>();
    /** map of (relevant) superclasses for a subject's class. Added to at runtime to cache lookups for subclasses */
    private static final Map<Class<?>, List<Class<?>>> superClasses = new HashMap<>();
    /** cached list of factories for a subject class */
    private static final Map<Class<?>, List<ComputerMethodFactory<?>>> hierarchyHandlers = new ConcurrentHashMap<>();

    /**
     * Adds a Factory to the registry
     * @param subject Class of the subject
     * @param factorySupplier constructor of the factory
     * @param parents Classes of the supertypes which will be checked for handlers (calculated at compile time)
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public synchronized static <T> void register(Class<T> subject, Supplier<ComputerMethodFactory<T>> factorySupplier, Class<?>... parents) {
        factories.put(subject, (Lazy) Lazy.of(factorySupplier));
        if (parents != null && parents.length > 0) {
            superClasses.put(subject, Arrays.asList(parents));
        } else {
            superClasses.put(subject, Collections.emptyList());
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public synchronized static <T> void registerInterface(Class<T> subject, Supplier<ComputerMethodFactory<T>> factorySupplier) {
        interfaceFactories.put(subject, (Lazy) Lazy.of(factorySupplier));
    }

    /**
     * Gathers methods for a subject into holder.
     *
     * @param holder the holder to add methods to
     * @param subject the subject to bind to
     */
    public static void bindTo(BoundMethodHolder holder, @NotNull Object subject) {
        bindTo(holder, subject, subject.getClass());
    }

    /**
     * Gathers methods for a subject class into a holder
     * Super classes are bound first, followed by the exact subject class
     *
     * @param holder the holder to add methods to
     * @param subject the nullable subject
     * @param subjectClass the actual class that subject will be (or would be if null)
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void bindTo(BoundMethodHolder holder, @Nullable Object subject, @NotNull Class<?> subjectClass) {
        List<ComputerMethodFactory<?>> factoriesToBind = getHandlersForHierarchy(subjectClass);
        for (ComputerMethodFactory computerMethodFactory : factoriesToBind) {
            computerMethodFactory.bindTo(subject, holder);
        }
        for (Map.Entry<Class<?>, Lazy<ComputerMethodFactory<?>>> interfaceEntry : interfaceFactories.entrySet()) {
            if (interfaceEntry.getKey().isAssignableFrom(subjectClass)) {
                ComputerMethodFactory computerMethodFactory = interfaceEntry.getValue().get();
                computerMethodFactory.bindTo(subject, holder);
            }
        }
    }

    /**
     * Caches and builds a list of factories applicable to the target class
     * @param target the subject class
     * @return a list of applicable handlers
     */
    private static List<ComputerMethodFactory<?>> getHandlersForHierarchy(Class<?> target) {
        List<ComputerMethodFactory<?>> handlers = hierarchyHandlers.get(target);
        if (handlers != null) {
            return handlers;
        }
        handlers = buildHandlersForHierarchy(target);
        hierarchyHandlers.put(target, handlers);
        return handlers;
    }

    /**
     * Gets Factories for target and its superclasses.
     * Recursive! via {@link #getHandlersForHierarchy(Class)} to store parents which we don't know
     *
     * @param target class to find handlers for
     * @return list of handlers (perhaps empty)
     */
    private static List<ComputerMethodFactory<?>> buildHandlersForHierarchy(Class<?> target) {
        if (factories.containsKey(target)) {
            //found one we handle, all supers will be present (if required)
            List<ComputerMethodFactory<?>> outList = new ArrayList<>();
            for (Class<?> aClass : superClasses.get(target)) {
                Lazy<ComputerMethodFactory<?>> computerMethodFactoryLazy = factories.get(aClass);
                if (computerMethodFactoryLazy != null) {
                    outList.add(computerMethodFactoryLazy.get());
                }
            }
            outList.add(factories.get(target).get());
            return outList;
        }
        Class<?> parent = target.getSuperclass();
        if (parent == Object.class || parent == null) {
            return Collections.emptyList();
        } else {
            return getHandlersForHierarchy(parent);
        }
    }
}
