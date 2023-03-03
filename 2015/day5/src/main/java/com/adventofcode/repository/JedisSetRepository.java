package com.adventofcode.repository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Set;
import java.util.stream.Collectors;

import com.adventofcode.factory.JedisPoolFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class JedisSetRepository<S extends Serializable> implements SetRepository<S> {

    private final JedisPool poolConnections = JedisPoolFactory.getInstance();
    private final String keyName;
    private final byte[] keyNameByte;

    public JedisSetRepository(String keyName) {
        this.keyName = keyName;
        this.keyNameByte = this.keyName.getBytes();
    }

    @Override
    public String getKeyName() {
        return keyName;
    }

    @Override
    public void add(S message) {

        try (
                Jedis conn = poolConnections.getResource();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos)) {

            oos.writeObject(message);
            oos.flush();

            conn.sadd(keyNameByte, baos.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public Set<S> intersect(SetRepository<S>... otherRepos) {

        String[] keyNames = new String[otherRepos.length];

        for(int i = 0; i < keyNames.length; i++) {
            keyNames[i] = otherRepos[i].getKeyName();
        }

        return intersect(keyNames);
    }

    @Override
    public Set<S> intersect(String... otherSets) {

        byte[][] byteArrays = new byte[otherSets.length][];

        for (int i = 0; i < byteArrays.length; i++) {
            byteArrays[i] = otherSets[i].getBytes();
        }

        return intersect(byteArrays);
    }

    @Override
    public Set<S> intersect(byte[]... otherSets) {

        byte[][] allSets = new byte[otherSets.length + 1][];
        allSets[0] = keyNameByte;

        for(int i = 0; i < otherSets.length; i++) {
            allSets[i + 1] = otherSets[i];
        }

        try (Jedis conn = poolConnections.getResource()) {

            return conn
                    .sinter(allSets)
                    .stream()
                    .map(byteArray -> {

                        try (
                                ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
                                ObjectInputStream ois = new ObjectInputStream(bais)) {

                            return (S) ois.readObject();
                        } catch (Exception e) {
                            e.printStackTrace();
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
