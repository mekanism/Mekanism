package mekanism.generators.common.item.generator;

import java.util.function.Consumer;
import javax.annotation.Nonnull;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.item.block.machine.ItemBlockMachine;
import mekanism.common.util.WorldUtils;
import mekanism.generators.client.render.GeneratorsRenderPropertiesProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.IItemRenderProperties;

public class ItemBlockWindGenerator extends ItemBlockMachine {

    public ItemBlockWindGenerator(BlockTile<?, ?> block) {
        super(block);
    }

    @Override
    public void initializeClient(@Nonnull Consumer<IItemRenderProperties> consumer) {
        consumer.accept(GeneratorsRenderPropertiesProvider.wind());
    }

    @Override
    public boolean placeBlock(@Nonnull BlockPlaceContext context, @Nonnull BlockState state) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        for (int yPos = 1; yPos < 5; yPos++) {
            BlockPos toCheck = pos.above(yPos);
            if (!WorldUtils.isValidReplaceableBlock(world, toCheck)) {
                //If there is not enough room, fail
                return false;
            }
        }
        return super.placeBlock(context, state);
    }
}