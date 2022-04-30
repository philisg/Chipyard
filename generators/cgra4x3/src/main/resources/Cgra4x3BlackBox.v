`timescale 1ns/1ps
module Cgra4x3BlackBox(
        Config_Clock, 
        Config_Reset, 
        ConfigIn, 
        ConfigOut, 
        CGRA_Clock, 
        CGRA_Reset, 
        dataIn0,
        dataIn1,
        dataIn2,
        dataIn3,
        dataOut0,
        dataOut1,
        dataOut2,
        dataOut3,
        write0,
        write1,
        write2,
        write3,
        write_rq0,
        from_mem0,
        to_mem0,
        addr0,
        write_rq1,
        from_mem1,
        to_mem1,
        addr1,
        write_rq2,
        from_mem2,
        to_mem2,
        addr2
        );

    // Specifying the ports
    input Config_Clock, Config_Reset, ConfigIn;
    output ConfigOut;
    input CGRA_Clock, CGRA_Reset;
    
    output write_rq0;
    input [31:0] from_mem0;
    output [31:0] to_mem0;
    output [31:0] addr0;
    output write_rq1;
    input [31:0] from_mem1;
    output [31:0] to_mem1;
    output [31:0] addr1;
    output write_rq2;
    input [31:0] from_mem2;
    output [31:0] to_mem2;
    output [31:0] addr2;

    wire [31:0] ext_io_top_0;
    wire [31:0] ext_io_top_1;
    wire [31:0] ext_io_top_2;
    wire [31:0] ext_io_top_3;

    input write0;
    input write1;
    input write2;
    input write3;
    input [31:0] dataIn0;
    input [31:0] dataIn1;
    input [31:0] dataIn2;
    input [31:0] dataIn3;
    output [31:0] dataOut0;
    output [31:0] dataOut1;
    output [31:0] dataOut2;
    output [31:0] dataOut3;

    assign ext_io_top_0 = write0? dataIn0 : 32'dz;
    assign ext_io_top_1 = write1? dataIn1 : 32'dz;
    assign ext_io_top_2 = write2? dataIn2 : 32'dz;
    assign ext_io_top_3 = write3? dataIn3 : 32'dz;
    assign dataOut0 = ext_io_top_0;
    assign dataOut1 = ext_io_top_1;
    assign dataOut2 = ext_io_top_2;
    assign dataOut3 = ext_io_top_3;

    // Wires for the the config cells
    wire [2:0] DrfAddrIn0_sig;
    wire DrfAddrIn0_config;
    wire [2:0] DrfAddrIn1_sig;
    wire DrfAddrIn1_config;
    wire [2:0] DrfAddrIn2_sig;
    wire DrfAddrIn2_config;
    wire [2:0] DrfAddrIn3_sig;
    wire DrfAddrIn3_config;
    wire [2:0] DrfAddrOut0_sig;
    wire DrfAddrOut0_config;
    wire [2:0] DrfAddrOut1_sig;
    wire DrfAddrOut1_config;
    wire [2:0] DrfAddrOut2_sig;
    wire DrfAddrOut2_config;
    wire [2:0] DrfAddrOut3_sig;
    wire DrfAddrOut3_config;
    wire [2:0] DrfAddrOut4_sig;
    wire DrfAddrOut4_config;
    wire [2:0] DrfAddrOut5_sig;
    wire DrfAddrOut5_config;
    wire [2:0] DrfAddrOut6_sig;
    wire DrfAddrOut6_config;
    wire [2:0] DrfAddrOut7_sig;
    wire DrfAddrOut7_config;
    wire DrfWE0_sig;
    wire DrfWE0_config;
    wire DrfWE1_sig;
    wire DrfWE1_config;
    wire DrfWE2_sig;
    wire DrfWE2_config;
    wire DrfWE3_sig;
    wire DrfWE3_config;
    wire RfC0R1AddrIn0_sig;
    wire RfC0R1AddrIn0_config;
    wire RfC0R1AddrOut0_sig;
    wire RfC0R1AddrOut0_config;
    wire RfC0R1AddrOut1_sig;
    wire RfC0R1AddrOut1_config;
    wire RfC0R1WE_sig;
    wire RfC0R1WE_config;
    wire RfC0R2AddrIn0_sig;
    wire RfC0R2AddrIn0_config;
    wire RfC0R2AddrOut0_sig;
    wire RfC0R2AddrOut0_config;
    wire RfC0R2AddrOut1_sig;
    wire RfC0R2AddrOut1_config;
    wire RfC0R2WE_sig;
    wire RfC0R2WE_config;
    wire RfC1R1AddrIn0_sig;
    wire RfC1R1AddrIn0_config;
    wire RfC1R1AddrOut0_sig;
    wire RfC1R1AddrOut0_config;
    wire RfC1R1AddrOut1_sig;
    wire RfC1R1AddrOut1_config;
    wire RfC1R1WE_sig;
    wire RfC1R1WE_config;
    wire RfC1R2AddrIn0_sig;
    wire RfC1R2AddrIn0_config;
    wire RfC1R2AddrOut0_sig;
    wire RfC1R2AddrOut0_config;
    wire RfC1R2AddrOut1_sig;
    wire RfC1R2AddrOut1_config;
    wire RfC1R2WE_sig;
    wire RfC1R2WE_config;
    wire RfC2R1AddrIn0_sig;
    wire RfC2R1AddrIn0_config;
    wire RfC2R1AddrOut0_sig;
    wire RfC2R1AddrOut0_config;
    wire RfC2R1AddrOut1_sig;
    wire RfC2R1AddrOut1_config;
    wire RfC2R1WE_sig;
    wire RfC2R1WE_config;
    wire RfC2R2AddrIn0_sig;
    wire RfC2R2AddrIn0_config;
    wire RfC2R2AddrOut0_sig;
    wire RfC2R2AddrOut0_config;
    wire RfC2R2AddrOut1_sig;
    wire RfC2R2AddrOut1_config;
    wire RfC2R2WE_sig;
    wire RfC2R2WE_config;
    wire RfC3R1AddrIn0_sig;
    wire RfC3R1AddrIn0_config;
    wire RfC3R1AddrOut0_sig;
    wire RfC3R1AddrOut0_config;
    wire RfC3R1AddrOut1_sig;
    wire RfC3R1AddrOut1_config;
    wire RfC3R1WE_sig;
    wire RfC3R1WE_config;
    wire RfC3R2AddrIn0_sig;
    wire RfC3R2AddrIn0_config;
    wire RfC3R2AddrOut0_sig;
    wire RfC3R2AddrOut0_config;
    wire RfC3R2AddrOut1_sig;
    wire RfC3R2AddrOut1_config;
    wire RfC3R2WE_sig;
    wire RfC3R2WE_config;

    // Wires connecting the main module and submodules
    wire [31:0] io_top_1_out_sig;
    wire [31:0] io_top_2_out_sig;
    wire [31:0] io_top_0_out_sig;
    wire [31:0] io_top_3_out_sig;
    wire [31:0] mem_0_out_sig;
    wire [31:0] mem_1_out_sig;
    wire [31:0] mem_2_out_sig;
    wire [31:0] pe_c0_r0_out_sig;
    wire [31:0] pe_c0_r0_fu_to_rf_sig;
    wire [31:0] pe_c0_r1_out_sig;
    wire [31:0] pe_c0_r1_fu_to_rf_sig;
    wire [31:0] pe_c0_r2_out_sig;
    wire [31:0] pe_c0_r2_fu_to_rf_sig;
    wire [31:0] pe_c1_r0_out_sig;
    wire [31:0] pe_c1_r0_fu_to_rf_sig;
    wire [31:0] pe_c1_r1_out_sig;
    wire [31:0] pe_c1_r1_fu_to_rf_sig;
    wire [31:0] pe_c1_r2_out_sig;
    wire [31:0] pe_c1_r2_fu_to_rf_sig;
    wire [31:0] pe_c2_r0_out_sig;
    wire [31:0] pe_c2_r0_fu_to_rf_sig;
    wire [31:0] pe_c2_r1_out_sig;
    wire [31:0] pe_c2_r1_fu_to_rf_sig;
    wire [31:0] pe_c2_r2_out_sig;
    wire [31:0] pe_c2_r2_fu_to_rf_sig;
    wire [31:0] pe_c3_r0_out_sig;
    wire [31:0] pe_c3_r0_fu_to_rf_sig;
    wire [31:0] pe_c3_r1_out_sig;
    wire [31:0] pe_c3_r1_fu_to_rf_sig;
    wire [31:0] pe_c3_r2_out_sig;
    wire [31:0] pe_c3_r2_fu_to_rf_sig;
    wire [31:0] drf_out0_sig;
    wire [31:0] drf_out1_sig;
    wire [31:0] drf_out2_sig;
    wire [31:0] drf_out3_sig;
    wire [31:0] drf_out4_sig;
    wire [31:0] drf_out5_sig;
    wire [31:0] drf_out6_sig;
    wire [31:0] drf_out7_sig;
    wire [31:0] rf_c0_r1_out0_sig;
    wire [31:0] rf_c0_r1_out1_sig;
    wire [31:0] rf_c0_r2_out0_sig;
    wire [31:0] rf_c0_r2_out1_sig;
    wire [31:0] rf_c1_r1_out0_sig;
    wire [31:0] rf_c1_r1_out1_sig;
    wire [31:0] rf_c1_r2_out0_sig;
    wire [31:0] rf_c1_r2_out1_sig;
    wire [31:0] rf_c2_r1_out0_sig;
    wire [31:0] rf_c2_r1_out1_sig;
    wire [31:0] rf_c2_r2_out0_sig;
    wire [31:0] rf_c2_r2_out1_sig;
    wire [31:0] rf_c3_r1_out0_sig;
    wire [31:0] rf_c3_r1_out1_sig;
    wire [31:0] rf_c3_r2_out0_sig;
    wire [31:0] rf_c3_r2_out1_sig;
    wire io_top_0_config;
    wire io_top_1_config;
    wire io_top_2_config;
    wire io_top_3_config;
    wire mem_0_config;
    wire mem_1_config;
    wire mem_2_config;
    wire pe_c0_r0_config;
    wire pe_c0_r1_config;
    wire pe_c0_r2_config;
    wire pe_c1_r0_config;
    wire pe_c1_r1_config;
    wire pe_c1_r2_config;
    wire pe_c2_r0_config;
    wire pe_c2_r1_config;
    wire pe_c2_r2_config;
    wire pe_c3_r0_config;
    wire pe_c3_r1_config;
    wire pe_c3_r2_config;

    // Declaring the config cells
    ConfigCell #(3) DrfAddrIn0 (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(ConfigIn),
        .ConfigOut(DrfAddrIn0_config),
        .select(DrfAddrIn0_sig));
    ConfigCell #(3) DrfAddrIn1 (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(DrfAddrIn0_config),
        .ConfigOut(DrfAddrIn1_config),
        .select(DrfAddrIn1_sig));
    ConfigCell #(3) DrfAddrIn2 (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(DrfAddrIn1_config),
        .ConfigOut(DrfAddrIn2_config),
        .select(DrfAddrIn2_sig));
    ConfigCell #(3) DrfAddrIn3 (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(DrfAddrIn2_config),
        .ConfigOut(DrfAddrIn3_config),
        .select(DrfAddrIn3_sig));
    ConfigCell #(3) DrfAddrOut0 (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(DrfAddrIn3_config),
        .ConfigOut(DrfAddrOut0_config),
        .select(DrfAddrOut0_sig));
    ConfigCell #(3) DrfAddrOut1 (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(DrfAddrOut0_config),
        .ConfigOut(DrfAddrOut1_config),
        .select(DrfAddrOut1_sig));
    ConfigCell #(3) DrfAddrOut2 (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(DrfAddrOut1_config),
        .ConfigOut(DrfAddrOut2_config),
        .select(DrfAddrOut2_sig));
    ConfigCell #(3) DrfAddrOut3 (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(DrfAddrOut2_config),
        .ConfigOut(DrfAddrOut3_config),
        .select(DrfAddrOut3_sig));
    ConfigCell #(3) DrfAddrOut4 (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(DrfAddrOut3_config),
        .ConfigOut(DrfAddrOut4_config),
        .select(DrfAddrOut4_sig));
    ConfigCell #(3) DrfAddrOut5 (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(DrfAddrOut4_config),
        .ConfigOut(DrfAddrOut5_config),
        .select(DrfAddrOut5_sig));
    ConfigCell #(3) DrfAddrOut6 (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(DrfAddrOut5_config),
        .ConfigOut(DrfAddrOut6_config),
        .select(DrfAddrOut6_sig));
    ConfigCell #(3) DrfAddrOut7 (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(DrfAddrOut6_config),
        .ConfigOut(DrfAddrOut7_config),
        .select(DrfAddrOut7_sig));
    ConfigCell #(1) DrfWE0 (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(DrfAddrOut7_config),
        .ConfigOut(DrfWE0_config),
        .select(DrfWE0_sig));
    ConfigCell #(1) DrfWE1 (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(DrfWE0_config),
        .ConfigOut(DrfWE1_config),
        .select(DrfWE1_sig));
    ConfigCell #(1) DrfWE2 (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(DrfWE1_config),
        .ConfigOut(DrfWE2_config),
        .select(DrfWE2_sig));
    ConfigCell #(1) DrfWE3 (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(DrfWE2_config),
        .ConfigOut(DrfWE3_config),
        .select(DrfWE3_sig));
    ConfigCell #(1) RfC0R1AddrIn0 (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(DrfWE3_config),
        .ConfigOut(RfC0R1AddrIn0_config),
        .select(RfC0R1AddrIn0_sig));
    ConfigCell #(1) RfC0R1AddrOut0 (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(RfC0R1AddrIn0_config),
        .ConfigOut(RfC0R1AddrOut0_config),
        .select(RfC0R1AddrOut0_sig));
    ConfigCell #(1) RfC0R1AddrOut1 (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(RfC0R1AddrOut0_config),
        .ConfigOut(RfC0R1AddrOut1_config),
        .select(RfC0R1AddrOut1_sig));
    ConfigCell #(1) RfC0R1WE (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(RfC0R1AddrOut1_config),
        .ConfigOut(RfC0R1WE_config),
        .select(RfC0R1WE_sig));
    ConfigCell #(1) RfC0R2AddrIn0 (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(RfC0R1WE_config),
        .ConfigOut(RfC0R2AddrIn0_config),
        .select(RfC0R2AddrIn0_sig));
    ConfigCell #(1) RfC0R2AddrOut0 (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(RfC0R2AddrIn0_config),
        .ConfigOut(RfC0R2AddrOut0_config),
        .select(RfC0R2AddrOut0_sig));
    ConfigCell #(1) RfC0R2AddrOut1 (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(RfC0R2AddrOut0_config),
        .ConfigOut(RfC0R2AddrOut1_config),
        .select(RfC0R2AddrOut1_sig));
    ConfigCell #(1) RfC0R2WE (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(RfC0R2AddrOut1_config),
        .ConfigOut(RfC0R2WE_config),
        .select(RfC0R2WE_sig));
    ConfigCell #(1) RfC1R1AddrIn0 (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(RfC0R2WE_config),
        .ConfigOut(RfC1R1AddrIn0_config),
        .select(RfC1R1AddrIn0_sig));
    ConfigCell #(1) RfC1R1AddrOut0 (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(RfC1R1AddrIn0_config),
        .ConfigOut(RfC1R1AddrOut0_config),
        .select(RfC1R1AddrOut0_sig));
    ConfigCell #(1) RfC1R1AddrOut1 (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(RfC1R1AddrOut0_config),
        .ConfigOut(RfC1R1AddrOut1_config),
        .select(RfC1R1AddrOut1_sig));
    ConfigCell #(1) RfC1R1WE (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(RfC1R1AddrOut1_config),
        .ConfigOut(RfC1R1WE_config),
        .select(RfC1R1WE_sig));
    ConfigCell #(1) RfC1R2AddrIn0 (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(RfC1R1WE_config),
        .ConfigOut(RfC1R2AddrIn0_config),
        .select(RfC1R2AddrIn0_sig));
    ConfigCell #(1) RfC1R2AddrOut0 (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(RfC1R2AddrIn0_config),
        .ConfigOut(RfC1R2AddrOut0_config),
        .select(RfC1R2AddrOut0_sig));
    ConfigCell #(1) RfC1R2AddrOut1 (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(RfC1R2AddrOut0_config),
        .ConfigOut(RfC1R2AddrOut1_config),
        .select(RfC1R2AddrOut1_sig));
    ConfigCell #(1) RfC1R2WE (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(RfC1R2AddrOut1_config),
        .ConfigOut(RfC1R2WE_config),
        .select(RfC1R2WE_sig));
    ConfigCell #(1) RfC2R1AddrIn0 (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(RfC1R2WE_config),
        .ConfigOut(RfC2R1AddrIn0_config),
        .select(RfC2R1AddrIn0_sig));
    ConfigCell #(1) RfC2R1AddrOut0 (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(RfC2R1AddrIn0_config),
        .ConfigOut(RfC2R1AddrOut0_config),
        .select(RfC2R1AddrOut0_sig));
    ConfigCell #(1) RfC2R1AddrOut1 (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(RfC2R1AddrOut0_config),
        .ConfigOut(RfC2R1AddrOut1_config),
        .select(RfC2R1AddrOut1_sig));
    ConfigCell #(1) RfC2R1WE (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(RfC2R1AddrOut1_config),
        .ConfigOut(RfC2R1WE_config),
        .select(RfC2R1WE_sig));
    ConfigCell #(1) RfC2R2AddrIn0 (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(RfC2R1WE_config),
        .ConfigOut(RfC2R2AddrIn0_config),
        .select(RfC2R2AddrIn0_sig));
    ConfigCell #(1) RfC2R2AddrOut0 (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(RfC2R2AddrIn0_config),
        .ConfigOut(RfC2R2AddrOut0_config),
        .select(RfC2R2AddrOut0_sig));
    ConfigCell #(1) RfC2R2AddrOut1 (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(RfC2R2AddrOut0_config),
        .ConfigOut(RfC2R2AddrOut1_config),
        .select(RfC2R2AddrOut1_sig));
    ConfigCell #(1) RfC2R2WE (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(RfC2R2AddrOut1_config),
        .ConfigOut(RfC2R2WE_config),
        .select(RfC2R2WE_sig));
    ConfigCell #(1) RfC3R1AddrIn0 (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(RfC2R2WE_config),
        .ConfigOut(RfC3R1AddrIn0_config),
        .select(RfC3R1AddrIn0_sig));
    ConfigCell #(1) RfC3R1AddrOut0 (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(RfC3R1AddrIn0_config),
        .ConfigOut(RfC3R1AddrOut0_config),
        .select(RfC3R1AddrOut0_sig));
    ConfigCell #(1) RfC3R1AddrOut1 (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(RfC3R1AddrOut0_config),
        .ConfigOut(RfC3R1AddrOut1_config),
        .select(RfC3R1AddrOut1_sig));
    ConfigCell #(1) RfC3R1WE (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(RfC3R1AddrOut1_config),
        .ConfigOut(RfC3R1WE_config),
        .select(RfC3R1WE_sig));
    ConfigCell #(1) RfC3R2AddrIn0 (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(RfC3R1WE_config),
        .ConfigOut(RfC3R2AddrIn0_config),
        .select(RfC3R2AddrIn0_sig));
    ConfigCell #(1) RfC3R2AddrOut0 (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(RfC3R2AddrIn0_config),
        .ConfigOut(RfC3R2AddrOut0_config),
        .select(RfC3R2AddrOut0_sig));
    ConfigCell #(1) RfC3R2AddrOut1 (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(RfC3R2AddrOut0_config),
        .ConfigOut(RfC3R2AddrOut1_config),
        .select(RfC3R2AddrOut1_sig));
    ConfigCell #(1) RfC3R2WE (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(RfC3R2AddrOut1_config),
        .ConfigOut(RfC3R2WE_config),
        .select(RfC3R2WE_sig));

    // Declaring the submodules
    registerFile_4in_8out_32b #(3, 32) drf(
        .CGRA_Clock(CGRA_Clock),
        .CGRA_Reset(CGRA_Reset),
        .WE0(DrfWE0_sig),
        .WE1(DrfWE1_sig),
        .WE2(DrfWE2_sig),
        .WE3(DrfWE3_sig),
        .address_in0(DrfAddrIn0_sig),
        .address_in1(DrfAddrIn1_sig),
        .address_in2(DrfAddrIn2_sig),
        .address_in3(DrfAddrIn3_sig),
        .address_out0(DrfAddrOut0_sig),
        .address_out1(DrfAddrOut1_sig),
        .address_out2(DrfAddrOut2_sig),
        .address_out3(DrfAddrOut3_sig),
        .address_out4(DrfAddrOut4_sig),
        .address_out5(DrfAddrOut5_sig),
        .address_out6(DrfAddrOut6_sig),
        .address_out7(DrfAddrOut7_sig),
        .in0(pe_c0_r0_fu_to_rf_sig),
        .in1(pe_c1_r0_fu_to_rf_sig),
        .in2(pe_c2_r0_fu_to_rf_sig),
        .in3(pe_c3_r0_fu_to_rf_sig),
        .out0(drf_out0_sig),
        .out1(drf_out1_sig),
        .out2(drf_out2_sig),
        .out3(drf_out3_sig),
        .out4(drf_out4_sig),
        .out5(drf_out5_sig),
        .out6(drf_out6_sig),
        .out7(drf_out7_sig));
    io_32b #(32) io_top_0(
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(RfC3R2WE_config),
        .ConfigOut(io_top_0_config),
        .CGRA_Clock(CGRA_Clock),
        .CGRA_Reset(CGRA_Reset),
        .bidir(ext_io_top_0),
        .in(pe_c0_r0_out_sig),
        .out(io_top_0_out_sig));
    io_32b #(32) io_top_1(
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(io_top_0_config),
        .ConfigOut(io_top_1_config),
        .CGRA_Clock(CGRA_Clock),
        .CGRA_Reset(CGRA_Reset),
        .bidir(ext_io_top_1),
        .in(pe_c1_r0_out_sig),
        .out(io_top_1_out_sig));
    io_32b #(32) io_top_2(
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(io_top_1_config),
        .ConfigOut(io_top_2_config),
        .CGRA_Clock(CGRA_Clock),
        .CGRA_Reset(CGRA_Reset),
        .bidir(ext_io_top_2),
        .in(pe_c2_r0_out_sig),
        .out(io_top_2_out_sig));
    io_32b #(32) io_top_3(
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(io_top_2_config),
        .ConfigOut(io_top_3_config),
        .CGRA_Clock(CGRA_Clock),
        .CGRA_Reset(CGRA_Reset),
        .bidir(ext_io_top_3),
        .in(pe_c3_r0_out_sig),
        .out(io_top_3_out_sig));
    memoryPort_4connect_32b mem_0(
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(io_top_3_config),
        .ConfigOut(mem_0_config),
        .in0(pe_c0_r0_out_sig),
        .in1(pe_c1_r0_out_sig),
        .in2(pe_c2_r0_out_sig),
        .in3(pe_c3_r0_out_sig),
        .out(mem_0_out_sig),
        .write_rq(write_rq0),
        .from_mem(from_mem0),
        .to_mem(to_mem0),
        .addr(addr0));
    memoryPort_4connect_32b mem_1(
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(mem_0_config),
        .ConfigOut(mem_1_config),
        .in0(pe_c0_r1_out_sig),
        .in1(pe_c1_r1_out_sig),
        .in2(pe_c2_r1_out_sig),
        .in3(pe_c3_r1_out_sig),
        .out(mem_1_out_sig),
        .write_rq(write_rq1),
        .from_mem(from_mem1),
        .to_mem(to_mem1),
        .addr(addr1));
    memoryPort_4connect_32b mem_2(
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(mem_1_config),
        .ConfigOut(mem_2_config),
        .in0(pe_c0_r2_out_sig),
        .in1(pe_c1_r2_out_sig),
        .in2(pe_c2_r2_out_sig),
        .in3(pe_c3_r2_out_sig),
        .out(mem_2_out_sig),
        .write_rq(write_rq2),
        .from_mem(from_mem2),
        .to_mem(to_mem2),
        .addr(addr2));
    adres_6in_vliw pe_c0_r0(
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(mem_2_config),
        .ConfigOut(pe_c0_r0_config),
        .fu_to_rf(pe_c0_r0_fu_to_rf_sig),
        .in0(io_top_0_out_sig),
        .in1(pe_c1_r0_out_sig),
        .in2(pe_c0_r1_out_sig),
        .in3(pe_c3_r0_out_sig),
        .in4(pe_c0_r2_out_sig),
        .in5(mem_0_out_sig),
        .out(pe_c0_r0_out_sig),
        .rf_to_muxa(drf_out0_sig),
        .rf_to_muxout(drf_out1_sig));
    adres_5in_vliw pe_c0_r1(
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(pe_c0_r0_config),
        .ConfigOut(pe_c0_r1_config),
        .fu_to_rf(pe_c0_r1_fu_to_rf_sig),
        .in0(pe_c0_r0_out_sig),
        .in1(pe_c1_r1_out_sig),
        .in2(pe_c0_r2_out_sig),
        .in3(pe_c3_r1_out_sig),
        .in4(mem_1_out_sig),
        .out(pe_c0_r1_out_sig),
        .rf_to_muxa(rf_c0_r1_out0_sig),
        .rf_to_muxout(rf_c0_r1_out1_sig));
    adres_5in_vliw pe_c0_r2(
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(pe_c0_r1_config),
        .ConfigOut(pe_c0_r2_config),
        .fu_to_rf(pe_c0_r2_fu_to_rf_sig),
        .in0(pe_c0_r1_out_sig),
        .in1(pe_c1_r2_out_sig),
        .in2(pe_c0_r0_out_sig),
        .in3(pe_c3_r2_out_sig),
        .in4(mem_2_out_sig),
        .out(pe_c0_r2_out_sig),
        .rf_to_muxa(rf_c0_r2_out0_sig),
        .rf_to_muxout(rf_c0_r2_out1_sig));
    adres_6in_vliw pe_c1_r0(
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(pe_c0_r2_config),
        .ConfigOut(pe_c1_r0_config),
        .fu_to_rf(pe_c1_r0_fu_to_rf_sig),
        .in0(io_top_1_out_sig),
        .in1(pe_c2_r0_out_sig),
        .in2(pe_c1_r1_out_sig),
        .in3(pe_c0_r0_out_sig),
        .in4(pe_c1_r2_out_sig),
        .in5(mem_0_out_sig),
        .out(pe_c1_r0_out_sig),
        .rf_to_muxa(drf_out2_sig),
        .rf_to_muxout(drf_out3_sig));
    adres_5in_vliw pe_c1_r1(
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(pe_c1_r0_config),
        .ConfigOut(pe_c1_r1_config),
        .fu_to_rf(pe_c1_r1_fu_to_rf_sig),
        .in0(pe_c1_r0_out_sig),
        .in1(pe_c2_r1_out_sig),
        .in2(pe_c1_r2_out_sig),
        .in3(pe_c0_r1_out_sig),
        .in4(mem_1_out_sig),
        .out(pe_c1_r1_out_sig),
        .rf_to_muxa(rf_c1_r1_out0_sig),
        .rf_to_muxout(rf_c1_r1_out1_sig));
    adres_5in_vliw pe_c1_r2(
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(pe_c1_r1_config),
        .ConfigOut(pe_c1_r2_config),
        .fu_to_rf(pe_c1_r2_fu_to_rf_sig),
        .in0(pe_c1_r1_out_sig),
        .in1(pe_c2_r2_out_sig),
        .in2(pe_c1_r0_out_sig),
        .in3(pe_c0_r2_out_sig),
        .in4(mem_2_out_sig),
        .out(pe_c1_r2_out_sig),
        .rf_to_muxa(rf_c1_r2_out0_sig),
        .rf_to_muxout(rf_c1_r2_out1_sig));
    adres_6in_vliw pe_c2_r0(
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(pe_c1_r2_config),
        .ConfigOut(pe_c2_r0_config),
        .fu_to_rf(pe_c2_r0_fu_to_rf_sig),
        .in0(io_top_2_out_sig),
        .in1(pe_c3_r0_out_sig),
        .in2(pe_c2_r1_out_sig),
        .in3(pe_c1_r0_out_sig),
        .in4(pe_c2_r2_out_sig),
        .in5(mem_0_out_sig),
        .out(pe_c2_r0_out_sig),
        .rf_to_muxa(drf_out4_sig),
        .rf_to_muxout(drf_out5_sig));
    adres_5in_vliw pe_c2_r1(
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(pe_c2_r0_config),
        .ConfigOut(pe_c2_r1_config),
        .fu_to_rf(pe_c2_r1_fu_to_rf_sig),
        .in0(pe_c2_r0_out_sig),
        .in1(pe_c3_r1_out_sig),
        .in2(pe_c2_r2_out_sig),
        .in3(pe_c1_r1_out_sig),
        .in4(mem_1_out_sig),
        .out(pe_c2_r1_out_sig),
        .rf_to_muxa(rf_c2_r1_out0_sig),
        .rf_to_muxout(rf_c2_r1_out1_sig));
    adres_5in_vliw pe_c2_r2(
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(pe_c2_r1_config),
        .ConfigOut(pe_c2_r2_config),
        .fu_to_rf(pe_c2_r2_fu_to_rf_sig),
        .in0(pe_c2_r1_out_sig),
        .in1(pe_c3_r2_out_sig),
        .in2(pe_c2_r0_out_sig),
        .in3(pe_c1_r2_out_sig),
        .in4(mem_2_out_sig),
        .out(pe_c2_r2_out_sig),
        .rf_to_muxa(rf_c2_r2_out0_sig),
        .rf_to_muxout(rf_c2_r2_out1_sig));
    adres_6in_vliw pe_c3_r0(
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(pe_c2_r2_config),
        .ConfigOut(pe_c3_r0_config),
        .fu_to_rf(pe_c3_r0_fu_to_rf_sig),
        .in0(io_top_3_out_sig),
        .in1(pe_c0_r0_out_sig),
        .in2(pe_c3_r1_out_sig),
        .in3(pe_c2_r0_out_sig),
        .in4(pe_c3_r2_out_sig),
        .in5(mem_0_out_sig),
        .out(pe_c3_r0_out_sig),
        .rf_to_muxa(drf_out6_sig),
        .rf_to_muxout(drf_out7_sig));
    adres_5in_vliw pe_c3_r1(
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(pe_c3_r0_config),
        .ConfigOut(pe_c3_r1_config),
        .fu_to_rf(pe_c3_r1_fu_to_rf_sig),
        .in0(pe_c3_r0_out_sig),
        .in1(pe_c0_r1_out_sig),
        .in2(pe_c3_r2_out_sig),
        .in3(pe_c2_r1_out_sig),
        .in4(mem_1_out_sig),
        .out(pe_c3_r1_out_sig),
        .rf_to_muxa(rf_c3_r1_out0_sig),
        .rf_to_muxout(rf_c3_r1_out1_sig));
    adres_5in_vliw pe_c3_r2(
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(pe_c3_r1_config),
        .ConfigOut(pe_c3_r2_config),
        .fu_to_rf(pe_c3_r2_fu_to_rf_sig),
        .in0(pe_c3_r1_out_sig),
        .in1(pe_c0_r2_out_sig),
        .in2(pe_c3_r0_out_sig),
        .in3(pe_c2_r2_out_sig),
        .in4(mem_2_out_sig),
        .out(pe_c3_r2_out_sig),
        .rf_to_muxa(rf_c3_r2_out0_sig),
        .rf_to_muxout(rf_c3_r2_out1_sig));
    registerFile_1in_2out_32b #(1, 32) rf_c0_r1(
        .CGRA_Clock(CGRA_Clock),
        .CGRA_Reset(CGRA_Reset),
        .WE0(RfC0R1WE_sig),
        .address_in0(RfC0R1AddrIn0_sig),
        .address_out0(RfC0R1AddrOut0_sig),
        .address_out1(RfC0R1AddrOut1_sig),
        .in0(pe_c0_r1_fu_to_rf_sig),
        .out0(rf_c0_r1_out0_sig),
        .out1(rf_c0_r1_out1_sig));
    registerFile_1in_2out_32b #(1, 32) rf_c0_r2(
        .CGRA_Clock(CGRA_Clock),
        .CGRA_Reset(CGRA_Reset),
        .WE0(RfC0R2WE_sig),
        .address_in0(RfC0R2AddrIn0_sig),
        .address_out0(RfC0R2AddrOut0_sig),
        .address_out1(RfC0R2AddrOut1_sig),
        .in0(pe_c0_r2_fu_to_rf_sig),
        .out0(rf_c0_r2_out0_sig),
        .out1(rf_c0_r2_out1_sig));
    registerFile_1in_2out_32b #(1, 32) rf_c1_r1(
        .CGRA_Clock(CGRA_Clock),
        .CGRA_Reset(CGRA_Reset),
        .WE0(RfC1R1WE_sig),
        .address_in0(RfC1R1AddrIn0_sig),
        .address_out0(RfC1R1AddrOut0_sig),
        .address_out1(RfC1R1AddrOut1_sig),
        .in0(pe_c1_r1_fu_to_rf_sig),
        .out0(rf_c1_r1_out0_sig),
        .out1(rf_c1_r1_out1_sig));
    registerFile_1in_2out_32b #(1, 32) rf_c1_r2(
        .CGRA_Clock(CGRA_Clock),
        .CGRA_Reset(CGRA_Reset),
        .WE0(RfC1R2WE_sig),
        .address_in0(RfC1R2AddrIn0_sig),
        .address_out0(RfC1R2AddrOut0_sig),
        .address_out1(RfC1R2AddrOut1_sig),
        .in0(pe_c1_r2_fu_to_rf_sig),
        .out0(rf_c1_r2_out0_sig),
        .out1(rf_c1_r2_out1_sig));
    registerFile_1in_2out_32b #(1, 32) rf_c2_r1(
        .CGRA_Clock(CGRA_Clock),
        .CGRA_Reset(CGRA_Reset),
        .WE0(RfC2R1WE_sig),
        .address_in0(RfC2R1AddrIn0_sig),
        .address_out0(RfC2R1AddrOut0_sig),
        .address_out1(RfC2R1AddrOut1_sig),
        .in0(pe_c2_r1_fu_to_rf_sig),
        .out0(rf_c2_r1_out0_sig),
        .out1(rf_c2_r1_out1_sig));
    registerFile_1in_2out_32b #(1, 32) rf_c2_r2(
        .CGRA_Clock(CGRA_Clock),
        .CGRA_Reset(CGRA_Reset),
        .WE0(RfC2R2WE_sig),
        .address_in0(RfC2R2AddrIn0_sig),
        .address_out0(RfC2R2AddrOut0_sig),
        .address_out1(RfC2R2AddrOut1_sig),
        .in0(pe_c2_r2_fu_to_rf_sig),
        .out0(rf_c2_r2_out0_sig),
        .out1(rf_c2_r2_out1_sig));
    registerFile_1in_2out_32b #(1, 32) rf_c3_r1(
        .CGRA_Clock(CGRA_Clock),
        .CGRA_Reset(CGRA_Reset),
        .WE0(RfC3R1WE_sig),
        .address_in0(RfC3R1AddrIn0_sig),
        .address_out0(RfC3R1AddrOut0_sig),
        .address_out1(RfC3R1AddrOut1_sig),
        .in0(pe_c3_r1_fu_to_rf_sig),
        .out0(rf_c3_r1_out0_sig),
        .out1(rf_c3_r1_out1_sig));
    registerFile_1in_2out_32b #(1, 32) rf_c3_r2(
        .CGRA_Clock(CGRA_Clock),
        .CGRA_Reset(CGRA_Reset),
        .WE0(RfC3R2WE_sig),
        .address_in0(RfC3R2AddrIn0_sig),
        .address_out0(RfC3R2AddrOut0_sig),
        .address_out1(RfC3R2AddrOut1_sig),
        .in0(pe_c3_r2_fu_to_rf_sig),
        .out0(rf_c3_r2_out0_sig),
        .out1(rf_c3_r2_out1_sig));
    assign ConfigOut = pe_c3_r2_config;
endmodule

