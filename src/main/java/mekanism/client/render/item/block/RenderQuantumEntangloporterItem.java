package mekanism.client.render.item.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import javax.annotation.Nonnull;
import mekanism.client.model.ModelQuantumEntangloporter;
import mekanism.client.render.item.MekanismISTER;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;

public class RenderQuantumEntangloporterItem extends MekanismISTER {

    public static final RenderQuantumEntangloporterItem RENDERER = new RenderQuantumEntangloporterItem();
    private ModelQuantumEntangloporter quantumEntangloporter;

    @Override
    public void onResourceManagerReload(@Nonnull ResourceManager resourceManager) {
        quantumEntangloporter = new ModelQuantumEntangloporter(getEntityModels());
    }

    @Override
    public void renderByItem(@Nonnull ItemStack stack, @Nonnull TransformType transformType, @Nonnull PoseStack matrix, @Nonnull MultiBufferSource renderer, int light, int overlayLight) {
        matrix.pushPose();
        matrix.translate(0.5, 0.5, 0.5);
        matrix.mulPose(Vector3f.ZP.rotationDegrees(180));
        matrix.translate(0, -1, 0);
        //TODO: Try to get the main part rendering based on the json model instead
        quantumEntangloporter.render(matrix, renderer, light, overlayLight, true, stack.hasFoil());
        matrix.popPose();
    }
}