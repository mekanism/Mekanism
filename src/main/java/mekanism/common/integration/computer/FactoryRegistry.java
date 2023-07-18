package mekanism.common.integration.computer;

import net.minecraftforge.common.util.Lazy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Created by Thiakil on 18/07/2023.
 */
public class FactoryRegistry {
    private static final Map<Class<?>, Lazy<ComputerMethodFactory<?>>> factories = new HashMap<>();
    private static final Map<Class<?>, List<Class<?>>> dependencies = new HashMap<>();

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> void register(Class<T> subject, Supplier<ComputerMethodFactory<T>> factorySupplier, Class<?>... parents) {
        factories.put(subject, (Lazy) Lazy.of(factorySupplier));
        if (parents != null && parents.length > 0) {
            dependencies.put(subject, Arrays.asList(parents));
        }
    }
}
