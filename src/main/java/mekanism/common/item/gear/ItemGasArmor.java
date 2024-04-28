package mekanism.common.item.gear;

import java.util.List;
import java.util.function.LongSupplier;
import mekanism.api.providers.IGasProvider;
import mekanism.common.attachments.IAttachmentAware;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.attachments.containers.chemical.gas.GasTanksBuilder;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.value.CachedLongValue;
import mekanism.common.item.interfaces.IGasItem;
import mekanism.common.registration.impl.CreativeTabDeferredRegister.ICustomCreativeTabContents;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.StorageUtils;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.bus.api.IEventBus;
import org.jetbrains.annotations.NotNull;

public abstract class ItemGasArmor extends ItemSpecialArmor implements IGasItem, ICustomCreativeTabContents, IAttachmentAware {

    protected ItemGasArmor(Holder<ArmorMaterial> material, ArmorItem.Type armorType, Properties properties) {
        super(material, armorType, properties.rarity(Rarity.RARE).setNoRepair().stacksTo(1));
    }

    protected abstract CachedLongValue getMaxGas();

    protected abstract LongSupplier getFillRate();

    protected abstract IGasProvider getGasType();

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        StorageUtils.addStoredGas(stack, tooltip, true, false);
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
    public void addItems(CreativeModeTab.Output tabOutput) {
        tabOutput.accept(ChemicalUtil.getFilledVariant(new ItemStack(this), getGasType()));
    }

    @Override
    public void attachAttachments(IEventBus eventBus) {
        ContainerType.GAS.addDefaultCreators(eventBus, this, () -> GasTanksBuilder.builder()
              .addInternalStorage(getFillRate(), getMaxGas(), gas -> gas == getGasType().getChemical())
              .build(), MekanismConfig.gear);
    }
}