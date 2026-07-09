package mekanism.tools.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import mekanism.tools.common.MekanismTools;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.equipment.ShieldModel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.ShieldSpecialRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Unit;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.jspecify.annotations.Nullable;

public class RenderMekanismShieldItem extends ShieldSpecialRenderer {

    private final ShieldModel model;
    private final SpriteGetter sprites;
    private final SpriteId sprite;

    public RenderMekanismShieldItem(ShieldModel model, SpriteGetter sprites, SpriteId sprite) {
        super(sprites, model);
        this.model = model;
        this.sprites = sprites;
        this.sprite = sprite;
    }

    @Override
    public void submit(@Nullable DataComponentMap components, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        BannerPatternLayers patterns = components == null ? BannerPatternLayers.EMPTY : components.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
        DyeColor baseColor = components == null ? null : components.get(DataComponents.BASE_COLOR);
        boolean hasPatterns = !patterns.layers().isEmpty() || baseColor != null;
        //from ShieldSpecialRenderer: SpriteId base = hasPatterns ? Sheets.SHIELD_BASE : Sheets.SHIELD_BASE_NO_PATTERN;
        submitNodeCollector.submitModel(this.model, Unit.INSTANCE, poseStack, lightCoords, overlayCoords, CommonColors.WHITE, this.sprite, this.sprites, outlineColor, null);
        if (hasPatterns) {
            BannerRenderer.submitPatterns(this.sprites, poseStack, submitNodeCollector, lightCoords, overlayCoords, this.model, Unit.INSTANCE, false,
                  Objects.requireNonNullElse(baseColor, DyeColor.WHITE), patterns, null);
        }

        if (hasFoil) {
            submitNodeCollector.order(patterns.layers().size() + 1)
                  .submitModel(this.model, Unit.INSTANCE, poseStack, RenderTypes.entityGlint(), lightCoords, overlayCoords, CommonColors.WHITE,
                  this.sprites.get(this.sprite), EntityRenderState.NO_OUTLINE, null);
        }
    }

    public record UnbakedShield(Identifier texture) implements SpecialModelRenderer.Unbaked<DataComponentMap> {

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