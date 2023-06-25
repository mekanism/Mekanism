package mekanism.common.registries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mekanism.api.text.IHasTranslationKey;
import mekanism.common.Mekanism;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class MekanismDamageTypes {

    private static final List<MekanismDamageType> INTERNAL_DAMAGE_TYPES = new ArrayList<>();
    public static final List<MekanismDamageType> DAMAGE_TYPES = Collections.unmodifiableList(INTERNAL_DAMAGE_TYPES);

    public static final MekanismDamageType LASER = new MekanismDamageType("laser", 0.1F);
    public static final MekanismDamageType RADIATION = new MekanismDamageType("radiation");

    public record MekanismDamageType(ResourceKey<DamageType> key, float exhaustion) implements IHasTranslationKey {

        public MekanismDamageType {
            INTERNAL_DAMAGE_TYPES.add(this);
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
            return new DamageSource(registryAccess.registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key()));
        }
    }
}