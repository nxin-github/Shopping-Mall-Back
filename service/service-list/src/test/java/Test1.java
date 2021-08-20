/**
 * @Author：王木风
 * @date 2021/8/19 20:43
 * @description：
 */
public class Test1 {
    public static void main(String[] args) {
        int[] arr = {8, 7, 3, -1, 5, -15, -8, 12};
        int[] operate = operate(arr);
        for (int i : operate) {
            System.out.print(i+"、");
        }
    }

    public static int[] operate(int[] arr) {
        int[] copyOprate = new int[arr.length];
        int forword = 0;
        int zero = 0;
        int rear = arr.length - 1;
        boolean flag = false;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] > 0) {
                copyOprate[rear--] = arr[i];
            } else if (arr[i] == 0) {
                copyOprate[zero] = 0;
            } else {
                copyOprate[forword++] = arr[i];
                copyOprate[++zero] = 0;
            }
        }
        return copyOprate;
    }
}
