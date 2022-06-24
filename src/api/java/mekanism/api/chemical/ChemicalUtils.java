package mekanism.api.chemical;

import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.function.Function;
import java.util.function.IntSupplier;
import mekanism.api.Action;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.SlurryStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ChemicalUtils {

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
          IForgeRegistry<CHEMICAL> registry) {
        if (name == null) {
            return empty;
        }
        CHEMICAL chemical = registry.getValue(name);
        if (chemical == null) {
            return empty;
        }
        return chemical;
    }

    /**
     * Util method for a generic insert implementation for various handlers. Mainly for internal use only
     */
    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> STACK insert(STACK stack, Action action, STACK empty,
          IntSupplier tankCount, Int2ObjectFunction<@NotNull STACK> inTankGetter, InsertChemical<STACK> insertChemical) {
        int tanks = tankCount.getAsInt();
        if (tanks == 1) {
            return insertChemical.insert(0, stack, action);
        }
        IntList matchingTanks = new IntArrayList();
        IntList emptyTanks = new IntArrayList();
        for (int tank = 0; tank < tanks; tank++) {
            STACK inTank = inTankGetter.get(tank);
            if (inTank.isEmpty()) {
                emptyTanks.add(tank);
            } else if (inTank.isTypeEqual(stack)) {
                matchingTanks.add(tank);
            }
        }
        STACK toInsert = stack;
        //Start by trying to insert into the tanks that have the same type
        for (int tank : matchingTanks) {
            STACK remainder = insertChemical.insert(tank, toInsert, action);
            if (remainder.isEmpty()) {
                //If we have no remaining chemical, return that we fit it all
                return empty;
            }
            //Update what we have left to insert, to be the amount we were unable to insert
            toInsert = remainder;
        }
        for (int tank : emptyTanks) {
            STACK remainder = insertChemical.insert(tank, toInsert, action);
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
     */
    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> STACK extract(long amount, Action action, STACK empty,
          IntSupplier tankCount, Int2ObjectFunction<@NotNull STACK> inTankGetter, ExtractChemical<STACK> extractChemical) {
        int tanks = tankCount.getAsInt();
        if (tanks == 1) {
            return extractChemical.extract(0, amount, action);
        }
        STACK extracted = empty;
        long toDrain = amount;
        for (int tank = 0; tank < tanks; tank++) {
            if (extracted.isEmpty() || extracted.isTypeEqual(inTankGetter.get(tank))) {
                //If there is chemical in the tank that matches the type we have started draining, or we haven't found a type yet
                STACK drained = extractChemical.extract(tank, toDrain, action);
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
     */
    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> STACK extract(STACK stack, Action action, STACK empty,
          IntSupplier tankCount, Int2ObjectFunction<@NotNull STACK> inTankGetter, ExtractChemical<STACK> extractChemical) {
        int tanks = tankCount.getAsInt();
        if (tanks == 1) {
            STACK inTank = inTankGetter.get(0);
            if (inTank.isEmpty() || !inTank.isTypeEqual(stack)) {
                return empty;
            }
            return extractChemical.extract(0, stack.getAmount(), action);
        }
        STACK extracted = empty;
        long toDrain = stack.getAmount();
        for (int tank = 0; tank < tanks; tank++) {
            if (stack.isTypeEqual(inTankGetter.get(tank))) {
                //If there is chemical in the tank that matches the type we are trying to drain, try to drain from it
                STACK drained = extractChemical.extract(tank, toDrain, action);
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

    @FunctionalInterface
    public interface InsertChemical<STACK extends ChemicalStack<?>> {

        STACK insert(int tank, STACK stack, Action action);
    }

    @FunctionalInterface
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