package mekanism.common.content.gear.mekatool;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.config.IModuleConfigItem;
import mekanism.api.gear.config.ModuleConfigItemCreator;
import mekanism.api.gear.config.ModuleEnumData;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.block.BlockBounding;
import mekanism.common.config.MekanismConfig;
import mekanism.common.network.to_client.PacketLightningRender;
import mekanism.common.network.to_client.PacketLightningRender.LightningPreset;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

@ParametersAreNonnullByDefault
public class ModuleVeinMiningUnit implements ICustomModule<ModuleVeinMiningUnit> {

    private IModuleConfigItem<Boolean> extendedMode;
    private IModuleConfigItem<ExcavationRange> excavationRange;

    @Override
    public void init(IModule<ModuleVeinMiningUnit> module, ModuleConfigItemCreator configItemCreator) {
        extendedMode = configItemCreator.createDisableableConfigItem("extended_mode", MekanismLang.MODULE_EXTENDED_MODE, false, MekanismConfig.gear.mekaToolExtendedMining);
        excavationRange = configItemCreator.createConfigItem("excavation_range", MekanismLang.MODULE_EXCAVATION_RANGE,
              new ModuleEnumData<>(ExcavationRange.class, module.getInstalledCount() + 1, ExcavationRange.LOW));
    }

    public boolean isExtended() {
        return extendedMode.get();
    }

    public int getExcavationRange() {
        return excavationRange.get().getRange();
    }

    public static boolean canVeinBlock(BlockState state) {
        //Even though we now handle breaking bounding blocks properly, don't allow vein mining them
        return !(state.getBlock() instanceof BlockBounding);
    }

    public static Object2IntMap<BlockPos> findPositions(Level world, Map<BlockPos, BlockState> initial, int extendedRange, Object2BooleanMap<Block> oreTracker) {
        Object2IntMap<BlockPos> found = new Object2IntLinkedOpenHashMap<>();

        int maxVein = MekanismConfig.gear.disassemblerMiningCount.get();
        int maxCount = initial.size() + maxVein * oreTracker.size();

        Map<BlockPos, BlockState> frontier = new LinkedHashMap<>(initial);
        TraversalDistance dist = new TraversalDistance(frontier.size());
        while (!frontier.isEmpty()) {
            Iterator<Entry<BlockPos, BlockState>> iterator = frontier.entrySet().iterator();
            Entry<BlockPos, BlockState> blockEntry = iterator.next();
            iterator.remove();

            BlockPos blockPos = blockEntry.getKey();
            found.put(blockPos, dist.getDistance());
            if (found.size() >= maxCount) {
                break;
            }

            Block block = blockEntry.getValue().getBlock();
            boolean isOre = oreTracker.getBoolean(block);
            //If it is extended or should be treated as an ore
            if (isOre || extendedRange > dist.getDistance()) {
                for (BlockPos nextPos : BlockPos.betweenClosed(blockPos.offset(-1, -1, -1), blockPos.offset(1, 1, 1))) {
                    //We can check contains as mutable
                    if (!found.containsKey(nextPos) && !frontier.containsKey(nextPos)) {
                        Optional<BlockState> nextState = WorldUtils.getBlockState(world, nextPos);
                        if (nextState.isPresent() && nextState.get().is(block)) {
                            //Make sure to add it as immutable
                            frontier.put(nextPos.immutable(), nextState.get());
                            //Note: We do this for all blocks we find/attempt to mine, not just ones we do mine, as it is a bit simpler
                            // and also represents those blocks getting checked by the vein mining for potentially being able to be mined
                            Mekanism.packetHandler().sendToAllTracking(new PacketLightningRender(LightningPreset.TOOL_AOE, Objects.hash(blockPos, nextPos),
                                  Vec3.atCenterOf(blockPos), Vec3.atCenterOf(nextPos), 10), world, blockPos);
                        }
                    }
                }
            }
            dist.updateDistance(found.size(), frontier.size());
        }
        return found;
    }

    @Override
    public void addHUDStrings(IModule<ModuleVeinMiningUnit> module, Player player, Consumer<Component> hudStringAdder) {
        //Only add hud string for extended vein mining if enabled in config
        if (module.isEnabled() && MekanismConfig.gear.mekaToolExtendedMining.get()) {
            hudStringAdder.accept(MekanismLang.MODULE_EXTENDED_ENABLED.translateColored(EnumColor.DARK_GRAY,
                  isExtended() ? EnumColor.BRIGHT_GREEN : EnumColor.DARK_RED,
                  isExtended() ? MekanismLang.MODULE_ENABLED_LOWER : MekanismLang.MODULE_DISABLED_LOWER));
        }
    }

    public enum ExcavationRange implements IHasTextComponent {
        OFF(0),
        LOW(2),
        MED(4),
        HIGH(6),
        EXTREME(8);

        private final int range;
        private final Component label;

        ExcavationRange(int range) {
            this.range = range;
            this.label = TextComponentUtil.getString(Integer.toString(range));
        }

        @Override
        public Component getTextComponent() {
            return label;
        }

        public int getRange() {
            return range;
        }
    }

    // Helper class to help calculate the breadth first traversal path distance
    private static class TraversalDistance {

        // Start at distance 0
        private int distance = 0;
        private int next;

        // Initialize with the number of elements at distance 0
        public TraversalDistance(int next) {
            this.next = next;
        }

        // When all elements at distance 0 are found, determine how many elements there are with distance 1 and increment distance
        public void updateDistance(int found, int frontierSize) {
            if (found == next) {
                distance++;
                next += frontierSize;
            }
        }

        public int getDistance() {
            return distance;
        }
    }
}
