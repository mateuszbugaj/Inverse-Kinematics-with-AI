import basicneuralnetwork.NeuralNetwork;
import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;

public class Manipulator {
    static PApplet p;
    int index;
    PVector basePos; // position of arm's base
    float segmentLength = 150;
    PVector targetPos;

    // segment 1 variables
    PVector segment_1_pos; // position of the segment 1
    float segment_1_rot = p.radians(110); // angle of rotation in radians of the segment 1
    PVector segment_1_end; // position of the segment's end

    // segment 2 variables
    PVector segment_2_pos; // position of the segment 2
    float segment_2_rot = p.radians(-90); // angle of rotation in radians of the segment 2
    PVector segment_2_end; // position of the segment's end

    float segment_2_rot_INTERNAL = p.radians(-90);
    float maxTurn_1[] = {p.radians(-90),p.radians(120)};
    float maxTurn_2[] = {p.radians(0),p.radians(-160)};

    PVector endPoint;
    float endPointAngle, endPointDist;

    NeuralNetwork brain;
    double[] inputs = new double[4];
    double[] outputs;
    float turningAccSeg_1;
    float turningAccSeg_2;
    float turningSpeedSeg_1;
    float turningSpeedSeg_2;
    float maxAcc = p.radians(10), maxSpeed = p.radians(15);
    float distTraveled; // total dist traveled
    ArrayList<PVector> route = new ArrayList<>(); // list of all points on road
    ArrayList<PVector[]> route2 = new ArrayList<>(); // boi
    float distTraveled2;
    float targetDist, targetAgnle; // target's dist and angle in cylindrical coordinate system
    float targetAngleFromAgent;
    float targetDistFromAgent; // dist from endPoint to target

    float[] RGBTargetPosition = new float[3];

    float timeLived = 0;
    boolean isBest = false;
    boolean isDead = false;
    float timeLimit = 60*20;
    boolean timeLimitEnabled = true;
    int[] color = new int[]{217, 164, 41};
    float score;
    float generalScore;
    float closestDist = 1000;
    boolean showInfo = false;

    PVector initialPos;
    Ray marchingRay;

    public Manipulator(PVector basePos, int index){
        this.basePos = basePos;
        this.index = index;

        targetPos = new PVector();
        segment_1_end = new PVector();
        segment_2_end = new PVector();
        segment_1_pos = new PVector();
        segment_2_pos = new PVector();
        endPoint = new PVector();
        initialPos = new PVector();

        //inverseKinematics();
        calculateSegmentsPositions();
        marchingRay = new Ray(p,endPoint,p.width);

        segment_1_pos = basePos;
        segment_2_pos = segment_1_end;

        brain = new NeuralNetwork(inputs.length,2,8,2);
    }

    public Manipulator(PApplet p, PVector basePos, int index){
        this.p = p;
        this.basePos = basePos;
        this.index = index;

        targetPos = new PVector();
        segment_1_end = new PVector();
        segment_2_end = new PVector();
        segment_1_pos = new PVector();
        segment_2_pos = new PVector();
        endPoint = new PVector();
        initialPos = new PVector();

        //inverseKinematics();
        calculateSegmentsPositions();
        marchingRay = new Ray(p,endPoint,p.width);

        segment_1_pos = basePos;
        segment_2_pos = segment_1_end;

        brain = new NeuralNetwork(inputs.length,2,8,2);
    }

    public Manipulator(PApplet p, PVector basePos,int index, NeuralNetwork brain){
        this.p = p;
        this.basePos = basePos;
        this.index = index;
        this.brain = brain;

        targetPos = new PVector();
        segment_1_end = new PVector();
        segment_2_end = new PVector();
        segment_1_pos = new PVector();
        segment_2_pos = new PVector();
        endPoint = new PVector();
        initialPos = new PVector();

        calculateSegmentsPositions();
        marchingRay = new Ray(p,endPoint,p.width);

        segment_1_pos = basePos;
        segment_2_pos = segment_1_end;

    }


    public void brainActivity(){
        inputs[0] = RGBTargetPosition[0];
        inputs[1] = RGBTargetPosition[1];
        inputs[2] = RGBTargetPosition[2];
        inputs[3] = targetDistFromAgent;
//        inputs[4] = segment_1_rot;
//        inputs[5] = segment_2_rot_INTERNAL;
//        inputs[6] = targetDist;
//        inputs[7] = targetAgnle;
//        inputs[8] = -maxTurn_1[0]+segment_1_rot;
//        inputs[9] = -maxTurn_2[1]+segment_2_rot_INTERNAL;
        outputs = brain.guess(inputs);
        turningAccSeg_1 = p.map((float)outputs[0], 0, 1, -maxAcc,maxAcc);
        turningAccSeg_2 = p.map((float)outputs[1], 0, 1, -maxAcc,maxAcc);

        // make sure that speed is in the right range
        if (turningSpeedSeg_1 < maxSpeed && turningSpeedSeg_1 > -maxSpeed) {
            turningSpeedSeg_1+=turningAccSeg_1;
        } else if( turningSpeedSeg_1>maxSpeed && turningAccSeg_1<0){
            turningSpeedSeg_1+=turningAccSeg_1;
        } else if(turningSpeedSeg_1<-maxSpeed && turningAccSeg_1>0){
            turningSpeedSeg_1+=turningAccSeg_1;
        }

        if (turningSpeedSeg_2 < maxSpeed && turningSpeedSeg_2 > -maxSpeed) {
            turningSpeedSeg_2+=turningAccSeg_2;
        } else if( turningSpeedSeg_2>maxSpeed && turningAccSeg_2<0){
            turningSpeedSeg_2+=turningAccSeg_2;
        } else if(turningSpeedSeg_2<-maxSpeed && turningAccSeg_2>0){
            turningSpeedSeg_2+=turningAccSeg_2;
        }

        // make sure that rotation is in the right range
        if (segment_1_rot < maxTurn_1[1] && segment_1_rot > maxTurn_1[0]) {
            segment_1_rot+= p.radians(turningSpeedSeg_1);
        } else if( segment_1_rot> maxTurn_1[1] && turningSpeedSeg_1<0){
            segment_1_rot+=p.radians(turningSpeedSeg_1);
        } else if(segment_1_rot<maxTurn_1[0] && turningSpeedSeg_1>0){
            segment_1_rot+=p.radians(turningSpeedSeg_1);
        }

        if (segment_2_rot_INTERNAL < maxTurn_2[1] && segment_2_rot_INTERNAL > maxTurn_2[0]) {
            segment_2_rot_INTERNAL+= p.radians(turningSpeedSeg_2);
        } else if( segment_2_rot_INTERNAL> maxTurn_2[1] && turningSpeedSeg_2<0){
            segment_2_rot_INTERNAL+=p.radians(turningSpeedSeg_2);
        } else if(segment_2_rot_INTERNAL<maxTurn_2[0] && turningSpeedSeg_2>0){
            segment_2_rot_INTERNAL+=p.radians(turningSpeedSeg_2);
        }


        segment_2_rot = segment_2_rot_INTERNAL + segment_1_rot;




        calculateSegmentsPositions();
    }

    public float[] inverseKinematics(PVector currentTarget){
        //calculate inverse kinematics i guess
//        targetPos.x-=segment_1_pos.x;
//        targetPos.y-=segment_1_pos.y;
//
//        float distance = p.sqrt(p.pow(targetPos.x,2)+p.pow(targetPos.y,2));
//        float c = p.min(distance, segmentLength*2);
//
//        segment_2_rot = p.atan2(targetPos.y,targetPos.x) + p.acos((p.pow(segmentLength,2) - p.pow(segmentLength,2) - p.pow(c,2))/(-2*segmentLength*c));
//        segment_1_rot = segment_2_rot + p.PI + p.acos((p.pow(c,2) - p.pow(segmentLength,2) - p.pow(segmentLength,2))/(-2*p.pow(segmentLength,2)));
//        segment_1_rot -= 2*p.PI;

        float segment_1_rot_calculated, segment_2_rot_calculated;

        currentTarget.x-=segment_1_pos.x;
        currentTarget.y-=segment_1_pos.y;

        float distance = p.sqrt(p.pow(currentTarget.x,2)+p.pow(currentTarget.y,2));
        float c = p.min(distance, segmentLength*2);

        segment_2_rot_calculated = p.atan2(currentTarget.y,currentTarget.x) + p.acos((p.pow(segmentLength,2) - p.pow(segmentLength,2) - p.pow(c,2))/(-2*segmentLength*c));
        segment_1_rot_calculated = segment_2_rot + p.PI + p.acos((p.pow(c,2) - p.pow(segmentLength,2) - p.pow(segmentLength,2))/(-2*p.pow(segmentLength,2)));
        segment_1_rot_calculated -= 2*p.PI;

        calculateSegmentsPositions();

        return new float[]{segment_1_rot_calculated,segment_2_rot_calculated};
    }

    public void calculateSegmentsPositions(){
        segment_1_end.x = p.cos(segment_1_rot)*segmentLength + basePos.x ;
        segment_1_end.y = p.sin(segment_1_rot)*segmentLength + basePos.y;

        segment_2_end.x = p.cos(segment_2_rot)*segmentLength + segment_1_end.x;
        segment_2_end.y = p.sin(segment_2_rot)*segmentLength + segment_1_end.y;

        endPoint = segment_2_end;
        endPointAngle = p.atan2(endPoint.y - segment_1_pos.y, endPoint.x - segment_1_pos.x);
        endPointDist = segment_1_pos.dist(endPoint);
        targetDistFromAgent = endPoint.dist(targetPos);

        if(timeLived<=1){
            initialPos = endPoint.copy();
        }
    }

    public void moveManally(PVector targetAsked){
        // calculation of rotations for the next point on route
        //float[] calculatedRotations = inverseKinematics(route.get(0));

//        // changing rotations from radians to angles for my small human brain
//        calculatedRotations[0] = p.degrees(calculatedRotations[0]);
//        calculatedRotations[1] = p.degrees(calculatedRotations[1]);

        targetPos = targetAsked;
        float[] rot_calculated = inverseKinematics(targetAsked);
        float segment_1_rot_temp = p.map(segment_1_rot,maxTurn_1[0],maxTurn_1[1],0,p.abs(maxTurn_1[0])+maxTurn_1[1]);
        float segment_2_rot_temp = p.map(segment_2_rot,maxTurn_2[0],maxTurn_2[1],0,p.abs(maxTurn_2[0])+maxTurn_2[1]);
        rot_calculated[0] = p.map(rot_calculated[0],maxTurn_1[0],maxTurn_1[1],0,p.abs(maxTurn_1[0])+maxTurn_1[1]);
        rot_calculated[1] = p.map(rot_calculated[1],maxTurn_2[0],maxTurn_2[1],0,p.abs(maxTurn_2[0])+maxTurn_2[1]);

        if(rot_calculated[0]-segment_1_rot_temp<0){
            segment_1_rot-=maxSpeed;
        } else if(rot_calculated[0]-segment_1_rot_temp>0){
            segment_1_rot+=maxSpeed;
        }

        if(rot_calculated[1]-segment_2_rot_temp<0){
            segment_2_rot-=maxSpeed;
        } else if(rot_calculated[1]-segment_2_rot_temp>0){
            segment_2_rot+=maxSpeed;
        }

        calculateSegmentsPositions();

        if(p.frameCount%10==0) {
            route.add(endPoint.copy());
        }
    }

    public void setTarget(PVector target){
        this.targetPos = new PVector(target.x ,target.y); // receive target and make it correct
    }

    public void update(){
        if(isBest){
            color = new int[]{222, 75, 64};
        } else{
            color = new int[]{217, 164, 41};
        }

        timeLived++;
        if(timeLived>timeLimit && timeLimitEnabled){
            isDead = true;
        }

        if(timeLived%10==0){
            route.add(endPoint.copy());
        }

        if(timeLived>=10) {
            route2.add(new PVector[]{endPoint.copy(), new PVector(p.cos(marchingRay.bestAngle) * marchingRay.smallestDistToRoad + endPoint.x, p.sin(marchingRay.bestAngle) * marchingRay.smallestDistToRoad + endPoint.y)});
        }

        if(targetDistFromAgent<closestDist){
            closestDist = targetDistFromAgent;
            score = (1/targetDistFromAgent)*1000;
        }

        //targetAngleFromAgent = p.atan2(targetPos.y - endPoint.y,targetPos.x - endPoint.x)-segment_2_rot;
        targetAngleFromAgent = p.atan2(targetPos.y - endPoint.y,targetPos.x - endPoint.x);
        targetDist = segment_1_pos.dist(targetPos);
        if(targetAngleFromAgent <0){
            targetAngleFromAgent +=p.PI*2;
        }
        targetAgnle = p.atan2(targetPos.y-segment_1_pos.y,targetPos.x - segment_1_pos.x);
        RGBTargetPosition = HtoRGB(targetAngleFromAgent);

        marchingRay.moveRay(new PVector[]{initialPos,targetPos});

        brainActivity();

        //segment_2_rot_INTERNAL += p.radians(0.1f);
        //segment_2_rot_INTERNAL = -p.PI/2;
        //segment_1_rot = p.PI/4;

        calculateSegmentsPositions();
    }

    public float[] HtoRGB(float Hue){
        // turn target angle into RGB color
        float[] RGBResoult;
        float S = 1; // saturation 0-1
        float V = 1; // value 0-1
        float C, X, m;
        float RPrim, GPrim, BPrim;

        float H = p.degrees(Hue);

        C = V * S;
        X = C * (1-p.abs((H/60)%2-1));
        m = V-C;
        RPrim = C;
        GPrim = X;
        BPrim = 0;

        if(H>=0 && H<60){
            RPrim = C;
            GPrim = X;
            BPrim = 0;
        } else if(H>=60 && H<120){
            RPrim = X;
            GPrim = C;
            BPrim = 0;
        } else if(H>=120 && H<180){
            RPrim = 0;
            GPrim = C;
            BPrim = X;
        } else if(H>=180 && H<240){
            RPrim = 0;
            GPrim = X;
            BPrim = C;
        } else if(H>=240 && H<300){
            RPrim = X;
            GPrim = 0;
            BPrim = C;
        } else if(H>=300 && H<360){
            RPrim = C;
            GPrim = 0;
            BPrim = X;
        }
        RGBResoult = new float[]{(RPrim+m)*255,(GPrim+m)*255,(BPrim+m)*255};
        return RGBResoult;
    }

    public void showManipulator(boolean agent){
        p.stroke(color[0], color[1], color[2]);


        // end point
        p.pushMatrix();{
            p.translate(segment_2_end.x, segment_2_end.y);
            p.strokeWeight(2);
            p.noFill();
            p.ellipse(0,0,20,20);
            p.stroke(0);
            p.strokeWeight(5);
            p.point(0,0);
        }p.popMatrix();

        if(!agent) {
            p.stroke(151, 201, 81);
        } else {
            if(isBest) {
                p.stroke(color[0], color[1], color[2],120);
            } else {
                p.stroke(color[0], color[1], color[2]);
            }
        }

        if(!agent){
            p.pushMatrix();{
                p.translate(segment_1_pos.x, segment_1_pos.y);
                p.rotate(segment_1_rot);
                p.strokeWeight(5);
                p.line(0,0,segmentLength,0);
                //p.fill(176, 164, 146);
                p.strokeWeight(2);
                p.ellipse(0,0,10,10);
            }p.popMatrix();

            // segment 2
            p.pushMatrix();{
                p.translate(segment_2_pos.x, segment_2_pos.y);
                p.rotate(segment_2_rot);
                p.strokeWeight(5);;
                p.line(0,0,segmentLength,0);
                //p.fill(176, 164, 146);
                p.strokeWeight(2);
                p.ellipse(0,0,10,10);
            }p.popMatrix();
        }


        if(showInfo) {
            // segment 1
            p.pushMatrix();{
                p.translate(segment_1_pos.x, segment_1_pos.y);
                p.rotate(segment_1_rot);
                p.strokeWeight(5);
                p.line(0,0,segmentLength,0);
                //p.fill(176, 164, 146);
                p.strokeWeight(2);
                p.ellipse(0,0,10,10);
            }p.popMatrix();

            // segment 2
            p.pushMatrix();{
                p.translate(segment_2_pos.x, segment_2_pos.y);
                p.rotate(segment_2_rot);
                p.strokeWeight(5);;
                p.line(0,0,segmentLength,0);
                //p.fill(176, 164, 146);
                p.strokeWeight(2);
                p.ellipse(0,0,10,10);
            }p.popMatrix();



            p.stroke(0, 50);
            //p.line(endPoint.x, endPoint.y, p.cos(targetAngleFromAgent + segment_2_rot) * targetDistFromAgent + endPoint.x, p.sin(targetAngleFromAgent + segment_2_rot) * targetDistFromAgent + endPoint.y); // line from endPoint to the target
            p.line(endPoint.x, endPoint.y, p.cos(targetAngleFromAgent) * targetDistFromAgent + endPoint.x, p.sin(targetAngleFromAgent) * targetDistFromAgent + endPoint.y); // line from endPoint to the target

            // optimal line
            p.stroke(73, 52, 235,150);
            p.line(initialPos.x,initialPos.y,targetPos.x,targetPos.y);

            // shortest line to the optimal line
            p.pushMatrix();
            {
                p.translate(endPoint.x, endPoint.y);
                p.strokeWeight(2);
                p.stroke(207, 37, 167);
                p.line(0,0, p.cos(marchingRay.bestAngle)*marchingRay.smallestDistToRoad,p.sin(marchingRay.bestAngle)*marchingRay.smallestDistToRoad);
            }
            p.popMatrix();

            // route2
//            for(int i = 0; i<route2.size();i++){
//                p.stroke(207, 37, 167,20);
//                p.line(route2.get(i)[0].x,route2.get(i)[0].y,route2.get(i)[1].x,route2.get(i)[1].y);
//            }

            // Hue circle
            p.pushMatrix();
            {
                p.translate(endPoint.x,endPoint.y);
                //p.rotate(segment_2_rot);
                p.noStroke();
                for (int i = 0; i < 360; i++) {
                    p.pushMatrix();
                    p.rotate(p.radians(i));
                    float[] RGBColor = HtoRGB(p.radians(i));
                    p.fill(RGBColor[0], RGBColor[1], RGBColor[2],50);
                    p.circle(50, 0,8);
                    p.popMatrix();
                }
            }p.popMatrix();

            // rectangle showing target rgb position
            p.pushMatrix();
            {
                p.translate(endPoint.x,endPoint.y);
                p.noStroke();
                //p.rotate(targetAngleFromAgent+segment_2_rot);
                p.rotate(targetAngleFromAgent);
                p.fill(RGBTargetPosition[0],RGBTargetPosition[1], RGBTargetPosition[2]);
                p.stroke(0);
                p.rect(50,0,10,10);
            }p.popMatrix();

        }
    }

    public void route(boolean display){
        p.strokeWeight(2);
        p.stroke(140, 171, 222,150);
        distTraveled = 0;
        for(int i =0;i<route.size();i++){
            if(i<route.size()-1){
                distTraveled +=route.get(i).dist(route.get(i+1));
                if(display){
                    p.line(route.get(i).x, route.get(i).y,route.get(i+1).x,route.get(i+1).y);
                }
            }

        }

        distTraveled2 = 0;
        for(int i = 0;i<route2.size();i++){
            if(i<route2.size()-1 && route2.size()>1) {
                float area = 0;

                ArrayList<PVector> vertices = new ArrayList();
                vertices.add(route2.get(i)[0]);
                vertices.add(route2.get(i)[1]);
                vertices.add(route2.get(i+1)[1]);
                vertices.add(route2.get(i+1)[0]);

                int j = vertices.size()-1;
                for(int k = 0;k<vertices.size();k++){
                    area = area + (vertices.get(j).x+vertices.get(k).x)*(vertices.get(j).y-vertices.get(k).y);
                    j = k;
                }
                distTraveled2 += p.abs(area/2);
                vertices.clear();
            }
        }




    }

}
