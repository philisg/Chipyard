`timescale 1ns/1ps
module adres_6in_vliw(Config_Clock, Config_Reset, ConfigIn, ConfigOut, fu_to_rf, in0, in1, in2, in3, in4, in5, out, rf_to_muxa, rf_to_muxout);
    // Specifying the ports
    input Config_Clock, Config_Reset, ConfigIn;
    output ConfigOut;
    output [31:0] fu_to_rf;
    input [31:0] in0;
    input [31:0] in1;
    input [31:0] in2;
    input [31:0] in3;
    input [31:0] in4;
    input [31:0] in5;
    output [31:0] out;
    input [31:0] rf_to_muxa;
    input [31:0] rf_to_muxout;

    // Wires for the the config cells
    wire [3:0] FuncConfig_sig;
    wire FuncConfig_config;
    wire [2:0] MuxAConfig_sig;
    wire MuxAConfig_config;
    wire [2:0] MuxBConfig_sig;
    wire MuxBConfig_config;
    wire [2:0] MuxBypassConfig_sig;
    wire MuxBypassConfig_config;
    wire MuxOutConfig_sig;
    wire MuxOutConfig_config;

    // Wires connecting the main module and submodules
    wire [31:0] mux_a_out_sig;
    wire [31:0] mux_b_out_sig;
    wire [31:0] mux_bypass_out_sig;
    wire [31:0] const_out_sig;
    wire [31:0] mux_out_out_sig;
    wire const_config;

    // Declaring the config cells
    ConfigCell #(4) FuncConfig (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(ConfigIn),
        .ConfigOut(FuncConfig_config),
        .select(FuncConfig_sig));
    ConfigCell #(3) MuxAConfig (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(FuncConfig_config),
        .ConfigOut(MuxAConfig_config),
        .select(MuxAConfig_sig));
    ConfigCell #(3) MuxBConfig (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(MuxAConfig_config),
        .ConfigOut(MuxBConfig_config),
        .select(MuxBConfig_sig));
    ConfigCell #(3) MuxBypassConfig (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(MuxBConfig_config),
        .ConfigOut(MuxBypassConfig_config),
        .select(MuxBypassConfig_sig));
    ConfigCell #(1) MuxOutConfig (
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(MuxBypassConfig_config),
        .ConfigOut(MuxOutConfig_config),
        .select(MuxOutConfig_sig));

    // Declaring the submodules
    const_32b #(32) ConstVal(
        .Config_Clock(Config_Clock),
        .Config_Reset(Config_Reset),
        .ConfigIn(MuxOutConfig_config),
        .ConfigOut(const_config),
        .out(const_out_sig));
    func_32b_add_multiply_sub_divide_and_or_xor_shl_ashr_lshr #(32) func(
        .in_a(mux_a_out_sig),
        .in_b(mux_b_out_sig),
        .out(fu_to_rf),
        .select(FuncConfig_sig));
    mux_8to1_32b #(32) mux_a(
        .in0(in0),
        .in1(in1),
        .in2(in2),
        .in3(in3),
        .in4(in4),
        .in5(in5),
        .in6(const_out_sig),
        .in7(rf_to_muxa),
        .out(mux_a_out_sig),
        .select(MuxAConfig_sig));
    mux_8to1_32b #(32) mux_b(
        .in0(in0),
        .in1(in1),
        .in2(in2),
        .in3(in3),
        .in4(in4),
        .in5(in5),
        .in6(const_out_sig),
        .in7(mux_out_out_sig),
        .out(mux_b_out_sig),
        .select(MuxBConfig_sig));
    mux_6to1_32b #(32) mux_bypass(
        .in0(in0),
        .in1(in1),
        .in2(in2),
        .in3(in3),
        .in4(in4),
        .in5(in5),
        .out(mux_bypass_out_sig),
        .select(MuxBypassConfig_sig));
    mux_2to1_32b #(32) mux_out(
        .in0(rf_to_muxout),
        .in1(mux_bypass_out_sig),
        .out(mux_out_out_sig),
        .select(MuxOutConfig_sig));
    assign out = mux_out_out_sig;
    assign ConfigOut = const_config;
endmodule

