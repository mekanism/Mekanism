package mekanism.common.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import mekanism.api.Coord4D;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.lib.radiation.RadiationManager.RadiationScale;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.UnitDisplayUtils.RadiationUnit;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.DimensionArgument;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.ILocationArgument;
import net.minecraft.command.arguments.Vec3Argument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

public class RadiationCommand {

    static ArgumentBuilder<CommandSource, ?> register() {
        return Commands.literal("radiation")
              .requires(cs -> cs.hasPermissionLevel(2))
              .then(Commands.literal("add")
                    .then(Commands.argument("magnitude", DoubleArgumentType.doubleArg(0, 10_000))
                          .executes(ctx -> {
                              //Get position based on source
                              CommandSource source = ctx.getSource();
                              return addRadiation(source, source.getPos(), source.getWorld(), DoubleArgumentType.getDouble(ctx, "magnitude"));
                          }).then(Commands.argument("location", Vec3Argument.vec3())
                                .executes(ctx -> {
                                    //Get position based on passed in value, and the source's world
                                    CommandSource source = ctx.getSource();
                                    return addRadiation(source, Vec3Argument.getLocation(ctx, "location"), source.getWorld(),
                                          DoubleArgumentType.getDouble(ctx, "magnitude"));
                                }).then(Commands.argument("dimension", DimensionArgument.getDimension())
                                      .executes(ctx -> {
                                          //Get position and dimension by passed in values
                                          CommandSource source = ctx.getSource();
                                          return addRadiation(source, Vec3Argument.getLocation(ctx, "location"),
                                                DimensionArgument.getDimensionArgument(ctx, "dimension"), DoubleArgumentType.getDouble(ctx, "magnitude"));
                                      })
                                )
                          )
                    )
              ).then(Commands.literal("get")
                    .executes(ctx -> {
                        //Get position based on source
                        CommandSource source = ctx.getSource();
                        return getRadiationLevel(source, source.getPos(), source.getWorld());
                    }).then(Commands.argument("location", Vec3Argument.vec3())
                          .executes(ctx -> {
                              //Get position based on passed in value, and the source's world
                              CommandSource source = ctx.getSource();
                              return getRadiationLevel(source, Vec3Argument.getLocation(ctx, "location"), source.getWorld());
                          }).then(Commands.argument("dimension", DimensionArgument.getDimension())
                                .executes(ctx -> {
                                    //Get position and dimension by passed in values
                                    return getRadiationLevel(ctx.getSource(), Vec3Argument.getLocation(ctx, "location"),
                                          DimensionArgument.getDimensionArgument(ctx, "dimension"));
                                })
                          )
                    )
              ).then(Commands.literal("heal")
                    .executes(ctx -> {
                        CommandSource source = ctx.getSource();
                        source.asPlayer().getCapability(Capabilities.RADIATION_ENTITY_CAPABILITY).ifPresent(c -> c.set(0));
                        source.sendFeedback(MekanismLang.COMMAND_RADIATION_CLEAR.translateColored(EnumColor.GRAY), true);
                        return 0;
                    }).then(Commands.argument("targets", EntityArgument.players())
                          .executes(ctx -> {
                              CommandSource source = ctx.getSource();
                              for (PlayerEntity player : EntityArgument.getPlayers(ctx, "targets")) {
                                  player.getCapability(Capabilities.RADIATION_ENTITY_CAPABILITY).ifPresent(c -> c.set(0));
                                  source.sendFeedback(MekanismLang.COMMAND_RADIATION_CLEAR_PLAYER.translateColored(EnumColor.GRAY, EnumColor.INDIGO,
                                        player.getDisplayName()), true);
                              }
                              return 0;
                          })
                    )
              ).then(Commands.literal("removeAll")
                    .executes(ctx -> {
                        Mekanism.radiationManager.clearSources();
                        ctx.getSource().sendFeedback(MekanismLang.COMMAND_RADIATION_REMOVE_ALL.translateColored(EnumColor.GRAY), true);
                        return 0;
                    })
              );
    }

    private static int addRadiation(CommandSource source, ILocationArgument location, World world, double magnitude) {
        return addRadiation(source, location.getPosition(source), world, magnitude);
    }

    private static int addRadiation(CommandSource source, Vector3d pos, World world, double magnitude) {
        Coord4D location = new Coord4D(pos.x, pos.y, pos.z, world.getDimensionKey());
        Mekanism.radiationManager.radiate(location, magnitude);
        source.sendFeedback(MekanismLang.COMMAND_RADIATION_ADD.translateColored(EnumColor.GRAY, RadiationScale.getSeverityColor(magnitude),
              UnitDisplayUtils.getDisplayShort(magnitude, RadiationUnit.SVH, 3), EnumColor.INDIGO, getPosition(location.getPos()), EnumColor.INDIGO,
              location.dimension.getLocation()), true);
        return 0;
    }

    private static int getRadiationLevel(CommandSource source, ILocationArgument location, World world) {
        return getRadiationLevel(source, location.getPosition(source), world);
    }

    private static int getRadiationLevel(CommandSource source, Vector3d pos, World world) {
        Coord4D location = new Coord4D(pos.x, pos.y, pos.z, world.getDimensionKey());
        double magnitude = Mekanism.radiationManager.getRadiationLevel(location);
        source.sendFeedback(MekanismLang.COMMAND_RADIATION_GET.translateColored(EnumColor.GRAY, EnumColor.INDIGO, getPosition(location.getPos()), EnumColor.INDIGO,
              location.dimension.getLocation(), RadiationScale.getSeverityColor(magnitude), UnitDisplayUtils.getDisplayShort(magnitude, RadiationUnit.SVH, 3)),
              true);
        return 0;
    }

    private static ITextComponent getPosition(BlockPos pos) {
        return MekanismLang.GENERIC_BLOCK_POS.translate(pos.getX(), pos.getY(), pos.getZ());
    }
}