package mekanism.generators.client.render.item;

import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderHelper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.client.model.ModelWindGenerator;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderWindGeneratorItem {

    private static ModelWindGenerator windGenerator = new ModelWindGenerator();
    private static int angle = 0;

    public static void renderStack(@Nonnull ItemStack stack, TransformType transformType, MekanismRenderHelper renderHelper) {
        MekanismRenderHelper localRenderHelper = new MekanismRenderHelper(true).rotateZ(180, 1);
        if (transformType == TransformType.THIRD_PERSON_RIGHT_HAND || transformType == TransformType.THIRD_PERSON_LEFT_HAND) {
            localRenderHelper.rotateY(180, 1).translateY(0.4F);
            if (transformType == TransformType.THIRD_PERSON_LEFT_HAND) {
                localRenderHelper.rotateY(-45, 1);
            } else {
                localRenderHelper.rotateY(45, 1);
            }
            localRenderHelper.rotateX(50, 1).scale(2.0F).translateY(-0.4F);
        } else {
            if (transformType == TransformType.GUI) {
                localRenderHelper.rotateY(90, 1);
            } else if (transformType == TransformType.FIRST_PERSON_RIGHT_HAND) {
                localRenderHelper.rotateY(180, 1);
            }
            localRenderHelper.translateY(0.4F);
        }

        MekanismRenderer.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "WindGenerator.png"));
        //TODO: Only update angle if the player is not in a blacklisted dimension, one that has no "wind".
        //The best way to do this would be to add an event listener for dimension change.
        //The event is server side only so we would need to send a packet to clients to tell them if they are
        //in a blacklisted dimension or not.
        angle = (angle + 2) % 360;
        windGenerator.render(0.016F, angle);
        localRenderHelper.cleanup();
    }
}