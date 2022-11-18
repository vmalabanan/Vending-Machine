package com.techelevator.models;

import com.techelevator.models.exceptions.AmountLessThanOneException;
import com.techelevator.models.exceptions.InsufficientFundsException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;

import static org.junit.Assert.*;

public class CurrencyControllerTest {

    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    CurrencyController currencyController;

    @Before
    public void setup(){
        currencyController = new CurrencyController();
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @Test
    public void addMoneyToMachine_ShouldAdd_DollarAmountTendered() throws AmountLessThanOneException {

        //Arrange
        BigDecimal expected = new BigDecimal("10");
        currencyController.addMoneyToMachine("10");

        //Act
        BigDecimal actual = currencyController.getMoneyInMachine();

        //Assert
        assertEquals("Because a new machine should report 10 when 10 is added to it", expected, actual);

    }

    @Test
    public void addMoneyToMachine_ShouldPrint_PleaseEnterValidAmount_OnDecimal() throws AmountLessThanOneException {

        //Arrange
        String expected = "Please enter a whole dollar amount only";

        //Act
        currencyController.addMoneyToMachine("4.5");
        String actual = outputStreamCaptor.toString().trim();

        //Assert
        assertEquals("Because the correct error message should be thrown on decimal input", expected, actual);

    }

    @Test(expected = AmountLessThanOneException.class)
    public void addMoneyToMachine_ShouldPrint_PleaseEnterPositiveAmount_OnZero() throws AmountLessThanOneException {

        //Arrange
        String expected = "Please enter a whole dollar amount greater than or equal to $1";

        //Act
        currencyController.addMoneyToMachine("0");
        String actual = outputStreamCaptor.toString().trim();

        //Assert
        assertEquals("Because the correct error message should be thrown on decimal input", expected, actual);

    }

    @Test
    public void subtractMoney_Should_SubtractPriceFromMoneyInMachine() throws InsufficientFundsException, AmountLessThanOneException {

        //Arrange
        BigDecimal price = new BigDecimal(5);
        BigDecimal expected = new BigDecimal("10");
        currencyController.addMoneyToMachine("15");
        currencyController.subtractMoney(price);

        //Act
        BigDecimal actual = currencyController.getMoneyInMachine();

        //Assert
        assertEquals("Because currencyController should report 5.00 when 10.00 is added and 5 is subtracted", expected, actual);

    }

    @Test(expected = InsufficientFundsException.class)
    public void subtractMoney_ShouldPrint_InsufficientFunds_OnZeroInMachine() throws InsufficientFundsException {

        //Arrange
        String expected = "You have insufficient funds for this purchase";
        BigDecimal price = new BigDecimal(10);

        //Act
        currencyController.subtractMoney(price);
        String actual = outputStreamCaptor.toString().trim();

        //Assert
        assertEquals("Because insufficient funds should be printed when price exceeds available funds", expected, actual);

    }

    @Test(expected = InsufficientFundsException.class)
    public void subtractMoney_ShouldPrint_InsufficientFunds_WhenPriceExceedsAvailableMoney() throws InsufficientFundsException, AmountLessThanOneException {

        //Arrange
        String expected = "You have insufficient funds for this purchase";
        currencyController.addMoneyToMachine("5");
        BigDecimal price = new BigDecimal(10);

        //Act
        currencyController.subtractMoney(price);
        String actual = outputStreamCaptor.toString().trim();

        //Assert
        assertEquals("Because insufficient funds should be printed when price exceeds available funds", expected, actual);

    }



    @After
    public void tearDown(){
        System.setOut(standardOut);
    }
}
