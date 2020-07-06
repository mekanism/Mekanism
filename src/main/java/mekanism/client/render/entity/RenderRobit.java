package mekanism.client.render.entity;

import javax.annotation.Nonnull;
import mekanism.client.model.ModelRobit;
import mekanism.common.entity.EntityRobit;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;

public class RenderRobit extends MobRenderer<EntityRobit, ModelRobit> {

    private static final ResourceLocation ROBIT = MekanismUtils.getResource(ResourceType.RENDER, "robit.png");
    private static final ResourceLocation ROBIT_ALT = MekanismUtils.getResource(ResourceType.RENDER, "robit2.png");

    public RenderRobit(EntityRendererManager renderManager) {
        super(renderManager, new ModelRobit(), 0.5F);
    }

    @Nonnull
    @Override
    public ResourceLocation getEntityTexture(@Nonnull EntityRobit robit) {
        if ((Math.abs(robit.getPosX() - robit.prevPosX) + Math.abs(robit.getPosX() - robit.prevPosX)) > 0.001) {
            if (robit.ticksExisted % 3 == 0) {
                robit.texTick = !robit.texTick;
            }
        }
        return robit.texTick ? ROBIT_ALT : ROBIT;
    }
}