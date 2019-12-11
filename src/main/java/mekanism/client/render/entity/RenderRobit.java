package mekanism.client.render.entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.client.model.ModelRobit;
import mekanism.common.entity.EntityRobit;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;

public class RenderRobit extends MobRenderer<EntityRobit, ModelRobit> {

    public RenderRobit(EntityRendererManager renderManager) {
        super(renderManager, new ModelRobit(), 0.5F);
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(@Nonnull EntityRobit robit) {
        if ((Math.abs(robit.posX - robit.prevPosX) + Math.abs(robit.posX - robit.prevPosX)) > 0.001) {
            if (robit.ticksExisted % 3 == 0) {
                robit.texTick = !robit.texTick;
            }
        }
        return MekanismUtils.getResource(ResourceType.RENDER, "robit" + (robit.texTick ? "2" : "") + ".png");
    }
}