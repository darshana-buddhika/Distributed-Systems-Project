

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

public class FileServer implements FileInterface {
	 private String uploadFilePath;
	 private SHAHash shaHash = new SHAHash();
	 private FileGenerator fileGenerator = new FileGenerator();
	 private String file; 
	    public FileServer(String uFilePath) {
	       uploadFilePath = uFilePath;
	       
	    }
	        
	    @Override
	    public String downloadFile(String fileName){
	        try {
	        	
	        	file = fileGenerator.createFile(2000);
//	            File file = new File(uploadFilePath+"/"+fileName);
//	            byte buffer[] = new byte[(int)file.length()];
//	            BufferedInputStream input = new
//	                    BufferedInputStream(new FileInputStream(uploadFilePath+"/"+fileName));
//	            input.read(buffer,0,buffer.length);
//	            input.close();
//	            System.out.printf(Integer.toString(buffer.length));
	            return(file);
	        } catch(Exception e){
	            System.out.println("FileImpl: "+e.getMessage());
	            e.printStackTrace();
	            return(null);
	        }
	    }
	    @Override
	    public String getHash(String fileName) {
	    	
	    	String hash = shaHash.hashString(file);
			return hash;
	    	
	    }
}
