package mekanism.client.model.itemtint;

import com.mojang.serialization.MapCodec;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.common.content.gear.shared.ModuleColorModulationUnit;
import mekanism.common.registries.MekanismModules;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class ColorModulationTint implements ItemTintSource {

    public static final ColorModulationTint INSTANCE = new ColorModulationTint();
    public static final MapCodec<ColorModulationTint> MAP_CODEC = MapCodec.unit(INSTANCE);

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
        IModule<ModuleColorModulationUnit> colorModulationUnit = IModuleHelper.INSTANCE.getModule(stack, MekanismModules.COLOR_MODULATION_UNIT);
        if (colorModulationUnit == null) {
            return -1;
        }
        return colorModulationUnit.getCustomInstance().tintARGB();
    }

    @Override
    public MapCodec<? extends ItemTintSource> type() {
        return MAP_CODEC;
    }
}
