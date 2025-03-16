package mekanism.common.registration.impl;

import javax.annotation.Nullable;
import mekanism.api.text.IHasTranslationKey;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public record MekanismDamageType(ResourceKey<DamageType> key, float exhaustion, DamageEffects effects) implements IHasTranslationKey {

    public DamageType toVanilla() {
        return new DamageType(registryName().toLanguageKey(), exhaustion, effects);
    }

    public ResourceLocation registryName() {
        return key.location();
    }

    @NotNull
    @Override
    public String getTranslationKey() {
        return registryName().toLanguageKey("death.attack");
    }

    public boolean is(DamageSource source) {
        return source.is(key);
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

    public DamageSource source(RegistryAccess registryAccess, @Nullable Entity directEntity, @Nullable Entity causingEntity) {
        return new DamageSource(holder(registryAccess), directEntity, causingEntity);
    }

    private Holder<DamageType> holder(RegistryAccess registryAccess) {
        return registryAccess.holderOrThrow(key());
    }
}