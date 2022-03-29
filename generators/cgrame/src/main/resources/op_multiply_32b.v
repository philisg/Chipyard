`timescale 1ns/1ps
`timescale 1ns/1ps
module op_multiply_32b(a, b, c);
    parameter size = 32;
    // Specifying the ports
    input [size-1:0] a;
    input [size-1:0] b;
    output [size-1:0] c;
    assign c = a * b;
endmodule

