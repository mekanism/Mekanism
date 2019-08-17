package mekanism.common.inventory.container.tile.advanced;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.recipe.machines.InjectionRecipe;
import mekanism.common.tile.TileEntityChemicalInjectionChamber;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;

public class ChemicalInjectionChamberContainer extends AdvancedElectricMachineContainer<InjectionRecipe, TileEntityChemicalInjectionChamber> {

    public ChemicalInjectionChamberContainer(int id, PlayerInventory inv, TileEntityChemicalInjectionChamber tile) {
        super(MekanismContainerTypes.CHEMICAL_INJECTION_CHAMBER, id, inv, tile);
    }

    public ChemicalInjectionChamberContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityChemicalInjectionChamber.class));
    }

    @Nullable
    @Override
    public Container createMenu(int i, @Nonnull PlayerInventory inv, @Nonnull PlayerEntity player) {
        return new ChemicalInjectionChamberContainer(i, inv, tile);
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return TextComponentUtil.translate("mekanism.container.chemical_injection_chamber");
    }
}