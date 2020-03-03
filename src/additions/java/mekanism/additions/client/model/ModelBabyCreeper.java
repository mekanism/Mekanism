package mekanism.additions.client.model;

import com.google.common.collect.ImmutableList;
import javax.annotation.Nonnull;
import mekanism.additions.common.entity.baby.EntityBabyCreeper;
import net.minecraft.client.renderer.entity.model.AgeableModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;

public class ModelBabyCreeper extends AgeableModel<EntityBabyCreeper> {

    private final ModelRenderer head;
    private final ModelRenderer body;
    private final ModelRenderer leg1;
    private final ModelRenderer leg2;
    private final ModelRenderer leg3;
    private final ModelRenderer leg4;

    public ModelBabyCreeper() {
        this(0);
    }

    public ModelBabyCreeper(float size) {
        this.head = new ModelRenderer(this, 0, 0);
        this.head.addBox(-4, -8, -4, 8, 8, 8, size);
        //Only real difference between this model and the vanilla creeper model is the "fix" for the head's rotation point
        // the other difference is extending ageable model instead
        this.head.setRotationPoint(0, 10, -2);
        this.body = new ModelRenderer(this, 16, 16);
        this.body.addBox(-4, 0, -2, 8, 12, 4, size);
        this.body.setRotationPoint(0, 6, 0);
        this.leg1 = new ModelRenderer(this, 0, 16);
        this.leg1.addBox(-2, 0, -2, 4, 6, 4, size);
        this.leg1.setRotationPoint(-2, 18, 4);
        this.leg2 = new ModelRenderer(this, 0, 16);
        this.leg2.addBox(-2, 0, -2, 4, 6, 4, size);
        this.leg2.setRotationPoint(2, 18, 4);
        this.leg3 = new ModelRenderer(this, 0, 16);
        this.leg3.addBox(-2, 0, -2, 4, 6, 4, size);
        this.leg3.setRotationPoint(-2, 18, -4);
        this.leg4 = new ModelRenderer(this, 0, 16);
        this.leg4.addBox(-2, 0, -2, 4, 6, 4, size);
        this.leg4.setRotationPoint(2, 18, -4);
    }

    @Nonnull
    @Override
    protected Iterable<ModelRenderer> getHeadParts() {
        return ImmutableList.of(this.head);
    }

    @Nonnull
    @Override
    protected Iterable<ModelRenderer> getBodyParts() {
        return ImmutableList.of(this.body, this.leg1, this.leg2, this.leg3, this.leg4);
    }

    @Override
    public void setRotationAngles(@Nonnull EntityBabyCreeper creeper, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.rotateAngleY = netHeadYaw * ((float) Math.PI / 180F);
        this.head.rotateAngleX = headPitch * ((float) Math.PI / 180F);
        this.leg1.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.leg2.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
        this.leg3.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
        this.leg4.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
    }
}