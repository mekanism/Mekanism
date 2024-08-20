package mekanism.common.upgrade;

import java.util.List;
import mekanism.api.chemical.ChemicalStack;
import mekanism.common.inventory.slot.chemical.ChemicalInventorySlot;
import mekanism.common.tile.TileEntityChemicalTank.GasMode;
import mekanism.common.tile.component.ITileComponent;
import mekanism.common.tile.interfaces.IRedstoneControl.RedstoneControl;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public class ChemicalTankUpgradeData implements IUpgradeData {

    public final boolean redstone;
    public final RedstoneControl controlType;
    public final ChemicalInventorySlot drainSlot;
    public final ChemicalInventorySlot fillSlot;
    public final GasMode dumping;
    public final ChemicalStack storedChemical;
    public final CompoundTag components;

    public ChemicalTankUpgradeData(HolderLookup.Provider provider, boolean redstone, RedstoneControl controlType, ChemicalInventorySlot drainSlot, ChemicalInventorySlot fillSlot, GasMode dumping, ChemicalStack storedChemical, List<ITileComponent> components) {
        this.redstone = redstone;
        this.controlType = controlType;
        this.drainSlot = drainSlot;
        this.fillSlot = fillSlot;
        this.dumping = dumping;
        this.storedChemical = storedChemical;
        this.components = new CompoundTag();
        for (ITileComponent component : components) {
            component.write(this.components, provider);
        }
    }
}