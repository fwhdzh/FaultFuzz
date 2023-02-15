package edu.iscas.CCrashFuzzer;

import java.util.Arrays;

public class BitArray {
	private static final int ALL_ONES = 0xFFFFFFFF;
    public static int WORD_SIZE = 64;
    public int[] bits = null;
    public byte[] data = null;

    public BitArray(int size) {
//        bits = new int[size / WORD_SIZE + (size % WORD_SIZE == 0 ? 0 : 1)];
        data = new byte[size / 8 + (size % 8 == 0 ? 0 : 1)];
        Arrays.fill(data, (byte)0);
    }

    public boolean getIntBit(int pos) {
        return (bits[pos / WORD_SIZE] & (1 << (pos % WORD_SIZE))) != 0;
    }

    public void setIntBit(int pos, boolean b) {
        int word = bits[pos / WORD_SIZE];
        int posBit = 1 << (pos % WORD_SIZE);
        if (b) {
            word |= posBit;
        } else {
            word &= (ALL_ONES - posBit);
        }
        bits[pos / WORD_SIZE] = word;
    }

    public void setBit(int pos, boolean b) {
    	int posByte = pos/8;
        int posBit = pos%8;
        byte blockMark = data[posByte];
        if (b) {
        	blockMark |= (1 << posBit);
        } else {
        	blockMark &= ~(1 << posBit);
        }
        data[posByte] = blockMark;
    }

    public boolean getBit(int pos) {
    	return (data[pos / 8] & (1 << (pos % 8))) != 0;
    }
}
