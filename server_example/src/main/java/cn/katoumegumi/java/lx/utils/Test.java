package cn.katoumegumi.java.lx.utils;

/**
 * @author ws
 */
public class Test {

    public static void main(String[] args) {
        System.out.println(addBinary("100","110010"));
    }


    public static String addBinary(String a, String b) {
        int nextAdd = 0;
        int aLength = a.length();
        int bLength = b.length();
        int maxLength = aLength > bLength?aLength:bLength;
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < maxLength; i++){
            Character c1 = '0',c2 = '0';
            if(i < aLength){
                c1 = a.charAt(aLength - i - 1);
                if(c1 == null){
                    c1 = '0';
                }
            }
            if(i < bLength){
                c2 = b.charAt(bLength - i - 1);
                if(c2 == null){
                    c2 = '0';
                }
            }
            if(c1.equals(c2)){
                if(c1.equals('1')){
                    if(nextAdd > 0){
                        sb.append('1');
                    }else {
                        sb.append('0');
                        nextAdd++;
                    }
                }else {
                    if(nextAdd > 0){
                        if(nextAdd > 1){
                            nextAdd--;
                            sb.append('0');
                        }else {
                            nextAdd--;
                            sb.append('1');
                        }

                    }else {
                        sb.append('0');
                    }
                }
            }else {
                if(nextAdd > 0){
                    if(nextAdd > 1){
                        sb.append('0');
                        nextAdd--;
                    }else {
                        sb.append('0');
                    }

                }else {
                    sb.append('1');
                }
            }
        }
        int k = nextAdd / 2;
        int j = nextAdd % 2;
        for(int i = 0; i < k; i++){
            sb.append('0');
        }

        if(j > 0){
            if(k > 0) {
                sb.append('0');
            }
            sb.append('1');
        }else {
            sb.append('0');
        }
        sb = sb.reverse();
        int local = sb.indexOf("1");
        if(local < 0){
            return "0";
        }
        if(local > 0){
            return sb.substring(local);
        }
        return sb.toString();
    }

}
