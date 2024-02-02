package mekanism.common.item.block.machine;

import java.util.List;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeFactoryType;
import mekanism.common.block.prefab.BlockFactoryMachine.BlockFactory;
import mekanism.common.capabilities.chemical.variable.RateLimitGasTank;
import mekanism.common.capabilities.chemical.variable.RateLimitInfusionTank;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.ItemChemical;
import mekanism.common.registration.impl.RecipeTypeRegistryObject;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tile.machine.TileEntityMetallurgicInfuser;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import org.jetbrains.annotations.NotNull;

public class ItemBlockFactory extends ItemBlockMachine {

    public ItemBlockFactory(BlockFactory<?> block) {
        super(block);
    }

    @Override
    public FactoryTier getTier() {
        return Attribute.getTier(getBlock(), FactoryTier.class);
    }

    @Override
    protected void addTypeDetails(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        //Should always be present but validate it just in case
        Attribute.ifPresent(getBlock(), AttributeFactoryType.class, attribute -> tooltip.add(MekanismLang.FACTORY_TYPE.translateColored(EnumColor.INDIGO, EnumColor.GRAY,
              attribute.getFactoryType())));
        super.addTypeDetails(stack, world, tooltip, flag);
    }

    @Override
    public void attachAttachments(IEventBus eventBus) {
        super.attachAttachments(eventBus);
        //Should always be present but validate it just in case
        Attribute.ifPresent(getBlock(), AttributeFactoryType.class, attribute -> {
            int processes = getTier().processes;
            //TODO - 1.20.4: Do we want to rate limit these at all? Also deny extracting, deny insertion or stuff?
            switch (attribute.getFactoryType()) {
                case COMPRESSING, INJECTING, PURIFYING -> {
                    RecipeTypeRegistryObject<?, ItemChemical<Gas, GasStack, ItemStackGasToItemStackRecipe>> recipeType = switch (attribute.getFactoryType()) {
                        case COMPRESSING -> MekanismRecipeType.COMPRESSING;
                        case PURIFYING -> MekanismRecipeType.PURIFYING;
                        case INJECTING -> MekanismRecipeType.INJECTING;
                        default -> throw new IllegalStateException("Unsupported gas factory type");
                    };
                    //Note: We pass null for the event bus to not expose this attachment as a capability
                    ContainerType.GAS.addDefaultContainer(null, this,
                          stack -> RateLimitGasTank.createBasicItem(TileEntityAdvancedElectricMachine.MAX_GAS * processes,
                                ChemicalTankBuilder.GAS.manualOnly, ChemicalTankBuilder.GAS.alwaysTrueBi,
                                gas -> recipeType.getInputCache().containsInputB(null, gas.getStack(1))
                          )
                    );
                }
                //Note: We pass null for the event bus to not expose this attachment as a capability
                case INFUSING -> ContainerType.INFUSION.addDefaultContainer(null, this,
                      stack -> RateLimitInfusionTank.createBasicItem(TileEntityMetallurgicInfuser.MAX_INFUSE * processes,
                            ChemicalTankBuilder.INFUSION.manualOnly, ChemicalTankBuilder.INFUSION.alwaysTrueBi,
                            infuseType -> MekanismRecipeType.METALLURGIC_INFUSING.getInputCache().containsInputB(null, infuseType.getStack(1))
                      )
                );
            }
        });
    }
}