package mekanism.generators.common.item;

import java.util.List;
import java.util.Optional;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.ItemCapabilityWrapper.ItemCapability;
import mekanism.common.capabilities.chemical.item.RateLimitGasHandler;
import mekanism.common.item.CapabilityItem;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.StorageUtils;
import mekanism.generators.common.GeneratorTags;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.registries.GeneratorsGases;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemHohlraum extends CapabilityItem {

    public ItemHohlraum(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        Optional<IGasHandler> capability = stack.getCapability(Capabilities.GAS_HANDLER).resolve();
        if (capability.isPresent()) {
            IGasHandler gasHandlerItem = capability.get();
            if (gasHandlerItem.getTanks() > 0) {
                //Validate something didn't go terribly wrong, and we actually do have the tank we expect to have
                GasStack storedGas = gasHandlerItem.getChemicalInTank(0);
                if (!storedGas.isEmpty()) {
                    tooltip.add(MekanismLang.STORED.translate(storedGas, storedGas.getAmount()));
                    if (storedGas.getAmount() == gasHandlerItem.getTankCapacity(0)) {
                        tooltip.add(GeneratorsLang.READY_FOR_REACTION.translateColored(EnumColor.DARK_GREEN));
                    } else {
                        tooltip.add(GeneratorsLang.INSUFFICIENT_FUEL.translateColored(EnumColor.DARK_RED));
                    }
                    return;
                }
            }
        }
        tooltip.add(MekanismLang.NO_GAS.translate());
        tooltip.add(GeneratorsLang.INSUFFICIENT_FUEL.translateColored(EnumColor.DARK_RED));
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(@NotNull ItemStack stack) {
        return StorageUtils.getBarWidth(stack);
    }

    @Override
    public int getBarColor(@NotNull ItemStack stack) {
        return ChemicalUtil.getRGBDurabilityForDisplay(stack);
    }

    @Override
    public void fillItemCategory(@NotNull CreativeModeTab group, @NotNull NonNullList<ItemStack> items) {
        super.fillItemCategory(group, items);
        if (allowedIn(group)) {
            items.add(ChemicalUtil.getFilledVariant(new ItemStack(this), MekanismGeneratorsConfig.generators.hohlraumMaxGas.get(), GeneratorsGases.FUSION_FUEL));
        }
    }

    @Override
    protected void gatherCapabilities(List<ItemCapability> capabilities, ItemStack stack, CompoundTag nbt) {
        super.gatherCapabilities(capabilities, stack, nbt);
        capabilities.add(RateLimitGasHandler.create(MekanismGeneratorsConfig.generators.hohlraumFillRate, MekanismGeneratorsConfig.generators.hohlraumMaxGas,
              ChemicalTankBuilder.GAS.notExternal, ChemicalTankBuilder.GAS.alwaysTrueBi, GeneratorTags.Gases.FUSION_FUEL_LOOKUP::contains));
    }
}