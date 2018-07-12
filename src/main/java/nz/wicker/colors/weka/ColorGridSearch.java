/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package nz.wicker.colors.weka;

import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.instance.Resample;



import weka.classifiers.meta.GridSearch;


/**
 * GridSearch class adapted to handle cross evaluation
 * keeping the users in the same training or test set.
 * Changes are just using different objects.
 * 
 */
public class ColorGridSearch extends GridSearch {
    /**
     * One of the methods that have to be adapted slightly.
     * 
     * returns the best values-pair in the grid.
     * 
     * @return 		the best values pair
     * @throws Exception 	if something goes wrong
     */
    protected PointDouble findBest() throws Exception {
        PointInt		center;
        Grid		neighborGrid;
        boolean		finished;
        PointDouble		result;
        PointDouble		resultOld;
        int			iteration;
        Instances		sample;
        Resample		resample;

        log("Step 1:\n");

        // generate sample
        if (getSampleSizePercent() == 100) {
            sample = m_Data;
        }
        else {
            log("Generating sample (" + getSampleSizePercent() + "%)");
            resample = new Resample();
            resample.setRandomSeed(getSeed());
            resample.setSampleSizePercent(getSampleSizePercent());
            resample.setInputFormat(m_Data);
            sample = Filter.useFilter(m_Data, resample);
        }
    
        finished                  = false;
        iteration                 = 0;
        m_GridExtensionsPerformed = 0;
        m_UniformPerformance      = false;
    
        // find first center
        log("\n=== Initial grid - Start ===");
        result = determineBestInGrid(m_Grid, sample, 2);
        log("\nResult of Step 1: " + result + "\n");
        log("=== Initial grid - End ===\n");

        finished = m_UniformPerformance;
    
        if (!finished) {
            do {
                iteration++;
                resultOld = (PointDouble) result.clone();
                center    = m_Grid.getLocation(result);
                // on border? -> finished (if it cannot be extended)
                if (m_Grid.isOnBorder(center)) {
                    log("Center is on border of grid.");

                    // can we extend grid?
                    if (getGridIsExtendable()) {
                        // max number of extensions reached?
                        if (m_GridExtensionsPerformed == getMaxGridExtensions()) {
                            log("Maximum number of extensions reached!\n");
                            finished = true;
                        }
                        else {
                            m_GridExtensionsPerformed++;
                            m_Grid = m_Grid.extend(result);
                            center = m_Grid.getLocation(result);
                            log("Extending grid (" + m_GridExtensionsPerformed + "/" 
                                + getMaxGridExtensions() + "):\n" + m_Grid + "\n");
                        }
                    }
                    else {
                        finished = true;
                    }
                }

                // new grid with current best one at center and immediate neighbors 
                // around it
                if (!finished) {
                    neighborGrid = m_Grid.subgrid(
                                                  (int) center.getY() + 1, (int) center.getX() - 1, 
                                                  (int) center.getY() - 1, (int) center.getX() + 1);
                    result = determineBestInGrid(neighborGrid, sample, 3);
                    log("\nResult of Step 2/Iteration " + (iteration) + ":\n" + result);
                    finished = m_UniformPerformance;

                    // no improvement?
                    if (result.equals(resultOld)) {
                        finished = true;
                        log("\nNo better point found.");
                    }
                }
            }
            while (!finished);
        }
    
        log("\nFinal result: " + result);

        return result;
    }

}
