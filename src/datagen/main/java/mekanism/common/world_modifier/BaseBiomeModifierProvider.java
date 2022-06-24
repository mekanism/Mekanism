package mekanism.common.world_modifier;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

public abstract class BaseBiomeModifierProvider extends BaseModifierProvider<BiomeModifier> {

    protected BaseBiomeModifierProvider(DataGenerator gen, String modid) {
        super(gen, modid, ForgeRegistries.Keys.BIOME_MODIFIERS);
    }

    @NotNull
    @Override
    public String getName() {
        return "Biome modifiers: " + modid;
    }
}