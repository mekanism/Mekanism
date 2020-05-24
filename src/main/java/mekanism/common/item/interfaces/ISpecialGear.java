package mekanism.common.item.interfaces;

import javax.annotation.Nonnull;
import mekanism.client.render.armor.CustomArmor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ISpecialGear {

    @Nonnull
    @OnlyIn(Dist.CLIENT)
    CustomArmor getGearModel();
}