package com.github.tjake.rbm.minst;


import com.github.tjake.rbm.*;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.*;
import java.util.Iterator;

public class GenerativeMinstDBN extends Canvas {

    final StackedRBM rbm;

    int count = 0;
    Layer input;
    String label;

    public GenerativeMinstDBN(StackedRBM rbm) {
        super();
        this.rbm = rbm;
    }

    public void update() {

        synchronized (rbm) {

            int current = count++ % 10;
            label = String.valueOf(current);


            SimpleRBM r = rbm.getInnerRBMs().get(rbm.getInnerRBMs().size() - 1);

            input = new Layer(r.biasVisible.size());

            //setup input
            for (int i = 0; i < input.size()-10; i++)
                input.set(i, 0.0f);

            //Position == Digit
            input.set(input.size() - 10 + current, 100000.0f);


            Iterator<Tuple> it = r.iterator(input);

            for (int i = 0; i < 1; i++)
                it.next();

            input = it.next().visible;

            for (int i = rbm.getInnerRBMs().size() - 2; i >= 0; i--) {
                SimpleRBM prevRbm = rbm.getInnerRBMs().get(i);

                if (input.size() > prevRbm.biasHidden.size()) {
                    float[] newInput = new float[prevRbm.biasHidden.size()];
                    System.arraycopy(input.get(), 0, newInput, 0, newInput.length);
                    input = new Layer(newInput);
                }

                input = prevRbm.activateVisible(input,null);
            }
        }
        repaint();
    }


    public void paint(Graphics g) {

        synchronized (rbm) {

            BufferedImage in = new BufferedImage(28, 28, BufferedImage.TYPE_INT_RGB);

            int draw[] = new int[input.size()];
            for (int i = 0; i < input.size(); i++)
                draw[i] = Math.round(input.get(i) * 255f);

            WritableRaster r = in.getRaster();
            r.setDataElements(0, 0, 28, 28, draw);

            //Resize
            BufferedImage newImage = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);

            Graphics2D g2 = newImage.createGraphics();
            try {
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2.clearRect(0, 0, 256, 256);
                g2.drawImage(in, 0, 0, 256, 256, null);
            } finally {
                g2.dispose();
            }

            g.drawImage(newImage, 10, 10, null);


            g.drawString("Generative version of: "+label, 10, 300);
        }
    }


    public static void start(File stateFile) {

        GenerativeMinstDBN m;

        try {
            DataInput input = new DataInputStream( new BufferedInputStream(new FileInputStream(stateFile)));
            StackedRBM rbm = new StackedRBM();
            rbm.load(input, new LayerFactory());
            m = new GenerativeMinstDBN(rbm);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        JFrame frame = new JFrame("MINST Generative Draw");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(310, 310);


        m.setSize(310, 310);
        frame.add(m);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        while (true) {
            m.update();
            try {
                //if (count > 1000)
                Thread.sleep(2000);

            } catch (InterruptedException e) {
            }
        }

    }
}