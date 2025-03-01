package mekanism.tools.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Objects;
import mekanism.client.render.item.MekanismISTER;
import mekanism.common.Mekanism;
import mekanism.tools.client.ShieldTextures;
import mekanism.tools.common.registries.ToolsItems;
import net.minecraft.client.model.ShieldModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.jetbrains.annotations.NotNull;

public class RenderMekanismShieldItem extends MekanismISTER {

    public static final RenderMekanismShieldItem RENDERER = new RenderMekanismShieldItem();

    private ShieldModel shieldModel;

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {
        shieldModel = new ShieldModel(getEntityModels().bakeLayer(ModelLayers.SHIELD));
    }

    @Override
    public void renderByItem(@NotNull ItemStack stack, @NotNull ItemDisplayContext displayContext, @NotNull PoseStack matrix, @NotNull MultiBufferSource renderer,
          int light, int overlayLight) {
        ShieldTextures textures;
        if (stack.is(ToolsItems.BRONZE_SHIELD)) {
            textures = ShieldTextures.BRONZE;
        } else if (stack.is(ToolsItems.LAPIS_LAZULI_SHIELD)) {
            textures = ShieldTextures.LAPIS_LAZULI;
        } else if (stack.is(ToolsItems.OSMIUM_SHIELD)) {
            textures = ShieldTextures.OSMIUM;
        } else if (stack.is(ToolsItems.REFINED_GLOWSTONE_SHIELD)) {
            textures = ShieldTextures.REFINED_GLOWSTONE;
        } else if (stack.is(ToolsItems.REFINED_OBSIDIAN_SHIELD)) {
            textures = ShieldTextures.REFINED_OBSIDIAN;
        } else if (stack.is(ToolsItems.STEEL_SHIELD)) {
            textures = ShieldTextures.STEEL;
        } else {
            Mekanism.logger.warn("Unknown item for mekanism shield renderer: {}", stack.getItem());
            return;
        }
        Material material = textures.getBase();
        matrix.pushPose();
        matrix.scale(1, -1, -1);
        VertexConsumer buffer = material.sprite().wrap(ItemRenderer.getFoilBufferDirect(renderer, shieldModel.renderType(material.atlasLocation()), true, stack.hasFoil()));
        BannerPatternLayers bannerPattern = stack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
        DyeColor color = stack.get(DataComponents.BASE_COLOR);
        if (!bannerPattern.layers().isEmpty() || color != null) {
            shieldModel.handle().render(matrix, buffer, light, overlayLight, 0xFFFFFFFF);
            BannerRenderer.renderPatterns(matrix, renderer, light, overlayLight, shieldModel.plate(), material, false,
                  Objects.requireNonNullElse(color, DyeColor.WHITE), bannerPattern, stack.hasFoil());
        } else {
            shieldModel.renderToBuffer(matrix, buffer, light, overlayLight, 0xFFFFFFFF);
        }
        matrix.popPose();
    }
}