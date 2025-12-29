package mekanism.client.render.layer;

import mekanism.common.Mekanism;
import mekanism.common.registries.MekanismItems;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class MekanismElytraLayer<STATE extends HumanoidRenderState, MODEL extends EntityModel<STATE>> extends WingsLayer<STATE, MODEL> {

    private static final Identifier HDPE_ELYTRA = Mekanism.rl("textures/entity/hdpe_elytra.png");

    public MekanismElytraLayer(RenderLayerParent<STATE, MODEL> entityRenderer, EntityModelSet modelSet, EquipmentLayerRenderer equipmentRenderer) {
        super(entityRenderer, modelSet, equipmentRenderer);
    }

    @Override
    public boolean shouldRender(@NotNull ItemStack stack, @NotNull STATE state) {
        return stack.is(MekanismItems.HDPE_REINFORCED_ELYTRA);
    }

    @NotNull
    @Override
    public Identifier getElytraTexture(@NotNull ItemStack stack, @NotNull STATE state) {
        return HDPE_ELYTRA;
    }
}