package com.github.tjake.rbm.minst;


import com.github.tjake.rbm.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BinaryMinstDBN {
    static MinstDatasetReader dr;
    StackedRBM rbm;
    final StackedRBMTrainer trainer;
    final LayerFactory layerFactory = new LayerFactory();

    public BinaryMinstDBN(File labels, File images) {
        dr = new MinstDatasetReader(labels, images);

        rbm = new StackedRBM();
        trainer = new StackedRBMTrainer(rbm, 0.5f, 0.001f, 0.2f, 0.2f, layerFactory);
    }

    void learn(int iterations, boolean addLabels, int stopAt) {

        for (int p = 0; p < iterations; p++) {

            // Get random input
            List<Layer> inputBatch = new ArrayList<Layer>();
            List<Layer> labelBatch = addLabels ? new ArrayList<Layer>() : null;


            for (int j = 0; j < 30; j++) {
                MinstItem trainItem = dr.getTrainingItem();
                Layer input = layerFactory.create(trainItem.data.length);

                for (int i = 0; i < trainItem.data.length; i++)
                    input.set(i, trainItem.data[i]);

                inputBatch.add(new BinaryLayer(input));

                if (addLabels) {
                    float[] labelInput = new float[10];
                    labelInput[Integer.valueOf(trainItem.label)] = 1.0f;
                    labelBatch.add(layerFactory.create(labelInput));
                }
            }

            double error = trainer.learn(inputBatch, labelBatch, stopAt);

            if (p % 100 == 0)
                System.err.println("Iteration " + p + ", Error = " + error+", Energy = "+rbm.freeEnergy());
        }
    }

    Iterator<Tuple> evaluate(MinstItem test) {

        Layer input = layerFactory.create(test.data.length);

        for (int i = 0; i < test.data.length; i++)
            input.set(i, test.data[i]);

        input = new BinaryLayer(input);

        int stackNum = rbm.getInnerRBMs().size();

        for (int i = 0; i < stackNum; i++) {

            SimpleRBM iRBM = rbm.getInnerRBMs().get(i);

            if (iRBM.biasVisible.size() > input.size()) {
                Layer newInput = new Layer(iRBM.biasVisible.size());

                System.arraycopy(input.get(), 0, newInput.get(), 0, input.size());
                for (int j = input.size(); j < newInput.size(); j++)
                    newInput.set(j, 0.1f);

                input = newInput;
            }

            if (i == (stackNum - 1)) {
                return iRBM.iterator(input);
            }

            input = iRBM.activateHidden(input, null);
        }

        return null;
    }


    public static void start(File labels, File images, File saveto) {

        BinaryMinstDBN m = new BinaryMinstDBN(labels,images);

        boolean prevStateLoaded = false;

        if (saveto.exists()){
            try {
                DataInput input = new DataInputStream(new BufferedInputStream(new FileInputStream(saveto)));
                m.rbm.load(input, m.layerFactory);
                prevStateLoaded = true;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        if (!prevStateLoaded) {
            int numIterations = 1000;

            m.rbm.setLayerFactory(m.layerFactory).addLayer(dr.rows * dr.cols, false).addLayer(500, false).addLayer(500, false).addLayer(2000, false).withCustomInput(510).build();

            System.err.println("Training level 1");
            m.learn(numIterations, false, 1);
            System.err.println("Training level 2");
            m.learn(numIterations, false, 2);
            System.err.println("Training level 3");
            m.learn(numIterations, true, 3);

            try {
                DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(saveto)));
                m.rbm.save(out);

                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        double numCorrect = 0;
        double numWrong = 0;
        double numAlmost = 0.0;

        while (true) {
            MinstItem testCase = m.dr.getTestItem();

            Iterator<Tuple> it = m.evaluate(testCase);

            float[] labeld = new float[10];

            for (int i = 0; i < 2; i++) {
                Tuple t = it.next();

                for (int j = (t.visible.size() - 10), k = 0; j < t.visible.size() && k < 10; j++, k++) {
                    labeld[k] += t.visible.get(j);
                }
            }

            float max1 = 0.0f;
            float max2 = 0.0f;
            int p1 = -1;
            int p2 = -1;

            System.err.print("Label is: " + testCase.label);


            for (int i = 0; i < labeld.length; i++) {
                labeld[i] /= 2;
                if (labeld[i] > max1) {
                    max2 = max1;
                    max1 = labeld[i];

                    p2 = p1;
                    p1 = i;
                }
            }

            System.err.print(", Winner is " + p1 + "(" + max1 + ") second is " + p2 + "(" + max2 + ")");
            if (p1 == Integer.valueOf(testCase.label)) {
                System.err.println(" CORRECT!");
                numCorrect++;

            } else if (p2 == Integer.valueOf(testCase.label)) {
                System.err.println(" Almost!");
                numAlmost++;
            } else {
                System.err.println(" wrong :(");
                numWrong++;
            }

            System.err.println("Error Rate = " + ((numWrong / (numAlmost + numCorrect + numWrong)) * 100));

        }
    }
}
