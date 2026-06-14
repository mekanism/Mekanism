package mekanism.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.function.Consumer;
import mekanism.api.robit.RobitSkin;
import mekanism.client.model.robit.RobitSkinManager;
import mekanism.client.model.robit.RobitSkinManager.BakeResult;
import mekanism.client.render.entity.RenderRobit;
import mekanism.common.Mekanism;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.registries.MekanismRobitSkins;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import org.joml.Vector3fc;

public class RenderRobitItem implements SpecialModelRenderer<BakeResult> {

    public static final RenderRobitItem INSTANCE = new RenderRobitItem();

    private Vector3fc @Nullable[] extents = null;

    @Override
    public void submit(@Nullable BakeResult argument, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        if (argument == null) {
            return;
        }
        RenderRobit.submitRobitSkin(argument, poseStack, submitNodeCollector, overlayCoords, lightCoords, outlineColor);
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        if (extents == null) {
            extents = RobitSkinManager.get().getExtents();
        }
        for (Vector3fc extent : extents) {
            output.accept(extent);
        }
    }

    @Nullable
    @Override
    public BakeResult extractArgument(ItemStack stack) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            Mekanism.logger.error("Failed to get robit item skin. No registry access available");
            return null;
        }
        try {
            ResourceKey<RobitSkin> skinKey = stack.getOrDefault(MekanismDataComponents.ROBIT_SKIN, MekanismRobitSkins.BASE);
            RobitSkin skin = MekanismRobitSkins.get(level.registryAccess(), skinKey);

            List<Identifier> textures = skin.textures();

            return RobitSkinManager.get().getBaked(skin, textures.isEmpty() ? null : textures.getFirst());
        } catch (Exception e) {
            Mekanism.logger.error("Failed to get robit item skin", e);
            return null;
        }
    }

    public static class Unbaked implements SpecialModelRenderer.Unbaked<BakeResult> {

        public static final Unbaked INSTANCE = new Unbaked();
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(INSTANCE);

        private Unbaked(){}

        @Override
        @Nullable
        public RenderRobitItem bake(BakingContext context) {
            return RenderRobitItem.INSTANCE;
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
