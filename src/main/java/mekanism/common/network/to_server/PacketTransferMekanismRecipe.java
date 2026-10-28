package mekanism.common.network.to_server;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.lib.recipetransfer.PlaceMekanismRecipe;
import mekanism.common.lib.recipetransfer.RecipePlacementHelper;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.recipe.lookup.IRecipeLookupHandler.RecipeContainers;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.PlayerInventoryWrapper;
import net.neoforged.neoforge.transfer.resource.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public record PacketTransferMekanismRecipe(Int2ObjectMap<ResourceStack<ItemResource>> resultState) implements IMekanismPacket {

    public static final Type<PacketTransferMekanismRecipe> TYPE = new Type<>(Mekanism.rl("transfer_recipe"));
    private static final StreamCodec<RegistryFriendlyByteBuf, Int2ObjectMap<ResourceStack<ItemResource>>> ITEMS_TRANSFERRED_CODEC = ByteBufCodecs.map(
          Int2ObjectOpenHashMap::new, ByteBufCodecs.VAR_INT, StreamCodec.composite(
                ItemResource.STREAM_CODEC, ResourceStack::resource,
                ByteBufCodecs.VAR_INT, ResourceStack::amount,
                ResourceStack::new
          ));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketTransferMekanismRecipe> STREAM_CODEC = StreamCodec.composite(
          ITEMS_TRANSFERRED_CODEC, PacketTransferMekanismRecipe::resultState,
          PacketTransferMekanismRecipe::new
    );

    @Override
    public Type<PacketTransferMekanismRecipe> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        Player player = context.player();
        if (player.containerMenu instanceof MekanismContainer container) {
            RecipeContainers recipeContainers = container.getRecipeContainers();
            if (recipeContainers == null) {
                Mekanism.logger.warn("Received transfer request from: {}, but the container did not have any recipe containers.", player);
            } else {
                try (Transaction transaction = Transaction.openRoot()) {
                    if (PlaceMekanismRecipe.tryTransfer(new PacketRecipePlacementHelper(player), recipeContainers.inputs(), resultState, transaction)) {
                        transaction.commit();
                    }
                }
            }
        }
    }

    private record PacketRecipePlacementHelper(ResourceHandler<ItemResource> mainSlots) implements RecipePlacementHelper<Boolean> {

        private PacketRecipePlacementHelper(Player player) {
            this(PlayerInventoryWrapper.of(player).getMainSlots());
        }

        @Override
        public ResourceHandler<ItemResource> itemHandler() {
            return mainSlots;
        }

        @Override
        public Boolean success() {
            return true;
        }

        @Override
        public Boolean error() {
            return false;
        }

        @Override
        public boolean useMaxItems() {
            return false;
        }
    }
}
