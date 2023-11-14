package mekanism.common.item.gear;

import java.util.List;
import java.util.function.LongSupplier;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.providers.IGasProvider;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.chemical.item.RateLimitGasHandler;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.value.CachedLongValue;
import mekanism.common.item.interfaces.IGasItem;
import mekanism.common.registration.impl.CreativeTabDeferredRegister.ICustomCreativeTabContents;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.StorageUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ItemGasArmor extends ItemSpecialArmor implements IGasItem, ICustomCreativeTabContents {

    protected ItemGasArmor(ArmorMaterial material, ArmorItem.Type armorType, Properties properties) {
        super(material, armorType, properties.rarity(Rarity.RARE).setNoRepair().stacksTo(1));
    }

    protected abstract CachedLongValue getMaxGas();

    protected abstract LongSupplier getFillRate();

    protected abstract IGasProvider getGasType();

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
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
        tabOutput.accept(ChemicalUtil.getFilledVariant(new ItemStack(this), getMaxGas(), getGasType()));
    }

    @Override
    public void attachCapabilities(RegisterCapabilitiesEvent event) {
        super.attachCapabilities(event);
        event.registerItem(Capabilities.GAS_HANDLER.item(), (stack, ctx) -> {
            if (!MekanismConfig.gear.isLoaded()) {//Only expose the capabilities if the required configs are loaded
                return null;
            }
            return RateLimitGasHandler.create(stack, getFillRate(), getMaxGas(), ChemicalTankBuilder.GAS.notExternal, ChemicalTankBuilder.GAS.alwaysTrueBi,
                  gas -> gas == getGasType().getChemical());
        }, this);
    }
}