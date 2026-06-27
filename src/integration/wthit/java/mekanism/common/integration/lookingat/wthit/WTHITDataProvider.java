package mekanism.common.integration.lookingat.wthit;

import mcp.mobius.waila.api.IDataProvider;
import mcp.mobius.waila.api.IDataWriter;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IServerAccessor;
import mekanism.common.integration.lookingat.LookingAtUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;

public class WTHITDataProvider implements IDataProvider<BlockEntity> {

    static final WTHITDataProvider INSTANCE = new WTHITDataProvider();

    @Override
    public void appendData(IDataWriter dataWriter, IServerAccessor<BlockEntity> serverAccessor, IPluginConfig config) {
        if (serverAccessor.getHitResult() instanceof BlockHitResult hitResult && hitResult.getType() != Type.MISS) {
            Level level = serverAccessor.getWorld();
            BlockEntity tile = serverAccessor.getTarget();
            BlockPos pos = hitResult.getBlockPos();
            BlockState state = tile == null ? level.getBlockState(pos) : tile.getBlockState();
            WTHITLookingAtHelper helper = new WTHITLookingAtHelper();
            LookingAtUtils.addInfoOrRedirect(helper, level, pos, state, tile, true, true);
            dataWriter.add(WTHITLookingAtHelper.TYPE, result -> result.add(helper));
        }
    }
}