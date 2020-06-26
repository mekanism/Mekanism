package mekanism.common.registries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.text.IHasTranslationKey;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.vector.Vector3d;

//Note: This isn't an actual registry but should make things a bit cleaner
@MethodsReturnNonnullByDefault
public class MekanismDamageSource extends DamageSource implements IHasTranslationKey {

    public static final MekanismDamageSource LASER = new MekanismDamageSource("laser");
    public static final MekanismDamageSource RADIATION = new MekanismDamageSource("radiation").setDamageBypassesArmor();

    private final String translationKey;

    private final Vector3d damageLocation;


    public MekanismDamageSource(String damageType) {
        this(damageType, null);
    }

    private MekanismDamageSource(@Nonnull String damageType, @Nullable Vector3d damageLocation) {
        super(damageType);
        this.translationKey = "death.attack." + getDamageType();
        this.damageLocation = damageLocation;
    }

    /**
     * Gets a new instance of this damage source, that is positioned at the given location.
     */
    public MekanismDamageSource fromPosition(@Nonnull Vector3d damageLocation) {
        return new MekanismDamageSource(getDamageType(), damageLocation);
    }

    @Override
    public String getTranslationKey() {
        return translationKey;
    }

    @Nullable
    @Override
    public Vector3d getDamageLocation() {
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
    public MekanismDamageSource setDamageBypassesArmor() {
        super.setDamageBypassesArmor();
        return this;
    }

    @Override
    public MekanismDamageSource setDamageAllowedInCreativeMode() {
        super.setDamageAllowedInCreativeMode();
        return this;
    }

    @Override
    public MekanismDamageSource setDamageIsAbsolute() {
        super.setDamageIsAbsolute();
        return this;
    }

    @Override
    public MekanismDamageSource setFireDamage() {
        super.setFireDamage();
        return this;
    }

    @Override
    public MekanismDamageSource setDifficultyScaled() {
        super.setDifficultyScaled();
        return this;
    }

    @Override
    public MekanismDamageSource setMagicDamage() {
        super.setMagicDamage();
        return this;
    }
}