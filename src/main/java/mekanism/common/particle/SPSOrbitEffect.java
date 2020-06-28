package mekanism.common.particle;

import mekanism.common.content.sps.SPSMultiblockData;
import mekanism.common.lib.Color;
import mekanism.common.lib.effect.CustomEffect;
import mekanism.common.lib.math.Pos3D;
import mekanism.common.lib.math.Quaternion;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;

public class SPSOrbitEffect extends CustomEffect {

    private static final ResourceLocation TEXTURE = MekanismUtils.getResource(ResourceType.RENDER, "sps_orbit_effect.png");

    private final SPSMultiblockData multiblock;

    private final Vector3d center;
    private final Vector3d start;
    private final Pos3D axis;

    private float speed = 0.5F;
    private final float radius;

    public SPSOrbitEffect(SPSMultiblockData multiblock, Vector3d center) {
        super(TEXTURE, 1);
        this.multiblock = multiblock;
        this.center = center;
        radius = 1 + (float) rand.nextDouble();
        start = randVec().scale(radius);
        pos = center.add(start);
        axis = new Pos3D(randVec());
        scale = 0.01F + rand.nextFloat() * 0.04F;
        color = Color.rgbai(102, 215, 237, 240);
    }

    @Override
    public boolean tick() {
        if (super.tick()) {
            return true;
        }
        speed = (float) Math.log10(multiblock.lastReceivedEnergy.doubleValue());
        return !multiblock.isFormed();
    }

    @Override
    public Vector3d getPos(float partialTick) {
        return center.add(Quaternion.rotate(start, axis, (ticker + partialTick) * speed));
    }
}
