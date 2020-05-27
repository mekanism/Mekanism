package mekanism.common.capabilities.chemical.dynamic;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.infuse.IInfusionTank;
import net.minecraft.util.Direction;

public interface IInfusionTracker extends IContentsListener {

    @Nonnull
    List<IInfusionTank> getInfusionTanks(@Nullable Direction side);
}