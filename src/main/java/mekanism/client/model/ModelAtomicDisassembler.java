package mekanism.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;

public class ModelAtomicDisassembler extends MekanismJavaModel {

    private static final ResourceLocation DISASSEMBLER_TEXTURE = MekanismUtils.getResource(ResourceType.RENDER, "atomic_disassembler.png");
    private static final RenderType BLADE_RENDER_TYPE = MekanismRenderType.bladeRender(DISASSEMBLER_TEXTURE);
    private final RenderType RENDER_TYPE = getRenderType(DISASSEMBLER_TEXTURE);

    private final ModelRenderer handle;
    private final ModelRenderer handleTop;
    private final ModelRenderer bladeBack;
    private final ModelRenderer head;
    private final ModelRenderer neck;
    private final ModelRenderer bladeFrontUpper;
    private final ModelRenderer bladeFrontLower;
    private final ModelRenderer neckAngled;
    private final ModelRenderer bladeFrontConnector;
    private final ModelRenderer bladeHolderBack;
    private final ModelRenderer bladeHolderMain;
    private final ModelRenderer bladeHolderFront;
    private final ModelRenderer rearBar;
    private final ModelRenderer bladeBackSmall;
    private final ModelRenderer handleBase;
    private final ModelRenderer handleTopBack;

    public ModelAtomicDisassembler() {
        super(RenderType::getEntitySolid);
        textureWidth = 64;
        textureHeight = 32;

        handle = new ModelRenderer(this, 0, 10);
        handle.addBox(0F, -1F, -3F, 1, 16, 1, false);
        handle.setRotationPoint(0F, 0F, 0F);
        handle.setTextureSize(64, 32);
        handle.mirror = true;
        setRotation(handle, 0F, 0F, 0F);
        handleTop = new ModelRenderer(this, 34, 9);
        handleTop.addBox(-0.5F, -3.5F, -3.5F, 2, 5, 2, false);
        handleTop.setRotationPoint(0F, 0F, 0F);
        handleTop.setTextureSize(64, 32);
        handleTop.mirror = true;
        setRotation(handleTop, 0F, 0F, 0F);
        bladeBack = new ModelRenderer(this, 42, 0);
        bladeBack.addBox(0F, -4F, -4F, 1, 2, 10, false);
        bladeBack.setRotationPoint(0F, 0F, 0F);
        bladeBack.setTextureSize(64, 32);
        bladeBack.mirror = true;
        setRotation(bladeBack, 0F, 0F, 0F);
        head = new ModelRenderer(this, 24, 0);
        head.addBox(-5F, -5.7F, -5.5F, 3, 3, 6, false);
        head.setRotationPoint(0F, 0F, 0F);
        head.setTextureSize(64, 32);
        head.mirror = true;
        setRotation(head, 0F, 0F, 0.7853982F);
        neck = new ModelRenderer(this, 0, 0);
        neck.addBox(-0.5F, -6F, -7F, 2, 2, 8, false);
        neck.setRotationPoint(0F, 0F, 0F);
        neck.setTextureSize(64, 32);
        neck.mirror = true;
        setRotation(neck, 0F, 0F, 0F);
        bladeFrontUpper = new ModelRenderer(this, 60, 0);
        bladeFrontUpper.addBox(0F, -0.5333334F, -9.6F, 1, 3, 1, false);
        bladeFrontUpper.setRotationPoint(0F, 0F, 0F);
        bladeFrontUpper.setTextureSize(64, 32);
        bladeFrontUpper.mirror = true;
        setRotation(bladeFrontUpper, -0.7853982F, 0F, 0F);
        bladeFrontLower = new ModelRenderer(this, 58, 0);
        bladeFrontLower.addBox(0F, -9.58F, -4F, 1, 5, 2, false);
        bladeFrontLower.setRotationPoint(0F, 0F, 0F);
        bladeFrontLower.setTextureSize(64, 32);
        bladeFrontLower.mirror = true;
        setRotation(bladeFrontLower, 0.7853982F, 0F, 0F);
        neckAngled = new ModelRenderer(this, 12, 0);
        neckAngled.addBox(-0.5F, -8.2F, -2.5F, 2, 1, 1, false);
        neckAngled.setRotationPoint(0F, 0F, 0F);
        neckAngled.setTextureSize(64, 32);
        neckAngled.mirror = true;
        setRotation(neckAngled, 0.7853982F, 0F, 0F);
        bladeFrontConnector = new ModelRenderer(this, 56, 0);
        bladeFrontConnector.addBox(0F, -2.44F, -6.07F, 1, 2, 3, false);
        bladeFrontConnector.setRotationPoint(0F, 0F, 0F);
        bladeFrontConnector.setTextureSize(64, 32);
        bladeFrontConnector.mirror = true;
        setRotation(bladeFrontConnector, 0F, 0F, 0F);
        bladeHolderBack = new ModelRenderer(this, 42, 14);
        bladeHolderBack.addBox(-0.5F, -0.5F, 3.5F, 2, 1, 1, false);
        bladeHolderBack.setRotationPoint(0F, -4F, 0F);
        bladeHolderBack.setTextureSize(64, 32);
        bladeHolderBack.mirror = true;
        setRotation(bladeHolderBack, 0F, 0F, 0F);
        bladeHolderMain = new ModelRenderer(this, 30, 16);
        bladeHolderMain.addBox(-0.5F, -3.5F, -1.5F, 2, 1, 4, false);
        bladeHolderMain.setRotationPoint(0F, 0F, 0F);
        bladeHolderMain.setTextureSize(64, 32);
        bladeHolderMain.mirror = true;
        setRotation(bladeHolderMain, 0F, 0F, 0F);
        bladeHolderFront = new ModelRenderer(this, 42, 12);
        bladeHolderFront.addBox(-0.5F, -4.5F, 1.5F, 2, 1, 1, false);
        bladeHolderFront.setRotationPoint(0F, 0F, 0F);
        bladeHolderFront.setTextureSize(64, 32);
        bladeHolderFront.mirror = true;
        setRotation(bladeHolderFront, 0F, 0F, 0F);
        rearBar = new ModelRenderer(this, 4, 10);
        rearBar.addBox(0F, -5.3F, 0F, 1, 1, 7, false);
        rearBar.setRotationPoint(0F, 0F, 0F);
        rearBar.setTextureSize(64, 32);
        rearBar.mirror = true;
        setRotation(rearBar, 0F, 0F, 0F);
        bladeBackSmall = new ModelRenderer(this, 60, 0);
        bladeBackSmall.addBox(0F, -4F, 6F, 1, 1, 1, false);
        bladeBackSmall.setRotationPoint(0F, 0F, 0F);
        bladeBackSmall.setTextureSize(64, 32);
        bladeBackSmall.mirror = true;
        setRotation(bladeBackSmall, 0F, 0F, 0F);
        handleBase = new ModelRenderer(this, 26, 9);
        handleBase.addBox(-0.5F, 15F, -3.5F, 2, 4, 2, false);
        handleBase.setRotationPoint(0F, 0F, 0F);
        handleBase.setTextureSize(64, 32);
        handleBase.mirror = true;
        setRotation(handleBase, 0F, 0F, 0F);
        handleTopBack = new ModelRenderer(this, 37, 0);
        handleTopBack.addBox(0F, -2F, -2F, 1, 4, 1, false);
        handleTopBack.setRotationPoint(0F, 0F, 0F);
        handleTopBack.setTextureSize(64, 32);
        handleTopBack.mirror = true;
        setRotation(handleTopBack, 0F, 0F, 0F);
    }

    public void render(@Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight, boolean hasEffect) {
        render(matrix, getVertexBuilder(renderer, RENDER_TYPE, hasEffect), light, overlayLight, 1, 1, 1, 1);
        renderBlade(matrix, getVertexBuilder(renderer, BLADE_RENDER_TYPE, hasEffect), MekanismRenderer.FULL_LIGHT, overlayLight, 1, 1, 1, 0.75F);
    }

    @Override
    public void render(@Nonnull MatrixStack matrix, @Nonnull IVertexBuilder vertexBuilder, int light, int overlayLight, float red, float green, float blue, float alpha) {
        handle.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        handleTop.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        head.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        neck.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        rearBar.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        neckAngled.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        bladeHolderBack.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        bladeHolderMain.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        bladeHolderFront.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        handleBase.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        handleTopBack.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
    }

    private void renderBlade(@Nonnull MatrixStack matrix, @Nonnull IVertexBuilder vertexBuilder, int light, int overlayLight, float red, float green, float blue, float alpha) {
        bladeFrontConnector.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        bladeBack.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        bladeFrontUpper.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        bladeFrontLower.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        bladeBackSmall.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
    }
}