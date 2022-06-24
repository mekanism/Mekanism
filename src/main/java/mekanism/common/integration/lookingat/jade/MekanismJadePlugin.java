package mekanism.common.integration.lookingat.jade;

import mekanism.api.NBTConstants;
import mekanism.common.block.BlockBounding;
import mekanism.common.entity.EntityRobit;
import mekanism.common.integration.lookingat.LookingAtUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.TooltipPosition;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

@WailaPlugin
public class MekanismJadePlugin implements IWailaPlugin {

    private static final ResourceLocation FORGE_ENERGY = new ResourceLocation("fe");
    private static final ResourceLocation FORGE_FLUID = new ResourceLocation("fluid");

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
        registration.registerBlockComponent(new IBlockComponentProvider() {
            @Override
            public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
                if (accessor.getServerData().contains(NBTConstants.MEK_DATA, Tag.TAG_LIST)) {
                    tooltip.remove(FORGE_ENERGY);
                    tooltip.remove(FORGE_FLUID);
                }
            }

            @Override
            public ResourceLocation getUid() {
                return JadeConstants.REMOVE_BUILTIN;
            }

            @Override
            public int getDefaultPriority() {
                //Run in tail to ensure we are after the provider adding forge energy and fluid
                // so that we can remove it if we are adding our own
                return TooltipPosition.TAIL;
            }
        }, Block.class);
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