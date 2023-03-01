package com.adventofcode.repository;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Set;

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
    public void add(S message) {

        try(
            Jedis conn = poolConnections.getResource();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
        ) {

            oos.writeObject(message);
            oos.flush();

            conn.sadd(keyNameByte, baos.toByteArray());

        } catch(Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public Set<S> intersect(String... otherSets) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'intersect'");
    }

    @Override
    public Set<S> intersect(byte[]... otherSets) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'intersect'");
    }
}
