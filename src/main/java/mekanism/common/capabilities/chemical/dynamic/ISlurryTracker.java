package mekanism.common.capabilities.chemical.dynamic;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.chemical.slurry.ISlurryTank;
import net.minecraft.util.Direction;

public interface ISlurryTracker {

    @Nonnull
    List<ISlurryTank> getSlurryTanks(@Nullable Direction side);

    void onContentsChanged();
}