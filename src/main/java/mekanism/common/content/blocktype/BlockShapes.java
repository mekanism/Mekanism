package mekanism.common.content.blocktype;

import static mekanism.common.util.VoxelShapeUtils.setShape;
import static net.minecraft.block.Block.makeCuboidShape;

import mekanism.common.util.EnumUtils;
import mekanism.common.util.VoxelShapeUtils;
import net.minecraft.util.Direction;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.shapes.VoxelShape;

public final class BlockShapes {

    private BlockShapes() {
    }

    public static final VoxelShape[] ELECTROLYTIC_SEPARATOR = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] DIGITAL_MINER = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] CHEMICAL_CRYSTALLIZER = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] PRESSURIZED_REACTION_CHAMBER = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] METALLURGIC_INFUSER = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] CHEMICAL_WASHER = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] CHEMICAL_OXIDIZER = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] CHEMICAL_INFUSER = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] CHEMICAL_DISSOLUTION_CHAMBER = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] ROTARY_CONDENSENTRATOR = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] FLUIDIC_PLENISHER = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] ELECTRIC_PUMP = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] SOLAR_NEUTRON_ACTIVATOR = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] CHARGEPAD = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] FLUID_TANK = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] LASER = new VoxelShape[EnumUtils.DIRECTIONS.length];
    public static final VoxelShape[] LASER_AMPLIFIER = new VoxelShape[EnumUtils.DIRECTIONS.length];
    public static final VoxelShape[] RESISTIVE_HEATER = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] SEISMIC_VIBRATOR = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] PERSONAL_CHEST = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] QUANTUM_ENTANGLOPORTER = new VoxelShape[EnumUtils.DIRECTIONS.length];
    public static final VoxelShape[] LOGISTICAL_SORTER = new VoxelShape[EnumUtils.DIRECTIONS.length];
    public static final VoxelShape[] SECURITY_DESK = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] CHEMICAL_TANK = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] INDUSTRIAL_ALARM = new VoxelShape[EnumUtils.DIRECTIONS.length];
    public static final VoxelShape[] QIO_DASHBOARD = new VoxelShape[EnumUtils.DIRECTIONS.length];
    public static final VoxelShape[] QIO_DRIVE_ARRAY = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] QIO_IMPORTER = new VoxelShape[EnumUtils.DIRECTIONS.length];
    public static final VoxelShape[] QIO_EXPORTER = new VoxelShape[EnumUtils.DIRECTIONS.length];
    public static final VoxelShape[] QIO_REDSTONE_ADAPTER = new VoxelShape[EnumUtils.DIRECTIONS.length];
    public static final VoxelShape[] RADIOACTIVE_WASTE_BARREL = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] MODIFICATION_STATION = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] ISOTOPIC_CENTRIFUGE = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] SUPERCHARGED_COIL = new VoxelShape[EnumUtils.DIRECTIONS.length];
    public static final VoxelShape[] ANTIPROTONIC_NUCLEOSYNTHESIZER = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];

    static {
        setShape(VoxelShapeUtils.rotate(VoxelShapeUtils.combine(
              makeCuboidShape(0, 0, 0, 16, 4, 16), // base
              makeCuboidShape(15, 3, 3, 16, 13, 13), // portToggle1
              makeCuboidShape(0, 4, 4, 1, 12, 12), // portToggle2a
              makeCuboidShape(4, 4, 0, 12, 12, 1), // portToggle3a
              makeCuboidShape(4, 4, 15, 12, 12, 16), // portToggle4a
              makeCuboidShape(1, 4, 7, 3, 11, 9), // portToggle2b
              makeCuboidShape(7, 4, 1, 8, 11, 3), // portToggle3b
              makeCuboidShape(7, 4, 13, 8, 11, 15), // portToggle4b
              makeCuboidShape(8, 4, 0, 16, 16, 16), // tank1
              makeCuboidShape(0, 4, 9, 7, 14, 16), // tank2
              makeCuboidShape(0, 4, 0, 7, 14, 7), // tank3
              makeCuboidShape(6.5, 10, 7.5, 9.5, 11, 8.5), // tube1
              makeCuboidShape(3, 12, 7.5, 7, 13, 8.5), // tube2
              makeCuboidShape(3, 12, 7.5, 4, 15, 8.5), // tube3
              makeCuboidShape(3, 15, 3, 4, 16, 13), // tube4
              makeCuboidShape(3, 14, 3, 4, 15, 4), // tube5
              makeCuboidShape(3, 14, 12, 4, 15, 13)// tube6
        ), Rotation.CLOCKWISE_90), ELECTROLYTIC_SEPARATOR);

        setShape(VoxelShapeUtils.combine(
              makeCuboidShape(16, 18, -13, 30, 28, -11), // monitor1
              makeCuboidShape(17, 19, -13.01, 29, 27, -13.01), // monitor1_on
              makeCuboidShape(17, 20, -11, 29, 26, -10), // monitor_back1
              makeCuboidShape(17, 17.5, -12.005, 18, 18.5, -11.005), // led1
              makeCuboidShape(1, 18, -13, 15, 28, -11), // monitor2
              makeCuboidShape(2, 19, -13.01, 14, 27, -13.01), // monitor2_on
              makeCuboidShape(2, 20, -11, 14, 26, -10), // monitor_back2
              makeCuboidShape(2, 17.5, -12.005, 3, 18.5, -11.005), // led2
              makeCuboidShape(-14, 18, -13, 0, 28, -11), // monitor3
              makeCuboidShape(-13, 19, -13.01, -1, 27, -13.01), // monitor3_on
              makeCuboidShape(-13, 20, -11, -1, 26, -10), // monitor_back3
              makeCuboidShape(-13, 17.5, -12.005, -12, 18.5, -11.005), // led3
              makeCuboidShape(-2, 22, -11.95, 2, 24, -10.95), // monitorBar1
              makeCuboidShape(14, 22, -11.95, 18, 24, -10.95), // monitorBar2
              makeCuboidShape(4, 22, -11, 6, 24, -9), // monitorMount1
              makeCuboidShape(10, 22, -11, 12, 24, -9), // monitorMount2
              makeCuboidShape(5, 11, -14, 6, 13, -9), // keyboard_support1
              makeCuboidShape(10, 11, -14, 11, 13, -9), // keyboard_support2
              makeCuboidShape(4, 11.5, -13.5, 12, 12.5, -10.5), // keyboardBottom
              makeCuboidShape(3, 12.5, -14.5, 13, 13.5, -9.5), // keyboard
              makeCuboidShape(-8, 3, -9, 24, 32, 3), // frame1
              makeCuboidShape(-8, 26.99, 4, 24, 31.99, 19), // frame2a
              makeCuboidShape(-8, 21, 4, 24, 26, 19), // frame2b
              makeCuboidShape(-8, 15, 4, 24, 20, 19), // frame2c
              makeCuboidShape(-8, 9, 4, 24, 14, 19), // frame2d
              makeCuboidShape(-8, 3, 4, 24, 8, 19), // frame2e
              makeCuboidShape(-8, 3, 19.99, 24, 32, 31.99), // frame3
              makeCuboidShape(-7, 15, 3, 23, 31, 20), // core_top
              makeCuboidShape(-7, 4, 3, 23, 15, 20), // core_bottom
              makeCuboidShape(-9, 24, -6, -8, 29, 0), // bracket_east1
              makeCuboidShape(-13, 24, -8, -8, 29, -6), // bracket_north1
              makeCuboidShape(-13, 24, 0, -8, 29, 2), // bracket_south1
              makeCuboidShape(-9, 24, 23, -8, 29, 29), // bracket_east2
              makeCuboidShape(-13, 24, 21, -8, 29, 23), // bracket_north2
              makeCuboidShape(-13, 24, 29, -8, 29, 31), // bracket_south2
              makeCuboidShape(24, 24, -6, 25, 29, 0), // bracket_west1
              makeCuboidShape(24, 24, 0, 29, 29, 2), // bracket_south3
              makeCuboidShape(24, 24, -8, 29, 29, -6), // bracket_north3
              makeCuboidShape(24, 24, 23, 25, 29, 29), // bracket_west2
              makeCuboidShape(24, 24, 29, 29, 29, 31), // bracket_south4
              makeCuboidShape(24, 24, 21, 29, 29, 23), // bracket_north4
              makeCuboidShape(5, 2, -6, 11, 4, 5), // power_cable1a
              makeCuboidShape(5, 1, 5, 11, 3, 11), // power_cable1b
              makeCuboidShape(4, 0, 4, 12, 1, 12), // port1
              makeCuboidShape(23, 5, 5, 31, 11, 11), // power_cable2
              makeCuboidShape(30.99, 4, 4, 31.99, 12, 12), // port2a
              makeCuboidShape(24, 4, 4, 25, 12, 12), // port2b
              makeCuboidShape(-15, 5, 5, -7, 11, 11), // power_cable3
              makeCuboidShape(-15.99, 4, 4, -14.99, 12, 12), // port3a
              makeCuboidShape(-9, 4, 4, -8, 12, 12), // port3b
              makeCuboidShape(-14, 2, -7, -10, 30, 1), // beam1
              makeCuboidShape(-15, 0, -8, -8, 2, 2), // foot1
              makeCuboidShape(-14, 2, 22, -10, 30, 30), // beam2
              makeCuboidShape(-15, 0, 21, -8, 2, 31), // foot2
              makeCuboidShape(26.5, 2, 22, 30.5, 30, 30), // beam3
              makeCuboidShape(24.5, 0, 21, 31.5, 2, 31), // foot3
              makeCuboidShape(26.5, 2, -7, 30.5, 30, 1), // beam4
              makeCuboidShape(24.5, 0, -8, 31.5, 2, 2), // foot4
              makeCuboidShape(4, 20, 30.993, 12, 28, 31.993), // port_back
              makeCuboidShape(4, 30.993, 4, 12, 31.993, 12) // port_top
        ), DIGITAL_MINER);

        setShape(VoxelShapeUtils.combine(
              makeCuboidShape(0, 5, 14, 16, 7, 16), // rim_south
              makeCuboidShape(0, 5, 2, 2, 7, 14), // rim_west
              makeCuboidShape(0, 5, 0, 16, 7, 2), // rim_north
              makeCuboidShape(14, 5, 2, 16, 7, 14), // rim_east
              makeCuboidShape(3, 5, 3, 13, 6, 13), // tray
              makeCuboidShape(-0.005, 4, 4, 0.995, 12, 12), // port_west
              makeCuboidShape(-0.01, 10, 5, 0.99, 11, 11), // port_west_led1
              makeCuboidShape(-0.01, 5, 5, 0.99, 6, 11), // port_west_led2
              makeCuboidShape(-0.01, 6, 5, 0.99, 10, 6), // port_west_led3
              makeCuboidShape(-0.01, 6, 10, 0.99, 10, 11), // port_west_led4
              makeCuboidShape(15.005, 4, 4, 16.005, 12, 12), // port_east
              makeCuboidShape(0, 7, 0, 1, 11, 1), // support1
              makeCuboidShape(0, 7, 15, 1, 11, 16), // support2
              makeCuboidShape(15, 7, 15, 16, 11, 16), // support3
              makeCuboidShape(15, 7, 0, 16, 11, 1), // support4
              makeCuboidShape(0, 0, 0, 16, 5, 16), // base
              makeCuboidShape(0, 11, 0, 16, 16, 16), // tank
              makeCuboidShape(6, 9, 8, 7, 11, 9), // rod1
              makeCuboidShape(9, 8, 9, 10, 11, 10), // rod2
              makeCuboidShape(7.5, 7, 6, 8.5, 11, 7), // rod3
              makeCuboidShape(1, 7, 1, 15, 11, 15) // glass
        ), CHEMICAL_CRYSTALLIZER);

        setShape(VoxelShapeUtils.combine(
              makeCuboidShape(0, 4, 6, 16, 16, 16), // body
              makeCuboidShape(10, 4, 1, 12, 15, 6), // front_div_1
              makeCuboidShape(13, 4, 1, 15, 15, 6), // front_div_2
              makeCuboidShape(1, 4, 2, 10, 14, 6), // front
              makeCuboidShape(0, 0, 0, 16, 4, 16), // base
              makeCuboidShape(12, 13, 2, 13, 14, 6), // bar1
              makeCuboidShape(12, 11, 2, 13, 12, 6), // bar2
              makeCuboidShape(12, 9, 2, 13, 10, 6), // bar3
              makeCuboidShape(12, 7, 2, 13, 8, 6), // bar4
              makeCuboidShape(12, 5, 2, 13, 6, 6) // bar5
        ), PRESSURIZED_REACTION_CHAMBER);

        setShape(VoxelShapeUtils.combine(
              makeCuboidShape(11, 11, 5, 12, 16, 8), // connector1
              makeCuboidShape(4, 11, 5, 5, 16, 8), // connector2
              makeCuboidShape(2, 7, 2, 14, 8, 8), // divider
              makeCuboidShape(0, 0, 0, 16, 4, 16), // base
              makeCuboidShape(0, 4, 8, 16, 16, 16), // back
              makeCuboidShape(1, 12, 1, 15, 15, 8), // plate_top
              makeCuboidShape(1, 8, 1, 15, 11, 8), // plate_middle
              makeCuboidShape(1, 4, 1, 15, 7, 8), // plate_bottom
              makeCuboidShape(13, 11, 2, 14, 12, 3), // bar1
              makeCuboidShape(2, 11, 2, 3, 12, 3) // bar2
        ), METALLURGIC_INFUSER);

        setShape(VoxelShapeUtils.combine(
              makeCuboidShape(0, 0, 0, 16, 4, 16), // base
              makeCuboidShape(4, 13, 4, 12, 15, 12), // pipe1
              makeCuboidShape(5, 4, 5, 11, 13, 10), // pipe2
              makeCuboidShape(0, 4, 0, 7, 14, 8), // tank1
              makeCuboidShape(9, 4, 0, 16, 14, 8), // tank2
              makeCuboidShape(0, 4, 10, 16, 14, 16), // tank3
              makeCuboidShape(13, 5, 8, 15, 11, 10), // connector1
              makeCuboidShape(7, 7, 3, 9, 9, 5), // conduit
              makeCuboidShape(7, 6, 1, 9, 7, 2), // bridge4
              makeCuboidShape(7, 8, 1, 9, 9, 2), // bridge3
              makeCuboidShape(7, 10, 1, 9, 11, 2), // bridge2
              makeCuboidShape(7, 12, 1, 9, 13, 2), // bridge1
              makeCuboidShape(1, 14, 4, 2, 15, 5), // vent1
              makeCuboidShape(1, 14, 6, 2, 15, 7), // vent2
              makeCuboidShape(13, 14, 4, 14, 15, 12), // tubeLeft2
              makeCuboidShape(1, 5, 8, 3, 11, 10), // connector2
              makeCuboidShape(3, 15, 3, 13, 16, 13), // port_up
              makeCuboidShape(-0.01, 4, 4, 0.99, 12, 12), // port_west
              makeCuboidShape(15.01, 4, 4, 16.01, 12, 12) // port_east
        ), CHEMICAL_WASHER);

        setShape(VoxelShapeUtils.combine(
              makeCuboidShape(9, 4, 2, 13, 5, 14), // stand
              makeCuboidShape(8, 5, 7, 14, 16, 15), // tower2
              makeCuboidShape(8, 5, 1, 14, 16, 6), // tower1
              makeCuboidShape(7, 7, 9, 8, 10, 12), // pipe1
              makeCuboidShape(0, 4, 0, 7, 16, 16), // tank
              makeCuboidShape(-0.005, 4, 4, 0.995, 12, 12), // connectorToggle
              makeCuboidShape(9, 6, 6, 13, 15, 7), // bridge
              makeCuboidShape(13, 5, 5, 15, 11, 11), // pipe2
              makeCuboidShape(15.005, 3, 3, 16.005, 13, 13), // connector
              makeCuboidShape(0, 0, 0, 16, 4, 16) // base
        ), CHEMICAL_OXIDIZER);

        setShape(VoxelShapeUtils.combine(
              makeCuboidShape(-0.01, 4, 4, 0.99, 12, 12), // portRight
              makeCuboidShape(15.01, 4, 4, 16.01, 12, 12), // portLeft
              makeCuboidShape(2, 5, 1, 14, 12, 8), // tank3
              makeCuboidShape(9, 13.5, 4, 10, 14.5, 6), // tube4
              makeCuboidShape(7, 5, 13, 9, 11, 15), // connector1
              makeCuboidShape(3, 5, 6, 13, 11, 9), // connector2
              makeCuboidShape(8, 8, 9, 9, 9, 13), // connector3
              makeCuboidShape(1, 5, 5, 2, 11, 9), // pipe2
              makeCuboidShape(6, 13.5, 4, 7, 14.5, 6), // tube5
              makeCuboidShape(7, 14, 13, 9, 15, 14), // tube10
              makeCuboidShape(11, 13.5, 6.5, 13, 14.5, 7.5), // tube2
              makeCuboidShape(4, 4, -0.01, 12, 12, 0.99), // portFront
              makeCuboidShape(0, 0, 0, 16, 5, 16), // base
              makeCuboidShape(3, 13.5, 7.5, 4, 14.5, 9.5), // tube8
              makeCuboidShape(14, 5, 5, 15, 11, 9), // pipe1
              makeCuboidShape(12, 13.5, 7.5, 13, 14.5, 9.5), // tube1
              makeCuboidShape(6, 11.5, 4, 7, 13.5, 5), // tube6
              makeCuboidShape(4, 4, 15.01, 12, 12, 16.01), // portBack
              makeCuboidShape(3, 13.5, 6.5, 5, 14.5, 7.5), // tube7
              makeCuboidShape(7, 14, 10, 9, 15, 11), // tube9
              makeCuboidShape(5, 12.5, 5.5, 11, 15.5, 8.5), // compressor
              makeCuboidShape(9, 11.5, 4, 10, 13.5, 5), // tube3
              makeCuboidShape(11, 12, 2, 12, 13, 3), // exhaust1
              makeCuboidShape(9, 12, 2, 10, 13, 3), // exhaust2
              makeCuboidShape(6, 12, 2, 7, 13, 3), // exhaust3
              makeCuboidShape(4, 12, 2, 5, 13, 3), // exhaust4
              makeCuboidShape(9, 5, 9, 15, 16, 15), // tank1
              makeCuboidShape(1, 5, 9, 7, 16, 15) // tank2
        ), CHEMICAL_INFUSER);

        setShape(VoxelShapeUtils.rotate(VoxelShapeUtils.combine(
              makeCuboidShape(0, 0, 0, 16, 7, 16), // base
              makeCuboidShape(1, 7, 0, 15, 15, 2), // back
              makeCuboidShape(1, 7, 2, 15, 12, 15), // glass
              makeCuboidShape(4, 13, 2, 12, 15, 12), // vents
              makeCuboidShape(0, 15, 0, 16, 16, 16), // top
              makeCuboidShape(0, 12, 1, 16, 13, 16), // top2
              makeCuboidShape(15, 7, 0, 16, 15, 1), // backEdge1
              makeCuboidShape(0, 7, 0, 1, 15, 1), // backEdge2
              makeCuboidShape(14, 13, 14, 15, 15, 15), // support1
              makeCuboidShape(1, 13, 14, 2, 15, 15), // support2
              makeCuboidShape(0, 3, 3, 1, 13, 13), // portToggle1
              makeCuboidShape(15, 4, 4, 16, 12, 12)// portToggle2
        ), Rotation.CLOCKWISE_180), CHEMICAL_DISSOLUTION_CHAMBER);

        setShape(VoxelShapeUtils.combine(
              makeCuboidShape(0, 0, 0, 16, 5, 16), // base
              makeCuboidShape(0, 15, 0, 16, 16, 16), // top
              makeCuboidShape(0, 13, 0, 16, 14, 16), // middle
              makeCuboidShape(7.5, 11, 7.5, 8.5, 13, 8.5), // shaft
              makeCuboidShape(4, 14, 4, 12, 15, 12), // bridge
              makeCuboidShape(7, 5, 5, 9, 11, 11), // pipe
              makeCuboidShape(9, 5, 1, 15, 13, 15), // tankLeft
              makeCuboidShape(1, 5, 1, 7, 13, 15), // tankRight
              makeCuboidShape(15, 4, 4, 16, 12, 12), // portLeft
              makeCuboidShape(0, 3, 3, 1, 13, 13), // portRight
              makeCuboidShape(14, 14, 14, 15, 15, 15), // support1
              makeCuboidShape(14, 14, 1, 15, 15, 2), // support2
              makeCuboidShape(1, 14, 1, 2, 15, 2), // support3
              makeCuboidShape(1, 14, 14, 2, 15, 15), // support4
              makeCuboidShape(7, 11, 2, 9, 12, 3), // tube1
              makeCuboidShape(7, 9, 2, 9, 10, 3), // tube2
              makeCuboidShape(7, 7, 2, 9, 8, 3), // tube3
              makeCuboidShape(7, 5, 2, 9, 6, 3), // tube4
              makeCuboidShape(7, 7, 13, 9, 8, 14), // tube5
              makeCuboidShape(7, 9, 13, 9, 10, 14), // tube6
              makeCuboidShape(7, 11, 13, 9, 12, 14), // tube7
              makeCuboidShape(7, 5, 13, 9, 6, 14)// tube8
        ), ROTARY_CONDENSENTRATOR);

        setShape(VoxelShapeUtils.combine(
              makeCuboidShape(3, 15, 3, 13, 16, 13), // portTop
              makeCuboidShape(4, 4, 15, 12, 12, 16), // portBack
              makeCuboidShape(3.5, 1, 3.5, 12.5, 13, 12.5), // tank
              makeCuboidShape(5.5, 5.5, 11, 10.5, 10.5, 15), // Connector
              makeCuboidShape(4.5, 4.5, 13, 11.5, 11.5, 14), // connectorRing
              makeCuboidShape(2.5, 13, 2.5, 13.5, 14, 13.5), // ringTank
              makeCuboidShape(4, 0, 4, 12, 1, 12), // ringBottom
              makeCuboidShape(4, 14, 4, 12, 15, 12), // ringTop
              makeCuboidShape(12, 6, 6, 13, 10, 10), // bearingLeft
              makeCuboidShape(3, 6, 6, 4, 10, 10), // bearingRight
              makeCuboidShape(10, 10, 12, 11, 11, 15), // rod1
              makeCuboidShape(5, 10, 12, 6, 11, 15), // rod2
              makeCuboidShape(10, 5, 12, 11, 6, 15), // rod3
              makeCuboidShape(5, 5, 12, 6, 6, 15)// rod4
        ), FLUIDIC_PLENISHER);

        setShape(VoxelShapeUtils.rotate(VoxelShapeUtils.combine(
              makeCuboidShape(4, 1, 4, 12, 12, 12), // pumpCasing
              makeCuboidShape(10, 10, 1, 11, 11, 5), // powerConnectorFrame1
              makeCuboidShape(5, 10, 1, 6, 11, 5), // powerConnectorFrame2
              makeCuboidShape(5, 5, 1, 6, 6, 5), // powerConnectorFrame3
              makeCuboidShape(10, 5, 1, 11, 6, 5), // powerConnectorFrame4
              makeCuboidShape(5, 0, 5, 11, 15, 11), // pumpBase
              makeCuboidShape(4, 15, 4, 12, 16, 12), // pumpPortTop
              makeCuboidShape(4, 13, 4, 12, 14, 12), // pumpRingTop
              makeCuboidShape(6, 6, 1, 10, 10, 4), // powerConnector
              makeCuboidShape(4, 4, 0, 12, 12, 1) // powerPort
        ), Rotation.CLOCKWISE_180), ELECTRIC_PUMP);

        setShape(VoxelShapeUtils.combine(
              makeCuboidShape(6, 14, 14, 10, 29, 16),
              makeCuboidShape(4, 4, 0, 12, 12, 1),
              makeCuboidShape(2, 4, 2, 14, 5, 15),
              makeCuboidShape(5, 14, 12, 6, 15, 13),
              makeCuboidShape(5, 15, 10, 11, 16, 11),
              makeCuboidShape(5, 14, 10, 6, 15, 11),
              makeCuboidShape(5, 15, 12, 11, 16, 13),
              makeCuboidShape(10, 14, 12, 11, 15, 13),
              makeCuboidShape(10, 14, 10, 11, 15, 11),
              makeCuboidShape(7, 13.5, 7, 9, 14.5, 14),
              makeCuboidShape(0, 5, 0, 16, 14, 16),
              makeCuboidShape(0, 0, 0, 16, 4, 16),
              makeCuboidShape(0.5, 4, 0.5, 1.5, 5, 1.5),
              makeCuboidShape(0.5, 4, 14.5, 1.5, 5, 15.5),
              makeCuboidShape(0.5, 4, 2.5, 1.5, 5, 3.5),
              makeCuboidShape(0.5, 4, 4.5, 1.5, 5, 5.5),
              makeCuboidShape(0.5, 4, 6.5, 1.5, 5, 7.5),
              makeCuboidShape(0.5, 4, 8.5, 1.5, 5, 9.5),
              makeCuboidShape(0.5, 4, 10.5, 1.5, 5, 11.5),
              makeCuboidShape(0.5, 4, 12.5, 1.5, 5, 13.5),
              makeCuboidShape(14.5, 4, 14.5, 15.5, 5, 15.5),
              makeCuboidShape(14.5, 4, 12.5, 15.5, 5, 13.5),
              makeCuboidShape(14.5, 4, 10.5, 15.5, 5, 11.5),
              makeCuboidShape(14.5, 4, 8.5, 15.5, 5, 9.5),
              makeCuboidShape(14.5, 4, 6.5, 15.5, 5, 7.5),
              makeCuboidShape(14.5, 4, 4.5, 15.5, 5, 5.5),
              makeCuboidShape(14.5, 4, 2.5, 15.5, 5, 3.5),
              makeCuboidShape(14.5, 4, 0.5, 15.5, 5, 1.5),
              makeCuboidShape(5, 4, 1, 11, 5, 2),
              //Rough estimates of slanted things
              makeCuboidShape(6, 14, 1, 7, 14.75, 3),
              makeCuboidShape(9, 14, 1, 10, 14.75, 3),
              makeCuboidShape(5, 14, 3, 11, 15.25, 4.5),
              makeCuboidShape(5, 14, 4.5, 11, 15, 6),
              makeCuboidShape(5, 14, 6, 11, 14.875, 7.5),
              makeCuboidShape(5, 14, 7.5, 11, 14.75, 9),
              makeCuboidShape(6.5, 14, 4.5, 9.5, 16, 5.5),
              makeCuboidShape(6.5, 14, 5.5, 7.5, 16, 6.5),
              makeCuboidShape(8.5, 14, 5.5, 9.5, 16, 6.5),
              makeCuboidShape(6.5, 14, 6.5, 9.5, 16, 7.5),
              //Top center
              makeCuboidShape(7, 26, 10, 9, 26.5, 14),
              makeCuboidShape(7, 26.5, 5.75, 9, 29, 14),
              makeCuboidShape(7.5, 25.75, 6.625, 8.5, 26.5, 7.625),
              makeCuboidShape(5, 29.5, 0, 11, 30.5, 1),
              makeCuboidShape(5, 28.5, 1, 11, 30.5, 4.5),
              makeCuboidShape(5, 28, 4.5, 11, 30, 8),
              makeCuboidShape(5, 27.75, 8, 11, 29.5, 11.5),
              makeCuboidShape(5, 27.25, 11.5, 11, 29, 15),
              //Left Side panel
              makeCuboidShape(11, 30, 0, 12.25, 31, 4),
              makeCuboidShape(11, 29.5, 4, 12.25, 30.5, 8),
              makeCuboidShape(11, 29, 8, 12.25, 30, 12),
              makeCuboidShape(11, 28.5, 12, 12.25, 29.5, 16.1),
              makeCuboidShape(12.25, 30.5, 0, 14.75, 31.5, 4),
              makeCuboidShape(12.25, 30, 4, 14.75, 31, 8),
              makeCuboidShape(12.25, 29.5, 8, 14.75, 30.5, 12),
              makeCuboidShape(12.25, 29, 12, 14.75, 30, 16.1),
              makeCuboidShape(14.75, 31, 0.25, 16.5, 32.25, 4),
              makeCuboidShape(14.75, 30.5, 4, 16.5, 31.5, 8),
              makeCuboidShape(14.75, 30, 8, 16.5, 31, 12),
              makeCuboidShape(14.75, 29.5, 12, 16.5, 30.5, 16.1),
              //Right Side panel
              makeCuboidShape(3.75, 30, 0, 5, 31, 4),
              makeCuboidShape(3.75, 29.5, 4, 5, 30.5, 8),
              makeCuboidShape(3.75, 29, 8, 5, 30, 12),
              makeCuboidShape(3.75, 28.5, 12, 5, 29.5, 16.1),
              makeCuboidShape(1.25, 30.5, 0, 3.75, 31.5, 4),
              makeCuboidShape(1.25, 30, 4, 3.75, 31, 8),
              makeCuboidShape(1.25, 29.5, 8, 3.75, 30.5, 12),
              makeCuboidShape(1.25, 29, 12, 3.75, 30, 16.1),
              makeCuboidShape(-0.5, 31, 0.25, 1.25, 32.25, 4),
              makeCuboidShape(-0.5, 30.5, 4, 1.25, 31.5, 8),
              makeCuboidShape(-0.5, 30, 8, 1.25, 31, 12),
              makeCuboidShape(-0.5, 29.5, 12, 1.25, 30.5, 16.1)
        ), SOLAR_NEUTRON_ACTIVATOR);

        setShape(VoxelShapeUtils.combine(
              makeCuboidShape(10, 1, 11.99, 12, 8, 13.99),//pillar1
              makeCuboidShape(4, 1, 11.99, 6, 8, 13.99),//pillar2
              makeCuboidShape(0, 0, 0, 16, 1, 16),//base
              makeCuboidShape(5, 5, 14, 11, 11, 15),//connector
              makeCuboidShape(5, 1, 13, 11, 11, 14),//stand
              makeCuboidShape(7, 4, 11, 9, 5, 13),//plug
              makeCuboidShape(4, 4, 15, 12, 12, 16),//port
              makeCuboidShape(5, 5, 15, 11, 11, 16)//port_ring
        ), CHARGEPAD);

        setShape(VoxelShapeUtils.combine(
              makeCuboidShape(5, 2, 4, 11, 9, 12), // body
              makeCuboidShape(8.995, 10, 7.5, 9.995, 13, 8.5), // wire
              makeCuboidShape(11, 8, 4, 12, 9, 12), // fin1
              makeCuboidShape(11, 6, 4, 12, 7, 12), // fin2
              makeCuboidShape(11, 4, 4, 12, 5, 12), // fin3
              makeCuboidShape(11, 2, 4, 12, 3, 12), // fin4
              makeCuboidShape(4, 8, 4, 5, 9, 12), // fin5
              makeCuboidShape(4, 6, 4, 5, 7, 12), // fin6
              makeCuboidShape(4, 4, 4, 5, 5, 12), // fin7
              makeCuboidShape(4, 2, 4, 5, 3, 12), // fin8
              makeCuboidShape(5, 9, 5, 11, 10, 11), // shaft
              makeCuboidShape(7, 10, 7, 9, 16, 9), // center
              makeCuboidShape(10.995, 3, 9, 11.995, 8, 10), // rod1
              makeCuboidShape(10.995, 3, 6, 11.995, 8, 7), // rod2
              makeCuboidShape(4.005, 3, 6, 5.005, 8, 7), // rod3
              makeCuboidShape(4.005, 3, 9, 5.005, 8, 10), // rod4
              makeCuboidShape(4, 0, 4, 12, 1, 12), // port
              makeCuboidShape(5, 1, 5, 11, 2, 11), // connector
              makeCuboidShape(6, 13, 6, 10, 14, 10), // ring1
              makeCuboidShape(6, 11, 6, 10, 12, 10) // ring2
        ), LASER, true, true);

        setShape(VoxelShapeUtils.combine(
              makeCuboidShape(1, 1, 1, 15, 15, 15),//Base
              makeCuboidShape(0, 3, 3, 1, 13, 13),//S1
              makeCuboidShape(3, 3, 15, 13, 13, 16),//S2
              makeCuboidShape(15, 3, 3, 16, 13, 13),//S3
              makeCuboidShape(3, 0, 3, 13, 1, 13),//S4
              makeCuboidShape(3, 3, 0, 13, 13, 1),//S5
              makeCuboidShape(3, 15, 3, 13, 16, 13)//S6
        ), LASER_AMPLIFIER, true);

        setShape(VoxelShapeUtils.combine(
              makeCuboidShape(-0.005, 4, 4, 0.995, 12, 12), // port_left
              makeCuboidShape(-0.01, 5, 5, 0.99, 11, 11), // port_left_led
              makeCuboidShape(15.005, 4, 4, 16.005, 12, 12), // port_right
              makeCuboidShape(15.01, 5, 5, 16.01, 11, 11), // port_right_led
              makeCuboidShape(4, 13, 1.5, 5, 14, 14.5), // bar1
              makeCuboidShape(6, 13, 1.5, 7, 14, 14.5), // bar2
              makeCuboidShape(9, 13, 1.5, 10, 14, 14.5), // bar3
              makeCuboidShape(11, 13, 1.5, 12, 14, 14.5), // bar4
              makeCuboidShape(0, 0, 0, 16, 7, 16), // base
              makeCuboidShape(13, 7, 0, 16, 16, 16), // wall_right
              makeCuboidShape(0, 7, 0, 3, 16, 16), // wall_left
              makeCuboidShape(3, 6, 1, 13, 15, 2), // fin1
              makeCuboidShape(3, 6, 3, 13, 15, 4), // fin2
              makeCuboidShape(3, 6, 5, 13, 15, 6), // fin3
              makeCuboidShape(3, 6, 7, 13, 15, 8), // fin4
              makeCuboidShape(3, 6, 8, 13, 15, 9), // fin5
              makeCuboidShape(3, 6, 10, 13, 15, 11), // fin6
              makeCuboidShape(3, 6, 12, 13, 15, 13), // fin7
              makeCuboidShape(3, 6, 14, 13, 15, 15) // fin8
        ), RESISTIVE_HEATER);

        setShape(VoxelShapeUtils.combine(
              makeCuboidShape(4, 0, 4, 12, 2, 12),
              makeCuboidShape(0, 0, 13, 16, 5, 16),
              makeCuboidShape(5, 25, 5, 11, 29, 15),
              makeCuboidShape(4, 4, 15, 12, 12, 16),
              makeCuboidShape(0.5, 5, 14.5, 1.5, 30, 15.5),
              makeCuboidShape(6.5, 18, 6.5, 9.5, 29, 9.5),
              makeCuboidShape(7, 3, 7, 9, 18, 9),
              makeCuboidShape(6, 1, 6, 10, 3, 10),
              makeCuboidShape(6, 15, 6, 10, 17, 10),
              makeCuboidShape(6.5, 15, 10, 9.5, 17, 14),
              makeCuboidShape(0, 30, 0, 16, 32, 16),
              makeCuboidShape(0.5, 5, 0.5, 1.5, 30, 1.5),
              makeCuboidShape(0, 0, 3, 3, 5, 13),
              makeCuboidShape(0, 0, 0, 16, 5, 3),
              makeCuboidShape(13, 0, 3, 16, 5, 13),
              makeCuboidShape(0.5, 17, 1.5, 1.5, 18, 14.5),
              makeCuboidShape(14.5, 5, 0.5, 15.5, 30, 1.5),
              makeCuboidShape(1.5, 17, 14.5, 14.5, 18, 15.5),
              makeCuboidShape(14.5, 5, 14.5, 15.5, 30, 15.5),
              makeCuboidShape(14.5, 17, 1.5, 15.5, 18, 14.5),
              makeCuboidShape(6, 5, 14, 10, 30, 16),
              makeCuboidShape(3, 29, 3, 13, 30, 15),
              makeCuboidShape(3.5, 28.5, 11.5, 4.5, 29.5, 12.5),
              makeCuboidShape(11.5, 28.5, 11.5, 12.5, 29.5, 12.5),
              makeCuboidShape(11.5, 28.5, 3.5, 12.5, 29.5, 4.5),
              makeCuboidShape(3.5, 28.5, 3.5, 4.5, 29.5, 4.5),
              makeCuboidShape(11.5, 28.5, 5.5, 12.5, 29.5, 6.5),
              makeCuboidShape(3.5, 28.5, 5.5, 4.5, 29.5, 6.5),
              makeCuboidShape(11.5, 28.5, 7.5, 12.5, 29.5, 8.5),
              makeCuboidShape(3.5, 28.5, 7.5, 4.5, 29.5, 8.5),
              makeCuboidShape(11.5, 28.5, 9.5, 12.5, 29.5, 10.5),
              makeCuboidShape(3.5, 28.5, 9.5, 4.5, 29.5, 10.5),
              //Walls uses full walls instead of angles because even though we have code to calculate the proper angles
              // it causes lag when looking at the overly complicated bounding box
              makeCuboidShape(0.5, 0, 14.5, 15.5, 32, 15.5),
              makeCuboidShape(14.5, 0, 0.5, 15.5, 32, 15.5),
              makeCuboidShape(0.5, 0, 0.5, 1.5, 32, 15.5)
        ), SEISMIC_VIBRATOR);

        setShape(VoxelShapeUtils.combine(
              makeCuboidShape(1, 0, 1, 15, 14, 15),//Main chest
              makeCuboidShape(7, 7, 0, 9, 11, 1)//latch
        ), PERSONAL_CHEST);

        setShape(VoxelShapeUtils.combine(
              makeCuboidShape(4, 4, 0, 12, 12, 1),//portFront
              makeCuboidShape(0, 4, 4, 1, 12, 12),//portRight
              makeCuboidShape(15, 4, 4, 16, 12, 12),//portLeft
              makeCuboidShape(4, 15, 4, 12, 16, 12),//portTop
              makeCuboidShape(4, 0, 4, 12, 1, 12),//portBottom
              makeCuboidShape(4, 4, 15, 12, 12, 16),//portBack
              makeCuboidShape(13, 13, 0, 16, 16, 3),//corner1
              makeCuboidShape(0, 13, 0, 3, 16, 3),//corner2
              makeCuboidShape(13, 13, 13, 16, 16, 16),//corner3
              makeCuboidShape(0, 13, 13, 3, 16, 16),//corner4
              makeCuboidShape(13, 0, 0, 16, 3, 3),//corner5
              makeCuboidShape(0, 0, 0, 3, 3, 3),//corner6
              makeCuboidShape(13, 0, 13, 16, 3, 16),//corner7
              makeCuboidShape(0, 0, 13, 3, 3, 16),//corner8
              makeCuboidShape(13, 3, 1, 15, 13, 3),//frame1
              makeCuboidShape(1, 3, 1, 3, 13, 3),//frame2
              makeCuboidShape(13, 3, 13, 15, 13, 15),//frame3
              makeCuboidShape(1, 3, 13, 3, 13, 15),//frame4
              makeCuboidShape(3, 1, 1, 13, 3, 3),//frame5
              makeCuboidShape(13, 1, 3, 15, 3, 13),//frame6
              makeCuboidShape(1, 1, 3, 3, 3, 13),//frame7
              makeCuboidShape(3, 1, 13, 13, 3, 15),//frame8
              makeCuboidShape(3, 13, 1, 13, 15, 3),//frame9
              makeCuboidShape(13, 13, 3, 15, 15, 13),//frame10
              makeCuboidShape(1, 13, 3, 3, 15, 13),//frame11
              makeCuboidShape(3, 13, 13, 13, 15, 15),//frame12
              makeCuboidShape(14.5, 3, 0.5, 15.5, 13, 1.5),//frameEdge1
              makeCuboidShape(0.5, 3, 0.5, 1.5, 13, 1.5),//frameEdge2
              makeCuboidShape(14.5, 3, 14.5, 15.5, 13, 15.5),//frameEdge3
              makeCuboidShape(0.5, 3, 14.5, 1.5, 13, 15.5),//frameEdge4
              makeCuboidShape(3, 0.5, 0.5, 13, 1.5, 1.5),//frameEdge5
              makeCuboidShape(14.5, 0.5, 3, 15.5, 1.5, 13),//frameEdge6
              makeCuboidShape(0.5, 0.5, 3, 1.5, 1.5, 13),//frameEdge7
              makeCuboidShape(3, 0.5, 14.5, 13, 1.5, 15.5),//frameEdge8
              makeCuboidShape(3, 14.5, 0.5, 13, 15.5, 1.5),//frameEdge9
              makeCuboidShape(14.5, 14.5, 3, 15.5, 15.5, 13),//frameEdge10
              makeCuboidShape(0.5, 14.5, 3, 1.5, 15.5, 13),//frameEdge11
              makeCuboidShape(3, 14.5, 14.5, 13, 15.5, 15.5)//frameEdge12
        ), QUANTUM_ENTANGLOPORTER, true);

        setShape(VoxelShapeUtils.rotate(VoxelShapeUtils.combine(
              makeCuboidShape(2, 2, 15, 14, 14, 16), // portBackLarge
              makeCuboidShape(7, 10, 2, 9, 12, 3), // pistonBrace1
              makeCuboidShape(7, 4, 2, 9, 6, 3), // pistonBrace2
              makeCuboidShape(4, 4, 13, 12, 12, 14), // ring1
              makeCuboidShape(4, 4, 11, 12, 12, 12), // ring2
              makeCuboidShape(4, 4, 9, 12, 12, 10), // ring3
              makeCuboidShape(4, 4, 7, 12, 12, 8), // ring4
              makeCuboidShape(4, 4, 5, 12, 12, 6), // ring5
              makeCuboidShape(4, 4, 3, 12, 12, 4), // ring6
              makeCuboidShape(4, 4, 1, 12, 12, 2), // ring7
              makeCuboidShape(7, 11, 4, 9, 13, 5), // pistonConnector1
              makeCuboidShape(7, 3, 4, 9, 5, 5), // pistonConnector2
              makeCuboidShape(3, 3, 14, 13, 13, 15), // connectorBack
              makeCuboidShape(7, 11, 9.01, 9, 13, 14.01), // pistonBase1
              makeCuboidShape(7, 3, 9.01, 9, 5, 14.01), // pistonBase2
              makeCuboidShape(3, 3, 0, 13, 13, 1), // portFront
              makeCuboidShape(5, 5, 1, 11, 11, 15), // pipe
              makeCuboidShape(7, 12, 5, 9, 13, 9), // pistonBar1
              makeCuboidShape(7, 3, 5, 9, 4, 9), // pistonBar2
              makeCuboidShape(11.005, 6.5, 4, 12.005, 9.5, 11), // panel1
              makeCuboidShape(3.995, 6.5, 4, 4.995, 9.5, 11), // panel2
              makeCuboidShape(4, 4, 16, 12, 12, 17), // portBack
              makeCuboidShape(11.5, 7.5, 8, 12.5, 8.5, 9), // bulb1
              makeCuboidShape(3.5, 7.5, 8, 4.5, 8.5, 9), // bulb2
              makeCuboidShape(3.5, 7.5, 6, 4.5, 8.5, 7), // bulb3
              makeCuboidShape(11.5, 7.5, 6, 12.5, 8.5, 7) // bulb4
        ), Direction.NORTH), LOGISTICAL_SORTER, true, true);

        setShape(VoxelShapeUtils.combine(
              makeCuboidShape(0, 6, 0, 16, 13, 16), // desk_top
              makeCuboidShape(1, 5, 1, 15, 6, 15), // desk_middle
              makeCuboidShape(0, 0, 0, 16, 5, 16), // desk_base
              makeCuboidShape(1, 14, 11, 15, 24, 13), // monitor
              makeCuboidShape(2, 15, 10.99, 14, 23, 10.99), // monitor_screen_led
              makeCuboidShape(2, 13.5, 11.5, 3, 14.5, 12.5), // button_led
              makeCuboidShape(2, 16, 13, 14, 22, 14), // monitor_back
              makeCuboidShape(4, 13, 10, 12, 14, 14), // stand_base
              makeCuboidShape(7, 14, 13, 9, 21, 14), // stand_neck
              makeCuboidShape(3, 13, 2, 13, 14, 7) // keyboard
        ), SECURITY_DESK);

        setShape(VoxelShapeUtils.combine(
              makeCuboidShape(6, 13, 4, 10, 14, 5), // rim0
              makeCuboidShape(10, 13, 4, 12, 16, 5), // rim1
              makeCuboidShape(11, 13, 5, 12, 16, 11), // rim2
              makeCuboidShape(4, 13, 11, 12, 16, 12), // rim3
              makeCuboidShape(4, 13, 5, 5, 16, 11), // rim4
              makeCuboidShape(4, 13, 4, 6, 16, 5), // rim5
              makeCuboidShape(3, 1, 3, 13, 13, 13), // tank
              makeCuboidShape(4, 0, 4, 12, 1, 12), // tankBase
              makeCuboidShape(6.5, 14, 6.5, 9.5, 15, 9.5), // valve
              makeCuboidShape(7, 12, 7, 9, 14, 9) // valveBase
        ), CHEMICAL_TANK);

        setShape(VoxelShapeUtils.combine(
              makeCuboidShape(5, 15, 5, 11, 16, 11),
              makeCuboidShape(6, 11, 6, 10, 15, 10)
        ), INDUSTRIAL_ALARM, true);

        setShape(VoxelShapeUtils.combine(
              makeCuboidShape(1, 15, 1, 15, 16, 15)
        ), QIO_DASHBOARD, true);

        setShape(VoxelShapeUtils.combine(
              makeCuboidShape(0, 12, 0, 16, 16, 9), // drive_top
              makeCuboidShape(11, 5, 15.005, 12, 6, 16.005), // drive_status_led_1
              makeCuboidShape(11, 3, 15.005, 12, 4, 16.005), // drive_status_led_2
              makeCuboidShape(14, 10, 15.005, 15, 13, 16.005), // drive_frequency_led_right
              makeCuboidShape(1, 10, 15.005, 2, 13, 16.005), // drive_frequency_led_left
              makeCuboidShape(0, 0, 9, 16, 16, 16), // drive_front
              makeCuboidShape(0, 6, 0, 16, 9, 9), // rack_top
              makeCuboidShape(0, 0, 0, 16, 3, 9), // rack_bottom
              makeCuboidShape(0, 3, 0, 1, 6, 1), // post_bottom_right
              makeCuboidShape(0, 9, 0, 1, 12, 1), // post_top_right
              makeCuboidShape(15, 3, 0, 16, 6, 1), // post_bottom_left
              makeCuboidShape(15, 9, 0, 16, 12, 1) // post_top_left
        ), QIO_DRIVE_ARRAY);

        setShape(VoxelShapeUtils.combine(
              makeCuboidShape(4, 0, 4, 12, 1, 12), // disc_base
              makeCuboidShape(5, 4, 5, 11, 5, 6), // ring_top_led_1
              makeCuboidShape(5, 4, 10, 11, 5, 11), // ring_top_led_2
              makeCuboidShape(5, 4, 6, 6, 5, 10), // ring_top_led_3
              makeCuboidShape(10, 4, 6, 11, 5, 10), // ring_top_led_4
              makeCuboidShape(10, 2, 6, 11, 3, 10), // ring_bottom_led_1
              makeCuboidShape(5, 2, 6, 6, 3, 10), // ring_bottom_led_2
              makeCuboidShape(5, 2, 10, 11, 3, 11), // ring_bottom_led_3
              makeCuboidShape(5, 2, 5, 11, 3, 6), // ring_bottom_led_4
              makeCuboidShape(9, 1, 6, 10, 6, 7), // post_1
              makeCuboidShape(9, 1, 9, 10, 6, 10), // post_2
              makeCuboidShape(6, 1, 9, 7, 6, 10), // post_3
              makeCuboidShape(6, 1, 6, 7, 6, 7), // post_4
              makeCuboidShape(9, 8, 9, 10, 9, 10), // top_post_led_1
              makeCuboidShape(6, 9, 6, 7, 10, 7), // top_post_led_2
              makeCuboidShape(7.5, 1, 7.5, 8.5, 6, 8.5), // core_led
              makeCuboidShape(5, 6, 5, 11, 7, 11), // disc_mid
              makeCuboidShape(9, 7, 9, 10, 8, 10), // antenna_1
              makeCuboidShape(6, 7, 6, 7, 9, 7) // antenna_2
        ), QIO_IMPORTER, true, true);

        setShape(VoxelShapeUtils.combine(
              makeCuboidShape(4, 0, 4, 12, 1, 12), // disc_base
              makeCuboidShape(5, 4, 5, 11, 5, 6), // ring_top_led_1
              makeCuboidShape(5, 4, 10, 11, 5, 11), // ring_top_led_2
              makeCuboidShape(5, 4, 6, 6, 5, 10), // ring_top_led_3
              makeCuboidShape(10, 4, 6, 11, 5, 10), // ring_top_led_4
              makeCuboidShape(10, 2, 6, 11, 3, 10), // ring_bottom_led_1
              makeCuboidShape(5, 2, 6, 6, 3, 10), // ring_bottom_led_2
              makeCuboidShape(5, 2, 10, 11, 3, 11), // ring_bottom_led_3
              makeCuboidShape(5, 2, 5, 11, 3, 6), // ring_bottom_led_4
              makeCuboidShape(9, 1, 6, 10, 6, 7), // post_1
              makeCuboidShape(9, 1, 9, 10, 6, 10), // post_2
              makeCuboidShape(6, 1, 9, 7, 6, 10), // post_3
              makeCuboidShape(6, 1, 6, 7, 6, 7), // post_4
              makeCuboidShape(7, 7, 7, 9, 8, 9), // top_post_led_1
              makeCuboidShape(7, 8.01, 7, 9, 9.01, 9), // top_post_led_2
              makeCuboidShape(7.5, 1, 7.5, 8.5, 6, 8.5), // core_led
              makeCuboidShape(5, 6, 5, 11, 7, 11), // disc_mid
              makeCuboidShape(6, 8, 6, 10, 9, 10), // disc_top
              makeCuboidShape(9, 9, 9, 10, 10, 10), // crown_1
              makeCuboidShape(6, 9, 9, 7, 10, 10), // crown_2
              makeCuboidShape(6, 9, 6, 7, 10, 7), // crown_3
              makeCuboidShape(9, 9, 6, 10, 10, 7) // crown_4
        ), QIO_EXPORTER, true, true);

        setShape(VoxelShapeUtils.combine(
              makeCuboidShape(4, 0, 4, 12, 1, 12), // disc_base
              makeCuboidShape(5, 2, 5, 11, 3, 6), // ring_led_1
              makeCuboidShape(5, 2, 10, 11, 3, 11), // ring_led_2
              makeCuboidShape(5, 2, 6, 6, 3, 10), // ring_led_3
              makeCuboidShape(10, 2, 6, 11, 3, 10), // ring_led_4
              makeCuboidShape(9, 1, 6, 10, 4, 7), // post_1
              makeCuboidShape(9, 1, 9, 10, 4, 10), // post_2
              makeCuboidShape(6, 1, 9, 7, 4, 10), // post_3
              makeCuboidShape(6, 1, 6, 7, 4, 7), // post_4
              makeCuboidShape(7.5, 1, 7.5, 8.5, 4, 8.5), // core_led
              makeCuboidShape(5, 4, 5, 11, 5, 11), // disc_mid
              makeCuboidShape(6, 8, 6, 10, 9, 10), // disc_top
              makeCuboidShape(7, 5, 7, 9, 11, 9) // torch
        ), QIO_REDSTONE_ADAPTER, true, true);

        setShape(VoxelShapeUtils.combine(
              makeCuboidShape(-16, 13, 0, 16, 16, 16), // desk_top
              makeCuboidShape(13, 0, 0, 16, 13, 3), // desk_part1
              makeCuboidShape(0, 0, 0, 3, 13, 3), // desk_part2
              makeCuboidShape(-16, 8, 0, 0, 11, 16), // desk_part3
              makeCuboidShape(0, 8, 3, 16, 11, 16), // desk_part4
              makeCuboidShape(0, 0, 3, 16, 6, 16), // desk_part5
              makeCuboidShape(-16, 0, 0, 0, 6, 16), // desk_part6
              makeCuboidShape(-15, 11, 1, 0, 13, 15), // desk_filler_right_top
              makeCuboidShape(-15, 6, 1, 0, 8, 15), // desk_filler_right_bottom
              makeCuboidShape(0, 11, 3, 15, 13, 15), // desk_filler_left_top
              makeCuboidShape(0, 6, 3, 15, 8, 15), // desk_filler_left_bottom
              makeCuboidShape(1, 17.5, 10.5, 15, 25.5, 12.5), // monitor
              makeCuboidShape(2, 17, 11, 3, 18, 12), // led
              makeCuboidShape(2, 15, 1, 14, 17, 7), // keyboard
              makeCuboidShape(6, 16, 11, 10, 22, 13), // monitor_stand_arm
              makeCuboidShape(5, 16, 10, 11, 17, 14), // monitor_stand_base
              makeCuboidShape(-14, 16, 2, -1, 17, 14), // modifier_base
              makeCuboidShape(-14, 16, 14, -11, 30, 16), // modifier_arm_right1
              makeCuboidShape(-14, 6, 14.005, -11, 16, 16.005), // modifier_arm_right2
              makeCuboidShape(-4, 16, 14, -1, 30, 16), // modifier_arm_left1
              makeCuboidShape(-4, 6, 14.005, -1, 16, 16.005), // modifier_arm_left2
              makeCuboidShape(-11, 23, 8, -10, 27, 9), // modifier_probe_right
              makeCuboidShape(-11, 22, 8, -10, 23, 9), // modifier_probe_right_led
              makeCuboidShape(-5, 23, 8, -4, 27, 9), // modifier_probe_left
              makeCuboidShape(-5, 22, 8, -4, 23, 9), // modifier_probe_left_led
              makeCuboidShape(-13, 27, 7, -2, 28, 10), // modifier_probe_base
              makeCuboidShape(-11, 28, 6, -4, 30, 11), // modifier_arm_top_center
              makeCuboidShape(-14, 28, 6, -11, 30, 14), // modifier_arm_top_right
              makeCuboidShape(-4, 28, 6, -1, 30, 14), // modifier_arm_top_left
              makeCuboidShape(4, 4, 15.005, 12, 12, 16.005) // energy_port
        ), MODIFICATION_STATION);

        setShape(VoxelShapeUtils.combine(
              makeCuboidShape(0, 0, 0, 16, 2, 16), // base
              makeCuboidShape(1, 2, 14, 2, 3, 15), // rivet1
              makeCuboidShape(1, 2, 1, 2, 3, 2), // rivet2
              makeCuboidShape(14, 2, 1, 15, 3, 2), // rivet3
              makeCuboidShape(14, 2, 14, 15, 3, 15), // rivet4
              makeCuboidShape(3, 2, 3, 13, 16, 13), // tower1
              makeCuboidShape(3, 27, 3, 13, 30, 13), // tower2
              makeCuboidShape(3, 16, 3, 6, 27, 6), // tower3
              makeCuboidShape(10, 16, 3, 13, 27, 6), // tower4
              makeCuboidShape(3, 16, 10, 6, 27, 13), // tower5
              makeCuboidShape(10, 16, 10, 13, 27, 13), // tower6
              makeCuboidShape(4, 16, 4, 12, 27, 12), // glass_tank
              makeCuboidShape(2, 9, 2, 14, 10, 14), // ring1
              makeCuboidShape(2, 7, 2, 14, 8, 14), // ring2
              makeCuboidShape(2, 5, 2, 14, 6, 14), // ring3
              makeCuboidShape(2, 3, 2, 14, 4, 14), // ring4
              makeCuboidShape(2, 10, 10, 3, 14, 11), // pipe1
              makeCuboidShape(4, 30, 4, 6, 32, 6), // node1
              makeCuboidShape(5, 30, 10, 6, 32, 11), // node2
              makeCuboidShape(6, 30, 6, 10, 31, 10), // coil
              makeCuboidShape(4, 4, 0, 12, 12, 1), // port
              makeCuboidShape(5, 5, 1, 11, 11, 3), // port_connector
              makeCuboidShape(6, 18, 7, 7, 25, 8), // random_shape1
              makeCuboidShape(6, 17, 7, 9, 18, 8), // random_shape2
              makeCuboidShape(8, 15, 7, 9, 17, 8) // random_shape3
        ), ISOTOPIC_CENTRIFUGE);

        setShape(VoxelShapeUtils.combine(
              makeCuboidShape(5, 1, 5, 11, 10, 11), // body
              makeCuboidShape(4.995, 8, 7, 5.995, 13, 8), // wire1
              makeCuboidShape(10.005, 8, 8, 11.005, 13, 9), // wire2
              makeCuboidShape(7, 15, 7, 9, 16, 9), // center
              makeCuboidShape(4, 0, 4, 12, 1, 12), // port
              makeCuboidShape(6, 10, 6, 10, 15, 10), // coil_large
              makeCuboidShape(7, 3, 4, 9, 8, 6), // coil_1
              makeCuboidShape(7, 3, 10, 9, 8, 12), // coil_2
              makeCuboidShape(4, 3, 7, 6, 8, 9), // coil_3
              makeCuboidShape(10, 3, 7, 12, 8, 9) // coil_4
        ), SUPERCHARGED_COIL, true, true);

        setShape(VoxelShapeUtils.combine(
              makeCuboidShape(0, 5, 0, 16, 8, 16),
              makeCuboidShape(1, 3, 1, 15, 5, 15),
              makeCuboidShape(0, 0, 0, 16, 3, 16),
              makeCuboidShape(12, 8, 0, 16, 16, 10),
              makeCuboidShape(11, 9, 4, 12, 14, 9),
              makeCuboidShape(4, 9, 4, 5, 14, 9),
              makeCuboidShape(5, 11, 6, 6, 12, 7),
              makeCuboidShape(10, 11, 6, 11, 12, 7),
              makeCuboidShape(0, 8, 10, 16, 16, 13),
              makeCuboidShape(0, 13, 13, 16, 16, 16),
              makeCuboidShape(1, 11, 13, 15, 12, 15), // fin1
              makeCuboidShape(1, 9, 13, 15, 10, 15), // fin2
              makeCuboidShape(0, 8, 0, 4, 16, 10),
              makeCuboidShape(4, 14, 1, 12, 15, 2), // glass_support
              makeCuboidShape(6, 8, 13, 10, 13, 16), // divider
              makeCuboidShape(6, 3, 15, 10, 5, 16), // divider
              makeCuboidShape(2, 8, 13, 3, 13, 14), // fuel_rod_led1
              makeCuboidShape(4, 8, 13, 5, 13, 14), // fuel_rod_led2
              makeCuboidShape(11, 8, 13, 12, 13, 14), // fuel_rod_led3
              makeCuboidShape(13, 8, 13, 14, 13, 14), // fuel_rod_led4
              makeCuboidShape(4, 4, 15.005, 12, 12, 16.005), // port
              makeCuboidShape(5, 10, 15.01, 11, 11, 16.01), // port_led1
              makeCuboidShape(5, 5, 15.01, 11, 6, 16.01), // port_led2
              makeCuboidShape(5, 6, 15.01, 6, 10, 16.01), // port_led3
              makeCuboidShape(10, 6, 15.01, 11, 10, 16.01), // port_led4
              makeCuboidShape(4, 7.99, 1.01, 12, 14.99, 10.01) // glass
        ), ANTIPROTONIC_NUCLEOSYNTHESIZER);

        //TODO: Don't bother rotating the shape, it is the same for all rotations
        setShape(makeCuboidShape(2, 0, 2, 14, 16, 14), FLUID_TANK);
        setShape(makeCuboidShape(2, 0, 2, 14, 16, 14), RADIOACTIVE_WASTE_BARREL);
    }
}
