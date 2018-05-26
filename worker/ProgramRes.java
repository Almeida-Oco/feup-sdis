package worker;

import java.util.*;
import java.io.*;

public class ProgramRes {
  String stdout;
  String stderr;
  int exitval;
  Date dstarted;
  Date dfinished;

  public ProgramRes(int exitval, String stdout, String stderr, Date start) {
    this.exitval  = exitval;
    this.stdout   = stdout;
    this.stderr   = stderr;
    this.dstarted = start;
  }

  public long secstaken() {
    long seconds = (this.dfinished.getTime() - dstarted.getTime()) / 1000;

    return seconds;
  }

  public String getStdout() {
    return this.stdout;
  }

  public void setStdout(String value) {
    this.stdout = value;
  }

  public String getStderr() {
    return this.stderr;
  }

  public void setStderr(String value) {
    this.stderr = value;
  }

  public int getExitval() {
    return this.exitval;
  }

  public void setExitval(int value) {
    this.exitval = value;
  }

  public Date getDstarted() {
    return this.dstarted;
  }

  public void setDstarted(Date value) {
    this.dstarted = value;
  }

  public Date getDfinished() {
    return this.dfinished;
  }

  public void storeToFiles(String folder) {
    ProgramRes.storeAFile(folder, this.stdout, "stdout.txt");
    ProgramRes.storeAFile(folder, this.stderr, "stderr.txt");
  }

  public static void storeAFile(String folder, String write, String filename) {
    File fout = new File(System.getProperty("user.dir") + "/" + folder + "/" + filename);

    if (!fout.getParentFile().exists()) {
      fout.getParentFile().mkdirs();
    }
    //Remove if clause if you want to overwrite file
    if (!fout.exists()) {
      try {
        fout.createNewFile();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    try {
      //dir will change directory and specifies file name for writer
      File        dir    = new File(fout.getParentFile(), fout.getName());
      PrintWriter writer = new PrintWriter(dir);
      writer.println(write);
      writer.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  public void setDfinished(Date value) {
    this.dfinished = value;
  }
}
