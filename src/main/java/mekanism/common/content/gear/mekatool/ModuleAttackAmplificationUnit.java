package mekanism.common.content.gear.mekatool;

import java.util.function.Consumer;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.config.IModuleConfigItem;
import mekanism.api.gear.config.ModuleConfigItemCreator;
import mekanism.api.gear.config.ModuleEnumData;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.MekanismLang;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;

@ParametersAreNonnullByDefault
public class ModuleAttackAmplificationUnit implements ICustomModule<ModuleAttackAmplificationUnit> {

    private IModuleConfigItem<AttackDamage> attackDamage;

    @Override
    public void init(IModule<ModuleAttackAmplificationUnit> module, ModuleConfigItemCreator configItemCreator) {
        attackDamage = configItemCreator.createConfigItem("attack_damage", MekanismLang.MODULE_ATTACK_DAMAGE,
              new ModuleEnumData<>(AttackDamage.class, module.getInstalledCount() + 2, AttackDamage.MED));
    }

    public int getDamage() {
        return attackDamage.get().getDamage();
    }

    @Override
    public void addHUDStrings(IModule<ModuleAttackAmplificationUnit> module, PlayerEntity player, Consumer<ITextComponent> hudStringAdder) {
        if (module.isEnabled()) {
            hudStringAdder.accept(MekanismLang.MODULE_DAMAGE.translateColored(EnumColor.DARK_GRAY, EnumColor.INDIGO, attackDamage.get().getDamage()));
        }
    }

    public enum AttackDamage implements IHasTextComponent {
        OFF(0),
        LOW(4),
        MED(8),
        HIGH(16),
        EXTREME(24),
        MAX(32);

        private final int damage;
        private final ITextComponent label;

        AttackDamage(int damage) {
            this.damage = damage;
            this.label = TextComponentUtil.getString(Integer.toString(damage));
        }

        @Override
        public ITextComponent getTextComponent() {
            return label;
        }

        public int getDamage() {
            return damage;
        }
    }
}