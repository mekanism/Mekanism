package mekanism.client.render.item.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import mekanism.client.model.ModelQuantumEntangloporter;
import mekanism.client.render.item.MekanismISTER;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class RenderQuantumEntangloporterItem extends MekanismISTER {

    public static final RenderQuantumEntangloporterItem RENDERER = new RenderQuantumEntangloporterItem();
    private ModelQuantumEntangloporter quantumEntangloporter;

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {
        quantumEntangloporter = new ModelQuantumEntangloporter(getEntityModels());
    }

    @Override
    public void renderByItem(@NotNull ItemStack stack, @NotNull TransformType transformType, @NotNull PoseStack matrix, @NotNull MultiBufferSource renderer, int light, int overlayLight) {
        matrix.pushPose();
        matrix.translate(0.5, 0.5, 0.5);
        matrix.mulPose(Vector3f.ZP.rotationDegrees(180));
        matrix.translate(0, -1, 0);
        //TODO: Try to get the main part rendering based on the json model instead
        quantumEntangloporter.render(matrix, renderer, light, overlayLight, true, stack.hasFoil());
        matrix.popPose();
    }
}