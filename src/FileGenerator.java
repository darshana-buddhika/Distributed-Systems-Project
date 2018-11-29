
public class FileGenerator {
	public String createFile(int msgSize) {
	    // Java chars are 2 bytes
	    msgSize = msgSize/2;
	    msgSize = msgSize * 1024;
	    StringBuilder sb = new StringBuilder(msgSize);
	    for (int i=0; i<msgSize; i++) {
	        sb.append('a');
	    }
	    return sb.toString();
	  }
}
