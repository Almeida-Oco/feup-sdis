package worker;

import java.util.*;
import java.io.*;
import java.security.*;
import java.math.*;
import java.nio.*;
import java.util.regex.*;
import java.util.Collections;


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

    System.out.println(command);

    String std_out = printLinestoString(command + " stdout:", pro.getInputStream());
    String std_err = printLinestoString(command + " stderr:", pro.getErrorStream());

    pro.waitFor();

    Date finished = new Date();

    ProgramRes results = new ProgramRes(pro.exitValue(), std_out, std_err, now);
    results.setDfinished(finished);
    return results;
}

public static String hash(String content){

    MessageDigest m;

    try {
        m = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException me){
        System.err.println("NO MD5 implemented");
        return new String();
    }

    m.reset();
    m.update(content.getBytes());
    byte[] digest = m.digest();
    BigInteger bigInt = new BigInteger(1,digest);
    String hashtext = bigInt.toString(16);
    // Now we need to zero pad it if you actually want the full 32 chars.
    while(hashtext.length() < 32 ){
      hashtext = "0"+hashtext;
    }

    return hashtext;

}

private static ProgramRes compileRunProgram(String name, String[] args, Boolean deletejava) throws Exception {

    String final_name;
    if (name.contains(".java")) {
        final_name = name.split("\\.")[0];
    } else {
        final_name = name;
    }

    String compile_str = "javac " + final_name + ".java";
    String run_str = "java " + final_name;

    if(args.length != 0){
        for (String arg: args) {
            run_str += ' ' + arg;
        }
    }

    System.out.println(run_str);
    ProgramRes compileRes = runProcess(compile_str);

    if( compileRes.getExitval() == 0){
        ProgramRes run_results = runProcess(run_str);
        run_results.storeToFiles("genCode/" + final_name);
        runProcess("rm " + final_name +".class"); //delete the compiled program after run

        if(deletejava){
            runProcess("rm " + final_name +".java");
        }

        return run_results;
    } else {
        System.err.println("Error on calling compiler or deleting files");
    }

    return null;

}

public static ProgramRes ProgramResfromString(String code, String[] args) throws Exception{

    String codename = Worker.getClassName(code);
    ProgramRes.storeAFile("NONE", code, codename+".java");
    ProgramRes results = compileRunProgram(codename, args, new Boolean(true));
    return results;
}

public static String getClassName(String code){

    System.out.println("CODE IS " + code);
    String classname = code.split("public\\s+class\\s+")[1];

    String pattern = "\\s*\\w+\\s+";

    // Create a Pattern object
    Pattern r = Pattern.compile(pattern);

    // Now create matcher object.
    Matcher m = r.matcher(classname);
    if (m.find( )) {
        return m.group(0).trim() ;
    }

    System.err.println("NO MATCH in get classname");

    return new String();

}

public static String[] programsToStrings(String[] program_names){
    List<String> all_programs = new ArrayList<String>();

    for (String p_name : program_names) {
        try{
            all_programs.add(Worker.readFile( p_name));
            System.out.println("Opened and read: " + p_name);
        } catch (IOException io){
            System.err.println("Cant open a file, maybe name is Wrong.");
        }
    }

    return all_programs.toArray(new String[0]);
}


public static String readFile(String pathname) throws IOException {

    File file = new File(pathname);
    StringBuilder fileContents = new StringBuilder((int)file.length());
    Scanner scanner = new Scanner(file);
    String lineSeparator = System.getProperty("line.separator");

    try {
        while(scanner.hasNextLine()) {
            fileContents.append(scanner.nextLine() + lineSeparator);
        }
        return fileContents.toString();
    } finally {
        scanner.close();
    }
}

public static void main(String args[])
{
    if (args.length < 1)
    {
        System.out.println("USAGE java worker <outputfile>");
        System.exit(1);
    }

    String hellocode = "public class HelloWorld { public static void main(String[] args) { System.out.println(\"Hello world from example program, arg 0: \" + args[0]); } }";

    try {

        String[] program_names = {"example/Fibonacci.java", "example/TowersOfHanoi.java"};
        String[] program_codes = Worker.programsToStrings(program_names);
     
        for (String code : program_codes) {
            ProgramRes results = Worker.ProgramResfromString(code, new String[0]);
            System.out.println(results.getStdout());
        }

        //String[] helloargs = {"OK"};

        //ProgramRes res = Worker.ProgramResfromString(hellocode, helloargs);

        //System.out.println(res.toString());

        //ProgramRes test = new ProgramRes(res.toString());

        //System.out.println(test.toString());

        //System.out.println("Name of class: " + Worker.getClassName(hellocode));
    } catch (Exception e) {
      System.err.println("Error on calling sys calls for compiling. Unable to launch Compiling Process.");  
      e.printStackTrace();
  }

      }
  }
