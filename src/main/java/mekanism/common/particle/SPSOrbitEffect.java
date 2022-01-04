package mekanism.common.particle;

import mekanism.common.content.sps.SPSMultiblockData;
import mekanism.common.lib.Color;
import mekanism.common.lib.effect.CustomEffect;
import mekanism.common.lib.math.Quaternion;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class SPSOrbitEffect extends CustomEffect {

    private static final ResourceLocation TEXTURE = MekanismUtils.getResource(ResourceType.RENDER, "sps_orbit_effect.png");

    private final Vec3 center;
    private final Vec3 start;
    private final Vec3 axis;

    private float speed = 0.5F;
    private final float radius;

    private SPSMultiblockData multiblock;

    public SPSOrbitEffect(SPSMultiblockData multiblock, Vec3 center) {
        super(TEXTURE, 1);
        this.multiblock = multiblock;
        this.center = center;
        radius = 1 + (float) rand.nextDouble();
        start = randVec().scale(radius);
        pos = center.add(start);
        axis = randVec();
        scale = 0.01F + rand.nextFloat() * 0.04F;
        color = Color.rgbai(102, 215, 237, 240);
    }

    public void updateMultiblock(SPSMultiblockData multiblock) {
        this.multiblock = multiblock;
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
    public Vec3 getPos(float partialTick) {
        return center.add(Quaternion.rotate(start, axis, (ticker + partialTick) * speed));
    }
}
