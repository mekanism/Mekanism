package mekanism.client.render.entity;

import mekanism.client.model.ModelRobit;
import mekanism.common.entity.EntityRobit;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderRobit extends RenderLiving<EntityRobit> {

    public RenderRobit(RenderManager renderManager) {
        super(renderManager, new ModelRobit(), 0.5F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityRobit robit) {
        if ((Math.abs(robit.posX - robit.prevPosX) + Math.abs(robit.posX - robit.prevPosX)) > 0.001) {
            if (robit.ticksExisted % 3 == 0) {
                robit.texTick = !robit.texTick;
            }
        }

        return MekanismUtils.getResource(ResourceType.RENDER, "Robit" + (robit.texTick ? "2" : "") + ".png");
    }
}
