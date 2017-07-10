package buildcraft.api.schematics;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.core.BuildCraftAPI;

public class SchematicBlockFactoryRegistry {
    private static final Set<SchematicBlockFactory<?>> FACTORIES = new TreeSet<>();

    public static <S extends ISchematicBlock<S>> void registerFactory(String name,
                                                                      int priority,
                                                                      Predicate<SchematicBlockContext> predicate,
                                                                      Supplier<S> supplier) {
        FACTORIES.add(new SchematicBlockFactory<>(
                BuildCraftAPI.nameToResourceLocation(name),
                priority,
                predicate,
                supplier
        ));
    }

    public static <S extends ISchematicBlock<S>> void registerFactory(String name,
                                                                      int priority,
                                                                      List<Block> blocks,
                                                                      Supplier<S> supplier) {
        registerFactory(
                name,
                priority,
                context -> blocks.contains(context.block),
                supplier
        );
    }

    public static List<SchematicBlockFactory<?>> getFactories() {
        return ImmutableList.copyOf(FACTORIES);
    }

    @Nonnull
    public static SchematicBlockFactory<?> getFactoryByInstance(ISchematicBlock<?> instance) {
        return FACTORIES.stream()
                .filter(schematicBlockFactory -> schematicBlockFactory.clazz == instance.getClass())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Didn't find a factory for " + instance.getClass()));
    }

    @Nullable
    public static SchematicBlockFactory<?> getFactoryByName(ResourceLocation name) {
        return FACTORIES.stream()
                .filter(schematicBlockFactory -> schematicBlockFactory.name.equals(name))
                .findFirst()
                .orElse(null);
    }
}
