package mekanism.common.lib.radial.data;

import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.MathUtils;
import mekanism.api.radial.ClassBasedRadialData;
import mekanism.api.radial.mode.IRadialMode;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class EnumRadialData<MODE extends Enum<MODE> & IRadialMode> extends ClassBasedRadialData<MODE> {

    private final List<MODE> modes;
    @Nullable
    private final MODE defaultMode;

    EnumRadialData(ResourceLocation identifier, MODE defaultMode) {
        super(identifier, Objects.requireNonNull(defaultMode, "Default mode cannot be null.").getDeclaringClass());
        //Store it in a list so that we don't have to convert it whenever we are accessing it
        this.modes = List.of(this.clazz.getEnumConstants());
        this.defaultMode = defaultMode;
    }

    EnumRadialData(ResourceLocation identifier, Class<MODE> enumClass) {
        super(identifier, enumClass);
        //Store it in a list so that we don't have to convert it whenever we are accessing it
        this.modes = List.of(this.clazz.getEnumConstants());
        this.defaultMode = this.modes.isEmpty() ? null : this.modes.getFirst();
    }

    @Nullable
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
        return mode.ordinal();
    }

    @Override
    public int getNetworkRepresentation(MODE mode) {
        return mode.ordinal();
    }

    @Override
    public MODE fromNetworkRepresentation(int networkRepresentation) {
        return MathUtils.getByIndexMod(modes, networkRepresentation);
    }
}