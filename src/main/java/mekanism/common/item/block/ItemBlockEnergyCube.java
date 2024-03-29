package mekanism.common.item.block;

import java.util.List;
import java.util.function.Consumer;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.text.EnumColor;
import mekanism.client.render.RenderPropertiesProvider;
import mekanism.common.MekanismLang;
import mekanism.common.block.BlockEnergyCube;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.capabilities.energy.item.EnergyCubeRateLimitEnergyContainer;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.registration.impl.CreativeTabDeferredRegister.ICustomCreativeTabContents;
import mekanism.common.registries.MekanismAttachmentTypes;
import mekanism.common.tier.EnergyCubeTier;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.IPersistentConfigInfo;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemBlockEnergyCube extends ItemBlockTooltip<BlockEnergyCube> implements ICustomCreativeTabContents {

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
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        StorageUtils.addStoredEnergy(stack, tooltip, true);
        tooltip.add(MekanismLang.CAPACITY.translateColored(EnumColor.INDIGO, EnumColor.GRAY, EnergyDisplay.of(getTier().getMaxEnergy())));
        super.appendHoverText(stack, world, tooltip, flag);
    }

    @Override
    protected void addTypeDetails(@NotNull ItemStack stack, @Nullable Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        //Don't call super so that we can exclude the stored energy from being shown as we show it in hover text
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        //If we are currently stacked, don't display the bar as it will overlap the stack count
        return stack.getCount() == 1;
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
    public void addItems(CreativeModeTab.Output tabOutput) {
        EnergyCubeTier tier = getTier();
        if (tier == EnergyCubeTier.CREATIVE) {
            //Add the empty and charged variants
            tabOutput.accept(withEnergyCubeSideConfig(DataType.INPUT));
            tabOutput.accept(StorageUtils.getFilledEnergyVariant(withEnergyCubeSideConfig(DataType.OUTPUT)));
        } else {
            tabOutput.accept(StorageUtils.getFilledEnergyVariant(new ItemStack(this)));
        }
    }

    @Override
    public boolean addDefault() {
        return getTier() != EnergyCubeTier.CREATIVE;
    }

    private ItemStack withEnergyCubeSideConfig(DataType dataType) {
        ItemStack stack = new ItemStack(this);
        IPersistentConfigInfo config = stack.getData(MekanismAttachmentTypes.SIDE_CONFIG).getConfig(TransmissionType.ENERGY);
        if (config != null) {
            config.setDataType(dataType, EnumUtils.SIDES);
        }
        return stack;
    }

    @Override
    protected IEnergyContainer getDefaultEnergyContainer(ItemStack stack) {
        return EnergyCubeRateLimitEnergyContainer.create(getTier());
    }
}