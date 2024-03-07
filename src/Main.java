import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;

public class Main extends PApplet {
    ArrayList<Agent> agents;
    public static float rateOfDecay = 0.001f;
    public static float[][] trailMap;
    public static float sensoryLength = 9;
    public static int spawnRadius = 10;
    public static float f;
    public static float sensoryAngle = QUARTER_PI;
    public static float rotationAngle = QUARTER_PI / 2;
    public static boolean enable_random_gen = false;

    public static int h, w;

    public void settings() {
        size(512, 512);
        h = height;
        w = width;
    }

    public void setup() {
        f = 0;
        agents = new ArrayList<Agent>();

        for (int i = 0; i < 100000; i++) {
            //PVector loc = new PVector(random(width/2-spawnRadius, width/2+spawnRadius), random(height/2-spawnRadius, height/2+spawnRadius));
            PVector loc = PVector.add(new PVector(width/2, height/2), PVector.mult(PVector.random2D(), spawnRadius));
            //PVector loc = new PVector(random(width), random(height));
            //PVector dir = PVector.sub(new PVector(width/2, height/2), loc);
            PVector dir = PVector.random2D();
            agents.add(new Agent(loc, dir));
        }

        trailMap = new float[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                trailMap[x][y] = 0;
            }
        }
    }

    private void display(Agent a) {
        stroke(255, 255, 255);
        fill(255, 255, 255);
        //circle(loc.x, loc.y, 10);
        point(a.loc.x, a.loc.y);
    }

    public void draw() {
        background(0);
        f += 0.03;

        if (enable_random_gen) {
            for (int i = 0; i < 200; i++) {
                //PVector loc = new PVector(random(width/2-spawnRadius, width/2+spawnRadius), random(height/2-spawnRadius, height/2+spawnRadius));
                PVector loc = new PVector(random(width), random(height));
                //PVector loc = new PVector(random(width), random(height));
                //PVector dir = PVector.sub(new PVector(width/2, height/2), loc);
                PVector dir = PVector.random2D();
                agents.add(new Agent(loc, dir));
            }
        }

        //loadPixels();
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

        for (int i = 0; i < 1000; i++) {
            //agents.add(new Agent(new PVector(width/2, height/2), PVector.fromAngle(map(noise(f), 0, 1, 0, TWO_PI))));
        }

        for (Agent a : agents) {
            display(a);
        }


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
            PVector dirCopy = PVector.mult(a.dir, sensoryLength);
            dirCopy.rotate(-sensoryAngle);
            PVector newCoordsLeft = PVector.add(a.loc, dirCopy);
            dirCopy = PVector.mult(a.dir, 9);
            dirCopy.rotate(sensoryAngle);
            PVector newCoordsRight = PVector.add(a.loc, dirCopy);

            float F = getLegalTrailMapEntry(newCoords);
            float FL = getLegalTrailMapEntry(newCoordsLeft);
            float FR = getLegalTrailMapEntry(newCoordsRight);

            if (F > FL && F > FR) {
                // all good, continue facing the same direction!
            } else if (F < FL && F < FR) {
                float pFL = FL / (FL+FR);
                if (random(1) < pFL) {
                    a.dir.rotate(-rotationAngle);
                } else {
                    a.dir.rotate(rotationAngle);
                }
            } else if (FL < FR) {
                a.dir.rotate(rotationAngle);
            } else if (FR < FL) {
                a.dir.rotate(-rotationAngle);
            }
        }

        filter(BLUR);

        // saveFrame("frames/#####.png");
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

    public static void main(String[] args) {
        PApplet.main("Main");
    }
}
