package mekanism.client.render.item.machine;

import javax.annotation.Nonnull;
import mekanism.client.model.ModelSolarNeutronActivator;
import mekanism.client.render.GLSMHelper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderSolarNeutronActivatorItem {

    private static ModelSolarNeutronActivator solarNeutronActivator = new ModelSolarNeutronActivator();

    public static void renderStack(@Nonnull ItemStack stack, TransformType transformType) {
        GLSMHelper.INSTANCE.rotateZ(180, 1).scale(0.6F).translateY(-0.55F);
        MekanismRenderer.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "SolarNeutronActivator.png"));
        solarNeutronActivator.render(0.0625F);
    }
}