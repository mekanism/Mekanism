package mekanism.common.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import mekanism.api.Coord4D;
import mekanism.api.MekanismAPI;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.lib.radiation.RadiationManager.RadiationScale;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.UnitDisplayUtils.RadiationUnit;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.DimensionArgument;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.ILocationArgument;
import net.minecraft.command.arguments.Vec3Argument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

public class RadiationCommand {

    static ArgumentBuilder<CommandSource, ?> register() {
        return Commands.literal("radiation")
              .requires(cs -> cs.hasPermission(2))
              .then(Commands.literal("add")
                    .then(Commands.argument("magnitude", DoubleArgumentType.doubleArg(0, 10_000))
                          .executes(ctx -> {
                              //Get position based on source
                              CommandSource source = ctx.getSource();
                              return addRadiation(source, source.getPosition(), source.getLevel(), DoubleArgumentType.getDouble(ctx, "magnitude"));
                          }).then(Commands.argument("location", Vec3Argument.vec3())
                                .executes(ctx -> {
                                    //Get position based on passed in value, and the source's world
                                    CommandSource source = ctx.getSource();
                                    return addRadiation(source, Vec3Argument.getCoordinates(ctx, "location"), source.getLevel(),
                                          DoubleArgumentType.getDouble(ctx, "magnitude"));
                                }).then(Commands.argument("dimension", DimensionArgument.dimension())
                                      .executes(ctx -> {
                                          //Get position and dimension by passed in values
                                          CommandSource source = ctx.getSource();
                                          return addRadiation(source, Vec3Argument.getCoordinates(ctx, "location"),
                                                DimensionArgument.getDimension(ctx, "dimension"), DoubleArgumentType.getDouble(ctx, "magnitude"));
                                      })
                                )
                          )
                    )
              ).then(Commands.literal("get")
                    .executes(ctx -> {
                        //Get position based on source
                        CommandSource source = ctx.getSource();
                        return getRadiationLevel(source, source.getPosition(), source.getLevel());
                    }).then(Commands.argument("location", Vec3Argument.vec3())
                          .executes(ctx -> {
                              //Get position based on passed in value, and the source's world
                              CommandSource source = ctx.getSource();
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
                    .executes(ctx -> {
                        CommandSource source = ctx.getSource();
                        source.getPlayerOrException().getCapability(Capabilities.RADIATION_ENTITY_CAPABILITY).ifPresent(c -> {
                            c.set(0);
                            source.sendSuccess(MekanismLang.COMMAND_RADIATION_CLEAR.translateColored(EnumColor.GRAY), true);
                        });
                        return 0;
                    }).then(Commands.argument("targets", EntityArgument.entities())
                          .executes(ctx -> {
                              CommandSource source = ctx.getSource();
                              for (Entity entity : EntityArgument.getEntities(ctx, "targets")) {
                                  if (entity instanceof LivingEntity) {
                                      entity.getCapability(Capabilities.RADIATION_ENTITY_CAPABILITY).ifPresent(c -> {
                                          c.set(0);
                                          source.sendSuccess(MekanismLang.COMMAND_RADIATION_CLEAR_ENTITY.translateColored(EnumColor.GRAY, EnumColor.INDIGO,
                                                entity.getDisplayName()), true);
                                      });
                                  }
                              }
                              return 0;
                          })
                    )
              ).then(Commands.literal("removeAll")
                    .executes(ctx -> {
                        RadiationManager.INSTANCE.clearSources();
                        ctx.getSource().sendSuccess(MekanismLang.COMMAND_RADIATION_REMOVE_ALL.translateColored(EnumColor.GRAY), true);
                        return 0;
                    })
              );
    }

    private static int addRadiation(CommandSource source, ILocationArgument location, World world, double magnitude) {
        return addRadiation(source, location.getPosition(source), world, magnitude);
    }

    private static int addRadiation(CommandSource source, Vector3d pos, World world, double magnitude) {
        Coord4D location = new Coord4D(pos.x, pos.y, pos.z, world.dimension());
        MekanismAPI.getRadiationManager().radiate(location, magnitude);
        source.sendSuccess(MekanismLang.COMMAND_RADIATION_ADD.translateColored(EnumColor.GRAY, RadiationScale.getSeverityColor(magnitude),
              UnitDisplayUtils.getDisplayShort(magnitude, RadiationUnit.SVH, 3), EnumColor.INDIGO, getPosition(location.getPos()), EnumColor.INDIGO,
              location.dimension.location()), true);
        return 0;
    }

    private static int getRadiationLevel(CommandSource source, ILocationArgument location, World world) {
        return getRadiationLevel(source, location.getPosition(source), world);
    }

    private static int getRadiationLevel(CommandSource source, Vector3d pos, World world) {
        Coord4D location = new Coord4D(pos.x, pos.y, pos.z, world.dimension());
        double magnitude = MekanismAPI.getRadiationManager().getRadiationLevel(location);
        source.sendSuccess(MekanismLang.COMMAND_RADIATION_GET.translateColored(EnumColor.GRAY, EnumColor.INDIGO, getPosition(location.getPos()), EnumColor.INDIGO,
                    location.dimension.location(), RadiationScale.getSeverityColor(magnitude), UnitDisplayUtils.getDisplayShort(magnitude, RadiationUnit.SVH, 3)),
              true);
        return 0;
    }

    private static ITextComponent getPosition(BlockPos pos) {
        return MekanismLang.GENERIC_BLOCK_POS.translate(pos.getX(), pos.getY(), pos.getZ());
    }
}