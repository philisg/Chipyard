`timescale 1ns/1ps
module memoryPort_6connect_32b(Config_Clock, Config_Reset, ConfigIn, ConfigOut, in0, in1, in2, in3, in4, in5, out, write_rq, from_mem, to_mem, addr);
    // Specifying the ports
    input Config_Clock, Config_Reset, ConfigIn;
    output ConfigOut;
    input [31:0] in0;
    input [31:0] in1;
    input [31:0] in2;
    input [31:0] in3;
    input [31:0] in4;
    input [31:0] in5;
    input [31:0] from_mem;
    output [31:0] out;
    output write_rq;
    output [31:0] addr;
    output [31:0] to_mem;

    // Wires for the the config cells
    wire [2:0] MuxAddr_sig;
    wire MuxAddr_config;
    wire [2:0] MuxData_sig;
    wire MuxData_config;
    wire WriteRq_sig;
    wire WriteRq_config;

    // Wires connecting the main module and submodules
    wire [31:0] mux_addr_out_sig;
    wire [31:0] mux_data_out_sig;

    // Declaring the config cells
    ConfigCell #(3) MuxAddr (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(ConfigIn),
        .ConfigOut(MuxAddr_config),
        .select(MuxAddr_sig));
    ConfigCell #(3) MuxData (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(MuxAddr_config),
        .ConfigOut(MuxData_config),
        .select(MuxData_sig));
    ConfigCell #(1) WriteRq (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(MuxData_config),
        .ConfigOut(WriteRq_config),
        .select(WriteRq_sig));

    // Declaring the submodules
    mux_6to1_32b #(32) mux_addr(
        .in0(in0),
        .in1(in1),
        .in2(in2),
        .in3(in3),
        .in4(in4),
        .in5(in5),
        .out(mux_addr_out_sig),
        .select(MuxAddr_sig));
    mux_6to1_32b #(32) mux_data(
        .in0(in0),
        .in1(in1),
        .in2(in2),
        .in3(in3),
        .in4(in4),
        .in5(in5),
        .out(mux_data_out_sig),
        .select(MuxData_sig));
    assign write_rq     = WriteRq_sig;
    assign to_mem       = mux_data_out_sig;
    assign addr         = mux_addr_out_sig;
    assign out          = from_mem;
    
    assign ConfigOut    = WriteRq_config;
endmodule

