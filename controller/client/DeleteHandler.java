package controller.client;

import java.rmi.Remote;

class DeleteHandler implements Remote {
  void delete(String f_name) {
    System.out.println("GOT DELETE MSG! '" + f_name + "'");
  }
}
