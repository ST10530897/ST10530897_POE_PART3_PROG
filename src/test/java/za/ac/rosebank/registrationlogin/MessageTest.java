package za.ac.rosebank.registrationlogin;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MessageTest {

    @Test
    public void testValidRecipientNumber() {
        assertTrue(
            Message.checkRecipientCell("+27821234567")
        );
    }

    @Test
    public void testInvalidRecipientNumber() {
        assertFalse(
            Message.checkRecipientCell("08212345")
        );
    }

    @Test
    public void testValidLocalRecipientNumber() {
        assertTrue(
            Message.checkRecipientCell("0821234567")
        );
    }

    @Test
    public void testValidMessageLength() {
        String testText = "Hello there";
        assertTrue(Message.checkMessageLength(testText));
    }

    @Test
    public void testMessageID() {
        Message message = new Message(
            1,
            "+27821234567",
            "Hello"
        );

        assertTrue(message.checkMessageID());
    }

    @Test
    public void testMessageHashCreation() {
        Message msg = new Message(
            1,
            "+27718693002",
            "HI Mike, can you join us for dinner tonight?"
        );

        assertNotNull(msg.getMessageHash());
        assertTrue(msg.getMessageHash().contains(":1:"));
    }
}
