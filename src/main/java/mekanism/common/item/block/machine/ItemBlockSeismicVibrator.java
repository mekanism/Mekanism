package mekanism.common.item.block.machine;

import java.util.function.Consumer;
import mekanism.client.render.RenderPropertiesProvider;
import mekanism.common.block.prefab.BlockTile;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

public class ItemBlockSeismicVibrator extends ItemBlockMachine {

    public ItemBlockSeismicVibrator(BlockTile<?, ?> block) {
        super(block);
    }

    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(RenderPropertiesProvider.seismicVibrator());
    }
}