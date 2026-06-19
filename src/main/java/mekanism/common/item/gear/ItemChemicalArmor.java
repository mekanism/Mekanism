package mekanism.common.item.gear;

import java.util.Optional;
import java.util.function.Consumer;
import mekanism.api.chemical.Chemical;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.item.interfaces.IChemicalItem;
import mekanism.common.registration.impl.CreativeTabDeferredRegister.ICustomCreativeTabContents;
import mekanism.common.util.ChemicalUtils;
import mekanism.common.util.ItemAccessUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.neoforged.neoforge.transfer.access.ItemAccess;

public abstract class ItemChemicalArmor extends ItemSpecialArmor implements IChemicalItem, ICustomCreativeTabContents {

    protected ItemChemicalArmor(ArmorMaterial material, ArmorType armorType, Item.Properties properties) {
        super(material, armorType, properties.rarity(Rarity.RARE).setNoCombineRepair().stacksTo(1));
    }

    protected abstract ResourceKey<Chemical> getChemicalType();

    @Override
    public boolean hasChemical(ItemAccess itemAccess) {
        return ChemicalUtils.hasChemicalOfType(itemAccess, getChemicalType());
    }

    @Override
    @Deprecated
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
        StorageUtils.addStoredChemical(ItemAccessUtils.sideEffectFreeAccess(stack), tooltipAdder);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return StorageUtils.isBarVisible(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return StorageUtils.getBarWidth(stack);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return ContainerType.CHEMICAL.getRGBDurabilityForDisplay(stack);
    }

    @Override
    public void addItems(ItemDisplayParameters displayParameters, Holder<Item> item, Consumer<ItemStack> tabOutput) {
        Optional<Reference<Chemical>> chemical = displayParameters.holders().get(getChemicalType());
        //noinspection OptionalIsPresent - Capturing lambda
        if (chemical.isPresent()) {
            tabOutput.accept(ContainerType.CHEMICAL.getFilledVariant(item, chemical.get(), null));
        }
    }
}