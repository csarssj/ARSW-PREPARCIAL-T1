package edu.eci.arsw.moneylaundering;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MoneyLaundering
{
    public static TransactionAnalyzer transactionAnalyzer;
    private TransactionReader transactionReader;
    private int amountOfFilesTotal;
    public static AtomicInteger amountOfFilesProcessed;
    private int hilos = 5;
    public static boolean pause;

    public static List<MoneyLaunderingThread> threads;

    public MoneyLaundering()
    {
        transactionAnalyzer = new TransactionAnalyzer();
        transactionReader = new TransactionReader();
        amountOfFilesProcessed = new AtomicInteger();
        pause = false;
    }

    public void processTransactionData()
    {
        amountOfFilesProcessed.set(0);
        List<File> transactionFiles = getTransactionFileList();
        amountOfFilesTotal = transactionFiles.size();
        int count = amountOfFilesTotal/hilos;
        int mod = amountOfFilesTotal%hilos;
        int inicio = 0;
        int fin = count;threads = new ArrayList<>();
        for (int i = 0; i < hilos; i++) {
        	if(i == hilos -1) {
        		fin += mod;
        	}
            List<File> aux = new ArrayList<>();
        	for(int j=inicio; j<fin;j++) {
        		aux.add(transactionFiles.get(j));
        	}
        	
        	MoneyLaunderingThread hilo = new MoneyLaunderingThread(aux);
        	hilo.start();
        	threads.add(hilo);
        	inicio = fin;
        	fin +=count;
        }
        for(MoneyLaunderingThread t:threads) {
        	try {
				t.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }

    public List<String> getOffendingAccounts()
    {
        return transactionAnalyzer.listOffendingAccounts();
    }

    private List<File> getTransactionFileList()
    {
        List<File> csvFiles = new ArrayList<>();
        try (Stream<Path> csvFilePaths = Files.walk(Paths.get("src/main/resources/")).filter(path -> path.getFileName().toString().endsWith(".csv"))) {
            csvFiles = csvFilePaths.map(Path::toFile).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return csvFiles;
    }

    public static void main(String[] args)
    {
        MoneyLaundering moneyLaundering = new MoneyLaundering();
        Thread processingThread = new Thread(() -> moneyLaundering.processTransactionData());
        processingThread.start();
        while(true)
        {
            Scanner scanner = new Scanner(System.in);
            String line = scanner.nextLine();
            if(line.contains("exit"))
                break;
            if(line.isEmpty()) {
            	if(!pause) {
            		System.out.println("Pausa");
		            String message = "Processed %d out of %d files.\nFound %d suspect accounts:\n%s";
		            List<String> offendingAccounts = moneyLaundering.getOffendingAccounts();
		            String suspectAccounts = offendingAccounts.stream().reduce("", (s1, s2)-> s1 + "\n"+s2);
		            message = String.format(message, moneyLaundering.amountOfFilesProcessed.get(), moneyLaundering.amountOfFilesTotal, offendingAccounts.size(), suspectAccounts);
		            System.out.println(message);
		            pause = true;
            	}else{
            		System.out.println("Continuar");
            		pause = false;
            		for (MoneyLaunderingThread t: threads) {
            			t.resumen();
            		}
            	}
            	
            	
            }
        }

    }


}
