package io.github.feign.util;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiNameValuePair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

@Slf4j
public class PathUtil {

    public static List<String> properties = Arrays.asList("path", "value", "name");

    public static String constructUrlPath(String prefix, String path){
        String ret;
        if (StringUtils.isEmpty(prefix))
            return constructUrlPath(path);
        if (StringUtils.isEmpty(path))
            return constructUrlPath(prefix);
        String tmp = constructUrlPath(prefix);
        ret = (tmp.equals("/") ? "" : tmp) + constructUrlPath(path);
        return ret;
    }

    public static String constructUrlPath(String path) {
        String ret = "";
        if (path == null) return ret;
        ret = StringUtils.startsWith(path, "/") ? path : "/" + path;
        ret = StringUtils.endsWith(path, "/") ? ret.substring(0, ret.length() - 1) : ret;
        return ret;
    }

    public static String extractUrlPath(PsiAnnotation psiAnnotation) {
        if (psiAnnotation == null || psiAnnotation.getParameterList().getAttributes().length == 0) return "";
        Map<String, String> methodPair = Arrays.stream(psiAnnotation.getParameterList().getAttributes()).collect(toMap(PsiNameValuePair::getAttributeName, psiNameValuePair -> StringUtils.isEmpty(psiNameValuePair.getLiteralValue()) ? "" : psiNameValuePair.getLiteralValue()));
        return methodPair.entrySet().stream().filter(e -> properties.contains(e.getKey())).findAny().map(Map.Entry::getValue).orElse("");
    }

    public static String extractServiceName(PsiAnnotation psiAnnotation){
        if (psiAnnotation == null || psiAnnotation.getParameterList().getAttributes().length == 0) return "";
        Map<String, String> classPair = Arrays.stream(psiAnnotation.getParameterList().getAttributes()).collect(toMap(PsiNameValuePair::getAttributeName, psiNameValuePair -> StringUtils.isEmpty(psiNameValuePair.getLiteralValue()) ? "" : psiNameValuePair.getLiteralValue()));
        return classPair.getOrDefault("value", "");

    }

}
