package org.esa.s2tbx.s2msi.idepix.operators.cloudshadow;

import java.awt.Rectangle;


/**
 * @author Grit Kirches
 * @author Michael Paperin
 */
class MountainVerticalExtent {

    static double[] getMountainVerticalExtent(Rectangle sourceRectangle, float[] sourceAltitude, int yPosition, int xPosition ) {

        double[] mountainExtent = new double[2];
        double mountainBase;
        double mountainTop;
        double temp = sourceAltitude[yPosition * sourceRectangle.width + xPosition];

        // todo cloud height properties
        mountainBase = 0; // [m]
        if (temp <= 0 || Double.isNaN(temp))
            mountainTop  = Double.NaN; // [m]
        else {
            mountainTop  = temp
            ; // [m]
        }

        //cloud top and cloud base height in [m]
        mountainExtent[0]= mountainBase;
        mountainExtent[1]= mountainTop;

    return mountainExtent;
    }


}
