package mekanism.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelElectrolyticSeparator extends ModelBase {

    ModelRenderer tank2;
    ModelRenderer tank1;
    ModelRenderer tank3;
    ModelRenderer tube6;
    ModelRenderer tube5;
    ModelRenderer tube4;
    ModelRenderer tube3;
    ModelRenderer tube2;
    ModelRenderer tube1;
    ModelRenderer base;
    ModelRenderer portToggle1;
    ModelRenderer portToggle2a;
    ModelRenderer portToggle2b;
    ModelRenderer portToggle3a;
    ModelRenderer portToggle3b;
    ModelRenderer portToggle4a;
    ModelRenderer portToggle4b;

    public ModelElectrolyticSeparator() {
        textureWidth = 128;
        textureHeight = 64;

        tank2 = new ModelRenderer(this, 64, 0);
        tank2.addBox(0F, 0F, 0F, 7, 10, 7);
        tank2.setRotationPoint(1F, 10F, 1F);
        tank2.setTextureSize(128, 64);
        tank2.mirror = true;
        setRotation(tank2, 0F, 0F, 0F);
        tank1 = new ModelRenderer(this, 0, 20);
        tank1.addBox(0F, 0F, 0F, 8, 12, 16);
        tank1.setRotationPoint(-8F, 8F, -8F);
        tank1.setTextureSize(128, 64);
        tank1.mirror = true;
        setRotation(tank1, 0F, 0F, 0F);
        tank3 = new ModelRenderer(this, 64, 0);
        tank3.addBox(0F, 0F, 0F, 7, 10, 7);
        tank3.setRotationPoint(1F, 10F, -8F);
        tank3.setTextureSize(128, 64);
        tank3.mirror = true;
        setRotation(tank3, 0F, 0F, 0F);
        tube6 = new ModelRenderer(this, 0, 0);
        tube6.addBox(0F, 0F, 0F, 1, 1, 1);
        tube6.setRotationPoint(4F, 9F, 4F);
        tube6.setTextureSize(128, 64);
        tube6.mirror = true;
        setRotation(tube6, 0F, 0F, 0F);
        tube5 = new ModelRenderer(this, 0, 0);
        tube5.addBox(0F, 0F, 0F, 1, 1, 1);
        tube5.setRotationPoint(4F, 9F, -5F);
        tube5.setTextureSize(128, 64);
        tube5.mirror = true;
        setRotation(tube5, 0F, 0F, 0F);
        tube4 = new ModelRenderer(this, 0, 48);
        tube4.addBox(0F, 0F, 0F, 1, 1, 10);
        tube4.setRotationPoint(4F, 8F, -5F);
        tube4.setTextureSize(128, 64);
        tube4.mirror = true;
        setRotation(tube4, 0F, 0F, 0F);
        tube3 = new ModelRenderer(this, 0, 0);
        tube3.addBox(0F, 0F, 0F, 1, 3, 1);
        tube3.setRotationPoint(4F, 9F, -0.5F);
        tube3.setTextureSize(128, 64);
        tube3.mirror = true;
        setRotation(tube3, 0F, 0F, 0F);
        tube2 = new ModelRenderer(this, 0, 0);
        tube2.addBox(-4F, -1F, 0F, 4, 1, 1);
        tube2.setRotationPoint(5F, 12F, -0.5F);
        tube2.setTextureSize(128, 64);
        tube2.mirror = true;
        setRotation(tube2, 0F, 0F, -0.5235988F);
        tube1 = new ModelRenderer(this, 0, 0);
        tube1.addBox(-3F, -1F, 0F, 3, 1, 1);
        tube1.setRotationPoint(1.5F, 14F, -0.5F);
        tube1.setTextureSize(128, 64);
        tube1.mirror = true;
        setRotation(tube1, 0F, 0F, 0F);
        base = new ModelRenderer(this, 0, 0);
        base.addBox(0F, 0F, 0F, 16, 4, 16);
        base.setRotationPoint(-8F, 20F, -8F);
        base.setTextureSize(128, 64);
        base.mirror = true;
        setRotation(base, 0F, 0F, 0F);
        portToggle1 = new ModelRenderer(this, 48, 36);
        portToggle1.addBox(0F, 0F, 0F, 1, 10, 10);
        portToggle1.setRotationPoint(-8.01F, 11F, -5F);
        portToggle1.setTextureSize(128, 64);
        portToggle1.mirror = true;
        setRotation(portToggle1, 0F, 0F, 0F);
        portToggle2a = new ModelRenderer(this, 48, 20);
        portToggle2a.addBox(0F, 0F, 0F, 1, 8, 8);
        portToggle2a.setRotationPoint(7.01F, 12F, -4F);
        portToggle2a.setTextureSize(128, 64);
        portToggle2a.mirror = true;
        setRotation(portToggle2a, 0F, 0F, 0F);
        portToggle2b = new ModelRenderer(this, 0, 4);
        portToggle2b.addBox(0F, 0F, 0F, 2, 7, 2);
        portToggle2b.setRotationPoint(5F, 13F, -1F);
        portToggle2b.setTextureSize(128, 64);
        portToggle2b.mirror = true;
        setRotation(portToggle2b, 0F, 0F, 0F);
        portToggle3a = new ModelRenderer(this, 66, 20);
        portToggle3a.addBox(0F, 0F, 0F, 8, 8, 1);
        portToggle3a.setRotationPoint(-4F, 12F, -8.01F);
        portToggle3a.setTextureSize(128, 64);
        portToggle3a.mirror = true;
        setRotation(portToggle3a, 0F, 0F, 0F);
        portToggle3b = new ModelRenderer(this, 0, 4);
        portToggle3b.addBox(0F, 0F, 0F, 1, 7, 2);
        portToggle3b.setRotationPoint(0F, 13F, -7F);
        portToggle3b.setTextureSize(128, 64);
        portToggle3b.mirror = true;
        setRotation(portToggle3b, 0F, 0F, 0F);
        portToggle4a = new ModelRenderer(this, 66, 20);
        portToggle4a.addBox(0F, 0F, 0F, 8, 8, 1);
        portToggle4a.setRotationPoint(-4F, 12F, 7.01F);
        portToggle4a.setTextureSize(128, 64);
        portToggle4a.mirror = true;
        setRotation(portToggle4a, 0F, 0F, 0F);
        portToggle4b = new ModelRenderer(this, 0, 4);
        portToggle4b.addBox(0F, 0F, 0F, 1, 7, 2);
        portToggle4b.setRotationPoint(0F, 13F, 5F);
        portToggle4b.setTextureSize(128, 64);
        portToggle4b.mirror = true;
        setRotation(portToggle4b, 0F, 0F, 0F);
    }

    public void render(float size) {
        tank2.render(size);
        tank1.render(size);
        tank3.render(size);
        tube6.render(size);
        tube5.render(size);
        tube4.render(size);
        tube3.render(size);
        tube2.render(size);
        tube1.render(size);
        base.render(size);
        portToggle1.render(size);
        portToggle2a.render(size);
        portToggle2b.render(size);
        portToggle3a.render(size);
        portToggle3b.render(size);
        portToggle4a.render(size);
        portToggle4b.render(size);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}
