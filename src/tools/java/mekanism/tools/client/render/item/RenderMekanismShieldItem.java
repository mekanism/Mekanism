package mekanism.tools.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import java.util.Objects;
import java.util.function.Consumer;
import mekanism.common.Mekanism;
import mekanism.tools.client.ShieldTextures;
import mekanism.tools.common.MekanismTools;
import mekanism.tools.common.registries.ToolsItems;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.equipment.ShieldModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public class RenderMekanismShieldItem implements SpecialModelRenderer<RenderMekanismShieldItem.MekShieldState> {

    private final ShieldModel model;
    private final SpriteGetter sprites;

    public RenderMekanismShieldItem(ShieldModel model, SpriteGetter sprites) {
        this.model = model;
        this.sprites = sprites;
    }

    @Nullable
    @Override
    public MekShieldState extractArgument(ItemStack stack) {
        MekShieldState state = new MekShieldState();

        ShieldTextures textures;
        if (stack.is(ToolsItems.BRONZE_SHIELD)) {
            textures = ShieldTextures.BRONZE;
        } else if (stack.is(ToolsItems.LAPIS_LAZULI_SHIELD)) {
            textures = ShieldTextures.LAPIS_LAZULI;
        } else if (stack.is(ToolsItems.OSMIUM_SHIELD)) {
            textures = ShieldTextures.OSMIUM;
        } else if (stack.is(ToolsItems.REFINED_GLOWSTONE_SHIELD)) {
            textures = ShieldTextures.REFINED_GLOWSTONE;
        } else if (stack.is(ToolsItems.REFINED_OBSIDIAN_SHIELD)) {
            textures = ShieldTextures.REFINED_OBSIDIAN;
        } else if (stack.is(ToolsItems.STEEL_SHIELD)) {
            textures = ShieldTextures.STEEL;
        } else {
            Mekanism.logger.warn("Unknown item for mekanism shield renderer: {}", stack.getItem());
            return null;
        }

        state.base = textures.getBase();
        state.bannerPattern = stack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
        state.color = stack.get(DataComponents.BASE_COLOR);
        return state;
    }

    @Override
    public void submit(@Nullable MekShieldState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        if (state == null || state.base == null) {
            return;
        }
        BannerPatternLayers patterns = state.bannerPattern;
        DyeColor baseColor = state.color;
        boolean hasPatterns = !patterns.layers().isEmpty() || baseColor != null;
        //from ShieldSpecialRenderer: SpriteId base = hasPatterns ? Sheets.SHIELD_BASE : Sheets.SHIELD_BASE_NO_PATTERN;
        submitNodeCollector.submitModel(this.model, Unit.INSTANCE, poseStack, lightCoords, overlayCoords, -1, state.base, this.sprites, outlineColor, null);
        if (hasPatterns) {
            BannerRenderer.submitPatterns(
                  this.sprites,
                  poseStack,
                  submitNodeCollector,
                  lightCoords,
                  overlayCoords,
                  this.model,
                  Unit.INSTANCE,
                  false,
                  Objects.requireNonNullElse(baseColor, DyeColor.WHITE),
                  patterns,
                  null
            );
        }

        if (hasFoil) {
            submitNodeCollector.submitModel(
                  this.model, Unit.INSTANCE, poseStack, RenderTypes.entityGlint(), lightCoords, overlayCoords, -1, this.sprites.get(state.base), 0, null
            );
        }
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        PoseStack poseStack = new PoseStack();
        this.model.root().getExtentsForGui(poseStack, output);
    }

    public static class MekShieldState {

        @Nullable
        SpriteId base;
        BannerPatternLayers bannerPattern = BannerPatternLayers.EMPTY;
        @Nullable
        DyeColor color;
    }

    public static class UnbakedShield implements Unbaked<RenderMekanismShieldItem.MekShieldState> {

        public static final Identifier ID = MekanismTools.rl("shield");
        public static final UnbakedShield INSTANCE = new UnbakedShield();
        public static final MapCodec<UnbakedShield> MAP_CODEC = MapCodec.unit(INSTANCE);

        @Override
        public @Nullable RenderMekanismShieldItem bake(BakingContext context) {
            return new RenderMekanismShieldItem(new ShieldModel(context.entityModelSet().bakeLayer(ModelLayers.SHIELD)), context.sprites());
        }

        @Override
        public MapCodec<UnbakedShield> type() {
            return MAP_CODEC;
        }
    }
}