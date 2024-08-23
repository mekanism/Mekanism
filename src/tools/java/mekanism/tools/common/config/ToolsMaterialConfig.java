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
import mekanism.tools.common.material.impl.vanilla.DiamondPaxelMaterialDefaults;
import mekanism.tools.common.material.impl.vanilla.GoldPaxelMaterialDefaults;
import mekanism.tools.common.material.impl.vanilla.IronPaxelMaterialDefaults;
import mekanism.tools.common.material.impl.vanilla.NetheritePaxelMaterialDefaults;
import mekanism.tools.common.material.impl.vanilla.StonePaxelMaterialDefaults;
import mekanism.tools.common.material.impl.vanilla.WoodPaxelMaterialDefaults;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.common.ModConfigSpec;

public class ToolsMaterialConfig extends BaseMekanismConfig {

    private final ModConfigSpec configSpec;

    public final VanillaPaxelMaterialCreator wood;
    public final VanillaPaxelMaterialCreator stone;
    public final VanillaPaxelMaterialCreator iron;
    public final VanillaPaxelMaterialCreator diamond;
    public final VanillaPaxelMaterialCreator gold;
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
        wood = new VanillaPaxelMaterialCreator(this, builder, new WoodPaxelMaterialDefaults());
        stone = new VanillaPaxelMaterialCreator(this, builder, new StonePaxelMaterialDefaults());
        gold = new VanillaPaxelMaterialCreator(this, builder, new GoldPaxelMaterialDefaults());
        iron = new VanillaPaxelMaterialCreator(this, builder, new IronPaxelMaterialDefaults());
        diamond = new VanillaPaxelMaterialCreator(this, builder, new DiamondPaxelMaterialDefaults());
        netherite = new VanillaPaxelMaterialCreator(this, builder, new NetheritePaxelMaterialDefaults());

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