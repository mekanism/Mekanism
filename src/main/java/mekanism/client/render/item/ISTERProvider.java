package mekanism.client.render.item;

import java.util.concurrent.Callable;
import mekanism.client.render.item.block.RenderChemicalDissolutionChamberItem;
import mekanism.client.render.item.block.RenderEnergyCubeItem;
import mekanism.client.render.item.block.RenderFluidTankItem;
import mekanism.client.render.item.block.RenderIndustrialAlarmItem;
import mekanism.client.render.item.block.RenderQuantumEntangloporterItem;
import mekanism.client.render.item.block.RenderSeismicVibratorItem;
import mekanism.client.render.item.block.RenderSolarNeutronActivatorItem;
import mekanism.client.render.item.gear.RenderArmoredJetpack;
import mekanism.client.render.item.gear.RenderAtomicDisassembler;
import mekanism.client.render.item.gear.RenderFlameThrower;
import mekanism.client.render.item.gear.RenderFreeRunners;
import mekanism.client.render.item.gear.RenderJetpack;
import mekanism.client.render.item.gear.RenderScubaMask;
import mekanism.client.render.item.gear.RenderScubaTank;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;

//This class is used to prevent class loading issues on the server without having to use OnlyIn hacks
public class ISTERProvider {

    private ISTERProvider() {
    }

    public static Callable<ItemStackTileEntityRenderer> energyCube() {
        return RenderEnergyCubeItem::new;
    }

    public static Callable<ItemStackTileEntityRenderer> dissolution() {
        return RenderChemicalDissolutionChamberItem::new;
    }

    public static Callable<ItemStackTileEntityRenderer> fluidTank() {
        return RenderFluidTankItem::new;
    }

    public static Callable<ItemStackTileEntityRenderer> industrialAlarm() {
        return RenderIndustrialAlarmItem::new;
    }

    public static Callable<ItemStackTileEntityRenderer> entangloporter() {
        return RenderQuantumEntangloporterItem::new;
    }

    public static Callable<ItemStackTileEntityRenderer> seismicVibrator() {
        return RenderSeismicVibratorItem::new;
    }

    public static Callable<ItemStackTileEntityRenderer> activator() {
        return RenderSolarNeutronActivatorItem::new;
    }

    public static Callable<ItemStackTileEntityRenderer> armoredJetpack() {
        return RenderArmoredJetpack::new;
    }

    public static Callable<ItemStackTileEntityRenderer> disassembler() {
        return RenderAtomicDisassembler::new;
    }

    public static Callable<ItemStackTileEntityRenderer> flamethrower() {
        return RenderFlameThrower::new;
    }

    public static Callable<ItemStackTileEntityRenderer> freeRunners() {
        return RenderFreeRunners::new;
    }

    public static Callable<ItemStackTileEntityRenderer> scubaMask() {
        return RenderScubaMask::new;
    }

    public static Callable<ItemStackTileEntityRenderer> jetpack() {
        return RenderJetpack::new;
    }

    public static Callable<ItemStackTileEntityRenderer> scubaTank() {
        return RenderScubaTank::new;
    }
}