package mekanism.common.integration.lookingat.wthit;

import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.ICommonAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.IEventListener;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.data.EnergyData;
import mcp.mobius.waila.api.data.FluidData;
import mekanism.common.Mekanism;
import mekanism.common.block.BlockBounding;
import mekanism.common.entity.EntityRobit;
import mekanism.common.integration.lookingat.LookingAtUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class MekanismWTHITPlugin implements IWailaPlugin {

    static final ResourceLocation MEK_DATA = Mekanism.rl("wthit_data");

    @Override
    public void register(IRegistrar registration) {
        registration.addBlockData(WTHITDataProvider.INSTANCE, BlockEntity.class);
        registration.addEntityData(WTHITEntityDataProvider.INSTANCE, EntityRobit.class);
        registration.addConfig(LookingAtUtils.ENERGY, true);
        registration.addConfig(LookingAtUtils.FLUID, true);
        registration.addConfig(LookingAtUtils.CHEMICAL, true);
        registration.addComponent((IEntityComponentProvider) WTHITTooltipRenderer.INSTANCE, TooltipPosition.BODY, EntityRobit.class);
        registration.addComponent((IBlockComponentProvider) WTHITTooltipRenderer.INSTANCE, TooltipPosition.BODY, Block.class);
        registration.addDataType(WTHITLookingAtHelper.TYPE, WTHITLookingAtHelper.STREAM_CODEC);

        registration.addEventListener(new IEventListener() {
            @Override
            public void onHandleTooltip(ITooltip tooltip, ICommonAccessor accessor, IPluginConfig config) {
                if (tooltip.getLine(MEK_DATA) != null) {
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
        registration.addOverride(new IBlockComponentProvider() {
            @Nullable
            @Override
            public BlockState getOverride(IBlockAccessor accessor, IPluginConfig config) {
                BlockHitResult result = accessor.getBlockHitResult();
                if (result.getType() != Type.MISS) {
                    Level level = accessor.getWorld();
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