package com.playtech.assignment;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


// This template shows input parameters format.
// It is otherwise not mandatory to use, you can write everything from scratch if you wish.
public class TransactionProcessorSample {

    public static void main(final String[] args) throws IOException {

        //List<User> users = TransactionProcessorSample.readUsers(Paths.get(args[0]));
        //List<Transaction> transactions = TransactionProcessorSample.readTransactions(Paths.get(args[1]));
        //List<BinMapping> binMappings = TransactionProcessorSample.readBinMappings(Paths.get(args[2]));

        //List<Event> events = TransactionProcessorSample.processTransactions(users, transactions, binMappings);

        //TransactionProcessorSample.writeBalances(Paths.get(args[3]), users);
        //TransactionProcessorSample.writeEvents(Paths.get(args[4]), events);

        Path userFilePath = Paths.get("test-data/manual test data 75% validations/input/users.csv");
        Path transactionFilePath = Paths.get("test-data/manual test data 75% validations/input/transactions.csv");
        Path binsFilePath = Paths.get("test-data/manual test data 75% validations/input/bins.csv");
/*
        Path userFilePath = Paths.get("test-data/test random data (small)/input/users.csv");
        Path transactionFilePath = Paths.get("test-data/test random data (small)/input/transactions.csv");
        Path binsFilePath = Paths.get("test-data/test random data (small)/input/bins.csv");*/
/*
        Path userFilePath = Paths.get("test-data/test random data 50% validations/input/users.csv");
        Path transactionFilePath = Paths.get("test-data/test random data 50% validations/input/transactions.csv");
        Path binsFilePath = Paths.get("test-data/test random data 50% validations/input/bins.csv");*/

        try {
            List<User> users = TransactionProcessorSample.readUsers(userFilePath);
            for (User user : users){
                //System.out.println(user);
            }
            List<Transaction> transactions = TransactionProcessorSample.readTransactions(transactionFilePath);
            for (Transaction transaction: transactions){
                //System.out.println(transaction);
            }
            List<BinMapping> binMappings = TransactionProcessorSample.readBinMappings(binsFilePath);
            for(BinMapping binMapping : binMappings){
                //System.out.println(binMapping);
            }
            List<Event> events = processTransactions(users, transactions, binMappings);
            for (Event event : events){
                System.out.println(event);
            }
            Path eventsOutputPath = Paths.get("eventsOutput.csv");
            writeEvents(eventsOutputPath, events);
            Path balanceOutputPath = Paths.get("balanceOutput.csv");
            writeBalances(balanceOutputPath, users);

        }catch (IOException e){
            System.out.println(e);
        }

    }

    private static List<User> readUsers(final Path filePath) throws IOException{
        List<User> users = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
            String line;
            reader.readLine(); //To read first unnecessary line
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                try {
                    User user = new User(parts[0], parts[1], Double.parseDouble(parts[2]), parts[3], Integer.parseInt(parts[4]), Double.parseDouble(parts[5]), Double.parseDouble(parts[6]), Double.parseDouble(parts[7]), Double.parseDouble(parts[8]));
                    users.add(user);
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    System.err.println("Error creating user: " + e.getMessage());
                }
            }
        }
        return users;
    }

    private static List<Transaction> readTransactions(final Path filePath) throws IOException {
        List<Transaction> transactions = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))){
            String line;
            reader.readLine(); //To read first unnecessary line
            while ((line = reader.readLine()) != null){
                String[] parts = line.split(",");
                try {
                    Transaction transaction = new Transaction(parts[0], parts[1], parts[2], Double.parseDouble(parts[3]), parts[4], parts[5]);
                    transactions.add(transaction);
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    System.err.println("Error creating transaction: " + e.getMessage());
                }

            }
        }
        return transactions;
    }

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

    private static List<Event> processTransactions(final List<User> users, final List<Transaction> transactions, final List<BinMapping> binMappings) {
        List<Event> events = new ArrayList<>();
        List<String> allowedToWithdraw = new ArrayList<>();
        Map<String, String> usedPaymentAccounts = new HashMap<>();

        Map<String, User> userMap = new HashMap<>();
        for (User user : users){
            userMap.put(user.userId, user);
        }

        for (Transaction transaction : transactions){
            try{
                //Check that there is not transaction with that ID in handled transactions
                if(!isTransactionUnique(transaction, events)){
                    events.add(new Event(transaction.transactionId, Event.STATUS_DECLINED, "Transaction "+ transaction.transactionId + " already processed (id non-unique)"));
                    continue;
                }

                //Check if that user exists
                Optional<User> user = Optional.ofNullable(userMap.get(transaction.userId));
                if(user.isEmpty() || user.get().frozen == 1){
                    events.add(new Event(transaction.transactionId, Event.STATUS_DECLINED, "User " + transaction.userId + " not found in Users or is frozen"));
                    continue;
                }

                if(!usedPaymentAccounts.containsKey(transaction.accountNumber) || usedPaymentAccounts.get(transaction.accountNumber).equals(user.get().userId)) {
                    //Check payment method validation
                    if (transaction.method.equals("TRANSFER")) {
                        String IBAN = transaction.accountNumber;
                        String cardCountry = IBAN.substring(0, 2);
                        if (!isValidIBAN(IBAN)) {
                            events.add(new Event(transaction.transactionId, Event.STATUS_DECLINED, "Invalid iban " + IBAN));
                            continue;
                        }
                        if (!cardCountry.equals(user.get().country)) {
                            events.add(new Event(transaction.transactionId, Event.STATUS_DECLINED, "Invalid account country " + cardCountry + "; expected " + user.get().country));
                            continue;
                        }
                    } else if (transaction.method.equals("CARD")) {
                        String cardNumber = transaction.accountNumber;
                        String cardNumberFirst10Digits = cardNumber.substring(0, 10);
                        ArrayList<String> validCard = isValidCard(cardNumberFirst10Digits, binMappings, user.get().country);

                        if (Objects.equals(validCard.get(0), "false")) {
                            events.add(new Event(transaction.transactionId, Event.STATUS_DECLINED, validCard.get(1)));
                            continue;
                        }
                    } else {
                        events.add(new Event(transaction.transactionId, Event.STATUS_DECLINED, "Invalid payment method"));
                        continue;
                    }

                    if (transaction.amount <= 0) {
                        events.add(new Event(transaction.transactionId, Event.STATUS_DECLINED, "Payment amount is not positive"));
                        continue;
                    }

                    if (transaction.type.equals("DEPOSIT")) {
                        if (transaction.amount < user.get().depositMin) {
                            events.add(new Event(transaction.transactionId, Event.STATUS_DECLINED, "Amount " + transaction.amount + " is under the deposit limit " + user.get().depositMin));
                            continue;
                        } else if (transaction.amount > user.get().depositMax) {
                            events.add(new Event(transaction.transactionId, Event.STATUS_DECLINED, "Amount " + transaction.amount + " is over the deposit limit " + user.get().depositMax));
                            continue;
                        } else {
                            user.get().balance += transaction.amount;
                            allowedToWithdraw.add(transaction.accountNumber);
                        }
                    } else if (transaction.type.equals("WITHDRAW")) {
                        if (!allowedToWithdraw.contains(transaction.accountNumber)) {
                            events.add(new Event(transaction.transactionId, Event.STATUS_DECLINED, "Cannot withdraw with a new account " + transaction.accountNumber));
                            continue;
                        } else if (transaction.amount < user.get().withdrawMin) {
                            events.add(new Event(transaction.transactionId, Event.STATUS_DECLINED, "Amount " + transaction.amount + " is under the withdrawal limit " + user.get().withdrawMin));
                            continue;
                        } else if (transaction.amount > user.get().withdrawMax) {
                            events.add(new Event(transaction.transactionId, Event.STATUS_DECLINED, "Amount " + transaction.amount + " is over the withdrawal limit " + user.get().withdrawMax));
                            continue;
                        } else if (transaction.amount > user.get().balance) {
                            events.add(new Event(transaction.transactionId, Event.STATUS_DECLINED, "Not enough balance to withdraw " + transaction.amount + " - balance is too low at " + user.get().balance));
                            continue;
                        }else {
                            user.get().balance -= transaction.amount;
                        }

                    } else {
                        events.add(new Event(transaction.transactionId, Event.STATUS_DECLINED, "Transaction type is not correct: " + transaction.type));
                        continue;
                    }
                }else {
                    events.add(new Event(transaction.transactionId, Event.STATUS_DECLINED, "Account " + transaction.accountNumber + " is in use by other user"));
                    continue;
                }

                usedPaymentAccounts.put(transaction.accountNumber, user.get().userId);
                events.add(new Event(transaction.transactionId, Event.STATUS_APPROVED, "OK"));
            }catch (Exception e){
                System.out.println(e);
                //events.add(new Event(transaction.transactionId, Event.STATUS_DECLINED, "Unexpected error while processing transaction"));
            }
        }
        return events;
    }

    private static ArrayList<String> isValidCard(String cardNumberFirst10Digits, List<BinMapping> binMappings, String userCountry){

        ArrayList<String> status= new ArrayList<>();
        for (BinMapping binMapping : binMappings) {
            if (cardNumberFirst10Digits.compareTo(binMapping.rangeFrom) >= 0 && cardNumberFirst10Digits.compareTo(binMapping.rangeTo) <= 0) {
                String alpha3CountryCode = convertAlpha2ToAlpha3(userCountry);
                if(!binMapping.country.equals(alpha3CountryCode)){
                    status.add(0, "false");
                    status.add(1, "Invalid country " + binMapping.country + "; expected " + userCountry);
                    return status;
                } else if (!binMapping.type.equals("DC")) {
                    status.add(0, "false");
                    status.add(1, "Only DC cards allowed got " + binMapping.type);
                    return status;
                }
            }
        }
        status.add(0,"true");
        status.add(1, "OK");
        return status;
    }


    public static String convertAlpha2ToAlpha3(String alpha2Code) {
        try {
            return new Locale("", alpha2Code).getISO3Country();
        } catch (Exception e) {
            return "Unknown";
        }
    }


    private static boolean isTransactionUnique(Transaction transaction, List<Event> events) {
        for (Event handledTransaction : events){
            if(handledTransaction.transactionId.equals(transaction.transactionId)){
                return false;
            }
        }
        return true;

        //And a fancy way :D
        //return events.stream().map(Event::getTransactionId).noneMatch(transactionId -> transactionId.equals(transaction.getTransactionId()));
    }


    private static boolean isValidIBAN(String IBAN){
        if (IBAN.length() < 4 || IBAN.length() > 34) {
            return false;
        }
        String rearrangedIBAN  = IBAN.substring(4) + IBAN.substring(0, 4);
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

        return ibanAsBigInt.mod(BigInteger.valueOf(97)).equals(BigInteger.ONE);


    }

    private static void writeBalances(final Path filePath, final List<User> users) throws IOException {
        try (final FileWriter writer = new FileWriter(filePath.toFile(), false)) {
            writer.append("USER_ID,BALANCE\n");
            for (int i = 0; i < users.size(); i++){
                User user = users.get(i);
                String userBalance = String.valueOf(user.balance);
                String line = String.join(",", user.userId, userBalance);
                if(i < users.size() - 1){
                    line += "\n";
                }
                writer.append(line);
            }
        }
    }

    private static void writeEvents(final Path filePath, final List<Event> events) throws IOException {
        try (final FileWriter writer = new FileWriter(filePath.toFile(), false)) {
            writer.append("TRANSACTION_ID,STATUS,MESSAGE\n");
            for (int i = 0; i < events.size(); i++){
                Event event = events.get(i);
                String line = String.join(",", event.transactionId, event.status, event.message);
                if(i < events.size() - 1){
                    line += "\n";
                }
                writer.append(line);
            }
        }
    }


}







