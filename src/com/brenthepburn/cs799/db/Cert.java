package com.brenthepburn.cs799.db;

import java.math.BigInteger;
import java.util.Date;

import methods.cryptography.RSA;
import methods.hash.MD5;

public class Cert {
	String fName, lName, email, modulus, key, signature, timestamp;

	private static final BigInteger p = new BigInteger("901329939047"),
			q = new BigInteger("749017486879"), e = new BigInteger(
					"13997034167977");

	public static RSA rsa;
	static {
		rsa = new RSA();
		try {
			rsa.setPrivateInfo(p, q);
			rsa.setEncryptionKey(e);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getModulus() {
		return modulus;
	}

	public void setModulus(String modulus) {
		this.modulus = modulus;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getfName() {
		return fName;
	}

	public void setfName(String fName) {
		this.fName = fName;
	}

	public String getlName() {
		return lName;
	}

	public void setlName(String lName) {
		this.lName = lName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getTimestamp() {
		if (timestamp == null)
			timestamp = new Date().toString();
		return timestamp;
	}

	public boolean check() {
		return fName != null && lName != null && email != null && key != null
				&& modulus != null;
	}

	public void sign() throws Exception {
		MD5 md5 = new MD5();
		md5.setMessage(this.toString());
		byte[] digest = md5.calculateDigest();

		byte[] functionalDigest = new byte[4];
		for (int i = 0; i < 4; i++) {
			functionalDigest[0] = digest[0];
		}

		BigInteger bi = rsa.encrypt(new BigInteger(functionalDigest));
		signature = bi.toString();
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(email).append(',');
		buffer.append(fName).append(',');
		buffer.append(lName).append(',');
		buffer.append(key).append(',');
		buffer.append(modulus).append(',');
		buffer.append(timestamp);
		return buffer.toString();
	}

	public String toString(boolean includeSignature) {
		String value = toString();
		if(includeSignature)
			value = value.concat(",").concat(signature);
		return value;
	}

}
