package mekanism.client.integration.framedblocks;

import mekanism.api.chemical.ChemicalResource;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.integration.framedblocks.ChemicalParticleOptions;
import mekanism.common.util.WorldUtils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.RandomSource;
import org.jspecify.annotations.Nullable;

final class ChemicalSpriteParticle extends SingleQuadParticle {

    private final SingleQuadParticle.Layer layer;
    private final BlockPos pos;
    private final float uo;
    private final float vo;
    private final int brightness;

    ChemicalSpriteParticle(ClientLevel level, double x, double y, double z, double sx, double sy, double sz, ChemicalResource chemical) {
        super(level, x, y, z, sx, sy, sz, MekanismRenderer.getChemicalTexture(chemical));
        this.pos = BlockPos.containing(x, y, z);
        this.gravity = 1F;
        this.quadSize /= 2F;
        this.uo = random.nextFloat() * 3F;
        this.vo = random.nextFloat() * 3F;
        this.brightness = 0;

        int tint = chemical.value().tint();
        this.rCol = 0.6F * ARGB.redFloat(tint);
        this.gCol = 0.6F * ARGB.greenFloat(tint);
        this.bCol = 0.6F * ARGB.blueFloat(tint);
        this.layer = Layer.bySprite(sprite);
    }

    @Override
    protected SingleQuadParticle.Layer getLayer() {
        return layer;
    }

    @Override
    protected float getU0() {
        return sprite.getU((uo + 1.0F) / 4.0F);
    }

    @Override
    protected float getU1() {
        return sprite.getU(uo / 4.0F);
    }

    @Override
    protected float getV0() {
        return sprite.getV(vo / 4.0F);
    }

    @Override
    protected float getV1() {
        return sprite.getV((vo + 1.0F) / 4.0F);
    }

    @Override
    public int getLightCoords(float partialTick) {
        int light = WorldUtils.isChunkLoaded(level, pos) ? LightCoordsUtil.getLightCoords(level, pos) : 0;
        int block = Math.max(brightness, LightCoordsUtil.block(light));
        return LightCoordsUtil.pack(block, LightCoordsUtil.sky(light));
    }

    static final class Provider implements ParticleProvider<ChemicalParticleOptions> {

        @Nullable
        @Override
        public Particle createParticle(ChemicalParticleOptions type, ClientLevel level, double x, double y, double z, double sx, double sy, double sz, RandomSource random) {
            if (!type.chemical().isEmpty()) {
                return new ChemicalSpriteParticle(level, x, y, z, sx, sy, sz, type.chemical());
            }
            return null;
        }
    }
}
