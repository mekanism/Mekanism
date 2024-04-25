package mekanism.common.upgrade;

import java.util.List;
import mekanism.api.energy.IEnergyContainer;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.tile.component.ITileComponent;
import mekanism.common.tile.interfaces.IRedstoneControl.RedstoneControl;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public class EnergyCubeUpgradeData implements IUpgradeData {

    public final boolean redstone;
    public final RedstoneControl controlType;
    public final IEnergyContainer energyContainer;
    public final EnergyInventorySlot chargeSlot;
    public final EnergyInventorySlot dischargeSlot;
    public final CompoundTag components;

    public EnergyCubeUpgradeData(HolderLookup.Provider provider, boolean redstone, RedstoneControl controlType, IEnergyContainer energyContainer,
          EnergyInventorySlot chargeSlot, EnergyInventorySlot dischargeSlot, List<ITileComponent> components) {
        this.redstone = redstone;
        this.controlType = controlType;
        this.energyContainer = energyContainer;
        this.chargeSlot = chargeSlot;
        this.dischargeSlot = dischargeSlot;
        this.components = new CompoundTag();
        for (ITileComponent component : components) {
            component.write(this.components, provider);
        }
    }
}