package com.joney.joneyutil.tools;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串处理相关工具类
 */
public class StringUtils {

    private SpannableString matcherSearchText(int color, String keyword, String strValue) {
        SpannableString spannableString = new SpannableString(strValue);
        // 条件 keyword
        Pattern pattern = Pattern.compile(keyword);
        // 匹配
        Matcher matcher = pattern.matcher(spannableString);
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            // ForegroundColorSpan 需要new 不然也只能是部分变色
            spannableString.setSpan(new ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        // 返回变色处理的结果
        return spannableString;
    }
}
