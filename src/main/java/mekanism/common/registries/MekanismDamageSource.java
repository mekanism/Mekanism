package mekanism.common.registries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.text.IHasTranslationKey;
import mekanism.common.Mekanism;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.Vec3;

//Note: This isn't an actual registry but should make things a bit cleaner
@MethodsReturnNonnullByDefault
public class MekanismDamageSource extends DamageSource implements IHasTranslationKey {

    public static final MekanismDamageSource LASER = new MekanismDamageSource("laser");
    public static final MekanismDamageSource RADIATION = new MekanismDamageSource("radiation").bypassArmor();

    private final String translationKey;

    private final Vec3 damageLocation;


    public MekanismDamageSource(String damageType) {
        this(damageType, null);
    }

    private MekanismDamageSource(@Nonnull String damageType, @Nullable Vec3 damageLocation) {
        super(Mekanism.MODID + "." + damageType);
        this.translationKey = "death.attack." + getMsgId();
        this.damageLocation = damageLocation;
    }

    /**
     * Gets a new instance of this damage source, that is positioned at the given location.
     */
    public MekanismDamageSource fromPosition(@Nonnull Vec3 damageLocation) {
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