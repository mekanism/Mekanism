package mekanism.common.lib.radial.data;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import mekanism.api.IDisableableEnum;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.MathUtils;
import mekanism.api.radial.ClassBasedRadialData;
import mekanism.api.radial.mode.IRadialMode;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class DisableableEnumRadialData<MODE extends Enum<MODE> & IDisableableEnum<MODE> & IRadialMode> extends ClassBasedRadialData<MODE> {

    private final MODE[] modes;
    @Nullable
    private final MODE defaultMode;

    DisableableEnumRadialData(ResourceLocation identifier, MODE defaultMode) {
        super(identifier, Objects.requireNonNull(defaultMode, "Default mode cannot be null.").getDeclaringClass());
        this.modes = this.clazz.getEnumConstants();
        this.defaultMode = defaultMode;
    }

    DisableableEnumRadialData(ResourceLocation identifier, Class<MODE> enumClass) {
        super(identifier, enumClass);
        this.modes = this.clazz.getEnumConstants();
        this.defaultMode = this.modes.length == 0 ? null : this.modes[0];
    }

    @Nullable
    @Override
    public MODE getDefaultMode(List<MODE> modes) {
        return defaultMode;
    }

    @Override
    public List<MODE> getModes() {
        return Arrays.stream(modes).filter(IDisableableEnum::isEnabled).toList();
    }

    @Override
    public int index(List<MODE> modes, MODE mode) {
        if (modes.size() == this.modes.length) {
            //No modes are disabled just return ordinal rather than finding actual index
            return mode.ordinal();
        }
        return super.index(modes, mode);
    }

    @Override
    public int getNetworkRepresentation(MODE mode) {
        return mode.isEnabled() ? mode.ordinal() : -1;
    }

    @Nullable
    @Override
    public MODE fromNetworkRepresentation(int networkRepresentation) {
        MODE mode = MathUtils.getByIndexMod(modes, networkRepresentation);
        return mode.isEnabled() ? mode : defaultMode;
    }
}