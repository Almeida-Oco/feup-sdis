package controller.client;

import java.rmi.Remote;

class StateHandler implements Remote {
  void state(String f_name) {
    System.out.println("GOT BACKUP MSG! '" + f_name + "'");
  }
}
