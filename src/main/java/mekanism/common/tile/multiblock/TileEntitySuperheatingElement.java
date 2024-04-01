package mekanism.common.tile.multiblock;

import java.util.UUID;
import mekanism.common.content.boiler.BoilerMultiblockData;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.prefab.TileEntityInternalMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntitySuperheatingElement extends TileEntityInternalMultiblock {

    public TileEntitySuperheatingElement(BlockPos pos, BlockState state) {
        super(MekanismBlocks.SUPERHEATING_ELEMENT, pos, state);
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        UUID multiblockUUID = getMultiblockUUID();
        setActive(multiblockUUID != null && BoilerMultiblockData.hotMap.getBoolean(multiblockUUID));
        return sendUpdatePacket;
    }
}