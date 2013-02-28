package com.github.tjake.rbm;

import java.util.Arrays;

/**
 * Represents a layer in nodes in a Neural network, supports some simple operations
 */
public class Layer {

    final float[] layer;

    public Layer(int size) {
        layer = new float[size];
    }

    public Layer(float[] layer){
        this.layer = layer;
    }

    public void set(int i, float f) {
        layer[i] = f;
    }

    public float get(int i) {
       return layer[i];
    }

    public void add(int i, float f) {
        layer[i] += f;
    }

    public void div(int i, float f) {
        layer[i] /= f;
    }

    public void mult(int i, float f) {
        layer[i] *= f;
    }

    public int size() {
        return layer.length;
    }

    public Layer clone() {
        Layer c = new Layer(layer.length);
        System.arraycopy(layer,0,c.layer,0,layer.length);
        return c;
    }

    public void clear() {
        Arrays.fill(layer,0.0f);
    }

    public void copy(float[] src) {
        System.arraycopy(layer,0,src,0,layer.length);
    }

    public float[] get() {
        return layer;
    }
}
