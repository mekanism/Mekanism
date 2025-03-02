package mekanism.common.content.gear.mekasuit;

import java.util.function.Consumer;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IHUDElement;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.math.MathUtils;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.StorageUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

@ParametersAreNotNullByDefault
public class ModuleNutritionalInjectionUnit implements ICustomModule<ModuleNutritionalInjectionUnit> {

    private static final ResourceLocation icon = MekanismUtils.getResource(ResourceType.GUI_HUD, "nutritional_injection_unit.png");

    @Override
    public void tickServer(IModule<ModuleNutritionalInjectionUnit> module, IModuleContainer moduleContainer, ItemStack stack, Player player) {
        long usage = MekanismConfig.gear.mekaSuitEnergyUsageNutritionalInjection.get();
        if (MekanismUtils.isPlayingMode(player) && player.canEat(false)) {
            //Check if we can use a single iteration of it
            IFluidHandlerItem handler = Capabilities.FLUID.getCapability(stack);
            if (handler != null) {
                int contained = StorageUtils.getContainedFluid(handler, MekanismFluids.NUTRITIONAL_PASTE.asStack(1)).getAmount();
                int needed = Math.min(20 - player.getFoodData().getFoodLevel(), contained / MekanismConfig.general.nutritionalPasteMBPerFood.get());
                int toFeed = Math.min(MathUtils.clampToInt(module.getContainerEnergy(stack) / usage), needed);
                if (toFeed > 0) {
                    module.useEnergy(player, stack, usage * toFeed);
                    handler.drain(MekanismFluids.NUTRITIONAL_PASTE.asStack(toFeed * MekanismConfig.general.nutritionalPasteMBPerFood.get()), FluidAction.EXECUTE);
                    player.getFoodData().eat(needed, MekanismConfig.general.nutritionalPasteSaturation.get());
                }
            }
        }
    }

    @Override
    public void addHUDElements(IModule<ModuleNutritionalInjectionUnit> module, IModuleContainer moduleContainer, ItemStack stack, Player player, Consumer<IHUDElement> hudElementAdder) {
        if (module.isEnabled()) {
            IFluidHandlerItem handler = Capabilities.FLUID.getCapability(stack);
            double ratio = 0;
            if (handler != null) {
                int max = MekanismConfig.gear.mekaSuitNutritionalMaxStorage.getAsInt();
                handler.drain(MekanismFluids.NUTRITIONAL_PASTE.asStack(max), FluidAction.SIMULATE);
                FluidStack stored = StorageUtils.getContainedFluid(handler, MekanismFluids.NUTRITIONAL_PASTE.asStack(1));
                ratio = StorageUtils.getRatio(stored.getAmount(), MekanismConfig.gear.mekaSuitNutritionalMaxStorage.get());
            }
            hudElementAdder.accept(IModuleHelper.INSTANCE.hudElementPercent(icon, ratio));
        }
    }
}
