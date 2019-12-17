package mekanism.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import javax.annotation.Nonnull;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;

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

    @Override
    public void func_225598_a_(@Nonnull MatrixStack matrix, @Nonnull IVertexBuilder vertexBuilder, int light, int otherLight, float red, float green, float blue, float alpha) {
        box.func_228309_a_(matrix, vertexBuilder, light, otherLight, red, green, blue, alpha);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}