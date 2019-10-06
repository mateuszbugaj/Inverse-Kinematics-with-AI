import basicneuralnetwork.NeuralNetwork;
import controlP5.CColor;
import controlP5.ControlP5;
import java.io.IOException;
import processing.core.PApplet;
import com.fazecast.jSerialComm.*;
import processing.core.PFont;
import processing.core.PShape;
import processing.core.PVector;
import processing.serial.Serial;
import arduino.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.TreeMap;

public class Main extends PApplet {

    public static void main(String[] args) {
        PApplet.main("Main", args);

    }
    public void settings(){
        size(1200,800);
    }

    //Arduino arduino = new Arduino("COM3", 115200);

    ControlP5 cp5Menu;
    ControlP5 cp5;
    int option = 0;
    Simulation simulation;
    Manual manual;
    //String buffer;

    public void setup(){
        //arduino.openConnection();
        Manipulator.p = this;
        rectMode(CENTER);
        cp5 = new ControlP5(this);
        cp5.hide();
        simulation = new Simulation(this);
        manual = new Manual(this);

        cp5Menu = new ControlP5(this);
        setUpMenuButtons();
    }

    public void draw(){

        background(180);

        switch (option){
            case 0:
                menu();
                break;

            case 1:
                cp5Menu.hide();
                simulation.cp5.show();
                // draw windows and calculate, "cycles" times per frame
                simulation.drawFunction();
                break;
            case 2:
                cp5Menu.hide();
                manual.cp5.show();
                manual.drawFunction();
                //buffer = Float.toString((int)degrees(manual.manipulator.segment_1_rot)) + "_" + Float.toString((int)degrees(manual.manipulator.segment_2_rot));
                if(frameCount%1==0) {
                    //println(buffer);
                    //arduino.serialWrite(buffer);
                }
                break;
        }
        frameRate(120);
    }

    public void menu(){
        // pause before running simulation
        simulation.pause = true;
        simulation.cp5.get("pauseButton").setColorBackground(color(189, 74, 4));
        simulation.cp5.get("pauseButton").setColorForeground(color(189, 74, 4));

        for(int i = 0;i<SerialPort.getCommPorts().length;i++){
            text(SerialPort.getCommPorts()[i].toString(),50,20+20*i);
        }
    }

    public void setUpMenuButtons(){
        cp5Menu.addButton("StartGA")
                .setPosition(width/2-150,300)
                .setColorBackground(color(0, 0, 0))
                .setColorForeground(color(0,0,0))
                .setColorActive(color(126, 168, 8))
                .setSize(300,30)
                .setFont(createFont("Arial",20))
                .setLabel("Start genetic algorithm");

        cp5Menu.addButton("ManualControl")
                .setPosition(width/2-150,360)
                .setColorBackground(color(0, 0, 0))
                .setColorForeground(color(0,0,0))
                .setColorActive(color(126, 168, 8))
                .setSize(300,30)
                .setFont(createFont("Arial",20))
                .setLabel("Manual control");
    }

        public void StartGA(){
        option = 1;
    }

    public void ManualControl(){
        option = 2;
    }

    public void mousePressed(){
        switch (option) {
            case 1:simulation.mousePressed(); break;
            case 2:manual.mousePressed(); break;
        }
    }

    public void mouseDragged(){
        switch (option) {
            case 1:simulation.mouseDragged(); break;
            case 2:manual.mouseDragged(); break;
        }
    }

    public void keyPressed(){
        switch (option) {
            case 1:simulation.keyTyped(key); break;
            case 2:manual.keyTyped(key); break;
        }
    }


}

//import basicneuralnetwork.NeuralNetwork;
//import controlP5.CColor;
//import controlP5.ControlP5;
//import processing.core.PApplet;
//import processing.core.PFont;
//import processing.core.PShape;
//import processing.core.PVector;
//
//public class Main extends PApplet {
//    ControlP5 cp5;
//    ControlP5 cp5Menu;
//    int option = 0;
//
//    Manipulator mainManipulator; // manipulator that uses inverse kinematics
//    Generation generation; // generation creates and contains populations
//    Manipulator bestOfGeneration; // agent that performed the best in current generation
//    Manipulator infoManipulator; // agent which info to display
//    final PVector BASE_POS = new PVector(180,390); // position of manipulator's base
//    PVector pointerPos = new PVector();  // position of the target
//    float pointerAngle = 0; // angle
//    float pointerDist = 0; // and dist from the base
//
//    boolean pause = false; // pause simulation
//    boolean showRoute = false; // show all routes that agents traveled
//    boolean autoSpawn = true; // spawn after all agents are dead
//    int cycles = 1; // cycles per frame
//
//    public static void main(String[] args){
//        PApplet.main("Main", args);
//    }
//    public void settings(){
//        size(1200,800);
//    }
//
//    public void setup(){
//        cp5 = new ControlP5(this);
//        cp5.hide();
//        cp5Menu = new ControlP5(this);
//        setUpButtons();
//        setUpMenuButtons();
//
//        rectMode(CENTER);
//        mainManipulator = new Manipulator(this,BASE_POS,0);
//        generation = new Generation(this,BASE_POS,50); // new generation creating population
//        newSet();
//    }
//
//    public void draw(){
//
//        switch (option){
//            case 0:
//                menu();
//                break;
//
//            case 1:
//                // draw windows and calculate, "cycles" times per frame
//                for(int i = 0;i<cycles;i++) {
//                    background(224, 193, 108);
//                    cp5Menu.hide();
//                    cp5.show();
//                    //buttonsVisible();
//                    calculatePointer();
//                    drawMainWindow();
//                    drawInfoWindow();
//                    buttonsCaptions();
//                }
//                break;
//        }
//
//
//    }
//
//    public void menu(){
//        // pause before running simulation
//        pause = true;
//        cp5.get("pauseButton").setColorBackground(color(189, 74, 4));
//        cp5.get("pauseButton").setColorForeground(color(189, 74, 4));
//    }
//
//    public void drawMainWindow(){
//        // main simulation, drawing agents, showing coordinate system
//        // finding best and initiating creations of new populations
//
//        pushMatrix();
//        translate(0,0);// translate whole window if necessary
//
//        // window stuff
//        // show name and draw
//        stroke(0);
//        strokeWeight(2);
//        textSize(30);
//        fill(0);
//        text("Work space",15,35);
//        rectMode(CORNER);
//        fill(219, 216, 206);
//        rect(-285,40,1000,700);
//        rectMode(CENTER);
//
//        // show "pause" text
//        if(pause) {
//            fill(0);
//            textSize(20);
//            text("PAUSE", 20, 60);
//            textSize(12);
//        }
//
//        // draw coordinates lines
//        drawCoordinateSystem();
//
//        // main mainManipulator
////        mainManipulator.setTarget(pointerPos);
////        mainManipulator.inverseKinematics();
////        mainManipulator.showManipulator(false);
//
//        // agents
//        // maintain agents
//        if(generation.agents.size()>0){ // if there are agents
//            for(Manipulator manipulator:generation.agents){ // for every agent
//                manipulator.setTarget(pointerPos); // update target
//                manipulator.route(showRoute); // calculate route, display if enabled
//                if(!pause)manipulator.update(); // update function if not paused
//                manipulator.showManipulator(true); // show manipulator (only endPoint)
//
//                // if dist to the target is lower than 10, mark agent as dead and add it to the "agents that reached target" array
//                if(manipulator.targetDistFromAgent<10){
//                    manipulator.isDead = true;
//                    generation.agentsAtTarget.add(manipulator);
//                }
//            }
//        } else if(autoSpawn){ // if there are no more agents, generate new population
//            newSet();
////            if(generation.generationCount>500){
////                pause = true;
////            }
//        }
//
//        findTheBest(); // find best from agents
//        generation.agents.removeIf(i -> i.isDead); // delete those that are marked as dead
//
//        // pointer
//        // show pointer and line to the pointer
//        stroke(2);
//        stroke(133, 168, 13,100);
//        line(BASE_POS.x,BASE_POS.y,cos(pointerAngle)*pointerDist+BASE_POS.x,sin(pointerAngle)*pointerDist+BASE_POS.y);
//        fill(207, 95, 43);
//        noStroke();
//        ellipse(pointerPos.x,pointerPos.y,15,15);
//
//        popMatrix();
//    }
//
//    public void drawInfoWindow(){
//        // general and individual information about simulation and agents
//
//        pushMatrix();
//        translate(0,0); // translate whole window if necessary
//
//        // display name and show
//        stroke(0);
//        strokeWeight(2);
//        textSize(30);
//        fill(0);
//        text("Info",735,35);
//        rectMode(CORNER);
//        fill(219, 216, 206);
//        rect(730,40,550,700);
//        rectMode(CENTER);
//
//        fill(0);
//        pushMatrix();{
//            translate(740,80);
//            textSize(15);
//            text("General",0,-20);
//            line(-3,-15,150,-15);
//            text("Pointer angle: " + (int) degrees(pointerAngle),0,0);
//            text("Pointer distance: " +(int) pointerDist,0,20);
//            text("Generation: " +generation.generationCount,0,40);
//            text("Agents that reached the target: " +generation.agentsAtTarget.size(),0,60);
//            if(generation.agents.size()>0)
//            text("Seconds passed for this gen.: " + (int)generation.agents.get(0).timeLived/60,0,80);
//
//            text("Individual",0,160);
//            line(-3,165,150,165);
//
//            if(generation.agents.size()>0 && infoManipulator!=null) {
//                pushMatrix();
//                {
//                    translate(0, 180);
//                    text("Id: " + infoManipulator.index, 0, 0);
//                    text("Inputs: ", 0, 20);
//                    text("1) RGB position [0]: " + (int)infoManipulator.inputs[0], 20, 40);
//                    text("2) RGB position [1]: " + (int) infoManipulator.inputs[1], 20, 60);
//                    text("3) RGB position [2]: " + (int)infoManipulator.inputs[2], 20, 80);
//                    text("4) Dist from endPoint to target: " + (int)infoManipulator.inputs[3], 20, 100);
//                    text("5) Rotation segment 1: " + degrees((float)infoManipulator.inputs[4]), 20, 120);
//                    text("6) Rotation segment 2: " + degrees((float)infoManipulator.inputs[5]), 20, 140);
//                    text("7) Dist of target from base: " + (int)infoManipulator.inputs[6], 20, 160);
//                    text("8) Angle of target from base: " + degrees((float)infoManipulator.inputs[7]), 20, 180);
//                    text("9) Angle to max. rot. 1: " + degrees((float)infoManipulator.inputs[8]), 20, 200);
//                    text("10) Angle to max. rot. 2: " + degrees((float)infoManipulator.inputs[9]), 20, 220);
//                    text("Outputs[0]: " + (float) infoManipulator.outputs[0], 0, 240);
//                    text("Outputs[1]: " + (float) infoManipulator.outputs[1], 0, 260);
//                    text("Turning acc seg_1: " + infoManipulator.turningAccSeg_1, 0, 280);
//                    text("Turning acc seg_2: " +  infoManipulator.turningAccSeg_2, 0, 300);
//                    text("Turning speed seg_1: " + infoManipulator.turningSpeedSeg_1, 0, 320);
//                    text("Turning speed seg_2: " +  infoManipulator.turningSpeedSeg_2, 0, 340);
//                    text("Rotation 1: " + degrees(infoManipulator.segment_1_rot), 0, 360);
//                    text("Rotation 2: " +  degrees(infoManipulator.segment_2_rot_INTERNAL), 0, 380);
//                    text("Dist traveled: " +  infoManipulator.distTraveled, 0, 400);
//                    text("Dist traveled2: " +  infoManipulator.distTraveled2, 0, 420);
//                    //text("Is marked as best: " +  infoManipulator.isBest, 0, 420);
//                    text("Dist from endPoint to target: " +  infoManipulator.targetDistFromAgent, 0, 440);
//                    text("Angle from endPoint to target: " +  degrees(infoManipulator.targetAngleFromAgent), 0, 460);
//                }
//                popMatrix();
//            }
//
//        }popMatrix();
//
//        popMatrix();
//
//    }
//
//    public void drawCoordinateSystem(){
//        // draw coordinates lines
//
//        pushMatrix();{
//            translate(BASE_POS.x,BASE_POS.y);
//
//
//            // angle to the pointer
//            noStroke();
//            fill(133, 168, 13);
//            ellipse(0,0,95,95);
//            fill(219, 216, 206);
//            ellipse(0,0,75,75);
//
//            // shape to cover
//            PShape cover = createShape();
//            cover.beginShape();
//            cover.fill(219, 216, 206);
//            cover.noStroke();
//            if(degrees(pointerAngle)<0) {
//                cover.vertex(-100, 100);
//                cover.vertex(-100, -100);
//                cover.vertex(0, -100);
//                cover.vertex(cos(pointerAngle)*100, sin(pointerAngle) * 100);
//                cover.vertex(0, 0);
//                cover.vertex(100,0);
//                cover.vertex(100, 100);
//            } else {
//                cover.vertex(-100, 100);
//                cover.vertex(-100, -100);
//                cover.vertex(0, -100);
//                cover.vertex(100, 0);
//                cover.vertex(0, 0);
//                cover.vertex(cos(pointerAngle)*100, sin(pointerAngle) * 100);
//                cover.vertex(0, 100);
//            }
//            cover.endShape(CLOSE);
//            shape(cover);
//
//            // working area
//            strokeWeight(1);
//            stroke(0,50);
//            noFill();
//            circle(0,0,560);
//            circle(0,0,160);
//            line(0,0,0,-280);
//            beginShape();
//            {
//                fill(219, 216, 206);
//                noStroke();
//                vertex(-200, -300);
//                vertex(0, -300);
//                vertex(0, 0);
//                vertex(-sin(radians(30))*300, 300);
//                vertex(-200, 300);
//            }
//            endShape();
//
//            // non-manipulative space
//            beginShape();
//            {
//                fill(255, 17, 0,20);
//                noStroke();
//                vertex(0, 0);
//                vertex(sin(radians(30))*300, 300);
//                vertex(-sin(radians(30))*300, 300);
//            }
//            endShape();
//
//            // coordinate system
//            stroke(180);
//            strokeWeight(2);
//            line(-50,0,400,0);
//            line(0,-150,0,150);
//
//            stroke(180);
//
//
//
//            // little lines
//            stroke(180);
//            for(int i = 1;i<18;i++){
//                pushMatrix();
//                rotate(radians(-90+i*10));
//                line(35,0,50,0);
//                popMatrix();
//            }
//
//
//
//
//        } popMatrix();
//    }
//
//    public void calculatePointer(){
//        pointerAngle = atan2(pointerPos.y - BASE_POS.y,pointerPos.x - BASE_POS.x);
//        pointerDist = BASE_POS.dist(pointerPos);
//    }
//
//    public void newSet(){
//        // randomize pointer and create new population
//
//        randomizePointer();
//
//        // if it's first generation, just create new population
//        if(generation.generationCount==0){
//            generation.newPopulation();
//        } else {
//            // if there are agents that reached target, find one that traveled the least dist
////            if(generation.agentsAtTarget.size()>0) {
////                float shortestDist = generation.agentsAtTarget.get(0).distTraveled2;
////                for (Manipulator manipulator : generation.agentsAtTarget) {
////                    if (manipulator.distTraveled2 < shortestDist) {
////                        shortestDist = manipulator.distTraveled2;
////                        bestOfGeneration = manipulator;
////                    }
////                }
////            }
//
//            if(generation.agentsAtTarget.size() == 0){
//                generation.agentsAtTarget.add(bestOfGeneration);
//            }
//
//            // calculate shortest and longest traveled dist
//            float shortestDist = Integer.MAX_VALUE, longestDist = 0;
//            for(Manipulator manipulator:generation.agentsAtTarget){
//                if(manipulator.distTraveled<shortestDist){
//                    shortestDist = manipulator.distTraveled;
//                }
//
//                if(manipulator.distTraveled>longestDist){
//                    longestDist = manipulator.distTraveled;
//                }
//            }
//
//            // calculate shortest and longest traveled dist 2
//            float shortestDist2 = Integer.MAX_VALUE, longestDist2 = 0;
//            for(Manipulator manipulator:generation.agentsAtTarget){
//                if(manipulator.distTraveled2<shortestDist2){
//                    shortestDist2 = manipulator.distTraveled2;
//                }
//
//                if(manipulator.distTraveled2>longestDist2){
//                    longestDist2 = manipulator.distTraveled2;
//                }
//            }
//
//            //calculate general score
//            for(Manipulator manipulator:generation.agentsAtTarget){
//                manipulator.generalScore =
//                        (
//                        (((longestDist-manipulator.distTraveled)/(longestDist-shortestDist))*100)*1
//                        + (((longestDist2-manipulator.distTraveled2)/(longestDist2-shortestDist2))*100)*0
//                        )/(1+0);
//            }
//
//            bestOfGeneration = generation.agentsAtTarget.get(0);
//
//            for(Manipulator manipulator:generation.agentsAtTarget){
//                if(manipulator.generalScore>bestOfGeneration.generalScore){
//                    bestOfGeneration = manipulator;
//                }
//            }
//
//            // clear arrays and generate new population based on the best agent
//            generation.agents.clear();
//            generation.agentsAtTarget.clear();
//            infoManipulator = null;
//            generation.newPopulation(bestOfGeneration);
//        }
//    }
//
//    public void findTheBest(){
//        // finds who is currently the closest based in it's score
//
//        if(generation.agentsAtTarget.size()==0 && generation.agents.size()>0) {
//            bestOfGeneration = generation.agents.get(0);
//            for (Manipulator manipulator : generation.agents) {
//                if (manipulator.score >= bestOfGeneration.score) {
//                    bestOfGeneration = manipulator;
//                    manipulator.isBest = true;
//                    for(Manipulator manipulator1:generation.agents){
//                        if(manipulator1.index!=bestOfGeneration.index){
//                            manipulator1.isBest = false;
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    public void randomizePointer(){
//        // generate random pointer position until it matches certain conditions
//
//        pointerPos.set(random(BASE_POS.x,BASE_POS.x+300),random(BASE_POS.y-300,BASE_POS.y+300));
//        while(pointerPos.dist(BASE_POS)>280 || pointerPos.dist(BASE_POS)<80 || atan2(pointerPos.y-BASE_POS.y,pointerPos.x - BASE_POS.x)>radians(60)){
//            pointerPos.set(random(BASE_POS.x,BASE_POS.x+300),random(BASE_POS.y-300,BASE_POS.y+300));
//        }
//    }
//
//    public void spawnSawed(){
//        // spawn agent from file and disable it's time limit and add it to the agents array
//        Manipulator manipulatorFromFile = new Manipulator(this,BASE_POS,420, NeuralNetwork.readFromFile());
//        manipulatorFromFile.timeLimitEnabled = false;
//        generation.agents.add(manipulatorFromFile);
//    }
//
//    public void mouseDragged(){
//        if(mouseButton==LEFT) {
//            if (mouseX > 15 && mouseX < 715 && mouseY > 40 && mouseY < 740) { // if mouse is dragged in main window
//                pointerPos.set(mouseX, mouseY);
//            }
//        }
//    }
//
//    public void mousePressed(){
//        if(mouseButton == LEFT && option==1) {
//            if (mouseX > 15 && mouseX < 715 && mouseY > 40 && mouseY < 740) { // if mouse is pressed in main window
//                pointerPos.set(mouseX, mouseY);
//            }
//        } else if(mouseButton == RIGHT){
//            for(Manipulator manipulator:generation.agents){
//                if(mouseX <= manipulator.endPoint.x+15 &&
//                        mouseX >= manipulator.endPoint.x-15 &&
//                        mouseY <= manipulator.endPoint.y + 15 &&
//                        mouseY >= manipulator.endPoint.y -15){
//                    infoManipulator = manipulator;
//                    manipulator.showInfo = true;
//                    for(Manipulator manipulator1:generation.agents){
//                        if(manipulator1.index!=infoManipulator.index){
//                            manipulator1.showInfo=false;
//                        }
//                    }
//                } else {
//                    //infoManipulator = null;
//                    //manipulator.showInfo = false;
//                }
//            }
//        }
//    }
//
//    public void keyTyped(){
//        if(key == 'g'){
//            newSet();
//        }
//
//        if(key == 'p'){
//            if(!pause){
//                pause = true;
//                //cp5.get("pauseButton").setLabel("Paused");
//                cp5.get("pauseButton").setColorBackground(color(189, 74, 4));
//                cp5.get("pauseButton").setColorForeground(color(189, 74, 4));
//            } else {
//                pause = false;
//                cp5.get("pauseButton").setColorBackground(color(0,0, 0));
//                cp5.get("pauseButton").setColorForeground(color(0,0,0));
//                //cp5.get("pauseButton").setLabel("Pause");
//            }
//        }
//
////        if(key == 'r'){
////            if(!showRoute){
////                showRoute = true;
////            } else {
////                showRoute = false;
////            }
////        }
////
////        if(key == 't'){
////            if(!autoSpawn){
////                autoSpawn = true;
////            } else {
////                autoSpawn = false;
////            }
////        }
////
////        if(key == 'i') {
////            if (generation.agents.size() > 0) {
////                if (!generation.agents.get(0).timeLimitEnabled) {
////                    for (Manipulator manipulator : generation.agents) {
////                        manipulator.timeLimitEnabled = true;
////                    }
////                } else {
////                    for (Manipulator manipulator : generation.agents) {
////                        manipulator.timeLimitEnabled = false;
////                    }
////                }
////            }
////        }
////
////        if(key == 'o'){
////            if(cycles==20){
////                cycles = 1;
////            } else {
////                cycles = 20;
////            }
////        }
////
////        if(key == 'm'){
////            bestOfGeneration.brain.writeToFile();// write to file the best enemy of generation
////        }
////
////        if(key == 'y'){
////            spawnSawed();
////        }
//    }
//
//    public void setUpMenuButtons(){
//        cp5Menu.addButton("StartGA")
//                .setPosition(width/2-150,300)
//                .setColorBackground(color(0, 0, 0))
//                .setColorForeground(color(0,0,0))
//                .setColorActive(color(126, 168, 8))
//                .setSize(300,30)
//                .setFont(createFont("Arial",20))
//                .setLabel("Start genetic algorithm");
//    }
//
//    public void StartGA(){
//        option = 1;
//    }
//
//    public void setUpButtons(){
//        cp5.addButton("pauseButton")
//                //.setSwitch(true)
//                .setPosition(30,750)
//                .setColorBackground(color(0, 0, 0))
//                .setColorForeground(color(0,0,0))
//                .setColorActive(color(126, 168, 8))
//                .setSize(100,30)
//                .setFont(createFont("Arial",20))
//                //.hide()
//                .setLabel("PAUSE");
//
//        cp5.addButton("routeButton")
//                //.setSwitch(true)
//                .setPosition(140,750)
//                .setColorBackground(color(0, 0, 0))
//                .setColorForeground(color(0,0,0))
//                .setColorActive(color(126, 168, 8))
//                .setSize(100,30)
//                .setFont(createFont("Arial",20))
//                //.hide()
//                .setLabel("ROUTE");
//
//        cp5.addButton("autoSpawnButton")
//                .setPosition(250,750)
//                .setColorBackground(color(0, 0, 0))
//                .setColorForeground(color(0,0,0))
//                .setColorActive(color(126, 168, 8))
//                .setSize(150,30)
//                .setFont(createFont("Arial",20))
//                //.hide()
//                .setLabel("AUTO SPAWN");
//
//        cp5.addButton("timeLimitButton")
//                .setPosition(410,750)
//                .setColorBackground(color(0, 0, 0))
//                .setColorForeground(color(0,0,0))
//                .setColorActive(color(126, 168, 8))
//                .setSize(150,30)
//                .setFont(createFont("Arial",20))
//                //.hide()
//                .setLabel("Time Limit");
//
//        cp5.addButton("speedUpButton")
//                .setPosition(570,750)
//                .setColorBackground(color(0, 0, 0))
//                .setColorForeground(color(0,0,0))
//                .setColorActive(color(126, 168, 8))
//                .setSize(150,30)
//                .setFont(createFont("Arial",20))
//                //.hide()
//                .setLabel("Speed Up");
//
//        cp5.addButton("saveBestButton")
//                .setPosition(730,750)
//                .setColorBackground(color(0, 0, 0))
//                .setColorForeground(color(0,0,0))
//                .setColorActive(color(126, 168, 8))
//                .setSize(150,30)
//                .setFont(createFont("Arial",20))
//                //.hide()
//                .setLabel("Save Best");
//
//        cp5.addButton("loadSavedButton")
//                .setPosition(890,750)
//                .setColorBackground(color(0, 0, 0))
//                .setColorForeground(color(0,0,0))
//                .setColorActive(color(126, 168, 8))
//                .setSize(150,30)
//                .setFont(createFont("Arial",20))
//                //.hide()
//                .setLabel("Load Saved");
//
//    }
//
//    public void buttonsVisible(){
//        cp5.get("pauseButton").show();
//        cp5.get("routeButton").show();
//        cp5.get("autoSpawnButton").show();
//        cp5.get("timeLimitButton").show();
//        cp5.get("speedUpButton").show();
//        cp5.get("saveBestButton").show();
//        cp5.get("loadSavedButton").show();
//    }
//
//    public void buttonsCaptions(){
//        if(cp5.get("pauseButton").isMouseOver()){
//            text("Click to pause simulation (Or press 'p' key)",30,730);
//        }
//        if(cp5.get("routeButton").isMouseOver()){
//            text("Click to show traveled route",30,730);
//        }
//
//        if(cp5.get("autoSpawnButton").isMouseOver()){
//            text("Click to enable/disable auto spawn (On by default)",30,730);
//        }
//
//        if(cp5.get("timeLimitButton").isMouseOver()){
//            text("Click to enable/disable time limit (On by default)",30,730);
//        }
//
//        if(cp5.get("speedUpButton").isMouseOver()){
//            text("Click to speed up simulation by *20x (Depends on actual amount of frames per second)",30,730);
//        }
//
//        if(cp5.get("saveBestButton").isMouseOver()){
//            text("Click to save the best from current generation",30,730);
//        }
//
//        if(cp5.get("loadSavedButton").isMouseOver()){
//            text("Click to load saved AI from file",30,730);
//        }
//
//    }
//
//    public void pauseButton(){
//        if(!pause){
//            pause = true;
//            //cp5.get("pauseButton").setLabel("Paused");
//            cp5.get("pauseButton").setColorBackground(color(189, 74, 4));
//            cp5.get("pauseButton").setColorForeground(color(189, 74, 4));
//        } else {
//            pause = false;
//            cp5.get("pauseButton").setColorBackground(color(0,0, 0));
//            cp5.get("pauseButton").setColorForeground(color(0,0,0));
//            //cp5.get("pauseButton").setLabel("Pause");
//        }
//
//    }
//
//    public void routeButton(){
//        if(!showRoute){
//            showRoute = true;
//            //cp5.get("pauseButton").setLabel("Paused");
//            cp5.get("routeButton").setColorBackground(color(189, 74, 4));
//            cp5.get("routeButton").setColorForeground(color(189, 74, 4));
//        } else {
//            showRoute = false;
//            cp5.get("routeButton").setColorBackground(color(0,0, 0));
//            cp5.get("routeButton").setColorForeground(color(0,0,0));
//            //cp5.get("pauseButton").setLabel("Pause");
//        }
//
//    }
//
//    public void autoSpawnButton(){
//        if(!autoSpawn){
//            autoSpawn = true;
//            //cp5.get("pauseButton").setLabel("Paused");
//            cp5.get("autoSpawnButton").setColorBackground(color(0,0, 0));
//            cp5.get("autoSpawnButton").setColorForeground(color(0,0,0));
//        } else {
//            autoSpawn = false;
//            cp5.get("autoSpawnButton").setColorBackground(color(189, 74, 4));
//            cp5.get("autoSpawnButton").setColorForeground(color(189, 74, 4));
//            //cp5.get("pauseButton").setLabel("Pause");
//        }
//
//    }
//
//    public void timeLimitButton(){
//        if (generation.agents.size() > 0) {
//            if (!generation.agents.get(0).timeLimitEnabled) {
//                for (Manipulator manipulator : generation.agents) {
//                    manipulator.timeLimitEnabled = true;
//                }
//                cp5.get("timeLimitButton").setColorBackground(color(0,0, 0));
//                cp5.get("timeLimitButton").setColorForeground(color(0,0,0));
//            } else {
//                for (Manipulator manipulator : generation.agents) {
//                    manipulator.timeLimitEnabled = false;
//                }
//                cp5.get("timeLimitButton").setColorBackground(color(189, 74, 4));
//                cp5.get("timeLimitButton").setColorForeground(color(189, 74, 4));
//            }
//        }
//
//    }
//
//    public void speedUpButton(){
//        if(cycles==20){
//            cycles = 1;
//            cp5.get("speedUpButton").setColorBackground(color(0,0, 0));
//            cp5.get("speedUpButton").setColorForeground(color(0,0,0));
//
//        } else {
//            cycles = 20;
//            cp5.get("speedUpButton").setColorBackground(color(189, 74, 4));
//            cp5.get("speedUpButton").setColorForeground(color(189, 74, 4));
//        }
//
//    }
//
//    public void saveBestButton(){
//        bestOfGeneration.brain.writeToFile();// write to file the best enemy of generation
//    }
//
//    public void loadSavedButton(){
//        spawnSawed();
//    }
//
//
//
//}
