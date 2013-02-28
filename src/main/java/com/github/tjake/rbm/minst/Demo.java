package com.github.tjake.rbm.minst;


import java.io.File;

public class Demo {
    public static void main(String[] args) {

        if (args.length < 2)
            usage("");

        if (args[0].equalsIgnoreCase("rbm")) {
            File labels = new File(args[1]);
            File images = new File(args[2]);


            if (!labels.isFile())
                usage("invalid minst labels file: "+args[1]);

            if (!images.isFile())
                usage("invalid minst images file: "+args[2]);

            BinaryMinstRBM.start(labels,images);
        }
        else if (args[0].equalsIgnoreCase("dbn")) {
            File labels = new File(args[1]);
            File images = new File(args[2]);
            File saveto = new File(args[3]);

            if (!labels.isFile())
                usage("invalid minst labels file: "+args[1]);

            if (!images.isFile())
                usage("invalid minst images file: "+args[2]);

            BinaryMinstDBN.start(labels,images,saveto);
        } else if (args[0].equalsIgnoreCase("gen")) {
            File load = new File(args[1]);

            if (!load.isFile())
                usage("invalid dbn file: "+args[1]);

             GenerativeMinstDBN.start(load);
        }

    }

    private static void usage(String err) {
        System.err.println("Usage: \t[rbm minst-labels.gz minst-images.gz]\n\t [dbn minst-images.gz minst-labels.gz dbn.bin]\n\t [gen dbn.bin]");
        if (err != null && err.length() > 0)
            System.err.println(err);

        System.exit(-1);
    }
}
