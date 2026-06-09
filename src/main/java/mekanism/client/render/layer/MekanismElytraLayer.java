package mekanism.client.render.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.common.Mekanism;
import mekanism.common.registries.MekanismItems;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class MekanismElytraLayer<STATE extends HumanoidRenderState, MODEL extends EntityModel<STATE>> extends WingsLayer<STATE, MODEL> {

    private static final Identifier HDPE_ELYTRA = Mekanism.rl("textures/entity/hdpe_elytra.png");

    public MekanismElytraLayer(RenderLayerParent<STATE, MODEL> entityRenderer, EntityModelSet modelSet, EquipmentLayerRenderer equipmentRenderer) {
        super(entityRenderer, modelSet, equipmentRenderer);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, STATE state, float yRot, float xRot) {
        ItemStack stack = state.chestEquipment;
        if (!stack.is(MekanismItems.HDPE_REINFORCED_ELYTRA)) {
            return;
        }
        super.submit(poseStack, submitNodeCollector, lightCoords, state, yRot, xRot);
    }

    //todo - 26.1: this is now a static method @Override
    public Identifier getElytraTexture(ItemStack stack, STATE state) {
        return HDPE_ELYTRA;
    }
}