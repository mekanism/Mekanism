package mekanism.common.content.gear.mekatool;

import java.util.List;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.common.MekanismLang;
import mekanism.common.content.gear.Module;
import mekanism.common.content.gear.ModuleConfigItem;
import mekanism.common.content.gear.ModuleConfigItem.EnumData;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class ModuleMekaTool extends Module {

    public ActionResultType onItemUse(ItemUseContext context) {
        return ActionResultType.PASS;
    }

    public static class ModuleAttackAmplificationUnit extends ModuleMekaTool {

        private ModuleConfigItem<AttackDamage> attackDamage;

        @Override
        public void init() {
            super.init();
            addConfigItem(attackDamage = new ModuleConfigItem<>(this, "attack_damage", MekanismLang.MODULE_ATTACK_DAMAGE, new EnumData<>(AttackDamage.class, getInstalledCount() + 2), AttackDamage.MED));
        }

        public int getDamage() {
            return attackDamage.get().getDamage();
        }

        @Override
        public void addHUDStrings(List<ITextComponent> list) {
            if (!isEnabled()) {
                return;
            }
            list.add(MekanismLang.MODULE_DAMAGE.translateColored(EnumColor.DARK_GRAY, EnumColor.INDIGO, attackDamage.get().getDamage()));
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
                this.label = new StringTextComponent(Integer.toString(damage));
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

    public static class ModuleSilkTouchUnit extends ModuleMekaTool {}

    public static class ModuleTeleportationUnit extends ModuleMekaTool {}
}
