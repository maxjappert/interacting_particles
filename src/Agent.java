import processing.core.PApplet;
import processing.core.PVector;

public class Agent extends PApplet {
    PVector loc, dir;

    Agent(PVector loc, PVector dir) {
        this.loc = loc;
        this.dir = dir;
        this.dir.setMag(1);
    }


}