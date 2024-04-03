package com.playtech.assignment;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


// This template shows input parameters format.
// It is otherwise not mandatory to use, you can write everything from scratch if you wish.
public class TransactionProcessorSample {

    public static void main(final String[] args) throws IOException {


        /*
        Path userFilePath = Paths.get("test-data/manual test data 75% validations/input/users.csv");
        Path transactionFilePath = Paths.get("test-data/manual test data 75% validations/input/transactions.csv");
        Path binsFilePath = Paths.get("test-data/manual test data 75% validations/input/bins.csv");*/
        /*
        Path userFilePath = Paths.get("test-data/test random data (small)/input/users.csv");
        Path transactionFilePath = Paths.get("test-data/test random data (small)/input/transactions.csv");
        Path binsFilePath = Paths.get("test-data/test random data (small)/input/bins.csv");*/
        /*
        Path userFilePath = Paths.get("test-data/test random data 50% validations/input/users.csv");
        Path transactionFilePath = Paths.get("test-data/test random data 50% validations/input/transactions.csv");
        Path binsFilePath = Paths.get("test-data/test random data 50% validations/input/bins.csv");*/

        try {
            
            List<User> users = TransactionProcessorSample.readUsers(Paths.get(args[0]));
            List<Transaction> transactions = TransactionProcessorSample.readTransactions(Paths.get(args[1]));
            List<BinMapping> binMappings = TransactionProcessorSample.readBinMappings(Paths.get(args[2]));
            List<Event> events = processTransactions(users, transactions, binMappings);
            writeBalances(Paths.get(args[3]), users);
            writeEvents(Paths.get(args[4]), events);
            /*
            List<User> users = TransactionProcessorSample.readUsers(userFilePath);
            List<Transaction> transactions = TransactionProcessorSample.readTransactions(transactionFilePath);
            List<BinMapping> binMappings = TransactionProcessorSample.readBinMappings(binsFilePath);

            List<Event> events = processTransactions(users, transactions, binMappings);
            for (Event event : events){
                System.out.println(event);
            }
            Path eventsOutputPath = Paths.get("eventsOutput.csv");
            writeEvents(eventsOutputPath, events);
            Path balanceOutputPath = Paths.get("balanceOutput.csv");
            writeBalances(balanceOutputPath, users);*/

        }catch (IOException e){
            System.out.println(e.getMessage());
        }

    }

    /**
     * Function that reads Users from file and puts them into list
     * @param filePath The file we are reading from
     * @return Returns List that contains all Users
     * @throws IOException For example when filepath is wrong
     */
    private static List<User> readUsers(final Path filePath) throws IOException{
        List<User> users = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
            String line;
            reader.readLine(); //To read first unnecessary line
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                try {
                    User user = new User(parts[0], parts[1], new BigDecimal(parts[2]), parts[3], Integer.parseInt(parts[4]), new BigDecimal(parts[5]), new BigDecimal(parts[6]), new BigDecimal(parts[7]), new BigDecimal(parts[8]));
                    users.add(user);
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    System.err.println("Error creating user: " + e.getMessage());
                }
            }
        }
        return users;
    }

    /**
     * Function that reads Transactions from file and puts them into list
     * @param filePath The file we are reading from
     * @return returns List that contains all Transactions
     * @throws IOException For example when filepath is wrong
     */
    private static List<Transaction> readTransactions(final Path filePath) throws IOException {
        List<Transaction> transactions = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))){
            String line;
            reader.readLine(); //To read first unnecessary line
            while ((line = reader.readLine()) != null){
                String[] parts = line.split(",");
                try {
                    Transaction transaction = new Transaction(parts[0], parts[1], parts[2], new BigDecimal(parts[3]), parts[4], parts[5]);
                    transactions.add(transaction);
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    System.err.println("Error creating transaction: " + e.getMessage());
                }

            }
        }
        return transactions;
    }

    /**
     * Function that reads BinMappings from file and puts them into List
     * @param filePath The file path where binmappings are
     * @return Returns List that contains all Binmappings
     * @throws IOException For example when filepath is wrong
     */
    private static List<BinMapping> readBinMappings(final Path filePath) throws IOException{
        List<BinMapping> binMappings = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))){
            String line;
            reader.readLine(); //To read first unnecessary line
            while ((line = reader.readLine()) != null){
                String[] parts = line.split(",");
                try {
                    BinMapping binMapping = new BinMapping(parts[0], parts[1], parts[2], parts[3], parts[4]);
                    binMappings.add(binMapping);
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.err.println("Error creating mapping: " + e.getMessage());
                }
            }
        }
        return binMappings;
    }

    /**
     * Main function that handles all the transaction logic and checks everything according to given rules
     * @param users List of Users that were read from file
     * @param transactions List of Transactions that were read from file
     * @param binMappings List of BinMappings that were read from file
     * @return Returns List that contains all events except the ones with unexpected errors
     */
    private static List<Event> processTransactions(final List<User> users, final List<Transaction> transactions, final List<BinMapping> binMappings) {
        List<Event> events = new ArrayList<>();
        List<String> allowedToWithdraw = new ArrayList<>();
        Map<String, String> usedPaymentAccounts = new HashMap<>();

        Map<String, User> userMap = new HashMap<>();
        for (User user : users){
            userMap.put(user.getUserId(), user);
        }

        for (Transaction transaction : transactions){
            try{
                //Check that there is not transaction with that ID in handled transactions
                if(!isTransactionUnique(transaction, events)){
                    events.add(new Event(transaction.getTransactionId(), Event.STATUS_DECLINED, "Transaction "+ transaction.getTransactionId() + " already processed (id non-unique)"));
                    continue;
                }

                //Check if that user exists
                Optional<User> userOptional = Optional.ofNullable(userMap.get(transaction.getUserId()));
                User user = userOptional.orElse(null);
                if(user == null || user.getFrozen() == 1){
                    events.add(new Event(transaction.getTransactionId(), Event.STATUS_DECLINED, "User " + transaction.getUserId() + " not found in Users or is frozen"));
                    continue;
                }

                //Check if account is not used by other user
                if(!usedPaymentAccounts.containsKey(transaction.getAccountNumber()) || usedPaymentAccounts.get(transaction.getAccountNumber()).equals(user.getUserId())) {
                    //Check payment method validation
                    if (transaction.getMethod().equals("TRANSFER")) {
                        String IBAN = transaction.getAccountNumber();
                        String cardCountry = IBAN.substring(0, 2);
                        if (!isValidIBAN(IBAN)) { //is IBAN valid
                            events.add(new Event(transaction.getTransactionId(), Event.STATUS_DECLINED, "Invalid iban " + IBAN));
                            continue;
                        }
                        if (!cardCountry.equals(user.getCountry())) { //Do countries match
                            events.add(new Event(transaction.getTransactionId(), Event.STATUS_DECLINED, "Invalid account country " + cardCountry + "; expected " + user.getCountry()));
                            continue;
                        }
                    } else if (transaction.getMethod().equals("CARD")) {
                        String cardNumber = transaction.getAccountNumber();
                        String cardNumberFirst10Digits = cardNumber.substring(0, 10);
                        ArrayList<String> validCard = isValidCard(cardNumberFirst10Digits, binMappings, user.getCountry());

                        if (Objects.equals(validCard.get(0), "false")) {
                            events.add(new Event(transaction.getTransactionId(), Event.STATUS_DECLINED, validCard.get(1)));
                            continue;
                        }
                    } else {
                        events.add(new Event(transaction.getTransactionId(), Event.STATUS_DECLINED, "Invalid payment method"));
                        continue;
                    }

                    if (bigDecimalCompare(transaction.getAmount(), BigDecimal.valueOf(0)) <= 0) { //Not positive
                        events.add(new Event(transaction.getTransactionId(), Event.STATUS_DECLINED, "Payment amount is not positive"));
                        continue;
                    }

                    if (transaction.getType().equals("DEPOSIT")) {
                        if (bigDecimalCompare(transaction.getAmount(), user.getDepositMin()) == -1) { //Is amount under deposit min
                            events.add(new Event(transaction.getTransactionId(), Event.STATUS_DECLINED, "Amount " + transaction.getAmount() + " is under the deposit limit " + user.getDepositMin()));
                            continue;
                        } else if (bigDecimalCompare(transaction.getAmount(), user.getDepositMax()) == 1) { //Is amount over deposit max
                            events.add(new Event(transaction.getTransactionId(), Event.STATUS_DECLINED, "Amount " + transaction.getAmount() + " is over the deposit limit " + user.getDepositMax()));
                            continue;
                        } else { //complete the deposit and add account to list that contains accounts that are allowed to withdraw
                            user.setBalance(user.getBalance().add(transaction.getAmount()));
                            allowedToWithdraw.add(transaction.getAccountNumber());
                        }
                    } else if (transaction.getType().equals("WITHDRAW")) {
                        if (!allowedToWithdraw.contains(transaction.getAccountNumber())) { //Is not allowed to withdraw
                            events.add(new Event(transaction.getTransactionId(), Event.STATUS_DECLINED, "Cannot withdraw with a new account " + transaction.getAccountNumber()));
                            continue;
                        } else if (bigDecimalCompare(transaction.getAmount(), user.getWithdrawMin()) == -1) { //Is amount under withdraw min
                            events.add(new Event(transaction.getTransactionId(), Event.STATUS_DECLINED, "Amount " + transaction.getAmount() + " is under the withdrawal limit " + user.getWithdrawMin()));
                            continue;
                        } else if (bigDecimalCompare(transaction.getAmount(), user.getWithdrawMax()) == 1) { //Is amount under withdraw max
                            events.add(new Event(transaction.getTransactionId(), Event.STATUS_DECLINED, "Amount " + transaction.getAmount() + " is over the withdrawal limit " + user.getWithdrawMax()));
                            continue;
                        } else if (bigDecimalCompare(transaction.getAmount(), user.getBalance()) == 1) { //Is enough balance
                            events.add(new Event(transaction.getTransactionId(), Event.STATUS_DECLINED, "Not enough balance to withdraw " + transaction.getAmount() + " - balance is too low at " + user.getBalance()));
                            continue;
                        }else { //Complete the withdrawal
                            user.setBalance(user.getBalance().subtract(transaction.getAmount()));
                        }

                    } else { //Not Withdraw or Deposit
                        events.add(new Event(transaction.getTransactionId(), Event.STATUS_DECLINED, "Transaction type is not correct: " + transaction.getType()));
                        continue;
                    }
                }else { //Account is in use by other user
                    events.add(new Event(transaction.getTransactionId(), Event.STATUS_DECLINED, "Account " + transaction.getAccountNumber() + " is in use by other user"));
                    continue;
                }

                usedPaymentAccounts.put(transaction.getAccountNumber(), user.getUserId());
                events.add(new Event(transaction.getTransactionId(), Event.STATUS_APPROVED, "OK")); //Transaction handled
            }catch (Exception e){
                System.out.println(e);
                //events.add(new Event(transaction.transactionId, Event.STATUS_DECLINED, "Unexpected error while processing transaction"));
            }
        }
        return events;
    }

    /**
     * Function that compares big decimals, double was not enough to handle 20digits accurately
     * @param number1 First number that is in comparison
     * @param number2 Second number that is in comparison
     * @return Returns int value that indicates comparison result (lower, bigger, equal)
     */
    private static int bigDecimalCompare(BigDecimal number1, BigDecimal number2){
        int comparisonResult = number1.compareTo(number2);
        if (comparisonResult < 0){ //number1 is lower than number2
            return -1;
        } else if (comparisonResult > 0) { //number1 is bigger than number2
            return 1;
        }else{ //numbers are equal
            return 0;
        }
    }

    /**
     * Function that checks if Card is valid to use
     * @param cardNumberFirst10Digits First ten digits of the card that user wants to use
     * @param binMappings BinMappings list
     * @param userCountry User country
     * @return Returns ArrayList that contains String boolean value if card is valid and Transaction Message
     */
    private static ArrayList<String> isValidCard(String cardNumberFirst10Digits, List<BinMapping> binMappings, String userCountry){

        ArrayList<String> status= new ArrayList<>();
        for (BinMapping binMapping : binMappings) {
            if (cardNumberFirst10Digits.compareTo(binMapping.getRangeFrom()) >= 0 && cardNumberFirst10Digits.compareTo(binMapping.getRangeTo()) <= 0) {
                String alpha3CountryCode = convertAlpha2ToAlpha3(userCountry);
                if(!binMapping.getCountry().equals(alpha3CountryCode)){
                    status.add(0, "false");
                    status.add(1, "Invalid country " + binMapping.getCountry() + "; expected " + userCountry);
                    return status;
                } else if (!binMapping.getType().equals("DC")) {
                    status.add(0, "false");
                    status.add(1, "Only DC cards allowed got " + binMapping.getType());
                    return status;
                }
            }
        }
        status.add(0,"true");
        status.add(1, "OK");
        return status;
    }

    /**
     * Function that converts Alpha2 country codes to Alpha3 format
     * @param alpha2Code Country code in Alpha2
     * @return returns Country code in Alpha3 or "Unknown" if error occurs
     */
    public static String convertAlpha2ToAlpha3(String alpha2Code) {
        try {
            return new Locale("", alpha2Code).getISO3Country();
        } catch (Exception e) {
            return "Unknown";
        }
    }

    /**
     * Function that checks if transaction is unique
     * @param transaction The transaction we are checking
     * @param events Events list for checking if given transaction is in events list
     * @return Returns if the transaction is unique or not (True or False)
     */
    private static boolean isTransactionUnique(Transaction transaction, List<Event> events) {
        for (Event handledTransaction : events){
            if(handledTransaction.getTransactionId().equals(transaction.getTransactionId())){
                return false;
            }
        }
        return true;

        //And a fancy way :D
        //return events.stream().map(Event::getTransactionId).noneMatch(transactionId -> transactionId.equals(transaction.getTransactionId()));
    }


    /**
     * Function that checks if IBAN is valid. Validation check info from wikipedia: https://en.wikipedia.org/wiki/International_Bank_Account_Number
     * @param IBAN User's IBAN
     * @return Returns if IBAN is valid or not (True or False)
     */
    private static boolean isValidIBAN(String IBAN){
        if (IBAN.length() < 4 || IBAN.length() > 34) {
            return false;
        }
        String rearrangedIBAN  = IBAN.substring(4) + IBAN.substring(0, 4); //Rearrange the IBAN according to validation check rules
        StringBuilder convertedIBAN = new StringBuilder();
        for (int i = 0; i < rearrangedIBAN.length(); i++){
            char c = rearrangedIBAN.charAt(i);
            if (Character.isDigit(c)){
                convertedIBAN.append(c);
            } else if (Character.isUpperCase(c)) {
                convertedIBAN.append((int) (c - 'A' + 10));
            } else {
                return false;
            }
        }
        BigInteger ibanAsBigInt = new BigInteger(convertedIBAN.toString());

        return ibanAsBigInt.mod(BigInteger.valueOf(97)).equals(BigInteger.ONE); //Modulus check for IBAN


    }

    /**
     * Function that writes user balances to output file, does not return anything
     * @param filePath File path where balances are written
     * @param users List of Users
     * @throws IOException
     */
    private static void writeBalances(final Path filePath, final List<User> users) throws IOException {
        try (final FileWriter writer = new FileWriter(filePath.toFile(), false)) {
            writer.append("USER_ID,BALANCE\n");
            for (int i = 0; i < users.size(); i++){
                User user = users.get(i);
                String userBalance = String.valueOf(user.getBalance().setScale(2, RoundingMode.HALF_UP));
                String line = String.join(",", user.getUserId(), userBalance);
                if(i < users.size() - 1){ //So I don't write one empty line at the very end
                    line += "\n";
                }
                writer.append(line);
            }
        }
    }

    /**
     * Function that writes events to output file, does not return anything
     * @param filePath File path where balances are written
     * @param events List of events
     * @throws IOException
     */
    private static void writeEvents(final Path filePath, final List<Event> events) throws IOException {
        try (final FileWriter writer = new FileWriter(filePath.toFile(), false)) {
            writer.append("TRANSACTION_ID,STATUS,MESSAGE\n");
            for (int i = 0; i < events.size(); i++){
                Event event = events.get(i);
                String line = String.join(",", event.getTransactionId(), event.getStatus(), event.getMessage()); //Form the line
                if(i < events.size() - 1){ //So I don't write one empty line at the very end
                    line += "\n";
                }
                writer.append(line);
            }
        }
    }
}







