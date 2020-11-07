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
	
	//블록 생성자
	
	public Block(String previousHash ) {
		this.previousHash = previousHash;
		this.timeStamp = new Date().getTime();
		
		this.hash = calculateHash();
	}
	
	//블록 내용을 기반으로 해시 생성하기
	public String calculateHash() {
		String calculatedhash = StringUtil.applySha256( 
				previousHash +
				Long.toString(timeStamp) +
				Integer.toString(nonce) + 
				merkleRoot
				);
		return calculatedhash;
	}
	
	//해시 대상에 도달 할 때까지 nonce 값을 늘립니다
	public void mineBlock(int difficulty) {
		merkleRoot = StringUtil.getMerkleRoot(transactions);
		String target = StringUtil.getDificultyString(difficulty); //Create a string with difficulty * "0" 
		while(!hash.substring( 0, difficulty).equals(target)) {
			nonce ++;
			hash = calculateHash();
		}
		// System.out.println("채굴된 블록 : " + hash);
	}
	
	//블록에 거래추가
	public boolean addTransaction(Transaction transaction) {
		//		트랜잭션을 처리하고 블록이 제네시스 블록이 아닌 경우 유효한지 확인한 다음 무시합니다.
		if(transaction == null) return false;		
		if((!"0".equals(previousHash))) {
			if((transaction.processTransaction() != true)) {
				System.out.println("거래 오류");
				return false;
			}
		}

		transactions.add(transaction);
		System.out.println("거래 성공");
		return true;
	}
	
}
