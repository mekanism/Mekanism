package mekanism.generators.common.item.generator;

import java.util.function.Consumer;
import javax.annotation.Nonnull;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.item.block.machine.ItemBlockMachine;
import mekanism.generators.client.render.GeneratorsRenderPropertiesProvider;
import net.minecraftforge.client.IItemRenderProperties;

public class ItemBlockWindGenerator extends ItemBlockMachine {

    public ItemBlockWindGenerator(BlockTile<?, ?> block) {
        super(block);
    }

    @Override
    public void initializeClient(@Nonnull Consumer<IItemRenderProperties> consumer) {
        consumer.accept(GeneratorsRenderPropertiesProvider.wind());
    }
}