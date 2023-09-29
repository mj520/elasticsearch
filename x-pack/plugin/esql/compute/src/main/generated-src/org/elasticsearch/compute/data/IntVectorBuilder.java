/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */

package org.elasticsearch.compute.data;

import java.util.Arrays;

/**
 * Builder for {@link IntVector}s that grows as needed.
 * This class is generated. Do not edit it.
 */
final class IntVectorBuilder extends AbstractVectorBuilder implements IntVector.Builder {

    private int[] values;

    IntVectorBuilder(int estimatedSize, BlockFactory blockFactory) {
        super(blockFactory);
        int initialSize = Math.max(estimatedSize, 2);
        adjustBreaker(initialSize);
        values = new int[Math.max(estimatedSize, 2)];
    }

    @Override
    public IntVectorBuilder appendInt(int value) {
        ensureCapacity();
        values[valueCount] = value;
        valueCount++;
        return this;
    }

    @Override
    protected int elementSize() {
        return Integer.BYTES;
    }

    @Override
    protected int valuesLength() {
        return values.length;
    }

    @Override
    protected void growValuesArray(int newSize) {
        values = Arrays.copyOf(values, newSize);
    }

    @Override
    public IntVector build() {
        finish();
        IntVector vector;
        if (valueCount == 1) {
            vector = new ConstantIntVector(values[0], 1, blockFactory);
        } else {
            if (values.length - valueCount > 1024 || valueCount < (values.length / 2)) {
                values = Arrays.copyOf(values, valueCount);
            }
            vector = new IntArrayVector(values, valueCount, blockFactory);
        }
        /*
         * Update the breaker with the actual bytes used.
         * We pass false below even though we've used the bytes. That's weird,
         * but if we break here we will throw away the used memory, letting
         * it be deallocated. The exception will bubble up and the builder will
         * still technically be open, meaning the calling code should close it
         * which will return all used memory to the breaker.
         */
        blockFactory.adjustBreaker(vector.ramBytesUsed() - estimatedBytes, false);
        built();
        return vector;
    }
}
