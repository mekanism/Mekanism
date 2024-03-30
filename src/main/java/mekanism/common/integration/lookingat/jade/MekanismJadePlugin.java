package mekanism.common.integration.lookingat.jade;

import mekanism.common.block.BlockBounding;
import mekanism.common.entity.EntityRobit;
import mekanism.common.integration.lookingat.LookingAtUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class MekanismJadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(JadeDataProvider.INSTANCE, BlockEntity.class);
        registration.registerEntityDataProvider(JadeEntityDataProvider.INSTANCE, EntityRobit.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.addConfig(LookingAtUtils.ENERGY, true);
        registration.addConfig(LookingAtUtils.FLUID, true);
        registration.addConfig(LookingAtUtils.GAS, true);
        registration.addConfig(LookingAtUtils.INFUSE_TYPE, true);
        registration.addConfig(LookingAtUtils.PIGMENT, true);
        registration.addConfig(LookingAtUtils.SLURRY, true);
        registration.registerEntityComponent(JadeTooltipRenderer.INSTANCE, EntityRobit.class);
        registration.registerBlockComponent(JadeTooltipRenderer.INSTANCE, Block.class);
        registration.registerEntityComponent(JadeBuiltinRemover.INSTANCE, EntityRobit.class);
        registration.registerBlockComponent(JadeBuiltinRemover.INSTANCE, Block.class);
        registration.addRayTraceCallback((hitResult, accessor, originalAccessor) -> {
            //Redirect bounding blocks to the main tile for purposes of naming and the like
            if (accessor instanceof BlockAccessor target && target.getBlockState().getBlock() instanceof BlockBounding) {
                Level level = target.getLevel();
                BlockHitResult blockHitResult = target.getHitResult();
                BlockPos mainPos = BlockBounding.getMainBlockPos(level, blockHitResult.getBlockPos());
                if (mainPos != null) {
                    return registration.blockAccessor()
                          .from(target)
                          .hit(blockHitResult.withPosition(mainPos))
                          .blockState(level.getBlockState(mainPos))
                          .blockEntity(WorldUtils.getTileEntity(level, mainPos))
                          .build();
                }
            }
            return accessor;
        });
    }
}