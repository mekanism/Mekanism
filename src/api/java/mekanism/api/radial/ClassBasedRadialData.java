package mekanism.api.radial;

import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.radial.mode.INestedRadialMode;
import mekanism.api.radial.mode.IRadialMode;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * Base implementation for radial data that knows the type of modes it can handle.
 *
 * @param <MODE> Radial Mode.
 *
 * @since 10.3.2
 */
@NothingNullByDefault
public abstract class ClassBasedRadialData<MODE extends IRadialMode> extends RadialData<MODE> {

    protected final Class<MODE> clazz;

    /**
     * @param identifier Identifier representing this radial data. Must be unique within the radial level if this is a nested radial element.
     * @param clazz      Class representing the type of data that this radial data knows how to handle.
     */
    protected ClassBasedRadialData(ResourceLocation identifier, Class<MODE> clazz) {
        super(identifier);
        this.clazz = Objects.requireNonNull(clazz, "Radial mode class type cannot be null.");
    }

    @Nullable
    @Override
    public INestedRadialMode fromIdentifier(ResourceLocation identifier) {
        return INestedRadialMode.class.isAssignableFrom(clazz) ? super.fromIdentifier(identifier) : null;
    }

    @Override
    public int tryGetNetworkRepresentation(IRadialMode mode) {
        return clazz.isInstance(mode) ? getNetworkRepresentation(clazz.cast(mode)) : 0;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (other == this) {
            return true;
        } else if (other == null || getClass() != other.getClass() || !super.equals(other)) {
            return false;
        }
        return clazz == ((ClassBasedRadialData<?>) other).clazz;
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + clazz.hashCode();
    }
}