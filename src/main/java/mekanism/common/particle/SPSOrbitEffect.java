package mekanism.common.particle;

import mekanism.client.render.MekanismRenderType;
import mekanism.common.content.sps.SPSMultiblockData;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.util.RandomSource;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class SPSOrbitEffect {

    public static final RenderType RENDER_TYPE = MekanismRenderType.SPS.apply(MekanismUtils.getRenderResource("sps_orbit_effect.png"));
    public static final int COLOR = 0xF066D7ED;

    private final Quaternionf rotation = new Quaternionf();
    private final Vector3f start;
    private final Vector3f axis;
    private final float scale;

    private float speed = 0.5F;
    private int ticker;

    private SPSMultiblockData multiblock;

    public SPSOrbitEffect(SPSMultiblockData multiblock, RandomSource random) {
        this.multiblock = multiblock;
        this.start = randVec(random).mul(1 + random.nextFloat());
        this.axis = randVec(random);
        this.scale = 0.01F + random.nextFloat() * 0.04F;
    }

    public void updateMultiblock(SPSMultiblockData multiblock) {
        this.multiblock = multiblock;
    }

    public boolean tick() {
        if (!multiblock.isFormed()) {
            return true;
        }
        ticker++;
        speed = (float) Math.log10(multiblock.lastReceivedEnergy);
        return false;
    }

    public int getTick() {
        return ticker;
    }

    public float getScale() {
        return scale;
    }

    public void transformPos(Vector3f pos, float partialTick) {
        this.rotation.fromAxisAngleDeg(axis, (ticker + partialTick) * speed);
        this.rotation.transform(start, pos);
    }

    private static Vector3f randVec(RandomSource random) {
        return new Vector3f(random.nextFloat() - 0.5F, random.nextFloat() - 0.5F, random.nextFloat() - 0.5F).normalize();
    }
}