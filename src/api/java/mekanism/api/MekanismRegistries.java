package mekanism.api;

import com.mojang.serialization.MapCodec;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalSerializer;
import mekanism.api.gear.ModuleData;
import mekanism.api.recipes.ingredients.chemical.ChemicalIngredient;
import mekanism.api.robit.RobitSkin;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

/// A class that exposes static references to Mekanism registries. It is still advised that you register things with [RegisterEvent] or [DeferredRegister], but queries
/// and iterations can use this.
///
/// Vanilla's registries can be found in [BuiltInRegistries], and their keys in [Registries].
///
/// Neo's registries can be found in [net.neoforged.neoforge.registries.NeoForgeRegistries].
///
/// @since 10.8.0
public class MekanismRegistries {

    private MekanismRegistries() {
    }


    /// Gets the Registry for [Chemical] serializers.
    ///
    /// @see Keys#CHEMICAL_SERIALIZERS
    public static final Registry<ChemicalSerializer> CHEMICAL_SERIALIZERS = new RegistryBuilder<>(Keys.CHEMICAL_SERIALIZERS).create();

    /// Gets the Registry for [ChemicalIngredient] type serializers.
    ///
    /// @see Keys#CHEMICAL_INGREDIENT_TYPES
    public static final Registry<MapCodec<? extends ChemicalIngredient>> CHEMICAL_INGREDIENT_TYPES = new RegistryBuilder<>(Keys.CHEMICAL_INGREDIENT_TYPES).sync(true).create();

    /// Gets the Registry for [ModuleData].
    ///
    /// @see Keys#MODULES
    public static final Registry<ModuleData<?>> MODULES = new RegistryBuilder<>(Keys.MODULES).sync(true).create();

    /// Gets the Registry for [RobitSkin] serializers.
    ///
    /// @see Keys#ROBIT_SKIN_SERIALIZERS
    public static final Registry<MapCodec<? extends RobitSkin>> ROBIT_SKIN_SERIALIZERS = new RegistryBuilder<>(Keys.ROBIT_SKIN_SERIALIZERS).create();


    public static final class Keys {

        private Keys() {
        }

        /// Gets the [ResourceKey] representing the name of the Registry for [`chemicals`][Chemical] serializers.
        ///
        /// @apiNote When registering [`chemicals`][Chemical] serializers using [DeferredRegister], use this field to get access to the [ResourceKey].
        public static final ResourceKey<Registry<ChemicalSerializer>> CHEMICAL_SERIALIZERS = key("chemical_serializer");

        /// Gets the [ResourceKey] representing the name of the Registry for [ChemicalIngredient] ingredient type serializers.
        ///
        /// @apiNote When registering chemical ingredient types using [DeferredRegister], use this field to get access to the [ResourceKey].
        public static final ResourceKey<Registry<MapCodec<? extends ChemicalIngredient>>> CHEMICAL_INGREDIENT_TYPES = key("chemical_ingredient_type");

        /// Gets the [ResourceKey] representing the name of the Registry for [`modules`][ModuleData].
        ///
        /// @apiNote When registering [`modules`][ModuleData] using [DeferredRegister], use this field to get access to the [ResourceKey].
        public static final ResourceKey<Registry<ModuleData<?>>> MODULES = key("module");

        /// Gets the [ResourceKey] representing the name of the Registry for [`robit skin`][RobitSkin] serializers.
        ///
        /// @apiNote When registering [`robit skin`][RobitSkin] serializers using [DeferredRegister], use this field to get access to the [ResourceKey].
        public static final ResourceKey<Registry<MapCodec<? extends RobitSkin>>> ROBIT_SKIN_SERIALIZERS = key("robit_skin_serializer");

        //Data pack registry keys
        /// Gets the [ResourceKey] representing the name of the Datapack Registry for [`chemicals`][Chemical].
        public static final ResourceKey<Registry<Chemical>> CHEMICAL = key("chemical");//TODO - 26.2: Docs on how to register chemicals

        /// Gets the [ResourceKey] representing the name of the Datapack Registry for [`robit skins`][RobitSkin].
        public static final ResourceKey<Registry<RobitSkin>> ROBIT_SKINS = key("robit_skin");

        private static <T> ResourceKey<Registry<T>> key(String path) {
            return ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, path));
        }
    }
}