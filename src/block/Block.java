package block;

import java.util.ArrayList;
import java.util.Date;

public class Block {
	
	public String hash;
	public String previousHash; 
	public String merkleRoot;
	public ArrayList<Transaction> transactions = new ArrayList<Transaction>(); //our data will be a simple message.
	public long timeStamp; 
	public int nonce;
	
	//��� ������
	
	public Block(String previousHash ) {
		this.previousHash = previousHash;
		this.timeStamp = new Date().getTime();
		
		this.hash = calculateHash();
	}
	
	//��� ������ ������� �ؽ� �����ϱ�
	public String calculateHash() {
		String calculatedhash = StringUtil.applySha256( 
				previousHash +
				Long.toString(timeStamp) +
				Integer.toString(nonce) + 
				merkleRoot
				);
		return calculatedhash;
	}
	
	//�ؽ� ��� ���� �� ������ nonce ���� �ø��ϴ�
	public void mineBlock(int difficulty) {
		merkleRoot = StringUtil.getMerkleRoot(transactions);
		String target = StringUtil.getDificultyString(difficulty); //Create a string with difficulty * "0" 
		while(!hash.substring( 0, difficulty).equals(target)) {
			nonce ++;
			hash = calculateHash();
		}
		// System.out.println("ä���� ��� : " + hash);
	}
	
	//��Ͽ� �ŷ��߰�
	public boolean addTransaction(Transaction transaction) {
		//		Ʈ������� ó���ϰ� ����� ���׽ý� ����� �ƴ� ��� ��ȿ���� Ȯ���� ���� �����մϴ�.
		if(transaction == null) return false;		
		if((!"0".equals(previousHash))) {
			if((transaction.processTransaction() != true)) {
				System.out.println("�ŷ� ����");
				return false;
			}
		}

		transactions.add(transaction);
		System.out.println("�ŷ� ����");
		return true;
	}
	
}
