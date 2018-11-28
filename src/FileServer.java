

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

public class FileServer implements FileInterface {
	 String uploadFilePath;
	    public FileServer(String uFilePath) {
	       uploadFilePath = uFilePath;
	    }
	        
	    @Override
	    public byte[] downloadFile(String fileName){
	        try {
	            File file = new File(uploadFilePath+"/"+fileName);
	            byte buffer[] = new byte[(int)file.length()];
	            BufferedInputStream input = new
	                    BufferedInputStream(new FileInputStream(uploadFilePath+"/"+fileName));
	            input.read(buffer,0,buffer.length);
	            input.close();
	            System.out.printf(Integer.toString(buffer.length));
	            return(buffer);
	        } catch(Exception e){
	            System.out.println("FileImpl: "+e.getMessage());
	            e.printStackTrace();
	            return(null);
	        }
	    }
}
