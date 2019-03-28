package mekanism.client.model;

import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelChargepad extends ModelBase {

    public static ResourceLocation OVERLAY = MekanismUtils.getResource(ResourceType.RENDER, "Chargepad_Overlay.png");

    ModelRenderer base;
    ModelRenderer port;
    ModelRenderer plug;
    ModelRenderer connector;
    ModelRenderer stand;
    ModelRenderer pillar2;
    ModelRenderer pillar1;

    public ModelChargepad() {
        textureWidth = 64;
        textureHeight = 64;

        base = new ModelRenderer(this, 0, 0);
        base.addBox(0F, 0F, 0F, 16, 1, 16);
        base.setRotationPoint(-8F, 23F, -8F);
        base.setTextureSize(64, 64);
        base.mirror = true;
        setRotation(base, 0F, 0F, 0F);
        port = new ModelRenderer(this, 0, 17);
        port.addBox(0F, 0F, 0F, 8, 8, 1);
        port.setRotationPoint(-4F, 12F, 7F);
        port.setTextureSize(64, 64);
        port.mirror = true;
        setRotation(port, 0F, 0F, 0F);
        plug = new ModelRenderer(this, 0, 11);
        plug.addBox(0F, 0F, 0F, 2, 1, 2);
        plug.setRotationPoint(-1F, 19F, 3F);
        plug.setTextureSize(64, 64);
        plug.mirror = true;
        setRotation(plug, 0F, 0F, 0F);
        connector = new ModelRenderer(this, 18, 17);
        connector.addBox(0F, 0F, 0F, 6, 6, 1);
        connector.setRotationPoint(-3F, 13F, 6F);
        connector.setTextureSize(64, 64);
        connector.mirror = true;
        setRotation(connector, 0F, 0F, 0F);
        stand = new ModelRenderer(this, 0, 0);
        stand.addBox(0F, 0F, 0F, 6, 10, 1);
        stand.setRotationPoint(-3F, 13F, 5F);
        stand.setTextureSize(64, 64);
        stand.mirror = true;
        setRotation(stand, 0F, 0F, 0F);
        pillar2 = new ModelRenderer(this, 48, 0);
        pillar2.mirror = true;
        pillar2.addBox(0F, 0F, 0F, 2, 7, 2);
        pillar2.setRotationPoint(2F, 16F, 3.99F);
        pillar2.setTextureSize(64, 64);
        setRotation(pillar2, 0F, 0F, 0F);
        pillar1 = new ModelRenderer(this, 48, 0);
        pillar1.addBox(0F, 0F, 0F, 2, 7, 2);
        pillar1.setRotationPoint(-4F, 16F, 3.99F);
        pillar1.setTextureSize(64, 64);
        pillar1.mirror = true;
        setRotation(pillar1, 0F, 0F, 0F);
    }

    public void render(float size, TextureManager manager) {
        GlStateManager.pushMatrix();
        MekanismRenderer.blendOn();

        manager.bindTexture(OVERLAY);
        GlStateManager.scale(1.001F, 1.001F, 1.001F);
        GlStateManager.translate(0, -0.0011F, 0);
        MekanismRenderer.glowOn();

        doRender(size);

        MekanismRenderer.glowOff();
        MekanismRenderer.blendOff();
        GlStateManager.popMatrix();
    }

    private void doRender(float size) {
        base.render(size);
        port.render(size);
        plug.render(size);
        connector.render(size);
        stand.render(size);
        pillar2.render(size);
        pillar1.render(size);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}
