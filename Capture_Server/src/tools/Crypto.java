package tools;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import jonelo.jacksum.JacksumAPI;
import jonelo.jacksum.algorithm.AbstractChecksum;

public class Crypto {
	// TODO : Check if JackSum is thread safe! We can reduce the bottleneck if so
	// But then again, this function is called occasionally, so it doesnt really matter that much
	// But if we do, make sure to create a cypto object for each user connection
	
	AbstractChecksum checksum=null;
	char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'}; // This has to be done. Faster way to build the hex representation of a byte stream is to do a bit operation and a lookup
	
	public Crypto() throws NoSuchAlgorithmException{
		checksum = JacksumAPI.getChecksumInstance("whirlpool"); 
	}

	public synchronized String getHash(String password, String salt){
		// Append the 256bit salt and then hash
		if(checksum!=null){
			checksum.reset();
			checksum.update((salt+password).getBytes());
			checksum.format("HEX_UPPERCASE");				// format the hash into a hex string. So no escape characters will appear in the SQL string
			String hash = checksum.getFormattedValue();
			return hash;
		}
		else{
			return "";
		}
	}

	public synchronized String getSalt(int length){
		// generates a 64 character hex salt (256bit)
		// Use javas cryptographicly safe Random number generator (Not PNRG) to generate 'length' amount chars
		SecureRandom random = new SecureRandom();
		byte bytes[] = new byte[length];	// where the random bytes are stored
		char chars[] = new char[length*2];	// where the hex characters
		random.nextBytes(bytes);
		for (int i = 0; i < bytes.length; i++) {
			chars[2*i]		= hexArray[((int)(bytes[i]>>4) & 0xF)];		// read the first 4 bits
			chars[(2*i)+1]	= hexArray[(int)(bytes[i] & 0xF)];			// read the last 4 bits
			//chars[i]=(char)(33+Math.abs((bytes[i])%94));				// this method generates escape characters to the sql string. Only way to send without losing secure random bits is to convert to hex
		}
		String randomchar=new String(chars);
		return randomchar;

	}
}
