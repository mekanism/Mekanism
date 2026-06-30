package mekanism.tools.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.function.Consumer;
import mekanism.tools.client.render.item.RenderMekanismShieldItem.MekShieldState;
import mekanism.tools.common.MekanismTools;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.equipment.ShieldModel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Unit;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public class RenderMekanismShieldItem implements SpecialModelRenderer<MekShieldState> {

    private final ShieldModel model;
    private final SpriteGetter sprites;
    private final SpriteId sprite;

    public RenderMekanismShieldItem(ShieldModel model, SpriteGetter sprites, SpriteId sprite) {
        this.model = model;
        this.sprites = sprites;
        this.sprite = sprite;
    }

    @Nullable
    @Override
    public MekShieldState extractArgument(ItemStack stack) {
        MekShieldState state = new MekShieldState();
        state.bannerPattern = stack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
        state.color = stack.get(DataComponents.BASE_COLOR);
        return state;
    }

    @Override
    public void submit(@Nullable MekShieldState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        if (state == null) {
            return;
        }
        BannerPatternLayers patterns = state.bannerPattern;
        DyeColor baseColor = state.color;
        boolean hasPatterns = !patterns.layers().isEmpty() || baseColor != null;
        //from ShieldSpecialRenderer: SpriteId base = hasPatterns ? Sheets.SHIELD_BASE : Sheets.SHIELD_BASE_NO_PATTERN;
        submitNodeCollector.submitModel(this.model, Unit.INSTANCE, poseStack, lightCoords, overlayCoords, CommonColors.WHITE, this.sprite, this.sprites, outlineColor, null);
        if (hasPatterns) {
            BannerRenderer.submitPatterns(this.sprites, poseStack, submitNodeCollector, lightCoords, overlayCoords, this.model, Unit.INSTANCE, false,
                  Objects.requireNonNullElse(baseColor, DyeColor.WHITE), patterns, null);
        }

        if (hasFoil) {
            submitNodeCollector.submitModel(this.model, Unit.INSTANCE, poseStack, RenderTypes.entityGlint(), lightCoords, overlayCoords, CommonColors.WHITE,
                  this.sprites.get(this.sprite), EntityRenderState.NO_OUTLINE, null);
        }
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        PoseStack poseStack = new PoseStack();
        this.model.root().getExtentsForGui(poseStack, output);
    }

    public static class MekShieldState {

        BannerPatternLayers bannerPattern = BannerPatternLayers.EMPTY;
        @Nullable
        DyeColor color;
    }

    public record UnbakedShield(Identifier texture) implements Unbaked<MekShieldState> {

        public static final Identifier ID = MekanismTools.rl("shield");
        public static final MapCodec<UnbakedShield> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
              Identifier.CODEC.fieldOf("texture").forGetter(UnbakedShield::texture)
        ).apply(i, UnbakedShield::new));

        @Override
        public RenderMekanismShieldItem bake(BakingContext context) {
            SpriteId fullTexture = Sheets.SHIELD_MAPPER.apply(this.texture);
            return new RenderMekanismShieldItem(new ShieldModel(context.entityModelSet().bakeLayer(ModelLayers.SHIELD)), context.sprites(), fullTexture);
        }

        @Override
        public MapCodec<UnbakedShield> type() {
            return MAP_CODEC;
        }
    }
}