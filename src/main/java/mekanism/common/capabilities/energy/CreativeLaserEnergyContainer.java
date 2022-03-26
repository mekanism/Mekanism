package mekanism.common.capabilities.energy;

import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NonNull;
import mekanism.api.math.FloatingLong;
import mekanism.common.tile.base.TileEntityMekanism;

public class CreativeLaserEnergyContainer extends LaserEnergyContainer {

    private CreativeLaserEnergyContainer(Predicate<@NonNull AutomationType> canExtract, Predicate<@NonNull AutomationType> canInsert,
          @Nullable IContentsListener listener) {
        super(FloatingLong.MAX_VALUE, canExtract, canInsert, listener);
    }

    public static CreativeLaserEnergyContainer create(Predicate<@NonNull AutomationType> canExtract, Predicate<@NonNull AutomationType> canInsert, TileEntityMekanism tile,
          @Nullable IContentsListener listener) {
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        return new CreativeLaserEnergyContainer(canExtract, canInsert, listener);
    }

    @Override
    public FloatingLong extract(FloatingLong amount, Action action, AutomationType automationType) {
        if (amount.isZero() || !canExtract.test(automationType)) {
            return FloatingLong.ZERO;
        }
        return amount.copy();
    }
}
