package za.ac.rosebank.registrationlogin;

import java.util.ArrayList;
import java.util.Scanner;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.IOException;
import org.json.JSONObject;

public class QuickChat {

    private ArrayList<Message> messages = new ArrayList<>();
    private ArrayList<Message> sentMessages = new ArrayList<>();
    private ArrayList<Message> disregardedMessages = new ArrayList<>();

    private ArrayList<JSONObject> storedMessages = new ArrayList<>();
    private ArrayList<String> messageHashes = new ArrayList<>();
    private ArrayList<String> messageIDs = new ArrayList<>();

    private int totalMessages = 0;
    private String sender = "Unknown";

    public void start(Scanner scanner) {
        start(scanner, "Unknown");
    }

    public void start(Scanner scanner, String username) {

        if (username != null && !username.trim().isEmpty()) {
            sender = username;
        }

        System.out.println("Welcome to QuickChat!");

        loadStoredMessages();
        addTestMessages();
        saveStoredMessages();

        boolean running = true;

        while (running) {

            System.out.println();
            System.out.println("QUICKCHAT MENU");
            System.out.println("1) Send Messages");
            System.out.println("2) Show recently sent messages");
            System.out.println("3) Show disregarded messages");
            System.out.println("4) Stored Messages");
            System.out.println("5) Quit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1":
                    sendMessages(scanner);
                    break;

                case "2":
                    showSentMessages();
                    break;

                case "3":
                    showDisregardedMessages();
                    break;

                case "4":
                    storedMessagesMenu(scanner);
                    break;

                case "5":
                    running = false;
                    System.out.println("Thank you for using QuickChat!");
                    break;

                default:
                    System.out.println("Invalid option. Please choose 1 to 5.");
            }
        }
    }

    private void addTestMessages() {
        addTestMessage("+27834557896", "Did you get the cake?", "Sent");
        addTestMessage("+27838884567", "Where are you? You are late! I have asked you to be on time.", "Stored");
        addTestMessage("+27834484567", "Yoooooo, I am at your gate.", "Disregarded");
        addTestMessage("0838884567", "It is dinner time !", "Sent");
        addTestMessage("+27838884567", "Ok, I am leaving without you.", "Stored");
    }

    private void addTestMessage(String recipient, String text, String status) {

        if (status.equals("Stored") && storedMessageExists(recipient, text)) {
            return;
        }

        Message msg = new Message(totalMessages + 1, recipient, text);

        msg.setSender(sender);
        msg.setMessageStatus(status);

        Message.increaseTotalMessages();
        totalMessages++;

        messages.add(msg);
        messageIDs.add(msg.getMessageID());
        messageHashes.add(msg.getMessageHash());

        if (status.equals("Sent")) {
            sentMessages.add(msg);
        } else if (status.equals("Disregarded")) {
            disregardedMessages.add(msg);
        } else if (status.equals("Stored")) {
            storedMessages.add(createJSONMessage(msg));
        }
    }

    private boolean storedMessageExists(String recipient, String text) {
        for (JSONObject json : storedMessages) {
            if (json.getString("recipient").equals(recipient)
                    && json.getString("messageText").equals(text)) {
                return true;
            }
        }
        return false;
    }

    private void sendMessages(Scanner scanner) {

        System.out.print("How many messages do you wish to enter? ");
        int numberOfMessages;

        try {
            numberOfMessages = Integer.parseInt(scanner.nextLine().trim());
            if (numberOfMessages <= 0) {
                System.out.println("Enter a number greater than zero.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid whole number.");
            return;
        }

        for (int i = 1; i <= numberOfMessages; i++) {

            System.out.println();
            System.out.println("Message " + i);

            System.out.print("Enter recipient number: ");
            String recipient = scanner.nextLine().trim();

            if (!Message.checkRecipientCell(recipient)) {
                System.out.println("Invalid recipient number. Use +27 or local 0 followed by 9 digits.");
                i--;
                continue;
            }

            System.out.print("Enter your message: ");
            String text = scanner.nextLine();

            if (!Message.checkMessageLength(text)) {
                System.out.println("Message must contain 1 to 250 characters.");
                i--;
                continue;
            }

            System.out.println();
            System.out.println("Choose what to do with your message:");
            System.out.println("1) Send Message");
            System.out.println("2) Disregard Message");
            System.out.println("3) Store Message");
            System.out.print("Enter your choice: ");

            String action = scanner.nextLine().trim();

            if (!action.equals("1") && !action.equals("2") && !action.equals("3")) {
                System.out.println("Invalid choice. Please try again.");
                i--;
                continue;
            }

            Message msg = new Message(totalMessages + 1, recipient, text);
            msg.setSender(sender);

            switch (action) {
                case "1":
                    System.out.println(msg.sentMessage(1));
                    sentMessages.add(msg);
                    messages.add(msg);
                    break;

                case "2":
                    System.out.println(msg.sentMessage(2));
                    disregardedMessages.add(msg);
                    messages.add(msg);
                    break;

                case "3":
                    System.out.println(msg.sentMessage(3));
                    storedMessages.add(createJSONMessage(msg));
                    messages.add(msg);
                    saveStoredMessages();
                    break;
            }

            Message.increaseTotalMessages();
            totalMessages++;

            messageIDs.add(msg.getMessageID());
            messageHashes.add(msg.getMessageHash());

            System.out.println(msg.printMessages());
        }

        System.out.println("Total messages processed: " + totalMessages);
    }

    private void showSentMessages() {
        System.out.println();
        System.out.println("SENT MESSAGES");

        if (sentMessages.isEmpty()) {
            System.out.println("No sent messages available.");
            return;
        }

        for (Message msg : sentMessages) {
            System.out.println(msg.printMessages());
            System.out.println();
        }
    }

    private void showDisregardedMessages() {
        System.out.println();
        System.out.println("DISREGARDED MESSAGES");

        if (disregardedMessages.isEmpty()) {
            System.out.println("No disregarded messages available.");
            return;
        }

        for (Message msg : disregardedMessages) {
            System.out.println(msg.printMessages());
            System.out.println();
        }
    }

    private JSONObject createJSONMessage(Message msg) {
        return msg.toJSON();
    }

    private void loadStoredMessages() {
        try (BufferedReader reader = new BufferedReader(new FileReader("messages.json"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                JSONObject json = new JSONObject(line);
                String loadedSender = json.optString("sender", "Unknown");

                if (loadedSender.equals("Unknown")
                        || loadedSender.equals("QuickChat user")
                        || loadedSender.isEmpty()) {
                    loadedSender = sender;
                    json.put("sender", loadedSender);
                }

                String id = json.getString("messageID");
                String recipient = json.getString("recipient");
                String text = json.getString("messageText");
                String hash = json.getString("messageHash");
                int number = json.optInt("messageNumber", 0);
                String status = json.optString("messageStatus", "Stored");

                if (number <= 0) {
                    number = totalMessages + 1;
                    json.put("messageNumber", number);
                }

                Message loadedMessage = new Message(number, recipient, text);
                loadedMessage.setSender(loadedSender);
                loadedMessage.setMessageStatus(status);
                loadedMessage.setMessageID(id);
                loadedMessage.setMessageHash(hash);

                storedMessages.add(json);
                messageIDs.add(id);
                messageHashes.add(hash);
                messages.add(loadedMessage);

                totalMessages++;
                Message.increaseTotalMessages();
            }
        } catch (IOException e) {
            System.out.println("No previous stored messages found.");
        }
    }

    private void storedMessagesMenu(Scanner scanner) {
        boolean menuRunning = true;

        while (menuRunning) {
            System.out.println();
            System.out.println("STORED MESSAGES MENU");
            System.out.println("a) Display sender and recipient");
            System.out.println("b) Display longest stored message");
            System.out.println("c) Search message using message ID");
            System.out.println("d) Search messages using recipient");
            System.out.println("e) Delete message using message hash");
            System.out.println("f) Display full message report");
            System.out.println("g) Return to main menu");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim().toLowerCase();

            switch (choice) {
                case "a":
                    showStoredRecipients();
                    break;
                case "b":
                    showLongestStoredMessage();
                    break;
                case "c":
                    System.out.print("Enter message ID: ");
                    searchById(scanner.nextLine().trim());
                    break;
                case "d":
                    System.out.print("Enter recipient number: ");
                    searchByRecipient(scanner.nextLine().trim());
                    break;
                case "e":
                    System.out.print("Enter message hash: ");
                    deleteByHash(scanner.nextLine().trim());
                    break;
                case "f":
                    showStoredDetails();
                    break;
                case "g":
                    menuRunning = false;
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private void showStoredRecipients() {
        System.out.println();
        System.out.println("STORED MESSAGE SENDERS AND RECIPIENTS");

        if (storedMessages.isEmpty()) {
            System.out.println("No stored messages available.");
            return;
        }

        for (JSONObject json : storedMessages) {
            System.out.println("Sender: " + json.optString("sender", "Unknown"));
            System.out.println("Recipient: " + json.getString("recipient"));
            System.out.println();
        }
    }

    public String getLongestStoredMessage() {
        if (storedMessages.isEmpty()) {
            return "";
        }

        JSONObject longest = storedMessages.get(0);
        for (JSONObject json : storedMessages) {
            if (json.getString("messageText").length() > longest.getString("messageText").length()) {
                longest = json;
            }
        }
        return longest.getString("messageText");
    }

    private void showLongestStoredMessage() {
        String longest = getLongestStoredMessage();
        if (longest.isEmpty()) {
            System.out.println("No stored messages available.");
        } else {
            System.out.println("Longest stored message:");
            System.out.println(longest);
        }
    }

    public String searchById(String id) {
        for (JSONObject json : storedMessages) {
            if (json.getString("messageID").equals(id)) {
                String result = "Recipient: " + json.getString("recipient")
                        + "\nMessage: " + json.getString("messageText");
                System.out.println(result);
                return result;
            }
        }
        System.out.println("Message ID not found.");
        return "";
    }

    public ArrayList<String> searchByRecipient(String recipient) {
        ArrayList<String> results = new ArrayList<>();
        for (JSONObject json : storedMessages) {
            if (json.getString("recipient").equals(recipient)) {
                results.add(json.getString("messageText"));
            }
        }

        if (results.isEmpty()) {
            System.out.println("No messages found for this recipient.");
        } else {
            System.out.println("Messages for " + recipient + ":");
            for (String message : results) {
                System.out.println("- " + message);
            }
        }
        return results;
    }

    public boolean deleteByHash(String hash) {
        for (int i = 0; i < storedMessages.size(); i++) {
            JSONObject json = storedMessages.get(i);
            if (json.getString("messageHash").equalsIgnoreCase(hash)) {
                String id = json.getString("messageID");
                String storedHash = json.getString("messageHash");

                storedMessages.remove(i);
                messageIDs.remove(id);
                messageHashes.remove(storedHash);

                messages.removeIf(msg -> msg.getMessageID().equals(id));
                saveStoredMessages();

                System.out.println("Message successfully deleted.");
                return true;
            }
        }
        System.out.println("Message hash not found.");
        return false;
    }

    private void saveStoredMessages() {
        try (FileWriter writer = new FileWriter("messages.json", false)) {
            for (JSONObject json : storedMessages) {
                writer.write(json.toString());
                writer.write(System.lineSeparator());
            }
        } catch (IOException e) {
            System.out.println("Error updating messages file: " + e.getMessage());
        }
    }

    public String showStoredDetails() {
        StringBuilder report = new StringBuilder();
        System.out.println();
        System.out.println("STORED MESSAGE REPORT");

        for (JSONObject json : storedMessages) {
            report.append("Message ID: ").append(json.getString("messageID")).append("\n");
            report.append("Message Hash: ").append(json.getString("messageHash")).append("\n");
            report.append("Sender: ").append(json.optString("sender", "Unknown")).append("\n");
            report.append("Recipient: ").append(json.getString("recipient")).append("\n");
            report.append("Message: ").append(json.getString("messageText")).append("\n");
           
            report.append("Status: ").append(json.optString("messageStatus", "Stored")).append("\n\n");
        }

        if (storedMessages.isEmpty()) {
            report.append("No stored messages available.");
        }

        System.out.println(report.toString());
        return report.toString();
    }

    public ArrayList<Message> getSentMessages() {
        return sentMessages;
    }

    public ArrayList<Message> getDisregardedMessages() {
        return disregardedMessages;
    }

    public ArrayList<JSONObject> getStoredMessages() {
        return storedMessages;
    }

    public ArrayList<String> getMessageHashes() {
        return messageHashes;
    }

    public ArrayList<String> getMessageIDs() {
        return messageIDs;
    }

    public int getTotalMessages() {
        return totalMessages;
    }
}