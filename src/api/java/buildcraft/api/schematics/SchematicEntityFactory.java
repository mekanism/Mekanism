package buildcraft.api.schematics;

import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import net.minecraft.util.ResourceLocation;

public class SchematicEntityFactory<S extends ISchematicEntity<S>> implements Comparable<SchematicEntityFactory> {
    @Nonnull
    public final ResourceLocation name;
    public final int priority;
    @Nonnull
    public final Predicate<SchematicEntityContext> predicate;
    @Nonnull
    public final Supplier<S> supplier;
    @Nonnull
    public final Class<S> clazz;

    @SuppressWarnings("unchecked")
    public SchematicEntityFactory(@Nonnull ResourceLocation name,
                                  int priority,
                                  @Nonnull Predicate<SchematicEntityContext> predicate,
                                  @Nonnull Supplier<S> supplier) {
        this.name = name;
        this.priority = priority;
        this.predicate = predicate;
        this.supplier = supplier;
        clazz = (Class<S>) supplier.get().getClass();
    }

    @Override
    public int compareTo(@Nonnull SchematicEntityFactory o) {
        return priority != o.priority
                ? Integer.compare(priority, o.priority)
                : name.toString().compareTo(o.name.toString());
    }
}
