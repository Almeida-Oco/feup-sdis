package controller.client;

import java.rmi.Remote;

class ReclaimHandler implements Remote {
  void reclaim(String f_name) {
    System.out.println("GOT RECLAIM MSG! '" + f_name + "'");
  }
}
