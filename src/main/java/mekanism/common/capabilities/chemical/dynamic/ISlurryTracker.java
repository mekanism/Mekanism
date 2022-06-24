package mekanism.common.capabilities.chemical.dynamic;

import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.slurry.ISlurryTank;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

@MethodsReturnNonnullByDefault
public interface ISlurryTracker extends IContentsListener {

    List<ISlurryTank> getSlurryTanks(@Nullable Direction side);
}