package nz.wicker.colors.weka;


import weka.experiment.CrossValidationResultProducer;
import weka.core.Utils;
import java.util.Random;
import weka.experiment.OutputZipper;
import weka.core.Instances;


/**
 * CrossValidationResultProducerColor class adapted to handle cross evaluation
 * keeping the users in the same training or test set.
 * Changes are just using different objects.
 * 
 *
 *
 * Created: Thu Oct 13 18:53:56 2016
 *
 * @author <a href="mailto:wicker@uni-mainz.de">Joerg Wicker</a>
 * @version 1.0
 */
public class CrossValidationResultProducerColor extends CrossValidationResultProducer {

    public int attributeIndex = 1;
    public int classIndex = 1;
    
    @Override
    public void doRun(int run) throws Exception {

        if (getRawOutput()) {
            if (m_ZipDest == null) {
                m_ZipDest = new OutputZipper(m_OutputFile);
            }
        }

        if (m_Instances == null) {
            throw new Exception("No Instances set");
        }
        // Randomize on a copy of the original dataset
        ColorInstances runInstances = new ColorInstances(m_Instances);
        
        Random random = new Random(run);
        runInstances.setClassIndex(22);
        runInstances.randomize(random);
        runInstances.stratify(m_NumFolds, 1);
        for (int fold = 0; fold < m_NumFolds; fold++) {
            // Add in some fields to the key like run and fold number, dataset name
            Object[] seKey = m_SplitEvaluator.getKey();
            Object[] key = new Object[seKey.length + 3];
            key[0] = Utils.backQuoteChars(m_Instances.relationName());
            key[1] = "" + run;
            key[2] = "" + (fold + 1);
            System.arraycopy(seKey, 0, key, 3, seKey.length);
            if (m_ResultListener.isResultRequired(this, key)) {
                ColorInstances train = runInstances.trainCV(m_NumFolds, fold, random, 1);

                ColorInstances test = runInstances.testCV(m_NumFolds, fold,  1);
                train.setClassIndex(classIndex);
                test.setClassIndex(classIndex);

                System.err.println("----------------> " + test.numInstances() + " " + train.numInstances() + " " + (test.numInstances() +  train.numInstances()));
        
                try {
                    Object[] seResults = m_SplitEvaluator.getResult(train, test);
                    Object[] results = new Object[seResults.length + 1];
                    results[0] = getTimestamp();
                    System.arraycopy(seResults, 0, results, 1, seResults.length);
                    if (m_debugOutput) {
                        String resultName = ("" + run + "." + (fold + 1) + "."
                                             + Utils.backQuoteChars(runInstances.relationName()) + "." + m_SplitEvaluator
                                             .toString()).replace(' ', '_');
                        resultName = Utils.removeSubstring(resultName, "weka.classifiers.");
                        resultName = Utils.removeSubstring(resultName, "weka.filters.");
                        resultName = Utils.removeSubstring(resultName,
                                                           "weka.attributeSelection.");
                        m_ZipDest.zipit(m_SplitEvaluator.getRawResultOutput(), resultName);
                    }
                    m_ResultListener.acceptResult(this, key, results);
                } catch (Exception ex) {
                    // Save the train and test datasets for debugging purposes?
                    throw ex;
                }
            }
        }
    }


}
