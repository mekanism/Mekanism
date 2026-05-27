package mekanism.generators.common.item;

import java.util.function.Consumer;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.registration.impl.CreativeTabDeferredRegister.ICustomCreativeTabContents;
import mekanism.common.util.ChemicalUtils;
import mekanism.common.util.StorageUtils;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.registries.GeneratorsChemicals;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import org.jetbrains.annotations.NotNull;

public class ItemHohlraum extends Item implements ICustomCreativeTabContents {

    public ItemHohlraum(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    @Deprecated
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> tooltipAdder, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
        ResourceHandler<ChemicalResource> handler = Capabilities.CHEMICAL.getCapability(ItemAccess.forStack(stack));
        if (handler != null && handler.size() > 0) {
            //Validate something didn't go terribly wrong, and we actually do have the tank we expect to have
            ChemicalResource storedChemical = handler.getResource(0);
            if (!storedChemical.isEmpty()) {
                long storedAmount = handler.getAmountAsLong(0);
                tooltipAdder.accept(MekanismLang.STORED.translate(storedChemical, storedAmount));
                if (storedAmount == handler.getCapacityAsLong(0, storedChemical)) {
                    tooltipAdder.accept(GeneratorsLang.READY_FOR_REACTION.translateColored(EnumColor.DARK_GREEN));
                } else {
                    tooltipAdder.accept(GeneratorsLang.INSUFFICIENT_FUEL.translateColored(EnumColor.DARK_RED));
                }
                return;
            }
        }
        tooltipAdder.accept(MekanismLang.NO_CHEMICAL.translate());
        tooltipAdder.accept(GeneratorsLang.INSUFFICIENT_FUEL.translateColored(EnumColor.DARK_RED));
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return StorageUtils.isBarVisible(stack);
    }

    @Override
    public int getBarWidth(@NotNull ItemStack stack) {
        return StorageUtils.getBarWidth(stack);
    }

    @Override
    public int getBarColor(@NotNull ItemStack stack) {
        return ChemicalUtils.getRGBDurabilityForDisplay(ItemAccess.forStack(stack));
    }

    @Override
    public void addItems(Holder<Item> item, Consumer<ItemStack> tabOutput) {
        tabOutput.accept(ChemicalUtils.getFilledVariant(item, GeneratorsChemicals.FUSION_FUEL));
    }
}