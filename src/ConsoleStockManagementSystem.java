import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.InputMismatchException;
import java.util.Scanner;

public class ConsoleStockManagementSystem {
    private static String[][] stock;
    private static int maxShelves;
    private static int[] slotsPerShelf;
    private static int[] shelfStartIndex;
    private static int totalSlots;
    private static int currentProductCount;
    private static ArrayList<String> insertionHistory;
    private static Scanner scanner;
    private static boolean stockInitialized;

    public static void main(String[] args) {
        try {
            scanner = new Scanner(System.in);
            insertionHistory = new ArrayList<>();
            stockInitialized = false;
            showMenu();
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
    }

    // Helper method to show available stock
    private static void showAvailableStock() {
        if (!stockInitialized || maxShelves == 0) {
            System.out.println("Stock available : None");
            return;
        }

        // Collect non-full shelves
        ArrayList<Integer> nonFullShelves = new ArrayList<>();
        for (int i = 0; i < maxShelves; i++) {
            boolean isFull = true;
            for (int j = 0; j < slotsPerShelf[i]; j++) {
                int slotIndex = shelfStartIndex[i] + j;
                if (stock[slotIndex] == null || stock[slotIndex][0] == null) {
                    isFull = false;
                    break;
                }
            }
            if (!isFull) {
                nonFullShelves.add(i + 1);
            }
        }

        // Display non-full shelves
        if (nonFullShelves.isEmpty()) {
            System.out.println("Stock available : None");
        } else {
            System.out.print("Stock available : ");
            for (int i = 0; i < nonFullShelves.size(); i++) {
                System.out.print(nonFullShelves.get(i));
                if (i < nonFullShelves.size() - 1) {
                    System.out.print("|");
                }
            }
            System.out.println();
        }
    }

    // Helper method to format current date and time
    private static String formatInsertionTime(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("EEEE, MMMM, d, yyyy, HH:mm:ss");
        return formatter.format(date);
    }

    //========== 1. Set Up Stock with Catalogue ==============
    private static void initializeStock() {
        try {
            System.out.print("[+] Enter the number of stocks: ");
            maxShelves = scanner.nextInt();
            scanner.nextLine(); // Clear buffer
            if (maxShelves <= 0) {
                throw new IllegalArgumentException("Number of stocks must be positive.");
            }
            slotsPerShelf = new int[maxShelves];
            shelfStartIndex = new int[maxShelves];
            totalSlots = 0;
            insertionHistory.clear();
            stockInitialized = true;

            // Prompt for number of catalogues per stock
            System.out.println("[+] Insert the number of catalogue for each stock:");
            for (int i = 0; i < maxShelves; i++) {
                System.out.print("[+] Insert number of catalogue on stock [" + (i + 1) + "]: ");
                int slots = scanner.nextInt();
                scanner.nextLine(); // Clear buffer
                if (slots <= 0) {
                    throw new IllegalArgumentException("Number of catalogues must be positive.");
                }
                slotsPerShelf[i] = slots;
                shelfStartIndex[i] = totalSlots; // Start index for this shelf
                totalSlots += slots;
            }

            // Initialize stock array
            stock = new String[totalSlots][3];
            currentProductCount = 0;

            // Prompt to fill slots
            System.out.println("[+] Enter products for up to " + totalSlots + " slots across " + maxShelves + " stocks. Enter shelf 0 to finish.");
            while (currentProductCount < totalSlots) {
                try {
                    showAvailableStock();
                    System.out.print("[+] Enter shelf number (1-" + maxShelves + ", 0 to finish): ");
                    int shelf = scanner.nextInt();
                    scanner.nextLine(); // Clear buffer
                    if (shelf == 0) {
                        break; // Exit if user chooses to finish
                    }
                    if (shelf < 1 || shelf > maxShelves) {
                        throw new IllegalArgumentException("Invalid shelf number. Choose between 1 and " + maxShelves + ".");
                    }
                    int shelfIndex = shelf - 1;
                    System.out.print("[+] Enter slot number (1-" + slotsPerShelf[shelfIndex] + "): ");
                    int slot = scanner.nextInt();
                    scanner.nextLine(); // Clear buffer
                    if (slot < 1 || slot > slotsPerShelf[shelfIndex]) {
                        throw new IllegalArgumentException("Invalid slot number. Choose between 1 and " + slotsPerShelf[shelfIndex] + ".");
                    }
                    int slotIndex = shelfStartIndex[shelfIndex] + slot - 1;
                    // Check if slot is occupied
                    if (stock[slotIndex] != null && stock[slotIndex][0] != null) {
                        throw new IllegalArgumentException("Slot [" + slot + "] on shelf [" + shelf + "] is occupied by [" + stock[slotIndex][0] + "].");
                    }
                    System.out.print("[+] Enter product name for shelf [" + shelf + "], slot [" + slot + "]: ");
                    String name = scanner.nextLine().trim();
                    if (name.isEmpty()) {
                        throw new IllegalArgumentException("Product name cannot be empty.");
                    }
                    // Check for duplicate product name
                    for (int i = 0; i < totalSlots; i++) {
                        if (stock[i] != null && stock[i][0] != null && stock[i][0].equalsIgnoreCase(name)) {
                            int[] location = getShelfAndSlot(i);
                            throw new IllegalArgumentException("Product [" + name + "] already exists in shelf " + location[0] + ", slot " + location[1] + ".");
                        }
                    }
                    System.out.print("[+] Enter quantity of product: ");
                    int quantity = scanner.nextInt();
                    if (quantity < 0) {
                        throw new IllegalArgumentException("Quantity cannot be negative.");
                    }
                    System.out.print("[+] Enter price of product: ");
                    double price = scanner.nextDouble();
                    if (price < 0) {
                        throw new IllegalArgumentException("Price cannot be negative.");
                    }
                    scanner.nextLine(); // Clear buffer

                    stock[slotIndex] = new String[]{name, String.valueOf(quantity), String.valueOf(price)};
                    currentProductCount++;
                    // Log insertion with date, time, and product name
                    String timeString = formatInsertionTime(Date.from(Instant.now()));
                    insertionHistory.add(timeString + "," + name);
                    System.out.println("Product [" + name + "] added to shelf [" + shelf + "], slot [" + slot + "].");
                } catch (InputMismatchException e) {
                    System.out.println("[+] Please enter valid numeric values.");
                    scanner.nextLine(); // Clear invalid input
                } catch (IllegalArgumentException e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
            System.out.println("----- SET UP STOCK SUCCEEDED -----");
            showAvailableStock();
        } catch (InputMismatchException e) {
            System.out.println("Please enter valid numeric values for stock or catalogue counts.");
            scanner.nextLine(); // Clear invalid input
            stockInitialized = false;
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
            stockInitialized = false;
        }
    }

    //==========  2. View Product in Stock ==========
    private static void viewProducts() {
        if (!stockInitialized) {
            System.out.println("Stock is not initialized. Please set up the stock first.");
            return;
        }
        try {
            System.out.println("-------- View Stock --------");
            for (int i = 0; i < maxShelves; i++) {
                System.out.print("Stock [" + (i + 1) + "] => ");
                for (int j = 0; j < slotsPerShelf[i]; j++) {
                    int slotIndex = shelfStartIndex[i] + j;
                    if (stock[slotIndex] != null && stock[slotIndex][0] != null) {
                        System.out.print("[ " + stock[slotIndex][0] + " ] ");
                    } else {
                        System.out.print("[ " + (j + 1) + " - EMPTY ] ");
                    }
                }
                System.out.println();
            }
        } catch (Exception e) {
            System.out.println("Error displaying products: " + e.getMessage());
        }
    }

    //==========  3. Insert Product to Stock Catalogue ==========
    private static void insertProduct() {
        if (!stockInitialized) {
            System.out.println("Stock is not initialized. Please set up the stock first.");
            return;
        }
        if (currentProductCount >= totalSlots) {
            System.out.println("Stock is full. Cannot add more products.");
            return;
        }
        try {
            showAvailableStock();
            System.out.print("[+] Enter shelf number to insert product (1-" + maxShelves + "): ");
            int shelf = scanner.nextInt();
            scanner.nextLine(); // Clear buffer
            if (shelf < 1 || shelf > maxShelves) {
                throw new IllegalArgumentException("Invalid shelf number. Choose between 1 and " + maxShelves + ".");
            }
            int shelfIndex = shelf - 1;
            System.out.print("[+] Enter slot number (1-" + slotsPerShelf[shelfIndex] + ") to insert product: ");
            int slot = scanner.nextInt();
            scanner.nextLine(); // Clear buffer
            if (slot < 1 || slot > slotsPerShelf[shelfIndex]) {
                throw new IllegalArgumentException("Invalid slot number. Choose between 1 and " + slotsPerShelf[shelfIndex] + ".");
            }
            int slotIndex = shelfStartIndex[shelfIndex] + slot - 1;
            if (stock[slotIndex] != null && stock[slotIndex][0] != null) {
                throw new IllegalArgumentException("Slot [" + slot + "] on shelf [" + shelf + "] is occupied by [" + stock[slotIndex][0] + "]. Use update to modify.");
            }
            System.out.print("[+] Enter product name: ");
            String name = scanner.nextLine().trim();
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Product name cannot be empty.");
            }
            for (int i = 0; i < totalSlots; i++) {
                if (stock[i] != null && stock[i][0] != null && stock[i][0].equalsIgnoreCase(name)) {
                    int[] location = getShelfAndSlot(i);
                    throw new IllegalArgumentException("Product [" + name + "] already exists in shelf [" + location[0] + "], slot [" + location[1] + "].");
                }
            }
            System.out.print("[+] Enter quantity of product: ");
            int quantity = scanner.nextInt();
            if (quantity < 0) {
                throw new IllegalArgumentException("Quantity cannot be negative.");
            }
            System.out.print("[+] Enter price of product: ");
            double price = scanner.nextDouble();
            if (price < 0) {
                throw new IllegalArgumentException("Price cannot be negative.");
            }
            scanner.nextLine(); // Clear buffer

            stock[slotIndex] = new String[]{name, String.valueOf(quantity), String.valueOf(price)};
            currentProductCount++;
            // Log insertion with date, time, and product name
            String timeString = formatInsertionTime(Date.from(Instant.now()));
            insertionHistory.add(timeString + "," + name);
            System.out.println("Product [" + name + "] added to shelf " + shelf + ", slot [" + slot + "].");
        } catch (InputMismatchException e) {
            System.out.println("Error: Please enter valid numeric values.");
            scanner.nextLine(); // Clear invalid input
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ========== 4. Update Product in Stock Catalogue by Product Name ==========
    private static void updateProduct() {
        if (!stockInitialized) {
            System.out.println("Error: Stock is not initialized. Please set up the stock first.");
            return;
        }
        try {
            System.out.print("[+] Enter product name to update: ");
            String name = scanner.nextLine().trim();
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Product name cannot be empty.");
            }
            int index = findProductIndex(name);
            if (index == -1) {
                System.out.println("Product not found.");
                return;
            }

            System.out.print("[+] Enter new product name: ");
            String newName = scanner.nextLine().trim();
            if (newName.isEmpty()) {
                throw new IllegalArgumentException("New product name cannot be empty.");
            }

            System.out.print("[+] Enter new quantity of product: ");
            int quantity = scanner.nextInt();
            if (quantity < 0) {
                throw new IllegalArgumentException("Quantity cannot be negative.");
            }
            System.out.print("[+] Enter new price of product: ");
            double price = scanner.nextDouble();
            if (price < 0) {
                throw new IllegalArgumentException("Price cannot be negative.");
            }
            scanner.nextLine(); // Clear buffer

            // Update stock
            stock[index][0] = newName;
            stock[index][1] = String.valueOf(quantity);
            stock[index][2] = String.valueOf(price);

            // Update insertion history to reflect the new name
            for (int i = 0; i < insertionHistory.size(); i++) {
                if (insertionHistory.get(i).contains(name)) { // Match the old name in history
                    insertionHistory.set(i, insertionHistory.get(i).replace(name, newName)); // Replace old name with new name
                }
            }

            System.out.println("Product updated successfully.");
        } catch (InputMismatchException e) {
            System.out.println("Error: Please enter valid numeric values for quantity and price.");
            scanner.nextLine(); // Clear invalid input
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ========== 5. Delete Product in Stock Catalogue by Name ==========
    private static void deleteProduct() {
        if (!stockInitialized) {
            System.out.println("Error: Stock is not initialized. Please set up the stock first.");
            return;
        }
        try {
            System.out.print("[+] Enter product name to delete: ");
            String name = scanner.nextLine().trim();
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Product name cannot be empty.");
            }
            int index = findProductIndex(name);
            if (index == -1) {
                System.out.println("---------- Product not found. ----------");
                return;
            }
            stock[index] = null;
            currentProductCount--;
            System.out.println("---------- Product deleted successfully. ----------");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ========== 6. View Insertion History in Stock Catalogue ==========
    private static void viewInsertionHistory() {
        if (!stockInitialized) {
            System.out.println("Error: Stock is not initialized. Please set up the stock first.");
            return;
        }
        try {
            if (insertionHistory.isEmpty()) {
                System.out.println("No insertion history available.");
                return;
            }
            System.out.println("\nInsertion History:");
            for (String entry : insertionHistory) {
                String[] parts = entry.split(",");
                String dateTime = parts[0] + "," + parts[1] + "," + parts[2] + "," + parts[3] + "," + parts[4];
                String productName = parts[5];
                System.out.println("Inserted at [" + dateTime + "], Product: [" + productName + "]");
            }
        } catch (Exception e) {
            System.out.println("Error displaying insertion history: " + e.getMessage());
        }
    }

    // Helper method to find product index by name
    private static int findProductIndex(String name) {
        try {
            for (int i = 0; i < totalSlots; i++) {
                if (stock[i] != null && stock[i][0] != null && stock[i][0].equalsIgnoreCase(name)) {
                    return i;
                }
            }
            return -1;
        } catch (Exception e) {
            System.out.println("Error searching for product: " + e.getMessage());
            return -1;
        }
    }

    // Helper method to get shelf and slot from stock index
    private static int[] getShelfAndSlot(int index) {
        for (int i = 0; i < maxShelves; i++) {
            if (index >= shelfStartIndex[i] && index < shelfStartIndex[i] + slotsPerShelf[i]) {
                int slot = index - shelfStartIndex[i] + 1;
                return new int[]{i + 1, slot};
            }
        }
        return new int[]{-1, -1};
    }


    // Display menu
    private static void showMenu() {
        while (true) {
            try {
                System.out.println("\n--------- Console Stock Management System -----------");
                System.out.println("1. Set Up Stock with Catalogue");
                System.out.println("2. View Product in Stock");
                System.out.println("3. Insert Product to Stock Catalogue");
                System.out.println("4. Update Product in Stock Catalogue by Product Name");
                System.out.println("5. Delete Product in Stock Catalogue by Name");
                System.out.println("6. View Insertion History in Stock Catalogue");
                System.out.println("7. Exit");
                System.out.print("[*] Choose an option (1-7): ");
                int choice = scanner.nextInt();
                scanner.nextLine(); // Clear buffer

                switch (choice) {
                    case 1 -> initializeStock();
                    case 2 -> viewProducts();
                    case 3 -> insertProduct();
                    case 4 -> updateProduct();
                    case 5 -> deleteProduct();
                    case 6 -> viewInsertionHistory();
                    case 7 -> {
                        System.out.println("---------- Exiting system. Goodbye! ----------");
                        return;
                    }
                    default -> System.out.println("Error: Invalid option. Please choose between 1 and 7.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Error: Please enter a valid integer for the menu option.");
                scanner.nextLine(); // Clear invalid input
            } catch (Exception e) {
                System.out.println("Unexpected error: " + e.getMessage());
                scanner.nextLine(); // Clear buffer
            }
        }
    }
}