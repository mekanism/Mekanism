package mekanism.common.world_modifier;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.world.StructureModifier;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

public abstract class BaseStructureModifierProvider extends BaseModifierProvider<StructureModifier> {

    protected BaseStructureModifierProvider(DataGenerator gen, String modid) {
        super(gen, modid, ForgeRegistries.Keys.STRUCTURE_MODIFIERS);
    }

    @NotNull
    @Override
    public String getName() {
        return "Structure modifiers: " + modid;
    }
}