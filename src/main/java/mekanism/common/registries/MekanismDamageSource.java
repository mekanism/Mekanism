package mekanism.common.registries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.text.IHasTranslationKey;
import mekanism.common.Mekanism;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.vector.Vector3d;

//Note: This isn't an actual registry but should make things a bit cleaner
@MethodsReturnNonnullByDefault
public class MekanismDamageSource extends DamageSource implements IHasTranslationKey {

    public static final MekanismDamageSource LASER = new MekanismDamageSource("laser");
    public static final MekanismDamageSource RADIATION = new MekanismDamageSource("radiation").bypassArmor();

    private final String translationKey;

    private final Vector3d damageLocation;


    public MekanismDamageSource(String damageType) {
        this(damageType, null);
    }

    private MekanismDamageSource(@Nonnull String damageType, @Nullable Vector3d damageLocation) {
        super(Mekanism.MODID + "." + damageType);
        this.translationKey = "death.attack." + getMsgId();
        this.damageLocation = damageLocation;
    }

    /**
     * Gets a new instance of this damage source, that is positioned at the given location.
     */
    public MekanismDamageSource fromPosition(@Nonnull Vector3d damageLocation) {
        return new MekanismDamageSource(getMsgId(), damageLocation);
    }

    @Override
    public String getTranslationKey() {
        return translationKey;
    }

    @Nullable
    @Override
    public Vector3d getSourcePosition() {
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