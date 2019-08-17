package mekanism.common.inventory.container.tile.electric;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.recipe.machines.SmeltingRecipe;
import mekanism.common.tile.TileEntityEnergizedSmelter;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;

public class EnergizedSmelterContainer extends ElectricMachineContainer<SmeltingRecipe, TileEntityEnergizedSmelter> {

    public EnergizedSmelterContainer(int id, PlayerInventory inv, TileEntityEnergizedSmelter tile) {
        super(MekanismContainerTypes.ENERGIZED_SMELTER, id, inv, tile);
    }

    public EnergizedSmelterContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityEnergizedSmelter.class));
    }

    @Nullable
    @Override
    public Container createMenu(int i, @Nonnull PlayerInventory inv, @Nonnull PlayerEntity player) {
        return new EnergizedSmelterContainer(i, inv, tile);
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return TextComponentUtil.translate("mekanism.container.energized_smelter");
    }
}