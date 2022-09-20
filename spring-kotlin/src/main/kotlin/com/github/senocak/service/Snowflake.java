package com.github.senocak.service;

import org.springframework.stereotype.Service;
import java.net.NetworkInterface;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Enumeration;

/**
 * A singleton class meant to be used by individual services to generate unique identifiers. These identifiers are 64-bit
 * (will fit into a MySQL BIGINT) and thus can be used as primary keys without impacting indexing performance. These
 * identifiers should be used as an alternative to autogenerated primary IDs from Hibernate.
 * <a href="https://web.archive.org/web/20170710132916/http://www.onjava.com/pub/a/onjava/2006/09/13/dont-let-hibernate-steal-your-identity.html">for more information on this.</a>
 */
@Service
public class Snowflake {

    private static final int TOTAL_BITS = 64;
    private static final int EPOCH_BITS = 42;
    private static final int NODE_ID_BITS = 10;
    private static final int SEQUENCE_BITS = 12;

    private static final int MAX_NODE_ID = (int)(Math.pow(2, NODE_ID_BITS) - 1);
    private static final int MAX_SEQUENCE = (int)(Math.pow(2, SEQUENCE_BITS) - 1);

    // custom epoch (January 1st, 2018 Midnight UTC = 2018-01-01T00:00:00Z)
    private static final long CUSTOM_EPOCH = 1514764800000L;

    private final int nodeId;
    private volatile long lastTimestamp = -1L;
    private volatile long sequence = 0L;

    public Snowflake() {
        this(createNodeId());
    }

    private Snowflake(int nodeId) {
        this.nodeId = nodeId;
    }

    /**
     * Generates a unique "snowflake" (64-bit numerical identifier) to be used for object identity.
     * The snowflake is composed as follows (from left to right):
     *  - 42 bits of epoch timestamp, adjusted by our arbitrary custom epoch
     *  - 10 bits of node identifier, derived from system MAC address
     *  - 12 bits of locally incremented sequence counter (reset each millisecond)
     *  This formula limits us to generating 4096 snowflakes per millisecond.
     * @return 64-bit snowflake
     */
    public synchronized long getNextId() {
        long currentTimestamp = timestamp();
        if(currentTimestamp < lastTimestamp) {
            throw new IllegalStateException("System clock is out of sync - Instant.now() went back in time");
        }

        if(currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if(sequence == 0) {
                // We have exhausted the sequence, wait until the next millisecond
                currentTimestamp = waitNextMillis(currentTimestamp);
            }
        } else {
            // reset sequence to start with zero for the next millisecond
            sequence = 0;
        }

        lastTimestamp = currentTimestamp;

        long id = currentTimestamp << (TOTAL_BITS - EPOCH_BITS);
        id |= ((long) nodeId << (TOTAL_BITS - EPOCH_BITS - NODE_ID_BITS));
        id |= sequence;
        return id;
    }

    /**
     * This method generates an epoch timestamp, offset by the arbitrarily chosen CUSTOM_EPOCH to increase the number of
     * IDs that can be generated without hitting the limit of a 64-bit integer.
     *
     * @return epoch timestamp, adjusted by CUSTOM_EPOCH
     */
    private static long timestamp() {
        return Instant.now().toEpochMilli() - CUSTOM_EPOCH;
    }

    // Blocks until the next millisecond to ensure we get a unique timestamp when generating snowflake
    private long waitNextMillis(long currentTimestamp) {
        while (currentTimestamp == lastTimestamp) {
            currentTimestamp = timestamp();
        }
        return currentTimestamp;
    }

    /**
     * This method uses the MAC address of the container/host to generate an identifier that should be unique within a
     * given Kuberneters cluster.
     *
     * @return node identifier
     */
    private static int createNodeId() {
        int nodeId;
        try {
            StringBuilder sb = new StringBuilder();
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while(networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                byte[] macAddress = networkInterface.getHardwareAddress();
                if (macAddress != null) {
                    for(byte b : macAddress) {
                        sb.append(String.format("%02X", b));
                    }
                }
            }
            if (sb.length() == 0) {
                throw new Exception("No MAC address found");
            }

            nodeId = sb.toString().hashCode();
        } catch(Exception e) {
            nodeId = (new SecureRandom().nextInt());
        }

        nodeId = nodeId & MAX_NODE_ID;
        return nodeId;
    }
}