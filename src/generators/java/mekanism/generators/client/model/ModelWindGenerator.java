package mekanism.generators.client.model;

import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.model.Model;

public class ModelWindGenerator extends Model {

    private final ModelRenderer head;
    private final ModelRenderer plateConnector2;
    private final ModelRenderer plateConnector;
    private final ModelRenderer plate;
    private final ModelRenderer bladeCap;
    private final ModelRenderer bladeCenter;
    private final ModelRenderer baseRim;
    private final ModelRenderer base;
    private final ModelRenderer wire;
    private final ModelRenderer rearPlate1;
    private final ModelRenderer rearPlate2;
    private final ModelRenderer blade1a;
    private final ModelRenderer blade2a;
    private final ModelRenderer blade3a;
    private final ModelRenderer blade1b;
    private final ModelRenderer blade2b;
    private final ModelRenderer blade3b;
    private final ModelRenderer post1a;
    private final ModelRenderer post1b;
    private final ModelRenderer post1c;
    private final ModelRenderer post1d;

    public ModelWindGenerator() {
        textureWidth = 128;
        textureHeight = 128;

        head = new ModelRenderer(this, 20, 0);
        head.func_228304_a_(-3.5F, -3.5F, 0F, 7, 7, 9, false);
        head.setRotationPoint(0F, -48F, -4F);
        head.setTextureSize(128, 128);
        head.mirror = true;
        setRotation(head, 0F, 0F, 0F);
        plateConnector2 = new ModelRenderer(this, 42, 34);
        plateConnector2.func_228304_a_(0F, 0F, 0F, 6, 6, 10, false);
        plateConnector2.setRotationPoint(-3F, 13F, -7F);
        plateConnector2.setTextureSize(128, 128);
        plateConnector2.mirror = true;
        setRotation(plateConnector2, 0F, 0F, 0F);
        plateConnector = new ModelRenderer(this, 0, 75);
        plateConnector.func_228304_a_(0F, 0F, 0F, 4, 2, 2, false);
        plateConnector.setRotationPoint(-2F, 19F, -5.5F);
        plateConnector.setTextureSize(128, 128);
        plateConnector.mirror = true;
        setRotation(plateConnector, 0F, 0F, 0F);
        plate = new ModelRenderer(this, 42, 25);
        plate.func_228304_a_(0F, 0F, 0F, 8, 8, 1, false);
        plate.setRotationPoint(-4F, 12F, -8F);
        plate.setTextureSize(128, 128);
        plate.mirror = true;
        setRotation(plate, 0F, 0F, 0F);
        bladeCap = new ModelRenderer(this, 22, 0);
        bladeCap.func_228304_a_(-1F, -1F, -8F, 2, 2, 1, false);
        bladeCap.setRotationPoint(0F, -48F, 0F);
        bladeCap.setTextureSize(128, 128);
        bladeCap.mirror = true;
        setRotation(bladeCap, 0F, 0F, 0F);
        bladeCenter = new ModelRenderer(this, 20, 25);
        bladeCenter.func_228304_a_(-2F, -2F, -7F, 4, 4, 3, false);
        bladeCenter.setRotationPoint(0F, -48F, 0F);
        bladeCenter.setTextureSize(128, 128);
        bladeCenter.mirror = true;
        setRotation(bladeCenter, 0F, 0F, 0F);
        baseRim = new ModelRenderer(this, 26, 50);
        baseRim.func_228304_a_(0F, 0F, 0F, 12, 2, 12, false);
        baseRim.setRotationPoint(-6F, 21F, -6F);
        baseRim.setTextureSize(128, 128);
        baseRim.mirror = true;
        setRotation(baseRim, 0F, 0F, 0F);
        base = new ModelRenderer(this, 10, 64);
        base.func_228304_a_(0F, 0F, 0F, 16, 2, 16, false);
        base.setRotationPoint(-8F, 22F, -8F);
        base.setTextureSize(128, 128);
        base.mirror = true;
        setRotation(base, 0F, 0F, 0F);
        wire = new ModelRenderer(this, 74, 0);
        wire.func_228304_a_(-1F, 0F, -1.1F, 2, 65, 2, false);
        wire.setRotationPoint(0F, -46F, -1.5F);
        wire.setTextureSize(128, 128);
        wire.mirror = true;
        setRotation(wire, -0.0349066F, 0F, 0F);
        rearPlate1 = new ModelRenderer(this, 20, 16);
        rearPlate1.func_228304_a_(-2.5F, -6F, 0F, 5, 6, 3, false);
        rearPlate1.setRotationPoint(0F, -44.5F, 4F);
        rearPlate1.setTextureSize(128, 128);
        rearPlate1.mirror = true;
        setRotation(rearPlate1, 0.122173F, 0F, 0F);
        rearPlate2 = new ModelRenderer(this, 36, 16);
        rearPlate2.func_228304_a_(-1.5F, -5F, -1F, 3, 5, 2, false);
        rearPlate2.setRotationPoint(0F, -45F, 7F);
        rearPlate2.setTextureSize(128, 128);
        rearPlate2.mirror = true;
        setRotation(rearPlate2, 0.2094395F, 0F, 0F);
        blade1a = new ModelRenderer(this, 20, 32);
        blade1a.func_228304_a_(-1F, -32F, 0F, 2, 32, 1, false);
        blade1a.setRotationPoint(0F, -48F, -5.99F);
        blade1a.setTextureSize(128, 128);
        blade1a.mirror = true;
        setRotation(blade1a, 0F, 0F, 0F);
        blade2a = new ModelRenderer(this, 20, 32);
        blade2a.func_228304_a_(-1F, 0F, 0F, 2, 32, 1, false);
        blade2a.setRotationPoint(0F, -48F, -6F);
        blade2a.setTextureSize(128, 128);
        blade2a.mirror = true;
        setRotation(blade2a, 0F, 0F, 1.047198F);
        blade3a = new ModelRenderer(this, 20, 32);
        blade3a.func_228304_a_(-1F, 0F, 0F, 2, 32, 1, false);
        blade3a.setRotationPoint(0F, -48F, -6F);
        blade3a.setTextureSize(128, 128);
        blade3a.mirror = true;
        setRotation(blade3a, 0F, 0F, -1.047198F);
        blade1b = new ModelRenderer(this, 26, 32);
        blade1b.func_228304_a_(-2F, -28F, 0F, 2, 28, 1, false);
        blade1b.setRotationPoint(0F, -48F, -6F);
        blade1b.setTextureSize(128, 128);
        blade1b.mirror = true;
        setRotation(blade1b, 0F, 0F, 0.0349066F);
        blade2b = new ModelRenderer(this, 26, 32);
        blade2b.func_228304_a_(0F, 0F, 0F, 2, 28, 1, false);
        blade2b.setRotationPoint(0F, -48F, -6.01F);
        blade2b.setTextureSize(128, 128);
        blade2b.mirror = true;
        setRotation(blade2b, 0F, 0F, 1.082104F);
        blade3b = new ModelRenderer(this, 26, 32);
        blade3b.func_228304_a_(0F, 0F, 0F, 2, 28, 1, false);
        blade3b.setRotationPoint(0F, -48F, -6.01F);
        blade3b.setTextureSize(128, 128);
        blade3b.mirror = true;
        setRotation(blade3b, 0F, 0F, -1.012291F);
        post1a = new ModelRenderer(this, 0, 0);
        post1a.func_228304_a_(-2.5F, 0F, -2.5F, 5, 68, 5, false);
        post1a.setRotationPoint(0F, -46F, 0F);
        post1a.setTextureSize(128, 128);
        post1a.mirror = true;
        setRotation(post1a, -0.0349066F, 0F, 0.0349066F);
        post1b = new ModelRenderer(this, 0, 0);
        post1b.func_228304_a_(-2.5F, 0F, -2.5F, 5, 68, 5, false);
        post1b.setRotationPoint(0F, -46F, 0F);
        post1b.setTextureSize(128, 128);
        post1b.mirror = true;
        setRotation(post1b, 0.0349066F, 0F, -0.0349066F);
        post1c = new ModelRenderer(this, 0, 0);
        post1c.func_228304_a_(-2.5F, 0F, -2.5F, 5, 68, 5, false);
        post1c.setRotationPoint(0F, -46F, 0F);
        post1c.setTextureSize(128, 128);
        post1c.mirror = true;
        setRotation(post1c, 0.0347321F, 0F, 0.0347321F);
        post1d = new ModelRenderer(this, 0, 0);
        post1d.func_228304_a_(-2.5F, 0F, -2.5F, 5, 68, 5, false);
        post1d.setRotationPoint(0F, -46F, 0F);
        post1d.setTextureSize(128, 128);
        post1d.mirror = true;
        setRotation(post1d, -0.0347321F, 0F, -0.0347321F);
    }

    public void render(float size, double angle) {
        head.render(size);
        plateConnector2.render(size);
        plateConnector.render(size);
        plate.render(size);
        baseRim.render(size);
        base.render(size);
        wire.render(size);
        rearPlate1.render(size);
        rearPlate2.render(size);
        post1a.render(size);
        post1b.render(size);
        post1c.render(size);
        post1d.render(size);

        setRotation(blade1a, 0F, 0F, getRotation(getAbsoluteAngle(angle)));
        setRotation(blade1b, 0F, 0F, 0.0349066F + getRotation(getAbsoluteAngle(angle)));

        setRotation(blade2a, 0F, 0F, getRotation(getAbsoluteAngle(angle - 60)));
        setRotation(blade2b, 0F, 0F, 0.0349066F + getRotation(getAbsoluteAngle(angle - 60)));

        setRotation(blade3a, 0F, 0F, getRotation(getAbsoluteAngle(angle + 60)));
        setRotation(blade3b, 0F, 0F, 0.0349066F + getRotation(getAbsoluteAngle(angle + 60)));

        setRotation(bladeCap, 0F, 0F, getRotation(getAbsoluteAngle(angle)));
        setRotation(bladeCenter, 0F, 0F, getRotation(getAbsoluteAngle(angle)));

        blade1a.render(size);
        blade2a.render(size);
        blade3a.render(size);
        blade1b.render(size);
        blade2b.render(size);
        blade3b.render(size);

        bladeCap.render(size);
        bladeCenter.render(size);
    }

    public float getRotation(double angle) {
        return (float) Math.toRadians(angle);
    }

    public double getAbsoluteAngle(double angle) {
        return angle % 360;
    }

    private void setRotation(ModelRenderer model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}