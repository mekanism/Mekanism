package mekanism.common.item.gear;

import javax.annotation.Nonnull;
import mekanism.client.render.ModelCustomArmor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ISpecialGear {

    @Nonnull
    @OnlyIn(Dist.CLIENT)
    ModelCustomArmor getGearModel();
}