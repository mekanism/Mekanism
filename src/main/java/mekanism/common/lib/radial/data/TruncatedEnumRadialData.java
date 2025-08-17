package mekanism.common.lib.radial.data;

import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.radial.ClassBasedRadialData;
import mekanism.api.radial.mode.IRadialMode;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class TruncatedEnumRadialData<MODE extends Enum<MODE> & IRadialMode> extends ClassBasedRadialData<MODE> {

    private final List<MODE> modes;
    private final MODE defaultMode;

    TruncatedEnumRadialData(ResourceLocation identifier, int accessibleValues, MODE defaultMode) {
        super(identifier, Objects.requireNonNull(defaultMode, "Default mode cannot be null.").getDeclaringClass());
        if (accessibleValues <= 0) {
            throw new IllegalArgumentException("Invalid accessibleValues, there must be at least one mode that is accessible.");
        }
        this.defaultMode = defaultMode;
        MODE[] constants = this.clazz.getEnumConstants();
        if (constants.length < accessibleValues) {
            throw new IllegalArgumentException("There are more accessible values than the number of elements in " + this.clazz.getSimpleName());
        } else if (constants.length == accessibleValues) {
            this.modes = List.of(constants);
        } else {
            if (this.defaultMode.ordinal() >= accessibleValues) {
                throw new IllegalArgumentException("Invalid default, it is out of range of the accessible values.");
            }
            this.modes = List.of(constants).subList(0, accessibleValues);
        }
    }

    @Override
    public MODE getDefaultMode(List<MODE> modes) {
        return defaultMode;
    }

    @Override
    public List<MODE> getModes() {
        return modes;
    }

    @Override
    public int index(List<MODE> modes, MODE mode) {
        return getNetworkRepresentation(mode);
    }

    @Override
    public int getNetworkRepresentation(MODE mode) {
        int networkRepresentation = mode.ordinal();
        if (networkRepresentation >= modes.size()) {
            return -1;
        }
        return networkRepresentation;
    }

    @Nullable
    @Override
    public MODE fromNetworkRepresentation(int networkRepresentation) {
        if (networkRepresentation >= 0 && networkRepresentation < modes.size()) {
            return modes.get(networkRepresentation);
        }
        return null;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (other == this) {
            return true;
        } else if (other == null || getClass() != other.getClass() || !super.equals(other)) {
            return false;
        }
        return modes.size() == ((TruncatedEnumRadialData<?>) other).modes.size();
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + modes.size();
        return result;
    }
}