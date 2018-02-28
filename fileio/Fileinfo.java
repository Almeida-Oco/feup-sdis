package fileio

import java.security.MessageDigest;

public class Fileinfo{

	String file_hash;
	String file_content;
	int[] chunk_number;
	int[] chunk_rep;


	Fileinfo(String file_content, int[] chunk_number, int[] chunk_rep){
		
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		byte[] encodedhash = digest.digest(
  		file_content.getBytes(StandardCharsets.UTF_8));

		this.file_hash = String(encodedhash);
		this.file_content = file_content;
		this.chunk_number = chunk_number;
		this.chunk_rep = chunk_rep;
	}


	Fileinfo(String file_content){
		
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		byte[] encodedhash = digest.digest(
  		file_content.getBytes(StandardCharsets.UTF_8));

		this.file_hash = String(encodedhash);
		this.file_content = file_content;
		this.chunk_number = 0;
		this.chunk_rep = 0;
	}


	public String get_hash(){
		return this.file_hash;
	}

	public String get_content(){
		return this.file_content;
	}

	public int[] get_chunk_number(){
		return this.chunk_number;
	}

	public int[] get_chunk_rep(){
		return this.chunk_rep;
	}



}