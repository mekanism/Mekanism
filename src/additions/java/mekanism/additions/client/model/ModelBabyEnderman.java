package mekanism.additions.client.model;

import javax.annotation.Nonnull;
import mekanism.additions.common.entity.baby.EntityBabyEnderman;
import net.minecraft.client.renderer.entity.model.EndermanModel;

public class ModelBabyEnderman extends EndermanModel<EntityBabyEnderman> {

    public ModelBabyEnderman() {
        super(0);
    }

    @Override
    public void setupAnim(@Nonnull EntityBabyEnderman enderman, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(enderman, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        //Shift the head to be in the proper place for baby endermen
        head.y += 5.0F;
        if (creepy) {
            //Shift the head when angry to only the third the distance it goes up when it is an adult
            head.y += 1.67F;
        }
    }
}