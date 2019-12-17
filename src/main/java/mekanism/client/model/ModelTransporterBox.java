package mekanism.client.model;

import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.model.Model;

public class ModelTransporterBox extends Model {

    private final ModelRenderer box;

    public ModelTransporterBox() {
        textureWidth = 64;
        textureHeight = 64;

        box = new ModelRenderer(this, 0, 0);
        box.func_228304_a_(0F, 0F, 0F, 7, 7, 7, false);
        box.setRotationPoint(-3.5F, 0, -3.5F);
        box.setTextureSize(64, 64);
        box.mirror = true;
        setRotation(box, 0F, 0F, 0F);
    }

    public void render(float size) {
        box.render(size);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}