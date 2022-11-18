package com.company;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.util.*;

public class CVK {
    private PrivateKey privateKey;
    public PublicKey publicKey;
    ArrayList<Citizen> citizens;
    HashSet<UUID> voters;
    HashMap<UUID, Integer> candidates;

    public CVK() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(512, new SecureRandom());
            KeyPair keyPair = keyGen.generateKeyPair();
            publicKey = keyPair.getPublic();
            privateKey = keyPair.getPrivate();

            citizens = new ArrayList<>();
            candidates = new HashMap<>();
            voters = new HashSet<>();
        } catch (NoSuchAlgorithmException e) {
            System.out.println(e.getMessage());
        }
    }

    public void addCitizen(String firstName, String lastname, CitizenType type, boolean voteAllowed) {
        Citizen citizen = new Citizen(firstName, lastname, this, type, voteAllowed);
        citizens.add(citizen);
        if (citizen.citizenType == CitizenType.CANDIDATE){
            candidates.put(citizen.getId(), 0);
        }
        if (voteAllowed){
            voters.add(citizen.getId());
        }

    }

    public ArrayList<Vote> validatePackages(ArrayList<ArrayList<Vote>> packages){
        Random random = new Random();
        int preservedPackageNumber = random.nextInt(9);
        ArrayList<Vote> reservedPackage = packages.remove(preservedPackageNumber);
        UUID voterId = reservedPackage.get(0).getVoterID();
        // check every other package but one
        for (ArrayList<Vote> votePackage : packages) {
            for (Vote vote : votePackage) {
                if (!checkVoteCorrect(vote) || vote.getVoterID() != voterId) {
                    System.out.println("There was a mistake in a package");
                    return null;
                }
            }
        }

        // check that voter
        if (voters.contains(voterId)) {
            // if all correct, blindly sign the untouched package
            try {
                Signature signature = Signature.getInstance("SHA1withRSA");
                signature.initSign(privateKey, new SecureRandom());
                for (Vote vote : reservedPackage) {
                    signature.update(vote.getEncryptedMessage());
                    byte[] sigBytes = signature.sign();

                    vote.setSigBytes(sigBytes);
                }

                return reservedPackage;

            } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
                System.out.println(e.getMessage());
            }
        } else {
            System.out.println("You dont have the right to vote");
        }

        return null;
    }

    public boolean checkVoteCorrect(Vote vote){
        try {
            Cipher decryptionCipher = Cipher.getInstance("RSA");
            decryptionCipher.init(Cipher.DECRYPT_MODE, vote.voterPublicKey);
            byte[] decryptedMessage = decryptionCipher.doFinal(vote.getEncryptedMessage());
            String decryption = new String(decryptedMessage);
//            System.out.println("decrypted message = "+decryption);
            return decryption.split(" ").length == 3 &&
                    candidates.containsKey(UUID.fromString(decryption.split(" ")[2]));

        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | BadPaddingException
                | IllegalBlockSizeException e){
            System.out.println(e.getMessage());
        }
        return false;
    }

    public void checkVote(Vote vote){
        try {
            // Verify
            Signature signature = Signature.getInstance("SHA1withRSA");
            signature.initVerify(publicKey);
            signature.update(vote.encryptedMessage);
            boolean result = signature.verify(vote.getSigBytes());

            if (!result) {
                // Decrypt
                Cipher decryptionCipher = Cipher.getInstance("RSA");
                decryptionCipher.init(Cipher.DECRYPT_MODE, privateKey);
                byte[] decryptedMessage = decryptionCipher.doFinal(vote.getEncryptedMessage());
                String decryption = new String(decryptedMessage);
//                System.out.println("decrypted message = " + decryption);

                if (!voters.contains(vote.getVoterID())){
                    System.out.println("Incorrect voter or this voter has already voted");
                } else {
                    // Count in
                    UUID chosenId = UUID.fromString(decryption.split(" ")[2]);
                    if (candidates.containsKey(chosenId)){
                        Integer currentVotes = candidates.get(chosenId);
                        candidates.put(chosenId, currentVotes + 1);
                        voters.remove(vote.getVoterID());
                        System.out.println("Voter with id: " + vote.getVoterID() + " successfully voted");
                    } else {
                        System.out.println("The candidate given is wrong");
                    }
                }
            } else {
                System.out.println("Signature verification failed");
            }

        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | BadPaddingException
                | IllegalBlockSizeException | SignatureException e){
            System.out.println(e.getMessage());
        }

    }

    public ArrayList<Citizen> getCitizens() {
        return citizens;
    }

    public HashMap<UUID, Integer> getCandidates() {
        return candidates;
    }

    public void printStandings(){
        for (UUID id : candidates.keySet()) {
            for (Citizen citizen : citizens) {
                if (citizen.getId() == id) System.out.println(citizen.getFirsName() + " " + citizen.getSurname() + ": " + candidates.get(id));
            }
        }
    }
}
