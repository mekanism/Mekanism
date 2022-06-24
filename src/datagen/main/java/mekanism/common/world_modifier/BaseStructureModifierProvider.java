package mekanism.common.world_modifier;

import javax.annotation.Nonnull;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.world.StructureModifier;
import net.minecraftforge.registries.ForgeRegistries;

public abstract class BaseStructureModifierProvider extends BaseModifierProvider<StructureModifier> {

    protected BaseStructureModifierProvider(DataGenerator gen, String modid) {
        super(gen, modid, ForgeRegistries.Keys.STRUCTURE_MODIFIERS);
    }

    @Nonnull
    @Override
    public String getName() {
        return "Structure modifiers: " + modid;
    }
}