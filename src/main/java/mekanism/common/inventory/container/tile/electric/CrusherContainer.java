package mekanism.common.inventory.container.tile.electric;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.recipe.machines.CrusherRecipe;
import mekanism.common.tile.TileEntityCrusher;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;

public class CrusherContainer extends ElectricMachineContainer<CrusherRecipe, TileEntityCrusher> {

    public CrusherContainer(int id, PlayerInventory inv, TileEntityCrusher tile) {
        super(MekanismContainerTypes.CRUSHER, id, inv, tile);
    }

    public CrusherContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityCrusher.class));
    }

    @Nullable
    @Override
    public Container createMenu(int i, @Nonnull PlayerInventory inv, @Nonnull PlayerEntity player) {
        return new CrusherContainer(i, inv, tile);
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return TextComponentUtil.translate("mekanism.container.crusher");
    }
}