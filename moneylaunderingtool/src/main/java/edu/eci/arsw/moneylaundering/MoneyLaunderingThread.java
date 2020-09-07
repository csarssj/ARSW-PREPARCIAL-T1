package edu.eci.arsw.moneylaundering;


import java.io.File;
import java.util.List;

public class MoneyLaunderingThread extends Thread {
    private TransactionReader transactionReader;
    private List<File> transactionFiles;
	public MoneyLaunderingThread (List<File > files) {
		this.transactionFiles = files;
		this.transactionReader = new TransactionReader();
	}
	
	public void run() {
		for(File transactionFile : transactionFiles)
        {            
            List<Transaction> transactions = transactionReader.readTransactionsFromFile(transactionFile);
            for(Transaction transaction : transactions)
            {	
            	synchronized(this) {
            			if(MoneyLaundering.pause) {
            				try {
								this.wait();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
            				
            			}
            	}
            	MoneyLaundering.transactionAnalyzer.addTransaction(transaction);
            }
            MoneyLaundering.amountOfFilesProcessed.incrementAndGet();
        }
	}
	public synchronized void resumen(){
		notifyAll();
	}
	
}