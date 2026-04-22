package mekanism.common.upgrade;

import java.util.Collections;
import java.util.List;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.tile.component.ITileComponent;
import mekanism.common.tile.interfaces.IRedstoneControl.RedstoneControl;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.util.ProblemReporter.PathElement;

public class SawmillUpgradeData extends MachineUpgradeData {

    //Precision Sawmill Constructor
    public SawmillUpgradeData(Provider provider, boolean redstone, RedstoneControl controlType, IEnergyContainer energyContainer, int operatingTicks,
          EnergyInventorySlot energySlot, InputInventorySlot inputSlot, OutputInventorySlot outputSlot, OutputInventorySlot secondaryOutputSlot,
          List<ITileComponent> components, PathElement problemPath) {
        this(provider, redstone, controlType, energyContainer, new int[]{operatingTicks}, energySlot, Collections.singletonList(inputSlot),
              List.of(outputSlot, secondaryOutputSlot), false, components, problemPath);
    }

    //Sawing Factory Constructor
    public SawmillUpgradeData(Provider provider, boolean redstone, RedstoneControl controlType, IEnergyContainer energyContainer, int[] progress,
          EnergyInventorySlot energySlot, List<IInventorySlot> inputSlots, List<IInventorySlot> outputSlots, boolean sorting, List<ITileComponent> components, PathElement problemPath) {
        super(provider, redstone, controlType, energyContainer, progress, energySlot, inputSlots, outputSlots, sorting, components, problemPath);
    }
}