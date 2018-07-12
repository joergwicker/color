package nz.wicker.colors.weka;

import java.util.Random;
import java.util.ArrayList;
import weka.core.Instances;
import weka.core.Instance;
import weka.core.UnassignedClassException;
import weka.core.UnassignedClassException;



/**
 * Instances class adapted to handle cross evaluation
 * keeping the users in the same training or test set.
 * Changes are just using different objects.
 * 
 */
public class ColorInstances extends Instances{
    

    public ColorInstances(Instances dataset){

        super(dataset);
    }


    public ColorInstances(Instances dataset, int n){

        super(dataset, n);
    }

    
    
    /**
     * Stratifies a set of instances according to its class values if the class
     * attribute is nominal (so that afterwards a stratified cross-validation can
     * be performed).
     * 
     * @param numFolds the number of folds in the cross-validation
     * @throws UnassignedClassException if the class is not set
     */
    public void stratify(int numFolds, int attributeIndex) {
        System.err.println("Something is in strtify " + numFolds + " " + attributeIndex);

        if (numFolds <= 1) {
            throw new IllegalArgumentException(
                                               "Number of folds must be greater than 1");
        }
        if (m_ClassIndex < 0) {
            throw new UnassignedClassException("Class index is negative (not set)!");
        }
        int index = 1;
        while (index < numInstances()) {
            Instance instance1 = instance(index - 1);
            for (int j = index; j < numInstances(); j++) {
                Instance instance2 = instance(j);
                if ((instance1.value(attributeIndex) == instance2.value(attributeIndex))) {
                    swap(index, j);
                    index++;
                }
            }
            index++;
        }
        stratStep(numFolds);
    }
    
    
    
    /**
     * Help function needed for stratification of set.
     * 
     * @param numFolds the number of folds for the stratification
     */
    protected void stratStep(int numFolds, int attributeIndex) {

        ArrayList<Instance> newVec = new ArrayList<Instance>(m_Instances.size());
        int start = 0, j;

        // create stratified batch
        while (newVec.size() < numInstances()) {
            j = start;
            while (j < numInstances()) {
                while (j+1 < numInstances() &&
                       this.instance(j).value(attributeIndex)
                       == this.instance(j+1).value(attributeIndex) ) {
                    newVec.add(instance(j));
                    j++;
                }
                newVec.add(instance(j));
                j = j + numFolds;
            }
            start++;
        }
        m_Instances = newVec;
    }


    
    /**
     * Creates the training set for one fold of a cross-validation on the dataset.
     * 
     * @param numFolds the number of folds in the cross-validation. Must be
     *          greater than 1.
     * @param numFold 0 for the first fold, 1 for the second, ...
     * @return the training set
     * @throws IllegalArgumentException if the number of folds is less than 2 or
     *           greater than the number of instances.
     */
    public ColorInstances trainCV(int numFolds, int numFold, int attributeIndex) {
        System.err.println("Something is in fold " + numFold);
        int numInstForFold, first, offset;
        ColorInstances train;

        int numDistinct = this.numInstances()/12;
        
        int sizeDistinct = numInstances() / numDistinct;
        
        if (numFolds < 2) {
            throw new IllegalArgumentException("Number of folds must be at least 2!");
        }
        if (numFolds > numInstances()) {
            throw new IllegalArgumentException(
                                               "Can't have more folds than instances!");
        }
        numInstForFold = (numDistinct / numFolds) * sizeDistinct;
        if (numFold < numDistinct % numFolds) {
            numInstForFold+=12;
            offset = numFold*12;
        } else {
            offset = (numDistinct % numFolds) * sizeDistinct;
        }
        
        train = new ColorInstances(this, numInstances() - numInstForFold);
        first = numFold * (numDistinct / numFolds)*sizeDistinct + offset;
         copyInstances(0, train, first);
        copyInstances(first + numInstForFold, train, numInstances() - first
                      - numInstForFold);
        return train;
    }


    /**
     * Creates the training set for one fold of a cross-validation on the dataset.
     * The data is subsequently randomized based on the given random number
     * generator.
     * 
     * @param numFolds the number of folds in the cross-validation. Must be
     *          greater than 1.
     * @param numFold 0 for the first fold, 1 for the second, ...
     * @param random the random number generator
     * @return the training set
     * @throws IllegalArgumentException if the number of folds is less than 2 or
     *           greater than the number of instances.
     */
    // @ requires 2 <= numFolds && numFolds < numInstances();
    // @ requires 0 <= numFold && numFold < numFolds;
    public ColorInstances trainCV(int numFolds, int numFold, Random random, int attributeIndex) {

        ColorInstances train = trainCV(numFolds, numFold, attributeIndex);
        train.randomize(random);
        return train;
    }


    /**
     * Creates the test set for one fold of a cross-validation on the dataset.
     * 
     * @param numFolds the number of folds in the cross-validation. Must be
     *          greater than 1.
     * @param numFold 0 for the first fold, 1 for the second, ...
     * @return the test set as a set of weighted instances
     * @throws IllegalArgumentException if the number of folds is less than 2 or
     *           greater than the number of instances.
     */
    // @ requires 2 <= numFolds && numFolds < numInstances();
    // @ requires 0 <= numFold && numFold < numFolds;
    public ColorInstances testCV(int numFolds, int numFold, int attributeIndex) {

        int numInstForFold, first, offset;
        ColorInstances test;

        int numDistinct = this.numInstances()/12;
        
        int sizeDistinct = numInstances() / numDistinct;

        if (numFolds < 2) {
            throw new IllegalArgumentException("Number of folds must be at least 2!");
        }
        if (numFolds > numInstances()) {
            throw new IllegalArgumentException(
                                               "Can't have more folds than instances!");
        }

        numInstForFold = (numDistinct / numFolds) * sizeDistinct;

        System.err.println(numInstForFold + " " + numInstances()  + " " + numFolds);
                

        
        if (numFold < numDistinct % numFolds) {
            numInstForFold+=12;
            offset = numFold*12;
        } else {
            offset = (numDistinct % numFolds) * sizeDistinct;
        }
        System.err.println(numInstForFold + " " + numInstances()  + " " + numFolds);

        test = new ColorInstances(this, numInstForFold);
        first = numFold * (numDistinct / numFolds) * sizeDistinct + offset;
        System.err.println(first + " " + numInstForFold  + " " + numFold);

        copyInstances(first, test, numInstForFold);

        return test;
    }

}
