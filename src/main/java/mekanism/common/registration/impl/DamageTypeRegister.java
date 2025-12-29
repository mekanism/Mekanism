package mekanism.common.registration.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageEffects;

public class DamageTypeRegister {

    private final Map<Identifier, MekanismDamageType> damageTypes = new HashMap<>();
    private final String modid;

    public DamageTypeRegister(String modid) {
        this.modid = modid;
    }

    public MekanismDamageType create(String name) {
        return create(name, 0);
    }

    public MekanismDamageType create(String name, float exhaustion) {
        return create(name, exhaustion, DamageEffects.HURT);
    }

    public MekanismDamageType create(String name, float exhaustion, DamageEffects effects) {
        MekanismDamageType type = new MekanismDamageType(ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(modid, name)), exhaustion, effects);
        damageTypes.put(type.registryName(), type);
        return type;
    }

    public Collection<MekanismDamageType> damageTypes() {
        return Collections.unmodifiableCollection(damageTypes.values());
    }
}