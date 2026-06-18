package mekanism.additions.client.model;

import java.util.Set;
import net.minecraft.client.model.BabyModelTransform;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.enderman.EndermanModel;
import net.minecraft.client.renderer.entity.state.EndermanRenderState;

public class ModelBabyEnderman extends EndermanModel<EndermanRenderState> {

    public static final BabyModelTransform BABY_MODEL_TRANSFORM = new BabyModelTransform(false, 18.5F, 2.0F, 2.0F, 2.0F, 24.0F, Set.of("head"));

    public ModelBabyEnderman(ModelPart part) {
        super(part);
    }

    @Override
    public void setupAnim(EndermanRenderState state) {
        super.setupAnim(state);
        if (state.isCreepy) {
            //Shift the head when angry to only the third the distance it goes up when it is an adult
            float amt = 5.0F / 3;
            this.head.y -= amt;
            this.hat.y += amt;
        }
    }
}