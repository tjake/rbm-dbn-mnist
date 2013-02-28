package com.github.tjake.rbm;


/*
 * Converts grayscale intensities to binary values
 */
public class BinaryLayer extends Layer {

    final Layer delegate;


    public BinaryLayer(Layer delegate)
    {
        super(null);
        this.delegate = delegate;

        convertToBinary();
    }

    private void convertToBinary()
    {
        for (int i=0; i<delegate.size(); i++)
        {
            float v = delegate.get(i);
            delegate.set(i, v > 30 ? 1.0f : 0.0f);
        }
    }

    public static float[] fromBinary(Layer delegate) {
        float [] output = new float[delegate.size()];
        for (int i = 0; i < output.length; i++) {
            output[i] = delegate.get(i) * 255.0f;
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
