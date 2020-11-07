package block;
import java.security.*;
import java.util.ArrayList;

public class Transaction {
	
	public String transactionId;
	public PublicKey sender;
	public PublicKey reciepient; 
	public float value;
	public byte[] signature; 
	
	public ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
	public ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>();
	
	private static int sequence = 0; 
	
	// Constructor: 
	public Transaction(PublicKey from, PublicKey to, float value,  ArrayList<TransactionInput> inputs) {
		this.sender = from;
		this.reciepient = to;
		this.value = value;
		this.inputs = inputs;
	}
	
	public boolean processTransaction() {
		
		if(verifySignature() == false) {
			System.out.println("거래 서명 오류");
			return false;
		}
				
		//트랜잭션 사용을 체크함
		for(TransactionInput i : inputs) {
			i.UTXO = Main.UTXOs.get(i.transactionOutputId);
		}

		//Checks if transaction is valid:
		if(getInputsValue() < Main.minimumTransaction) {
			System.out.println("Transaction Inputs too small: " + getInputsValue());
			System.out.println("Please enter the amount greater than " + Main.minimumTransaction);
			return false;
		}
		
		//Generate transaction outputs:
		float leftOver = getInputsValue() - value; //get value of inputs then the left over change:
		transactionId = calulateHash();
		outputs.add(new TransactionOutput( this.reciepient, value,transactionId)); //send value to recipient
		outputs.add(new TransactionOutput( this.sender, leftOver,transactionId)); //send the left over 'change' back to sender		
				
		//미사용
		for(TransactionOutput o : outputs) {
			Main.UTXOs.put(o.id , o);
		}
		
		//사용 된 UTXO 목록에서 트랜잭션 입력을 제거합니다.
		for(TransactionInput i : inputs) {
			if(i.UTXO == null) continue; //거래가 없어서 그냥 skip
			Main.UTXOs.remove(i.UTXO.id);
		}
		
		return true;
	}
	
	public float getInputsValue() {
		float total = 0;
		for(TransactionInput i : inputs) {
			if(i.UTXO == null) continue; 
			total += i.UTXO.value;
		}
		return total;
	}
	
	public void generateSignature(PrivateKey privateKey) {
		String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(reciepient) + Float.toString(value)	;
		signature = StringUtil.applyECDSASig(privateKey,data);		
	}
	
	public boolean verifySignature() {
		String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(reciepient) + Float.toString(value)	;
		return StringUtil.verifyECDSASig(sender, data, signature);
	}
	
	public float getOutputsValue() {
		float total = 0;
		for(TransactionOutput o : outputs) {
			total += o.value;
		}
		return total;
	}
	
	private String calulateHash() {
		sequence++; //동일한 해시를 갖는 2 개의 동일한 트랜잭션을 피하기 위해 시퀀스를 늘립니다
		return StringUtil.applySha256(
				StringUtil.getStringFromKey(sender) +
				StringUtil.getStringFromKey(reciepient) +
				Float.toString(value) + sequence
				);
	}
}
