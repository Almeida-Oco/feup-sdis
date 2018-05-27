package handlers.queries;


import handlers.Handler;
import network.chord.Node;
import network.comms.Packet;
import network.comms.PacketChannel;

import worker.*;

public class CodeExecutorHandler extends Handler {
  public CodeExecutorHandler(Node node) {
    super(node);
  }

  @Override
  public void run(Packet packet, PacketChannel buffer) {

    System.out.println("Running code executer handler");

    System.out.println("Got code : " + packet.getCode());

    try{
        ProgramRes code_results = Worker.ProgramResfromString(packet.getCode(), new String[0]);
        System.out.println("Results from code: " + code_results.toString());
    } catch (Exception e) {
        System.err.println("Unable to execute code");
    }

    //TODO execute response handler

  }

  @Override
  public void run() {
  }
}
