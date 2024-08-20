package mekanism.common.capabilities.chemical;

import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.IChemicalTank;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

@MethodsReturnNonnullByDefault
public interface IChemicalTracker extends IContentsListener {

    List<IChemicalTank> getChemicalTanks(@Nullable Direction side);
}