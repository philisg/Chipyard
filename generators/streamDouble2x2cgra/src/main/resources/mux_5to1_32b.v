`timescale 1ns/1ps
module mux_5to1_32b(in0, in1, in2, in3, in4, out, select);
    parameter size = 32;
    // Specifying the ports
    input [size-1:0] in0;
    input [size-1:0] in1;
    input [size-1:0] in2;
    input [size-1:0] in3;
    input [size-1:0] in4;
    output reg [size-1:0] out;
    input [2:0] select;
    always @(*)
        case (select)
            0: out = in0;
            1: out = in1;
            2: out = in2;
            3: out = in3;
            4: out = in4;
            default: out = {size{1'bx}};
        endcase
endmodule

