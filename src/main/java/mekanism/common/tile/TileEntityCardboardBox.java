package mekanism.common.tile;

import java.util.List;
import mekanism.api.SerializationConstants;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.tile.base.TileEntityUpdateable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueOutput;

//TODO: If we end up with more blocks where we care about the actual backing component, we should abstract some of this up into TileEntityUpdateable
public class TileEntityCardboardBox extends TileEntityUpdateable {

    public TileEntityCardboardBox(BlockPos pos, BlockState state) {
        super(MekanismTileEntityTypes.CARDBOARD_BOX, pos, state);
    }

    @Override
    public List<DataComponentType<?>> getRemapEntries() {
        List<DataComponentType<?>> remapEntries = super.getRemapEntries();
        if (!remapEntries.contains(MekanismDataComponents.BLOCK_DATA.get())) {
            //In general this won't contain it, but just in case we want to add to check it anyway,
            // as our list will be basically empty for the cardboard box
            remapEntries.add(MekanismDataComponents.BLOCK_DATA.get());
        }
        return remapEntries;
    }

    @Override
    public void writeReducedUpdatedTag(ValueOutput output) {
        super.writeReducedUpdatedTag(output);
        if (components().has(MekanismDataComponents.BLOCK_DATA.get())) {
            //If we have the block data component, just sync all the components to the client, as when handling the update tag
            // it deserializes any components and replaces the components with the new value
            output.store(SerializationConstants.COMPONENTS, DataComponentMap.CODEC, components());
        }
    }
}