package mekanism.common.integration.lookingat.wthit;

import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IClientRegistrar;
import mcp.mobius.waila.api.ICommonAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.IEventListener;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.IWailaClientPlugin;
import mcp.mobius.waila.api.data.EnergyData;
import mcp.mobius.waila.api.data.FluidData;
import mekanism.common.block.BlockBounding;
import mekanism.common.entity.EntityRobit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("unused")
public class MekanismWTHITClientPlugin implements IWailaClientPlugin {

    @Override
    public void register(IClientRegistrar registration) {
        registration.body((IEntityComponentProvider) WTHITTooltipRenderer.INSTANCE, EntityRobit.class);
        registration.body((IBlockComponentProvider) WTHITTooltipRenderer.INSTANCE, Block.class);

        registration.eventListener(new IEventListener() {
            @Override
            public void onHandleTooltip(ITooltip tooltip, ICommonAccessor accessor, IPluginConfig config) {
                if (tooltip.getLine(MekanismWTHITPlugin.MEK_DATA) != null) {
                    //If we have mekanism data then clear out the default energy and fluid data as we handle that ourselves
                    // Note: Setting adds it if it is not present, so only set it if it is present
                    if (tooltip.getLine(EnergyData.ID) != null) {
                        tooltip.setLine(EnergyData.ID);
                    }
                    if (tooltip.getLine(FluidData.ID) != null) {
                        tooltip.setLine(FluidData.ID);
                    }
                    //TODO: Figure out how to remove the health bar for the robit from WTHIT
                    /*if (accessor.getEntity() instanceof EntityRobit && tooltip.getLine(ID) != null) {
                        tooltip.setLine(ID);
                    }*/
                }
            }
        });
        registration.override(new IBlockComponentProvider() {
            @Nullable
            @Override
            public BlockState getOverride(IBlockAccessor accessor, IPluginConfig config) {
                BlockHitResult result = accessor.getBlockHitResult();
                if (result.getType() != Type.MISS) {
                    Level level = accessor.getLevel();
                    BlockPos mainPos = BlockBounding.getMainBlockPos(level, result.getBlockPos());
                    if (mainPos != null) {
                        return level.getBlockState(mainPos);
                    }
                }
                return null;
            }
        }, BlockBounding.class);
    }
}