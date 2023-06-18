package mekanism.common.registries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mekanism.api.text.IHasTranslationKey;
import mekanism.common.Mekanism;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class MekanismDamageTypes {

    private static final List<MekanismDamageType> INTERNAL_DAMAGE_TYPES = new ArrayList<>();
    public static final List<MekanismDamageType> DAMAGE_TYPES = Collections.unmodifiableList(INTERNAL_DAMAGE_TYPES);

    //TODO - 1.20: Do we want exhaustion or anything else for these
    public static final MekanismDamageType LASER = new MekanismDamageType("laser");
    public static final MekanismDamageType RADIATION = new MekanismDamageType("radiation");

    public record MekanismDamageType(ResourceKey<DamageType> key) implements IHasTranslationKey {

        public MekanismDamageType {
            INTERNAL_DAMAGE_TYPES.add(this);
        }

        private MekanismDamageType(String name) {
            this(ResourceKey.create(Registries.DAMAGE_TYPE, Mekanism.rl(name)));
        }

        @NotNull
        @Override
        public String getTranslationKey() {
            return "death.attack." + key.location();
        }

        public DamageSource source(Level level) {
            return source(level.registryAccess());
        }

        public DamageSource source(RegistryAccess registryAccess) {
            return new DamageSource(registryAccess.registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key()));
        }
    }
}