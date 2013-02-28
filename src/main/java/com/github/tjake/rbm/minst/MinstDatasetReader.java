package com.github.tjake.rbm.minst;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOError;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * Reads the Minst image data from
 */
public class MinstDatasetReader implements Enumeration<MinstItem>
{
    final DataInputStream labelsBuf;
    final DataInputStream imagesBuf;

    SecureRandom r = new SecureRandom();

    final Map<String, List<MinstItem>> trainingSet = new HashMap<String, List<MinstItem>>();
    final Map<String, List<MinstItem>> testSet = new HashMap<String, List<MinstItem>>();

    int rows = 0;
    int cols = 0;
    int count = 0;
    int current = 0;

    public MinstDatasetReader(File labelsFile, File imagesFile)
    {
        try
        {
            labelsBuf = new DataInputStream(new GZIPInputStream(new FileInputStream(labelsFile)));
            imagesBuf = new DataInputStream(new GZIPInputStream(new FileInputStream(imagesFile)));

            verify();

            createTrainingSet();
        }
        catch (FileNotFoundException e)
        {
            throw new IOError(e);
        }
        catch (IOException e)
        {
            throw new IOError(e);
        }
        finally
        {


        }



    }

    public void createTrainingSet() {
        boolean done = false;

        while (!done || !hasMoreElements()) {
            MinstItem i = nextElement();

            if (r.nextDouble() > 0.3) {
                List<MinstItem> l = testSet.get(i.label);
                if (l == null)
                    l = new ArrayList<MinstItem>();
                testSet.put(i.label, l);

                l.add(i);
            } else {
                List<MinstItem> l = trainingSet.get(i.label);
                if (l == null)
                    l = new ArrayList<MinstItem>();
                trainingSet.put(i.label, l);

                l.add(i);
            }

            if (trainingSet.isEmpty())
                continue;

            boolean isDone = true;
            for (Map.Entry<String, List<MinstItem>> entry : trainingSet.entrySet()) {
                if (entry.getValue().size() < 100) {
                    isDone = false;
                    break;
                }
            }

            done = isDone;
        }
    }

    public MinstItem getTestItem()
    {
        List<MinstItem> list = testSet.get(String.valueOf(r.nextInt(10)));
        return list.get(r.nextInt(list.size()));

    }

    public MinstItem getTrainingItem()
    {
        List<MinstItem> list = trainingSet.get(String.valueOf(r.nextInt(10)));
        return list.get(r.nextInt(list.size()));

    }

    public MinstItem getTrainingItem(int i)
    {
        List<MinstItem> list = trainingSet.get(String.valueOf(i));
        return list.get(r.nextInt(list.size()));

    }

    private void verify() throws IOException
    {
        int magic = labelsBuf.readInt();
        int labelCount = labelsBuf.readInt();

        System.err.println("Labels magic=" + magic + ", count=" + labelCount);

        magic = imagesBuf.readInt();
        int imageCount = imagesBuf.readInt();
        rows = imagesBuf.readInt();
        cols = imagesBuf.readInt();

        System.err.println("Images magic=" + magic + " count=" + imageCount + " rows=" + rows + " cols=" + cols);

        if (labelCount != imageCount)
            throw new IOException("Label Image count mismatch");

        count = imageCount;
    }

    public boolean hasMoreElements()
    {
        return current < count;
    }

    public MinstItem nextElement()
    {
        MinstItem m = new MinstItem();

        try
        {
            m.label = String.valueOf(labelsBuf.readUnsignedByte());
            m.data = new int[rows * cols];

            for (int i = 0; i < m.data.length; i++)
            {
                m.data[i] = imagesBuf.readUnsignedByte();
            }

            return m;
        }
        catch (IOException e)
        {
            current = count;
            throw new IOError(e);
        }
        finally
        {
            current++;
        }

    }
}
