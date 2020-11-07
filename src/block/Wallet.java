package block;
import java.nio.charset.Charset;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Wallet {
	
	public PrivateKey privateKey;
	public PublicKey publicKey;
	
	public HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>();
	
	public Wallet() {
		generateKeyPair();
	}
		
	
	
	
	public void generateKeyPair() {
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA","BC");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
			keyGen.initialize(ecSpec, random);
	        KeyPair keyPair = keyGen.generateKeyPair();
	        privateKey = keyPair.getPrivate();
	        publicKey = keyPair.getPublic();
	        
	        
	       
			System.out.println("공개키: "+bytesToHex(publicKey.getEncoded()));
			System.out.println("개인키: "+bytesToHex(privateKey.getEncoded()));
	        
	        
			
			Scanner sc = new Scanner(System.in);
			System.out.println("서명내용을 입력하세요");
			String plainText = sc.next();
			
			// 서명 생성 
			//String plainText = "안녕하세요! 인경일입니다.";
			System.out.println("평문: "+plainText);
			Charset charset = Charset.forName("UTF-8");
			byte[] signature = sign(privateKey, plainText.getBytes(charset));
			System.out.println("서명문: "+bytesToHex(signature));
			
			
			// 서명 검증
			boolean verified = verify(publicKey, signature, plainText.getBytes(charset));
			System.out.println("서명검증 = " + verified);
			
			
			
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	public float getBalance() {
		float total = 0;	
        for (Map.Entry<String, TransactionOutput> item: Main.UTXOs.entrySet()){
        	TransactionOutput UTXO = item.getValue();
            if(UTXO.isMine(publicKey)) {
            	UTXOs.put(UTXO.id,UTXO);
            	total += UTXO.value ; 
            }
        }  
		return total;
	}
	
	public Transaction sendFunds(PublicKey _recipient,float value ) {
		if(getBalance() < value) {
			System.out.println("#Not Enough funds to send transaction. Transaction Discarded.");
			return null;
		}
		ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
		
		float total = 0;
		for (Map.Entry<String, TransactionOutput> item: UTXOs.entrySet()){
			TransactionOutput UTXO = item.getValue();
			total += UTXO.value;
			inputs.add(new TransactionInput(UTXO.id));
			if(total > value) break;
		}
		
		Transaction newTransaction = new Transaction(publicKey, _recipient , value, inputs);
		newTransaction.generateSignature(privateKey);
		
		for(TransactionInput input: inputs){
			UTXOs.remove(input.transactionOutputId);
		}
		
		return newTransaction;
	}
	
	
	public static String bytesToHex(byte[] bytes) {
	    StringBuilder sb = new StringBuilder(bytes.length * 2);
	    @SuppressWarnings("resource")
		Formatter formatter = new Formatter(sb);
	    for (byte b : bytes) {
	        formatter.format("%02x", b);
	    }
	    return sb.toString();
	}
	
	
	public static byte[] sign(PrivateKey privateKey, byte[] plainData) throws
	GeneralSecurityException {
		Signature signature = Signature.getInstance("SHA256withECDSA");
		signature.initSign(privateKey);
		signature.update(plainData);
		byte[] signatureData = signature.sign();
		return signatureData;
	}
	
	public static boolean verify(PublicKey publicKey, byte[] signatureData,
	byte[] plainData) throws GeneralSecurityException {
		Signature signature = Signature.getInstance("SHA256withECDSA");
		signature.initVerify(publicKey);
		signature.update(plainData);
		return signature.verify(signatureData);
	}
	
	
}


