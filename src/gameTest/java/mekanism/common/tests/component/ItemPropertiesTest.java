
package mekanism.common.tests.component;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import mekanism.api.RelativeSide;
import mekanism.common.attachments.component.AttachedSideConfig;
import mekanism.common.attachments.component.AttachedSideConfig.LightConfigInfo;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tests.helpers.MekGameTestHelper;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;

@ForEachTest(groups = "component.item")
public class ItemPropertiesTest {

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Validate we don't have any transmission types missing from side configs, and that we don't define a side for ones we are disabled on.")
    public static void validateSideConfigs(final MekGameTestHelper helper) {
        helper.succeedIf(() -> {
            validateSideConfigs(helper, MekanismBlocks.BLOCKS.getPrimaryEntries());
            validateSideConfigs(helper, GeneratorsBlocks.BLOCKS.getPrimaryEntries());
        });
    }

    private static void validateSideConfigs(MekGameTestHelper helper, Collection<DeferredHolder<Block, ? extends Block>> blocks) {
        BlockPos center = helper.absolutePos(new BlockPos(0, 2, 0));
        for (DeferredHolder<Block, ? extends Block> holder : blocks) {
            Block block = holder.get();
            ItemStack stack = new ItemStack(block);
            if (!stack.isEmpty() && block instanceof IHasTileEntity<?> hasTileEntity &&
                //TODO: Would we rather actually place the block?
                hasTileEntity.newBlockEntity(center, block.defaultBlockState()) instanceof TileEntityConfigurableMachine configurable) {
                if (stack.get(MekanismDataComponents.EJECTOR) == null) {
                    helper.fail("Block " + holder.getId() + " is missing a default ejector data component");
                }
                AttachedSideConfig defaultConfig = stack.get(MekanismDataComponents.SIDE_CONFIG);
                if (defaultConfig == null) {
                    helper.fail("Block " + holder.getId() + " is missing a default side config data component");
                } else {
                    TileComponentConfig config = configurable.getConfig();
                    Set<TransmissionType> keys = defaultConfig.configInfo().keySet();
                    Set<TransmissionType> transmissionTypes = EnumSet.copyOf(config.getTransmissions());
                    if (!keys.containsAll(transmissionTypes)) {
                        helper.fail("Block " + holder.getId() + " is missing side configs for the following transmission types: " +
                                    Sets.difference(transmissionTypes, keys).stream().map(TransmissionType::getTransmission).collect(Collectors.joining(", ")));
                    } else if (keys.size() != transmissionTypes.size()) {
                        helper.fail("Block " + holder.getId() + " has side configs for the following transmission types, but the BE doesn't: " +
                                    Sets.difference(keys, transmissionTypes).stream().map(TransmissionType::getTransmission).collect(Collectors.joining(", ")));
                    } else {
                        for (Entry<TransmissionType, LightConfigInfo> entry : defaultConfig.configInfo().entrySet()) {
                            TransmissionType type = entry.getKey();
                            ConfigInfo info = config.getConfig(type);
                            helper.assertTrue(info != null, "Config " + type.getTransmission() + " cannot be null");
                            for (RelativeSide side : entry.getValue().sideConfig().keySet()) {
                                if (!info.isSideEnabled(side)) {
                                    helper.fail("Block " + holder.getId() + " has side config for type: " + type.getTransmission() + " on side: " +
                                                side.name() + ", but the BE has side configs disabled on that side.");
                                }
                            }
                        }
                    }
                }
            } else if (!stack.isEmpty()) {
                if (stack.get(MekanismDataComponents.EJECTOR) != null) {
                    helper.fail("Block " + holder.getId() + " is has a default ejector data component, but the tile doesn't");
                }
                if (stack.get(MekanismDataComponents.SIDE_CONFIG) != null) {
                    helper.fail("Block " + holder.getId() + " is has a default side config data component, but the tile doesn't");
                }
            }
        }
    }
}