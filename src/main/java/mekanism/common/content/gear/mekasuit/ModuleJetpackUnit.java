package mekanism.common.content.gear.mekasuit;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.gear.ModuleConfigItem;
import mekanism.common.content.gear.ModuleConfigItem.EnumData;
import mekanism.common.item.gear.ItemJetpack.JetpackMode;
import mekanism.common.item.gear.ItemMekaSuitArmor;
import mekanism.common.registries.MekanismGases;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public class ModuleJetpackUnit extends ModuleMekaSuit {

    private ModuleConfigItem<JetpackMode> jetpackMode;

    @Override
    public void init() {
        super.init();
        addConfigItem(jetpackMode = new ModuleConfigItem<JetpackMode>(this, "jetpack_mode", MekanismLang.MODULE_MODE, new EnumData<>(JetpackMode.class).withScale(0.6F), JetpackMode.NORMAL));
    }

    @Override
    public void addHUDStrings(List<ITextComponent> list) {
        list.add(MekanismLang.JETPACK_MODE.translateColored(EnumColor.DARK_GRAY, jetpackMode.get()));
        GasStack stored = GasStack.EMPTY;
        Optional<IGasHandler> capability = MekanismUtils.toOptional(getContainer().getCapability(Capabilities.GAS_HANDLER_CAPABILITY));
        if (capability.isPresent()) {
            IGasHandler gasHandlerItem = capability.get();
            if (gasHandlerItem.getGasTankCount() > 0) {
                stored = ((ItemMekaSuitArmor) getContainer().getItem()).getContainedGas(getContainer(), MekanismGases.HYDROGEN.get());
            }
        }
        list.add(MekanismLang.JETPACK_STORED.translateColored(EnumColor.DARK_GRAY, stored.getAmount()));
    }

    @Override
    public void changeMode(@Nonnull PlayerEntity player, @Nonnull ItemStack stack, int shift, boolean displayChangeMessage) {
        JetpackMode newMode = jetpackMode.get().adjust(shift);
        if (jetpackMode.get() != newMode) {
            jetpackMode.set(newMode, null);
            if (displayChangeMessage) {
                player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM,
                      MekanismLang.JETPACK_MODE_CHANGE.translateColored(EnumColor.GRAY, newMode)));
            }
        }
    }

    public JetpackMode getMode() {
        return jetpackMode.get();
    }
}
