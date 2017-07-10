package buildcraft.api.schematics;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import net.minecraft.entity.EntityList;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.core.BuildCraftAPI;

public class SchematicEntityFactoryRegistry {
    private static final Set<SchematicEntityFactory<?>> FACTORIES = new TreeSet<>();

    public static <S extends ISchematicEntity<S>> void registerFactory(String name,
                                                                       int priority,
                                                                       Predicate<SchematicEntityContext> predicate,
                                                                       Supplier<S> supplier) {
        FACTORIES.add(new SchematicEntityFactory<>(
                BuildCraftAPI.nameToResourceLocation(name),
                priority,
                predicate,
                supplier
        ));
    }

    public static <S extends ISchematicEntity<S>> void registerFactory(String name,
                                                                      int priority,
                                                                      List<ResourceLocation> entities,
                                                                      Supplier<S> supplier) {
        registerFactory(
                name,
                priority,
                context -> entities.contains(EntityList.getKey(context.entity)),
                supplier
        );
    }

    public static List<SchematicEntityFactory<?>> getFactories() {
        return ImmutableList.copyOf(FACTORIES);
    }

    @Nonnull
    public static SchematicEntityFactory<?> getFactoryByInstance(ISchematicEntity<?> instance) {
        return FACTORIES.stream()
                .filter(schematicEntityFactory -> schematicEntityFactory.clazz == instance.getClass())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Didn't find a factory for " + instance.getClass()));
    }

    @Nullable
    public static SchematicEntityFactory<?> getFactoryByName(ResourceLocation name) {
        return FACTORIES.stream()
                .filter(schematicEntityFactory -> schematicEntityFactory.name.equals(name))
                .findFirst()
                .orElse(null);
    }
}
