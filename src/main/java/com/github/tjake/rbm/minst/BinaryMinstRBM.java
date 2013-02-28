package com.github.tjake.rbm.minst;

import com.github.tjake.rbm.*;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BinaryMinstRBM extends Canvas {
    static int border = 10; // 10px

    MinstDatasetReader dr;

    static int count = 0;

    final SimpleRBM rbm;
    final LayerFactory layerFactory = new LayerFactory();

    MinstItem trainItem = null;
    List<int[]> outputs = new ArrayList<int[]>();

    final SimpleRBMTrainer trainer;

    public BinaryMinstRBM(File labels, File images) {

        dr = new MinstDatasetReader(labels, images);

        rbm = new SimpleRBM(dr.cols * dr.rows, 10 * 10, false, layerFactory);
        trainer = new SimpleRBMTrainer(0.2f, 0.001f, 0.2f, 0.1f, layerFactory);
    }

    float[] learn() {
        // Get random input
        List<Layer> inputBatch = new ArrayList<Layer>();

        for (int j = 0; j < 30; j++) {
            trainItem = dr.getTrainingItem();
            Layer input = layerFactory.create(trainItem.data.length);

            for (int i = 0; i < trainItem.data.length; i++)
                input.set(i, trainItem.data[i]);

            inputBatch.add(new BinaryLayer(input));
        }

        double error = trainer.learn(rbm, inputBatch, false); //up down


        if (count % 100 == 0)
            System.err.println("Error = " + error + ", Energy = " + rbm.freeEnergy());

        return inputBatch.get(inputBatch.size() - 1).get();
    }

    Iterator<Tuple> evaluate() {


        MinstItem test = dr.getTestItem();

        Layer input = layerFactory.create(test.data.length);

        for (int i = 0; i < trainItem.data.length; i++)
            input.set(i, trainItem.data[i]);

        return rbm.iterator(new BinaryLayer(input));
    }

    public void update() {
        learn();
        Iterator<Tuple> it = evaluate();

        synchronized (outputs) {
            outputs.clear();
            for (int j = 0; j < 10; j++) {
                Tuple t = it.next();
                int[] output = new int[t.visible.size()];
                float[] visible = BinaryLayer.fromBinary(t.visible);

                for (int i = 0; i < visible.length; i++) {
                    output[i] = Math.round(visible[i]);
                }

                outputs.add(output);
            }
        }
        repaint();
    }

    public void paint(Graphics g) {

        BufferedImage in = new BufferedImage(dr.cols, dr.rows, BufferedImage.TYPE_INT_RGB);

        if (trainItem == null)
            return;

        WritableRaster r = in.getRaster();
        r.setDataElements(0, 0, dr.cols, dr.rows, trainItem.data);
        g.drawImage(in, border, border, null);

        int offset = border;
        synchronized (outputs) {
            for (int[] output : outputs) {
                BufferedImage out = new BufferedImage(dr.cols, dr.rows, BufferedImage.TYPE_INT_RGB);


                r = out.getRaster();
                r.setDataElements(0, 0, dr.cols, dr.rows, output);

                //Resize
                BufferedImage newImage = new BufferedImage(56, 56, BufferedImage.TYPE_INT_RGB);

                Graphics2D g2 = newImage.createGraphics();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                    g2.clearRect(0, 0, 56, 56);
                    g2.drawImage(out, 0, 0, 56, 56, null);
                } finally {
                    g2.dispose();
                }
                g.drawImage(newImage, border * 2 + 28, offset, null);

                offset += border + dr.rows * 2;
            }

            int buf = 28 + border + border;
            for (int i = 0; i < rbm.weights.length; i++) {
                if (i % 10 == 0) {
                    offset = border;
                    buf += border + 56;
                }

                int[] start = new int[dr.cols * dr.rows];
                for (int j = 0; j < start.length; j++)
                    start[j] = rbm.weights[i].get(j) > 0 ? (Math.round(rbm.weights[i].get(j) * 255)) << 8 : ((Math.round(Math.abs(rbm.weights[i].get(j)) * 255)) << 16);

                BufferedImage out = new BufferedImage(dr.cols, dr.rows, BufferedImage.TYPE_INT_RGB);

                r = out.getRaster();
                r.setDataElements(0, 0, dr.cols, dr.rows, start);

                //Resize
                BufferedImage newImage = new BufferedImage(56, 56, BufferedImage.TYPE_INT_RGB);

                Graphics2D g2 = newImage.createGraphics();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                    g2.clearRect(0, 0, 56, 56);
                    g2.drawImage(out, 0, 0, 56, 56, null);
                } finally {
                    g2.dispose();
                }
                g.drawImage(newImage, buf, offset, null);

                offset += border + dr.rows * 2;
            }
        }
    }

    public static void start(File labels,File images) {
        JFrame frame = new JFrame("MINST Draw");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        BinaryMinstRBM cnvs = new BinaryMinstRBM(labels, images);


        cnvs.setSize(1024, 768);
        frame.add(cnvs);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);


        while (true) {
            cnvs.update();
            try {
                count++;

                if (count > 1000)
                    Thread.sleep(2000);

            } catch (InterruptedException e) {
            }
        }
    }
}
