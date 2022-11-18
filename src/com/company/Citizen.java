package com.company;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class Citizen {
    public UUID id;
    public String firsName;
    public String surname;
    ArrayList<Vote> signedVotePackage;
    boolean voteAllowed;
    boolean voted;

    private PrivateKey privateKey;
    public PublicKey publicKey;
    public CVK cvk;
    public CitizenType citizenType;

    public Citizen(String firsName, String surname, CVK cvk, CitizenType citizenType, boolean voteAllowed) {
        this.id = UUID.randomUUID();
        this.firsName = firsName;
        this.surname = surname;
        this.cvk = cvk;
        this.citizenType = citizenType;
        this.voteAllowed = voteAllowed;
        voted = false;

        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(512, new SecureRandom());
            KeyPair keyPair = keyGen.generateKeyPair();
            publicKey = keyPair.getPublic();
            privateKey = keyPair.getPrivate();
        } catch (NoSuchAlgorithmException ex) {
            System.out.println("Message " + ex.getMessage());
        }
    }

    public void formPackagesAndSend(Set<UUID> candidatesId){
        ArrayList<ArrayList<Vote>> result = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            ArrayList<Vote> votePackage = formVotePackage(candidatesId);
            result.add(votePackage);
        }

        signedVotePackage = cvk.validatePackages(result);
    }

    public ArrayList<Vote> formVotePackage(Set<UUID> candidatesId){
        ArrayList<Vote> votePackage = new ArrayList<>();
        for (UUID candidate : candidatesId){
            Vote vote = new Vote();
            vote.setVoterID(id);
            vote.setVoterPublicKey(publicKey);
            String messageString = "I vote " + candidate;

            try {
                //Encrypt message with private key
                Cipher encryptionCipher = Cipher.getInstance("RSA");
                encryptionCipher.init(Cipher.ENCRYPT_MODE, privateKey);
                byte[] message = messageString.getBytes();
                byte[] encryptedMessage =
                        encryptionCipher.doFinal(message);

                vote.setEncryptedMessage(encryptedMessage);

            } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException
                    | IllegalBlockSizeException | BadPaddingException ex) {
                System.out.println(ex.getMessage());
            }

            votePackage.add(vote);
        }
        return votePackage;
    }

    public void vote(UUID candidateID) {
        if (voteAllowed) {
            try {
                Vote neededVote = selectFromSigned(candidateID);

                //Decrypt vote
                Cipher decryptionCipher = Cipher.getInstance("RSA");
                decryptionCipher.init(Cipher.DECRYPT_MODE, publicKey);
                byte[] decryptedMessage = decryptionCipher.doFinal(neededVote.getEncryptedMessage());
//            String decryption = new String(decryptedMessage);
//            System.out.println(decryption);

                //Encrypt message with CVK key
                Cipher encryptionCipher = Cipher.getInstance("RSA");
                encryptionCipher.init(Cipher.ENCRYPT_MODE, cvk.publicKey);
                byte[] encryptedMessage = encryptionCipher.doFinal(decryptedMessage);

                neededVote.setEncryptedMessage(encryptedMessage);

                cvk.checkVote(neededVote);
            } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException
                    | IllegalBlockSizeException | BadPaddingException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    public Vote selectFromSigned(UUID candidate){
        try {
            Cipher decryptionCipher = Cipher.getInstance("RSA");
            decryptionCipher.init(Cipher.DECRYPT_MODE, publicKey);
            for (Vote vote : signedVotePackage) {
                byte[] decryptedMessage = decryptionCipher.doFinal(vote.getEncryptedMessage());

                String decryption = new String(decryptedMessage);
                if (UUID.fromString(decryption.split(" ")[2]).equals(candidate)) {
//                    vote.setEncryptedMessage(decryptedMessage);
                    return vote;
                }
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException | NoSuchPaddingException | BadPaddingException e) {
            System.out.println(e.getMessage());
        }
        System.out.println("There is no such candidate");
        return null;
    }

    public UUID getId() {
        return id;
    }

    public String getFirsName() {
        return firsName;
    }

    public String getSurname() {
        return surname;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public boolean isVoted() {
        return voted;
    }

    public void setVoted(boolean voted) {
        this.voted = voted;
    }
}
