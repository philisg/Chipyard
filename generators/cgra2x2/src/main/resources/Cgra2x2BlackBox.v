`timescale 1ns/1ps
module Cgra2x2BlackBox(
        Config_Clock, 
        Config_Reset, 
        ConfigIn, 
        ConfigOut, 
        CGRA_Clock, 
        CGRA_Reset, 
        // ext_io_top_0, 
        // ext_io_top_1, 
        dataIn0,
        dataIn1,
        dataOut0,
        dataOut1,
        write0,
        write1,
        write_rq0,
        from_mem0,
        to_mem0,
        addr0,
        write_rq1,
        from_mem1,
        to_mem1,
        addr1
        );
    // Specifying the ports
    input Config_Clock, Config_Reset, ConfigIn;
    output ConfigOut;
    input CGRA_Clock, CGRA_Reset, write0, write1;
    input [31:0] dataIn0;
    input [31:0] dataIn1;
    output [31:0] dataOut0;
    output [31:0] dataOut1;

    output write_rq0;
    input [31:0] from_mem0;
    output [31:0] to_mem0;
    output [31:0] addr0;
    output write_rq1;
    input [31:0] from_mem1;
    output [31:0] to_mem1;
    output [31:0] addr1;
    
    // inout [31:0] ext_io_top_0;
    // inout [31:0] ext_io_top_1;
    
    wire [31:0] ext_io_top_0;
    wire [31:0] ext_io_top_1;
    
    assign dataOut0 = ext_io_top_0;
    assign dataOut1 = ext_io_top_1;
    
    assign ext_io_top_0 = (write0)? dataIn0 : 32'dz;
    assign ext_io_top_1 = (write1)? dataIn1 : 32'dz;

    // Wires for the the config cells
    wire [2:0] DrfAddrIn0_sig;
    wire DrfAddrIn0_config;
    wire [2:0] DrfAddrIn1_sig;
    wire DrfAddrIn1_config;
    wire [2:0] DrfAddrOut0_sig;
    wire DrfAddrOut0_config;
    wire [2:0] DrfAddrOut1_sig;
    wire DrfAddrOut1_config;
    wire [2:0] DrfAddrOut2_sig;
    wire DrfAddrOut2_config;
    wire [2:0] DrfAddrOut3_sig;
    wire DrfAddrOut3_config;
    wire DrfWE0_sig;
    wire DrfWE0_config;
    wire DrfWE1_sig;
    wire DrfWE1_config;
    wire RfC0R1AddrIn0_sig;
    wire RfC0R1AddrIn0_config;
    wire RfC0R1AddrOut0_sig;
    wire RfC0R1AddrOut0_config;
    wire RfC0R1AddrOut1_sig;
    wire RfC0R1AddrOut1_config;
    wire RfC0R1WE_sig;
    wire RfC0R1WE_config;
    wire RfC1R1AddrIn0_sig;
    wire RfC1R1AddrIn0_config;
    wire RfC1R1AddrOut0_sig;
    wire RfC1R1AddrOut0_config;
    wire RfC1R1AddrOut1_sig;
    wire RfC1R1AddrOut1_config;
    wire RfC1R1WE_sig;
    wire RfC1R1WE_config;

    // Wires connecting the main module and submodules
    wire [31:0] mem_0_out_sig;
    wire [31:0] io_top_1_out_sig;
    wire [31:0] io_top_0_out_sig;
    wire [31:0] mem_1_out_sig;
    wire [31:0] pe_c0_r0_out_sig;
    wire [31:0] pe_c0_r0_fu_to_rf_sig;
    wire [31:0] pe_c0_r1_out_sig;
    wire [31:0] pe_c0_r1_fu_to_rf_sig;
    wire [31:0] pe_c1_r0_out_sig;
    wire [31:0] pe_c1_r0_fu_to_rf_sig;
    wire [31:0] pe_c1_r1_out_sig;
    wire [31:0] pe_c1_r1_fu_to_rf_sig;
    wire [31:0] drf_out0_sig;
    wire [31:0] drf_out1_sig;
    wire [31:0] drf_out2_sig;
    wire [31:0] drf_out3_sig;
    wire [31:0] rf_c0_r1_out0_sig;
    wire [31:0] rf_c0_r1_out1_sig;
    wire [31:0] rf_c1_r1_out0_sig;
    wire [31:0] rf_c1_r1_out1_sig;
    wire io_top_0_config;
    wire io_top_1_config;
    wire mem_0_config;
    wire mem_1_config;
    wire pe_c0_r0_config;
    wire pe_c0_r1_config;
    wire pe_c1_r0_config;
    wire pe_c1_r1_config;

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
    ConfigCell #(3) DrfAddrOut0 (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(DrfAddrIn1_config),
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
    ConfigCell #(1) DrfWE0 (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(DrfAddrOut3_config),
        .ConfigOut(DrfWE0_config),
        .select(DrfWE0_sig));
    ConfigCell #(1) DrfWE1 (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(DrfWE0_config),
        .ConfigOut(DrfWE1_config),
        .select(DrfWE1_sig));
    ConfigCell #(1) RfC0R1AddrIn0 (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(DrfWE1_config),
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
    ConfigCell #(1) RfC1R1AddrIn0 (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(RfC0R1WE_config),
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

    // Declaring the submodules
    registerFile_2in_4out_32b #(3, 32) drf(
        .CGRA_Clock(CGRA_Clock),
        .CGRA_Reset(CGRA_Reset),
        .WE0(DrfWE0_sig),
        .WE1(DrfWE1_sig),
        .address_in0(DrfAddrIn0_sig),
        .address_in1(DrfAddrIn1_sig),
        .address_out0(DrfAddrOut0_sig),
        .address_out1(DrfAddrOut1_sig),
        .address_out2(DrfAddrOut2_sig),
        .address_out3(DrfAddrOut3_sig),
        .in0(pe_c0_r0_fu_to_rf_sig),
        .in1(pe_c1_r0_fu_to_rf_sig),
        .out0(drf_out0_sig),
        .out1(drf_out1_sig),
        .out2(drf_out2_sig),
        .out3(drf_out3_sig));
    io_32b #(32) io_top_0(
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(RfC1R1WE_config),
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
    memoryPort_2connect_32b mem_0(
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(io_top_1_config),
        .ConfigOut(mem_0_config),
        .in0(pe_c0_r0_out_sig),
        .in1(pe_c1_r0_out_sig),
        .out(mem_0_out_sig),
        .write_rq(write_rq0),
        .from_mem(from_mem0),
        .to_mem(to_mem0),
        .addr(addr0));
    memoryPort_2connect_32b mem_1(
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(mem_0_config),
        .ConfigOut(mem_1_config),
        .in0(pe_c0_r1_out_sig),
        .in1(pe_c1_r1_out_sig),
        .out(mem_1_out_sig),
        .write_rq(write_rq1),
        .from_mem(from_mem1),
        .to_mem(to_mem1),
        .addr(addr1));
    adres_6in_vliw pe_c0_r0(
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(mem_1_config),
        .ConfigOut(pe_c0_r0_config),
        .fu_to_rf(pe_c0_r0_fu_to_rf_sig),
        .in0(io_top_0_out_sig),
        .in1(pe_c1_r0_out_sig),
        .in2(pe_c0_r1_out_sig),
        .in3(pe_c1_r0_out_sig),
        .in4(pe_c0_r1_out_sig),
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
        .in2(pe_c0_r0_out_sig),
        .in3(pe_c1_r1_out_sig),
        .in4(mem_1_out_sig),
        .out(pe_c0_r1_out_sig),
        .rf_to_muxa(rf_c0_r1_out0_sig),
        .rf_to_muxout(rf_c0_r1_out1_sig));
    adres_6in_vliw pe_c1_r0(
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(pe_c0_r1_config),
        .ConfigOut(pe_c1_r0_config),
        .fu_to_rf(pe_c1_r0_fu_to_rf_sig),
        .in0(io_top_1_out_sig),
        .in1(pe_c0_r0_out_sig),
        .in2(pe_c1_r1_out_sig),
        .in3(pe_c0_r0_out_sig),
        .in4(pe_c1_r1_out_sig),
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
        .in1(pe_c0_r1_out_sig),
        .in2(pe_c1_r0_out_sig),
        .in3(pe_c0_r1_out_sig),
        .in4(mem_1_out_sig),
        .out(pe_c1_r1_out_sig),
        .rf_to_muxa(rf_c1_r1_out0_sig),
        .rf_to_muxout(rf_c1_r1_out1_sig));
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
    assign ConfigOut = pe_c1_r1_config;
endmodule

