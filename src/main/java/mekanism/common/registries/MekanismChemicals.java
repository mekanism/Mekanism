package mekanism.common.registries;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalBuilder;
import mekanism.api.chemical.CleanDirtySlurryId;
import mekanism.api.text.EnumColorCollection;
import mekanism.common.ChemicalConstants;
import mekanism.common.Mekanism;
import mekanism.common.registration.impl.ChemicalDeferredRegister;
import mekanism.common.registration.impl.DeferredChemical;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.util.EnumUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;

public class MekanismChemicals {

    private MekanismChemicals() {
    }

    public static final ChemicalDeferredRegister CHEMICALS = new ChemicalDeferredRegister(Mekanism.MODID);

    public static final DeferredChemical<Chemical> EMPTY = CHEMICALS.register(MekanismAPI.EMPTY_CHEMICAL_KEY.identifier().getPath(), () -> new Chemical(ChemicalBuilder.builder()));

    public static final ResourceKey<Chemical> BIO = CHEMICALS.register("bio", Mekanism.rl("mek_chemical/infuse_type/bio"), 0xFF5A4630);
    public static final ResourceKey<Chemical> FUNGI = CHEMICALS.register("fungi", Mekanism.rl("mek_chemical/infuse_type/fungi"), 0xFF74656A);
    public static final ResourceKey<Chemical> TIN = CHEMICALS.registerInfuse("tin", 0xFFCCCCD9);
    public static final ResourceKey<Chemical> GOLD = CHEMICALS.registerInfuse("gold", 0xFFF2CD67);
    public static final ResourceKey<Chemical> REFINED_OBSIDIAN = CHEMICALS.registerInfuse("refined_obsidian", 0xFF7C00ED);
    public static final ResourceKey<Chemical> DIAMOND = CHEMICALS.registerInfuse("diamond", 0xFF6CEDD8);
    public static final ResourceKey<Chemical> REDSTONE = CHEMICALS.registerInfuse("redstone", 0xFFB30505);
    public static final ResourceKey<Chemical> CARBON = CHEMICALS.registerInfuse("carbon", 0xFF2C2C2C);

    public static final ResourceKey<Chemical> HYDROGEN = CHEMICALS.register(ChemicalConstants.HYDROGEN);
    public static final ResourceKey<Chemical> OXYGEN = CHEMICALS.register(ChemicalConstants.OXYGEN);
    public static final ResourceKey<Chemical> STEAM = CHEMICALS.register("steam", () -> new Chemical(ChemicalBuilder.builder(Mekanism.rl("mek_liquid/steam")))).getKey();
    public static final ResourceKey<Chemical> WATER_VAPOR = CHEMICALS.register("water_vapor", () -> new Chemical(ChemicalBuilder.builder(Mekanism.rl("mek_liquid/steam")))).getKey();
    public static final ResourceKey<Chemical> CHLORINE = CHEMICALS.register(ChemicalConstants.CHLORINE);
    public static final ResourceKey<Chemical> SULFUR_DIOXIDE = CHEMICALS.register(ChemicalConstants.SULFUR_DIOXIDE);
    public static final ResourceKey<Chemical> SULFUR_TRIOXIDE = CHEMICALS.register(ChemicalConstants.SULFUR_TRIOXIDE);
    public static final ResourceKey<Chemical> SULFURIC_ACID = CHEMICALS.register(ChemicalConstants.SULFURIC_ACID);
    public static final ResourceKey<Chemical> HYDROGEN_CHLORIDE = CHEMICALS.register(ChemicalConstants.HYDROGEN_CHLORIDE);
    public static final ResourceKey<Chemical> HYDROFLUORIC_ACID = CHEMICALS.register(ChemicalConstants.HYDROFLUORIC_ACID);
    public static final ResourceKey<Chemical> URANIUM_OXIDE = CHEMICALS.register(ChemicalConstants.URANIUM_OXIDE);
    public static final ResourceKey<Chemical> URANIUM_HEXAFLUORIDE = CHEMICALS.register(ChemicalConstants.URANIUM_HEXAFLUORIDE);
    //Internal gases
    public static final ResourceKey<Chemical> ETHENE = CHEMICALS.register(ChemicalConstants.ETHENE);
    public static final ResourceKey<Chemical> SODIUM = CHEMICALS.register(ChemicalConstants.SODIUM);
    public static final ResourceKey<Chemical> SUPERHEATED_SODIUM = CHEMICALS.register(ChemicalConstants.SUPERHEATED_SODIUM);
    public static final ResourceKey<Chemical> BRINE = CHEMICALS.register("brine", 0xFFFEEF9C);
    public static final ResourceKey<Chemical> LITHIUM = CHEMICALS.register(ChemicalConstants.LITHIUM);
    public static final ResourceKey<Chemical> OSMIUM = CHEMICALS.register("osmium", 0xFF52BDCA);
    public static final ResourceKey<Chemical> FISSILE_FUEL = CHEMICALS.register("fissile_fuel", 0xFF2E332F);
    public static final ResourceKey<Chemical> NUCLEAR_WASTE = CHEMICALS.register("nuclear_waste", 0xFF4F412A);
    public static final ResourceKey<Chemical> SPENT_NUCLEAR_WASTE = CHEMICALS.register("spent_nuclear_waste", 0xFF262015);
    public static final ResourceKey<Chemical> PLUTONIUM = CHEMICALS.register("plutonium", 0xFF1F919C);
    public static final ResourceKey<Chemical> POLONIUM = CHEMICALS.register("polonium", 0xFF1B9E7B);
    public static final ResourceKey<Chemical> ANTIMATTER = CHEMICALS.register("antimatter", 0xFFA464B3);

    public static EnumColorCollection<ResourceKey<Chemical>> SIMPLE_PIGMENTS = EnumColorCollection.VALUES
          .map(color -> CHEMICALS.registerPigment(color.getRegistryPrefix(), color.getPackedColor()));
    public static final Map<PrimaryResource, CleanDirtySlurryId> PROCESSED_RESOURCES = Util.make(() -> {
        Map<PrimaryResource, CleanDirtySlurryId> slurries = new EnumMap<>(PrimaryResource.class);
        for (PrimaryResource resource : EnumUtils.PRIMARY_RESOURCES) {
            slurries.put(resource, CHEMICALS.registerSlurry(resource));
        }
        return Collections.unmodifiableMap(slurries);
    });
}