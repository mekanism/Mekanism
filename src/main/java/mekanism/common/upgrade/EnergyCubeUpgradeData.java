package mekanism.common.upgrade;

import java.util.List;
import mekanism.api.energy.IEnergyContainer;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.tile.component.ITileComponent;
import mekanism.common.tile.interfaces.IRedstoneControl.RedstoneControl;
import net.minecraft.nbt.CompoundNBT;

public class EnergyCubeUpgradeData implements IUpgradeData {

    public final boolean redstone;
    public final RedstoneControl controlType;
    public final IEnergyContainer energyContainer;
    public final EnergyInventorySlot chargeSlot;
    public final EnergyInventorySlot dischargeSlot;
    public final CompoundNBT components;

    public EnergyCubeUpgradeData(boolean redstone, RedstoneControl controlType, IEnergyContainer energyContainer, EnergyInventorySlot chargeSlot, EnergyInventorySlot dischargeSlot,
          List<ITileComponent> components) {
        this.redstone = redstone;
        this.controlType = controlType;
        this.energyContainer = energyContainer;
        this.chargeSlot = chargeSlot;
        this.dischargeSlot = dischargeSlot;
        this.components = new CompoundNBT();
        for (ITileComponent component : components) {
            component.write(this.components);
        }
    }
}