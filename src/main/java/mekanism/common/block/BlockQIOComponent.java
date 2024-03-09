package mekanism.common.block;

import java.util.Optional;
import java.util.function.UnaryOperator;
import mekanism.common.attachments.FrequencyAware;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeStateActive;
import mekanism.common.block.prefab.BlockTile.BlockTileModel;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.registries.MekanismAttachmentTypes;
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
                Optional<FrequencyAware<?>> hasFrequency = stack.getExistingData(MekanismAttachmentTypes.FREQUENCY_AWARE)
                      //Should always be true but double check
                      .filter(frequencyAware -> frequencyAware.getFrequencyType() == FrequencyType.QIO)
                      .filter(context.getLevel().isClientSide ? frequencyAware -> frequencyAware.getIdentity() != null : frequencyAware -> frequencyAware.getFrequency() != null);
                state = attribute.setActive(state, hasFrequency.isPresent());
            }
        }
        return state;
    }
}