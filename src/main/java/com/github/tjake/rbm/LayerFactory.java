package com.github.tjake.rbm;


import java.awt.image.BufferedImage;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

public class LayerFactory {
    public static byte[] MAGIC = {(byte) 0xf0, (byte) 0x0d, (byte) 0x00, (byte) 0x0F};

    public Layer create(int size) {
        return new Layer(size);
    }

    public Layer create(float[] start) {
       return new Layer(start);
    }

    public Layer create(BufferedImage img) {
        Layer layer = create(img.getWidth() * img.getHeight());
        int width = 0, height = 0;
        for (int i = 0; i < layer.size(); i++) {
            layer.set(i, img.getData().getSample(width++, height, 0));

            if (width >= img.getWidth()) {
                width = 0;
                height++;
            }
        }

        return layer;
    }


    public void save(Layer layer, DataOutput dataOutput) throws IOException {
        //First write magic #
        dataOutput.write(MAGIC);

        float[] floats = layer.get();
        if (floats.length != layer.size())
            throw new IOException("get().length != size()");

        //Number of elements
        dataOutput.writeInt(layer.size());

        for (int i = 0; i < floats.length; i++)
            dataOutput.writeFloat(floats[i]);
    }

    public Layer load(DataInput dataInput) throws IOException {
        byte[] magic = new byte[4];
        dataInput.readFully(magic);

        if (!Arrays.equals(MAGIC, magic))
            throw new IOException("Bad File Format");

        int size = dataInput.readInt();

        if (size < 0)
            throw new IOException("Invalid size");

        float[] input = new float[size];
        for (int i = 0; i < size; i++)
            input[i] = dataInput.readFloat();


        return create(input);
    }

    public GaussianLayer createGaussian(BufferedImage img) {
        return new GaussianLayer(create(img));
    }

}
