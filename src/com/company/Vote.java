package com.company;

import java.security.PublicKey;
import java.util.UUID;

public class Vote {
    PublicKey voterPublicKey;
    UUID voterID;
    byte[] encryptedMessage;
    byte[] sigBytes;

    public UUID getVoterID() {
        return voterID;
    }

    public void setVoterID(UUID voterID) {
        this.voterID = voterID;
    }

    public PublicKey getVoterPublicKey() {
        return voterPublicKey;
    }

    public void setVoterPublicKey(PublicKey voterPublicKey) {
        this.voterPublicKey = voterPublicKey;
    }

    public byte[] getEncryptedMessage() {
        return encryptedMessage;
    }

    public void setEncryptedMessage(byte[] encryptedMessage) {
        this.encryptedMessage = encryptedMessage;
    }

    public byte[] getSigBytes() {
        return sigBytes;
    }

    public void setSigBytes(byte[] sigBytes) {
        this.sigBytes = sigBytes;
    }
}
