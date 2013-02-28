package com.github.tjake.rbm;

public  class Tuple
{
    public final Layer visible;
    public final Layer hidden;
    public final Layer input;   //For a DBN this is the initial input layer

    protected Tuple(Layer input, Layer visible, Layer hidden)
    {
        this.input = input;
        this.visible = visible;
        this.hidden = hidden;
    }

    public static class Factory {

        public final Layer input;

        public Factory(Layer input) {
            this.input = input;
        }

        public Tuple create(Layer visible, Layer hidden)
        {
            return new Tuple(input,visible,hidden);
        }
    }
}
