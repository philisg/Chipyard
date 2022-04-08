`timescale 1ns/1ps
module registerFile_2in_4out_32b(CGRA_Clock, CGRA_Reset, WE0, WE1, address_in0, address_in1, address_out0, address_out1, address_out2, address_out3, in0, in1, out0, out1, out2, out3);
    parameter log2regs = 3;
    parameter size = 32;
    // Specifying the ports
    input CGRA_Clock, CGRA_Reset;
    input WE0;
    input WE1;
    input [log2regs-1:0] address_in0;
    input [log2regs-1:0] address_in1;
    input [log2regs-1:0] address_out0;
    input [log2regs-1:0] address_out1;
    input [log2regs-1:0] address_out2;
    input [log2regs-1:0] address_out3;
    input [size-1:0] in0;
    input [size-1:0] in1;
    output reg [size-1:0] out0;
    output reg [size-1:0] out1;
    output reg [size-1:0] out2;
    output reg [size-1:0] out3;

    // Setting the always blocks and inside registers
    reg [size-1:0] register[2**log2regs-1:0];
    always@(posedge CGRA_Clock, posedge CGRA_Reset)
        if(CGRA_Reset)
            begin : RESET
                integer i;
                for (i = 0; i < 2**log2regs; i = i+1)
                    register[i] <= 0;
            end
        else
            begin
                out0 <= register[address_out0];
                out1 <= register[address_out1];
                out2 <= register[address_out2];
                out3 <= register[address_out3];
                if(WE0)
                    register[address_in0] <= in0;
                if(WE1)
                    register[address_in1] <= in1;
            end
endmodule

