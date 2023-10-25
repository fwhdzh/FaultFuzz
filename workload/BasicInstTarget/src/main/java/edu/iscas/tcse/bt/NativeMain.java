package edu.iscas.tcse.bt;

public class NativeMain {

    static
    {
        System.load("/home/fengwenhan/data/phosphor_test/test_out/Hello.so");
    }

    public native static void Hello();

    public static void main(String[] args)
    {
        Hello();
    }

}
