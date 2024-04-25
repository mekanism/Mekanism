package mekanism.api.chemical;

import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.ToIntFunction;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.JsonConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.container.ContainerInteraction;
import mekanism.api.container.InContainerGetter;
import mekanism.api.container.LongContainerInteraction;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ChemicalUtils {

    //nb: these can't be in their respective classes as they init before the custom registries are created, thus the EMPTY init fails
    public static final MapCodec<GasStack> GAS_STACK_CODEC = ChemicalStack.codec(Gas.CODEC, JsonConstants.GAS, GasStack::new);
    public static final MapCodec<InfusionStack> INFUSION_STACK_CODEC = ChemicalStack.codec(InfuseType.CODEC, JsonConstants.INFUSE_TYPE, InfusionStack::new);
    public static final MapCodec<PigmentStack> PIGMENT_STACK_CODEC = ChemicalStack.codec(Pigment.CODEC, JsonConstants.PIGMENT, PigmentStack::new);
    public static final MapCodec<SlurryStack> SLURRY_STACK_CODEC = ChemicalStack.codec(Slurry.CODEC, JsonConstants.SLURRY, SlurryStack::new);

    //TODO - 1.20.5: Replace these with more direct codecs and also add an optional variant to mimic the other types?
    public static final StreamCodec<RegistryFriendlyByteBuf, GasStack> GAS_STACK_STREAM_CODEC = StreamCodec.ofMember(GasStack::writeToPacket, GasStack::readFromPacket);
    public static final StreamCodec<RegistryFriendlyByteBuf, InfusionStack> INFUSION_STACK_STREAM_CODEC = StreamCodec.ofMember(InfusionStack::writeToPacket, InfusionStack::readFromPacket);
    public static final StreamCodec<RegistryFriendlyByteBuf, PigmentStack> PIGMENT_STACK_STREAM_CODEC = StreamCodec.ofMember(PigmentStack::writeToPacket, PigmentStack::readFromPacket);
    public static final StreamCodec<RegistryFriendlyByteBuf, SlurryStack> SLURRY_STACK_STREAM_CODEC = StreamCodec.ofMember(SlurryStack::writeToPacket, SlurryStack::readFromPacket);

    private ChemicalUtils() {
    }

    /**
     * Writes a Chemical Stack to a Packet Buffer.
     *
     * @param buffer Buffer to write to.
     * @param stack  Stack to write.
     */
    public static void writeChemicalStack(FriendlyByteBuf buffer, ChemicalStack<?> stack) {
        if (stack.isEmpty()) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            stack.writeToPacket(buffer);
        }
    }

    /**
     * Reads a Gas Stack from a buffer.
     *
     * @param buffer Buffer to read from.
     *
     * @return Gas Stack.
     */
    public static GasStack readGasStack(FriendlyByteBuf buffer) {
        return readStack(buffer, GasStack::readFromPacket, GasStack.EMPTY);
    }

    /**
     * Reads an Infusion Stack from a buffer.
     *
     * @param buffer Buffer to read from.
     *
     * @return Infusion Stack.
     */
    public static InfusionStack readInfusionStack(FriendlyByteBuf buffer) {
        return readStack(buffer, InfusionStack::readFromPacket, InfusionStack.EMPTY);
    }

    /**
     * Reads a Pigment Stack from a buffer.
     *
     * @param buffer Buffer to read from.
     *
     * @return Pigment Stack.
     */
    public static PigmentStack readPigmentStack(FriendlyByteBuf buffer) {
        return readStack(buffer, PigmentStack::readFromPacket, PigmentStack.EMPTY);
    }

    /**
     * Reads a Slurry Stack from a buffer.
     *
     * @param buffer Buffer to read from.
     *
     * @return Slurry Stack.
     */
    public static SlurryStack readSlurryStack(FriendlyByteBuf buffer) {
        return readStack(buffer, SlurryStack::readFromPacket, SlurryStack.EMPTY);
    }

    /**
     * Helper to read a Chemical Stack from a buffer.
     *
     * @param buffer Buffer to read from.
     * @param reader How to read it if it isn't empty.
     * @param empty  Empty variant.
     *
     * @return Chemical Stack.
     */
    private static <STACK extends ChemicalStack<?>> STACK readStack(FriendlyByteBuf buffer, Function<FriendlyByteBuf, STACK> reader, STACK empty) {
        return buffer.readBoolean() ? reader.apply(buffer) : empty;
    }

    /**
     * Helper to read a chemical from NBT and if it isn't present fallback to the empty chemical.
     *
     * @param nbtTags        NBT.
     * @param empty          Empty instance.
     * @param nbtName        Name of the chemical.
     * @param registryLookup Registry lookup.
     *
     * @return Chemical.
     */
    public static <CHEMICAL extends Chemical<CHEMICAL>> CHEMICAL readChemicalFromNBT(@Nullable CompoundTag nbtTags, CHEMICAL empty, String nbtName,
          Function<ResourceLocation, CHEMICAL> registryLookup) {
        if (nbtTags == null || nbtTags.isEmpty()) {
            return empty;
        }
        return registryLookup.apply(new ResourceLocation(nbtTags.getString(nbtName)));
    }

    /**
     * Helper to read a chemical from a registry and if it isn't present fallback to the empty chemical.
     *
     * @param name     Name of the chemical.
     * @param empty    Empty instance.
     * @param registry Registry.
     *
     * @return Chemical.
     */
    public static <CHEMICAL extends Chemical<CHEMICAL>> CHEMICAL readChemicalFromRegistry(@Nullable ResourceLocation name, CHEMICAL empty,
          Registry<CHEMICAL> registry) {
        if (name == null) {
            return empty;
        }
        CHEMICAL chemical = registry.get(name);
        //Note: This should never be null as the registry defaults to the empty variant, but we validate it anyway
        return Objects.requireNonNullElse(chemical, empty);
    }

    /**
     * Util method for a generic insert implementation for various handlers. Mainly for internal use only
     *
     * @deprecated Please use {@link #insert(ChemicalStack, Direction, Action, ChemicalStack, ToIntFunction, InContainerGetter, ContainerInteraction)} to avoid capturing
     * lambdas.
     */
    @Deprecated(forRemoval = true, since = "10.5.13")
    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> STACK insert(STACK stack, Action action, STACK empty,
          IntSupplier tankCount, IntFunction<@NotNull STACK> inTankGetter, InsertChemical<STACK> insertChemical) {
        if (stack.isEmpty()) {
            //Short circuit if nothing is actually being inserted
            return empty;
        }
        return insert(stack, null, action, empty, side -> tankCount.getAsInt(), (tank, side) -> inTankGetter.apply(tank),
              (tank, chemical, side, act) -> insertChemical.insert(tank, chemical, act));
    }

    /**
     * Util method for a generic insert implementation for various handlers. Mainly for internal use only
     *
     * @since 10.5.13
     */
    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> STACK insert(STACK stack, @Nullable Direction side, Action action,
          STACK empty, ToIntFunction<@Nullable Direction> tankCount, InContainerGetter<STACK> inTankGetter, ContainerInteraction<STACK> insertChemical) {
        if (stack.isEmpty()) {
            //Short circuit if nothing is actually being inserted
            return empty;
        }
        int tanks = tankCount.applyAsInt(side);
        if (tanks == 0) {
            return stack;
        } else if (tanks == 1) {
            return insertChemical.interact(0, stack, side, action);
        }
        STACK toInsert = stack;
        //Start by trying to insert into the tanks that have the same type
        IntList emptyTanks = new IntArrayList();
        for (int tank = 0; tank < tanks; tank++) {
            STACK inTank = inTankGetter.getStored(tank, side);
            if (inTank.isEmpty()) {
                emptyTanks.add(tank);
            } else if (inTank.isTypeEqual(stack)) {
                STACK remainder = insertChemical.interact(tank, toInsert, side, action);
                if (remainder.isEmpty()) {
                    //If we have no remaining chemical, return that we fit it all
                    return empty;
                }
                //Update what we have left to insert, to be the amount we were unable to insert
                toInsert = remainder;
            }
        }
        for (int tank : emptyTanks) {
            STACK remainder = insertChemical.interact(tank, toInsert, side, action);
            if (remainder.isEmpty()) {
                //If we have no remaining chemical, return that we fit it all
                return empty;
            }
            //Update what we have left to insert, to be the amount we were unable to insert
            toInsert = remainder;
        }
        return toInsert;
    }

    /**
     * Util method for a generic insert implementation for various handlers. Mainly for internal use only
     *
     * @since 10.5.13
     */
    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>> STACK insert(STACK stack,
          @Nullable Direction side, Function<@Nullable Direction, List<TANK>> tankSupplier, Action action, AutomationType automationType, STACK empty) {
        if (stack.isEmpty()) {
            //Short circuit if nothing is actually being inserted
            return empty;
        }
        List<TANK> chemicalContainers = tankSupplier.apply(side);
        if (chemicalContainers.isEmpty()) {
            return stack;
        } else if (chemicalContainers.size() == 1) {
            return chemicalContainers.get(0).insert(stack, action, automationType);
        }
        STACK toInsert = stack;
        //Start by trying to insert into the tanks that have the same type
        List<TANK> emptyTanks = new ArrayList<>();
        for (TANK tank : chemicalContainers) {
            if (tank.isEmpty()) {
                emptyTanks.add(tank);
            } else if (tank.isTypeEqual(stack)) {
                STACK remainder = tank.insert(toInsert, action, automationType);
                if (remainder.isEmpty()) {
                    //If we have no remaining chemical, return that we fit it all
                    return empty;
                }
                //Update what we have left to insert, to be the amount we were unable to insert
                toInsert = remainder;
            }
        }
        for (TANK tank : emptyTanks) {
            STACK remainder = tank.insert(toInsert, action, automationType);
            if (remainder.isEmpty()) {
                //If we have no remaining chemical, return that we fit it all
                return empty;
            }
            //Update what we have left to insert, to be the amount we were unable to insert
            toInsert = remainder;
        }
        return toInsert;
    }

    /**
     * Util method for a generic extraction implementation for various handlers. Mainly for internal use only
     *
     * @deprecated Please use {@link #extract(long, Direction, Action, ChemicalStack, ToIntFunction, InContainerGetter, LongContainerInteraction)} to avoid capturing
     * lambdas.
     */
    @Deprecated(forRemoval = true, since = "10.5.13")
    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> STACK extract(long amount, Action action, STACK empty,
          IntSupplier tankCount, IntFunction<@NotNull STACK> inTankGetter, ExtractChemical<STACK> extractChemical) {
        if (amount == 0) {
            return empty;
        }
        return extract(amount, null, action, empty, side -> tankCount.getAsInt(), (tank, side) -> inTankGetter.apply(tank),
              (tank, chemical, side, act) -> extractChemical.extract(tank, chemical, act));
    }

    /**
     * Util method for a generic extraction implementation for various handlers. Mainly for internal use only
     *
     * @since 10.5.13
     */
    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> STACK extract(long amount, @Nullable Direction side, Action action,
          STACK empty, ToIntFunction<@Nullable Direction> tankCount, InContainerGetter<STACK> inTankGetter, LongContainerInteraction<STACK> extractChemical) {
        if (amount == 0) {
            return empty;
        }
        int tanks = tankCount.applyAsInt(side);
        if (tanks == 0) {
            return empty;
        } else if (tanks == 1) {
            return extractChemical.interact(0, amount, side, action);
        }
        STACK extracted = empty;
        long toDrain = amount;
        for (int tank = 0; tank < tanks; tank++) {
            if (extracted.isEmpty() || extracted.isTypeEqual(inTankGetter.getStored(tank, side))) {
                //If there is chemical in the tank that matches the type we have started draining, or we haven't found a type yet
                STACK drained = extractChemical.interact(tank, toDrain, side, action);
                if (!drained.isEmpty()) {
                    //If we were able to drain something, set it as the type we have extracted/increase how much we have extracted
                    if (extracted.isEmpty()) {
                        extracted = drained;
                    } else {
                        extracted.grow(drained.getAmount());
                    }
                    toDrain -= drained.getAmount();
                    if (toDrain == 0) {
                        //If we are done draining break and return the amount extracted
                        break;
                    }
                    //Otherwise, keep looking and attempt to drain more from the handler, making sure that it is of
                    // the same type as we have found
                }
            }
        }
        return extracted;
    }

    /**
     * Util method for a generic extraction implementation for various handlers. Mainly for internal use only
     *
     * @since 10.5.13
     */
    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>> STACK extract(long amount,
          @Nullable Direction side, Function<@Nullable Direction, List<TANK>> tankSupplier, Action action, AutomationType automationType, STACK empty) {
        if (amount == 0) {
            return empty;
        }
        List<TANK> chemicalTanks = tankSupplier.apply(side);
        if (chemicalTanks.isEmpty()) {
            return empty;
        } else if (chemicalTanks.size() == 1) {
            return chemicalTanks.get(0).extract(amount, action, automationType);
        }
        STACK extracted = empty;
        long toDrain = amount;
        for (TANK tank : chemicalTanks) {
            if (extracted.isEmpty() || tank.isTypeEqual(extracted)) {
                //If there is chemical in the tank that matches the type we have started draining, or we haven't found a type yet
                STACK drained = tank.extract(toDrain, action, automationType);
                if (!drained.isEmpty()) {
                    //If we were able to drain something, set it as the type we have extracted/increase how much we have extracted
                    if (extracted.isEmpty()) {
                        extracted = drained;
                    } else {
                        extracted.grow(drained.getAmount());
                    }
                    toDrain -= drained.getAmount();
                    if (toDrain == 0) {
                        //If we are done draining break and return the amount extracted
                        break;
                    }
                    //Otherwise, keep looking and attempt to drain more from the handler, making sure that it is of
                    // the same type as we have found
                }
            }
        }
        return extracted;
    }

    /**
     * Util method for a generic extraction implementation for various handlers. Mainly for internal use only
     *
     * @deprecated Please use {@link #extract(long, Direction, Action, ChemicalStack, ToIntFunction, InContainerGetter, LongContainerInteraction)} to avoid capturing
     * lambdas.
     */
    @Deprecated(forRemoval = true, since = "10.5.13")
    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> STACK extract(STACK stack, Action action, STACK empty,
          IntSupplier tankCount, IntFunction<@NotNull STACK> inTankGetter, ExtractChemical<STACK> extractChemical) {
        if (stack.isEmpty()) {
            return empty;
        }
        return extract(stack, null, action, empty, side -> tankCount.getAsInt(), (tank, side) -> inTankGetter.apply(tank),
              (tank, chemical, side, act) -> extractChemical.extract(tank, chemical, act));
    }

    /**
     * Util method for a generic extraction implementation for various handlers. Mainly for internal use only
     *
     * @since 10.5.13
     */
    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> STACK extract(STACK stack, @Nullable Direction side, Action action, STACK empty,
          ToIntFunction<@Nullable Direction> tankCount, InContainerGetter<STACK> inTankGetter, LongContainerInteraction<STACK> extractChemical) {
        if (stack.isEmpty()) {
            return empty;
        }
        int tanks = tankCount.applyAsInt(side);
        if (tanks == 0) {
            return empty;
        } else if (tanks == 1) {
            STACK inTank = inTankGetter.getStored(0, side);
            if (inTank.isEmpty() || !inTank.isTypeEqual(stack)) {
                return empty;
            }
            return extractChemical.interact(0, stack.getAmount(), side, action);
        }
        STACK extracted = empty;
        long toDrain = stack.getAmount();
        for (int tank = 0; tank < tanks; tank++) {
            if (stack.isTypeEqual(inTankGetter.getStored(tank, side))) {
                //If there is chemical in the tank that matches the type we are trying to drain, try to drain from it
                STACK drained = extractChemical.interact(tank, toDrain, side, action);
                if (!drained.isEmpty()) {
                    //If we were able to drain something, set it as the type we have extracted/increase how much we have extracted
                    if (extracted.isEmpty()) {
                        extracted = drained;
                    } else {
                        extracted.grow(drained.getAmount());
                    }
                    toDrain -= drained.getAmount();
                    if (toDrain == 0) {
                        //If we are done draining break and return the amount extracted
                        break;
                    }
                    //Otherwise, keep looking and attempt to drain more from the handler
                }
            }
        }
        return extracted;
    }

    /**
     * Util method for a generic extraction implementation for various handlers. Mainly for internal use only
     *
     * @since 10.5.13
     */
    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>> STACK extract(STACK stack,
          @Nullable Direction side, Function<@Nullable Direction, List<TANK>> tankSupplier, Action action, AutomationType automationType, STACK empty) {
        if (stack.isEmpty()) {
            return empty;
        }
        List<TANK> chemicalTanks = tankSupplier.apply(side);
        if (chemicalTanks.isEmpty()) {
            return empty;
        } else if (chemicalTanks.size() == 1) {
            TANK tank = chemicalTanks.get(0);
            if (tank.isEmpty() || !tank.isTypeEqual(stack)) {
                return empty;
            }
            return tank.extract(stack.getAmount(), action, automationType);
        }
        STACK extracted = empty;
        long toDrain = stack.getAmount();
        for (TANK tank : chemicalTanks) {
            if (tank.isTypeEqual(stack)) {
                //If there is chemical in the tank that matches the type we are trying to drain, try to drain from it
                STACK drained = tank.extract(toDrain, action, automationType);
                if (!drained.isEmpty()) {
                    //If we were able to drain something, set it as the type we have extracted/increase how much we have extracted
                    if (extracted.isEmpty()) {
                        extracted = drained;
                    } else {
                        extracted.grow(drained.getAmount());
                    }
                    toDrain -= drained.getAmount();
                    if (toDrain == 0) {
                        //If we are done draining break and return the amount extracted
                        break;
                    }
                    //Otherwise, keep looking and attempt to drain more from the handler
                }
            }
        }
        return extracted;
    }

    /**
     * @deprecated See {@link mekanism.api.container.ContainerInteraction}
     */
    @FunctionalInterface
    @Deprecated(forRemoval = true, since = "10.5.13")
    public interface InsertChemical<STACK extends ChemicalStack<?>> {

        STACK insert(int tank, STACK stack, Action action);
    }

    /**
     * @deprecated See {@link mekanism.api.container.LongContainerInteraction}
     */
    @FunctionalInterface
    @Deprecated(forRemoval = true, since = "10.5.13")
    public interface ExtractChemical<STACK extends ChemicalStack<?>> {

        STACK extract(int tank, long amount, Action action);
    }

    @FunctionalInterface
    public interface ChemicalToStackCreator<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> {

        STACK createStack(CHEMICAL chemical, long amount);
    }

    @FunctionalInterface
    public interface StackToStackCreator<STACK extends ChemicalStack<?>> {

        STACK createStack(STACK chemical, long amount);
    }
}