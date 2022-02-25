package mekanism.common.integration.lookingat.hwyla;

import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.WailaPlugin;
import mcp.mobius.waila.api.event.WailaRayTraceEvent;
import mekanism.api.NBTConstants;
import mekanism.common.block.BlockBounding;
import mekanism.common.entity.EntityRobit;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.MinecraftForge;

@WailaPlugin
public class MekanismHwylaPlugin implements IWailaPlugin {

    public static final String TEXT = "text";
    public static final String CHEMICAL_STACK = "chemical";
    private static final ResourceLocation FORGE_ENERGY = new ResourceLocation("fe");
    private static final ResourceLocation FORGE_FLUID = new ResourceLocation("fluid");

    @Override
    @SuppressWarnings("UnstableApiUsage")//No need for limiting what version of jade we support. Unstable instead of deprecated because of how they annotated it
    public void register(IRegistrar registrar) {
        registrar.registerBlockDataProvider(HwylaDataProvider.INSTANCE, BlockEntity.class);
        registrar.registerEntityDataProvider(HwylaEntityDataProvider.INSTANCE, EntityRobit.class);
        registrar.registerComponentProvider(HwylaTooltipRenderer.INSTANCE, TooltipPosition.BODY, EntityRobit.class);
        registrar.registerComponentProvider(HwylaTooltipRenderer.INSTANCE, TooltipPosition.BODY, Block.class);
        registrar.registerComponentProvider((tooltip, accessor, config) -> {
            //Run in tail to ensure we are after the provider adding forge energy and fluid
            // so that we can remove it if we are adding our own
            if (accessor.getServerData().contains(NBTConstants.MEK_DATA, Tag.TAG_LIST)) {
                tooltip.remove(FORGE_ENERGY);
                tooltip.remove(FORGE_FLUID);
            }
        }, TooltipPosition.TAIL, Block.class);
        registrar.addConfig(HwylaTooltipRenderer.ENERGY, true);
        registrar.addConfig(HwylaTooltipRenderer.FLUID, true);
        registrar.addConfig(HwylaTooltipRenderer.GAS, true);
        registrar.addConfig(HwylaTooltipRenderer.INFUSE_TYPE, true);
        registrar.addConfig(HwylaTooltipRenderer.PIGMENT, true);
        registrar.addConfig(HwylaTooltipRenderer.SLURRY, true);
        MinecraftForge.EVENT_BUS.addListener((WailaRayTraceEvent event) -> {
            //Redirect bounding blocks to the main tile for purposes of naming and the like
            if (event.getAccessor() instanceof BlockAccessor target && target.getBlockState().getBlock() instanceof BlockBounding) {
                Level level = target.getLevel();
                BlockHitResult hitResult = target.getHitResult();
                BlockPos mainPos = BlockBounding.getMainBlockPos(level, hitResult.getBlockPos());
                if (mainPos != null) {
                    event.setAccessor(registrar.createBlockAccessor(level.getBlockState(mainPos), WorldUtils.getTileEntity(level, mainPos), level, target.getPlayer(),
                          target.getServerData(), hitResult.withPosition(mainPos), target.isServerConnected()));
                }
            }
        });
    }
}