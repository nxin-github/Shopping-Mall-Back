import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
* @Author：王木风
* @date 2021/8/17 13:52
* @description：
*/public class A {
    public static void main(String[] args) {
        String str="1234";
        changeStr(str);
        System.out.println(str);
    }
    public static void changeStr(String str){ str="welcome"; }
}
