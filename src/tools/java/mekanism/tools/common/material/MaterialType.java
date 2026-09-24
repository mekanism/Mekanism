package mekanism.tools.common.material;

import java.util.List;
import java.util.Locale;
import mekanism.tools.common.config.MekanismToolsConfig;
import mekanism.tools.common.registration.ArmorCollection;
import mekanism.tools.common.registration.ToolCollection;
import mekanism.tools.common.registries.ToolsItems;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Unmodifiable;

public enum MaterialType implements StringRepresentable {
    BRONZE(MekanismToolsConfig.materials.bronze, ToolsItems.BRONZE_ARMOR, ToolsItems.BRONZE_TOOLS),
    LAPIS_LAZULI(MekanismToolsConfig.materials.lapisLazuli, ToolsItems.LAPIS_LAZULI_ARMOR, ToolsItems.LAPIS_LAZULI_TOOLS),
    OSMIUM(MekanismToolsConfig.materials.osmium, ToolsItems.OSMIUM_ARMOR, ToolsItems.OSMIUM_TOOLS),
    REFINED_GLOWSTONE(MekanismToolsConfig.materials.refinedGlowstone, ToolsItems.REFINED_GLOWSTONE_ARMOR, ToolsItems.REFINED_GLOWSTONE_TOOLS),
    REFINED_OBSIDIAN(MekanismToolsConfig.materials.refinedObsidian, ToolsItems.REFINED_OBSIDIAN_ARMOR, ToolsItems.REFINED_OBSIDIAN_TOOLS),
    STEEL(MekanismToolsConfig.materials.steel, ToolsItems.STEEL_ARMOR, ToolsItems.STEEL_TOOLS);

    @Unmodifiable
    public static final List<MaterialType> VALUES = List.of(values());

    private final String serializedName;
    public final MaterialCreator material;
    public final ArmorCollection armor;
    public final ToolCollection tools;

    MaterialType(MaterialCreator material, ArmorCollection armor, ToolCollection tools) {
        this.serializedName = name().toLowerCase(Locale.ROOT);
        this.material = material;
        this.armor = armor;
        this.tools = tools;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}