package mekanism.tools.common.registries;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.MekanismDeferredRegister;
import mekanism.common.util.EnumUtils;
import mekanism.tools.common.MekanismTools;
import mekanism.tools.common.config.MekanismToolsConfig;
import mekanism.tools.common.material.BaseMekanismMaterial;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;

public class ToolsArmorMaterials {

    private ToolsArmorMaterials() {
    }

    public static final MekanismDeferredRegister<ArmorMaterial> ARMOR_MATERIALS = new MekanismDeferredRegister<>(Registries.ARMOR_MATERIAL, MekanismTools.MODID);

    public static final MekanismDeferredHolder<ArmorMaterial, ArmorMaterial> BRONZE = ARMOR_MATERIALS.register("bronze", name -> createMaterial(name, MekanismToolsConfig.materials.bronze));
    public static final MekanismDeferredHolder<ArmorMaterial, ArmorMaterial> LAPIS_LAZULI = ARMOR_MATERIALS.register("lapis_lazuli", name -> createMaterial(name, MekanismToolsConfig.materials.lapisLazuli));
    public static final MekanismDeferredHolder<ArmorMaterial, ArmorMaterial> OSMIUM = ARMOR_MATERIALS.register("osmium", name -> createMaterial(name, MekanismToolsConfig.materials.osmium));
    public static final MekanismDeferredHolder<ArmorMaterial, ArmorMaterial> REFINED_GLOWSTONE = ARMOR_MATERIALS.register("refined_glowstone", name -> createMaterial(name, MekanismToolsConfig.materials.refinedGlowstone));
    public static final MekanismDeferredHolder<ArmorMaterial, ArmorMaterial> REFINED_OBSIDIAN = ARMOR_MATERIALS.register("refined_obsidian", name -> createMaterial(name, MekanismToolsConfig.materials.refinedObsidian));
    public static final MekanismDeferredHolder<ArmorMaterial, ArmorMaterial> STEEL = ARMOR_MATERIALS.register("steel", name -> createMaterial(name, MekanismToolsConfig.materials.steel));

    private static ArmorMaterial createMaterial(ResourceLocation name, BaseMekanismMaterial material) {
        EnumMap<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
        for (ArmorItem.Type type : EnumUtils.ARMOR_TYPES) {
            int providedDefense = material.getDefense(type);
            if (providedDefense > 0) {
                defense.put(type, providedDefense);
            }
        }
        return new ArmorMaterial(defense.isEmpty() ? Collections.emptyMap() : defense, material.getEnchantmentValue(), material.equipSound(), material::getRepairIngredient,
              List.of(new ArmorMaterial.Layer(name)), material.toughness(), material.knockbackResistance()
        );
    }
}