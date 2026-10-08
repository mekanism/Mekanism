package mekanism.common.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Optional;
import mekanism.api.MekanismAPITags;
import mekanism.api.MekanismRegistries;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.gear.ModuleData;
import mekanism.common.MekanismLang;
import mekanism.common.base.MekanismPermissions;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.content.gear.ModuleContainer;
import mekanism.common.content.gear.ModuleHelper;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet.Named;
import net.minecraft.server.commands.CommandResponseTracker;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class GiveGearCommand {

    private static final SimpleCommandExceptionType NO_MODULE_CONTAINERS = new SimpleCommandExceptionType(MekanismLang.COMMAND_ERROR_GEAR_NO_CONTAINERS.translate());
    private static final CommandResponseTracker.Messages<ServerPlayer> RESPONSE = CommandResponseTracker.messages(
          (player, _) -> MekanismLang.COMMAND_GIVE_GEAR_SUCCESS_SINGLE.translate(player.getDisplayName()),
          (playerCount, _) -> MekanismLang.COMMAND_GIVE_GEAR_SUCCESS_MULTIPLE.translate(playerCount)
    );

    static ArgumentBuilder<CommandSourceStack, ?> register(CommandBuildContext context) {
        return Commands.literal("givegear")
              .requires(MekanismPermissions.COMMAND_GIVE_GEAR)
              .executes(ctx -> {
                  CommandSourceStack source = ctx.getSource();
                  ServerPlayer player = source.getPlayerOrException();
                  giveGear(player, createPrototypes(context));
                  source.sendSuccess(() -> MekanismLang.COMMAND_GIVE_GEAR_SUCCESS_SINGLE.translate(player.getDisplayName()), true);
                  return Command.SINGLE_SUCCESS;
              }).then(Commands.argument("targets", EntityArgument.players())
                    .requires(MekanismPermissions.COMMAND_GIVE_GEAR_OTHERS)
                    .executes(ctx -> {
                        CommandSourceStack source = ctx.getSource();
                        CommandResponseTracker<ServerPlayer> tracker = CommandResponseTracker.create();
                        ItemStack[] prototypes = createPrototypes(context);
                        for (ServerPlayer player : EntityArgument.getPlayers(ctx, "targets")) {
                            giveGear(player, prototypes);
                            tracker.track(player);
                        }
                        return tracker.sendFeedback(source, true, RESPONSE);
                    })
              );
    }

    private static ItemStack[] createPrototypes(CommandBuildContext context) throws CommandSyntaxException {
        Optional<Named<Item>> holders = context.get(MekanismAPITags.Items.MODULE_CONTAINERS);
        if (holders.isEmpty()) {
            throw NO_MODULE_CONTAINERS.create();
        }
        Named<Item> tag = holders.get();
        if (tag.size() == 0) {
            throw NO_MODULE_CONTAINERS.create();
        }
        return tag.stream()
              .map(item -> {
                  ItemStack stack = new ItemStack(item);
                  ModuleContainer container = ModuleHelper.get().getModuleContainer(stack);
                  //Container really shouldn't be null, but let's make sure just in case
                  if (container != null) {
                      ItemAccess itemAccess = ItemAccess.forStack(stack);
                      for (ModuleData<?> moduleData : IModuleHelper.INSTANCE.getSupported(item)) {
                          Holder<ModuleData<?>> moduleHolder = MekanismRegistries.MODULES.wrapAsHolder(moduleData);
                          if (container.canInstall(itemAccess, moduleHolder)) {
                              try (Transaction transaction = Transaction.openRoot()) {
                                  container.addModule(context, itemAccess, moduleHolder, moduleData.getMaxStackSize(), transaction);
                                  transaction.commit();
                              }
                              container = ModuleHelper.get().getModuleContainerUnsafe(stack);
                          }
                      }
                      return ContainerType.ENERGY.getFilledVariant(itemAccess, null);
                  }
                  return stack;
              })
              .toArray(ItemStack[]::new);
    }

    private static void giveGear(ServerPlayer player, ItemStack... prototypeItems) {
        for (ItemStack prototypeItem : prototypeItems) {
            giveItem(player, prototypeItem);
        }
    }

    /// Extracted from [net.minecraft.server.commands.GiveCommand#giveItem]
    private static void giveItem(ServerPlayer player, ItemStack prototypeItemStack) {
        ItemStack copyToDrop = prototypeItemStack.copy();
        boolean added = player.getInventory().add(copyToDrop);
        if (added && copyToDrop.isEmpty()) {
            ItemEntity drop = player.createItemStackToDrop(prototypeItemStack.copy(), false, false);
            if (drop != null) {
                drop.makeFakeItem();
                drop.level().addFreshEntity(drop);
            }
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F,
                  ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
            player.containerMenu.broadcastChanges();
        } else {
            ItemEntity drop = player.createItemStackToDrop(copyToDrop, false, false);
            if (drop != null) {
                drop.setNoPickUpDelay();
                drop.setTarget(player.getUUID());
                drop.level().addFreshEntity(drop);
            }
        }
    }
}