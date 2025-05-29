package mekanism.api.radial;

import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.radial.mode.INestedRadialMode;
import mekanism.api.radial.mode.IRadialMode;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * Base abstraction for providing functionality and required data to radials.
 *
 * @param <MODE> Radial Mode.
 *
 * @since 10.3.2
 */
@NothingNullByDefault
public abstract class RadialData<MODE extends IRadialMode> {

    private final ResourceLocation identifier;

    /**
     * @param identifier Identifier representing this radial data. Must be unique within the radial level if this is a nested radial element.
     */
    protected RadialData(ResourceLocation identifier) {
        this.identifier = Objects.requireNonNull(identifier, "Identifier cannot be null.");
    }

    /**
     * Gets a "unique" identifier for this radial data for networking tree purposes.
     */
    public final ResourceLocation getIdentifier() {
        return identifier;
    }

    /**
     * Tries to get a nested mode from the given identifier. In the majority of cases this will return {@code null}.
     *
     * @param identifier Identifier of child radial data.
     *
     * @return Nested mode or {@code null} if this radial data doesn't support nested modes or there is no child with the matching name.
     */
    @Nullable
    public INestedRadialMode fromIdentifier(ResourceLocation identifier) {
        for (MODE mode : getModes()) {
            //noinspection ConstantConditions: not null, validated by hasNestedData
            if (mode instanceof INestedRadialMode nested && nested.hasNestedData() && identifier.equals(nested.nestedData().getIdentifier())) {
                return nested;
            }
        }
        return null;
    }

    /**
     * Gets the list of currently available modes. It is recommended to store this in a local variable for use in querying other methods.
     */
    public abstract List<MODE> getModes();

    /**
     * Gets the default (fallback) mode from the list of currently available modes.
     *
     * @param modes List of currently available modes.
     *
     * @return Default mode or {@code null} if there is no default.
     */
    @Nullable
    public MODE getDefaultMode(List<MODE> modes) {
        return null;
    }

    /**
     * Gets the index of the given mode for this radial data. This will be the same as the index of the element in the given list presuming that the list of modes was
     * retrieved from {@link #getModes()}.
     *
     * @param modes List of currently available modes. May not be used if there is a more efficient index lookup possible.
     * @param mode  Mode to lookup.
     *
     * @return Index of mode in the list of modes or {@code -1} if the mode is not part of this radial data.
     */
    public int index(List<MODE> modes, MODE mode) {
        return modes.indexOf(mode);
    }

    /**
     * Gets the index of the given mode for this radial data. This will be the same as the index of the element in the given list presuming that the list of modes was
     * retrieved from {@link #getModes()}.
     *
     * @param modes List of currently available modes. May not be used if there is a more efficient index lookup possible.
     * @param mode  Mode to lookup.
     *
     * @return Index of mode in the list of modes or {@code -1} if the mode is not part of this radial data.
     *
     * @apiNote Helper for {@link #index(List, IRadialMode)} that returns {@code -1} when the given mode is {@code null} to cut down on nesting if statements.
     */
    public final int indexNullable(List<MODE> modes, @Nullable MODE mode) {
        return mode == null ? -1 : index(modes, mode);
    }

    /**
     * Tries to get the corresponding integer network representation for the given mode.
     *
     * @param mode Unchecked mode.
     *
     * @return An integer corresponding to the network representation of the given object or {@code -1} if the object is of the wrong type or there is no network
     * representation.
     *
     * @apiNote Helper for {@link #getNetworkRepresentation(IRadialMode)} that tries to validate the type matches the type of the current radial data.
     */
    public int tryGetNetworkRepresentation(IRadialMode mode) {
        return -1;
    }

    /**
     * Get the corresponding integer network representation for the given mode.
     *
     * @param mode Mode.
     *
     * @return An integer corresponding to the network representation of the given object or {@code -1} if  there is no network representation.
     *
     * @implNote When implementing this method, if you are not extending {@link ClassBasedRadialData} it is important to also override
     * {@link #tryGetNetworkRepresentation(IRadialMode)}.
     */
    public int getNetworkRepresentation(MODE mode) {
        return -1;
    }

    /**
     * Gets the mode corresponding to the given integer network representation.
     *
     * @param networkRepresentation Network representation
     *
     * @return Mode or {@code null} if no matching mode could be found.
     */
    @Nullable
    public MODE fromNetworkRepresentation(int networkRepresentation) {
        return null;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        } else if (other == null || getClass() != other.getClass()) {
            return false;
        }
        return identifier.equals(((RadialData<?>) other).identifier);
    }

    @Override
    public int hashCode() {
        return identifier.hashCode();
    }
}