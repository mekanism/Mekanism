package mekanism.generators.common.item.generator;

import java.util.function.Consumer;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.item.block.machine.ItemBlockMachine;
import mekanism.generators.client.render.GeneratorsRenderPropertiesProvider;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

public class ItemBlockWindGenerator extends ItemBlockMachine {

    public ItemBlockWindGenerator(BlockTile<?, ?> block) {
        super(block);
    }

    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(GeneratorsRenderPropertiesProvider.wind());
    }
}