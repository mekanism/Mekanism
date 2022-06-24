package mekanism.additions.client.model;

import java.util.List;
import mekanism.additions.common.entity.baby.EntityBabyEnderman;
import net.minecraft.client.model.EndermanModel;
import net.minecraft.client.model.geom.ModelPart;
import org.jetbrains.annotations.NotNull;

public class ModelBabyEnderman extends EndermanModel<EntityBabyEnderman> {

    public ModelBabyEnderman(ModelPart part) {
        super(part);
    }

    @NotNull
    @Override
    protected Iterable<ModelPart> headParts() {
        //Make the "hat" (the jaw) be part of the head for scaling purposes
        return List.of(this.head, this.hat);
    }

    @NotNull
    @Override
    protected Iterable<ModelPart> bodyParts() {
        return List.of(this.body, this.rightArm, this.leftArm, this.rightLeg, this.leftLeg);
    }

    @Override
    public void setupAnim(@NotNull EntityBabyEnderman enderman, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(enderman, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        //Shift the head and the "hat" (jaw) be in the proper place for baby endermen
        head.y += 5.0F;
        hat.y += 5.0F;
        if (creepy) {
            //Shift the head when angry to only the third the distance it goes up when it is an adult
            head.y += 1.67F;
        }
    }
}