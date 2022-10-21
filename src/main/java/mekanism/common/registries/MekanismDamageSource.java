package mekanism.common.registries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.text.IHasTranslationKey;
import mekanism.common.Mekanism;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//Note: This isn't an actual registry but should make things a bit cleaner
@NothingNullByDefault
public class MekanismDamageSource extends DamageSource implements IHasTranslationKey {

    private static final List<MekanismDamageSource> INTERNAL_DAMAGE_SOURCES = new ArrayList<>();
    public static final List<MekanismDamageSource> DAMAGE_SOURCES = Collections.unmodifiableList(INTERNAL_DAMAGE_SOURCES);
    public static final MekanismDamageSource LASER = new MekanismDamageSource("laser");
    public static final MekanismDamageSource RADIATION = new MekanismDamageSource("radiation").bypassArmor();

    private final String translationKey;
    @Nullable
    private final Vec3 damageLocation;


    private MekanismDamageSource(String damageType) {
        this(damageType, null);
        INTERNAL_DAMAGE_SOURCES.add(this);
    }

    private MekanismDamageSource(@NotNull String damageType, @Nullable Vec3 damageLocation) {
        super(Mekanism.MODID + "." + damageType);
        this.translationKey = "death.attack." + getMsgId();
        this.damageLocation = damageLocation;
    }

    /**
     * Gets a new instance of this damage source, that is positioned at the given location.
     */
    public MekanismDamageSource fromPosition(@NotNull Vec3 damageLocation) {
        return new MekanismDamageSource(getMsgId(), damageLocation);
    }

    @Override
    public String getTranslationKey() {
        return translationKey;
    }

    @Nullable
    @Override
    public Vec3 getSourcePosition() {
        return damageLocation;
    }

    @Override
    public MekanismDamageSource setProjectile() {
        super.setProjectile();
        return this;
    }

    @Override
    public MekanismDamageSource setExplosion() {
        super.setExplosion();
        return this;
    }

    @Override
    public MekanismDamageSource bypassArmor() {
        super.bypassArmor();
        return this;
    }

    @Override
    public MekanismDamageSource bypassInvul() {
        super.bypassInvul();
        return this;
    }

    @Override
    public MekanismDamageSource bypassMagic() {
        super.bypassMagic();
        return this;
    }

    @Override
    public MekanismDamageSource setIsFire() {
        super.setIsFire();
        return this;
    }

    @Override
    public MekanismDamageSource setScalesWithDifficulty() {
        super.setScalesWithDifficulty();
        return this;
    }

    @Override
    public MekanismDamageSource setMagic() {
        super.setMagic();
        return this;
    }
}