package mekanism.common.capabilities.chemical.dynamic;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.gas.IGasTank;
import net.minecraft.util.Direction;

public interface IGasTracker extends IContentsListener {

    @Nonnull
    List<IGasTank> getGasTanks(@Nullable Direction side);
}