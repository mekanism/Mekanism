package mekanism.common.capabilities.energy;

import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.math.FloatingLong;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.inventory.AutomationType;
import mekanism.common.block.attribute.AttributeEnergy;
import mekanism.common.tile.base.TileEntityMekanism;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LaserEnergyContainer extends BasicEnergyContainer {

    public static LaserEnergyContainer create(Predicate<@NonNull AutomationType> canExtract, Predicate<@NonNull AutomationType> canInsert, TileEntityMekanism tile) {
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        AttributeEnergy electricBlock = MachineEnergyContainer.validateBlock(tile);
        return new LaserEnergyContainer(electricBlock.getStorage(), canExtract, canInsert, tile);
    }

    private LaserEnergyContainer(FloatingLong maxEnergy, Predicate<@NonNull AutomationType> canExtract, Predicate<@NonNull AutomationType> canInsert,
          @Nullable IMekanismStrictEnergyHandler energyHandler) {
        super(maxEnergy, canExtract, canInsert, energyHandler);
    }
}