/* class Rock
 * Represents a rock using a rectangular grid; given a particular level
 * of subdivision l, the rock will be a 2^l+1 X 2^l+1 height field
 * The rock is drawn a little below the surface to get a rough edge.
 *
 */

import java.util.*;

import javax.media.opengl.GL;
import javax.vecmath.*;

class Rock implements Obstacle
{
    // Location of rock
    //private 
    double xpos, ypos, scale;

    // -- Rock mesh: a height-field of rsize X rsize vertices
    int rsize;
    // Height field: z values
    private double[][] height;
    // Whether height value has been set (locked) already
    private boolean[][] locked;

    // Random number generator
    Random rgen;

    // ---------------------------------------------------------------

    public Rock(Random randGen, int level, 
		double xPosition, double yPosition, double scaling)
    {
        // Grid size of (2^level + 1)
        rsize = (1 << level) + 1;

        // Height field -- initially all zeros
        height = new double[rsize][rsize];
        locked = new boolean[rsize][rsize];
 
        rgen = randGen;

	// Set rock position in the world
	xpos = xPosition;
	ypos = yPosition;
	scale = scaling;

	compute();
    }

    // ----------------------------------------------------------------
    // Obstacle methods

    // Get rock location (as a scene element)
    public Point3d getLocation()
    {
	return new Point3d(xpos, ypos, 0);
    }

    // Draw rock in scene
    public void draw(GL gl)
    {
	gl.glPushMatrix();

        // Translate rock down (so it has an interesting boundary)
	gl.glTranslated(xpos, ypos, -0.15);

	gl.glScaled(scale, scale, scale);

        gl.glColor3d(0.6, 0.6, 0.6);

        // Create these outside the loops, so objects persist and
        // unnecessary GC is avoided
        Point3d p = new Point3d();
        Vector3d n = new Vector3d();

        // Draw polygon grid of rock as quad-strips
        for (int i = 0; i < rsize-1; i++) {
            gl.glBegin(GL.GL_QUAD_STRIP);
            for (int j = 0; j < rsize; j++) {
                getRockPoint(i, j, p);
                getRockNormal(i, j, n);
                gl.glNormal3d(n.x, n.y, n.z);
                gl.glVertex3d(p.x, p.y, p.z);
                
                getRockPoint(i+1, j, p);
                getRockNormal(i+1, j, n);
                gl.glNormal3d(n.x, n.y, n.z);
                gl.glVertex3d(p.x, p.y, p.z);
            }
            gl.glEnd();
        }

        // Make GC easy
        p = null;
        n = null;
    
	gl.glPopMatrix();
    }
    
    // ---------------------------------------------------------------

    // Point (i,j) on the rock -- point p gets filled in
    public void getRockPoint(int i, int j, Point3d p)
    {
        // Rock (x,y) locations are on the grid [-0.5, 0.5] x [-0.5, 0.5]
        p.x = (double)i / (rsize-1) - 0.5;
        p.y = (double)j / (rsize-1) - 0.5;
        // Rock z comes from height field
        p.z = height[i][j];
    }

    // Normal vector (i,j) on the rock -- vector n gets filled in
    public void getRockNormal(int i, int j, Vector3d n)
    {
        // This is the formula for a normal vector of a height field with
        // regularly spaced x and y values (assuming rock is zero on
        // its borders and outside of it too)

        // X component is zleft - zright (respecting boundaries)
        n.x = height[(i == 0) ? i : i-1][j] - 
              height[(i == rsize-1) ? i : i+1][j];

        // Y component is zbottom - ztop (respecting boundaries)
        n.y = height[i][(j == 0) ? j : j-1] - 
              height[i][(j == rsize-1) ? j : j+1];

        // Z component is twice the separation
        n.z = 2 / (rsize-1);

        n.normalize();
    }

    // ---------------------------------------------------------------

    // Compute the geometry of the rock
    // (called when the rock is created)
    public void compute()
    {
	// Initialize mesh
	for (int i = 0; i < rsize; i++) {
            for (int j = 0; j < rsize; j++) {
                height[i][j] = 0;

                // Lock sides...
                locked[i][j] = (i == 0 || i == rsize-1 ||
                                j == 0 || j == rsize-1);
            }
	}

        // Raise the middle point and lock it there
        height[rsize/2][rsize/2] = 0.6;
        locked[rsize/2][rsize/2] = true;

        // Recursively compute fractal structure
	computeFractal(0, rsize-1, 0, rsize-1, 1);
	
    }

    // Recursively compute fractal rock geometry
    private void computeFractal(int lowRow, int highRow, int lowCol, int highCol, int recLevel)
    {
    	if(lowRow==highRow-1 || lowCol==highCol-1)
    		return;
    	
    	int midRow = (lowRow + highRow) / 2;
    	int midCol = (lowCol + highCol) / 2;
    	
    	double variance = 0.5 * (1 / Math.pow(2, recLevel));
    	
    	double randVal = rgen.nextDouble() * variance;
    	
    	if(!locked[lowRow][midCol]){
    		if((lowRow - (highRow - midRow))>=0 && locked[(lowRow - (highRow - midRow))][midCol] && locked[lowRow][lowCol] && locked[midRow][midCol] && locked[lowRow][highCol]){
    			height[lowRow][midCol] = (height[(lowRow - (highRow - midRow))][midCol] + height[lowRow][lowCol] + height[midRow][midCol] + height[lowRow][highCol])/4 + randVal;
    			locked[lowRow][midCol] = true;
    		}
    		else if(locked[lowRow][lowCol] && locked[midRow][midCol] && locked[lowRow][highCol]){
    			height[lowRow][midCol] = (height[lowRow][lowCol] + height[midRow][midCol] + height[lowRow][highCol])/3 + randVal;
    			locked[lowRow][midCol] = true;
    		}
    	}
    	
    	if(!locked[midRow][lowCol]){
    		if((lowCol - (highCol - midCol))>=0 && locked[midRow][(lowCol - (highCol - midCol))] && locked[lowRow][lowCol] && locked[midRow][midCol] && locked[highRow][lowCol]){
    			height[midRow][lowCol] = (height[midRow][(lowCol - (highCol - midCol))] + height[lowRow][lowCol] + height[midRow][midCol] + height[highRow][lowCol])/4 + randVal;
    			locked[midRow][lowCol] = true;
    		}
    		else if(locked[lowRow][lowCol] && locked[midRow][midCol] && locked[highRow][lowCol]){
    			height[midRow][lowCol] = (height[lowRow][lowCol] + height[midRow][midCol] + height[highRow][lowCol])/3 + randVal;
    			locked[midRow][lowCol] = true;
    		}
    	}
    	
    	if(!locked[highRow][midCol]){
    		if((highRow + (highRow - midRow))<=(rsize-1) && locked[(highRow + (highRow - midRow))][midCol] && locked[highRow][lowCol] && locked[midRow][midCol] && locked[highRow][highCol]){
    			height[highRow][midCol] = (height[(highRow + (highRow - midRow))][midCol] + height[highRow][lowCol] + height[midRow][midCol] + height[highRow][highCol])/4 + randVal;
    			locked[highRow][midCol] = true;
    		}
    		else if(locked[highRow][lowCol] && locked[midRow][midCol] && locked[highRow][highCol]){
    			height[highRow][midCol] = (height[highRow][lowCol] + height[midRow][midCol] + height[highRow][highCol])/3 + randVal;
    			locked[highRow][midCol] = true;
    		}
    	}
    	
    	if(!locked[midRow][highCol]){
    		if((highCol + (highCol - midCol))<=(rsize-1) && locked[midRow][(highCol + (highCol - midCol))] && locked[highRow][highCol] && locked[midRow][midCol] && locked[lowRow][highCol]){
    			height[midRow][highCol] = (height[midRow][(highCol + (highCol - midCol))] + height[highRow][highCol] + height[midRow][midCol] + height[lowRow][highCol])/4 + randVal;
    			locked[midRow][highCol] = true;
    		}
    		else if(locked[highRow][highCol] && locked[midRow][midCol] && locked[lowRow][highCol]){
    			height[midRow][highCol] = (height[highRow][highCol] + height[midRow][midCol] + height[lowRow][highCol])/3 + randVal;
    			locked[midRow][highCol] = true;
    		}
    	}
    	
    	if(!locked[(lowRow+midRow)/2][(lowCol+midCol)/2]){
    		height[(lowRow+midRow)/2][(lowCol+midCol)/2] = (height[lowRow][lowCol] + height[lowRow][midCol] + height[midRow][midCol] + height[midRow][lowCol])/4 + randVal;
    		locked[(lowRow+midRow)/2][(lowCol+midCol)/2] = true;
    	}
    	
    	if(!locked[(lowRow+midRow)/2][(midCol+highCol)/2]){
    		height[(lowRow+midRow)/2][(midCol+highCol)/2] = (height[lowRow][midCol] + height[lowRow][highCol] + height[midRow][highCol] + height[midRow][midCol])/4 + randVal;
    		locked[(lowRow+midRow)/2][(midCol+highCol)/2] = true;
    	}
    	
    	if(!locked[(midRow+highRow)/2][(lowCol+midCol)/2]){
    		height[(midRow+highRow)/2][(lowCol+midCol)/2] = (height[midRow][lowCol] + height[midRow][midCol] + height[highRow][midCol] + height[highRow][lowCol])/4 + randVal;
    		locked[(midRow+highRow)/2][(lowCol+midCol)/2] = true;
    	}
    	
    	if(!locked[(midRow+highRow)/2][(midCol+highCol)/2]){
    		height[(midRow+highRow)/2][(midCol+highCol)/2] = (height[midRow][midCol] + height[midRow][highCol] + height[highRow][midCol] + height[highRow][highCol])/4 + randVal;
    		locked[(midRow+highRow)/2][(midCol+highCol)/2] = true;
    	}
    	
    	computeFractal(lowRow, midRow, lowCol, midCol, recLevel+1);
        
    	computeFractal(lowRow, midRow, midCol, highCol, recLevel+1);
    	
    	computeFractal(midRow, highRow, lowCol, midCol, recLevel+1);
    	
    	computeFractal(midRow, highRow, midCol, highCol, recLevel+1);
    }
}
