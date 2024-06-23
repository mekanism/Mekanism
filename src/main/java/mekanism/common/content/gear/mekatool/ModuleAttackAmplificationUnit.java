package mekanism.common.content.gear.mekatool;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@ParametersAreNotNullByDefault
public record ModuleAttackAmplificationUnit(AttackDamage attackDamage) implements ICustomModule<ModuleAttackAmplificationUnit> {

    public static final ResourceLocation ATTACK_DAMAGE = Mekanism.rl("bonus_attack_damage");

    public ModuleAttackAmplificationUnit(IModule<ModuleAttackAmplificationUnit> module) {
        this(module.<AttackDamage>getConfigOrThrow(ATTACK_DAMAGE).get());
    }

    public int getDamage() {
        return attackDamage.getDamage();
    }

    @Override
    public void addHUDStrings(IModule<ModuleAttackAmplificationUnit> module, IModuleContainer moduleContainer, ItemStack stack, Player player, Consumer<Component> hudStringAdder) {
        if (module.isEnabled()) {
            hudStringAdder.accept(MekanismLang.MODULE_DAMAGE.translateColored(EnumColor.DARK_GRAY, EnumColor.INDIGO, getDamage()));
        }
    }

    @NothingNullByDefault
    public enum AttackDamage implements IHasTextComponent, StringRepresentable {
        OFF(0),
        LOW(4),
        MED(8),
        HIGH(16),
        EXTREME(24),
        MAX(32);

        public static final Codec<AttackDamage> CODEC = StringRepresentable.fromEnum(AttackDamage::values);
        public static final IntFunction<AttackDamage> BY_ID = ByIdMap.continuous(AttackDamage::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, AttackDamage> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, AttackDamage::ordinal);

        private final String serializedName;
        private final int damage;
        private final Component label;

        AttackDamage(int damage) {
            this.serializedName = name().toLowerCase(Locale.ROOT);
            this.damage = damage;
            this.label = TextComponentUtil.getString(Integer.toString(damage));
        }

        @Override
        public Component getTextComponent() {
            return label;
        }

        public int getDamage() {
            return damage;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }
}