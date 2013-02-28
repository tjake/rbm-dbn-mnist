package com.github.tjake.util;


import com.github.tjake.rbm.Layer;
import java.util.Random;

public class Utilities {

    static Random staticRand = new Random();

    public static float mean(final Layer input)
    {
        float m = 0.0f;
        for (int i=0; i<input.size(); i++)
            m += input.get(i);

        return m/input.size();
    }

    public static float stddev(final Layer input, float mean)
    {
        double sum = 0.0f;
        for (int i=0; i<input.size(); i++)
            sum += Math.pow(input.get(i) - mean, 2);

        return (float)Math.sqrt(sum/(input.size() - 1));
    }

    public static float sigmoid(float x)
    {
        return (float) (1.0f / (1.0f + Math.exp(-x)));
    }

    public static Layer bernoulli(Layer input)
    {
        Layer output = input.clone();
        //using uniform distribution, filter out all negative values
        //from inputs, keeping mostly strong weights
        for (int i=0; i<output.size(); i++)
            output.set(i, staticRand.nextFloat() < input.get(i) ? 1.0f : 0.0f);

        return output;
    }

}
