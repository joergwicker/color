package nz.wicker.colors.weka;

import java.util.Random;
import weka.core.Instances;

/**
 * Helper class that randomizes the class value.
 *
 *
 * Created: Mon Oct 10 18:40:24 2016
 *
 * @author <a href="mailto:wicker@uni-mainz.de">Joerg Wicker</a>
 * @version 1.0
 */
public class RandomizeClass {

    /**
     * Randomize method that randomizes the data.
     */
    public static void randomize(Instances inst, int times, int attributeIndex){
        System.err.println("starting...");
        for (int i = 0; i < times * inst.size(); i++) {
            Random random = new Random();
            
            int first = random.nextInt(inst.numInstances());
            
            int second = random.nextInt(inst.numInstances());

            while (first == second ||
                   inst.instance(first).value(attributeIndex) !=
                   inst.instance(second).value(attributeIndex)) {
                first = random.nextInt(inst.numInstances());

                second = random.nextInt(inst.numInstances());
            }

            
            double cv = inst.instance(first).classValue();
            
            inst.instance(first).setClassValue(inst.instance(second).classValue());
            inst.instance(second).setClassValue(cv);
            System.err.println("done i = " +i + " (max = " + times*inst.size()+", todo: " + (times*inst.size() -i) +")");
        }

        

    }


    /**
     * Randomize method that randomizes the data.
     */
    public static void randomize(Instances inst, int times){
        
        for (int i = 0; i < times * inst.size(); i++) {

            Random random = new Random();
            
            int first = random.nextInt(inst.numInstances());
            int second = random.nextInt(inst.numInstances());

            double cv = inst.instance(first).classValue();
            
            inst.instance(first).setClassValue(inst.instance(second).classValue());
            inst.instance(second).setClassValue(cv);
        }

        

    }
}
