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
            	MoneyLaundering.transactionAnalyzer.addTransaction(transaction);
            }
            MoneyLaundering.amountOfFilesProcessed.incrementAndGet();
        }
	}
}