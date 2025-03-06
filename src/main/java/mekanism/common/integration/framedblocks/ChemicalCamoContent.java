package mekanism.common.integration.framedblocks;

import mekanism.api.chemical.Chemical;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.registration.impl.FluidDeferredRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.common.util.TriState;
import org.jetbrains.annotations.Nullable;
import xfacthd.framedblocks.api.camo.CamoClientHandler;
import xfacthd.framedblocks.api.camo.CamoContent;

final class ChemicalCamoContent extends CamoContent<ChemicalCamoContent> {

    private final Holder<Chemical> chemicalHolder;
    private final MapColor mapColor;

    ChemicalCamoContent(Holder<Chemical> chemicalHolder) {
        this.chemicalHolder = chemicalHolder;
        this.mapColor = FluidDeferredRegister.getClosestColor(this.chemicalHolder.value().getColorRepresentation());
    }

    Holder<Chemical> getChemicalHolder() {
        return chemicalHolder;
    }

    @Override
    public boolean propagatesSkylightDown(BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public float getExplosionResistance(BlockGetter level, BlockPos pos, Explosion explosion) {
        return 0;
    }

    @Override
    public boolean isFlammable(BlockGetter level, BlockPos pos, Direction side) {
        return false;
    }

    @Override
    public int getFlammability(BlockGetter level, BlockPos pos, Direction side) {
        return 0;
    }

    @Override
    public int getFireSpreadSpeed(BlockGetter level, BlockPos pos, Direction side) {
        return 0;
    }

    @Override
    public float getShadeBrightness(BlockGetter level, BlockPos pos, float frameShade) {
        return 1F;
    }

    @Override
    public int getLightEmission() {
        // TODO: light level is currently not forwarded from ChemicalConstants to the registered Chemical
        return 0;
    }

    @Override
    public boolean isEmissive() {
        return false;
    }

    @Override
    public SoundType getSoundType() {
        return SoundType.WET_GRASS;
    }

    @Override
    public boolean shouldDisplayFluidOverlay(BlockAndTintGetter level, BlockPos pos, FluidState fluidState) {
        return true;
    }

    @Override
    public float getFriction(LevelReader level, BlockPos pos, @Nullable Entity entity, float frameFriction) {
        return frameFriction;
    }

    @Override
    public TriState canSustainPlant(BlockGetter level, BlockPos pos, Direction side, BlockState plant) {
        return TriState.DEFAULT;
    }

    @Override
    public boolean canEntityDestroy(BlockGetter level, BlockPos pos, Entity entity) {
        return true;
    }

    @Override
    @Nullable
    public MapColor getMapColor(BlockGetter level, BlockPos pos) {
        return mapColor;
    }

    @Override
    public int getTintColor(BlockAndTintGetter blockAndTintGetter, BlockPos pos, int tintIdx) {
        return chemicalHolder.value().getTint();
    }

    @Override
    public Integer getBeaconColorMultiplier(LevelReader levelReader, BlockPos pos, BlockPos beaconPos) {
        return chemicalHolder.value().getColorRepresentation();
    }

    @Override
    public boolean isSolid(BlockGetter level, BlockPos pos) {
        return false;
    }

    @Override
    public boolean canOcclude() {
        return false;
    }

    @Override
    public BlockState getAsBlockState() {
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public BlockState getAppearanceState() {
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public boolean isOccludedBy(BlockState adjState, BlockGetter level, BlockPos pos, BlockPos adjPos) {
        return adjState.isSolidRender(level, pos);
    }

    @Override
    public boolean isOccludedBy(CamoContent<?> adjCamo, BlockGetter level, BlockPos pos, BlockPos adjPos) {
        return adjCamo.isSolid(level, pos) || equals(adjCamo);
    }

    @Override
    public boolean occludes(BlockState adjState, BlockGetter level, BlockPos pos, BlockPos adjPos) {
        return false;
    }

    @Override
    public ParticleOptions makeRunningLandingParticles(BlockPos pos) {
        return new ChemicalParticleOptions(chemicalHolder);
    }

    @Override
    public String getCamoId() {
        return chemicalHolder.getRegisteredName();
    }

    @Override
    public MutableComponent getCamoName() {
        return TextComponentUtil.build(chemicalHolder);
    }

    @Override
    public CamoClientHandler<ChemicalCamoContent> getClientHandler() {
        return ChemicalCamoClientHandler.INSTANCE;
    }

    @Override
    public int hashCode() {
        return chemicalHolder.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != ChemicalCamoContent.class) return false;
        return chemicalHolder.is(((ChemicalCamoContent) obj).chemicalHolder);
    }

    @Override
    public String toString() {
        return "ChemicalCamoContent{" + getCamoId() + "}";
    }
}
