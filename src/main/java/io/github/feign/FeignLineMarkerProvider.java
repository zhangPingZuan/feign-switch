package io.github.feign;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.AnnotatedElementsSearch;
import io.github.feign.entity.ConcreteControllerMethod;
import io.github.feign.entity.FeignClientInterfaceMethod;
import io.github.feign.entity.MappingMethod;
import io.github.feign.util.PathUtil;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Slf4j
public class FeignLineMarkerProvider extends RelatedItemLineMarkerProvider {
    private static final Icon FeignIconIn = IconLoader.getIcon("/icons/logout-box-line.png"); // 16x16
    private static final Icon FeignIconOut = IconLoader.getIcon("/icons/logout-box-r-line.png"); // 16x16
    private static final String QUALIFIED_FEIGN_NAME = "org.springframework.cloud.openfeign.FeignClient";
    private static final List<String> QUALIFIED_WEB_MAPPINGS = Arrays.asList(
            "org.springframework.web.bind.annotation.RequestMapping",
            "org.springframework.web.bind.annotation.PostMapping",
            "org.springframework.web.bind.annotation.GetMapping",
            "org.springframework.web.bind.annotation.DeleteMapping",
            "org.springframework.web.bind.annotation.PutMapping",
            "org.springframework.web.bind.annotation.PatchMapping");
    private static final List<String> QUALIFIED_WEB_IMPORTS = Arrays.asList(
            "io.creams.actuator.util.PathRestController",
            "org.springframework.web.bind.annotation.RestController",
            "org.springframework.web.bind.annotation.Controller");

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo> result) {

//        test(element);

        if (!(element instanceof PsiMethod)) return;
        PsiMethod psiMethod = (PsiMethod) element;

        // 如果为null 直接返回
        PsiClass root = (PsiClass) psiMethod.getContext();
        if (root == null) return;

        // 如果没有mapping注解 直接返回
        if (QUALIFIED_WEB_MAPPINGS.stream().noneMatch(psiMethod::hasAnnotation)) return;
        PsiAnnotation psiClassAnnotation = root.getAnnotation(QUALIFIED_FEIGN_NAME);

        // psiClassAnnotation 不为空， 说明是feign method
        if (psiClassAnnotation != null) {
            MappingMethod clientMethod = constructMappingMethod(psiMethod, false);
            if (clientMethod == null) return;

            // find the corresponding method of feign method
            PsiMethod searchResult = searchPsiMethodInProject(psiMethod, clientMethod, PathUtil.extractServiceName(psiClassAnnotation));

            // construct RelatedItemLineMarkerInfo
            if (searchResult != null) {
                NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder.create(FeignIconIn)
                        .setTargets(searchResult)
                        .setTooltipText("Navigate to service concrete mapping");
                result.add(builder.createLineMarkerInfo(psiMethod));
            }
        } else {
            PsiAnnotation psiAnnotation = QUALIFIED_WEB_MAPPINGS.stream().filter(psiMethod::hasAnnotation).map(psiMethod::getAnnotation).findAny().orElse(null);
            if (psiAnnotation == null) return;
            MappingMethod mappingMethod = constructMappingMethod(psiMethod, true);
            if (mappingMethod == null) return;
            PsiMethod[] psiMethods = searchPsiMethodsInProject(psiMethod, mappingMethod.getFullMappingPath(), mappingMethod.getQualifiedAnnotationName());
            if (psiMethods.length > 0) {
                NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder.create(FeignIconOut)
                        .setTargets(psiMethods)
                        .setTooltipText("Navigate to feign client mapping");
                result.add(builder.createLineMarkerInfo(psiMethod));
            }
        }
    }

    private void test(PsiElement element) {
        if (!(element instanceof PsiMethod)) return;
        Project project = element.getProject();
        PsiClass psiAnnotationClass = JavaPsiFacade.getInstance(project).findClass("org.springframework.web.bind.annotation.PostMapping", GlobalSearchScope.allScope(project));
        if (psiAnnotationClass == null) return;
        AnnotatedElementsSearch.searchPsiMethods(psiAnnotationClass, GlobalSearchScope.allScope(project));
        log.info("xxx");
    }

    // find all methods which match qualifiedPath in project
    private PsiMethod searchPsiMethodInProject(PsiElement psiElement, MappingMethod clientMethod, String serviceName) {
        // get project
        Project project = psiElement.getProject();
        // 查到对应的module
        PsiFile[] yamlFiles = FilenameIndex.getFilesByName(project, "bootstrap.yml", GlobalSearchScope.allScope(project));
        VirtualFile virtualFile = Arrays.stream(yamlFiles).filter(y -> y.getText().contains(serviceName)).findAny().map(PsiFile::getVirtualFile).orElse(null);
        if (virtualFile == null) return null;
        Module module = ProjectRootManager.getInstance(project).getFileIndex().getModuleForFile(virtualFile);
        if (module == null) return null;

        PsiClass psiAnnotationClass = JavaPsiFacade.getInstance(project).findClass(clientMethod.getQualifiedAnnotationName(), GlobalSearchScope.allScope(project));
        if (psiAnnotationClass == null) return null;
        return AnnotatedElementsSearch.searchPsiMethods(psiAnnotationClass, module.getModuleScope())
                .filtering(m -> {
                    MappingMethod method = constructMappingMethod(m, true);
                    if (method == null) return false;
                    return method.getFullMappingPath().equals(clientMethod.getFullMappingPath());
                }).findFirst();
    }

    private PsiMethod[] searchPsiMethodsInProject(PsiMethod psiMethod, String fullMappingPath, String qualifiedAnnotationName) {
        // get project
        Project project = psiMethod.getProject();
        PsiClass psiAnnotationClass = JavaPsiFacade.getInstance(project).findClass(qualifiedAnnotationName, GlobalSearchScope.allScope(project));
        if (psiAnnotationClass == null) return null;
        return AnnotatedElementsSearch.searchPsiMethods(psiAnnotationClass, GlobalSearchScope.allScope(project))
                .filtering(m -> {
                    MappingMethod method = constructMappingMethod(m, false);
                    if (method == null) return false;
                    return method.getFullMappingPath().equals(fullMappingPath);
                }).findAll().toArray(PsiMethod[]::new);
    }

    /**
     * @Param PsiMethod psiMethod
     * @Param boolean concreteOrInterface 具体服务 或者 feign接口
     * @Return MappingMethod
     */
    private MappingMethod constructMappingMethod(PsiMethod psiMethod, boolean concreteOrInterface) {
        PsiClass root = (PsiClass) psiMethod.getContext();
        if (root == null || (!concreteOrInterface && !root.hasAnnotation(QUALIFIED_FEIGN_NAME))) return null;
        // 获取root上的注解
        PsiAnnotation psiClassAnnotation = concreteOrInterface
                ? (root.hasAnnotation(QUALIFIED_WEB_IMPORTS.get(0)) ? root.getAnnotation(QUALIFIED_WEB_IMPORTS.get(0)) : root.getAnnotation(QUALIFIED_WEB_MAPPINGS.get(0)))
                : root.getAnnotation(QUALIFIED_FEIGN_NAME);

        // 获取method上的注解
        PsiAnnotation psiMethodAnnotation = QUALIFIED_WEB_MAPPINGS.stream().filter(psiMethod::hasAnnotation).map(psiMethod::getAnnotation).findAny().orElse(null);
        if (psiMethodAnnotation == null) return null;
        return concreteOrInterface ? new ConcreteControllerMethod(psiClassAnnotation, psiMethodAnnotation) : new FeignClientInterfaceMethod(psiClassAnnotation, psiMethodAnnotation);
    }
}
