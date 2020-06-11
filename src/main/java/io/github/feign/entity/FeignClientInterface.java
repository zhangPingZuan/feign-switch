package io.github.feign.entity;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiNameValuePair;
import io.github.feign.util.PathUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FeignClientInterface {

    private String mappingPath;
    private String serviceName;

    public String getMappingPath() {
        return PathUtil.constructUrlPath(this.mappingPath);
    }

    public FeignClientInterface(PsiAnnotation psiClassAnnotation){
        Map<String, String> classPair = Arrays.stream(psiClassAnnotation.getParameterList().getAttributes()).collect(toMap(PsiNameValuePair::getAttributeName, psiNameValuePair -> StringUtils.isEmpty(psiNameValuePair.getLiteralValue()) ? "" : psiNameValuePair.getLiteralValue()));
        this.mappingPath = classPair.get("path");
        this.serviceName = classPair.get("value");
    }

}
