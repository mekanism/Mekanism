package mekanism.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.common.tier.FluidTankTier;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;

//TODO: Replace usage of this by using the json model and drawing fluid inside of it?
public class ModelFluidTank extends MekanismJavaModel {

    private static final ResourceLocation TANK_TEXTURE = MekanismUtils.getResource(ResourceType.RENDER, "fluid_tank.png");
    private static final RenderType GLASS_RENDER_TYPE = RenderType.getEntityCutout(TANK_TEXTURE);
    private final RenderType RENDER_TYPE = getRenderType(TANK_TEXTURE);

    private final ModelRenderer Base;
    private final ModelRenderer PoleFL;
    private final ModelRenderer PoleLB;
    private final ModelRenderer PoleBR;
    private final ModelRenderer PoleRF;
    private final ModelRenderer Top;
    private final ModelRenderer FrontGlass;
    private final ModelRenderer BackGlass;
    private final ModelRenderer RightGlass;
    private final ModelRenderer LeftGlass;

    public ModelFluidTank() {
        super(RenderType::getEntitySolid);
        textureWidth = 128;
        textureHeight = 128;

        Base = new ModelRenderer(this, 0, 0);
        Base.addBox(0F, 0F, 0F, 12, 1, 12, false);
        Base.setRotationPoint(-6F, 23F, -6F);
        Base.setTextureSize(128, 128);
        Base.mirror = true;
        setRotation(Base, 0F, 0F, 0F);
        PoleFL = new ModelRenderer(this, 48, 0);
        PoleFL.addBox(0F, 0F, 0F, 1, 14, 1, false);
        PoleFL.setRotationPoint(5F, 9F, -6F);
        PoleFL.setTextureSize(128, 128);
        PoleFL.mirror = true;
        setRotation(PoleFL, 0F, 0F, 0F);
        PoleLB = new ModelRenderer(this, 48, 0);
        PoleLB.addBox(0F, 0F, 0F, 1, 14, 1, false);
        PoleLB.setRotationPoint(5F, 9F, 5F);
        PoleLB.setTextureSize(128, 128);
        PoleLB.mirror = true;
        setRotation(PoleLB, 0F, 0F, 0F);
        PoleBR = new ModelRenderer(this, 48, 0);
        PoleBR.addBox(0F, 0F, 0F, 1, 14, 1, false);
        PoleBR.setRotationPoint(-6F, 9F, 5F);
        PoleBR.setTextureSize(128, 128);
        PoleBR.mirror = true;
        setRotation(PoleBR, 0F, 0F, 0F);
        PoleRF = new ModelRenderer(this, 48, 0);
        PoleRF.addBox(0F, 0F, 0F, 1, 14, 1, false);
        PoleRF.setRotationPoint(-6F, 9F, -6F);
        PoleRF.setTextureSize(128, 128);
        PoleRF.mirror = true;
        setRotation(PoleRF, 0F, 0F, 0F);
        Top = new ModelRenderer(this, 0, 0);
        Top.addBox(0F, 0F, 0F, 12, 1, 12, false);
        Top.setRotationPoint(-6F, 8F, -6F);
        Top.setTextureSize(128, 128);
        Top.mirror = true;
        setRotation(Top, 0F, 0F, 0F);
        FrontGlass = new ModelRenderer(this, 0, 13);
        FrontGlass.addBox(0F, 0F, 0F, 10, 14, 1, false);
        FrontGlass.setRotationPoint(-5F, 9F, -6F);
        FrontGlass.setTextureSize(128, 128);
        FrontGlass.mirror = true;
        setRotation(FrontGlass, 0F, 0F, 0F);
        BackGlass = new ModelRenderer(this, 0, 28);
        BackGlass.addBox(0F, 0F, 3F, 10, 14, 1, false);
        BackGlass.setRotationPoint(-5F, 9F, 2F);
        BackGlass.setTextureSize(128, 128);
        BackGlass.mirror = true;
        setRotation(BackGlass, 0F, 0F, 0F);
        RightGlass = new ModelRenderer(this, 22, 13);
        RightGlass.addBox(0F, 0F, 0F, 1, 14, 10, false);
        RightGlass.setRotationPoint(-6F, 9F, -5F);
        RightGlass.setTextureSize(128, 128);
        RightGlass.mirror = true;
        setRotation(RightGlass, 0F, 0F, 0F);
        LeftGlass = new ModelRenderer(this, 22, 37);
        LeftGlass.addBox(0F, 0F, 0F, 1, 14, 10, false);
        LeftGlass.setRotationPoint(5F, 9F, -5F);
        LeftGlass.setTextureSize(128, 128);
        LeftGlass.mirror = true;
        setRotation(LeftGlass, 0F, 0F, 0F);
    }

    public void render(@Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight, FluidTankTier tier, boolean hasEffect) {
        render(matrix, getVertexBuilder(renderer, RENDER_TYPE, hasEffect), light, overlayLight, 1, 1, 1, 1);
        EnumColor color = tier.getBaseTier().getColor();
        //TODO: Try to make it so the lines can still show up on the back walls of the tank in first person
        renderGlass(matrix, getVertexBuilder(renderer, GLASS_RENDER_TYPE, hasEffect), light, overlayLight, color.getColor(0), color.getColor(1), color.getColor(2), 1);
    }

    @Override
    public void render(@Nonnull MatrixStack matrix, @Nonnull IVertexBuilder vertexBuilder, int light, int overlayLight, float red, float green, float blue, float alpha) {
        Base.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        PoleFL.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        PoleLB.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        PoleBR.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        PoleRF.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        Top.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
    }

    private void renderGlass(@Nonnull MatrixStack matrix, @Nonnull IVertexBuilder vertexBuilder, int light, int overlayLight, float red, float green, float blue, float alpha) {
        FrontGlass.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        BackGlass.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        RightGlass.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
        LeftGlass.render(matrix, vertexBuilder, light, overlayLight, red, green, blue, alpha);
    }
}