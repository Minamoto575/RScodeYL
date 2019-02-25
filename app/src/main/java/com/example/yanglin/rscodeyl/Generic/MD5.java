
package com.example.yanglin.rscodeyl.Generic;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yanglin on 2019/2/23.
 */

public class MD5 {
    public String print(String s1){

        String result1=md5Password(s1);
      //  String result2=md5Password(s2);

        String regex="[^0-9]";
        Pattern p= Pattern.compile(regex);

        Matcher m1=p.matcher(result1);
      //  Matcher m2=p.matcher(result2);
        String str =m1.replaceAll("").trim();
                //+","+m2.replaceAll("").trim();
        return str;
    }

    public String md5Password(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("md5");
            byte[] result = digest.digest(password.getBytes());
            StringBuffer buffer = new StringBuffer();
            for(byte b:result) {
                int number=b & 0xff;//加盐
                String string = Integer.toHexString(number);
                if(string.length()==1) {
                    buffer.append("0");
                }
                buffer.append(string);
            }
            return buffer.toString().substring(8,24);
        }catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return " ";
        }
    }
}
