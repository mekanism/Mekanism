package mekanism.common.integration.computer;

import net.minecraftforge.common.util.Lazy;

import java.util.*;
import java.util.function.Supplier;

public class FactoryRegistry {
    private static final Map<Class<?>, Lazy<ComputerMethodFactory<?>>> factories = new HashMap<>();
    private static final Map<Class<?>, List<Class<?>>> superClasses = new HashMap<>();
    private static final Map<Class<?>, List<ComputerMethodFactory<?>>> hierarchyHandlers = new HashMap<>();

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> void register(Class<T> subject, Supplier<ComputerMethodFactory<T>> factorySupplier, Class<?>... parents) {
        factories.put(subject, (Lazy) Lazy.of(factorySupplier));
        if (parents != null && parents.length > 0) {
            superClasses.put(subject, Arrays.asList(parents));
        } else {
            superClasses.put(subject, Collections.emptyList());
        }
    }

    public static void bindTo(BoundMethodHolder holder, Object subject) {
        bindTo(holder, subject, subject.getClass());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void bindTo(BoundMethodHolder holder, Object subject, Class<?> subjectClass) {
        List<ComputerMethodFactory<?>> factoriesToBind = getHandlersForHierarchy(subjectClass);
        for (ComputerMethodFactory computerMethodFactory : factoriesToBind) {
            computerMethodFactory.bindTo(subject, holder);
        }
    }

    private static List<ComputerMethodFactory<?>> getHandlersForHierarchy(Class<?> target) {
        return hierarchyHandlers.computeIfAbsent(target, FactoryRegistry::buildHandlersForHierarchy);
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
