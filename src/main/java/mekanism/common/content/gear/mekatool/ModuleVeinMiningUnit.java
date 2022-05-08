package mekanism.common.content.gear.mekatool;

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
import mekanism.common.config.MekanismConfig;
import mekanism.common.network.to_client.PacketLightningRender;
import mekanism.common.network.to_client.PacketLightningRender.LightningPreset;
import mekanism.common.tags.MekanismTags;
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

    public static Map<BlockPos, BlockState> findPositions(Level world, Map<BlockPos, BlockState> initial, boolean extended, int maxRange) {
        int maxVein = MekanismConfig.gear.disassemblerMiningCount.get();
        int maxCount = (int) (initial.size() + maxVein * initial.values().stream().map(bs->bs.getBlock()).distinct().count());
        Map<BlockPos, BlockState> found = new LinkedHashMap<>();
        Map<BlockPos, BlockState> openSet = new LinkedHashMap<>();
        openSet.putAll(initial);
        for (int dist = 0, next = openSet.size(); !openSet.isEmpty(); next += (found.size() == next && ++dist > 0) ? openSet.size() : 0) {
            Entry<BlockPos, BlockState> blockEntry = openSet.entrySet().iterator().next();
            BlockPos blockPos = blockEntry.getKey();
            BlockState blockState = blockEntry.getValue();
            found.put(blockPos, blockState);
            openSet.remove(blockPos);
            if (found.size() > maxCount) {
                break;
            }
            boolean isOre = blockState.is(MekanismTags.Blocks.ATOMIC_DISASSEMBLER_ORE);
            //If it is extended or should be treated as an ore
            if (isOre || extended) {
                Block block = blockState.getBlock();
                for (BlockPos nextPos : BlockPos.betweenClosed(blockPos.offset(-1, -1, -1), blockPos.offset(1, 1, 1))) {
                    //We can check contains as mutable
                    if (!found.containsKey(nextPos) && (isOre || dist < maxRange)) {
                        Optional<BlockState> nextState = WorldUtils.getBlockState(world, nextPos);
                        if (nextState.isPresent() && block == nextState.get().getBlock()) {
                            //Make sure to add it as immutable
                            openSet.put(nextPos.immutable(), nextState.get());
                            //Note: We do this for all blocks we find/attempt to mine, not just ones we do mine, as it is a bit simpler
                            // and also represents those blocks getting checked by the vein mining for potentially being able to be mined
                            Mekanism.packetHandler().sendToAllTracking(new PacketLightningRender(LightningPreset.TOOL_AOE, Objects.hash(blockPos, nextPos),
                                    Vec3.atCenterOf(blockPos), Vec3.atCenterOf(nextPos), 10), world, blockPos);
                        }
                    }
                }
            }
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
}
