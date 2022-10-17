public class Test {
    public static void main(String[] args) {
        String a = "adming123&adgmoinew";
        String[] b = a.split("&");
        String c = "ading";
        String[] d = c.split("&");
        System.out.println(a);
        System.out.println(c);
        System.out.println(b[1]+b[0]);
        System.out.println(d[0]);
        int i =0;
        while (d[i] !=null) {
            System.out.println(i);
            i++;
        }
    }
}
