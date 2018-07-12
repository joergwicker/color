package nz.wicker.colors.main;

import nz.wicker.colors.weka.CrossValidationResultProducerColor;
import weka.core.Utils;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.trees.RandomForest;
import nz.wicker.colors.weka.ColorInstances;
import nz.wicker.colors.weka.ColorGridSearch;
import nz.wicker.colors.weka.RandomizeClass;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.Remove;
import javax.swing.DefaultListModel;
import java.util.Random;
import weka.experiment.Experiment;
import weka.experiment.AveragingResultProducer;
import weka.experiment.InstancesResultListener;
import weka.experiment.CrossValidationResultProducer;
import weka.experiment.ClassifierSplitEvaluator;
import weka.classifiers.meta.FilteredClassifier;
import weka.filters.unsupervised.attribute.Remove;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.supportVector.RBFKernel;
import weka.core.SelectedTag;
import weka.core.converters.ArffSaver;
import weka.classifiers.functions.SMO;
import java.io.File;
import weka.classifiers.functions.supportVector.RBFKernel;
import weka.classifiers.meta.GridSearch;

/**
 * Describe class Main here.
 *
 * 
 *
 * Created: Fri Oct  7 19:44:11 2016
 *
 * @author <a href="mailto:js@wicker.nz">Joerg Wicker</a>
 * @version 1.0
 */
public class Main {

    public static void main(String[] args) throws Exception{
        int run = -1;
        int ds = -1;
        int id = -1;
        if (args.length > 0) {
            // do only a cross validation
            if (args[0].equals("-cvonly")) {
                simplecv();
                return;
            }

            // generate randomized data sets
            if (args[0].equals("-genrand")) {
                genrand();
                return;
                
            }

            // otherwise do one run of the experiments
            id = Integer.parseInt(args[0]);
            run = id%15;
            ds = id/15;

        }

        // do for all countries (and all data)
        for (String country : new String[]{"all","de","en","gr","zh"}){

            // initialize
            
            int idindex = 1;
            String path = "data/defEmoVector-"+country+".csv.arff";
            DataSource source = new DataSource(path);
            Instances data = source.getDataSet();

        
            // prepare filter to remove uid
            
            Remove removeUID = new Remove();
            removeUID.setAttributeIndicesArray(new int[]{0,1});//idindex});
                    
            //optimized smo
        
            RBFKernel rbf = new RBFKernel();
        
            SMO smo = new SMO();
        
            smo.setKernel(rbf);
            smo.setBuildCalibrationModels(true);
        
        
            ColorGridSearch gs = new ColorGridSearch();
            
            gs.setEvaluation(new SelectedTag(GridSearch.EVALUATION_ACC,
                                             GridSearch.TAGS_EVALUATION));
            gs.setGridIsExtendable(false);



            gs.setClassifier(smo);

            gs.setXBase(10.0);
            gs.setXExpression("pow(BASE,I)");
            gs.setXMin(-3.0);
            gs.setXMax(3.0);
            gs.setXProperty("C");
            gs.setXStep(1.0);
        
        
            gs.setYBase(10.0);
            gs.setYExpression("pow(BASE,I)");
            gs.setYMin(-3.0);
            gs.setYMax(3.0);
            gs.setYProperty("kernel.gamma");
            gs.setYStep(1.0);
        

            // Random Forest
            
            RandomForest rf = new RandomForest();
            rf.setSeed(id);
            FilteredClassifier fc = new FilteredClassifier();
            fc.setFilter(removeUID);


            // optimized smo
            //fc.setClassifier(gs);

            // random forest
            fc.setClassifier(rf);


            // Initialize experiment objects
            ClassifierSplitEvaluator cse = new ClassifierSplitEvaluator();
            cse.setClassifier(fc);
            cse.setClassForIRStatistics(0);



            CrossValidationResultProducerColor cvrp = new CrossValidationResultProducerColor();
            cvrp.attributeIndex = 1;
            cvrp.classIndex = 22;
            cvrp.setNumFolds(10);
            cvrp.setSplitEvaluator(cse);
        
            InstancesResultListener irl = new InstancesResultListener();
            irl.setOutputFile(new File("res-1711/res-color-predict-"+country+".arff"));
            if (id < 0) {
                System.out.println("Setting out to res-1711/res-color-predict-"+country+".arff");
                irl.setOutputFile(new File("res-1711/res-color-predict-"+country+".arff"));
                irl.setOutputFileName("res-1711/res-color-predict-"+country+".arff");
            } else {
                System.out.println("Setting out to res-1711/res-color-predict-"+country+".arff-"+ id);
                irl.setOutputFileName("res-1711/res-color-predict-"+country+".arff-"+ id);
                irl.setOutputFile(new File("res-1711/res-color-predict-"+country+".arff-"+id));

            }

       
            AveragingResultProducer arp = new AveragingResultProducer();
            arp.setResultProducer(cvrp);
        
            arp.setInstances(data);

        
            Experiment exp = new Experiment();
            exp.setResultProducer(arp);
            exp.setResultListener(irl);

            DefaultListModel<File> dlm = new DefaultListModel<File>();
            
            if (ds ==0) {
                dlm.addElement(new File("data/defEmoVector-"+country+".csv.arff"));
            }
            
            for (int i = 0; i < 10; i++) {
                if (id < 0 || ds-1 == i) {
                    dlm.addElement(new File("data/defEmoVector-"+country+"-rand-"+ i +".csv.arff"));
                }
            }

        
            
            exp.setDatasets(dlm);

            exp.setAdvanceDataSetFirst(true);
            if (run < 0) {
                exp.setRunLower(1);
                exp.setRunUpper(15);
            } else {
                exp.setRunLower(run);
                exp.setRunUpper(run);
            }

            // run experiments
            
            System.out.println("Running experiment...");
            System.out.println(exp.toString());
            System.out.println("Initializing...");
            exp.initialize();
            System.out.println("Running...");
            exp.runExperiment();
            System.out.println("Finishing...");
            exp.postProcess();
            System.out.println("Done");

            // write results            
            Experiment.write("experimenter/gen.xml", exp);
        
        
        }
    }

    public static void genrand() throws Exception{
        for (String country : new String[]{"de","en","gr","zh"}){
            String path = "data/defEmoVector-"+country+".csv.arff";
            
            DataSource source = new DataSource(path);
            Instances data = source.getDataSet();
            data.setClassIndex(22);
            for (int i = 0; i < 10; i ++) {
                
                
                
                RandomizeClass.randomize(data, 1000, 1);
                ArffSaver saver = new ArffSaver();
                saver.setInstances(data);
                saver.setFile(new File("data/defEmoVector-"+country+"-rand-"+i+".csv.arff"));
                saver.writeBatch();
            }

        }
    }

    public static void simplecv() throws Exception{
        for (String country : new String[]{"all","de","en","gr","zh"}){
            System.err.println("Starting " + country);
            int idindex = 1;
            int classIndex = 22;

            int seed = 6;
            int folds  = 10;
        
            String path = "data/defEmoVector-"+country+".csv.arff";
            DataSource source = new DataSource(path);
            ColorInstances data = new ColorInstances(source.getDataSet());

            data.setClassIndex(classIndex);
            System.err.println("data read");
        
            // prepare filter to remove uid
            Remove removeUID = new Remove();
            removeUID.setAttributeIndicesArray(new int[]{0,1});//idindex});

            //optimized smo
        
            RBFKernel rbf = new RBFKernel();
        
            SMO smo = new SMO();
        
            smo.setKernel(rbf);
            smo.setBuildCalibrationModels(true);
        
        
            ColorGridSearch gs = new ColorGridSearch();
            
        
            gs.setEvaluation(new SelectedTag(GridSearch.EVALUATION_ACC,
                                             GridSearch.TAGS_EVALUATION));
            gs.setGridIsExtendable(false);



            gs.setClassifier(smo);

            gs.setXBase(10.0);
            gs.setXExpression("pow(BASE,I)");
            gs.setXMin(-3.0);
            gs.setXMax(3.0);
            gs.setXProperty("C");
            gs.setXStep(1.0);
        
        
            gs.setYBase(10.0);
            gs.setYExpression("pow(BASE,I)");
            gs.setYMin(-3.0);
            gs.setYMax(3.0);
            gs.setYProperty("kernel.gamma");
            gs.setYStep(1.0);
        

            RandomForest rf = new RandomForest();
    
            FilteredClassifier fc = new FilteredClassifier();
            fc.setFilter(removeUID);

            fc.setClassifier(rf);

            System.err.println("everything setup");
            
            Random rand = new Random(seed);
            ColorInstances randData = new ColorInstances(data);

            randData.setClassIndex(22);
            
            randData.randomize(rand);

            randData.stratify(folds, 1);

            Evaluation eval = new Evaluation(randData);

            for (int n = 0; n < folds; n++) {
                System.err.println("F "+n);

                ColorInstances train = randData.trainCV(folds, n, rand, idindex);
                ColorInstances test = randData.testCV(folds, n,idindex);


                Classifier clsCopy = AbstractClassifier.makeCopy(fc);
                System.err.println("copy");
                clsCopy.buildClassifier(train);
                System.err.println("built");
                eval.evaluateModel(clsCopy, test);
                System.err.println("tested");
            
            }
        
            System.out.println();
            System.out.println("=== Setup === " + country + "====");
            System.out.println("Classifier: " + rf.getClass().getName() + " " + Utils.joinOptions(rf.getOptions()));
            System.out.println("Dataset: " + data.relationName());
            System.out.println("Folds: " + folds);
            System.out.println("Seed: " + seed);
            System.out.println();
            System.out.println(eval.toClassDetailsString());
            System.out.println();
            System.out.println(eval.toSummaryString("=== " + folds + "-fold Cross-validation ===", true));
            System.out.println();
            System.out.println(eval.toMatrixString());
            System.out.println();
            System.out.println();
            System.out.println(); 	
            System.out.println(eval.toCumulativeMarginDistributionString());
            System.out.println();
        
            System.err.println("Done " + country);

        
        }
    }
}
