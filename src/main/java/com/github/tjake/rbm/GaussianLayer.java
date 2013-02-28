package com.github.tjake.rbm;


import com.github.tjake.util.Utilities;

/**
 * Converts raw values to standard deviations
 */
public class GaussianLayer extends Layer{

    final Layer delegate;
    private float mean;
    private float stddev;


    public GaussianLayer(Layer delegate)
    {
        super(null);
        this.delegate = delegate;

        convertToStddev();

    }

    public GaussianLayer(Layer delegate, Layer base)
    {
        super(null);

        GaussianLayer gbase = (GaussianLayer) base;

        this.delegate = delegate;
        mean = gbase.mean;
        stddev = gbase.stddev;

    }

    private void convertToStddev()
    {
        mean = Utilities.mean(delegate);
        stddev = Utilities.stddev(delegate, mean);
        stddev = stddev < 0.1f ? 0.1f : stddev;

        double min = Double.MAX_VALUE, max = Double.MIN_VALUE;


        for (int i=0; i<delegate.size(); i++)
        {
            double v = (delegate.get(i) - mean)/stddev;
            if (v > max) max = v;
            if (v < min) min = v;

            delegate.set(i, (float)v);
        }


    }

    public float[] fromGaussian() {
        double min = Double.MAX_VALUE, max = Double.MIN_VALUE;
        float [] output = new float[delegate.size()];
        for (int i = 0; i < output.length; i++) {
            double v =  delegate.get(i);

            //Squash > 2 sigma
            if (Math.abs(v) > 2)
                v /= 2;

            v  = v * stddev + mean;

            if (v > max) max = v;
            if (v < min) min = v;

            output[i] = (float)(v < 0 ? 0 : v);
            output[i] = (float)(v > 255 ? 255 : v);
        }


        return output;
    }


    @Override
    public void set(int i, float f) {
        delegate.set(i,f);
    }

    @Override
    public float get(int i) {
        return delegate.get(i);
    }

    @Override
    public void add(int i, float f) {
        delegate.add(i,f);
    }

    @Override
    public void div(int i, float f) {
        delegate.div(i,f);
    }

    @Override
    public void mult(int i, float f) {
        delegate.div(i,f);
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public Layer clone() {
        return delegate.clone();
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public void copy(float[] src) {
        delegate.copy(src);
    }

    @Override
    public float[] get() {
        return delegate.get();
    }
}
