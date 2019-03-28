package mekanism.generators.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelAdvancedSolarGenerator extends ModelBase {

    ModelRenderer Shape1;
    ModelRenderer Shape2;
    ModelRenderer Shape3;
    ModelRenderer Shape4;
    ModelRenderer Shape5;
    ModelRenderer Shape6;
    ModelRenderer Shape7;
    ModelRenderer Shape8;
    ModelRenderer Shape9;
    ModelRenderer Shape10;
    ModelRenderer Shape11;
    ModelRenderer Shape12;
    ModelRenderer Shape13;
    ModelRenderer Shape14;
    ModelRenderer Shape15;
    ModelRenderer Shape16;

    public ModelAdvancedSolarGenerator() {
        textureWidth = 256;
        textureHeight = 256;

        Shape1 = new ModelRenderer(this, 0, 95);
        Shape1.addBox(0F, -1F, -1F, 40, 2, 2);
        Shape1.setRotationPoint(-20F, -17F, 0F);
        Shape1.setTextureSize(256, 256);
        Shape1.mirror = true;
        setRotation(Shape1, 0F, 0F, 0F); //rotates
        Shape2 = new ModelRenderer(this, 0, 49);
        Shape2.addBox(0F, -1F, -23F, 16, 1, 45);
        Shape2.setRotationPoint(7F, -17F, 0F);
        Shape2.setTextureSize(256, 256);
        Shape2.mirror = true;
        setRotation(Shape2, 0F, 0F, 0F); //rotates
        Shape3 = new ModelRenderer(this, 0, 0);
        Shape3.addBox(0F, -2F, -24F, 18, 1, 48);
        Shape3.setRotationPoint(6F, -17F, 0F);
        Shape3.setTextureSize(256, 256);
        Shape3.mirror = true;
        setRotation(Shape3, 0F, 0F, 0F); //rotates
        Shape4 = new ModelRenderer(this, 86, 21);
        Shape4.addBox(0F, 0F, 0F, 6, 6, 10);
        Shape4.setRotationPoint(-3F, 13F, -7F);
        Shape4.setTextureSize(256, 256);
        Shape4.mirror = true;
        setRotation(Shape4, 0F, 0F, 0F);
        Shape5 = new ModelRenderer(this, 0, 0);
        Shape5.addBox(0F, 0F, 0F, 4, 40, 4);
        Shape5.setRotationPoint(-2F, -16F, -2F);
        Shape5.setTextureSize(256, 256);
        Shape5.mirror = true;
        setRotation(Shape5, 0F, 0F, 0F);
        Shape6 = new ModelRenderer(this, 16, 28);
        Shape6.addBox(0F, 0F, 0F, 2, 2, 12);
        Shape6.setRotationPoint(1F, -14F, -6F);
        Shape6.setTextureSize(256, 256);
        Shape6.mirror = true;
        setRotation(Shape6, 0F, 0F, 0F);
        Shape7 = new ModelRenderer(this, 0, 50);
        Shape7.addBox(0F, 0F, 0F, 1, 7, 7);
        Shape7.setRotationPoint(1.5F, -20.5F, -3.5F);
        Shape7.setTextureSize(256, 256);
        Shape7.mirror = true;
        setRotation(Shape7, 0F, 0F, 0F);
        Shape8 = new ModelRenderer(this, 16, 28);
        Shape8.addBox(0F, 0F, 0F, 2, 2, 12);
        Shape8.setRotationPoint(-3F, -14F, -6F);
        Shape8.setTextureSize(256, 256);
        Shape8.mirror = true;
        setRotation(Shape8, 0F, 0F, 0F);
        Shape9 = new ModelRenderer(this, 16, 0);
        Shape9.addBox(0F, 0F, 0F, 8, 6, 6);
        Shape9.setRotationPoint(-4F, -20F, -3F);
        Shape9.setTextureSize(256, 256);
        Shape9.mirror = true;
        setRotation(Shape9, 0F, 0F, 0F);
        Shape10 = new ModelRenderer(this, 0, 50);
        Shape10.addBox(0F, 0F, 0F, 1, 7, 7);
        Shape10.setRotationPoint(-2.5F, -20.5F, -3.5F);
        Shape10.setTextureSize(256, 256);
        Shape10.mirror = true;
        setRotation(Shape10, 0F, 0F, 0F);
        Shape11 = new ModelRenderer(this, 0, 0);
        Shape11.addBox(0F, -2F, -24F, 18, 1, 48);
        Shape11.setRotationPoint(-24F, -17F, 0F);
        Shape11.setTextureSize(256, 256);
        Shape11.mirror = true;
        setRotation(Shape11, 0F, 0F, 0F); //rotates
        Shape12 = new ModelRenderer(this, 0, 49);
        Shape12.addBox(0F, -1F, -23F, 16, 1, 45);
        Shape12.setRotationPoint(-23F, -17F, 0F);
        Shape12.setTextureSize(256, 256);
        Shape12.mirror = true;
        setRotation(Shape12, 0F, 0F, 0F); //rotates
        Shape13 = new ModelRenderer(this, 78, 50);
        Shape13.addBox(0F, 0F, 0F, 16, 2, 16);
        Shape13.setRotationPoint(-8F, 22F, -8F);
        Shape13.setTextureSize(256, 256);
        Shape13.mirror = true;
        setRotation(Shape13, 0F, 0F, 0F);
        Shape14 = new ModelRenderer(this, 86, 12);
        Shape14.addBox(0F, 0F, 0F, 8, 8, 1);
        Shape14.setRotationPoint(-4F, 12F, -8F);
        Shape14.setTextureSize(256, 256);
        Shape14.mirror = true;
        setRotation(Shape14, 0F, 0F, 0F);
        Shape15 = new ModelRenderer(this, 16, 12);
        Shape15.addBox(0F, 0F, 0F, 8, 8, 8);
        Shape15.setRotationPoint(-4F, 14F, -4F);
        Shape15.setTextureSize(256, 256);
        Shape15.mirror = true;
        setRotation(Shape15, 0F, 0F, 0F);
        Shape16 = new ModelRenderer(this, 86, 0);
        Shape16.addBox(0F, 0F, 0F, 10, 2, 10);
        Shape16.setRotationPoint(-5F, 21F, -5F);
        Shape16.setTextureSize(256, 256);
        Shape16.mirror = true;
        setRotation(Shape16, 0F, 0F, 0F);
    }

    public void render(float size) {
        Shape1.render(size);
        Shape2.render(size);
        Shape3.render(size);
        Shape4.render(size);
        Shape5.render(size);
        Shape6.render(size);
        Shape7.render(size);
        Shape8.render(size);
        Shape9.render(size);
        Shape10.render(size);
        Shape11.render(size);
        Shape12.render(size);
        Shape13.render(size);
        Shape14.render(size);
        Shape15.render(size);
        Shape16.render(size);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}
