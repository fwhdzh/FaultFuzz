import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestArgs {
    public static class Father {
        public String s;
        public void test() {
            StackTraceElement[] callStack;
            callStack = Thread.currentThread().getStackTrace();
            List<String> callStackString = new ArrayList<String>();
            for(int i = 0; i < callStack.length; ++i) {
                callStackString.add(callStack[i].toString());
            }
            System.out.println("Father.instance.test():"+callStackString);
        }
        public void test2() {
            StackTraceElement[] callStack;
            callStack = Thread.currentThread().getStackTrace();
            List<String> callStackString = new ArrayList<String>();
            for(int i = 0; i < callStack.length; ++i) {
                callStackString.add(callStack[i].toString());
            }
            System.out.println("Father.instance.test2():"+callStackString);
        }
    }
    
    public static class Son extends Father {
        public void test() {
            StackTraceElement[] callStack;
            callStack = Thread.currentThread().getStackTrace();
            List<String> callStackString = new ArrayList<String>();
            for(int i = 0; i < callStack.length; ++i) {
                callStackString.add(callStack[i].toString());
            }
            System.out.println("Son.instance.test():"+callStackString);
        }
        public void test3() {
            StackTraceElement[] callStack;
            callStack = Thread.currentThread().getStackTrace();
            List<String> callStackString = new ArrayList<String>();
            for(int i = 0; i < callStack.length; ++i) {
                callStackString.add(callStack[i].toString());
            }
            System.out.println("Son.instance.test3():"+callStackString);
        }
    }

    public Integer inter;
    public static List<Son> sons = new ArrayList<Son>();
    public static void main(String[] args) throws IOException {
        Son son = new Son();
        son.s = "asdf";
        sons.add(son);
        Father father = son;
        father.test();
        father.test2();
        son.test();
        son.test2();
        son.test3();
        Father newFather = new Father();
        System.out.println((newFather instanceof Father));
        System.out.println((newFather instanceof Son));
        System.out.println((father instanceof Father));
        System.out.println((father instanceof Son));
        System.out.println((son instanceof Father));
        System.out.println((son instanceof Son));
    }

}
