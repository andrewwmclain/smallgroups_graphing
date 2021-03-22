package org.jgrapht.demo;

import org.jgrapht.*;
import org.jgrapht.graph.*;

import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.Scanner; // Import the Scanner class to read text files
import java.util.ArrayList; // import the ArrayList class

import javax.swing.JFrame;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

public class communities extends JFrame{

    //Graph building class
    public static class GraphBuilder extends JFrame
    {

        private static final long serialVersionUID = -2707712944901661771L;

        //Graph building class constructor
        public GraphBuilder(ArrayList<Integer>[] neighbors, String[] nameIndexes)
        {
            //build graph
            mxGraph graph = new mxGraph();
            Object parent = graph.getDefaultParent();

            graph.getModel().beginUpdate();

            //add vertices and edges
            try
            {
                //build the graph nodes around a square
                ArrayList<Object> vertexes = new ArrayList<Object>();

                //top side of square
                for(int i = 0; i < neighbors.length / 4; ++i){
                    vertexes.add(graph.insertVertex(parent, null, nameIndexes[i], 70*(i+1), 70, 40, 40));
                }
                //right side of square
                for(int i = neighbors.length / 4; i < neighbors.length / 2; ++i){
                    int diff = i - (neighbors.length / 4);
                    vertexes.add(graph.insertVertex(parent, null, nameIndexes[i], 70*(neighbors.length / 4), 70*(diff+1), 40, 40));
                }
                //bottom side of square
                for(int i = neighbors.length / 2; i < neighbors.length * 3 / 4; ++i){
                    int diff = i - (neighbors.length / 2);
                    vertexes.add(graph.insertVertex(parent, null, nameIndexes[i], 70*(diff+1), 70 + 70*(neighbors.length / 4), 40, 40));
                }
                //left side of square
                for(int i = neighbors.length * 3 / 4; i < neighbors.length; ++i){
                    int diff = i - (neighbors.length * 3 / 4);
                    vertexes.add(graph.insertVertex(parent, null, nameIndexes[i], 70, 70 + 70*(diff+1), 40, 40));
                }
                //add the edges
                for(int i = 0; i < neighbors.length; ++i){
                    Object v1 = vertexes.get(i);
                        for(int j = 0; j < neighbors[i].size(); ++j){
                            Object v2 = vertexes.get(neighbors[i].get(j));
                            graph.insertEdge(parent, null, null, v1, v2);
                        }
                }
            }
            finally
            {
                graph.getModel().endUpdate();
            }

            mxGraphComponent graphComponent = new mxGraphComponent(graph);
            getContentPane().add(graphComponent);
        }
    }

    static int edgeCount = 0;

    //make a single group for a specific week
    //@params numPeople, weekHosts, weekInvitees, invitedEveryone, nameIndexes,
    //groupSize, neighbors, g, partners
    public static void makeGroup(int numPeople, boolean[] weekHosts,
                                 boolean[] weekInvitees, boolean[] invitedEveryone,
                                 String[] nameIndexes, int groupSize,
                                 ArrayList<Integer>[] neighbors,
                                 Graph<String, DefaultEdge> g,
                                 ArrayList<Integer> partners
    ){
        //host is not found yet, will set to true when host found
        boolean hostFound = false;
        //find a host first and then invitees, searching through every person (vertex) if necessary
        for(int j = 0; j < numPeople; ++j){
            //find a host
            //if this person is hosting this week already, keep searching
            if(weekHosts[j] == true){
                continue;
            }else if(weekInvitees[j] == true){ //if person is an invitee this week, keep searching
                continue;
            }else if(invitedEveryone[j] == true){ //if person has invited everyone already, keep searching
                continue;
            }else{
                int start = 0;
                //host found. Mark person as host for this week.
                weekHosts[j] = true;
                //if the host has a partner, make their partner a host as well
                if(partners.get(j) != -1){
                    weekHosts[partners.get(j)] = true;
                    start++;
                }
                hostFound = true;
                System.out.print(nameIndexes[j]);
                if(partners.get(j) != -1){
                    System.out.print(", " + nameIndexes[partners.get(j)] + " ");
                }else{
                    System.out.print(" ");
                }
                System.out.print("(hosting) ");
                //find invitees for group with this host. # of invitees = groupSize
                int groupInvitees = 0;
                for(int k = start; k < groupSize; ++k){
                    //search for an invitee
                    for(int l = 0; l < numPeople; ++l){
                        //if person is a host this week, don't invite them
                        if(weekHosts[l] == true){
                            continue;
                        }else if(weekInvitees[l] == true){ //if person is already invitee this week,
                            continue;                      //don't invite them
                        }else if(neighbors[j].contains(l)){ //if person has already been invited by
                            continue;                       //this host in prior week, don't invite them
                        }//if person is married and their partner would exceed group size, don't
                         // invite them
                        else if(partners.get(l) != -1 && (groupSize - k < 2)){
                            continue;
                        }else{
                            //add invitee and update graph
                            weekInvitees[l] = true;
                            groupInvitees++;
                            //if invitee has partner, add them to group
                            if(partners.get(l) != -1){
                                weekInvitees[partners.get(l)] = true;
                                groupInvitees++;
                                k++;
                            }
                            System.out.print("| " + nameIndexes[l]);
                            if(partners.get(l) != -1){
                                System.out.print(", " + nameIndexes[partners.get(l)] + " ");
                            }else{
                                System.out.print(" ");
                            }
                            //if person has never been invited by this host, add them to list of
                            //hosts invitee history
                            if(!neighbors[j].contains(l)){
                                neighbors[j].add(l);
                                g.addEdge(nameIndexes[j], nameIndexes[l]);
                                edgeCount++;
                                //update host's "invited everyone" status if necessary
                                if(neighbors[j].size() == numPeople - 1){
                                    invitedEveryone[j] = true;
                                }
                                if(partners.get(l) != -1){
                                    neighbors[j].add(partners.get(l));
                                    g.addEdge(nameIndexes[j], nameIndexes[partners.get(l)]);
                                    edgeCount++;
                                    if(neighbors[j].size() == numPeople - 1){
                                        invitedEveryone[j] = true;
                                    }
                                }
                                //repeat process with invitees partner if applicable
                                if(partners.get(j) != -1){
                                    neighbors[partners.get(j)].add(l);
                                    g.addEdge(nameIndexes[partners.get(j)], nameIndexes[l]);
                                    edgeCount++;
                                    if(neighbors[partners.get(j)].size() == numPeople - 1){
                                        invitedEveryone[partners.get(j)] = true;
                                    }
                                    if(partners.get(l) != -1){
                                        neighbors[partners.get(j)].add(partners.get(l));
                                        g.addEdge(nameIndexes[partners.get(j)], nameIndexes[partners.get(l)]);
                                        edgeCount++;
                                        if(neighbors[partners.get(j)].size() == numPeople - 1){
                                            invitedEveryone[partners.get(j)] = true;
                                        }
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
                //if there were no possible invitees who that host hasn't already
                //hosted before, invite anyone who is not already in a group this week
                if(groupInvitees < groupSize){
                    int val = groupSize - groupInvitees;
                    for(int k = start; k < val; ++k){
                            for(int l = 0; l < numPeople; ++l){
                            //if person is a host this week, don't invite them
                            if(weekHosts[l] == true){
                                continue;
                            }else if(weekInvitees[l] == true){ //don't invite people already invited
                                                               // this week
                                continue;
                            }//if person is married, don't invite them if their partner exceeds group
                             //size
                            else if(partners.get(l) != -1 && ((groupSize - groupInvitees) - k < 2)){
                                continue;
                            }else{
                                //update invitees list for invitee and partner if applicable
                                weekInvitees[l] = true;
                                groupInvitees++; //
                                if(partners.get(l) != -1){
                                    groupInvitees++; //
                                    k++;
                                    weekInvitees[partners.get(l)] = true;
                                }
                                System.out.print("| " + nameIndexes[l]);
                                if(partners.get(l) != -1){
                                    System.out.print(", " + nameIndexes[partners.get(l)] + " ");
                                }else{
                                    System.out.print(" ");
                                }
                                //update host's invitee history list.
                                if(!neighbors[j].contains(l)){
                                    neighbors[j].add(l);
                                    g.addEdge(nameIndexes[j], nameIndexes[l]);
                                    edgeCount++;
                                    //update host's "invited everyone" status if necessary
                                    if(neighbors[j].size() == numPeople - 1){
                                        invitedEveryone[j] = true;
                                    }
                                    if(partners.get(l) != -1){
                                        neighbors[j].add(partners.get(l));
                                        g.addEdge(nameIndexes[j], nameIndexes[partners.get(l)]);
                                        edgeCount++;
                                        if(neighbors[j].size() == numPeople - 1){
                                            invitedEveryone[j] = true;
                                        }
                                    }
                                    //repeat process for invitee's partner
                                    if(partners.get(j) != -1){
                                        neighbors[partners.get(j)].add(l);
                                        g.addEdge(nameIndexes[partners.get(j)], nameIndexes[l]);
                                        edgeCount++;
                                        if(neighbors[partners.get(j)].size() == numPeople - 1){
                                            invitedEveryone[partners.get(j)] = true;
                                        }
                                        if(partners.get(l) != -1){
                                            neighbors[partners.get(j)].add(partners.get(l));
                                            g.addEdge(nameIndexes[partners.get(j)], nameIndexes[partners.get(l)]);
                                            edgeCount++;
                                            if(neighbors[partners.get(j)].size() == numPeople - 1){
                                                invitedEveryone[partners.get(j)] = true;
                                            }
                                        }
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
                //if the group is still not full, remove another condition for entry
                if(groupInvitees < groupSize){
                    int val = groupSize - groupInvitees;
                    //find an invitee
                    for(int k = start; k < val; ++k){
                        for(int l = 0; l < numPeople; ++l){
                            if(weekHosts[l] == true){ //can't invite someone already hosting
                                continue;
                            }else if(weekInvitees[l] == true){ //can't invite someone already hosting
                                continue;
                            }else{
                                //add invitee and update invitee list, repeat process for invitee's
                                //partner if applicable
                                weekInvitees[l] = true;
                                groupInvitees++;
                                if(partners.get(l) != -1){
                                    groupInvitees++;
                                    k++;
                                    weekInvitees[partners.get(l)] = true;
                                }
                                System.out.print("| " + nameIndexes[l]);
                                if(partners.get(l) != -1){
                                    System.out.print(", " + nameIndexes[partners.get(l)] + " ");
                                }else{
                                    System.out.print(" ");
                                }
                                //update host's list of neighbors
                                if(!neighbors[j].contains(l)){
                                    neighbors[j].add(l);
                                    g.addEdge(nameIndexes[j], nameIndexes[l]);
                                    edgeCount++;
                                    if(neighbors[j].size() == numPeople - 1){
                                        invitedEveryone[j] = true;
                                    }
                                    if(partners.get(l) != -1){
                                        neighbors[j].add(partners.get(l));
                                        g.addEdge(nameIndexes[j], nameIndexes[partners.get(l)]);
                                        edgeCount++;
                                        if(neighbors[j].size() == numPeople - 1){
                                            invitedEveryone[j] = true;
                                        }
                                    }

                                    if(partners.get(j) != -1){
                                        neighbors[partners.get(j)].add(l);
                                        g.addEdge(nameIndexes[partners.get(j)], nameIndexes[l]);
                                        edgeCount++;
                                        if(neighbors[partners.get(j)].size() == numPeople - 1){
                                            invitedEveryone[partners.get(j)] = true;
                                        }
                                        if(partners.get(l) != -1){
                                            neighbors[partners.get(j)].add(partners.get(l));
                                            g.addEdge(nameIndexes[partners.get(j)], nameIndexes[partners.get(l)]);
                                            edgeCount++;
                                            if(neighbors[partners.get(j)].size() == numPeople - 1){
                                                invitedEveryone[partners.get(j)] = true;
                                            }
                                        }
                                    }
                                }
                                break;
                            }
                        }
                    }
                }

                break;
            }
        }
        //if no host was found who has not already invited everyone,
        //find anyone who is not already in a group for this week to host
        //same logic as before with one less conditional
        if(!hostFound){
            for(int j = 0; j < numPeople; ++j){
                //find a host for group i
                if(weekHosts[j] == true){
                    continue;
                }else if(weekInvitees[j] == true){
                    continue;
                }else{
                    //add the host and their partner as host if applicable
                    int start = 0;
                    weekHosts[j] = true;
                    if(partners.get(j) != -1){
                        weekHosts[partners.get(j)] = true;
                        start++;
                    }
                    System.out.print(nameIndexes[j]);
                    if(partners.get(j) != -1){
                        System.out.print(", " + nameIndexes[partners.get(j)] + " ");
                    }else{
                        System.out.print(" ");
                    }
                    System.out.print("(hosting) ");
                    //find invitees for group i
                    int groupInvitees = 0;
                    for(int k = start; k < groupSize; ++k){
                        for(int l = 0; l < numPeople; ++l){
                            //conditions for inviting, can't be in group already and
                            //can't have a partner if it makes group size too large
                            if(weekHosts[l] == true){
                                continue;
                            }else if(weekInvitees[l] == true){
                                continue;
                            }else if(neighbors[j].contains(l)){
                                continue;
                            }else if(partners.get(l) != -1 && (groupSize - k < 2)){
                                continue;
                            }else{
                                //update week invitees list
                                weekInvitees[l] = true;
                                groupInvitees++;
                                System.out.print(nameIndexes[l]);
                                if(partners.get(l) != -1){
                                    weekInvitees[partners.get(l)] = true;
                                    groupInvitees++;
                                    k++;
                                    System.out.print(", " + nameIndexes[partners.get(l)] + " ");
                                }else{
                                    System.out.print(" ");
                                }
                                //update host's list of neighbors with invitee and partner if applicable
                                if(!neighbors[j].contains(l)){
                                    neighbors[j].add(l);
                                    g.addEdge(nameIndexes[j], nameIndexes[l]);
                                    edgeCount++;
                                    if(neighbors[j].size() == numPeople - 1){
                                        invitedEveryone[j] = true;
                                    }
                                    if(partners.get(l) != -1){
                                        neighbors[j].add(partners.get(l));
                                        g.addEdge(nameIndexes[j], nameIndexes[partners.get(l)]);
                                        edgeCount++;
                                        if(neighbors[j].size() == numPeople - 1){
                                            invitedEveryone[j] = true;
                                        }
                                    }

                                    if(partners.get(j) != -1){
                                        neighbors[partners.get(j)].add(l);
                                        g.addEdge(nameIndexes[partners.get(j)], nameIndexes[l]);
                                        edgeCount++;
                                        if(neighbors[partners.get(j)].size() == numPeople - 1){
                                            invitedEveryone[partners.get(j)] = true;
                                        }
                                        if(partners.get(l) != -1){
                                            neighbors[partners.get(j)].add(partners.get(l));
                                            g.addEdge(nameIndexes[partners.get(j)], nameIndexes[partners.get(l)]);
                                            edgeCount++;
                                            if(neighbors[partners.get(j)].size() == numPeople - 1){
                                                invitedEveryone[partners.get(j)] = true;
                                            }
                                        }
                                    }
                                }
                                break;
                            }
                        }
                    }
                    //if there were no possible invitees who that host hasn't already
                    //hosted before, invite anyone who is not already in a group
                    //this week
                    if(groupInvitees < groupSize){
                        int val = groupSize - groupInvitees; //
                            for(int k = start; k < val; ++k){
                                //can't join if already in a group or partner exceeds group size
                                for(int l = 0; l < numPeople; ++l){
                                if(weekHosts[l] == true){
                                    continue;
                                }else if(weekInvitees[l] == true){
                                    continue;
                                }else if(partners.get(l) != -1 && (groupSize - k < 2)){
                                    continue;
                                }else{
                                    weekInvitees[l] = true;
                                    groupInvitees++;
                                    System.out.print("| " + nameIndexes[l]);
                                    if(partners.get(l) != -1){
                                        weekInvitees[partners.get(l)] = true;
                                        groupInvitees++;
                                        k++; //
                                        System.out.print(", " + nameIndexes[partners.get(l)] + " ");
                                    }else{
                                        System.out.print(" ");
                                    }
                                    if(!neighbors[j].contains(l)){
                                        neighbors[j].add(l);
                                        g.addEdge(nameIndexes[j], nameIndexes[l]);
                                        edgeCount++;
                                        if(neighbors[j].size() == numPeople - 1){
                                            invitedEveryone[j] = true;
                                        }
                                        if(partners.get(l) != -1){
                                            neighbors[j].add(partners.get(l));
                                            g.addEdge(nameIndexes[j], nameIndexes[partners.get(l)]);
                                            edgeCount++;
                                            if(neighbors[j].size() == numPeople - 1){
                                                invitedEveryone[j] = true;
                                            }
                                        }

                                        if(partners.get(j) != -1){
                                            neighbors[partners.get(j)].add(l);
                                            g.addEdge(nameIndexes[partners.get(j)], nameIndexes[l]);
                                            edgeCount++;
                                            if(neighbors[partners.get(j)].size() == numPeople - 1){
                                                invitedEveryone[partners.get(j)] = true;
                                            }
                                            if(partners.get(l) != -1){
                                                neighbors[partners.get(j)].add(partners.get(l));
                                                g.addEdge(nameIndexes[partners.get(j)], nameIndexes[partners.get(l)]);
                                                edgeCount++;
                                                if(neighbors[partners.get(j)].size() == numPeople - 1){
                                                    invitedEveryone[partners.get(j)] = true;
                                                }
                                            }
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                    }
                    //invite anyone who is not already in a group as a last resort
                    if(groupInvitees < groupSize){
                        int val = groupSize - groupInvitees;
                        for(int k = start; k < val; ++k){
                            for(int l = 0; l < numPeople; ++l){
                                //invite anyone who is not already in a group
                                if(weekHosts[l] == true){
                                    continue;
                                }else if(weekInvitees[l] == true){
                                    continue;
                                }else{
                                    //update invitation list
                                    weekInvitees[l] = true;
                                    groupInvitees++;
                                    if(partners.get(l) != -1){
                                        groupInvitees++;
                                        k++;
                                        weekInvitees[partners.get(l)] = true;
                                    }
                                    System.out.print("| " + nameIndexes[l]);
                                    if(partners.get(l) != -1){
                                        System.out.print(", " + nameIndexes[partners.get(l)] + " ");
                                    }else{
                                        System.out.print(" ");
                                    }
                                    if(!neighbors[j].contains(l)){
                                        neighbors[j].add(l);
                                        g.addEdge(nameIndexes[j], nameIndexes[l]);
                                        edgeCount++;
                                        if(neighbors[j].size() == numPeople - 1){
                                            invitedEveryone[j] = true;
                                        }
                                        if(partners.get(l) != -1){
                                            neighbors[j].add(partners.get(l));
                                            g.addEdge(nameIndexes[j], nameIndexes[partners.get(l)]);
                                            edgeCount++;
                                            if(neighbors[j].size() == numPeople - 1){
                                                invitedEveryone[j] = true;
                                            }
                                        }

                                        if(partners.get(j) != -1){
                                            neighbors[partners.get(j)].add(l);
                                            g.addEdge(nameIndexes[partners.get(j)], nameIndexes[l]);
                                            edgeCount++;
                                            if(neighbors[partners.get(j)].size() == numPeople - 1){
                                                invitedEveryone[partners.get(j)] = true;
                                            }
                                            if(partners.get(l) != -1){
                                                neighbors[partners.get(j)].add(partners.get(l));
                                                g.addEdge(nameIndexes[partners.get(j)], nameIndexes[partners.get(l)]);
                                                edgeCount++;
                                                if(neighbors[partners.get(j)].size() == numPeople - 1){
                                                    invitedEveryone[partners.get(j)] = true;
                                                }
                                            }
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                    }

                    break;
                }
            }
        }
        System.out.println();
    }

    public static void main(String[] args){
        //graph to model relationships between people. An edge (u, v) means that
        //person u has hosted person v. The graph is complete when there is a clique,
        //since that means everyone has hosted everyone else at their home
        Graph<String, DefaultEdge> g = new SimpleDirectedGraph<>(DefaultEdge.class);

        Scanner input = new Scanner(System.in);  // Create a Scanner object
        ArrayList<String> names = new ArrayList<String>(); // Create an ArrayList object
        ArrayList<Integer> partners = new ArrayList<Integer>(); // Create an ArrayList object

        //Get file input
        System.out.println("Enter file name: ");
        String fileName = input.nextLine();
        int numPeople = 0;
        try {
            File nameFile = new File(fileName);
            Scanner reader = new Scanner(nameFile);
            while (reader.hasNextLine()) {
                String data = reader.nextLine();
                //add married couples
                if(data.contains(",")){
                    names.add(data.substring(0, data.indexOf(',')));
                    partners.add(numPeople + 1);
                    names.add(data.substring(data.indexOf(',') + 2));
                    partners.add(numPeople);
                    numPeople++;
                }else{
                    partners.add(-1);
                    names.add(data);
                }
                numPeople++;
            }
            reader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        //get group size input
        System.out.println("Enter group size: ");
        int groupSize = Integer.parseInt(input.nextLine());  // Read user input
        //error handling for group size input
        while(groupSize <= 0 || groupSize > numPeople){
            if(groupSize <= 0){
                System.out.println("Group size must be greater than 0");
                System.out.println("Enter group size: ");
                groupSize = Integer.parseInt(input.nextLine());  // Read user input
            }else{
                System.out.println("Group size must be smaller than the number of people");
                System.out.println("Enter group size: ");
                groupSize = Integer.parseInt(input.nextLine());  // Read user input
            }
        }

        //build array to quickly access O(1) people and their names
        String[] nameIndexes = new String[numPeople];
        for(int i = 0; i < numPeople; ++i){
            nameIndexes[i] = names.get(i);
        }

        //number of edges required to form a clique = n(n-1)
        int cliqueEdgeCount = numPeople * (numPeople - 1);
        //build array to quickly O(1) check if someone has invited someone else before
        boolean[] invitedEveryone = new boolean[numPeople];
        for(int i = 0; i < numPeople; ++i){
            invitedEveryone[i] = false;
        }
        //print out list of people
        System.out.println("List of people: ");
        for(int i = 0; i < numPeople; ++i){
            g.addVertex(names.get(i));
            System.out.println("'" + nameIndexes[i] + "'");
        }


        System.out.println();
        //calculate number of groups based on group size
        int numGroups = numPeople / groupSize;
        int numExtras = numPeople % groupSize;
        //if the remainder is greater than the number of groups,
        //increase the group size appropriately
        if(numExtras > numGroups){
            groupSize += (numExtras / numGroups);
            numExtras = (numExtras % numGroups);
        }
        //build arrays to keep track of invitees and hosts
        //for each week
        boolean[] weekHosts = new boolean[numPeople];
        boolean[] weekInvitees = new boolean[numPeople];
        //adjacency list of hosts and invitees relationships
        ArrayList<Integer>[] neighbors = new ArrayList[numPeople];
        for (int i = 0; i < numPeople; i++) {
            neighbors[i] = new ArrayList<Integer>();
        }

        //married couples are automatically on each other's adjacency list
        for(int i = 0; i < numPeople; ++i){
            if(partners.get(i) != -1){
                g.addEdge(nameIndexes[i], nameIndexes[partners.get(i)]);
                edgeCount++;
                neighbors[i].add(partners.get(i));
            }
        }

        int numWeeks = 0;
        //while there are still people who haven't hosted everyone else,
        //form groups for the week
        while(edgeCount < cliqueEdgeCount){
            numWeeks++;
            System.out.println("WEEK " + numWeeks);
            //clear out hosts for the week
            for(int i = 0; i < numPeople; ++i){
                weekHosts[i] = false;
            }
            //clear out invitees for the week
            for(int i = 0; i < numPeople; ++i){
                weekInvitees[i] = false;
            }

            //find groups with extra people for the week
            for(int i = 0; i < numExtras; ++i){
                //build group i
                System.out.print("GROUP " + i + ": ");
                makeGroup(numPeople, weekHosts, weekInvitees, invitedEveryone,
                        nameIndexes, groupSize, neighbors, g, partners);
            }

            //create remaining groups for the week (groups with no extras)
            for(int i = numExtras; i < numGroups; ++i){
                //build group i
                System.out.print("GROUP " + i + ": ");
                makeGroup(numPeople, weekHosts, weekInvitees, invitedEveryone,
                        nameIndexes, groupSize - 1, neighbors, g, partners);
            }
            System.out.println();
        }

        //print adjacency list
        System.out.println("Adjacency list of everyone and everyone they invited");
        for(int i = 0; i < neighbors.length; ++i){
            System.out.print("*'" + nameIndexes[i] + "' ");
            for(int j = 0; j < neighbors[i].size(); ++j){
                System.out.print("'" + nameIndexes[neighbors[i].get(j)] + "' ");
            }
            System.out.println();
        }

        //build the graph frame and display
        GraphBuilder frame = new GraphBuilder(neighbors, nameIndexes);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(2500, 4000);
        frame.setVisible(true);
    }
}