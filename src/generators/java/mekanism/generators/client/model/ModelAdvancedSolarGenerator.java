package mekanism.generators.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import javax.annotation.Nonnull;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;

public class ModelAdvancedSolarGenerator extends Model {

    private static final ResourceLocation GENERATOR_TEXTURE = MekanismUtils.getResource(ResourceType.RENDER, "advanced_solar_generator.png");
    private final RenderType RENDER_TYPE = func_228282_a_(GENERATOR_TEXTURE);

    private final ModelRenderer crossBar;
    private final ModelRenderer panel1Bottom;
    private final ModelRenderer panel1Top;
    private final ModelRenderer portBase;
    private final ModelRenderer verticalBar;
    private final ModelRenderer sideBar1;
    private final ModelRenderer wire1;
    private final ModelRenderer sideBar2;
    private final ModelRenderer jointBox;
    private final ModelRenderer wire2;
    private final ModelRenderer panel2Top;
    private final ModelRenderer panel2Bottom;
    private final ModelRenderer base1;
    private final ModelRenderer port;
    private final ModelRenderer base3;
    private final ModelRenderer base2;

    //TODO: Do we want to implement what I think was an original idea based on the rotates comments
    // where the panels rotate to face towards the sun?
    public ModelAdvancedSolarGenerator() {
        super(RenderType::func_228634_a_);
        textureWidth = 256;
        textureHeight = 256;

        crossBar = new ModelRenderer(this, 0, 95);
        crossBar.func_228304_a_(0F, -1F, -1F, 40, 2, 2, false);
        crossBar.setRotationPoint(-20F, -17F, 0F);
        crossBar.setTextureSize(256, 256);
        crossBar.mirror = true;
        setRotation(crossBar, 0F, 0F, 0F); //rotates
        panel1Bottom = new ModelRenderer(this, 0, 49);
        panel1Bottom.func_228304_a_(0F, -1F, -23F, 16, 1, 45, false);
        panel1Bottom.setRotationPoint(7F, -17F, 0F);
        panel1Bottom.setTextureSize(256, 256);
        panel1Bottom.mirror = true;
        setRotation(panel1Bottom, 0F, 0F, 0F); //rotates
        panel1Top = new ModelRenderer(this, 0, 0);
        panel1Top.func_228304_a_(0F, -2F, -24F, 18, 1, 48, false);
        panel1Top.setRotationPoint(6F, -17F, 0F);
        panel1Top.setTextureSize(256, 256);
        panel1Top.mirror = true;
        setRotation(panel1Top, 0F, 0F, 0F); //rotates
        portBase = new ModelRenderer(this, 86, 21);
        portBase.func_228304_a_(0F, 0F, 0F, 6, 6, 10, false);
        portBase.setRotationPoint(-3F, 13F, -7F);
        portBase.setTextureSize(256, 256);
        portBase.mirror = true;
        setRotation(portBase, 0F, 0F, 0F);
        verticalBar = new ModelRenderer(this, 0, 0);
        verticalBar.func_228304_a_(0F, 0F, 0F, 4, 40, 4, false);
        verticalBar.setRotationPoint(-2F, -16F, -2F);
        verticalBar.setTextureSize(256, 256);
        verticalBar.mirror = true;
        setRotation(verticalBar, 0F, 0F, 0F);
        sideBar1 = new ModelRenderer(this, 16, 28);
        sideBar1.func_228304_a_(0F, 0F, 0F, 2, 2, 12, false);
        sideBar1.setRotationPoint(1F, -14F, -6F);
        sideBar1.setTextureSize(256, 256);
        sideBar1.mirror = true;
        setRotation(sideBar1, 0F, 0F, 0F);
        wire1 = new ModelRenderer(this, 0, 50);
        wire1.func_228304_a_(0F, 0F, 0F, 1, 7, 7, false);
        wire1.setRotationPoint(1.5F, -20.5F, -3.5F);
        wire1.setTextureSize(256, 256);
        wire1.mirror = true;
        setRotation(wire1, 0F, 0F, 0F);
        sideBar2 = new ModelRenderer(this, 16, 28);
        sideBar2.func_228304_a_(0F, 0F, 0F, 2, 2, 12, false);
        sideBar2.setRotationPoint(-3F, -14F, -6F);
        sideBar2.setTextureSize(256, 256);
        sideBar2.mirror = true;
        setRotation(sideBar2, 0F, 0F, 0F);
        jointBox = new ModelRenderer(this, 16, 0);
        jointBox.func_228304_a_(0F, 0F, 0F, 8, 6, 6, false);
        jointBox.setRotationPoint(-4F, -20F, -3F);
        jointBox.setTextureSize(256, 256);
        jointBox.mirror = true;
        setRotation(jointBox, 0F, 0F, 0F);
        wire2 = new ModelRenderer(this, 0, 50);
        wire2.func_228304_a_(0F, 0F, 0F, 1, 7, 7, false);
        wire2.setRotationPoint(-2.5F, -20.5F, -3.5F);
        wire2.setTextureSize(256, 256);
        wire2.mirror = true;
        setRotation(wire2, 0F, 0F, 0F);
        panel2Top = new ModelRenderer(this, 0, 0);
        panel2Top.func_228304_a_(0F, -2F, -24F, 18, 1, 48, false);
        panel2Top.setRotationPoint(-24F, -17F, 0F);
        panel2Top.setTextureSize(256, 256);
        panel2Top.mirror = true;
        setRotation(panel2Top, 0F, 0F, 0F); //rotates
        panel2Bottom = new ModelRenderer(this, 0, 49);
        panel2Bottom.func_228304_a_(0F, -1F, -23F, 16, 1, 45, false);
        panel2Bottom.setRotationPoint(-23F, -17F, 0F);
        panel2Bottom.setTextureSize(256, 256);
        panel2Bottom.mirror = true;
        setRotation(panel2Bottom, 0F, 0F, 0F); //rotates
        base1 = new ModelRenderer(this, 78, 50);
        base1.func_228304_a_(0F, 0F, 0F, 16, 2, 16, false);
        base1.setRotationPoint(-8F, 22F, -8F);
        base1.setTextureSize(256, 256);
        base1.mirror = true;
        setRotation(base1, 0F, 0F, 0F);
        port = new ModelRenderer(this, 86, 12);
        port.func_228304_a_(0F, 0F, 0F, 8, 8, 1, false);
        port.setRotationPoint(-4F, 12F, -8F);
        port.setTextureSize(256, 256);
        port.mirror = true;
        setRotation(port, 0F, 0F, 0F);
        base3 = new ModelRenderer(this, 16, 12);
        base3.func_228304_a_(0F, 0F, 0F, 8, 8, 8, false);
        base3.setRotationPoint(-4F, 14F, -4F);
        base3.setTextureSize(256, 256);
        base3.mirror = true;
        setRotation(base3, 0F, 0F, 0F);
        base2 = new ModelRenderer(this, 86, 0);
        base2.func_228304_a_(0F, 0F, 0F, 10, 2, 10, false);
        base2.setRotationPoint(-5F, 21F, -5F);
        base2.setTextureSize(256, 256);
        base2.mirror = true;
        setRotation(base2, 0F, 0F, 0F);
    }

    public void render(@Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int otherLight) {
        matrix.func_227860_a_();
        matrix.func_227863_a_(Vector3f.field_229183_f_.func_229187_a_(180));
        func_225598_a_(matrix, renderer.getBuffer(RENDER_TYPE), light, otherLight, 1, 1, 1, 1);
        matrix.func_227865_b_();
    }

    @Override
    public void func_225598_a_(@Nonnull MatrixStack matrix, @Nonnull IVertexBuilder vertexBuilder, int light, int otherLight, float red, float green, float blue, float alpha) {
        crossBar.func_228309_a_(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);
        panel1Bottom.func_228309_a_(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);
        panel1Top.func_228309_a_(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);
        portBase.func_228309_a_(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);
        verticalBar.func_228309_a_(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);
        sideBar1.func_228309_a_(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);
        wire1.func_228309_a_(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);
        sideBar2.func_228309_a_(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);
        jointBox.func_228309_a_(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);
        wire2.func_228309_a_(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);
        panel2Top.func_228309_a_(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);
        panel2Bottom.func_228309_a_(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);
        base1.func_228309_a_(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);
        port.func_228309_a_(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);
        base3.func_228309_a_(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);
        base2.func_228309_a_(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}