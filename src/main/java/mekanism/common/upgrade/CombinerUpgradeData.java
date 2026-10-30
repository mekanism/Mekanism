package mekanism.common.upgrade;

import java.util.List;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.recipe.lookup.monitor.ProgressRecipeCacheLookupMonitor;
import mekanism.common.tile.component.ITileComponent;
import mekanism.common.tile.interfaces.IRedstoneControl.RedstoneControl;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.util.ProblemReporter.PathElement;

public class CombinerUpgradeData extends MachineUpgradeData {

    public final InputInventorySlot extraSlot;

    //Combiner Constructor
    public CombinerUpgradeData(Provider provider, boolean redstone, RedstoneControl controlType, IEnergyContainer energyContainer, ProgressRecipeCacheLookupMonitor<?> cacheLookupMonitor,
          EnergyInventorySlot energySlot, InputInventorySlot extraSlot, InputInventorySlot inputSlot, OutputInventorySlot outputSlot, List<ITileComponent> components, PathElement problemPath) {
        super(provider, redstone, controlType, energyContainer, energySlot, cacheLookupMonitor, inputSlot, outputSlot, components, problemPath);
        this.extraSlot = extraSlot;
    }

    //Combining Factory Constructor
    public CombinerUpgradeData(Provider provider, boolean redstone, RedstoneControl controlType, IEnergyContainer energyContainer, EnergyInventorySlot energySlot,
          ProgressRecipeCacheLookupMonitor<?>[] cacheLookupMonitors, InputInventorySlot extraSlot, List<IInventorySlot> inputSlots,
          List<IInventorySlot> outputSlots, boolean sorting, List<ITileComponent> components, PathElement problemPath) {
        super(provider, redstone, controlType, energyContainer, energySlot, cacheLookupMonitors, inputSlots, outputSlots, sorting, components, problemPath);
        this.extraSlot = extraSlot;
    }
}