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
        builder.comment("Mekanism Tools Startup Config. This config is loaded on early, and requires a game restart to take effect, and is not synced automatically between "
                        + "client and server. It is highly recommended to ensure you are using the same values for this config on the server and client.").push("startup");

        builder.comment("Startup material configs").push("materials");
        wood = new VanillaPaxelMaterialCreator(this, builder, new WoodPaxelMaterialDefaults());
        stone = new VanillaPaxelMaterialCreator(this, builder, new StonePaxelMaterialDefaults());
        iron = new VanillaPaxelMaterialCreator(this, builder, new IronPaxelMaterialDefaults());
        diamond = new VanillaPaxelMaterialCreator(this, builder, new DiamondPaxelMaterialDefaults());
        gold = new VanillaPaxelMaterialCreator(this, builder, new GoldPaxelMaterialDefaults());
        netherite = new VanillaPaxelMaterialCreator(this, builder, new NetheritePaxelMaterialDefaults());

        bronze = new MaterialCreator(this, builder, new BronzeMaterialDefaults());
        lapisLazuli = new MaterialCreator(this, builder, new LapisLazuliMaterialDefaults());
        osmium = new MaterialCreator(this, builder, new OsmiumMaterialDefaults());
        refinedGlowstone = new MaterialCreator(this, builder, new RefinedGlowstoneMaterialDefaults());
        refinedObsidian = new MaterialCreator(this, builder, new RefinedObsidianMaterialDefaults());
        steel = new MaterialCreator(this, builder, new SteelMaterialDefaults());
        builder.pop();
        configSpec = builder.build();
    }

    @Override
    public String getFileName() {
        return "tools-materials-startup";
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