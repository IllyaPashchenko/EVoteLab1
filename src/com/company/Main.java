package com.company;

import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class Main {

    public static void main(String[] args) {
        CVK cvk = new CVK();

        cvk.addCitizen("Illya", "Pashchenko", CitizenType.VOTER, true);
        cvk.addCitizen("Vasyl", "Pupkin", CitizenType.VOTER, true);
        cvk.addCitizen("Grigoriy", "Vasilchenko", CitizenType.VOTER, true);
        cvk.addCitizen("Fedor", "Soloviy", CitizenType.VOTER, true);
        cvk.addCitizen("Anastasia", "Vulchanska", CitizenType.VOTER, true);
        cvk.addCitizen("Margarita", "Volna", CitizenType.VOTER, true);
        cvk.addCitizen("Denis", "Volna", CitizenType.VOTER, true);

        cvk.addCitizen("Lev", "Petrechenko", CitizenType.CANDIDATE, true);
        cvk.addCitizen("Slava", "Ukrainko", CitizenType.CANDIDATE, true);
        cvk.addCitizen("Yuriy", "Sokolenko", CitizenType.CANDIDATE, true);

        Set<UUID> candidatesId = cvk.getCandidates().keySet();

        for (Citizen citizen : cvk.getCitizens()) {
            citizen.formPackagesAndSend(candidatesId);
            citizen.vote(randomCandidate(candidatesId));
        }
//        cvk.getCitizens().get(4).vote(randomCandidate(candidatesId));

        cvk.printStandings();
    }

    public static UUID randomCandidate(Set<UUID> set){
        int size = set.size();
//        int item = set.size()-1;
        int item = new Random().nextInt(size);
        int i = 0;
        for(UUID id : set)
        {
            if (i == item)
                return id;
            i++;
        }
        return null;
    }
}
