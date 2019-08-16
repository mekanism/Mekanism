package mekanism.common.inventory.container.tile.double_electric;

import javax.annotation.Nonnull;
import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.recipe.machines.CombinerRecipe;
import mekanism.common.tile.TileEntityCombiner;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;

public class CombinerContainer extends DoubleElectricMachineContainer<CombinerRecipe, TileEntityCombiner> {

    public CombinerContainer(int id, PlayerInventory inv, TileEntityCombiner tile) {
        super(MekanismContainerTypes.COMBINER, id, inv, tile);
    }

    public CombinerContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityCombiner.class));
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return TextComponentUtil.translate("mekanism.container.combiner");
    }
}