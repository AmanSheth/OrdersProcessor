/**
 * 
 */
package processor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.TreeMap;
import java.util.Map;
import java.util.Scanner;

/**
 * @author Aman Sheth
 *
 */
public class OrdersProcessor {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	
			Scanner scanner = new Scanner(System.in);
			
			System.out.println("Enter item's data file name: ");
			String referenceFileName = scanner.next();
			
			TreeMap<String, Double> itemCosts = new TreeMap<String, Double>();
			Scanner referenceScanner;
			try {
				referenceScanner = new Scanner(new File(referenceFileName));
				while(referenceScanner.hasNextLine()) {
					String temp = referenceScanner.next();
					Double tempPrice = referenceScanner.nextDouble();
					itemCosts.put(temp, tempPrice);
				}
				referenceScanner.close();
			} catch (FileNotFoundException e) { e.printStackTrace(); }
			
			System.out.println("Enter 'y' for multiple threads, any other character otherwise: ");
			String tempMultithreaded = scanner.next();
			boolean multithreaded = (tempMultithreaded.equalsIgnoreCase("y")) ? true : false;
		
			System.out.println("Enter number of orders to process: ");
			int numberOfOrders = scanner.nextInt();
			
			System.out.println("Enter order's base filename: ");
			String dataFileHeader = scanner.next();
			
			System.out.println("Enter result's filename: ");
			String resultFile = scanner.next();
			
			long startTime = System.currentTimeMillis();
			
			scanner.close();
			
			TreeMap<Integer, CustomerOrder> customers = new TreeMap<Integer, CustomerOrder>();
			if(multithreaded) {
				multithreadedSolution(customers, dataFileHeader, referenceFileName, numberOfOrders, itemCosts);
			} else {
				nonMultithreadedSolution(customers, dataFileHeader, referenceFileName, numberOfOrders, itemCosts);
			}
			writeToFile(customers, itemCosts, resultFile);
			
			long endTime = System.currentTimeMillis();
			System.out.println("Processing time (msec): " + (endTime - startTime));
			
			System.out.println("Results can be found in the file: " + resultFile);
	}
	
	private static void multithreadedSolution(TreeMap<Integer, CustomerOrder> customers, String dataFileHeader, String referenceFileName, int customerNumber, TreeMap<String, Double> productPrices) {
		Thread[] threads = new Thread[customerNumber];
		
		synchronized(customers) {
			for(int i = 1; i <= customerNumber; i++) {
				try {
					Scanner tempScanner = new Scanner(new File(dataFileHeader + Integer.toString(i) + ".txt"));
					tempScanner.next();
					
					CustomerOrder tempCO = new CustomerOrder(dataFileHeader + Integer.toString(i) + ".txt", productPrices);
					
					Thread t = new Thread(tempCO);
					threads[i-1] = t;
					
					t.start();
					
					customers.put(tempScanner.nextInt(), tempCO);
					tempScanner.close();
				} catch (FileNotFoundException e) { e.printStackTrace(); }
			}			
		}
		
		//makes sure all threads are closed before continuing program
		for(Thread thread: threads) {
			try { thread.join(); } 
			catch (InterruptedException e) { e.printStackTrace(); } 
		}
	}

	private static void nonMultithreadedSolution(TreeMap<Integer, CustomerOrder> customers, String dataFileHead, String referenceFileName, int customerNumber, TreeMap<String, Double> productPrices) {
   		for(int i = 1; i <= customerNumber; i++) {
			try {
				
				Scanner tempScanner = new Scanner(new File(dataFileHead + Integer.toString(i) + ".txt"));
				tempScanner.next();
				
				CustomerOrder tempOrder = new CustomerOrder(dataFileHead + Integer.toString(i) + ".txt", productPrices);
				tempOrder.run();
				
				customers.put(tempScanner.nextInt(), tempOrder);
			} catch (FileNotFoundException e) { e.printStackTrace(); }
		}
	}
	
	private static void writeToFile(TreeMap<Integer, CustomerOrder> orders, TreeMap<String, Double> itemCosts, String targetFile) {
		NumberFormat df = NumberFormat.getInstance();
		df.setGroupingUsed(true);
		df.setMaximumFractionDigits(2);
		df.setMinimumFractionDigits(2);
		
		FileWriter output;
		try {
			output = new FileWriter(targetFile);	
			
			for(Map.Entry<Integer, CustomerOrder> entry: orders.entrySet()) {
				output.write("----- Order details for client with Id: " + entry.getKey() + " -----\n");
				output.write(entry.getValue().toString());        
			} 
			
			output.write("***** Summary of all orders *****\n");
			
			double total = 0;
			for(String product: itemCosts.keySet()) {
				int productCount = 0;
				for(Integer key: orders.keySet()) { productCount += orders.get(key).get(product); }
				
				if(productCount > 0) {
					output.write("Summary - Item's name: " + product + ", Cost per item: $" + df.format(itemCosts.get(product)));
				
					output.write(", Number sold: " + productCount + ", Item's Total: $" + df.format(productCount * itemCosts.get(product)));
				}
				
				total += productCount * itemCosts.get(product);
			}
			
			output.write("\nSummary Grand Total: $" + df.format(total));
			
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
