package mekanism.common.item.block.machine;

import java.util.function.Consumer;
import javax.annotation.Nonnull;
import mekanism.client.render.RenderPropertiesProvider;
import mekanism.common.block.prefab.BlockTile;
import net.minecraftforge.client.IItemRenderProperties;

public class ItemBlockSeismicVibrator extends ItemBlockMachine {

    public ItemBlockSeismicVibrator(BlockTile<?, ?> block) {
        super(block);
    }

    @Override
    public void initializeClient(@Nonnull Consumer<IItemRenderProperties> consumer) {
        consumer.accept(RenderPropertiesProvider.seismicVibrator());
    }
}