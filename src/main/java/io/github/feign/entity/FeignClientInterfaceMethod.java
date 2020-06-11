package io.github.feign.entity;

import com.intellij.psi.PsiAnnotation;
import io.github.feign.util.PathUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FeignClientInterfaceMethod implements MappingMethod {

    private FeignClientInterface feignClientInterface;

    private String mappingPath;

    private String qualifiedAnnotationName;


    public String getMappingPath() {
        return PathUtil.constructUrlPath(this.mappingPath);
    }

    @Override
    public String getSuperMappingPath() {
        return this.feignClientInterface.getMappingPath();
    }

    @Override
    public String getFullMappingPath() {
        if (this.feignClientInterface == null) return this.getMappingPath();
        return PathUtil.constructUrlPath(this.feignClientInterface.getMappingPath(), this.getMappingPath());
    }

    public FeignClientInterfaceMethod(PsiAnnotation psiClassAnnotation, PsiAnnotation psiMethodAnnotation) {
        this.mappingPath = PathUtil.extractUrlPath(psiMethodAnnotation);
        this.qualifiedAnnotationName = psiMethodAnnotation.getQualifiedName();
        this.feignClientInterface = new FeignClientInterface(psiClassAnnotation);
    }
}
