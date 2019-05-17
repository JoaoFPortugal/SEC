package hds_security;

import java.util.ArrayList;

public class NoobChain {
	
	public ArrayList<Block> blockchain = new ArrayList<Block>();
	public int difficulty = 5;
/*
	public static void main(String[] args) {

		NoobChain chain = new NoobChain();

		//add our blocks to the blockchain ArrayList:
		
		chain.blockchain.add(new Block("Hi im the first block", "0"));
		System.out.println("Trying to Mine block 1... ");
		chain.blockchain.get(0).mineBlock(chain.difficulty);
		
		chain.blockchain.add(new Block("Yo im the second block", chain.blockchain.get(chain.blockchain.size()-1).hash));
		System.out.println("Trying to Mine block 2... ");
		chain.blockchain.get(1).mineBlock(chain.difficulty);
		
		chain.blockchain.add(new Block("Hey im the third block", chain.blockchain.get(chain.blockchain.size()-1).hash));
		System.out.println("Trying to Mine block 3... ");
		chain.blockchain.get(2).mineBlock(chain.difficulty);
		
		System.out.println("\nBlockchain is Valid: " + chain.isChainValid());
		
		System.out.println("\nThe block chain: ");
		chain.printContents();
	}
	*/
	
	public Boolean isChainValid() {
		Block currentBlock; 
		Block previousBlock;
		String hashTarget = new String(new char[difficulty]).replace('\0', '0');
		
		//loop through blockchain to check hashes:
		for(int i=1; i < blockchain.size(); i++) {
			currentBlock = blockchain.get(i);
			previousBlock = blockchain.get(i-1);
			//compare registered hash and calculated hash:
			if(!currentBlock.hash.equals(currentBlock.calculateHash()) ){
				System.out.println("Current Hashes not equal");			
				return false;
			}
			//compare previous hash and registered previous hash
			if(!previousBlock.hash.equals(currentBlock.previousHash) ) {
				System.out.println("Previous Hashes not equal");
				return false;
			}
			//check if hash is solved
			if(!currentBlock.hash.substring( 0, difficulty).equals(hashTarget)) {
				System.out.println("This block hasn't been mined");
				return false;
			}
		}
		return true;
	}

	public void printContents() {

		for(int i = 0; i < blockchain.size(); i++) {
			System.out.println("BLOCK " + i);
			blockchain.get(i).printContents();
		}
	}
}