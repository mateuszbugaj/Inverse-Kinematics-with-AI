import basicneuralnetwork.NeuralNetwork;
import controlP5.ControlP5;
import processing.core.PApplet;
import processing.core.PShape;
import processing.core.PVector;

import static processing.core.PConstants.CENTER;
import static processing.core.PConstants.CLOSE;
import static processing.core.PConstants.CORNER;

public class Simulation {
    PApplet p;
    ControlP5 cp5;

    Manipulator mainManipulator; // manipulator that uses inverse kinematics
    Generation generation; // generation creates and contains populations
    Manipulator bestOfGeneration; // agent that performed the best in current generation
    Manipulator infoManipulator; // agent which info to display
    final PVector BASE_POS = new PVector(180,390); // position of manipulator's base
    PVector pointerPos = new PVector();  // position of the target
    float pointerAngle = 0; // angle
    float pointerDist = 0; // and dist from the base

    boolean pause = false; // pause simulation
    boolean showRoute = false; // show all routes that agents traveled
    boolean autoSpawn = true; // spawn after all agents are dead
    int cycles = 1; // cycles per frame

    public Simulation(PApplet p){
        this.p = p;
        //this.cp5 = cp5;
        this.cp5 = new ControlP5(p);
        this.cp5.hide();
        mainManipulator = new Manipulator(BASE_POS,0);
        generation = new Generation(p,BASE_POS,50); // new generation creating population
        newSet();

        setUpButtons();
    }

    public void drawFunction(){
        for(int i = 0;i<cycles;i++) {
            p.background(224, 193, 108);
            cp5.show();
            //buttonsVisible();
            calculatePointer();
            drawMainWindow();
            drawInfoWindow();
            buttonsCaptions();
        }
    }

    public void drawMainWindow(){
        // main simulation, drawing agents, showing coordinate system
        // finding best and initiating creations of new populations

        p.pushMatrix();
        p.translate(0,0);// translate whole window if necessary

        // window stuff
        // show name and draw
        p.stroke(0);
        p.strokeWeight(2);
        p.textSize(30);
        p.fill(0);
        p.text("Work space",15,35);
        p.rectMode(CORNER);
        p.fill(219, 216, 206);
        p.rect(-285,40,1000,700);
        p.rectMode(CENTER);

        // show "pause" text
        if(pause) {
            p.fill(0);
            p.textSize(20);
            p.text("PAUSE", 20, 60);
            p.textSize(12);
        }

        // draw coordinates lines
        drawCoordinateSystem();

        // main mainManipulator
//        mainManipulator.setTarget(pointerPos);
//        mainManipulator.inverseKinematics();
//        mainManipulator.showManipulator(false);

        // agents
        // maintain agents
        if(generation.agents.size()>0){ // if there are agents
            for(Manipulator manipulator:generation.agents){ // for every agent
                manipulator.setTarget(pointerPos); // update target
                manipulator.route(showRoute); // calculate route, display if enabled
                if(!pause)manipulator.update(); // update function if not paused
                manipulator.showManipulator(true); // show manipulator (only endPoint)

                // if dist to the target is lower than 10, mark agent as dead and add it to the "agents that reached target" array
                if(manipulator.targetDistFromAgent<10){
                    manipulator.isDead = true;
                    generation.agentsAtTarget.add(manipulator);
                }
            }
        } else if(autoSpawn){ // if there are no more agents, generate new population
            newSet();
//            if(generation.generationCount>500){
//                pause = true;
//            }
        }

        findTheBest(); // find best from agents
        generation.agents.removeIf(i -> i.isDead); // delete those that are marked as dead

        // pointer
        // show pointer and line to the pointer
        p.stroke(2);
        p.stroke(133, 168, 13,100);
        p.line(BASE_POS.x,BASE_POS.y,p.cos(pointerAngle)*pointerDist+BASE_POS.x,p.sin(pointerAngle)*pointerDist+BASE_POS.y);
        p.fill(207, 95, 43);
        p.noStroke();
        p.ellipse(pointerPos.x,pointerPos.y,15,15);

        p.popMatrix();
    }

    public void drawInfoWindow(){
        // general and individual information about simulation and agents

        p.pushMatrix();
        p.translate(0,0); // translate whole window if necessary

        // display name and show
        p.stroke(0);
        p.strokeWeight(2);
        p.textSize(30);
        p.fill(0);
        p.text("Info",735,35);
        p.rectMode(CORNER);
        p.fill(219, 216, 206);
        p.rect(730,40,550,700);
        p.rectMode(CENTER);

        p.fill(0);
        p.pushMatrix();{
            p.translate(740,80);
            p.textSize(15);
            p.text("General",0,-20);
            p.line(-3,-15,150,-15);
            p.text("Pointer angle: " + (int) p.degrees(pointerAngle),0,0);
            p.text("Pointer distance: " +(int) pointerDist,0,20);
            p.text("Generation: " +generation.generationCount,0,40);
            p.text("Agents that reached the target: " +generation.agentsAtTarget.size(),0,60);
            if(generation.agents.size()>0)
                p.text("Seconds passed for this gen.: " + (int)generation.agents.get(0).timeLived/60,0,80);

            p.text("Individual",0,160);
            p.line(-3,165,150,165);

            if(generation.agents.size()>0 && infoManipulator!=null) {
                p.pushMatrix();
                {
                    p.translate(0, 180);
                    p.text("Id: " + infoManipulator.index, 0, 0);
                    p.text("Inputs: ", 0, 20);
                    p.text("1) RGB position [0]: " + (int)infoManipulator.inputs[0], 20, 40);
                    p.text("2) RGB position [1]: " + (int) infoManipulator.inputs[1], 20, 60);
                    p.text("3) RGB position [2]: " + (int)infoManipulator.inputs[2], 20, 80);
                    p.text("4) Dist from endPoint to target: " + (int)infoManipulator.inputs[3], 20, 100);
//                    p.text("5) Rotation segment 1: " + p.degrees((float)infoManipulator.inputs[4]), 20, 120);
//                    p.text("6) Rotation segment 2: " + p.degrees((float)infoManipulator.inputs[5]), 20, 140);
//                    p.text("7) Dist of target from base: " + (int)infoManipulator.inputs[6], 20, 160);
//                    p.text("8) Angle of target from base: " + p.degrees((float)infoManipulator.inputs[7]), 20, 180);
//                    p.text("9) Angle to max. rot. 1: " + p.degrees((float)infoManipulator.inputs[8]), 20, 200);
//                    p.text("10) Angle to max. rot. 2: " + p.degrees((float)infoManipulator.inputs[9]), 20, 220);
                    p.text("Outputs[0]: " + (float) infoManipulator.outputs[0], 0, 240);
                    p.text("Outputs[1]: " + (float) infoManipulator.outputs[1], 0, 260);
                    p.text("Turning acc seg_1: " + infoManipulator.turningAccSeg_1, 0, 280);
                    p.text("Turning acc seg_2: " +  infoManipulator.turningAccSeg_2, 0, 300);
                    p.text("Turning speed seg_1: " + infoManipulator.turningSpeedSeg_1, 0, 320);
                    p.text("Turning speed seg_2: " +  infoManipulator.turningSpeedSeg_2, 0, 340);
                    p.text("Rotation 1: " + p.degrees(infoManipulator.segment_1_rot), 0, 360);
                    p.text("Rotation 2: " +  p.degrees(infoManipulator.segment_2_rot_INTERNAL), 0, 380);
                    p.text("Dist traveled: " +  infoManipulator.distTraveled, 0, 400);
                    p.text("Dist traveled2: " +  infoManipulator.distTraveled2, 0, 420);
                    //text("Is marked as best: " +  infoManipulator.isBest, 0, 420);
                    p.text("Dist from endPoint to target: " +  infoManipulator.targetDistFromAgent, 0, 440);
                    p.text("Angle from endPoint to target: " +  p.degrees(infoManipulator.targetAngleFromAgent), 0, 460);
                }
                p.popMatrix();
            }

        }p.popMatrix();

        p.popMatrix();

    }

    public void drawCoordinateSystem(){
        // draw coordinates lines

        p.pushMatrix();{
            p.translate(BASE_POS.x,BASE_POS.y);


            // angle to the pointer
            p.noStroke();
            p.fill(133, 168, 13);
            p.ellipse(0,0,95,95);
            p.fill(219, 216, 206);
            p.ellipse(0,0,75,75);

            // shape to cover
            PShape cover = p.createShape();
            cover.beginShape();
            cover.fill(219, 216, 206);
            cover.noStroke();
            if(p.degrees(pointerAngle)<0) {
                cover.vertex(-100, 100);
                cover.vertex(-100, -100);
                cover.vertex(0, -100);
                cover.vertex(p.cos(pointerAngle)*100, p.sin(pointerAngle) * 100);
                cover.vertex(0, 0);
                cover.vertex(100,0);
                cover.vertex(100, 100);
            } else {
                cover.vertex(-100, 100);
                cover.vertex(-100, -100);
                cover.vertex(0, -100);
                cover.vertex(100, 0);
                cover.vertex(0, 0);
                cover.vertex(p.cos(pointerAngle)*100, p.sin(pointerAngle) * 100);
                cover.vertex(0, 100);
            }
            cover.endShape(CLOSE);
            p.shape(cover);

            // working area
            p.strokeWeight(1);
            p.stroke(0,50);
            p.noFill();
            p.circle(0,0,560);
            p.circle(0,0,160);
            p.line(0,0,0,-280);
            p.beginShape();
            {
                p.fill(219, 216, 206);
                p.noStroke();
                p.vertex(-200, -300);
                p.vertex(0, -300);
                p.vertex(0, 0);
                p.vertex(-p.sin(p.radians(30))*300, 300);
                p.vertex(-200, 300);
            }
            p.endShape();

            // non-manipulative space
            p.beginShape();
            {
                p.fill(255, 17, 0,20);
                p.noStroke();
                p.vertex(0, 0);
                p.vertex(p.sin(p.radians(30))*300, 300);
                p.vertex(-p.sin(p.radians(30))*300, 300);
            }
            p.endShape();

            // coordinate system
            p.stroke(180);
            p.strokeWeight(2);
            p.line(-50,0,400,0);
            p.line(0,-150,0,150);

            p.stroke(180);



            // little lines
            p.stroke(180);
            for(int i = 1;i<18;i++){
                p.pushMatrix();
                p.rotate(p.radians(-90+i*10));
                p.line(35,0,50,0);
                p.popMatrix();
            }




        } p.popMatrix();
    }

    public void calculatePointer(){
        pointerAngle = p.atan2(pointerPos.y - BASE_POS.y,pointerPos.x - BASE_POS.x);
        pointerDist = BASE_POS.dist(pointerPos);
    }

    public void newSet(){
        // randomize pointer and create new population

        randomizePointer();

        // if it's first generation, just create new population
        if(generation.generationCount==0){
            generation.newPopulation();
        } else {
            // if there are agents that reached target, find one that traveled the least dist
//            if(generation.agentsAtTarget.size()>0) {
//                float shortestDist = generation.agentsAtTarget.get(0).distTraveled2;
//                for (Manipulator manipulator : generation.agentsAtTarget) {
//                    if (manipulator.distTraveled2 < shortestDist) {
//                        shortestDist = manipulator.distTraveled2;
//                        bestOfGeneration = manipulator;
//                    }
//                }
//            }

            if(generation.agentsAtTarget.size() == 0){
                generation.agentsAtTarget.add(bestOfGeneration);
            }

            // calculate shortest and longest traveled dist
            float shortestDist = Integer.MAX_VALUE, longestDist = 0;
            for(Manipulator manipulator:generation.agentsAtTarget){
                if(manipulator.distTraveled<shortestDist){
                    shortestDist = manipulator.distTraveled;
                }

                if(manipulator.distTraveled>longestDist){
                    longestDist = manipulator.distTraveled;
                }
            }

            // calculate shortest and longest traveled dist 2
            float shortestDist2 = Integer.MAX_VALUE, longestDist2 = 0;
            for(Manipulator manipulator:generation.agentsAtTarget){
                if(manipulator.distTraveled2<shortestDist2){
                    shortestDist2 = manipulator.distTraveled2;
                }

                if(manipulator.distTraveled2>longestDist2){
                    longestDist2 = manipulator.distTraveled2;
                }
            }

            //calculate general score
            for(Manipulator manipulator:generation.agentsAtTarget){
                manipulator.generalScore =
                        (
                                (((longestDist-manipulator.distTraveled)/(longestDist-shortestDist))*100)*1
                                        + (((longestDist2-manipulator.distTraveled2)/(longestDist2-shortestDist2))*100)*0
                        )/(1+0);
            }

            bestOfGeneration = generation.agentsAtTarget.get(0);

            for(Manipulator manipulator:generation.agentsAtTarget){
                if(manipulator.generalScore>bestOfGeneration.generalScore){
                    bestOfGeneration = manipulator;
                }
            }

            // clear arrays and generate new population based on the best agent
            generation.agents.clear();
            generation.agentsAtTarget.clear();
            infoManipulator = null;
            generation.newPopulation(bestOfGeneration);
        }
    }

    public void findTheBest(){
        // finds who is currently the closest based in it's score

        if(generation.agentsAtTarget.size()==0 && generation.agents.size()>0) {
            bestOfGeneration = generation.agents.get(0);
            for (Manipulator manipulator : generation.agents) {
                if (manipulator.score >= bestOfGeneration.score) {
                    bestOfGeneration = manipulator;
                    manipulator.isBest = true;
                    for(Manipulator manipulator1:generation.agents){
                        if(manipulator1.index!=bestOfGeneration.index){
                            manipulator1.isBest = false;
                        }
                    }
                }
            }
        }
    }

    public void randomizePointer(){
        // generate random pointer position until it matches certain conditions

        pointerPos.set(p.random(BASE_POS.x,BASE_POS.x+300),p.random(BASE_POS.y-300,BASE_POS.y+300));
        while(pointerPos.dist(BASE_POS)>280 || pointerPos.dist(BASE_POS)<80 || p.atan2(pointerPos.y-BASE_POS.y,pointerPos.x - BASE_POS.x)>p.radians(60)){
            pointerPos.set(p.random(BASE_POS.x,BASE_POS.x+300),p.random(BASE_POS.y-300,BASE_POS.y+300));
        }
    }

    public void spawnSawed(){
        // spawn agent from file and disable it's time limit and add it to the agents array
        Manipulator manipulatorFromFile = new Manipulator(p,BASE_POS,420, NeuralNetwork.readFromFile());
        manipulatorFromFile.timeLimitEnabled = false;
        generation.agents.add(manipulatorFromFile);
    }


    public void setUpButtons(){
        cp5.addButton("pauseButton")
                //.setSwitch(true)
                .setPosition(30,750)
                .setColorBackground(p.color(0, 0, 0))
                .setColorForeground(p.color(0,0,0))
                .setColorActive(p.color(126, 168, 8))
                .setSize(100,30)
                .setFont(p.createFont("Arial",20))
                .plugTo(this)
                //.hide()
                .setLabel("PAUSE");

        cp5.addButton("routeButton")
                //.setSwitch(true)
                .setPosition(140,750)
                .setColorBackground(p.color(0, 0, 0))
                .setColorForeground(p.color(0,0,0))
                .setColorActive(p.color(126, 168, 8))
                .setSize(100,30)
                .setFont(p.createFont("Arial",20))
                .plugTo(this)
                //.hide()
                .setLabel("ROUTE");

        cp5.addButton("autoSpawnButton")
                .setPosition(250,750)
                .setColorBackground(p.color(0, 0, 0))
                .setColorForeground(p.color(0,0,0))
                .setColorActive(p.color(126, 168, 8))
                .setSize(150,30)
                .setFont(p.createFont("Arial",20))
                .plugTo(this)
                //.hide()
                .setLabel("AUTO SPAWN");

        cp5.addButton("timeLimitButton")
                .setPosition(410,750)
                .setColorBackground(p.color(0, 0, 0))
                .setColorForeground(p.color(0,0,0))
                .setColorActive(p.color(126, 168, 8))
                .setSize(150,30)
                .setFont(p.createFont("Arial",20))
                .plugTo(this)
                //.hide()
                .setLabel("Time Limit");

        cp5.addButton("speedUpButton")
                .setPosition(570,750)
                .setColorBackground(p.color(0, 0, 0))
                .setColorForeground(p.color(0,0,0))
                .setColorActive(p.color(126, 168, 8))
                .setSize(150,30)
                .setFont(p.createFont("Arial",20))
                .plugTo(this)
                //.hide()
                .setLabel("Speed Up");

        cp5.addButton("saveBestButton")
                .setPosition(730,750)
                .setColorBackground(p.color(0, 0, 0))
                .setColorForeground(p.color(0,0,0))
                .setColorActive(p.color(126, 168, 8))
                .setSize(150,30)
                .setFont(p.createFont("Arial",20))
                .plugTo(this)
                //.hide()
                .setLabel("Save Best");

        cp5.addButton("loadSavedButton")
                .setPosition(890,750)
                .setColorBackground(p.color(0, 0, 0))
                .setColorForeground(p.color(0,0,0))
                .setColorActive(p.color(126, 168, 8))
                .setSize(150,30)
                .setFont(p.createFont("Arial",20))
                .plugTo(this)
                //.hide()
                .setLabel("Load Saved");

    }

    public void buttonsVisible(){
        cp5.get("pauseButton").show();
        cp5.get("routeButton").show();
        cp5.get("autoSpawnButton").show();
        cp5.get("timeLimitButton").show();
        cp5.get("speedUpButton").show();
        cp5.get("saveBestButton").show();
        cp5.get("loadSavedButton").show();
    }

    public void buttonsCaptions(){
        if(cp5.get("pauseButton").isMouseOver()){
            p.text("Click to pause simulation (Or press 'p' key)",30,730);
        }
        if(cp5.get("routeButton").isMouseOver()){
            p.text("Click to show traveled route",30,730);
        }

        if(cp5.get("autoSpawnButton").isMouseOver()){
            p.text("Click to enable/disable auto spawn (On by default)",30,730);
        }

        if(cp5.get("timeLimitButton").isMouseOver()){
            p.text("Click to enable/disable time limit (On by default)",30,730);
        }

        if(cp5.get("speedUpButton").isMouseOver()){
            p.text("Click to speed up simulation by *20x (Depends on actual amount of frames per second)",30,730);
        }

        if(cp5.get("saveBestButton").isMouseOver()){
            p.text("Click to save the best from current generation",30,730);
        }

        if(cp5.get("loadSavedButton").isMouseOver()){
            p.text("Click to load saved AI from file",30,730);
        }

    }

    public void pauseButton(){
        if(!pause){
            pause = true;
            //cp5.get("pauseButton").setLabel("Paused");
            cp5.get("pauseButton").setColorBackground(p.color(189, 74, 4));
            cp5.get("pauseButton").setColorForeground(p.color(189, 74, 4));
        } else {
            pause = false;
            cp5.get("pauseButton").setColorBackground(p.color(0,0, 0));
            cp5.get("pauseButton").setColorForeground(p.color(0,0,0));
            //cp5.get("pauseButton").setLabel("Pause");
        }

    }

    public void routeButton(){
        if(!showRoute){
            showRoute = true;
            //cp5.get("pauseButton").setLabel("Paused");
            cp5.get("routeButton").setColorBackground(p.color(189, 74, 4));
            cp5.get("routeButton").setColorForeground(p.color(189, 74, 4));
        } else {
            showRoute = false;
            cp5.get("routeButton").setColorBackground(p.color(0,0, 0));
            cp5.get("routeButton").setColorForeground(p.color(0,0,0));
            //cp5.get("pauseButton").setLabel("Pause");
        }

    }

    public void autoSpawnButton(){
        if(!autoSpawn){
            autoSpawn = true;
            //cp5.get("pauseButton").setLabel("Paused");
            cp5.get("autoSpawnButton").setColorBackground(p.color(0,0, 0));
            cp5.get("autoSpawnButton").setColorForeground(p.color(0,0,0));
        } else {
            autoSpawn = false;
            cp5.get("autoSpawnButton").setColorBackground(p.color(189, 74, 4));
            cp5.get("autoSpawnButton").setColorForeground(p.color(189, 74, 4));
            //cp5.get("pauseButton").setLabel("Pause");
        }

    }

    public void timeLimitButton(){
        if (generation.agents.size() > 0) {
            if (!generation.agents.get(0).timeLimitEnabled) {
                for (Manipulator manipulator : generation.agents) {
                    manipulator.timeLimitEnabled = true;
                }
                cp5.get("timeLimitButton").setColorBackground(p.color(0,0, 0));
                cp5.get("timeLimitButton").setColorForeground(p.color(0,0,0));
            } else {
                for (Manipulator manipulator : generation.agents) {
                    manipulator.timeLimitEnabled = false;
                }
                cp5.get("timeLimitButton").setColorBackground(p.color(189, 74, 4));
                cp5.get("timeLimitButton").setColorForeground(p.color(189, 74, 4));
            }
        }

    }

    public void speedUpButton(){
        if(cycles==20){
            cycles = 1;
            cp5.get("speedUpButton").setColorBackground(p.color(0,0, 0));
            cp5.get("speedUpButton").setColorForeground(p.color(0,0,0));

        } else {
            cycles = 20;
            cp5.get("speedUpButton").setColorBackground(p.color(189, 74, 4));
            cp5.get("speedUpButton").setColorForeground(p.color(189, 74, 4));
        }

    }

    public void saveBestButton(){
        bestOfGeneration.brain.writeToFile();// write to file the best enemy of generation
    }

    public void loadSavedButton(){
        spawnSawed();
    }

    public void mousePressed(){
        if(p.mouseButton == p.LEFT) {
            if (p.mouseX > 15 && p.mouseX < 715 && p.mouseY > 40 && p.mouseY < 740) { // if mouse is pressed in main window
                pointerPos.set(p.mouseX, p.mouseY);
            }
        } else if(p.mouseButton == p.RIGHT){
            for(Manipulator manipulator:generation.agents){
                if(p.mouseX <= manipulator.endPoint.x+15 &&
                        p.mouseX >= manipulator.endPoint.x-15 &&
                        p.mouseY <= manipulator.endPoint.y + 15 &&
                        p.mouseY >= manipulator.endPoint.y -15){
                    infoManipulator = manipulator;
                    manipulator.showInfo = true;
                    for(Manipulator manipulator1:generation.agents){
                        if(manipulator1.index!=infoManipulator.index){
                            manipulator1.showInfo=false;
                        }
                    }
                } else {
                    //infoManipulator = null;
                    //manipulator.showInfo = false;
                }
            }
        }
    }

    public void mouseDragged(){
        if(p.mouseButton==p.LEFT) {
            if (p.mouseX > 15 && p.mouseX < 715 && p.mouseY > 40 && p.mouseY < 740) { // if mouse is dragged in main window
                pointerPos.set(p.mouseX, p.mouseY);
            }
        }
    }

    public void keyTyped(char key) {
        if (key == 'p') {
            if (!pause) {
                pause = true;
                //cp5.get("pauseButton").setLabel("Paused");
                cp5.get("pauseButton").setColorBackground(p.color(189, 74, 4));
                cp5.get("pauseButton").setColorForeground(p.color(189, 74, 4));
            } else {
                pause = false;
                cp5.get("pauseButton").setColorBackground(p.color(0, 0, 0));
                cp5.get("pauseButton").setColorForeground(p.color(0, 0, 0));
                //cp5.get("pauseButton").setLabel("Pause");
            }
        }
    }

}
