package mekanism.common.inventory.container.tile.double_electric;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.recipe.machines.CombinerRecipe;
import mekanism.common.tile.TileEntityCombiner;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;

public class CombinerContainer extends DoubleElectricMachineContainer<CombinerRecipe, TileEntityCombiner> {

    public CombinerContainer(int id, PlayerInventory inv, TileEntityCombiner tile) {
        super(MekanismContainerTypes.COMBINER, id, inv, tile);
    }

    public CombinerContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityCombiner.class));
    }

    @Nullable
    @Override
    public Container createMenu(int i, @Nonnull PlayerInventory inv, @Nonnull PlayerEntity player) {
        return new CombinerContainer(i, inv, tile);
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return TextComponentUtil.translate("mekanism.container.combiner");
    }
}