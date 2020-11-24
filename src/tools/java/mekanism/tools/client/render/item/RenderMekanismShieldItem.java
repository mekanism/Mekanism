package mekanism.tools.client.render.item;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.NBTConstants;
import mekanism.common.Mekanism;
import mekanism.tools.client.ShieldTextures;
import mekanism.tools.common.registries.ToolsItems;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.tileentity.BannerTileEntityRenderer;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.tileentity.BannerTileEntity;

public class RenderMekanismShieldItem extends ItemStackTileEntityRenderer {

    @Override
    public void func_239207_a_(@Nonnull ItemStack stack, @Nonnull TransformType transformType, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight) {
        Item item = stack.getItem();
        ShieldTextures textures;
        if (item == ToolsItems.BRONZE_SHIELD.getItem()) {
            textures = ShieldTextures.BRONZE;
        } else if (item == ToolsItems.LAPIS_LAZULI_SHIELD.getItem()) {
            textures = ShieldTextures.LAPIS_LAZULI;
        } else if (item == ToolsItems.OSMIUM_SHIELD.getItem()) {
            textures = ShieldTextures.OSMIUM;
        } else if (item == ToolsItems.REFINED_GLOWSTONE_SHIELD.getItem()) {
            textures = ShieldTextures.REFINED_GLOWSTONE;
        } else if (item == ToolsItems.REFINED_OBSIDIAN_SHIELD.getItem()) {
            textures = ShieldTextures.REFINED_OBSIDIAN;
        } else if (item == ToolsItems.STEEL_SHIELD.getItem()) {
            textures = ShieldTextures.STEEL;
        } else {
            Mekanism.logger.warn("Unknown item for mekanism shield renderer: {}", item.getRegistryName());
            return;
        }
        RenderMaterial material = textures.getBase();
        matrix.push();
        matrix.scale(1, -1, -1);
        IVertexBuilder buffer = material.getSprite().wrapBuffer(ItemRenderer.getEntityGlintVertexBuilder(renderer, modelShield.getRenderType(material.getAtlasLocation()), true, stack.hasEffect()));
        if (stack.getChildTag(NBTConstants.BLOCK_ENTITY_TAG) != null) {
            modelShield.func_228294_b_().render(matrix, buffer, light, overlayLight, 1, 1, 1, 1);
            List<Pair<BannerPattern, DyeColor>> list = BannerTileEntity.getPatternColorData(ShieldItem.getColor(stack), BannerTileEntity.getPatternData(stack));
            BannerTileEntityRenderer.func_230180_a_(matrix, renderer, light, overlayLight, modelShield.func_228293_a_(), material, false, list);
        } else {
            modelShield.render(matrix, buffer, light, overlayLight, 1, 1, 1, 1);
        }
        matrix.pop();
    }
}