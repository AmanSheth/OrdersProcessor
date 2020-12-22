package processor;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.NumberFormat;
import java.util.TreeMap;
import java.util.Map;
import java.util.Scanner;

public class CustomerOrder implements Runnable{
	private TreeMap<String, Integer> orders;
	private File orderReferenceFile;
	private TreeMap<String, Double> productPrices;
	
	public CustomerOrder(String fileName, TreeMap<String, Double> productPrices) {
		this.orderReferenceFile = new File(fileName);
		this.productPrices = productPrices;
		orders = new TreeMap<String, Integer>();
	}

	@Override
	public void run() {
		try {
			Scanner scanner = new Scanner(orderReferenceFile);
			scanner.next();
			System.out.println("Reading order for client with id: " + scanner.next());
			while(scanner.hasNext()) {
				String temp = scanner.next();
				if(!orders.containsKey(temp)) { orders.put(temp, 1); }
				else { orders.put(temp, orders.get(temp) + 1); }
				scanner.next();
			}
			scanner.close();
		} catch (FileNotFoundException e) { e.printStackTrace(); }
	}
	
	public double getTotal() {
		double total = 0;
		for(Map.Entry<String, Integer> entry: orders.entrySet()) {
			total += orders.get(entry.getKey()).doubleValue() * productPrices.get(entry.getKey());
		}
		return total;
	}
	
	public String toString() {
		NumberFormat df = NumberFormat.getInstance();
		df.setGroupingUsed(true);
		df.setMaximumFractionDigits(2);
		df.setMinimumFractionDigits(2);
		String returnStr = "";
		for(Map.Entry<String, Integer> entry: orders.entrySet()) {
			if(entry.getValue() > 0) {
				returnStr += "Item's name: " + entry.getKey() + ", " + "Cost per item: $" + df.format(productPrices.get(entry.getKey())) + ", Quantity: " + entry.getValue()
					+ ", Cost: $" + df.format(entry.getValue() * productPrices.get(entry.getKey())) + "\n";
			}
		}
		
		returnStr += "Order Total: $" + df.format(this.getTotal()) + "\n";
		return returnStr;
	}
	
	public Integer get(String product) { 
		if(orders.containsKey(product)) {
			return orders.get(product);
		} else {
			return 0;
		}
	}
}
