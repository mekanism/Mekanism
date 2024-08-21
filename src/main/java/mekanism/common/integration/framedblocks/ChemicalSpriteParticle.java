package mekanism.common.integration.framedblocks;

import mekanism.api.chemical.Chemical;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.WorldUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;

final class ChemicalSpriteParticle extends TextureSheetParticle {

    private final BlockPos pos;
    private final float uo;
    private final float vo;
    private final int brightness;

    ChemicalSpriteParticle(ClientLevel level, double x, double y, double z, double sx, double sy, double sz, Chemical chemical) {
        super(level, x, y, z, sx, sy, sz);
        this.pos = BlockPos.containing(x, y, z);
        this.gravity = 1F;
        this.quadSize /= 2F;
        this.uo = random.nextFloat() * 3F;
        this.vo = random.nextFloat() * 3F;
        this.brightness = 0;

        int tint = chemical.getTint();
        this.rCol = .6F * MekanismRenderer.getRed(tint);
        this.gCol = .6F * MekanismRenderer.getGreen(tint);
        this.bCol = .6F * MekanismRenderer.getBlue(tint);

        setSprite(Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(chemical.getIcon()));
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.TERRAIN_SHEET;
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
    public int getLightColor(float partialTick) {
        int light = WorldUtils.isChunkLoaded(level, pos) ? LevelRenderer.getLightColor(level, pos) : 0;
        int block = Math.max(brightness, LightTexture.block(light));
        return LightTexture.pack(block, LightTexture.sky(light));
    }

    static final class Provider implements ParticleProvider<ChemicalParticleOptions> {

        @Override
        public Particle createParticle(ChemicalParticleOptions type, ClientLevel level, double x, double y, double z, double sx, double sy, double sz) {
            return new ChemicalSpriteParticle(level, x, y, z, sx, sy, sz, type.chemical());
        }
    }
}
