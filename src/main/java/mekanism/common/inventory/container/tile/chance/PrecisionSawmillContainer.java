package mekanism.common.inventory.container.tile.chance;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.recipe.machines.SawmillRecipe;
import mekanism.common.tile.TileEntityPrecisionSawmill;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;

public class PrecisionSawmillContainer extends ChanceMachineContainer<SawmillRecipe, TileEntityPrecisionSawmill> {

    public PrecisionSawmillContainer(int id, PlayerInventory inv, TileEntityPrecisionSawmill tile) {
        super(MekanismContainerTypes.PRECISION_SAWMILL, id, inv, tile);
    }

    public PrecisionSawmillContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityPrecisionSawmill.class));
    }

    @Nullable
    @Override
    public Container createMenu(int i, @Nonnull PlayerInventory inv, @Nonnull PlayerEntity player) {
        return new PrecisionSawmillContainer(i, inv, tile);
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return TextComponentUtil.translate("mekanism.container.precision_sawmill");
    }
}