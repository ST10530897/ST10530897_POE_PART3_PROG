package za.ac.rosebank.registrationlogin;

import java.util.Random;
import org.json.JSONObject;

public class Message {

    private static int totalMessages = 0;

    private String messageID;
    private String recipient;
    private String messageText;
    private String messageHash;
    private String sender;
    private int messageNumber;
    private String messageStatus;

    public Message(int messageNumber, String recipient, String messageText) {
        this.messageNumber = messageNumber;
        this.recipient = recipient;
        this.messageText = messageText;
        this.sender = "Unknown";
        this.messageStatus = "Pending";

        generateMessageID();
        createMessageHash();
    }

    private void generateMessageID() {
        Random random = new Random();
        long id = 100000000L + (long) (random.nextDouble() * 9000000000L);
        messageID = String.valueOf(id);
    }

    public boolean checkMessageID() {
        return messageID != null && messageID.matches("[0-9]{1,10}");
    }

   
    public static boolean checkRecipientCell(String recipient) {
        return recipient != null && recipient.matches("(\\+27|0)[0-9]{9}");
    }

    public static boolean checkMessageLength(String messageText) {
        return messageText != null
                && !messageText.trim().isEmpty()
                && messageText.length() <= 250;
    }

    private void createMessageHash() {
        String firstTwoID = messageID.substring(0, 2);
        String[] words = messageText.trim().split("\\s+");
        String firstWord = words.length > 0 ? words[0] : "";
        String lastWord = words.length > 0 ? words[words.length - 1] : "";

        messageHash = (firstTwoID + ":"
                + messageNumber + ":"
                + firstWord + lastWord).toUpperCase();
    }

    public void setSender(String sender) {
        if (sender != null && !sender.trim().isEmpty()) {
            this.sender = sender;
        }
        createMessageHash();
    }

    public void setMessageStatus(String messageStatus) {
        this.messageStatus = messageStatus;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public void setMessageHash(String messageHash) {
        this.messageHash = messageHash;
    }

    public String sentMessage(int choice) {
        switch (choice) {
            case 1:
                messageStatus = "Sent";
                return "Message successfully sent.";
            case 2:
                messageStatus = "Disregarded";
                return "Message disregarded.";
            case 3:
                messageStatus = "Stored";
                return "Message stored.";
            default:
                return "Invalid message option.";
        }
    }

    public static void increaseTotalMessages() {
        totalMessages++;
    }

    public static int returnTotalMessages() {
        return totalMessages;
    }

    public String printMessages() {
        return "Message ID: " + messageID
                + "\nMessage Hash: " + messageHash
                + "\nMessage Number: " + messageNumber
                + "\nSender: " + sender
                + "\nRecipient: " + recipient
                + "\nMessage: " + messageText
                + "\nStatus: " + messageStatus;
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("messageID", messageID);
        json.put("messageHash", messageHash);
        json.put("sender", sender);
        json.put("recipient", recipient);
        json.put("messageText", messageText);
        json.put("messageNumber", messageNumber);
        json.put("messageStatus", messageStatus);
        return json;
    }

    public String getMessageID() {
        return messageID;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getMessageText() {
        return messageText;
    }

    public String getMessageHash() {
        return messageHash;
    }

    public String getSender() {
        return sender;
    }

    public int getMessageNumber() {
        return messageNumber;
    }

    public String getMessageStatus() {
        return messageStatus;
    }
}
