`timescale 1ns/1ps
module memUnit_32b(addr, data_in, data_out, w_rq);
    parameter size = 32;
    // Specifying the ports
    input [size-1:0] addr;
    input [size-1:0] data_in;
    output [size-1:0] data_out;
    input w_rq;
    
    assign data_out = 32'hdead;
    // ALERT: This module is an unimplemented place holder.
endmodule

