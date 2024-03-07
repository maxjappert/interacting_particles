import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;

import static processing.core.PApplet.floor;

public class AgentProcessor extends PApplet implements Runnable {
    private ArrayList<Agent> agents;
    private float[][] trailMap;

    AgentProcessor(ArrayList<Agent> agents, float[][] trailMap) {
        this.agents = agents;
        this.trailMap = trailMap;
    }

    @Override
    public void run() {
        for (int i = 2; i < height-2; i++) {
            for (int j = 2; j < width-2; j++) {
                trailMap[j][i] = blur(i, j, 2);

                if (trailMap[j][i] < 0) {
                    trailMap[j][i] = 0;
                }

                //pixels[i*width+j] = color(map(trailMap[j][i], 0, 1, 0, 255));
            }
        }
        //updatePixels();

        //for (int i = 0; i < 1000; i++) {
        //agents.add(new Agent(new PVector(width/2, height/2), PVector.fromAngle(map(noise(f), 0, 1, 0, TWO_PI))));
        //}


        for (Agent a : agents) {
            // Motor stage
            PVector newCoords = PVector.add(a.loc, a.dir);
            if (successfulMoveForward(newCoords)) {
                a.loc.add(a.dir);
                trailMap[floor(newCoords.x)][floor(newCoords.y)] += 5;
            } else {
                while(!successfulMoveForward(newCoords)) {
                    a.dir = PVector.random2D();
                    newCoords = PVector.add(a.loc, a.dir);
                }
            }

            // Sensory stage

            // Sample trail map values
            PVector dirCopy = PVector.mult(a.dir, Main.sensoryLength);
            dirCopy.rotate(-Main.sensoryAngle);
            PVector newCoordsLeft = PVector.add(a.loc, dirCopy);
            dirCopy = PVector.mult(a.dir, 9);
            dirCopy.rotate(Main.sensoryAngle);
            PVector newCoordsRight = PVector.add(a.loc, dirCopy);

            float F = getLegalTrailMapEntry(newCoords);
            float FL = getLegalTrailMapEntry(newCoordsLeft);
            float FR = getLegalTrailMapEntry(newCoordsRight);

            if (F > FL && F > FR) {
                // all good, continue facing the same direction!
            } else if (F < FL && F < FR) {
                float pFL = FL / (FL+FR);
                if (random(1) < pFL) {
                    a.dir.rotate(-Main.rotationAngle);
                } else {
                    a.dir.rotate(Main.rotationAngle);
                }
            } else if (FL < FR) {
                a.dir.rotate(Main.rotationAngle);
            } else if (FR < FL) {
                a.dir.rotate(-Main.rotationAngle);
            }
        }
        //saveFrame("frames/#####.png");
    }


float getLegalTrailMapEntry(PVector coords) {

    if (floor(coords.x) < 0) {
        coords.x = 0;
    }

    if (floor(coords.x) > Main.w-1) {
        coords.x = Main.w-1;
    }

    if (floor(coords.y) < 0) {
        coords.y = 0;
    }

    if (floor(coords.y) > Main.h-1) {
        coords.y = Main.h-1;
    }

    return Main.trailMap[floor(coords.x)][floor(coords.y)];
}

boolean successfulMoveForward(PVector newCoords) {
    return newCoords.x >= 0 && newCoords.x <= Main.w-1 && newCoords.y >= 0 && newCoords.y <= height-1;
}

float blur(int index_i, int index_j, int degree) {
    float value = 0;
    int normaliser = 0;
    for (int i = index_i-degree; i <= index_i+degree; i++) {
        for (int j = index_j-degree; j <= index_j+degree; j++) {
            value += trailMap[i][j];
            normaliser++;
        }
    }

    return value / normaliser;
}
}