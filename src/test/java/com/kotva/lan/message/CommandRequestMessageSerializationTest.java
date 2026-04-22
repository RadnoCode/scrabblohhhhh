package com.kotva.lan.message;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.kotva.domain.action.ActionPlacement;
import com.kotva.domain.action.ActionType;
import com.kotva.domain.action.PlayerAction;
import com.kotva.domain.model.Position;
import com.kotva.infrastructure.network.CommandEnvelope;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import org.junit.Test;

public class CommandRequestMessageSerializationTest {
    @Test
    public void commandRequestMessageSerializesPlaceAction() throws IOException, ClassNotFoundException {
        CommandRequestMessage message = new CommandRequestMessage(
                new CommandEnvelope(
                        "command-1",
                        "session-1",
                        "player-2",
                        3,
                        PlayerAction.place(
                                "player-2",
                                List.of(new ActionPlacement("tile-1", new Position(7, 8), 't')))));

        byte[] serialized = serialize(message);
        LocalGameMessage deserialized = deserialize(serialized);

        assertTrue(deserialized instanceof CommandRequestMessage);
        CommandEnvelope commandEnvelope =
                ((CommandRequestMessage) deserialized).getCommandEnvelope();
        assertNotNull(commandEnvelope);
        assertEquals("command-1", commandEnvelope.getCommandId());
        assertEquals("session-1", commandEnvelope.getSessionId());
        assertEquals("player-2", commandEnvelope.getPlayerId());
        assertEquals(3, commandEnvelope.getExpectedTurnNumber());
        assertEquals(ActionType.PLACE_TILE, commandEnvelope.getAction().type());
        assertEquals("player-2", commandEnvelope.getAction().playerId());
        assertEquals(1, commandEnvelope.getAction().placements().size());
        assertEquals("tile-1", commandEnvelope.getAction().placements().get(0).tileId());
        assertEquals(7, commandEnvelope.getAction().placements().get(0).position().getRow());
        assertEquals(8, commandEnvelope.getAction().placements().get(0).position().getCol());
        assertEquals(Character.valueOf('T'), commandEnvelope.getAction().placements().get(0).assignedLetter());
    }

    private byte[] serialize(LocalGameMessage message) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {
            objectOutputStream.writeObject(message);
        }
        return outputStream.toByteArray();
    }

    private LocalGameMessage deserialize(byte[] serialized)
            throws IOException, ClassNotFoundException {
        try (ObjectInputStream objectInputStream =
                new ObjectInputStream(new ByteArrayInputStream(serialized))) {
            return (LocalGameMessage) objectInputStream.readObject();
        }
    }
}
