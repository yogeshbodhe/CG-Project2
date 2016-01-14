/* class Tree
 * Class for representing a tree, providing methods to create and draw
 * the tree in terms of TreeParts (a recursive data structure)
 * 
 */

import java.util.*;

import javax.media.opengl.GL;
import javax.vecmath.*;

class Tree implements Obstacle
{
    // Location of tree
    private double xpos, ypos;

    // Base of tree
    TreePart tree;

    // ---------------------------------------------------------------

    // constructor
    public Tree(Random rgen, int level, int branching,
		double trunkLen, double trunkDiam,
		double xPosition, double yPosition)
    {
	super();

	// Set tree position
	xpos = xPosition;
	ypos = yPosition;

	// Construct tree
	tree = new TreePart(rgen, level, branching, trunkLen, trunkDiam, 0, 0, 0, 0);
    }

    // ---------------------------------------------------------------
    // Obstacle methods

    // Get tree location (as a scene element)
    public Point3d getLocation()
    {
	return new Point3d(xpos, ypos, 0);
    }

    // Draw tree in scene
    public void draw(GL gl)
    {
	gl.glPushMatrix();
	gl.glTranslated(xpos, ypos, 0);
	tree.draw(gl);
	gl.glPopMatrix();
    }
}
