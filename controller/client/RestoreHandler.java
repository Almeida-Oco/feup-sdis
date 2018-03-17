package controller.client;

import java.rmi.Remote;

class RestoreHandler implements Remote {
  void restore(String f_name) {
    System.out.println("GOT BACKUP MSG! '" + f_name + "'");
  }
}
