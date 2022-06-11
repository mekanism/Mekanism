package mekanism.common.integration.lookingat.wthit;

import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.WailaPlugin;
import mekanism.common.Mekanism;
import mekanism.common.block.BlockBounding;
import mekanism.common.entity.EntityRobit;
import mekanism.common.integration.lookingat.LookingAtUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import org.jetbrains.annotations.Nullable;

@WailaPlugin(id = Mekanism.MODID)
public class MekanismWTHITPlugin implements IWailaPlugin {

    @Override
    public void register(IRegistrar registration) {
        registration.addBlockData(WTHITDataProvider.INSTANCE, BlockEntity.class);
        registration.addEntityData(WTHITEntityDataProvider.INSTANCE, EntityRobit.class);
        registration.addConfig(LookingAtUtils.ENERGY, true);
        registration.addConfig(LookingAtUtils.FLUID, true);
        registration.addConfig(LookingAtUtils.GAS, true);
        registration.addConfig(LookingAtUtils.INFUSE_TYPE, true);
        registration.addConfig(LookingAtUtils.PIGMENT, true);
        registration.addConfig(LookingAtUtils.SLURRY, true);
        registration.addComponent((IEntityComponentProvider) WTHITTooltipRenderer.INSTANCE, TooltipPosition.BODY, EntityRobit.class);
        registration.addComponent((IBlockComponentProvider) WTHITTooltipRenderer.INSTANCE, TooltipPosition.BODY, Block.class);

        registration.addOverride(new IBlockComponentProvider() {
            @Override
            public @Nullable BlockState getOverride(IBlockAccessor accessor, IPluginConfig config) {
                if (accessor.getHitResult() instanceof BlockHitResult result && result.getType() != Type.MISS) {
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