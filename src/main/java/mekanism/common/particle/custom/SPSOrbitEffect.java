package mekanism.common.particle.custom;

import mekanism.common.content.sps.SPSMultiblockData;
import mekanism.common.lib.Color;
import mekanism.common.lib.math.Pos3D;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

public class SPSOrbitEffect extends CustomEffect {

    private static final ResourceLocation TEXTURE = MekanismUtils.getResource(ResourceType.RENDER, "sps_orbit_effect.png");

    private SPSMultiblockData multiblock;
    private Vec3d center;
    private Vec3d start;
    private Pos3D axis;
    private float speed = 0.5F;
    private float radius = 2F;

    public SPSOrbitEffect(SPSMultiblockData multiblock, Vec3d center) {
        super(TEXTURE, 1);
        this.multiblock = multiblock;
        this.center = center;
        radius = 1 + (float) rand.nextDouble();
        start = randVec().scale(radius);
        pos = center.add(start);
        axis = new Pos3D(randVec());
        scale = 0.01F + rand.nextFloat() * 0.04F;
        color = Color.rgba(102, 215, 237, 240);
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
    public Vec3d getPos(float partialTick) {
        return center.add(new Pos3D(start).rotate((ticker + partialTick) * speed, axis));
    }
}
