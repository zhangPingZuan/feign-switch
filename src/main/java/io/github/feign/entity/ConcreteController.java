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
public class ConcreteController {

    private String mappingPath;

    public String getMappingPath() {
        return PathUtil.constructUrlPath(this.mappingPath);
    }

    public ConcreteController(PsiAnnotation psiClassAnnotation) {
        if (psiClassAnnotation != null) {
            this.mappingPath = PathUtil.extractUrlPath(psiClassAnnotation);
        }
    }

}
