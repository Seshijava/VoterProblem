package snippet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

public class Main {
    private static Area[] regions = new Area[5];
    public static Set<Integer> voterIDs;
    private static int count;
    private static HashMap<String, Integer> contestantPoints;
    private static HashMap<String, Integer> totalcontestantPoints = null;


    public static void main(String[] args) {
        voterIDs = new HashSet<Integer>();

        try {
            count = 0;
            String sCurrentLine;
                Scanner file;
            PrintWriter writer;

            try {

                file = new Scanner(new File("voting.dat"));
                writer = new PrintWriter("voting.txt");

                while (file.hasNext()) {
                    String line = file.nextLine();
                    if (!line.isEmpty()) {
                        writer.write(line);
                        writer.write("\n");
                    }
                }

                file.close();
                writer.close();

            } catch (FileNotFoundException ex) {
            }



            BufferedReader br = new BufferedReader(new FileReader("voting.dat"));
            while ((sCurrentLine = br.readLine()) != null) {
                if (sCurrentLine.contains("&&") || sCurrentLine.isEmpty())
                    break;

                if (sCurrentLine.contains("/") && sCurrentLine.contains(" ")) {
                    String[] parts = sCurrentLine.split("/");
                    String[] contestants = parts[1].split(" ");
                    regions[count] = new Area(parts[0].trim());
                    regions[count].addContestant(contestants);
                    count++;


                }
                if (!sCurrentLine.contains(" ") && !sCurrentLine.isEmpty()) {
//                    System.out.println(sCurrentLine);
                    for (int i = 0; i < count; i++) {
                        if (regions[i].toString().equals(sCurrentLine)) {
                            do {
                                sCurrentLine = br.readLine();
                                if (sCurrentLine.isEmpty())
                                    break;
                                if (sCurrentLine.matches(".*\\d\\s.*")) {
                                    String[] parts = sCurrentLine.split(" ", 2);
                                    String[] votes = parts[1].split(" ");
                                    if (votes.length > 1) {
                                        Integer voterID = Integer.parseInt(parts[0]);
                                        regions[i].addVotes(voterID, Arrays.copyOfRange(votes, 0, votes.length));
                                        voterIDs.add(voterID);
                                    }

                                }


                            } while (sCurrentLine.contains(" ") );

                        }


                    }
                }


            }
        } catch (IOException e) {

            e.printStackTrace();

        }
        displayCheifOfficer();
        displayRegionalHeads();
    }

    public static void mergeAllRegionContestantPoints(HashMap<String, Integer> a) {
        if (totalcontestantPoints == null) {
            totalcontestantPoints = new HashMap<String, Integer>();
            for (String s : a.keySet()) {
                totalcontestantPoints.put(s, a.get(s));
            }
        } else {
            for (String s : a.keySet()) {
                if (totalcontestantPoints.containsKey(s)) {
                    totalcontestantPoints.put(s, a.get(s) + totalcontestantPoints.get(s));
                } else {
                    totalcontestantPoints.put(s, a.get(s));
                }
            }
        }
    }

    public static void displayCheifOfficer() {
        {
            for (int i = 0; i < count; i++) {
                contestantPoints = regions[i].getContestantPoints();
                if (contestantPoints != null)
                    mergeAllRegionContestantPoints(contestantPoints);
            }
            Map.Entry<String, Integer> entry = MaxHashMap.maxHashMapValue(totalcontestantPoints);
            System.out.println("Chief Officer is : " + entry.getKey() + " with total points: " + entry.getValue());
        }
    }


    public static void displayRegionalHeads() {
        for (int i = 0; i < count; i++) {
            regions[i].displayRegionalHead();
            System.out.println("Invalid vote count in region " + regions[i].toString() + " is " + regions[i].getInvalidVoteCount());

        }
    }
}


class Area {
    private String name;
    private ArrayList<String> contestants;
    private Map<Integer, String[]> votes;
    private HashMap<String, Integer> points;
    private int invalidVoteCount;
    private static final Integer firstPrefernce = 3;
    private static final Integer secondPrefernce = 2;
    private static final Integer thirdPrefernce = 1;


    public void addContestant(String[] contestantList) {
        for (String contestantName : contestantList)
            contestants.add(contestantName);


    }


    public void addVotes(Integer voterID, String[] listOfPreferences) {
        if (!Main.voterIDs.contains(voterID) && (listOfPreferences.length > 0 && listOfPreferences.length < 4) && isListOfPreferencesValid(listOfPreferences)) {
            votes.put(voterID, listOfPreferences);

        } else {
            invalidVoteCount++;
        }

    }


    public HashMap<String, Integer> getContestantPoints() {
        if (!votes.isEmpty()) {
            for (Map.Entry<Integer, String[]> entry : votes.entrySet()) {
                String[] list = entry.getValue();
                int rank = 1;
                for (String member : list) {
                    Integer actualValue = points.get(member);
                    if (actualValue == null)
                        actualValue = 0;
                    if (rank == 1)
                        points.put(member, calculatePoints(actualValue, firstPrefernce));
                    else if (rank == 2)
                        points.put(member, calculatePoints(actualValue, secondPrefernce));
                    else if (rank == 3)
                        points.put(member, calculatePoints(actualValue, thirdPrefernce));
                    rank++;
                }
            }
            return points;
        } else
            return null;
    }


    public Integer calculatePoints(int actualValue, int toBeAdded) {
        return actualValue + toBeAdded;

    }

    public boolean isContestantValid(String contestantName) {
        return contestants.contains(contestantName);

    }

    public Area(String name) {
        this.name = name;
        contestants = new ArrayList<String>();
        votes = new HashMap<Integer, String[]>();
        points = new HashMap<String, Integer>();
        invalidVoteCount = 0;
    }

    public boolean isListOfPreferencesValid(String[] memberList) {
        Set<String> memberSet = new HashSet<String>();
        for (String memberName : memberList) {
            if ((memberSet.contains(memberName.trim()) || (!isContestantValid(memberName))))
                return false;
            memberSet.add(memberName);
        }
        return true;
    }

    public void displayRegionalHead() {
        if (!points.isEmpty()) {
            Map.Entry<String, Integer> entry = MaxHashMap.maxHashMapValue(points);
            System.out.println("Regional Head for region " + toString() + " is : " + entry.getKey() + " with total points: " + entry.getValue());
        } else {
            System.out.println("No valid votes found");
        }
    }


    public int getInvalidVoteCount() {
        return invalidVoteCount;
    }


    public String toString() {
        return name;
    }

}

class MaxHashMap {
    public static Entry<String, Integer> maxHashMapValue(HashMap<String, Integer> map) {
        int maxValueInMap = (Collections.max(map.values()));
        for (Entry<String, Integer> entry : map.entrySet()) {
            if (entry.getValue() == maxValueInMap) {
                return entry;
            }
        }
        return null;
    }
}