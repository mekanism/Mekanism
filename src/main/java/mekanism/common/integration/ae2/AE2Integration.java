package mekanism.common.integration.ae2;

import appeng.client.render.cablebus.CableBusRenderState;
import appeng.client.render.cablebus.FacadeRenderState;
import appeng.core.definitions.AEItems;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.EnumUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class AE2Integration {

    public static CableBusRenderState getFacadeRenderData(TileEntityTransmitter transmitter, BlockGetter level) {
        CableBusRenderState renderState = new CableBusRenderState();
        renderState.setPos(transmitter.getBlockPos());
        //Sets null attachments on all connected sides so the anchor is not rendered
        for (Direction side : EnumUtils.DIRECTIONS) {
            if (transmitter.getTransmitter().getConnectionType(side) != ConnectionType.NONE) {
                renderState.getAttachments().put(side, null);
            }
        }
        for (Direction side : EnumUtils.DIRECTIONS) {
            Block facade = transmitter.getTransmitter().getFacade(side);
            if (facade != null) {
                BlockState state = facade.defaultBlockState();
                renderState.getFacades().put(side, new FacadeRenderState(state, !state.isSolidRender(level, transmitter.getBlockPos())));
            }
        }
        return renderState;
    }

    public static ItemStack getFacadeItem(Block facade) {
        return AEItems.FACADE.asItem().createFacadeForItemUnchecked(new ItemStack(facade));
    }

}
