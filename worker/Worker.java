package worker;

import java.util.*;
import java.io.*;

public class Worker
{

  private static void printLines(String name, InputStream ins) throws Exception {
    String line = null;
    BufferedReader in = new BufferedReader(
        new InputStreamReader(ins));
    while ((line = in.readLine()) != null) {
        System.out.println(name + " " + line);
    }
}

  private static void printLinestoFile(String name, InputStream ins,  String filename) throws Exception {
    String line = null;

    PrintWriter writer = new PrintWriter(name, "UTF-8");

    BufferedReader in = new BufferedReader(
        new InputStreamReader(ins));
    while ((line = in.readLine()) != null) {
        writer.println(name + " " + line);
    }

    writer.close();
}

  private static String printLinestoString(String name, InputStream ins) throws Exception {
    String line = null;

    StringWriter writer = new StringWriter();

    BufferedReader in = new BufferedReader(
        new InputStreamReader(ins));
    while ((line = in.readLine()) != null) {
        writer.write(name + " " + line + '\n');
    }

    writer.close();
    return writer.toString();
}

private static ProgramRes runProcess(String command) throws Exception {


    Date now = new Date();

    Process pro = Runtime.getRuntime().exec(command);
    
    //printLines(command + " stdout:", pro.getInputStream());
    //printLines(command + " stderr:", pro.getErrorStream());

    //printLinestoFile(command + " stdout:", pro.getInputStream(), "stdout");
    //printLinestoFile(command + " stderr:", pro.getErrorStream(), "stderr");

    System.out.println(command);

    String std_out = printLinestoString(command + " stdout:", pro.getInputStream());
    String std_err = printLinestoString(command + " stderr:", pro.getErrorStream());

    pro.waitFor();

    Date finished = new Date();
    
    //System.out.println(command + " exitValue() " + pro.exitValue());
    
    ProgramRes results = new ProgramRes(pro.exitValue(), std_out, std_err, now);
    results.setDfinished(finished);
    return results;
}

private static void compileRunProgram(String name) throws Exception {

    String final_name;
    if (name.contains(".java")) {
        final_name = name.split("\\.")[0];
    } else {
        final_name = name;
    }

    String compile_str = "javac " + final_name + ".java";
    String run_str = "java " + final_name;
    if(runProcess(compile_str).getExitval() == 0){
        ProgramRes run_results = runProcess(run_str);
        run_results.storeToFiles(final_name);
    } else {
        System.err.println("Error on calling compiler");
    }

}


public static void main(String args[])
{
    if (args.length < 1)
    {
        System.out.println("USAGE java worker <outputfile>");
        System.exit(1);
    }

    try {
        compileRunProgram(args[0]);
    } catch (Exception e) {
      System.err.println("Error on calling sys calls for compiling. Unable to launch Compiling Process.");  
      e.printStackTrace();
  }

      }
  }
