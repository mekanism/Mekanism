package mekanism.common.world_modifier;

import javax.annotation.Nonnull;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.registries.ForgeRegistries;

public abstract class BaseBiomeModifierProvider extends BaseModifierProvider<BiomeModifier> {

    protected BaseBiomeModifierProvider(DataGenerator gen, String modid) {
        super(gen, modid, ForgeRegistries.Keys.BIOME_MODIFIERS);
    }

    @Nonnull
    @Override
    public String getName() {
        return "Biome modifiers: " + modid;
    }
}