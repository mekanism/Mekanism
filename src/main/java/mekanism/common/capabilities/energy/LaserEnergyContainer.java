package mekanism.common.capabilities.energy;

import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.math.FloatingLong;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.MethodsReturnNonnullByDefault;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LaserEnergyContainer extends BasicEnergyContainer {

    public static LaserEnergyContainer create(Predicate<@NonNull AutomationType> canExtract, Predicate<@NonNull AutomationType> canInsert, TileEntityMekanism tile,
          @Nullable IContentsListener listener) {
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        return new LaserEnergyContainer(MachineEnergyContainer.validateBlock(tile).getStorage(), canExtract, canInsert, listener);
    }

    private LaserEnergyContainer(FloatingLong maxEnergy, Predicate<@NonNull AutomationType> canExtract, Predicate<@NonNull AutomationType> canInsert,
          @Nullable IContentsListener listener) {
        super(maxEnergy, canExtract, canInsert, listener);
    }
}