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
    private static final Color COLOR = Color.rgbai(102, 215, 237, 240);

    private final Vec3 center;
    private final Vec3 start;
    private final Vec3 axis;

    private float speed = 0.5F;

    private SPSMultiblockData multiblock;

    public SPSOrbitEffect(SPSMultiblockData multiblock, Vec3 center) {
        super(TEXTURE, 1);
        this.multiblock = multiblock;
        this.center = center;
        float radius = 1 + (float) rand.nextDouble();
        start = randVec().scale(radius);
        axis = randVec();
        setPos(this.center.add(start));
        setScale(0.01F + rand.nextFloat() * 0.04F);
        setColor(COLOR);
    }

    public void updateMultiblock(SPSMultiblockData multiblock) {
        this.multiblock = multiblock;
    }

    @Override
    public boolean tick() {
        if (super.tick() || !multiblock.isFormed()) {
            return true;
        }
        speed = (float) Math.log10(multiblock.lastReceivedEnergy);
        return false;
    }

    @Override
    public Vec3 getPos(float partialTick) {
        return center.add(Quaternion.rotate(start, axis, (ticker + partialTick) * speed));
    }
}