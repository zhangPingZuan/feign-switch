package io.github.feign.entity;

import com.intellij.psi.PsiAnnotation;
import io.github.feign.util.PathUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class ConcreteControllerMethod implements MappingMethod {

    private String mappingPath;

    private String qualifiedAnnotationName;

    private ConcreteController concreteController;

    @Override
    public String getSuperMappingPath() {
        return concreteController.getMappingPath();
    }

    @Override
    public String getFullMappingPath() {
        if (this.getConcreteController() == null) return this.getMappingPath();
        return PathUtil.constructUrlPath(this.concreteController.getMappingPath(), this.getMappingPath());
    }


    public String getMappingPath() {
        return PathUtil.constructUrlPath(this.mappingPath);
    }

    public ConcreteControllerMethod(PsiAnnotation psiClassAnnotation, PsiAnnotation psiMethodAnnotation) {
        this.mappingPath = PathUtil.extractUrlPath(psiMethodAnnotation);
        this.qualifiedAnnotationName = psiMethodAnnotation.getQualifiedName();
        this.concreteController = new ConcreteController(psiClassAnnotation);
    }


}
