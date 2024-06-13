package mekanism.additions.client.render.entity.layer;

import mekanism.additions.client.model.ModelBabyEnderman;
import mekanism.additions.common.entity.baby.EntityBabyEnderman;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class BabyEndermanEyesLayer extends EyesLayer<EntityBabyEnderman, ModelBabyEnderman> {

    private static final RenderType RENDER_TYPE = RenderType.eyes(ResourceLocation.withDefaultNamespace("textures/entity/enderman/enderman_eyes.png"));

    public BabyEndermanEyesLayer(RenderLayerParent<EntityBabyEnderman, ModelBabyEnderman> renderer) {
        super(renderer);
    }

    @NotNull
    @Override
    public RenderType renderType() {
        return RENDER_TYPE;
    }
}