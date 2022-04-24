package mekanism.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;

public abstract class MekanismISTER extends BlockEntityWithoutLevelRenderer {

    protected MekanismISTER() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    protected EntityModelSet getEntityModels() {
        //Just have this method as a helper for what we pass as entity models rather than bothering to
        // use an AT to access it directly
        return Minecraft.getInstance().getEntityModels();
    }

    @Override
    public abstract void onResourceManagerReload(@Nonnull ResourceManager resourceManager);

    @Override
    public abstract void renderByItem(@Nonnull ItemStack stack, @Nonnull TransformType transformType, @Nonnull PoseStack matrix, @Nonnull MultiBufferSource renderer,
          int light, int overlayLight);
}