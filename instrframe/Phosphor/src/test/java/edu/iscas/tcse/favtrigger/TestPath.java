package edu.iscas.tcse.favtrigger;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class TestPath {

    public static void main(String[] args) throws IOException {
        // TODO Auto-generated method stub
        String str="/home/gaoyu/evaluation/hadoop-3.2.2/nmdir/nmPrivate/application_1619517212200_0001/container_1619517212200_0001_01_000001/container_1619517212200_0001_01_000001.tokens";
        System.out.println(str.replaceAll("\\d+",""));
        
        String str2="/home/gaoyu/evaluation/hadoop-3.2.2/nmdir/nmPrivate/application_1619517212200_0001/container_1619517212200_0001_01_000004/container_1619517212200_0001_01_000004.tokens";
        System.out.println(str2.replaceAll("\\d+",""));
        
        System.out.println(CompareTwoSTring(str, str2));
        
        FileOutputStream out = new FileOutputStream("add-output@Test", false);
        out.write("34".getBytes());
        out.close();
    }

    public static String CompareTwoSTring(String firsValue, String secondValue)
    {
        String[] Arrayvalue1 = new String[1000];
        String[] Arrayvalue2 = new String[1000];
        String values=null;

        for (int i = 0; i < firsValue.length(); i++)
        {
            Arrayvalue1[i] = firsValue.substring(i, i+1);
            for (int j = 0; j < secondValue.length(); j++)
            {
                Arrayvalue2[j] = secondValue.substring(j, j+1);
                if (Arrayvalue1[i].equals(Arrayvalue2[j]))
                {
                    values = values + Arrayvalue1[i];

                }
            }
        }

        return values;

    }
}
