package mekanism.common.item.block;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;
import mekanism.api.RelativeSide;
import mekanism.api.energy.IEnergyContainer;
import mekanism.common.block.BlockEnergyCube;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.component.component.AttachedEjector;
import mekanism.common.component.component.AttachedSideConfig;
import mekanism.common.component.component.AttachedSideConfig.LightConfigInfo;
import mekanism.common.component.containers.creator.IContainerCreator;
import mekanism.common.component.containers.energy.EnergyContainerBuilder;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.registration.impl.CreativeTabDeferredRegister.ICustomCreativeTabContents;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tier.EnergyCubeTier;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.core.Holder;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class ItemBlockEnergyCube extends ItemBlockTooltip<BlockEnergyCube> implements ICustomCreativeTabContents {

    public static final AttachedSideConfig SIDE_CONFIG = sideConfig(LightConfigInfo.FRONT_OUT_EJECT);
    public static final AttachedSideConfig ALL_INPUT = Util.make(() -> {
        Map<RelativeSide, DataType> sideData = new EnumMap<>(RelativeSide.class);
        for (RelativeSide side : EnumUtils.SIDES) {
            sideData.put(side, DataType.INPUT);
        }
        return sideConfig(new LightConfigInfo(sideData, false));
    });
    public static final AttachedSideConfig ALL_OUTPUT = Util.make(() -> {
        Map<RelativeSide, DataType> sideData = new EnumMap<>(RelativeSide.class);
        for (RelativeSide side : EnumUtils.SIDES) {
            sideData.put(side, DataType.OUTPUT);
        }
        return sideConfig(new LightConfigInfo(sideData, true));
    });

    private static AttachedSideConfig sideConfig(LightConfigInfo energyConfig) {
        Map<TransmissionType, LightConfigInfo> configInfo = new EnumMap<>(TransmissionType.class);
        configInfo.put(TransmissionType.ITEM, LightConfigInfo.FRONT_OUT_NO_EJECT);
        configInfo.put(TransmissionType.ENERGY, energyConfig);
        return new AttachedSideConfig(configInfo);
    }

    private final EnergyCubeTier tier;

    public ItemBlockEnergyCube(BlockEnergyCube block, Item.Properties properties) {
        EnergyCubeTier tier = Attribute.getTierNN(block, EnergyCubeTier.class);
        this.tier = tier;
        super(block, properties
              .component(MekanismDataComponents.ENERGY_CUBE_TIER, tier)
              .component(MekanismDataComponents.EJECTOR, AttachedEjector.DEFAULT)
              .component(MekanismDataComponents.SIDE_CONFIG, SIDE_CONFIG)
              .component(MekanismDataComponents.DETAILS, Unit.INSTANCE)
        );
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return StorageUtils.isEnergyBarVisible(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return StorageUtils.getEnergyBarWidth(stack);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return MekanismConfig.client.energyColor.get();
    }

    @Override
    public void addItems(ItemDisplayParameters displayParameters, Holder<Item> item, Consumer<ItemStack> tabOutput) {
        if (addDefault()) {
            tabOutput.accept(ContainerType.ENERGY.getFilledVariant(item, null));
        } else {
            //Add the empty and charged variants
            tabOutput.accept(withCreativeSideConfig(ALL_INPUT).toStack());
            tabOutput.accept(ContainerType.ENERGY.getFilledVariant(withCreativeSideConfig(ALL_OUTPUT), null));
        }
    }

    @Override
    public boolean addDefault() {
        return !tier.isCreative();
    }

    public static ItemResource withCreativeSideConfig(AttachedSideConfig config) {
        return ItemResource.of(MekanismBlocks.CREATIVE_ENERGY_CUBE)
              .with(MekanismDataComponents.SIDE_CONFIG, config);
    }

    @Override
    protected IContainerCreator<IEnergyContainer, Long> getDefaultEnergyContainer() {
        return EnergyContainerBuilder.ENERGY_CUBE;
    }
}