package mekanism.common.capabilities.chemical.dynamic;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.pigment.IPigmentTank;
import net.minecraft.util.Direction;

public interface IPigmentTracker extends IContentsListener {

    @Nonnull
    List<IPigmentTank> getPigmentTanks(@Nullable Direction side);
}