package com.adventofcode.repository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class JedisQueueRepository<S extends Serializable> implements QueueRepository<S> {

    private final JedisPool poolConnections;
    private final String keyName;
    private final byte[] keyNameByte;

    public JedisQueueRepository(
            JedisPool poolConnections,
            String keyName) {
        this.poolConnections = poolConnections;
        this.keyName = keyName;
        this.keyNameByte = this.keyName.getBytes();
    }

    @Override
    public void add(S message) {

        try (
                Jedis conn = poolConnections.getResource();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos)) {

            oos.writeObject(message);
            oos.flush();

            conn.lpush(keyNameByte, baos.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addAll(List<S> messages) {

        try (Jedis conn = poolConnections.getResource()) {

            byte[][] convertedMessages = new byte[messages.size()][];

            for(int i = 0; i < convertedMessages.length; i++) {

                try(ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(baos)) {

                    oos.writeObject(messages.get(i));
                    oos.flush();

                    convertedMessages[i] = baos.toByteArray();
                }
            }

            conn.lpush(keyNameByte, convertedMessages);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<S> pop() {

        try (Jedis connection = poolConnections.getResource()) {
            List<byte[]> popped = connection.brpop(2, keyNameByte);

            byte[] byteArray = null;

            if(popped == null || popped.isEmpty() || (byteArray = popped.get(1)) == null) {
                return Optional.empty();
            }

            try (
                ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
                ObjectInputStream ois = new ObjectInputStream(bais)) {

                    return Optional.of((S) ois.readObject());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
