package mekanism.common.upgrade;

import java.util.List;
import mekanism.api.chemical.gas.GasStack;
import mekanism.common.base.IRedstoneControl.RedstoneControl;
import mekanism.common.base.ITileComponent;
import mekanism.common.inventory.slot.GasInventorySlot;
import mekanism.common.tile.TileEntityGasTank.GasMode;
import net.minecraft.nbt.CompoundNBT;

public class GasTankUpgradeData implements IUpgradeData {

    public final boolean redstone;
    public final RedstoneControl controlType;
    public final GasInventorySlot drainSlot;
    public final GasInventorySlot fillSlot;
    public final GasMode dumping;
    public final GasStack stored;
    public final CompoundNBT components;

    public GasTankUpgradeData(boolean redstone, RedstoneControl controlType, GasInventorySlot drainSlot, GasInventorySlot fillSlot, GasMode dumping, GasStack stored,
          List<ITileComponent> components) {
        this.redstone = redstone;
        this.controlType = controlType;
        this.drainSlot = drainSlot;
        this.fillSlot = fillSlot;
        this.dumping = dumping;
        this.stored = stored;
        this.components = new CompoundNBT();
        for (ITileComponent component : components) {
            component.write(this.components);
        }
    }
}