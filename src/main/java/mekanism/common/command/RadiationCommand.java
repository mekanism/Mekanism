package mekanism.common.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import mekanism.api.math.MathUtils;
import mekanism.api.radiation.IRadiationManager;
import mekanism.api.radiation.capability.IRadiationEntity;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.base.MekanismPermissions;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.lib.radiation.RadiationScale;
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
              .then(subCommandAdd())
              .then(subCommandAddEntity())
              .then(subCommandGet())
              .then(subCommandHeal())
              .then(subCommandReduce())
              .then(Commands.literal("removeAll")
                    .requires(MekanismPermissions.COMMAND_RADIATION_REMOVE_ALL)
                    .executes(ctx -> {
                        RadiationManager.get().clearSources();
                        ctx.getSource().sendSuccess(() -> MekanismLang.COMMAND_RADIATION_REMOVE_ALL.translateColored(EnumColor.GRAY), true);
                        return Command.SINGLE_SUCCESS;
                    })
              );
    }

    private static ArgumentBuilder<CommandSourceStack, ?> subCommandAdd() {
        return Commands.literal("add")
              .requires(MekanismPermissions.COMMAND_RADIATION_ADD)
              .then(Commands.argument("magnitude", DoubleArgumentType.doubleArg(Double.MIN_VALUE, 10_000))
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
              );
    }

    private static ArgumentBuilder<CommandSourceStack, ?> subCommandAddEntity() {
        return Commands.literal("addEntity")
              .requires(MekanismPermissions.COMMAND_RADIATION_ADD_ENTITY)
              .then(Commands.argument("magnitude", DoubleArgumentType.doubleArg(Double.MIN_VALUE, 10_000))
                    .executes(ctx -> {
                        CommandSourceStack source = ctx.getSource();
                        double magnitude = DoubleArgumentType.getDouble(ctx, "magnitude");
                        IRadiationEntity cap = source.getPlayerOrException().getCapability(Capabilities.RADIATION_ENTITY);
                        if (cap != null) {
                            cap.radiate(magnitude);
                            source.sendSuccess(() -> MekanismLang.COMMAND_RADIATION_ADD_ENTITY.translateColored(EnumColor.GRAY, RadiationScale.getSeverityColor(magnitude),
                                  UnitDisplayUtils.getDisplayShort(magnitude, RadiationUnit.SVH, 3)), true);
                            return getReturnLevelFromRadiation(magnitude);
                        }
                        return 0;
                    })
              ).then(Commands.argument("targets", EntityArgument.entities())
                    .requires(MekanismPermissions.COMMAND_RADIATION_ADD_ENTITY_OTHERS)
                    .then(Commands.argument("magnitude", DoubleArgumentType.doubleArg(Double.MIN_VALUE, 10_000))
                          .executes(ctx -> {
                              CommandSourceStack source = ctx.getSource();
                              double magnitude = DoubleArgumentType.getDouble(ctx, "magnitude");
                              int addedTo = 0;
                              for (Entity entity : EntityArgument.getEntities(ctx, "targets")) {
                                  if (entity instanceof LivingEntity) {
                                      IRadiationEntity cap = entity.getCapability(Capabilities.RADIATION_ENTITY);
                                      if (cap != null) {
                                          cap.radiate(magnitude);
                                          source.sendSuccess(() -> MekanismLang.COMMAND_RADIATION_ADD_ENTITY_TARGET.translateColored(EnumColor.GRAY,
                                                RadiationScale.getSeverityColor(magnitude), UnitDisplayUtils.getDisplayShort(magnitude, RadiationUnit.SVH, 3),
                                                EnumColor.INDIGO, entity.getDisplayName()), true);
                                      }
                                      addedTo++;
                                  }
                              }
                              return addedTo;
                          })
                    )
              );
    }

    private static ArgumentBuilder<CommandSourceStack, ?> subCommandGet() {
        return Commands.literal("get")
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
              );
    }

    private static ArgumentBuilder<CommandSourceStack, ?> subCommandHeal() {
        return Commands.literal("heal")
              .requires(MekanismPermissions.COMMAND_RADIATION_HEAL)
              .executes(ctx -> {
                  CommandSourceStack source = ctx.getSource();
                  IRadiationEntity cap = source.getPlayerOrException().getCapability(Capabilities.RADIATION_ENTITY);
                  if (cap != null) {
                      cap.set(IRadiationManager.INSTANCE.baselineRadiation());
                      source.sendSuccess(() -> MekanismLang.COMMAND_RADIATION_CLEAR.translateColored(EnumColor.GRAY), true);
                  }
                  return Command.SINGLE_SUCCESS;
              }).then(Commands.argument("targets", EntityArgument.entities())
                    .requires(MekanismPermissions.COMMAND_RADIATION_HEAL_OTHERS)
                    .executes(ctx -> {
                        CommandSourceStack source = ctx.getSource();
                        int healed = 0;
                        for (Entity entity : EntityArgument.getEntities(ctx, "targets")) {
                            if (entity instanceof LivingEntity) {
                                IRadiationEntity cap = entity.getCapability(Capabilities.RADIATION_ENTITY);
                                if (cap != null) {
                                    cap.set(IRadiationManager.INSTANCE.baselineRadiation());
                                    source.sendSuccess(() -> MekanismLang.COMMAND_RADIATION_CLEAR_ENTITY.translateColored(EnumColor.GRAY, EnumColor.INDIGO,
                                          entity.getDisplayName()), true);
                                }
                                healed++;
                            }
                        }
                        return healed;
                    })
              );
    }

    private static ArgumentBuilder<CommandSourceStack, ?> subCommandReduce() {
        return Commands.literal("reduce")
              .requires(MekanismPermissions.COMMAND_RADIATION_REDUCE)
              .then(Commands.argument("magnitude", DoubleArgumentType.doubleArg(Double.MIN_VALUE, 10_000))
                    .executes(ctx -> {
                        CommandSourceStack source = ctx.getSource();
                        double magnitude = DoubleArgumentType.getDouble(ctx, "magnitude");
                        IRadiationEntity cap = source.getPlayerOrException().getCapability(Capabilities.RADIATION_ENTITY);
                        if (cap != null) {
                            double radiation = cap.getRadiation();
                            double newValue = Math.max(IRadiationManager.INSTANCE.baselineRadiation(), radiation - magnitude);
                            double reduced = radiation - newValue;
                            cap.set(newValue);
                            source.sendSuccess(() -> MekanismLang.COMMAND_RADIATION_REDUCE.translateColored(EnumColor.GRAY, RadiationScale.getSeverityColor(reduced),
                                  UnitDisplayUtils.getDisplayShort(reduced, RadiationUnit.SVH, 3)), true);
                            return getReturnLevelFromRadiation(reduced);
                        }
                        return 0;
                    })
              ).then(Commands.argument("targets", EntityArgument.entities())
                    .requires(MekanismPermissions.COMMAND_RADIATION_REDUCE_OTHERS)
                    .then(Commands.argument("magnitude", DoubleArgumentType.doubleArg(Double.MIN_VALUE, 10_000))
                          .executes(ctx -> {
                              CommandSourceStack source = ctx.getSource();
                              double magnitude = DoubleArgumentType.getDouble(ctx, "magnitude");
                              int reducedFrom = 0;
                              for (Entity entity : EntityArgument.getEntities(ctx, "targets")) {
                                  if (entity instanceof LivingEntity) {
                                      IRadiationEntity cap = entity.getCapability(Capabilities.RADIATION_ENTITY);
                                      if (cap != null) {
                                          double radiation = cap.getRadiation();
                                          double newValue = Math.max(IRadiationManager.INSTANCE.baselineRadiation(), radiation - magnitude);
                                          double reduced = radiation - newValue;
                                          cap.set(newValue);
                                          source.sendSuccess(() -> MekanismLang.COMMAND_RADIATION_REDUCE_TARGET.translateColored(EnumColor.GRAY,
                                                EnumColor.INDIGO, entity.getDisplayName(), RadiationScale.getSeverityColor(reduced),
                                                UnitDisplayUtils.getDisplayShort(reduced, RadiationUnit.SVH, 3)), true);
                                      }
                                      reducedFrom++;
                                  }
                              }
                              return reducedFrom;
                          })
                    )
              );
    }

    private static int addRadiation(CommandSourceStack source, Coordinates location, Level world, double magnitude) {
        return addRadiation(source, location.getPosition(source), world, magnitude);
    }

    private static int addRadiation(CommandSourceStack source, Vec3 pos, Level world, double magnitude) {
        BlockPos location = BlockPos.containing(pos);
        IRadiationManager.INSTANCE.radiate(world, location, magnitude);
        source.sendSuccess(() -> MekanismLang.COMMAND_RADIATION_ADD.translateColored(EnumColor.GRAY, RadiationScale.getSeverityColor(magnitude),
              UnitDisplayUtils.getDisplayShort(magnitude, RadiationUnit.SVH, 3), EnumColor.INDIGO, getPosition(location), EnumColor.INDIGO, world), true);
        return getReturnLevelFromRadiation(magnitude);
    }

    private static int getRadiationLevel(CommandSourceStack source, Coordinates location, Level world) {
        return getRadiationLevel(source, location.getPosition(source), world);
    }

    private static int getRadiationLevel(CommandSourceStack source, Vec3 pos, Level world) {
        BlockPos location = BlockPos.containing(pos);
        double magnitude = IRadiationManager.INSTANCE.getRadiationLevel(world, location);
        source.sendSuccess(() -> MekanismLang.COMMAND_RADIATION_GET.translateColored(EnumColor.GRAY, EnumColor.INDIGO, getPosition(location), EnumColor.INDIGO,
                    world, RadiationScale.getSeverityColor(magnitude), UnitDisplayUtils.getDisplayShort(magnitude, RadiationUnit.SVH, 3)),
              true);
        return getReturnLevelFromRadiation(magnitude);
    }

    private static Component getPosition(BlockPos pos) {
        return MekanismLang.GENERIC_BLOCK_POS.translate(pos.getX(), pos.getY(), pos.getZ());
    }

    private static int getReturnLevelFromRadiation(double magnitude) {
        if (magnitude < IRadiationManager.INSTANCE.minRadiationMagnitude()) {
            return 0;
        }
        //Multiply to make it so that we can differentiate various levels based on what the micro sievert rate would be
        return MathUtils.clampToInt(magnitude * 1_000_000);
    }
}
