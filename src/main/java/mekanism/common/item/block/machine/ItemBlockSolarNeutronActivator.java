package mekanism.common.item.block.machine;

import java.util.function.Consumer;
import javax.annotation.Nonnull;
import mekanism.client.render.RenderPropertiesProvider;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.content.blocktype.Machine;
import mekanism.common.tile.machine.TileEntitySolarNeutronActivator;
import net.minecraftforge.client.IItemRenderProperties;

public class ItemBlockSolarNeutronActivator extends ItemBlockMachine {

    public ItemBlockSolarNeutronActivator(BlockTile<TileEntitySolarNeutronActivator, Machine<TileEntitySolarNeutronActivator>> block) {
        super(block);
    }

    @Override
    public void initializeClient(@Nonnull Consumer<IItemRenderProperties> consumer) {
        consumer.accept(RenderPropertiesProvider.activator());
    }
}