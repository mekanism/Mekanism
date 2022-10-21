package mekanism.common.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import mekanism.api.Coord4D;
import mekanism.api.MekanismAPI;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.base.MekanismPermissions;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.lib.radiation.RadiationManager.RadiationScale;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.UnitDisplayUtils.RadiationUnit;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class RadiationCommand {

    static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("radiation")
              .requires(MekanismPermissions.COMMAND_RADIATION)
              .then(Commands.literal("add")
                    .requires(MekanismPermissions.COMMAND_RADIATION_ADD)
                    .then(Commands.argument("magnitude", DoubleArgumentType.doubleArg(0, 10_000))
                          .executes(ctx -> {
                              //Get position based on source
                              CommandSourceStack source = ctx.getSource();
                              return addRadiation(source, source.getPosition(), source.getLevel(), DoubleArgumentType.getDouble(ctx, "magnitude"));
                          }).then(Commands.argument("location", Vec3Argument.vec3())
                                .executes(ctx -> {
                                    //Get position based on passed in value, and the source's world
                                    CommandSourceStack source = ctx.getSource();
                                    return addRadiation(source, Vec3Argument.getCoordinates(ctx, "location"), source.getLevel(),
                                          DoubleArgumentType.getDouble(ctx, "magnitude"));
                                }).then(Commands.argument("dimension", DimensionArgument.dimension())
                                      .executes(ctx -> {
                                          //Get position and dimension by passed in values
                                          CommandSourceStack source = ctx.getSource();
                                          return addRadiation(source, Vec3Argument.getCoordinates(ctx, "location"),
                                                DimensionArgument.getDimension(ctx, "dimension"), DoubleArgumentType.getDouble(ctx, "magnitude"));
                                      })
                                )
                          )
                    )
              ).then(Commands.literal("get")
                    .requires(MekanismPermissions.COMMAND_RADIATION_GET)
                    .executes(ctx -> {
                        //Get position based on source
                        CommandSourceStack source = ctx.getSource();
                        return getRadiationLevel(source, source.getPosition(), source.getLevel());
                    }).then(Commands.argument("location", Vec3Argument.vec3())
                          .executes(ctx -> {
                              //Get position based on passed in value, and the source's world
                              CommandSourceStack source = ctx.getSource();
                              return getRadiationLevel(source, Vec3Argument.getCoordinates(ctx, "location"), source.getLevel());
                          }).then(Commands.argument("dimension", DimensionArgument.dimension())
                                .executes(ctx -> {
                                    //Get position and dimension by passed in values
                                    return getRadiationLevel(ctx.getSource(), Vec3Argument.getCoordinates(ctx, "location"),
                                          DimensionArgument.getDimension(ctx, "dimension"));
                                })
                          )
                    )
              ).then(Commands.literal("heal")
                    .requires(MekanismPermissions.COMMAND_RADIATION_HEAL)
                    .executes(ctx -> {
                        CommandSourceStack source = ctx.getSource();
                        source.getPlayerOrException().getCapability(Capabilities.RADIATION_ENTITY).ifPresent(c -> {
                            c.set(RadiationManager.BASELINE);
                            source.sendSuccess(MekanismLang.COMMAND_RADIATION_CLEAR.translateColored(EnumColor.GRAY), true);
                        });
                        return 0;
                    }).then(Commands.argument("targets", EntityArgument.entities())
                          .requires(MekanismPermissions.COMMAND_RADIATION_HEAL_OTHERS)
                          .executes(ctx -> {
                              CommandSourceStack source = ctx.getSource();
                              for (Entity entity : EntityArgument.getEntities(ctx, "targets")) {
                                  if (entity instanceof LivingEntity) {
                                      entity.getCapability(Capabilities.RADIATION_ENTITY).ifPresent(c -> {
                                          c.set(RadiationManager.BASELINE);
                                          source.sendSuccess(MekanismLang.COMMAND_RADIATION_CLEAR_ENTITY.translateColored(EnumColor.GRAY, EnumColor.INDIGO,
                                                entity.getDisplayName()), true);
                                      });
                                  }
                              }
                              return 0;
                          })
                    )
              ).then(Commands.literal("removeAll")
                    .requires(MekanismPermissions.COMMAND_RADIATION_REMOVE_ALL)
                    .executes(ctx -> {
                        RadiationManager.INSTANCE.clearSources();
                        ctx.getSource().sendSuccess(MekanismLang.COMMAND_RADIATION_REMOVE_ALL.translateColored(EnumColor.GRAY), true);
                        return 0;
                    })
              );
    }

    private static int addRadiation(CommandSourceStack source, Coordinates location, Level world, double magnitude) {
        return addRadiation(source, location.getPosition(source), world, magnitude);
    }

    private static int addRadiation(CommandSourceStack source, Vec3 pos, Level world, double magnitude) {
        Coord4D location = new Coord4D(pos.x, pos.y, pos.z, world.dimension());
        MekanismAPI.getRadiationManager().radiate(location, magnitude);
        source.sendSuccess(MekanismLang.COMMAND_RADIATION_ADD.translateColored(EnumColor.GRAY, RadiationScale.getSeverityColor(magnitude),
              UnitDisplayUtils.getDisplayShort(magnitude, RadiationUnit.SVH, 3), EnumColor.INDIGO, getPosition(location.getPos()), EnumColor.INDIGO,
              location.dimension.location()), true);
        return 0;
    }

    private static int getRadiationLevel(CommandSourceStack source, Coordinates location, Level world) {
        return getRadiationLevel(source, location.getPosition(source), world);
    }

    private static int getRadiationLevel(CommandSourceStack source, Vec3 pos, Level world) {
        Coord4D location = new Coord4D(pos.x, pos.y, pos.z, world.dimension());
        double magnitude = MekanismAPI.getRadiationManager().getRadiationLevel(location);
        source.sendSuccess(MekanismLang.COMMAND_RADIATION_GET.translateColored(EnumColor.GRAY, EnumColor.INDIGO, getPosition(location.getPos()), EnumColor.INDIGO,
                    location.dimension.location(), RadiationScale.getSeverityColor(magnitude), UnitDisplayUtils.getDisplayShort(magnitude, RadiationUnit.SVH, 3)),
              true);
        return 0;
    }

    private static Component getPosition(BlockPos pos) {
        return MekanismLang.GENERIC_BLOCK_POS.translate(pos.getX(), pos.getY(), pos.getZ());
    }
}
