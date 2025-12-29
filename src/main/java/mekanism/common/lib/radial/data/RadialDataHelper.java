package mekanism.common.lib.radial.data;

import mekanism.api.IDisableableEnum;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.radial.IRadialDataHelper;
import mekanism.api.radial.RadialData;
import mekanism.api.radial.mode.IRadialMode;
import net.minecraft.resources.Identifier;

/**
 * @apiNote Do not instantiate this class directly as it will be done via the service loader. Instead, access instances of this via {@link IRadialDataHelper#INSTANCE}
 */
@NothingNullByDefault
public class RadialDataHelper implements IRadialDataHelper {

    @Override
    public <MODE extends Enum<MODE> & IRadialMode> RadialData<MODE> dataForEnum(Identifier identifier, MODE defaultMode) {
        if (defaultMode instanceof IDisableableEnum) {
            //noinspection rawtypes,unchecked
            return new DisableableEnumRadialData(identifier, defaultMode);
        }
        return new EnumRadialData<>(identifier, defaultMode);
    }

    @Override
    public <MODE extends Enum<MODE> & IRadialMode> RadialData<MODE> dataForEnum(Identifier identifier, Class<MODE> enumClass) {
        if (IDisableableEnum.class.isAssignableFrom(enumClass)) {
            //noinspection rawtypes,unchecked
            return new DisableableEnumRadialData(identifier, enumClass);
        }
        return new EnumRadialData<>(identifier, enumClass);
    }

    @Override
    public <MODE extends Enum<MODE> & IRadialMode> RadialData<MODE> dataForTruncated(Identifier identifier, int accessibleValues, MODE defaultMode) {
        return new TruncatedEnumRadialData<>(identifier, accessibleValues, defaultMode);
    }

    @Override
    public RadialData<IRadialMode> booleanBasedData(Identifier identifier, BooleanRadialModes modes, boolean defaultValue) {
        return new BooleanRadialData(identifier, modes, defaultValue);
    }
}