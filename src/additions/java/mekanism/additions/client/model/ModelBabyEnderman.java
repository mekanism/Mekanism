package mekanism.additions.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.enderman.EndermanModel;
import net.minecraft.client.renderer.entity.state.EndermanRenderState;
import org.jetbrains.annotations.NotNull;

public class ModelBabyEnderman extends EndermanModel<EndermanRenderState> {

    public ModelBabyEnderman(ModelPart part) {
        super(part);
    }

    /*@NotNull TODO - 26.1: is this relevant/possible?
    @Override
    protected Iterable<ModelPart> headParts() {
        //Make the "hat" (the jaw) be part of the head for scaling purposes
        return List.of(this.head, this.hat);
    }
*/

    @Override
    public void setupAnim(@NotNull EndermanRenderState state) {
        super.setupAnim(state);
        //Shift the head and the "hat" (jaw) be in the proper place for baby endermen
        head.y += 5.0F;
        hat.y += 5.0F;
        if (state.isCreepy) {
            //Shift the head when angry to only the third the distance it goes up when it is an adult
            head.y += 1.67F;
        }
    }
}