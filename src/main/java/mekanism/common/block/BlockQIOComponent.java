package mekanism.common.block;

import java.util.function.UnaryOperator;
import mekanism.common.attachments.FrequencyAware;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeStateActive;
import mekanism.common.block.prefab.BlockTile.BlockTileModel;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tile.qio.TileEntityQIOComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockQIOComponent<TILE extends TileEntityQIOComponent, BLOCK extends BlockTypeTile<TILE>> extends BlockTileModel<TILE, BLOCK> {

    public BlockQIOComponent(BLOCK type, UnaryOperator<Properties> propertiesModifier) {
        super(type, propertiesModifier);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state != null) {
            AttributeStateActive attribute = Attribute.get(state, AttributeStateActive.class);
            if (attribute != null) {
                ItemStack stack = context.getItemInHand();
                FrequencyAware<QIOFrequency> frequencyAware = stack.get(MekanismDataComponents.QIO_FREQUENCY);
                if (frequencyAware != null) {
                    if (context.getLevel().isClientSide) {
                        state = attribute.setActive(state, frequencyAware.identity().isPresent());
                    } else {
                        state = attribute.setActive(state, frequencyAware.frequency().isPresent());
                    }
                }
            }
        }
        return state;
    }
}