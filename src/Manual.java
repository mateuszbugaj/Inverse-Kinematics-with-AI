import arduino.Arduino;
import basicneuralnetwork.NeuralNetwork;
import controlP5.ControlP5;
import processing.core.PApplet;
import processing.core.PShape;
import processing.core.PVector;

import java.util.ArrayList;

import static processing.core.PConstants.CENTER;
import static processing.core.PConstants.CLOSE;
import static processing.core.PConstants.CORNER;

public class Manual {
    Arduino arduino = new Arduino("COM5", 115200);
    String buffer;
    Thread thread;

    PApplet p;
    ControlP5 cp5;
    Manipulator manipulator;

    PVector pointerPos = new PVector();  // position of the target
    float pointerAngle = 0; // angle
    float pointerDist = 0; // and dist from the base
    final PVector BASE_POS = new PVector(180,390); // position of manipulator's base
    boolean pause = false; // pause simulation
    boolean showRoute = false;
    ArrayList<PVector> routeAsked = new ArrayList<>();


    public Manual(PApplet p) {
        this.p = p;
        this.cp5 = new ControlP5(p);

        arduino.openConnection();

        cp5 = new ControlP5(p);
        setUpButtons();
        cp5.hide();

        manipulator = new Manipulator(p, BASE_POS, 0, NeuralNetwork.readFromFile());
        manipulator.segment_1_rot = 0;
        manipulator.segment_2_rot = 0;
        manipulator.showInfo = false;
        manipulator.maxSpeed = p.radians(0.1f);
        randomizePointer();

        // create and run new thread where arduino communication takes place
        thread = new Thread(){
          @Override
          public void run(){

              while(true) {
                  buffer = String.format("%.2f",p.degrees(manipulator.segment_1_rot)) + "_" + String.format("%.2f",p.degrees(manipulator.segment_2_rot));
                  buffer = buffer.replace(",","."); // have to replace because I WANT MY DOT JAVA WHY
                  p.println(buffer);
                  arduino.serialWrite(buffer);
              }
          }
        };
        thread.start();

    }

    public void drawFunction(){
        p.background(224, 193, 108);
        drawMainWindow();
        drawInfoWindow();
        buttonsCaptions();

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
        showRouteAsked();
        calculatePointer();

        // main mainManipulator
        manipulator.showManipulator(false);
        manipulator.route(true);

        if(routeAsked.size()>0) {
            if(!pause) manipulator.moveManally(routeAsked.get(0).copy());

            if (manipulator.segment_2_end.dist(routeAsked.get(0)) < 1) {
                routeAsked.remove(0);
            }

            // if you want to make it work with nn:

//            p.circle(routeAsked.get(0).x,routeAsked.get(0).y,15);
//            manipulator.setTarget(routeAsked.get(0));
//            manipulator.route(true);
//            if (!pause) manipulator.update();
//            manipulator.showManipulator(true);
//
//            if (manipulator.targetDistFromAgent < 10) {
//                routeAsked.remove(0);
//            }
        }


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

    public void showRouteAsked(){
        p.stroke(217, 54, 39);
        for(int i =0;i<routeAsked.size();i++){
            if(i<routeAsked.size()-1){
                p.line(routeAsked.get(i).x, routeAsked.get(i).y,routeAsked.get(i+1).x,routeAsked.get(i+1).y);
            }

        }
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

    public void setUpButtons() {
        cp5.addButton("pauseButton")
                //.setSwitch(true)
                .setPosition(30, 750)
                .setColorBackground(p.color(0, 0, 0))
                .setColorForeground(p.color(0, 0, 0))
                .setColorActive(p.color(126, 168, 8))
                .setSize(100, 30)
                .setFont(p.createFont("Arial", 20))
                .plugTo(this)
                //.hide()
                .setLabel("PAUSE");

        cp5.addButton("routeButton")
                //.setSwitch(true)
                .setPosition(140, 750)
                .setColorBackground(p.color(0, 0, 0))
                .setColorForeground(p.color(0, 0, 0))
                .setColorActive(p.color(126, 168, 8))
                .setSize(100, 30)
                .setFont(p.createFont("Arial", 20))
                .plugTo(this)
                //.hide()
                .setLabel("ROUTE");
    }

    public void buttonsCaptions() {
        p.textSize(12);
        p.stroke(0);

        if (cp5.get("pauseButton").isMouseOver()) {
            p.text("Click to pause simulation (Or press 'p' key)", 30, 730);
        }
        if (cp5.get("routeButton").isMouseOver()) {
            p.text("Click to show traveled route", 30, 730);
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

    public void calculatePointer(){
        pointerAngle = p.atan2(pointerPos.y - BASE_POS.y,pointerPos.x - BASE_POS.x);
        pointerDist = BASE_POS.dist(pointerPos);
    }

    public void randomizePointer(){
        // generate random pointer position until it matches certain conditions

        pointerPos.set(p.random(BASE_POS.x,BASE_POS.x+300),p.random(BASE_POS.y-300,BASE_POS.y+300));
        while(pointerPos.dist(BASE_POS)>280 || pointerPos.dist(BASE_POS)<80 || p.atan2(pointerPos.y-BASE_POS.y,pointerPos.x - BASE_POS.x)>p.radians(60)){
            pointerPos.set(p.random(BASE_POS.x,BASE_POS.x+300),p.random(BASE_POS.y-300,BASE_POS.y+300));
        }
    }

    public void mousePressed(){
        if(p.mouseButton == p.LEFT) {
            if (p.mouseX > 15 && p.mouseX < 715 && p.mouseY > 40 && p.mouseY < 740) { // if mouse is pressed in main window
                pointerPos.set(p.mouseX, p.mouseY);

//                if(manipulator.timeLived%50==0) {
//                    routeAsked.add(pointerPos.copy());
//                }
            }
        }
    }

    public void mouseDragged(){
        if(p.mouseButton==p.LEFT) {
            if (p.mouseX > 15 && p.mouseX < 715 && p.mouseY > 40 && p.mouseY < 740) { // if mouse is dragged in main window
                pointerPos.set(p.mouseX, p.mouseY);

                //if(manipulator.timeLived%1==0) {
                    routeAsked.add(pointerPos.copy());
                //}
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
            p.text("Speed: " + manipulator.maxSpeed,0,0);
            p.text("Angle of rotation for segment_1: " + p.degrees(manipulator.segment_1_rot),0,20);
            p.text("Angle of rotation for segment_2: " + p.degrees(manipulator.segment_2_rot),0,40);


        }p.popMatrix();

        p.popMatrix();

    }
}
