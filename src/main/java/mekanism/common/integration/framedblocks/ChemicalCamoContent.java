package mekanism.common.integration.framedblocks;

import mekanism.api.annotations.MethodsAreNotNullByDefault;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.registration.impl.FluidDeferredRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.TriState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.Nullable;
import io.github.xfacthd.framedblocks.api.camo.CamoContentClientHandler;
import io.github.xfacthd.framedblocks.api.camo.CamoContent;

@ParametersAreNotNullByDefault
@MethodsAreNotNullByDefault
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
    public boolean propagatesSkylightDown() {
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
    public boolean isIgnitedByLava(BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return false;
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
    public boolean shouldDisplayFluidOverlay(BlockAndLightGetter level, BlockPos pos, FluidState fluidState) {
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

    //TODO - 26.1 @Override
    public int getTintColor(BlockAndLightGetter blockAndTintGetter, BlockPos pos, int tintIdx) {
        return chemicalHolder.value().getTint();
    }

    @Override
    public Integer getBeaconColorMultiplier(LevelReader levelReader, BlockPos pos, BlockPos beaconPos) {
        return chemicalHolder.value().getColorRepresentation();
    }

    @Override
    public boolean isSolid() {
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
    public boolean isOccludedBy(BlockState adjState, BlockGetter level, BlockPos pos, BlockPos adjPos, Direction direction) {
        return adjState.isSolidRender();
    }

    @Override
    public boolean isOccludedBy(CamoContent<?> adjCamo, BlockGetter level, BlockPos pos, BlockPos adjPos, Direction direction) {
        return adjCamo.isSolid() || equals(adjCamo);
    }

    @Override
    public boolean occludes(BlockState adjState, BlockGetter level, BlockPos pos, BlockPos adjPos, Direction direction) {
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
    public CamoContentClientHandler<ChemicalCamoContent> getClientHandler() {
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
        return chemicalHolder.is(((ChemicalCamoContent) obj).chemicalHolder.getKey());
    }

    @Override
    public String toString() {
        return "ChemicalCamoContent{" + getCamoId() + "}";
    }
}
