package mekanism.common.registries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import mekanism.api.text.IHasTranslationKey;
import mekanism.common.Mekanism;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class MekanismDamageTypes {

    private static final Map<ResourceLocation, MekanismDamageType> INTERNAL_DAMAGE_TYPES = new HashMap<>();

    public static Collection<MekanismDamageType> damageTypes() {
        return Collections.unmodifiableCollection(INTERNAL_DAMAGE_TYPES.values());
    }

    public static final Codec<MekanismDamageType> CODEC = ResourceLocation.CODEC.flatXmap(rl -> {
        MekanismDamageType damageType = INTERNAL_DAMAGE_TYPES.get(rl);
        return damageType == null ? DataResult.error(() -> "Expected " + rl + " to represent a Mekanism damage type") : DataResult.success(damageType);
    }, damageType -> DataResult.success(damageType.registryName()));

    public static final MekanismDamageType LASER = new MekanismDamageType("laser", 0.1F);
    public static final MekanismDamageType RADIATION = new MekanismDamageType("radiation");

    public record MekanismDamageType(ResourceKey<DamageType> key, float exhaustion) implements IHasTranslationKey {

        public MekanismDamageType {
            INTERNAL_DAMAGE_TYPES.put(key.location(), this);
        }

        private MekanismDamageType(String name) {
            this(name, 0);
        }

        private MekanismDamageType(String name, float exhaustion) {
            this(ResourceKey.create(Registries.DAMAGE_TYPE, Mekanism.rl(name)), exhaustion);
        }

        public String getMsgId() {
            return registryName().getNamespace() + "." + registryName().getPath();
        }

        public ResourceLocation registryName() {
            return key.location();
        }

        @NotNull
        @Override
        public String getTranslationKey() {
            return "death.attack." + getMsgId();
        }

        public DamageSource source(Level level) {
            return source(level.registryAccess());
        }

        public DamageSource source(RegistryAccess registryAccess) {
            return new DamageSource(holder(registryAccess));
        }

        public DamageSource source(Level level, Vec3 position) {
            return source(level.registryAccess(), position);
        }

        public DamageSource source(RegistryAccess registryAccess, Vec3 position) {
            return new DamageSource(holder(registryAccess), position);
        }

        private Holder<DamageType> holder(RegistryAccess registryAccess) {
            return registryAccess.holderOrThrow(key());
        }
    }
}