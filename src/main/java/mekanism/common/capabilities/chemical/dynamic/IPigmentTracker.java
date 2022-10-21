package mekanism.common.capabilities.chemical.dynamic;

import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.pigment.IPigmentTank;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

@MethodsReturnNonnullByDefault
public interface IPigmentTracker extends IContentsListener {

    List<IPigmentTank> getPigmentTanks(@Nullable Direction side);
}