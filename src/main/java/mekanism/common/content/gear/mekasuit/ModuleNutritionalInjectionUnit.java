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
import mekanism.common.util.ItemAccessUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodConstants;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

@ParametersAreNotNullByDefault
public class ModuleNutritionalInjectionUnit implements ICustomModule<ModuleNutritionalInjectionUnit> {

    private static final Identifier icon = MekanismUtils.getResource(ResourceType.GUI_HUD, "nutritional_injection_unit.png");

    @Override
    public void tickServer(IModule<ModuleNutritionalInjectionUnit> module, ItemAccess itemAccess, Player player, TransactionContext transaction) {
        if (MekanismUtils.isPlayingMode(player) && player.canEat(false)) {
            //Check if we can use a single iteration of it
            ResourceHandler<FluidResource> fluidHandler = Capabilities.FLUID.getCapability(itemAccess);
            if (fluidHandler != null) {
                FluidResource paste = MekanismFluids.NUTRITIONAL_PASTE.asResource();
                int missingFood = FoodConstants.MAX_FOOD - player.getFoodData().getFoodLevel();
                int pastePerFood = MekanismConfig.general.nutritionalPasteMBPerFood.get();
                int energyUsage = MekanismConfig.gear.mekaSuitEnergyUsageNutritionalInjection.get();
                int foodToFill;
                try (Transaction simulation = Transaction.open(transaction)) {
                    foodToFill = fluidHandler.extract(paste, missingFood * pastePerFood, simulation) / pastePerFood;
                    //Limit how much food we can handle by the amount of energy stored
                    foodToFill = module.getEnergyRateLimit(player, itemAccess, energyUsage, foodToFill, simulation);
                    if (foodToFill == 0) {
                        return;
                    }
                }
                try (Transaction subTransaction = Transaction.open(transaction)) {
                    int energyToUse = foodToFill * energyUsage;
                    int pasteToUse = foodToFill * pastePerFood;
                    //Note: This if statement should always be true given we already simulated that we could extract at least this much,
                    // but we validate it just in case before actually committing any changes
                    if (fluidHandler.extract(paste, pasteToUse, subTransaction) == pasteToUse && module.useAllEnergy(player, itemAccess, energyToUse, subTransaction)) {
                        player.getFoodData().eat(foodToFill, MekanismConfig.general.nutritionalPasteSaturation.get());
                        subTransaction.commit();
                    }
                }
            }
        }
    }

    @Override
    public <ITEM extends TypedInstance<Item> & DataComponentGetter> void addHUDElements(IModule<ModuleNutritionalInjectionUnit> module, IModuleContainer moduleContainer,
          ITEM instance, Player player, Consumer<IHUDElement> hudElementAdder) {
        if (module.isEnabled()) {
            double ratio = 0;
            ResourceHandler<FluidResource> fluidHandler = Capabilities.FLUID.getCapability(ItemAccessUtils.queryOnlyAccess(instance));
            if (fluidHandler != null) {
                long max = MekanismConfig.gear.mekaSuitNutritionalMaxStorage.getAsLong();
                long stored = 0;
                for (int tank = 0, size = fluidHandler.size(); tank < size; tank++) {
                    if (fluidHandler.getResource(tank).is(MekanismFluids.NUTRITIONAL_PASTE)) {
                        long inTank = fluidHandler.getAmountAsLong(tank);
                        if (stored >= max - inTank) {
                            stored = max;
                            break;
                        }
                        stored += inTank;
                    }
                }
                ratio = MathUtils.divideToLevel(stored, max);
            }
            hudElementAdder.accept(IModuleHelper.INSTANCE.hudElementPercent(icon, ratio));
        }
    }
}
