package mekanism.generators.common.network.to_server;

import mekanism.api.functions.TriConsumer;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.WorldUtils;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorCasing;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorLogicAdapter;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorLogicAdapter.FissionReactorLogic;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorBlock;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorController;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorLogicAdapter;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorLogicAdapter.FusionReactorLogic;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * Used for informing the server that an action happened in a GUI
 */
public class PacketGeneratorsGuiInteract implements IMekanismPacket {

    private final GeneratorsGuiInteraction interaction;
    private final BlockPos tilePosition;
    private final double extra;

    public PacketGeneratorsGuiInteract(GeneratorsGuiInteraction interaction, TileEntity tile) {
        this(interaction, tile.getBlockPos());
    }

    public PacketGeneratorsGuiInteract(GeneratorsGuiInteraction interaction, TileEntity tile, double extra) {
        this(interaction, tile.getBlockPos(), extra);
    }

    public PacketGeneratorsGuiInteract(GeneratorsGuiInteraction interaction, BlockPos tilePosition) {
        this(interaction, tilePosition, 0);
    }

    public PacketGeneratorsGuiInteract(GeneratorsGuiInteraction interaction, BlockPos tilePosition, double extra) {
        this.interaction = interaction;
        this.tilePosition = tilePosition;
        this.extra = extra;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        PlayerEntity player = context.getSender();
        if (player != null) {
            TileEntityMekanism tile = WorldUtils.getTileEntity(TileEntityMekanism.class, player.level, tilePosition);
            if (tile != null) {
                interaction.consume(tile, player, extra);
            }
        }
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeEnum(interaction);
        buffer.writeBlockPos(tilePosition);
        buffer.writeDouble(extra);
    }

    public static PacketGeneratorsGuiInteract decode(PacketBuffer buffer) {
        return new PacketGeneratorsGuiInteract(buffer.readEnum(GeneratorsGuiInteraction.class), buffer.readBlockPos(), buffer.readDouble());
    }

    public enum GeneratorsGuiInteraction {
        INJECTION_RATE((tile, player, extra) -> {
            if (tile instanceof TileEntityFusionReactorBlock) {
                ((TileEntityFusionReactorController) tile).setInjectionRateFromPacket((int) Math.round(extra));
            } else if (tile instanceof TileEntityFissionReactorCasing) {
                ((TileEntityFissionReactorCasing) tile).setRateLimitFromPacket(extra);
            }
        }),
        LOGIC_TYPE((tile, player, extra) -> {
            if (tile instanceof TileEntityFissionReactorLogicAdapter) {
                ((TileEntityFissionReactorLogicAdapter) tile).setLogicTypeFromPacket(FissionReactorLogic.byIndexStatic((int) Math.round(extra)));
            } else if (tile instanceof TileEntityFusionReactorLogicAdapter) {
                ((TileEntityFusionReactorLogicAdapter) tile).setLogicTypeFromPacket(FusionReactorLogic.byIndexStatic((int) Math.round(extra)));
            }
        }),
        FISSION_ACTIVE((tile, player, extra) -> {
            if (tile instanceof TileEntityFissionReactorCasing) {
                ((TileEntityFissionReactorCasing) tile).setReactorActive(Math.round(extra) == 1);
            }
        });

        private final TriConsumer<TileEntityMekanism, PlayerEntity, Double> consumerForTile;

        GeneratorsGuiInteraction(TriConsumer<TileEntityMekanism, PlayerEntity, Double> consumerForTile) {
            this.consumerForTile = consumerForTile;
        }

        public void consume(TileEntityMekanism tile, PlayerEntity player, double extra) {
            consumerForTile.accept(tile, player, extra);
        }
    }
}