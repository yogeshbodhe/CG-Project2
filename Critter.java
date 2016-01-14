/* class Critter
 * This abstract class implements methods for keeping track of the position,
 * velocity and acceleration of a critter (such as a bug), for integrating
 * these quantities over time, and for computing accelerations that give
 * the bug wandering behavior
 *
 */

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.vecmath.*;
import java.util.*;

abstract class Critter
{
    // Position, velocity, acceleration
    Point3d pos;
    Vector3d vel, acc;

    // Total distance traveled (used for keyframing)
    double dist;

    // Random number generator
    Random rgen;

    // ---------------------------------------------------------------

    // Constructor
    public Critter(Random randomGen)
    {
	pos = new Point3d();
	vel = new Vector3d();
	acc = new Vector3d();

	dist = 0;

	rgen = randomGen;
    }

    // Method to draw critter
    abstract void draw(GL gl);

    // Method to do keyframe animation
    abstract void keyframe(double t);

    // ---------------------------------------------------------------

    // Return location of critter
    public Point3d getLocation()
    {
	return pos;
    }

    // Method to integrate acc to get updated vel and pos;
    // also computes the distance traveled
    // (assumes acc is already computed)
    public void integrate(double dt)
    {
	// Euler integration

    	vel.set(vel.x + (acc.x * dt), vel.y + (acc.y * dt), 0.0);
    	pos.set(pos.x + (vel.x * dt), pos.y + (vel.y * dt), 0.0);

	// Update distance

    	dist += Math.sqrt(Math.pow(vel.x * dt, 2) + Math.pow(vel.y * dt, 2));
    }

    // Accessor for total distance traveled by bug
    public double distTraveled()
    {
	return dist;
    }

    // ---------------------------------------------------------------

    // Reset acceleration to zero
    public void accelReset()
    {
    	acc.set(0,0,0);
    }

    // Add in viscous drag (assume mass of 1):  a += -k v   (k > 0)
    public void accelDrag(double k)
    {
        // Add viscous drag to acceleration acc

    	acc.set(acc.x + ((-k)*vel.x), acc.y + ((-k)*vel.y), acc.z + ((-k)*vel.z));
    }

    // Add in attraction acceleration:  a+= direction * (k*dist^exp)
    // (negative values of k produce repulsion)
    public void accelAttract(Point3d p, double k, double exp)
    {
    	Vector3d dir = new Vector3d();
    	dir.set(p.x - pos.x, p.y - pos.y, p.z - pos.z);
    	dir.normalize();
    	
    	double distance = 0;
    	distance += Math.sqrt(Math.pow(dir.x, 2) + Math.pow(dir.y, 2) + Math.pow(dir.z, 2));
    	
    	double multiplier = k * Math.pow(distance, exp);
    	
    	acc.set(dir.x * multiplier, dir.y * multiplier, dir.z * multiplier);
    }

}
