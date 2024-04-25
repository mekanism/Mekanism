package mekanism.common.content.gear.mekasuit;

import java.util.function.Consumer;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IHUDElement;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.gear.config.IModuleConfigItem;
import mekanism.api.gear.config.ModuleConfigItemCreator;
import mekanism.api.gear.config.ModuleEnumData;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.item.interfaces.IJetpackItem.JetpackMode;
import mekanism.common.registries.MekanismGases;
import mekanism.common.util.StorageUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@ParametersAreNotNullByDefault
public class ModuleJetpackUnit implements ICustomModule<ModuleJetpackUnit> {

    private IModuleConfigItem<JetpackMode> jetpackMode;
    private IModuleConfigItem<ThrustMultiplier> thrustMultiplier;
    private IModuleConfigItem<ThrustMultiplier> hoverThrustMultiplier;

    @Override
    public void init(IModule<ModuleJetpackUnit> module, ModuleConfigItemCreator configItemCreator) {
        jetpackMode = configItemCreator.createConfigItem("jetpack_mode", MekanismLang.MODULE_JETPACK_MODE, new ModuleEnumData<>(JetpackMode.NORMAL));
        thrustMultiplier = configItemCreator.createConfigItem("jetpack_mult", MekanismLang.MODULE_JETPACK_MULT, new ModuleEnumData<>(ThrustMultiplier.NORMAL, module.getInstalledCount() + 1));
        hoverThrustMultiplier = configItemCreator.createConfigItem("jetpack_hover_mult", MekanismLang.MODULE_JETPACK_HOVER_MULT, new ModuleEnumData<>(ThrustMultiplier.NORMAL, module.getInstalledCount() + 1));
    }

    @Override
    public void addHUDElements(IModule<ModuleJetpackUnit> module, Player player, Consumer<IHUDElement> hudElementAdder) {
        if (module.isEnabled()) {
            IGasHandler gasHandler = module.getContainer().getCapabilityFromStack(Capabilities.GAS.item());
            GasStack stored = StorageUtils.getContainedGas(gasHandler, MekanismGases.HYDROGEN);
            long capacity = gasHandler == null ? 0 : gasHandler.getTankCapacity(0);
            double ratio = StorageUtils.getRatio(stored.getAmount(), capacity);
            hudElementAdder.accept(IModuleHelper.INSTANCE.hudElementPercent(jetpackMode.get().getHUDIcon(), ratio));
        }
    }

    @Override
    public void changeMode(IModule<ModuleJetpackUnit> module, Player player, ItemStack stack, int shift, boolean displayChangeMessage) {
        JetpackMode currentMode = getMode();
        JetpackMode newMode = currentMode.adjust(shift);
        if (currentMode != newMode) {
            jetpackMode.set(newMode);
            if (displayChangeMessage) {
                module.displayModeChange(player, MekanismLang.MODULE_JETPACK_MODE.translate(), newMode);
            }
        }
    }

    @Override
    public void onRemoved(IModule<ModuleJetpackUnit> module, boolean last) {
        //Vent the excess hydrogen from the jetpack
        IGasHandler gasHandler = module.getContainer().getCapabilityFromStack(Capabilities.GAS.item());
        if (gasHandler != null) {
            for (int tank = 0, tanks = gasHandler.getTanks(); tank < tanks; tank++) {
                GasStack stored = gasHandler.getChemicalInTank(tank);
                if (!stored.isEmpty()) {
                    long capacity = gasHandler.getTankCapacity(tank);
                    if (stored.getAmount() > capacity) {
                        gasHandler.setChemicalInTank(tank, stored.copyWithAmount(capacity));
                    }
                }
            }
        }
    }

    public JetpackMode getMode() {
        return jetpackMode.get();
    }

    public float getThrustMultiplier() {
        if (getMode() == JetpackMode.HOVER) {
            return hoverThrustMultiplier.get().getMultiplier();
        }
        return thrustMultiplier.get().getMultiplier();
    }

    @NothingNullByDefault
    public enum ThrustMultiplier implements IHasTextComponent {
        HALF(.5f),
        NORMAL(1f),
        FAST(2f),
        FASTER(3f),
        FASTEST(4f);

        private final float mult;
        private final Component label;

        ThrustMultiplier(float mult) {
            this.mult = mult;
            this.label = TextComponentUtil.getString(Float.toString(mult));
        }

        @Override
        public Component getTextComponent() {
            return label;
        }

        public float getMultiplier() {
            return mult;
        }
    }
}
