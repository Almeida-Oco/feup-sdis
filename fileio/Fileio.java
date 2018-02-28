package fileio

public class Fileio{

	final int MAX_CHUNK_SIZE = 64000;
	final int MAX_N_CHUNKS = 1000000;

	String cwd;

	Fileio(String cwd){
		this.cwd = cwd;
	}

	public String store_string(String content, String filename) {

        try {

        	String final_path = this.cwd+'/'+filename;

            File newTextFile = new File(final_path);

            FileWriter fw = new FileWriter(newTextFile);
            fw.write(content);
            fw.close();

            return final_path;

        } catch (IOException iox) {
        	System.err.println("Can't Store File!\n " + iox.getMessage());
            return "ERROR";
        }
    }

   	public String store_chunk(String content, String chunk_number, String chunk_rep) {

        try {

        	String final_path = this.cwd+'/'+filename;

            File newTextFile = new File(final_path);

            FileWriter fw = new FileWriter(newTextFile);
            fw.write(content);
            fw.close();

            return final_path;

        } catch (IOException iox) {
        	System.err.println("Can't Store File!\n " + iox.getMessage());
            return "ERROR";
        }
    }  

}