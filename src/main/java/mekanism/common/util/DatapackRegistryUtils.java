package mekanism.common.util;

import mekanism.api.chemical.BasicChemical;
import mekanism.api.chemical.Chemical;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.base.IChemicalConstant;
import mekanism.common.registries.MekanismTrimMaterials;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootPredicates;
import net.minecraft.world.level.storage.loot.providers.number.ints.ConditionalValue;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.StructureModifier;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class DatapackRegistryUtils {

    private DatapackRegistryUtils() {
    }

    public static void registerTrimMaterial(BootstrapContext<TrimMaterial> context, ResourceKey<TrimMaterial> registryKey, int hoverTextColor) {
        Identifier id = registryKey.identifier();
        context.register(registryKey, new TrimMaterial(
              id.withPrefix("trim/" + id.getNamespace() + "_"),
              TextComponentUtil.build(TextColor.fromRgb(hoverTextColor), TextComponentUtil.translate(MekanismTrimMaterials.getTranslationKey(registryKey)))
        ));
    }

    public static ResourceKey<BiomeModifier> biomeModifier(Identifier name) {
        return ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, name);
    }

    public static ResourceKey<StructureModifier> structureModifier(Identifier name) {
        return ResourceKey.create(NeoForgeRegistries.Keys.STRUCTURE_MODIFIERS, name);
    }

    public static void registerConstant(BootstrapContext<Chemical> context, IChemicalConstant constant) {
        context.register(constant.key(), BasicChemical.builder().tint(constant.getColor()).lightLevel(constant.getLightLevel()).build());
    }

    public static ContextIntProvider cooking(HolderGetter<LootItemCondition> predicates, Holder.Reference<ContextIntProvider> normalBurnTimeDivisor,
          Holder.Reference<ContextIntProvider> fastBurnTimeDivisor, int timeSeconds) {
        Holder<LootItemCondition> fasterCookingBlocks = predicates.getOrThrow(LootPredicates.FAST_FURNACE);
        ContextIntProvider fastConditional = new ConditionalValue(fasterCookingBlocks, fastBurnTimeDivisor, normalBurnTimeDivisor);
        return ContextIntProviders.div(ContextIntProviders.exactly(timeSeconds), Holder.direct(fastConditional)).value();
    }
}