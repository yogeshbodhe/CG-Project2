/* class TreePart
 * Class for representing a subtree, describing the size of the part at
 * the transformation to get to this subtree from the parent, the
 * current tree node (length and width) and whether this is a leaf node
 *
 */

import java.nio.DoubleBuffer;
import java.util.*;

import javax.media.opengl.GL;
import javax.vecmath.*;

class TreePart
{
    // Transformation for this branch/leaf (relative to its parent)

    // specify angles and translations to say how parts of the
    // tree are positioned with respect to each other

    // Leaf or trunk
    boolean leaf;

    // Size of part
    double length, width;

    // Children
    TreePart[] parts;
    
    double translation, xRotation, yRotation, zRotation;

    // ---------------------------------------------------------------

    // Constructor: recursively construct a treepart of a particular depth,
    // with specified branching factor, dimensions and transformation
    public TreePart(Random rgen,
		    int depth, int numBranch,
		    double partLen, double partWid,
                    // ... transformation specification can go here
            double translation, double xRotation, double yRotation, double zRotation)
    {
        this.leaf = false;
        this.length = partLen;
        this.width = partWid;
        this.translation = translation;
        this.xRotation = xRotation;
        this.yRotation = yRotation;
        this.zRotation = zRotation;


        double variance = 0.5 * (1 / Math.pow(2, depth));
    	double randVal = rgen.nextDouble() * variance;
    	//double randValRot;

        // Create branch or leaf (based on depth) and create children
        // branches/leaves recursively
        if(depth==0){
        	this.leaf = true;
        	return;
        }
        
        parts = new TreePart[numBranch];
        
        for(int i=0; i<numBranch; i++){
        	int branching_factor = (int)(numBranch + rgen.nextGaussian());
        	branching_factor = (branching_factor<=0) ? numBranch : branching_factor;
        	
        	double pLen = rgen.nextDouble() * partLen;
        	pLen = (pLen>((partLen*2)/3)) ? ((pLen*2)/3) : ((partLen*2)/3);
        	
        	int div = (int)Math.abs(rgen.nextGaussian());
        	double pWid = (div<2) ? (partWid/2) : (partWid/div);
        	
        	double trans = rgen.nextDouble() * partLen;
        	trans = (trans>(partLen/2)) ? trans : partLen;
        	
        	double xRot = (rgen.nextGaussian()/2) * 45;
        	double yRot = (rgen.nextGaussian()/2) * 45;
        	double zRot = (rgen.nextGaussian()/2) * 45;
        	
        	parts[i] = new TreePart(rgen, depth-1, branching_factor, pLen, pWid, trans, xRot, yRot, zRot);
        }
    }

    // Recursively draw a tree component
    //  - place the component using transformation for this subtree
    //  - draw leaf (if this is a leaf node)
    //  - draw subtree (if this is an interior node)
    //    (draw this component, recursively draw children)
    public void draw(GL gl)
    {
	gl.glPushMatrix();

	// Place this component
    // (apply transformation for this component)
	gl.glTranslated(0, 0, translation);
	gl.glRotated(zRotation, 0, 0, 1);
	gl.glRotated(yRotation, 0, 1, 0);
	gl.glRotated(xRotation, 1, 0, 0);
	gl.glScaled(width, width, length);
	
	double xS = 1/width, yS = 1/width, zS = 1/length;
	
	if (leaf) {
            // Draw leaf
		double color[] = new double[4];
		gl.glGetDoublev(GL.GL_CURRENT_COLOR, DoubleBuffer.wrap(color));
		gl.glColor3d(0.0, 1.0, 0.0);
		gl.glBegin(GL.GL_POLYGON);
		gl.glVertex3d(0.0, 0.0, 0.0);
		gl.glVertex3d(length*xS/2, length*yS/3, length*zS/2);
		gl.glVertex3d(length*xS/2, length*yS/3, -length*zS/2);
		gl.glVertex3d(0.0, length*yS, 0.0);
		gl.glVertex3d(-length*xS/2, length*yS/3, length*zS/2);
		gl.glVertex3d(-length*xS/2, length*yS/3, -length*zS/2);
		gl.glEnd();
		
		gl.glScaled(xS, yS, zS);
		gl.glColor3d(color[0], color[1], color[2]);
	} else {
            // Draw branch

            // (transformation for cylinder)

            Objs.cylinder(gl);
            
            gl.glScaled(xS, yS, zS);

	    // Recursively draw children
            for(int i=0; i<parts.length; i++){
            	parts[i].draw(gl);
            }
	}

	gl.glPopMatrix();
    }
}
