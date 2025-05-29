package mekanism.common.lib.radial.data;

import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.radial.RadialData;
import mekanism.api.radial.mode.INestedRadialMode;
import mekanism.api.radial.mode.NestedRadialMode;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class NestingRadialData extends RadialData<NestedRadialMode> {

    private final List<NestedRadialMode> modes;

    public NestingRadialData(ResourceLocation identifier, List<NestedRadialMode> modes) {
        super(identifier);
        this.modes = modes;
    }

    @Nullable
    @Override
    public NestedRadialMode getDefaultMode(List<NestedRadialMode> modes) {
        return null;
    }

    @Override
    public List<NestedRadialMode> getModes() {
        return modes;
    }

    @Nullable
    @Override
    public INestedRadialMode fromIdentifier(ResourceLocation identifier) {
        //Override to remove need for checking instanceof and if it has any nested data
        for (NestedRadialMode nested : getModes()) {
            if (identifier.equals(nested.nestedData().getIdentifier())) {
                return nested;
            }
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
        return modes.equals(((NestingRadialData) other).modes);
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + modes.hashCode();
    }
}