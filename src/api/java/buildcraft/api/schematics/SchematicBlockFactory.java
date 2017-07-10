package buildcraft.api.schematics;

import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import net.minecraft.util.ResourceLocation;

public class SchematicBlockFactory<S extends ISchematicBlock<S>> implements Comparable<SchematicBlockFactory> {
    @Nonnull
    public final ResourceLocation name;
    public final int priority;
    @Nonnull
    public final Predicate<SchematicBlockContext> predicate;
    @Nonnull
    public final Supplier<S> supplier;
    @Nonnull
    public final Class<S> clazz;

    @SuppressWarnings("unchecked")
    public SchematicBlockFactory(@Nonnull ResourceLocation name,
                                 int priority,
                                 @Nonnull Predicate<SchematicBlockContext> predicate,
                                 @Nonnull Supplier<S> supplier) {
        this.name = name;
        this.priority = priority;
        this.predicate = predicate;
        this.supplier = supplier;
        clazz = (Class<S>) supplier.get().getClass();
    }

    @Override
    public int compareTo(@Nonnull SchematicBlockFactory o) {
        return priority != o.priority
                ? Integer.compare(priority, o.priority)
                : name.toString().compareTo(o.name.toString());
    }
}
