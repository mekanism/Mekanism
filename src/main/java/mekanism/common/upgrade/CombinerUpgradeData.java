package mekanism.common.upgrade;

import java.util.List;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.tile.component.ITileComponent;
import mekanism.common.tile.interfaces.IRedstoneControl.RedstoneControl;
import net.minecraft.core.HolderLookup;

public class CombinerUpgradeData extends MachineUpgradeData {

    public final InputInventorySlot extraSlot;

    //Combiner Constructor
    public CombinerUpgradeData(HolderLookup.Provider provider, boolean redstone, RedstoneControl controlType, IEnergyContainer energyContainer, int operatingTicks,
          EnergyInventorySlot energySlot, InputInventorySlot extraSlot, InputInventorySlot inputSlot, OutputInventorySlot outputSlot, List<ITileComponent> components) {
        super(provider, redstone, controlType, energyContainer, operatingTicks, energySlot, inputSlot, outputSlot, components);
        this.extraSlot = extraSlot;
    }

    //Combining Factory Constructor
    public CombinerUpgradeData(HolderLookup.Provider provider, boolean redstone, RedstoneControl controlType, IEnergyContainer energyContainer, int[] progress,
          EnergyInventorySlot energySlot, InputInventorySlot extraSlot, List<IInventorySlot> inputSlots, List<IInventorySlot> outputSlots, boolean sorting,
          List<ITileComponent> components) {
        super(provider, redstone, controlType, energyContainer, progress, energySlot, inputSlots, outputSlots, sorting, components);
        this.extraSlot = extraSlot;
    }
}