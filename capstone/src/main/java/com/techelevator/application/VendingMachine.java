package com.techelevator.application;

import com.techelevator.models.CurrencyController;
import com.techelevator.models.Inventory;
import com.techelevator.models.exceptions.AmountLessThanOneException;
import com.techelevator.models.exceptions.InsufficientFundsException;
import com.techelevator.models.exceptions.InvalidIDException;
import com.techelevator.models.exceptions.SoldOutException;
import com.techelevator.models.file_io.Logger;
import com.techelevator.models.file_io.SalesReportPrinter;
import com.techelevator.models.products.Product;
import com.techelevator.ui.UserInput;
import com.techelevator.ui.UserOutput;
import java.math.BigDecimal;
import java.math.BigInteger;

public class VendingMachine
{
    private Inventory inventory = new Inventory();
    private CurrencyController currencyController = new CurrencyController();
    private Logger logger = new Logger("data");
    private SalesReportPrinter salesReportPrinter = new SalesReportPrinter("data");


    public void run()
    {
        // clear screen
        UserOutput.clearScreen();

        // display a welcome screen
        welcome();

        // display main menu
        mainMenu();

    }

    public void welcome() {
        UserOutput.displayWelcomeScreen();

        // prompt user to press enter to continue
        UserInput.pressEnterToContinuePrompt();

    }

    public void mainMenu() {
        String option = "";
        while(!option.equals("3"))
        {
            // clear screen
            UserOutput.clearScreen();

            // display menu
            UserOutput.displayHomeScreenMenu();

            // get user input
            option = UserInput.getSelection();

            // clear screen
            UserOutput.clearScreen();

            mainMenuMakeChoice(option);

        }
    }

    public void mainMenuMakeChoice(String option) {
        switch(option) {
            case "1":
                // display inventory
                displayInventoryScreen();
                break;
            case "2":
                // make purchase
                purchaseMenu();
                break;
            case "3":
                // say goodbye to the user
                UserOutput.goodbye();
                break;
            case "4":
                // print sales report
                salesReportPrinter.printReport();
                UserOutput.salesReportMessage();
                break;
            default:
                UserOutput.printSorryMessage();
                // invalid option try again
                UserOutput.invalidSelection();
                // prompt user to press enter to continue
                UserInput.pressEnterToContinuePrompt();
                break;
        }
    }

    public void displayInventoryScreen() {
        UserOutput.displayInventoryAsGrid(inventory);
        // prompt user to press enter to continue
        UserInput.pressEnterToContinuePrompt();

        // clear screen
        UserOutput.clearScreen();
    }

    public void purchaseMenu(){
        boolean keepLooping = true;

        while (keepLooping) {
            // clear screen
            UserOutput.clearScreen();

            // output vending machine image
            UserOutput.printVendingMachine();

            // show current money provided
            UserOutput.displayMoneyInMachine(currencyController);

            // display three menu options
            UserOutput.displayPurchaseMenu();

            // get user input
            String option = UserInput.getSelection();

            keepLooping = purchaseMenuMakeChoice(option);

        }

    }

    public boolean purchaseMenuMakeChoice(String option) {
        while (true) {
            // Feed Money
            if(option.equals("1")) {

                // display feedMoneyScreen
                feedMoneyScreen();

                // prompt user if they want to add more money or return to previous screen
                String choice = UserInput.addMoreMoneyPrompt();

                // break if they want to return to prev screen
                if (choice.equalsIgnoreCase("n")) break;
            }

            // Purchase product
             else if(option.equals("2"))
            {
                // clear screen
                UserOutput.clearScreen();

                // display inventory
                UserOutput.displayInventoryAsGrid(inventory);

                // show current money provided
                UserOutput.displayMoneyInMachine(currencyController);

                // if there's no money in machine, display no money message
                if (currencyController.getMoneyInMachine().compareTo(BigDecimal.ZERO) <= 0) {
                    UserInput.noMoneyInMachineMessage();
                    break;
                }

                // get user input
                String id = UserInput.getUserItemId();

                // clear screen
                UserOutput.clearScreen();

                validateAndMakePurchase(id);

                // show current money provided
                UserOutput.displayMoneyInMachine(currencyController);

                // if money in machine <= 0, break
                if (currencyController.getMoneyInMachine().compareTo(BigDecimal.ZERO) <= 0) break;

                String choice = UserInput.buyAnotherItemPrompt();

                // break if they want to return to prev screen
                if (choice.equalsIgnoreCase("n")) break;

            }
            // Finish transaction
            else if(option.equals("3"))
            {
                finishTransaction();

               return false;

            }
            else
            {
                // invalid option try again
                UserOutput.printSorryMessage();
                UserOutput.invalidSelection();

                // prompt user to press enter to continue
                UserInput.pressEnterToContinuePrompt();

                // break
                break;

            }
        }
        return true;

    }

    public void finishTransaction() {
        // clear screen
        UserOutput.clearScreen();

        // Logs the dispensing of change
        if (!currencyController.getMoneyInMachine().equals(BigDecimal.ZERO))
            logger.logMessage("DISPENSE CHANGE" , currencyController.getMoneyInMachine(), new BigDecimal(BigInteger.ZERO));

        // dispense change to the user
        UserOutput.dispenseChange(currencyController);

        // prompt user to press enter to continue
        UserInput.pressEnterToContinuePrompt();
    }

    public void validateAndMakePurchase(String id) {
        try {
            if (!inventory.isIDValid(id)) throw new InvalidIDException("\nThe ID you entered is invalid", id);
            Product product = inventory.getProductByID(id);

            // attempt to make purchase
            boolean wasPurchaseSuccessful = purchaseItem(product);

            // if purchase was successful, output vending machine success message
            if(wasPurchaseSuccessful) {
                UserOutput.vendingMachineSuccessMessage(product);

                // Logs the purchase in the transaction logger
                logger.logMessage((product.getName() + " " + product.getId()) , product.getPrice(), currencyController.getMoneyInMachine());

                // Logs the purchase in the Sales Report
                salesReportPrinter.logSale(product);
            }

        } catch (InvalidIDException ex) {
            UserOutput.printSorryMessage();
            System.out.println(ex.getMessage());
        }
    }

    private void feedMoneyScreen() {
        // clear screen
        UserOutput.clearScreen();

        // print money stack graphic
        UserOutput.printMoneyStack();

        // display money in machine
        UserOutput.displayMoneyInMachine(currencyController);

        // get user input
        String payment = UserInput.getPayment();

        // add money to currencyController
        try {
            currencyController.addMoneyToMachine(payment);
            //Log the transaction
            logger.logMessage("FEED MONEY", new BigDecimal(payment), currencyController.getMoneyInMachine());
        }catch (AmountLessThanOneException ex) {
            System.out.println(ex.getMessage());
        } catch (Exception ex) {
            System.out.println("Please enter a whole dollar amount only");
        }
        // show current money provided
        UserOutput.displayMoneyInMachine(currencyController);

    }

    public boolean purchaseItem(Product product){
        BigDecimal price = product.getPrice();
        BigDecimal balance = currencyController.getMoneyInMachine();

        boolean hasInsufficientFunds = currencyController.getMoneyInMachine().compareTo(price) < 0;
        boolean isSoldOut = inventory.getQuantity(product).compareTo(0) <= 0;

        try {
            // only if there is enough money/product in the machine
            if (!hasInsufficientFunds && !isSoldOut) {
                // subtract price from money in machine
                currencyController.subtractMoney(price);
                // decrement quantity in inventory
                inventory.decrementQuantity(product);
                // if successful, return true
                return true;
            }
            else if (hasInsufficientFunds) throw new InsufficientFundsException("\nYou have insufficient funds to purchase that item", balance, price);
            else throw new SoldOutException("\nSOLD OUT", product);
        } catch (InsufficientFundsException ex) {
            System.out.println(ex.getMessage());
        } catch (SoldOutException ex) {
            UserOutput.printSorryMessage();
            System.out.println(ex.getMessage());
        }

        return false;
    }
}
