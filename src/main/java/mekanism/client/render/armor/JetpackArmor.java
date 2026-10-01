package mekanism.client.render.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.client.model.MekanismJavaModel.FoilRendering;
import mekanism.client.model.ModelArmoredJetpack;
import mekanism.client.model.ModelJetpack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.server.packs.resources.ResourceManager;

public class JetpackArmor extends SimpleCustomArmor<ModelJetpack> {

    public static final JetpackArmor JETPACK = new JetpackArmor(false);
    public static final JetpackArmor ARMORED_JETPACK = new JetpackArmor(true);

    private final boolean armored;

    private JetpackArmor(boolean armored) {
        this.armored = armored;
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        EntityModelSet modelSet = Minecraft.getInstance().getEntityModels();
        if (armored) {
            model = new ModelArmoredJetpack(modelSet);
            babyModel = new ModelArmoredJetpack(modelSet.bakeLayer(ModelArmoredJetpack.ARMORED_JETPACK_BABY_LAYER));
        } else {
            model = new ModelJetpack(modelSet);
            babyModel = new ModelJetpack(modelSet.bakeLayer(ModelArmoredJetpack.JETPACK_BABY_LAYER));
        }
    }

    @Override
    protected void collect(PoseStack poseStack, SubmitNodeCollector collector, ModelJetpack model, int light, int overlayLight, FoilRendering foil, int outlineColor) {
        model.collect(poseStack, collector, light, overlayLight, foil, outlineColor);
    }

    @Override
    protected ModelPart humanoidPart(HumanoidModel<?> baseModel) {
        return baseModel.body;
    }

    @Override
    protected double getZOffset() {
        return 0.06;
    }
}