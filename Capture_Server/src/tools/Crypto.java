package tools;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import jonelo.jacksum.JacksumAPI;
import jonelo.jacksum.algorithm.AbstractChecksum;

public class Crypto {
	// TODO : Check if JackSum is thread safe! Else mark these stuff synchronized
	AbstractChecksum checksum=null;
	public Crypto() throws NoSuchAlgorithmException{
		checksum = JacksumAPI.getChecksumInstance("whirlpool"); 
	}

	public String getHash(String password){
		if(checksum!=null){
			checksum.update(password.getBytes());
			String hash = checksum.getFormattedValue();
			return hash;
		}
		else{
			return "";
		}
	}

	public String getSalt(int length){
		// generates a 32 character salt (256bit)
		// Use javas cryptographicly safe Random number generator (Not PNRG) to generate 'length' amount chars
		SecureRandom random = new SecureRandom();
		byte bytes[] = new byte[length];	// where the random bytes are stored
		char chars[] = new char[length];	// where the ascii chars of the above bytes are stored
		random.nextBytes(bytes);
		for (int i = 0; i < bytes.length; i++) {
			chars[i]=(char)(33+Math.abs((bytes[i])%94));
			System.out.println(chars[i]);
		}
		String randomchar=new String(chars);
		
		return randomchar;

	}
}
