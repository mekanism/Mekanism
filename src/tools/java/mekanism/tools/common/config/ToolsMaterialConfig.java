package mekanism.tools.common.config;

import mekanism.common.config.BaseMekanismConfig;
import mekanism.tools.common.material.MaterialCreator;
import mekanism.tools.common.material.VanillaPaxelMaterialCreator;
import mekanism.tools.common.material.impl.BronzeMaterialDefaults;
import mekanism.tools.common.material.impl.LapisLazuliMaterialDefaults;
import mekanism.tools.common.material.impl.OsmiumMaterialDefaults;
import mekanism.tools.common.material.impl.RefinedGlowstoneMaterialDefaults;
import mekanism.tools.common.material.impl.RefinedObsidianMaterialDefaults;
import mekanism.tools.common.material.impl.SteelMaterialDefaults;
import net.minecraft.world.item.ToolMaterial;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.common.ModConfigSpec;

public class ToolsMaterialConfig extends BaseMekanismConfig {

    private final ModConfigSpec configSpec;

    public final VanillaPaxelMaterialCreator wood;
    public final VanillaPaxelMaterialCreator stone;
    public final VanillaPaxelMaterialCreator copper;
    public final VanillaPaxelMaterialCreator iron;
    public final VanillaPaxelMaterialCreator gold;
    public final VanillaPaxelMaterialCreator diamond;
    public final VanillaPaxelMaterialCreator netherite;
    public final MaterialCreator bronze;
    public final MaterialCreator lapisLazuli;
    public final MaterialCreator osmium;
    public final MaterialCreator refinedGlowstone;
    public final MaterialCreator refinedObsidian;
    public final MaterialCreator steel;

    ToolsMaterialConfig() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        ToolsConfigTranslations.STARTUP_MATERIALS.applyToBuilder(builder).push("materials");
        wood = new VanillaPaxelMaterialCreator(this, builder, "wood", ToolMaterial.WOOD, 6.0F);
        stone = new VanillaPaxelMaterialCreator(this, builder, "stone", ToolMaterial.STONE, 7.0F);
        copper = new VanillaPaxelMaterialCreator(this, builder, "copper", ToolMaterial.COPPER, 7.0F);
        iron = new VanillaPaxelMaterialCreator(this, builder, "iron", ToolMaterial.IRON, 6.0F);
        gold = new VanillaPaxelMaterialCreator(this, builder, "gold", ToolMaterial.GOLD, 6.0F);
        diamond = new VanillaPaxelMaterialCreator(this, builder, "diamond", ToolMaterial.DIAMOND, 5.0F);
        netherite = new VanillaPaxelMaterialCreator(this, builder, "netherite", ToolMaterial.NETHERITE, 5.0F);

        lapisLazuli = new MaterialCreator(this, builder, new LapisLazuliMaterialDefaults());
        bronze = new MaterialCreator(this, builder, new BronzeMaterialDefaults());
        osmium = new MaterialCreator(this, builder, new OsmiumMaterialDefaults());
        steel = new MaterialCreator(this, builder, new SteelMaterialDefaults());
        refinedGlowstone = new MaterialCreator(this, builder, new RefinedGlowstoneMaterialDefaults());
        refinedObsidian = new MaterialCreator(this, builder, new RefinedObsidianMaterialDefaults());
        builder.pop();

        configSpec = builder.build();
    }

    @Override
    public String getFileName() {
        return "tools-materials-startup";
    }

    @Override
    public String getTranslation() {
        return "Material Config";
    }

    @Override
    public ModConfigSpec getConfigSpec() {
        return configSpec;
    }

    @Override
    public Type getConfigType() {
        return Type.STARTUP;
    }
}