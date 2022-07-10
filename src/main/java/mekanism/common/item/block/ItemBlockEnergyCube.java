package mekanism.common.item.block;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import mekanism.api.text.EnumColor;
import mekanism.client.render.RenderPropertiesProvider;
import mekanism.common.MekanismLang;
import mekanism.common.block.BlockEnergyCube;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.capabilities.ItemCapabilityWrapper.ItemCapability;
import mekanism.common.capabilities.energy.item.ItemStackEnergyHandler;
import mekanism.common.capabilities.energy.item.RateLimitEnergyHandler;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.interfaces.IItemSustainedInventory;
import mekanism.common.tier.EnergyCubeTier;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

public class ItemBlockEnergyCube extends ItemBlockTooltip<BlockEnergyCube> implements IItemSustainedInventory {

    public ItemBlockEnergyCube(BlockEnergyCube block) {
        super(block);
    }

    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(RenderPropertiesProvider.energyCube());
    }

    @NotNull
    @Override
    public EnergyCubeTier getTier() {
        return Attribute.getTier(getBlock(), EnergyCubeTier.class);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        StorageUtils.addStoredEnergy(stack, tooltip, true);
        tooltip.add(MekanismLang.CAPACITY.translateColored(EnumColor.INDIGO, EnumColor.GRAY, EnergyDisplay.of(getTier().getMaxEnergy())));
        super.appendHoverText(stack, world, tooltip, flag);
    }

    @Override
    protected void addTypeDetails(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        //Don't call super so that we can exclude the stored energy from being shown as we show it in hover text
    }

    @Override
    public void fillItemCategory(@NotNull CreativeModeTab group, @NotNull NonNullList<ItemStack> items) {
        if (allowedIn(group)) {
            EnergyCubeTier tier = Attribute.getTier(getBlock(), EnergyCubeTier.class);
            ItemStack stack = new ItemStack(this);
            if (tier == EnergyCubeTier.CREATIVE) {
                //TODO: Add side specific NBT configuration here rather than in BlockEnergyCube
            }
            //Add the empty and charged variants
            items.add(stack);
            items.add(StorageUtils.getFilledEnergyVariant(stack.copy(), tier.getMaxEnergy()));
        }
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(@NotNull ItemStack stack) {
        return StorageUtils.getEnergyBarWidth(stack);
    }

    @Override
    public int getBarColor(@NotNull ItemStack stack) {
        return MekanismConfig.client.energyColor.get();
    }

    @Override
    protected void gatherCapabilities(List<ItemCapability> capabilities, ItemStack stack, CompoundTag nbt) {
        super.gatherCapabilities(capabilities, stack, nbt);
        ItemCapability capability = RateLimitEnergyHandler.create(Attribute.getTier(getBlock(), EnergyCubeTier.class));
        int index = IntStream.range(0, capabilities.size()).filter(i -> capabilities.get(i) instanceof ItemStackEnergyHandler).findFirst().orElse(-1);
        if (index != -1) {
            //This is likely always the path that will be taken
            capabilities.set(index, capability);
        } else {
            capabilities.add(capability);
        }
    }
}