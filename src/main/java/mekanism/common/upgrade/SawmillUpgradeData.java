package mekanism.common.upgrade;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.tile.component.ITileComponent;
import mekanism.common.tile.interfaces.IRedstoneControl.RedstoneControl;

public class SawmillUpgradeData extends MachineUpgradeData {

    //Precision Sawmill Constructor
    public SawmillUpgradeData(boolean redstone, RedstoneControl controlType, IEnergyContainer energyContainer, int operatingTicks, EnergyInventorySlot energySlot,
          InputInventorySlot inputSlot, OutputInventorySlot outputSlot, OutputInventorySlot secondaryOutputSlot, List<ITileComponent> components) {
        this(redstone, controlType, energyContainer, new int[]{operatingTicks},
              energySlot, Collections.singletonList(inputSlot), Arrays.asList(outputSlot, secondaryOutputSlot), false, components);
    }

    //Sawing Factory Constructor
    public SawmillUpgradeData(boolean redstone, RedstoneControl controlType, IEnergyContainer energyContainer, int[] progress, EnergyInventorySlot energySlot,
          List<IInventorySlot> inputSlots, List<IInventorySlot> outputSlots, boolean sorting, List<ITileComponent> components) {
        super(redstone, controlType, energyContainer, progress, energySlot, inputSlots, outputSlots, sorting, components);
    }
}