package mekanism.common.upgrade;

import java.util.List;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.resource.LargeResourceStack;
import mekanism.common.inventory.slot.chemical.ChemicalInventorySlot;
import mekanism.common.tile.TileEntityChemicalTank.GasMode;
import mekanism.common.tile.component.ITileComponent;
import mekanism.common.tile.interfaces.IRedstoneControl.RedstoneControl;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter.PathElement;

public class ChemicalTankUpgradeData implements IUpgradeData {

    public final boolean redstone;
    public final RedstoneControl controlType;
    public final ChemicalInventorySlot drainSlot;
    public final ChemicalInventorySlot fillSlot;
    public final GasMode dumping;
    public final LargeResourceStack<ChemicalResource> storedChemical;
    public final CompoundTag components;

    public ChemicalTankUpgradeData(Provider provider, boolean redstone, RedstoneControl controlType, ChemicalInventorySlot drainSlot, ChemicalInventorySlot fillSlot,
          GasMode dumping, LargeResourceStack<ChemicalResource> storedChemical, List<ITileComponent> components, PathElement problemPath) {
        this.redstone = redstone;
        this.controlType = controlType;
        this.drainSlot = drainSlot;
        this.fillSlot = fillSlot;
        this.dumping = dumping;
        this.storedChemical = storedChemical;
        this.components = IUpgradeData.readComponents(provider, components, problemPath);
    }
}