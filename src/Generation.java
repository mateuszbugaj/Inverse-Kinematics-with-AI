import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;

public class Generation {
    PApplet p;
    ArrayList<Manipulator> agents;
    ArrayList<Manipulator> agentsAtTarget;
    PVector initialPosition;
    int populationNumber;
    int generationCount = 0;

    public Generation(PApplet p, PVector initialPosition, int populationNumber){
        this.p = p;
        this.initialPosition = initialPosition;
        this.populationNumber = populationNumber;
        agents = new ArrayList<>();
        agentsAtTarget = new ArrayList<>();
    }

    public void newPopulation(){
        // randomize segments rotations for each new populations
        float randomRot_1 = p.random(p.radians(-90),p.radians(120));
        float randomRot_2 = p.random(p.radians(-180),p.radians(0));

        // add "populationsNumber" new members of the population
        for(int i = 0;i<populationNumber;i++){
            agents.add(new Manipulator(p,initialPosition.copy(),i));//agents.add(new Manipulator(p,initialPosition.copy(),i));
            agents.get(i).segment_1_rot = randomRot_1;
            agents.get(i).segment_2_rot = randomRot_2;
            agents.get(i).update();
        }
        generationCount++;
    }

    public void newPopulation(Manipulator agentToClone){
        float randomRot_1 = p.random(p.radians(-90),p.radians(120));
        float randomRot_2 = p.random(p.radians(0),p.radians(-180));


        for(int i = 0;i<populationNumber;i++){
            agents.add(new Manipulator(p,initialPosition.copy(),i,agentToClone.brain.copy()));

            // mutate new agents based on the generation count
            if(generationCount<150) agents.get(i).brain.mutate(0.8);
            else if(generationCount<500) agents.get(i).brain.mutate(0.3);
            else if(generationCount<500) agents.get(i).brain.mutate(0.1);
            else  agents.get(i).brain.mutate(0.05);
            agents.get(i).segment_1_rot = randomRot_1;
            agents.get(i).segment_2_rot = randomRot_2;
        }
        generationCount++;
    }

}
