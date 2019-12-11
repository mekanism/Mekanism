package mekanism.client.model;

import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.Model;

public class ModelScubaTank extends Model {

    private final RendererModel tankL;
    private final RendererModel tankR;
    private final RendererModel tankdock;
    private final RendererModel capL;
    private final RendererModel capR;
    private final RendererModel tankbridge;
    private final RendererModel tankpipelower;
    private final RendererModel tankpipeupper;
    private final RendererModel tankbackbrace;

    public ModelScubaTank() {
        textureWidth = 128;
        textureHeight = 64;

        tankL = new RendererModel(this, 23, 54);
        tankL.addBox(-1F, 2F, 4F, 3, 7, 3);
        tankL.setRotationPoint(0F, 0F, 0F);
        tankL.setTextureSize(128, 64);
        tankL.mirror = true;
        setRotation(tankL, -0.2443461F, 0.5235988F, 0F);
        tankR = new RendererModel(this, 23, 54);
        tankR.addBox(-2F, 2F, 4F, 3, 7, 3);
        tankR.setRotationPoint(0F, 0F, 0F);
        tankR.setTextureSize(128, 64);
        tankR.mirror = true;
        setRotation(tankR, -0.2443461F, -0.5235988F, 0F);
        tankR.mirror = false;
        tankdock = new RendererModel(this, 0, 55);
        tankdock.addBox(-2F, 5F, 1F, 4, 4, 5);
        tankdock.setRotationPoint(0F, 0F, 0F);
        tankdock.setTextureSize(128, 64);
        tankdock.mirror = true;
        setRotation(tankdock, 0F, 0F, 0F);
        capL = new RendererModel(this, 23, 51);
        capL.addBox(-0.5F, 1F, 4.5F, 2, 1, 2);
        capL.setRotationPoint(0F, 0F, 0F);
        capL.setTextureSize(128, 64);
        capL.mirror = true;
        setRotation(capL, -0.2443461F, 0.5235988F, 0F);
        capR = new RendererModel(this, 23, 51);
        capR.addBox(-1.5F, 1F, 4.5F, 2, 1, 2);
        capR.setRotationPoint(0F, 0F, 0F);
        capR.setTextureSize(128, 64);
        capR.mirror = true;
        setRotation(capR, -0.2443461F, -0.5235988F, 0F);
        tankbridge = new RendererModel(this, 0, 47);
        tankbridge.addBox(-1F, 3F, -1.5F, 2, 5, 3);
        tankbridge.setRotationPoint(0F, 0F, 0F);
        tankbridge.setTextureSize(128, 64);
        tankbridge.mirror = true;
        setRotation(tankbridge, 0.5934119F, 0F, 0F);
        tankpipelower = new RendererModel(this, 0, 37);
        tankpipelower.addBox(-0.5F, 2F, 3F, 1, 4, 1);
        tankpipelower.setRotationPoint(0F, 0F, 0F);
        tankpipelower.setTextureSize(128, 64);
        tankpipelower.mirror = true;
        setRotation(tankpipelower, 0.2094395F, 0F, 0F);
        tankpipeupper = new RendererModel(this, 4, 38);
        tankpipeupper.addBox(-0.5F, 1F, 1.5F, 1, 1, 3);
        tankpipeupper.setRotationPoint(0F, 0F, 0F);
        tankpipeupper.setTextureSize(128, 64);
        tankpipeupper.mirror = true;
        setRotation(tankpipeupper, 0F, 0F, 0F);
        tankbackbrace = new RendererModel(this, 0, 42);
        tankbackbrace.addBox(-3F, 2F, 0.5F, 6, 3, 2);
        tankbackbrace.setRotationPoint(0F, 0F, 0F);
        tankbackbrace.setTextureSize(128, 64);
        tankbackbrace.mirror = true;
        setRotation(tankbackbrace, 0.2443461F, 0F, 0F);
    }

    public void render(float size) {
        tankL.render(size);
        tankR.render(size);
        tankdock.render(size);
        capL.render(size);
        capR.render(size);
        tankbridge.render(size);
        tankpipelower.render(size);
        tankpipeupper.render(size);
        tankbackbrace.render(size);
    }

    private void setRotation(RendererModel model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}