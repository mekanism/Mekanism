package mekanism.common.registries;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import mekanism.api.MekanismAPI;
import mekanism.api.robit.AdvancementBasedRobitSkin;
import mekanism.api.robit.RobitSkin;
import mekanism.api.robit.RobitSkinSerializationHelper;
import mekanism.common.Mekanism;
import mekanism.common.entity.RobitPrideSkinData;
import mekanism.common.registration.DatapackDeferredRegister;
import mekanism.common.registration.DeferredMapCodecHolder;
import mekanism.common.util.EnumUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jspecify.annotations.Nullable;

public class MekanismRobitSkins {

    private MekanismRobitSkins() {
    }

    private static final DatapackDeferredRegister<RobitSkin> ROBIT_SKINS = DatapackDeferredRegister.robitSkins(Mekanism.MODID);

    public static final DeferredMapCodecHolder<RobitSkin, RobitSkin> BASIC_SERIALIZER = ROBIT_SKINS.registerCodec("basic", () -> RobitSkinSerializationHelper.NETWORK_CODEC);
    public static final DeferredMapCodecHolder<RobitSkin, AdvancementBasedRobitSkin> ADVANCEMENT_BASED_SERIALIZER = ROBIT_SKINS.registerCodec("advancement_based", () -> RobitSkinSerializationHelper.ADVANCEMENT_BASED_ROBIT_SKIN_CODEC);

    public static final ResourceKey<RobitSkin> BASE = ROBIT_SKINS.dataKey("robit");
    public static final Identifier BASE_SKIN_TEXTURE = Mekanism.rl("robit");
    public static final ResourceKey<RobitSkin> ALLAY = ROBIT_SKINS.dataKey("allay");

    public static final Holder<RobitSkin> BASE_HOLDER = DeferredHolder.create(BASE);

    public static final Map<RobitPrideSkinData, ResourceKey<RobitSkin>> PRIDE_SKINS = Util.make(() -> {
        Map<RobitPrideSkinData, ResourceKey<RobitSkin>> internal = new EnumMap<>(RobitPrideSkinData.class);
        for (RobitPrideSkinData data : EnumUtils.PRIDE_SKINS) {
            internal.put(data, ROBIT_SKINS.dataKey(data.lowerCaseName()));
        }
        return Collections.unmodifiableMap(internal);
    });

    public static void createAndRegisterDatapack(IEventBus modEventBus) {
        ROBIT_SKINS.createAndRegisterDatapack(modEventBus, RobitSkinSerializationHelper.DIRECT_CODEC, RobitSkinSerializationHelper.NETWORK_CODEC.codec(),
              registryBuilder -> registryBuilder.defaultKey(BASE));
    }

    public static SkinLookup lookup(RegistryAccess registryAccess, ResourceKey<RobitSkin> key) {
        Registry<RobitSkin> robitSkins = getSkinRegistry(registryAccess);
        Optional<Holder.Reference<RobitSkin>> skin = robitSkins.get(key);
        //noinspection OptionalIsPresent - Capturing lambda
        if (skin.isEmpty()) {
            return new SkinLookup(BASE, robitSkins.getOrThrow(BASE));
        }
        return new SkinLookup(key, skin.get());
    }

    private static Registry<RobitSkin> getSkinRegistry(RegistryAccess registryAccess) {
        return registryAccess.lookupOrThrow(MekanismAPI.ROBIT_SKIN_REGISTRY_NAME);
    }

    public static RobitSkin get(RegistryAccess registryAccess, ResourceKey<RobitSkin> key) {
        Registry<RobitSkin> skinRegistry = getSkinRegistry(registryAccess);
        RobitSkin value = skinRegistry.getValue(key);
        return value == null ? BASE_HOLDER.value() : value;
    }

    public record SkinLookup(ResourceKey<RobitSkin> name, Holder.Reference<RobitSkin> skinHolder) {

        public Identifier identifier() {
            return name.identifier();
        }

        //TODO - 26.1: Re-evaluate exposing this method
        public RobitSkin skin() {
            return skinHolder.value();
        }

        @Nullable
        public Identifier customModel() {
            return skin().customModel();
        }

        public List<Identifier> textures() {
            return skin().textures();
        }
    }
}