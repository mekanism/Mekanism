package mekanism.common.item.block.machine;

import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import mekanism.client.render.RenderPropertiesProvider;
import mekanism.common.block.prefab.BlockTile.BlockTileModel;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import mekanism.common.util.MekanismUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.IItemRenderProperties;

public class ItemBlockQuantumEntangloporter extends ItemBlockTooltip<BlockTileModel<TileEntityQuantumEntangloporter, BlockTypeTile<TileEntityQuantumEntangloporter>>> {

    public ItemBlockQuantumEntangloporter(BlockTileModel<TileEntityQuantumEntangloporter, BlockTypeTile<TileEntityQuantumEntangloporter>> block) {
        super(block);
    }

    @Override
    public void initializeClient(@Nonnull Consumer<IItemRenderProperties> consumer) {
        consumer.accept(RenderPropertiesProvider.entangloporter());
    }

    @Override
    protected void addStats(@Nonnull ItemStack stack, Level world, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        MekanismUtils.addFrequencyToTileTooltip(stack, FrequencyType.INVENTORY, tooltip);
    }
}